"""
SurakshaScan — Live Threat Feed Sync
=====================================

Pulls recent verified-malicious URLs from URLhaus (abuse.ch) and PhishTank,
and upserts them into the `community_flags` table so the app's link-checker
gets real, current blocklist coverage instead of a static snapshot.

Run modes:
    python threat_feed_sync.py --source urlhaus
    python threat_feed_sync.py --source phishtank
    python threat_feed_sync.py --source all          (default)

Intended to run on a schedule (cron/systemd timer), e.g. hourly:
    0 * * * *  /usr/bin/python3 /path/to/threat_feed_sync.py --source all >> /var/log/surakshascan_sync.log 2>&1

Environment variables:
    SURAKSHA_DB_HOST / PORT / USER / PASSWORD / NAME   -- see db_config.py
    PHISHTANK_APP_KEY   optional; without it PhishTank's public feed is
                        rate-limited (a handful of requests/day), so an
                        API key (free, from phishtank.org) is recommended
                        for anything more frequent than daily.

Notes / limits (read before relying on this for production):
    * URLhaus's "recent" endpoint returns roughly the last ~1000 entries
      each call across ALL countries/threats — this script does not
      filter by India specifically, since URLhaus has no such filter.
      Feel free to add a keyword filter (e.g. matching Indian bank/UPI
      brand names) via --keyword-filter if you want to cut noise.
    * PhishTank's online-valid.json feed is large; this script only
      reads entries added since the last sync (tracked in a local
      state file) to avoid reprocessing everything each run.
    * This script only WRITES to community_flags. It does not delete
      or expire old entries — pair it with a periodic cleanup job if
      you want flags to age out.
"""

import argparse
import json
import os
import sys
import time
from datetime import datetime, timezone
from urllib.parse import urlparse

import requests

from db_config import get_connection

URLHAUS_RECENT_ENDPOINT = "https://urlhaus-api.abuse.ch/v1/urls/recent/"
PHISHTANK_FEED_URL = "https://data.phishtank.com/data/{key}online-valid.json"
STATE_FILE = os.path.join(os.path.dirname(__file__), ".sync_state.json")

REQUEST_TIMEOUT = 20


def load_state():
    if os.path.exists(STATE_FILE):
        with open(STATE_FILE) as f:
            return json.load(f)
    return {"last_phishtank_sync": None, "last_urlhaus_sync": None}


def save_state(state):
    with open(STATE_FILE, "w") as f:
        json.dump(state, f, indent=2)


def upsert_flag(cursor, flagged_value, initial_count=1):
    """Insert a new flag, or bump report_count if it already exists."""
    cursor.execute(
        """
        INSERT INTO community_flags (flagged_value, report_count, status)
        VALUES (%s, %s, 'active')
        ON DUPLICATE KEY UPDATE
            report_count = report_count + VALUES(report_count),
            status = 'active'
        """,
        (flagged_value, initial_count),
    )


def domain_from_url(url):
    try:
        host = urlparse(url).netloc
        return host.split(":")[0].lower() if host else None
    except Exception:
        return None


def sync_urlhaus(conn, keyword_filter=None):
    print("[urlhaus] fetching recent URLs...")
    try:
        resp = requests.get(URLHAUS_RECENT_ENDPOINT, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
    except requests.RequestException as e:
        print(f"[urlhaus] ERROR: request failed: {e}", file=sys.stderr)
        return 0
    except ValueError:
        print("[urlhaus] ERROR: response was not valid JSON", file=sys.stderr)
        return 0

    if data.get("query_status") != "ok":
        print(f"[urlhaus] ERROR: query_status={data.get('query_status')}", file=sys.stderr)
        return 0

    entries = data.get("urls", [])
    inserted = 0
    cursor = conn.cursor()
    for entry in entries:
        url = entry.get("url")
        host = entry.get("host") or domain_from_url(url)
        if not host:
            continue
        if keyword_filter and not any(kw.lower() in url.lower() for kw in keyword_filter):
            continue
        upsert_flag(cursor, host)
        inserted += 1
    conn.commit()
    cursor.close()
    print(f"[urlhaus] processed {len(entries)} entries, flagged {inserted} host(s)")
    return inserted


def sync_phishtank(conn, keyword_filter=None):
    app_key = os.environ.get("PHISHTANK_APP_KEY", "")
    key_path_segment = f"{app_key}/" if app_key else ""
    url = PHISHTANK_FEED_URL.format(key=key_path_segment)

    print("[phishtank] fetching verified phishing feed...")
    try:
        resp = requests.get(
            url,
            timeout=REQUEST_TIMEOUT,
            headers={"User-Agent": "phishtank/surakshascan"},
        )
        resp.raise_for_status()
        entries = resp.json()
    except requests.RequestException as e:
        print(f"[phishtank] ERROR: request failed: {e}", file=sys.stderr)
        return 0
    except ValueError:
        print(
            "[phishtank] ERROR: response was not valid JSON "
            "(you may be rate-limited without an API key)",
            file=sys.stderr,
        )
        return 0

    state = load_state()
    last_sync = state.get("last_phishtank_sync")
    last_sync_dt = datetime.fromisoformat(last_sync) if last_sync else None

    inserted = 0
    newest_seen = last_sync_dt
    cursor = conn.cursor()
    for entry in entries:
        if not entry.get("verified") == "yes":
            continue
        submitted = entry.get("submission_time")
        try:
            submitted_dt = datetime.fromisoformat(submitted.replace("Z", "+00:00"))
        except (ValueError, AttributeError, TypeError):
            submitted_dt = None

        if last_sync_dt and submitted_dt and submitted_dt <= last_sync_dt:
            continue

        target_url = entry.get("url")
        host = domain_from_url(target_url)
        if not host:
            continue
        if keyword_filter and not any(kw.lower() in target_url.lower() for kw in keyword_filter):
            continue

        upsert_flag(cursor, host)
        inserted += 1
        if submitted_dt and (newest_seen is None or submitted_dt > newest_seen):
            newest_seen = submitted_dt

    conn.commit()
    cursor.close()

    state["last_phishtank_sync"] = (newest_seen or datetime.now(timezone.utc)).isoformat()
    save_state(state)

    print(f"[phishtank] processed {len(entries)} entries, flagged {inserted} host(s)")
    return inserted


def main():
    parser = argparse.ArgumentParser(description="Sync URLhaus/PhishTank into SurakshaScan community_flags")
    parser.add_argument("--source", choices=["urlhaus", "phishtank", "all"], default="all")
    parser.add_argument(
        "--keyword-filter",
        nargs="*",
        default=None,
        help="Only flag entries whose URL contains one of these keywords "
             "(e.g. --keyword-filter sbi hdfc icici upi paytm) to focus on India-relevant hits.",
    )
    args = parser.parse_args()

    conn = get_connection()
    total = 0
    try:
        if args.source in ("urlhaus", "all"):
            total += sync_urlhaus(conn, args.keyword_filter)
        if args.source in ("phishtank", "all"):
            total += sync_phishtank(conn, args.keyword_filter)
    finally:
        conn.close()

    print(f"[sync] done at {datetime.now().isoformat()} — {total} host(s) flagged/updated")


if __name__ == "__main__":
    main()