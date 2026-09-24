# SurakshaScan â€” A Unified Scam Detection and Awareness Platform

SurakshaScan is a proactive scam-detection and awareness platform. It aims to help users identify suspicious SMS texts, UPI IDs, URLs/links, and calls.

## Technology Stack
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Backend**: Java Servlets, JDBC
- **Database**: MySQL

## Architecture
Three-tier architecture:
1. **Client** â€” HTML/CSS/JavaScript communicating via Fetch API
2. **Server** â€” Java Servlets returning JSON responses
3. **Database** â€” MySQL

## Project Structure
- `frontend/` - Contains all client-side code (HTML, CSS, JS)
- `backend/` - Contains the Java Servlet application (Maven structure)
- `database/` - Contains the database schema SQL scripts

## Authentication (Step 3)
The application provides session-based authentication using HTTP-only cookies.
Passwords are mathematically hashed using PBKDF2WithHmacSHA256 + unique salts.

### Endpoints
* POST /api/auth/register
  * Body: {"name":"...", "email":"...", "password":"...", "role":"normal|guardian"}
  * Registers a new user. 
* POST /api/auth/login
  * Body: {"email":"...", "password":"..."}
  * Verifies credentials and sets a session cookie.
* POST /api/auth/logout
  * Destroys the active session.
* GET /api/auth/me
  * Returns the current authenticated user identity (no password information).

Protected API routes (/api/user/*, /api/scan/*, etc.) require an active session and are protected by AuthenticationFilter.

## Core Scanner (Step 4)
An explainable scam detection pipeline for SMS, UPI IDs, and Links.

### Details
* POST /api/scan: The authenticated scanner API. Expects JSON { inputType, content }.
* **Risk Engine**: Normalizes input and applies egex and keyword matching from scam_patterns. Sums risk scores dynamically.
* **Community Flags**: Also checks exact domains/IDs against the community_flags table for added risk.
* **Verdict Thresholds**:
  * SAFE: Score < 20
  * SUSPICIOUS: 20 <= Score < 50
  * HIGH RISK: Score >= 50
* **Security**: Input size limits enforced (1000 chars), inputs are safely normalized, and URLs are NEVER visited via outward HTTP requests.
* **Explanations**: All rules matched are returned natively in the API to power an explainable UI. No ML black boxes are used.

## History and Dashboard (Step 5)
Provides an authenticated view into user scan records ensuring absolute data isolation.

### Details
* GET /api/history: Paginated list of scans belonging strictly to the logged-in user.
* GET /api/history/{id}: Detailed view of a single scan. Protects against IDOR by enforcing eport_id = ? AND user_id = ?.
* GET /api/dashboard/stats: Computes actual aggregated stats (Total, Safe, Suspicious, High Risk).
* **Database**: Added indexes to user_reports.user_id and created_at for scalable read performance.
* **UI**: Professional Dashboard and History views built with pure HTML/CSS/JS. No fake metrics are displayed.

## Community Intelligence System (Step 6)
An authenticated, isolated, duplicate-preventing community flag system.

### Details
* POST /api/community/report: Securely logs a user report. Checks community_reports transactionally to strictly prevent duplicate spamming from the same user.
* **Normalization**: All entries (phone, upi, domain) are normalized (e.g. domains extracted from URLs, spacing removed from phones) to prevent fragmenting flags.
* **Scoring Rules**: The RuleEngine queries Community Flags. The community scoring contribution is explicitly capped (10, 20, 30, 40 max) to prevent report counts from unfairly inflating scores to infinity.
* **Data Privacy**: Reporter identity is never exposed. The system only provides aggregated stats (eport_count).
* **Disclaimer**: Clearly indicates that "Community reports are indicators and are not independently verified proof of fraud."

## Awareness Center (Step 7)
A professional educational module teaching users to recognize warning signs before they act.

### Details
* **Educational Content**: Covers SMS, Phishing Links, UPI, OTP, and Impersonation scams. Content focuses on factual warning signs and explicitly labels examples as "Illustrative Example" to prevent panic or confusion.
* **Scan Integration**: Users can click "Try Scanning" on an illustrative example to pre-fill the scan.html utility, reinforcing the connection between education and active defense. Automatic submission is strictly blocked.
* **Interactive Quiz**: Includes a client-side quiz focused on teaching proper responses to threats (e.g., "Do not share your OTP"). It provides immediate feedback without saving metrics or creating fake statistics.
* **Safe DOM APIs**: Content and quiz rendering strictly utilize .textContent and safe sanitization to prevent accidental execution of example malicious payloads.
* **Responsive & Accessible**: Clean, semantic layout adopting SurakshaScan's core UI styling across all device widths.

## Guardian / Family Safety System (Step 8)
Secure in-app guardian alert system with role-enforced family link management.

### Family Link Lifecycle
1. A **guardian** user sends a link request via POST /api/guardian/link/request with the dependent's email.
2. The **dependent** sees pending requests via GET /api/guardian/requests and can accept or reject.
3. Once accepted, the relationship is ctive and appears in GET /api/guardian/links for both sides.
4. Either party may disconnect via DELETE /api/guardian/link/{id}.

### Guardian Alerts
- When a dependent performs a HIGH RISK scan, GuardianService automatically creates a guardian_alerts record for each active guardian.
- The scan always completes successfully even if alert creation fails (logged server-side, not surfaced to user).
- Duplicate alerts are prevented by a UNIQUE constraint: (guardian_id, report_id, alert_type).
- Only the owning guardian can read, view detail, or mark alerts as read.

### Role / Authorization
- POST /api/guardian/link/request: guardian role only (enforced from session, never frontend).
- POST /api/guardian/link/{id}/accept|reject: dependent_id must match authenticated user.
- GET /api/guardian/alerts*: guardian role only.
- DELETE /api/guardian/link/{id}: either guardian_id or dependent_id must match authenticated user.
- Frontend never supplies guardian_id or role claims; backend resolves from server session.

### Alert Privacy
- Guardians see alert metadata: verdict, risk score, title, dependent name, and date.
- No unrelated scan history, community reports, or account credentials are ever exposed.
- 'Guardian alerts are currently in-app alerts. External notification delivery is a future enhancement.'

### Supported Status Values
- pending — request sent, awaiting dependent response
- ctive — both parties connected
- Rejection deletes the pending record (simplest model, avoids unnecessary state expansion)

### Database Tables Added
- guardian_alerts with UNIQUE constraint on (guardian_id, report_id, alert_type) to prevent duplicates.
- Indexes on amily_links.guardian_id, amily_links.dependent_id, guardian_alerts.guardian_id.
