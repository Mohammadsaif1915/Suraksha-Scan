"""
SurakshaScan — DLT Sender-ID Checker
=====================================

Background:
    TRAI's Telecom Commercial Communications Customer Preference
    Regulations (TCCCPR) require every commercial/transactional/service
    SMS sent in India to originate from a DLT-registered "header":
    a 6-character alphanumeric code prefixed by a 2-letter series code,
    e.g.  VM-SBIINB,  AD-HDFCBK,  VK-ICICIB,  JM-IRCTC1

    Genuine bank/government/brand SMS should ALWAYS arrive from such a
    header — never from a plain 10-digit mobile number, and never from
    a header that doesn't match the brand it claims to be.

    This is often a STRONGER fraud signal than parsing message text,
    because it's a structural property fraudsters can't easily fake
    without using a spoofed/leased header (rarer, and itself flaggable
    once seen once) or a plain mobile number (very common in cheap
    scam operations and something a message-body regex alone won't
    catch).

Limitations:
    TRAI/DLT operators (Vilpower, Airtel DLT, Jio, etc.) do not expose
    a free public API to verify a specific header belongs to a specific
    registered entity. So this checker:
      1. Validates the STRUCTURAL format of the sender ID (does it even
         look like a real DLT header, or is it a 10-digit mobile number
         / random alphanumeric string masquerading as one).
      2. Cross-checks against a small maintained registry of known
         header PREFIXES for major Indian banks/brands (registry.py-
         style dict below) — extend this as you collect verified
         examples from real customer SMS.
      3. Flags a HIGH-risk mismatch when the message body claims to be
         from Brand X but the sender ID doesn't match any known header
         for Brand X, or isn't a DLT-format header at all.

Usage:
    from dlt_sender_check import check_sender

    result = check_sender(
        sender_id="AD-9876543210",
        message_text="Your SBI KYC is pending, click http://sbi-kyc.xyz to update"
    )
    print(result)

    # Or from the command line:
    python dlt_sender_check.py "9876543210" "Your SBI account will be blocked..."
"""

import re
import sys

# --- DLT header structural format -----------------------------------------
# Real format: 2-letter series code, hyphen, 6-char alphanumeric brand code.
# e.g. VM-SBIINB, AD-HDFCBK, TX-AMAZNI
DLT_HEADER_RE = re.compile(r"^[A-Z]{2}-[A-Z0-9]{5,6}$", re.IGNORECASE)

# A plain 10-digit Indian mobile number (fraudsters commonly send from these,
# real DLT-registered brand comms never do)
MOBILE_NUMBER_RE = re.compile(r"^[6-9]\d{9}$")

# Short/alphanumeric sender ID with NO hyphen at all (some legacy/telemarketer
# spoofing uses a bare 6-char code with no series prefix)
BARE_ALNUM_RE = re.compile(r"^[A-Z0-9]{5,8}$", re.IGNORECASE)

# --- Known header registry (extend as you verify real examples) -----------
# Maps a brand keyword (as it might appear in message text) to the set of
# DLT header codes (the part after the hyphen) genuinely used by that brand.
# This is necessarily incomplete — TRAI has no public lookup — treat misses
# as "unverified", not automatically "safe".
KNOWN_HEADER_REGISTRY = {
    "sbi": {"SBIINB", "SBIBNK", "SBICRD", "SBIPSG"},
    "hdfc": {"HDFCBK", "HDFCBN"},
    "icici": {"ICICIB", "ICICIT"},
    "axis": {"AXISBK", "AXISBN"},
    "kotak": {"KOTAKB", "KOTAKM"},
    "pnb": {"PNBSMS", "PNBBNK"},
    "paytm": {"PAYTMB", "PYTMPY"},
    "phonepe": {"PHONPE"},
    "amazon": {"AMAZNI", "AMAZON"},
    "flipkart": {"FLPKRT"},
    "irctc": {"IRCTC1", "IRCTCB"},
    "epfo": {"EPFOHO"},
    "incometax": {"ITDEFL", "INCTAX"},
}

# Brand keywords used to detect what the message body CLAIMS to be from,
# so we can cross-check the claim against the sender ID.
BRAND_KEYWORDS = list(KNOWN_HEADER_REGISTRY.keys()) + [
    "cbi", "police", "rbi", "npci", "uidai", "aadhaar", "electricity",
]


def _classify_sender_format(sender_id):
    sender_id = sender_id.strip()
    if MOBILE_NUMBER_RE.match(sender_id):
        return "mobile_number"
    if DLT_HEADER_RE.match(sender_id):
        return "dlt_header"
    if BARE_ALNUM_RE.match(sender_id):
        return "bare_alnum"
    return "unrecognised"


def _extract_claimed_brands(message_text):
    text_lower = message_text.lower()
    return [b for b in BRAND_KEYWORDS if b in text_lower]


def check_sender(sender_id, message_text=""):
    """
    Returns a dict:
        {
            "sender_id": ...,
            "format": "mobile_number" | "dlt_header" | "bare_alnum" | "unrecognised",
            "claimed_brands": [...],
            "risk_score": 0-100,
            "risk_level": "low" | "medium" | "high" | "critical",
            "reasons": [...]
        }
    """
    sender_id = (sender_id or "").strip()
    fmt = _classify_sender_format(sender_id)
    claimed_brands = _extract_claimed_brands(message_text)

    reasons = []
    score = 0

    if fmt == "mobile_number":
        score += 60
        reasons.append(
            "Sender is a plain 10-digit mobile number. TRAI/DLT rules require "
            "commercial and transactional SMS to come from a registered header, "
            "never a personal mobile number."
        )
    elif fmt == "bare_alnum":
        score += 30
        reasons.append(
            "Sender ID has no DLT series prefix (e.g. 'VM-', 'AD-') — format does "
            "not match a standard registered header."
        )
    elif fmt == "unrecognised":
        score += 40
        reasons.append("Sender ID does not match any recognised DLT header or mobile-number pattern.")

    if fmt == "dlt_header":
        header_code = sender_id.split("-", 1)[1].upper()
        for brand in claimed_brands:
            known_codes = KNOWN_HEADER_REGISTRY.get(brand)
            if known_codes and header_code not in known_codes:
                score += 50
                reasons.append(
                    f"Message claims to be from '{brand.upper()}' but header code "
                    f"'{header_code}' does not match any known registered header for that brand."
                )

    if claimed_brands and fmt != "dlt_header":
        score += 20
        reasons.append(
            f"Message claims to represent {', '.join(b.upper() for b in claimed_brands)} "
            "but is not sent from a DLT-format header at all — strong impersonation signal."
        )

    score = min(score, 100)
    if score >= 80:
        level = "critical"
    elif score >= 55:
        level = "high"
    elif score >= 25:
        level = "medium"
    else:
        level = "low"

    if not reasons:
        reasons.append("Sender ID format looks like a standard DLT header; no brand mismatch detected.")

    return {
        "sender_id": sender_id,
        "format": fmt,
        "claimed_brands": claimed_brands,
        "risk_score": score,
        "risk_level": level,
        "reasons": reasons,
    }


def main():
    if len(sys.argv) < 2:
        print("Usage: python dlt_sender_check.py <sender_id> [message_text]")
        sys.exit(1)

    sender_id = sys.argv[1]
    message_text = sys.argv[2] if len(sys.argv) > 2 else ""

    result = check_sender(sender_id, message_text)
    print(f"Sender ID:     {result['sender_id']}")
    print(f"Format:        {result['format']}")
    print(f"Claimed brand: {', '.join(result['claimed_brands']) or '(none detected)'}")
    print(f"Risk score:    {result['risk_score']} ({result['risk_level']})")
    print("Reasons:")
    for r in result["reasons"]:
        print(f"  - {r}")


if __name__ == "__main__":
    main()