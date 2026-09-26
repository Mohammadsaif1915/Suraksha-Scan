"""
Shared MySQL connection helper for SurakshaScan scripts.

Reads connection details from environment variables so credentials
never live in source control:

    SURAKSHA_DB_HOST      default: localhost
    SURAKSHA_DB_PORT      default: 3306
    SURAKSHA_DB_USER      required
    SURAKSHA_DB_PASSWORD  required
    SURAKSHA_DB_NAME      default: surakshascan

Usage:
    from db_config import get_connection
    conn = get_connection()
"""

import os
import sys

try:
    import mysql.connector
except ImportError:
    sys.exit(
        "Missing dependency: mysql-connector-python.\n"
        "Install with: pip install mysql-connector-python --break-system-packages"
    )


def get_connection():
    host = os.environ.get("SURAKSHA_DB_HOST", "localhost")
    port = int(os.environ.get("SURAKSHA_DB_PORT", "3306"))
    user = os.environ.get("SURAKSHA_DB_USER")
    password = os.environ.get("SURAKSHA_DB_PASSWORD")
    database = os.environ.get("SURAKSHA_DB_NAME", "surakshascan")

    if not user or not password:
        sys.exit(
            "SURAKSHA_DB_USER and SURAKSHA_DB_PASSWORD environment variables "
            "must be set before running this script."
        )

    return mysql.connector.connect(
        host=host, port=port, user=user, password=password, database=database
    )