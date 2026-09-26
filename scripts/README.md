# SurakshaScan Scripts

This directory contains auxiliary Python scripts for the SurakshaScan platform.

## Setup

1. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

2. **Environment Variables:**
   The scripts rely on environment variables for database connections and API keys. You must set the following:
   *   `SURAKSHA_DB_USER`: Database username (Required)
   *   `SURAKSHA_DB_PASSWORD`: Database password (Required)
   *   `SURAKSHA_DB_HOST`: Database host (Default: localhost)
   *   `SURAKSHA_DB_PORT`: Database port (Default: 3306)
   *   `SURAKSHA_DB_NAME`: Database name (Default: surakshascan)
   *   `PHISHTANK_APP_KEY`: PhishTank API application key (Optional but recommended to avoid rate limits)

## Scripts

### 1. `threat_feed_sync.py`
Syncs live threat intelligence (malicious URLs) from URLhaus and PhishTank directly into the `community_flags` table of the database.

**Usage:**
```bash
python threat_feed_sync.py --source all --keyword-filter sbi hdfc upi paytm
```

**Automation (Cron Example):**
To run this script hourly, add the following to your crontab (ensure environment variables are sourced or set in the cron job):

```cron
# Run threat feed sync every hour
0 * * * * cd /path/to/SurakshaScan/scripts && SURAKSHA_DB_USER=root SURAKSHA_DB_PASSWORD=yourpass python threat_feed_sync.py --keyword-filter sbi hdfc icici axis paytm >> sync.log 2>&1
```

### 2. `dlt_sender_check.py`
Checks SMS sender IDs against India's TRAI DLT (Distributed Ledger Technology) header rules to identify potential spoofing or unregistered senders.

*Note: The DLT registry within this script is manually maintained as there is no free public TRAI lookup API available.*

**Usage:**
```bash
python dlt_sender_check.py <sender_id> [optional_message_text]
```

**Examples:**
```bash
# Check a suspicious mobile number claiming to be SBI
python dlt_sender_check.py 9876543210 "Your SBI account is blocked."

# Check a legitimate DLT header
python dlt_sender_check.py VM-SBIINB "Your SBI OTP is 123456"
```
