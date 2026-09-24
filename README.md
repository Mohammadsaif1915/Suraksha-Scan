# 🛡️ SurakshaScan

### Unified Scam Detection, Awareness & Community Intelligence Platform

> **Detect suspicious activity. Understand the warning signs. Stay protected.**

SurakshaScan is a proactive **scam detection and cyber-safety platform** designed to help users identify suspicious **SMS messages, UPI IDs, URLs/links, and scam indicators** through an explainable rule-based detection system.

Instead of relying on a black-box AI model, SurakshaScan uses a transparent **risk-scoring engine**, community intelligence, educational resources, scan history, family safety alerts, and an administrative intelligence layer.

The platform is designed around one core principle:

> **Help users recognize suspicious activity before they click, pay, or share sensitive information.**

---

## ✨ Key Features

| Feature                       | Description                                                            |
| ----------------------------- | ---------------------------------------------------------------------- |
| 🔐 Secure Authentication      | Session-based authentication with HTTP-only cookies                    |
| 🧠 Explainable Scam Detection | Rule-based risk scoring with transparent explanations                  |
| 📱 SMS Analysis               | Detect suspicious keywords, patterns and social-engineering indicators |
| 💳 UPI Analysis               | Check suspicious UPI identifiers and community reports                 |
| 🔗 Link Analysis              | Analyze URLs/domains without visiting them                             |
| 📊 Risk Scoring               | SAFE, SUSPICIOUS and HIGH RISK verdicts                                |
| 🗂️ Scan History              | Secure, user-isolated scan records                                     |
| 👥 Community Intelligence     | Aggregate user reports for suspicious identifiers                      |
| 📚 Awareness Center           | Educational scam-prevention content                                    |
| 🧪 Awareness Quiz             | Interactive safety-learning quiz                                       |
| 👨‍👩‍👧 Family Safety        | Guardian-dependent linking system                                      |
| 🚨 Guardian Alerts            | In-app alerts for HIGH RISK dependent scans                            |
| ⚙️ Admin Panel                | Scam rules, users, community and scan management                       |
| 📝 Audit Logging              | Tracks important administrative actions                                |
| 📱 Responsive UI              | Designed for desktop, tablet and mobile                                |

---

# 🏗️ Technology Stack

### Frontend

* **HTML5**
* **CSS3**
* **Vanilla JavaScript**
* **Fetch API**

### Backend

* **Java**
* **Java Servlets**
* **JDBC**
* **Maven**

### Database

* **MySQL**

### Security

* HTTP-only session cookies
* PBKDF2WithHmacSHA256 password hashing
* Unique password salts
* PreparedStatements
* Server-side authorization
* Input validation
* IDOR protection
* Input size limits
* Safe DOM rendering
* No outbound URL requests

---

# 🧩 Architecture

SurakshaScan follows a clean **three-tier architecture**:

```text
┌──────────────────────────────────────────────┐
│                 CLIENT LAYER                 │
│                                              │
│       HTML5 + CSS3 + Vanilla JavaScript     │
│                Fetch API                    │
└──────────────────────┬───────────────────────┘
                       │
                       │ HTTP / JSON
                       ▼
┌──────────────────────────────────────────────┐
│                SERVER LAYER                  │
│                                              │
│ Java Servlets → Services → DAO → JDBC       │
│       Authentication / Authorization         │
│             Risk Engine / Logic              │
└──────────────────────┬───────────────────────┘
                       │
                       │ JDBC
                       ▼
┌──────────────────────────────────────────────┐
│                DATABASE LAYER                │
│                                              │
│                    MySQL                    │
│                                              │
│ Users • Reports • Rules • Community         │
│ Family Links • Alerts • Audit Logs          │
└──────────────────────────────────────────────┘
```

---

# 📁 Project Structure

```text
SurakshaScan/
│
├── frontend/
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── dashboard.html
│   ├── scan.html
│   ├── history.html
│   ├── awareness.html
│   ├── community.html
│   ├── guardian.html
│   ├── admin.html
│   │
│   ├── css/
│   │   ├── style.css
│   │   ├── auth.css
│   │   ├── dashboard.css
│   │   ├── scan.css
│   │   ├── awareness.css
│   │   └── admin.css
│   │
│   └── js/
│       ├── api.js
│       ├── auth.js
│       ├── dashboard.js
│       ├── scan.js
│       ├── history.js
│       ├── awareness.js
│       ├── community.js
│       ├── guardian.js
│       └── admin.js
│
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── ...
│           └── webapp/
│
├── database/
│   ├── schema.sql
│   └── ...
│
└── README.md
```

> The exact filenames may differ depending on the current implementation. The structure above represents the project's functional organization.

---

# 🔐 Authentication & Security

SurakshaScan uses **session-based authentication**.

Sessions are managed using secure HTTP-only cookies.

Passwords are never stored as plain text.

Passwords are hashed using:

```text
PBKDF2WithHmacSHA256
        +
Unique Salt
```

### Authentication Flow

```text
User
 │
 ▼
Register / Login
 │
 ▼
Credentials Verified
 │
 ▼
Password Hash Validation
 │
 ▼
Session Created
 │
 ▼
HTTP-only Session Cookie
 │
 ▼
Protected APIs
```

### Authentication APIs

| Method | Endpoint             | Purpose                |
| ------ | -------------------- | ---------------------- |
| POST   | `/api/auth/register` | Register user          |
| POST   | `/api/auth/login`    | Authenticate user      |
| POST   | `/api/auth/logout`   | Destroy session        |
| GET    | `/api/auth/me`       | Get authenticated user |

Protected APIs require an active server-side session.

---

# 🧠 Core Scam Detection Engine

The core scanner provides an **explainable, rule-based scam detection pipeline**.

Supported input types:

```text
SMS
UPI
LINK
```

The scanner does not use an opaque machine-learning model.

Instead:

```text
User Input
     │
     ▼
Normalization
     │
     ▼
Input Type Detection
     │
     ▼
Scam Pattern Matching
     │
     ├───────────────┐
     ▼               ▼
Keyword Rules    Regex Rules
     │               │
     └───────┬───────┘
             ▼
      Community Flags
             │
             ▼
       Risk Calculation
             │
             ▼
          Verdict
             │
             ▼
      Explanation
```

---

# 🎯 Risk Verdicts

The current scoring thresholds are:

| Risk Score | Verdict       |
| ---------: | ------------- |
|     `< 20` | 🟢 SAFE       |
|  `20 – 49` | 🟡 SUSPICIOUS |
|     `≥ 50` | 🔴 HIGH RISK  |

Example:

```text
Risk Score: 72

Verdict:
HIGH RISK
```

The scanner also returns the matched rules so the user can understand **why** something was flagged.

---

# 🔍 Explainable Detection

Every detection can provide information such as:

```text
Matched Indicator:
Urgent KYC verification request

Risk Contribution:
+20

Category:
SMS

Explanation:
The message creates urgency and requests immediate
account verification.
```

This makes the result understandable rather than simply showing:

> "This is a scam."

---

# 🛡️ Scanner Security

The scanner implements several security protections.

### Input Limits

Maximum input size:

```text
1000 characters
```

### URL Safety

SurakshaScan **does not visit submitted URLs**.

The system analyzes URL/domain information without making outbound HTTP requests.

### Database Safety

All database queries use:

```java
PreparedStatement
```

No SQL query should be constructed through unsafe string concatenation.

---

# 📊 Dashboard & Scan History

Authenticated users have access to their own dashboard and scan history.

### Dashboard

The dashboard provides:

* Total scans
* Safe scans
* Suspicious scans
* High-risk scans
* Recent scans
* Quick access to scanner
* Family safety information where applicable

All statistics are calculated from real database records.

No fake metrics are displayed.

### History API

```http
GET /api/history
```

Returns paginated scans belonging strictly to the authenticated user.

### Scan Details

```http
GET /api/history/{id}
```

The backend verifies:

```text
report_id = requested ID
AND
user_id = authenticated user
```

This prevents **IDOR / Insecure Direct Object Reference** vulnerabilities.

---

# 👥 Community Intelligence

SurakshaScan allows authenticated users to report suspicious identifiers.

Supported types:

```text
📱 Phone Number
💳 UPI ID
🔗 Domain / Link
```

### Reporting Flow

```text
User
 │
 ▼
Submit Suspicious Identifier
 │
 ▼
Normalize Input
 │
 ▼
Check Duplicate Report
 │
 ▼
Create Community Report
 │
 ▼
Update Community Flag
 │
 ▼
Community Intelligence
```

---

# 🔄 Identifier Normalization

Normalization prevents the same identifier from being stored in multiple forms.

Example:

```text
 HTTPS://Example.COM/login
             ↓
        example.com
```

UPI:

```text
 FraudExample@UPI
             ↓
     fraudexample@upi
```

Phone numbers are normalized to prevent formatting differences from fragmenting reports.

---

# 🚫 Duplicate Community Reports

A single user cannot repeatedly increase the community report count for the same normalized identifier.

If a duplicate report is submitted:

```http
409 Conflict
```

Example:

```json
{
  "success": false,
  "message": "You have already reported this item"
}
```

Reporter identities are not exposed through community intelligence.

---

# 📈 Community Risk Contribution

Community reports contribute to the risk score using a capped model.

```text
0 reports       → +0
1–2 reports     → +10
3–4 reports     → +20
5–9 reports     → +30
10+ reports     → +40 maximum
```

The contribution is intentionally capped.

Therefore:

```text
100 reports
1000 reports
10000 reports
```

cannot create an unlimited risk score.

> Community reports are indicators and are not independently verified proof of fraud.

---

# 📚 Awareness Center

SurakshaScan includes an educational **Awareness Center** designed to help users recognize scams before interacting with them.

Covered topics include:

* SMS scams
* Phishing links
* UPI/payment scams
* OTP scams
* Impersonation
* Fake rewards
* KYC/account-warning scams

The educational content focuses on practical warning signs.

---

# 🚨 Common Warning Signs

The Awareness Center teaches users to recognize patterns such as:

* Unexpected urgency
* Threats of account suspension
* Requests for OTP/PIN/password
* Unknown payment requests
* Suspicious links
* Fake rewards
* Impersonation
* Pressure to act immediately
* Requests for confidential information

---

# 🧪 Interactive Awareness Quiz

The Awareness Center also includes a small educational quiz.

The quiz covers practical situations such as:

```text
Unexpected OTP request
        ↓
Do not share OTP
```

```text
Unknown UPI payment request
        ↓
Verify before taking action
```

```text
Urgent suspicious link
        ↓
Do not click
```

The quiz provides immediate feedback.

It does not create fake statistics or unnecessary competitive metrics.

---

# 🔗 Awareness → Scanner Integration

Educational examples can be sent directly to the scanner.

Flow:

```text
Awareness Example
       │
       ▼
"Try Scanning"
       │
       ▼
Scanner
       │
       ▼
Example Pre-filled
       │
       ▼
User clicks Scan
```

The example is **never automatically submitted**.

---

# 👨‍👩‍👧 Guardian / Family Safety

SurakshaScan provides an optional family safety system.

Supported roles:

```text
normal
guardian
admin
```

A guardian can request a connection with another user.

The dependent must approve the request.

---

# 🔗 Family Link Lifecycle

```text
Guardian
   │
   │ Send Request
   ▼
PENDING
   │
   │ Dependent Accepts
   ▼
ACTIVE
   │
   │ Disconnect
   ▼
REMOVED
```

Supported operations include:

```http
POST   /api/guardian/link/request
GET    /api/guardian/requests
POST   /api/guardian/link/{id}/accept
POST   /api/guardian/link/{id}/reject
GET    /api/guardian/links
DELETE /api/guardian/link/{id}
```

Authorization is enforced using the authenticated server-side session.

The frontend never determines the user's actual authorization.

---

# 🚨 Guardian Alerts

When a dependent performs a:

```text
HIGH RISK
```

scan, the system checks active guardian relationships.

If an active guardian exists:

```text
HIGH RISK SCAN
      │
      ▼
GuardianService
      │
      ▼
guardian_alerts
      │
      ▼
Guardian Dashboard
```

The guardian receives an **in-app safety alert**.

The alert contains relevant information such as:

* verdict
* risk score
* dependent name
* alert title
* date/time

Unrelated scan history and credentials are never exposed.

---

# 🔒 Guardian Alert Security

Only the guardian who owns an alert can:

* view it
* open details
* mark it as read

Duplicate alerts are prevented using:

```text
guardian_id
+
report_id
+
alert_type
```

External email/SMS notifications are not currently implemented.

---

# ⚙️ Admin Panel

SurakshaScan includes a server-side authorized administrative system.

The admin role is:

```text
admin
```

Admin authorization is enforced on the backend.

```text
Unauthenticated
      ↓
HTTP 401

Authenticated Non-Admin
      ↓
HTTP 403

Authenticated Admin
      ↓
Access Granted
```

Frontend visibility is only defense-in-depth.

It is **not** the authorization mechanism.

---

# 🛠️ Admin Capabilities

Administrators can manage:

### 🧠 Scam Rules

* View rules
* Create rules
* Update rules
* Enable/disable rules
* Validate regex patterns

### 👥 Users

* Search users
* Filter by role
* View user information
* Change normal ↔ guardian roles

Admin promotion is blocked through the normal role-management flow.

### 🚩 Community

* View community flags
* Review report counts
* Remove invalid flags
* Restore flags

Historical community reports remain preserved.

### 🔎 Scan Activity

* View scans
* Filter by verdict
* Filter by input type
* View scan details

### 📝 Audit Logs

Important administrative actions are recorded.

---

# 🧠 Scam Rule Management

The scanner uses the:

```text
scam_patterns
```

table.

Supported information includes:

* category
* pattern text
* pattern type
* risk weight
* description
* active status

Supported pattern types include:

```text
keyword
regex
```

Regex rules are validated server-side before being stored.

Invalid regex patterns are rejected.

---

# 🔄 Active / Inactive Rules

Rules can be disabled without deleting them.

```text
ACTIVE
  ↓
Used by Scanner

INACTIVE
  ↓
Ignored by Scanner
```

This preserves historical rule context while allowing administrators to update detection behavior.

---

# 🚩 Community Moderation

Community flags support moderation status:

```text
active
removed
```

When a flag is removed:

```text
Scanner
   ↓
Ignores Removed Flag
```

However, historical community report records remain preserved for auditability.

---

# 👤 User Management

Administrators can search users by:

* name
* email

and filter by:

* normal
* guardian
* admin

Sensitive authentication information is never returned.

The system does not expose:

* passwords
* password hashes
* session tokens
* authentication secrets

---

# 📝 Administrative Audit Logging

Important administrative mutations are recorded in:

```text
admin_audit_logs
```

Examples:

```text
RULE_CREATED
RULE_UPDATED
RULE_ENABLED
RULE_DISABLED

COMMUNITY_FLAG_REMOVED
COMMUNITY_FLAG_RESTORED

USER_ROLE_CHANGED
```

Administrative mutations and their audit records are handled transactionally where applicable.

This provides traceability for sensitive administrative actions.

---

# 🗄️ Database Design

The platform uses MySQL for persistent storage.

Major entities include:

```text
users
   │
   ├── user_reports
   │
   ├── community_reports
   │
   └── family_links
            │
            └── guardian_alerts

scam_patterns

community_flags

admin_audit_logs
```

---

# 🔗 Major Relationships

```text
USERS
 │
 ├──────────────► USER_REPORTS
 │
 ├──────────────► COMMUNITY_REPORTS
 │
 ├──────────────► FAMILY_LINKS
 │                       │
 │                       ▼
 │                GUARDIAN_ALERTS
 │
 └──────────────► ADMIN_AUDIT_LOGS

SCAM_PATTERNS
      │
      ▼
  RISK ENGINE

COMMUNITY_FLAGS
      │
      ▼
  RISK ENGINE
```

---

# 🚀 API Overview

## Authentication

| Method | Endpoint             |
| ------ | -------------------- |
| POST   | `/api/auth/register` |
| POST   | `/api/auth/login`    |
| POST   | `/api/auth/logout`   |
| GET    | `/api/auth/me`       |

## Scanner

| Method | Endpoint    |
| ------ | ----------- |
| POST   | `/api/scan` |

## Dashboard / History

| Method | Endpoint               |
| ------ | ---------------------- |
| GET    | `/api/history`         |
| GET    | `/api/history/{id}`    |
| GET    | `/api/dashboard/stats` |

## Community

| Method | Endpoint                |
| ------ | ----------------------- |
| POST   | `/api/community/report` |
| GET    | `/api/community/check`  |

## Guardian

| Method | Endpoint                            |
| ------ | ----------------------------------- |
| POST   | `/api/guardian/link/request`        |
| GET    | `/api/guardian/requests`            |
| POST   | `/api/guardian/link/{id}/accept`    |
| POST   | `/api/guardian/link/{id}/reject`    |
| GET    | `/api/guardian/links`               |
| DELETE | `/api/guardian/link/{id}`           |
| GET    | `/api/guardian/alerts`              |
| GET    | `/api/guardian/alerts/{id}`         |
| POST   | `/api/guardian/alerts/{id}/read`    |
| GET    | `/api/guardian/alerts/unread-count` |

## Admin

| Method | Endpoint                           |
| ------ | ---------------------------------- |
| GET    | `/api/admin/stats`                 |
| GET    | `/api/admin/patterns`              |
| POST   | `/api/admin/patterns`              |
| PUT    | `/api/admin/patterns/{id}`         |
| PATCH  | `/api/admin/patterns/{id}/status`  |
| GET    | `/api/admin/community`             |
| PATCH  | `/api/admin/community/{id}/status` |
| GET    | `/api/admin/users`                 |
| PATCH  | `/api/admin/users/{id}/role`       |
| GET    | `/api/admin/scans`                 |
| GET    | `/api/admin/scans/{id}`            |
| GET    | `/api/admin/audit-logs`            |

---

# 🛡️ Security Principles

SurakshaScan follows a defense-in-depth approach.

### Authentication

Server-side session authentication.

### Authorization

Role and ownership checks happen on the server.

### Password Security

```text
PBKDF2WithHmacSHA256
+
Unique Salt
```

### SQL Injection Protection

All database operations use:

```java
PreparedStatement
```

### IDOR Protection

Ownership is verified using authenticated user IDs.

### Input Validation

Inputs are validated before processing.

### Input Limits

Scanner input is limited to:

```text
1000 characters
```

### URL Safety

Submitted URLs are never visited.

### Frontend Safety

Dynamic data is safely rendered using DOM APIs such as:

```javascript
textContent
```

rather than inserting untrusted API data directly as HTML.

---

# 📱 Responsive Design

The platform is designed for:

```text
Desktop
    ↓
Tablet
    ↓
Mobile
```

Major interfaces are responsive:

* Authentication
* Dashboard
* Scanner
* History
* Awareness
* Community
* Guardian
* Admin

Tables should remain usable on smaller screens through responsive layouts or controlled horizontal scrolling.

---

# 🎨 Design Philosophy

SurakshaScan follows a:

### Professional Cyber-Safety Design Language

Core characteristics:

* Clean layouts
* Strong information hierarchy
* Trustworthy colors
* Clear risk indicators
* Consistent cards
* Accessible controls
* Responsive spacing
* Minimal unnecessary animation

The platform intentionally avoids:

* neon cyberpunk visuals
* excessive gradients
* glassmorphism
* crypto-style interfaces
* unnecessary 3D effects
* fake statistics
* decorative clutter

---

# 🔄 Complete User Flow

```text
                ┌───────────────┐
                │    Landing    │
                └───────┬───────┘
                        │
              ┌─────────▼─────────┐
              │ Register / Login  │
              └─────────┬─────────┘
                        │
                 ┌──────▼──────┐
                 │  Dashboard  │
                 └──────┬──────┘
                        │
       ┌────────────────┼────────────────┐
       │                │                │
       ▼                ▼                ▼
   Scan Scam        Awareness       Community
       │                │                │
       ▼                ▼                ▼
 Risk Engine         Learn          Report
       │
       ▼
   Verdict
       │
 ┌─────┼───────────┐
 │     │           │
 ▼     ▼           ▼
SAFE  SUSPICIOUS  HIGH RISK
                   │
                   ▼
             Guardian Alert
```

---

# 🧪 Testing Strategy

The project includes testing for major security and functionality areas.

### Authentication

* Registration
* Login
* Logout
* Session validation
* Unauthorized access

### Scanner

* SMS
* UPI
* Link
* Rule matching
* Risk scoring
* Verdict calculation
* Input limits
* URL safety

### History

* User isolation
* Pagination
* Scan details
* IDOR protection

### Community

* Normalization
* Duplicate prevention
* Community scoring
* Privacy
* Moderation

### Awareness

* Category filtering
* Quiz
* Safe example rendering
* Scanner integration

### Guardian

* Link requests
* Accept/reject
* Relationship authorization
* High-risk alerts
* Alert privacy

### Admin

* Role authorization
* Rule management
* Community moderation
* User management
* Scan activity
* Audit logs

---

# ⚙️ Installation

## 1. Clone the Repository

```bash
git clone <repository-url>
cd SurakshaScan
```

---

## 2. Database Setup

Create a MySQL database.

Example:

```sql
CREATE DATABASE surakshascan;
```

Import the project's SQL schema from:

```text
database/
```

Configure the database connection used by the Java backend.

---

# 3. Backend Setup

Navigate to:

```bash
cd backend
```

Build using Maven:

```bash
mvn clean package
```

Deploy/run the generated Java web application using the configured servlet container.

---

# 4. Frontend

Serve the frontend through the configured development/server environment.

The frontend communicates with the Java backend using the Fetch API.

---

# 🔧 Configuration

Configure:

```text
Database URL
Database username
Database password
Servlet container
Application/session settings
```

Do not commit production credentials to Git.

Use environment-specific configuration where supported.

---

# 📌 Important Project Principles

### No Fake Data

The dashboard, history, community, guardian and admin sections should display real database information.

### No Black-Box Detection

The scanner uses explainable rules and scoring.

### No Automatic URL Visiting

Submitted links are analyzed without outbound requests.

### No Frontend Authorization

Frontend visibility is never treated as security.

### No Reporter Exposure

Community reporting does not expose reporter identity.

### No Unrestricted Family Surveillance

Guardian access is limited to authorized family relationships.

---

# 🌟 What Makes SurakshaScan Different

SurakshaScan combines multiple cyber-safety concepts into one platform:

```text
          ┌──────────────────────┐
          │   SCAM DETECTION     │
          └──────────┬───────────┘
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
   RULE ENGINE   COMMUNITY      AWARENESS
       │         INTELLIGENCE        │
       │             │               │
       └─────────────┼───────────────┘
                     ▼
              USER PROTECTION
                     │
          ┌──────────┴──────────┐
          ▼                     ▼
   FAMILY SAFETY            HISTORY
          │                     │
          └──────────┬──────────┘
                     ▼
               ADMIN CONTROL
```

The project therefore goes beyond a simple:

> "Paste message → Get scam/not scam"

workflow.

It combines:

**Detection + Explanation + Community Intelligence + Education + History + Family Safety + Administration**

into a unified platform.

---

# 📜 Project Scope

SurakshaScan is designed as a proactive cyber-safety and awareness platform.

It should not be treated as:

* a replacement for official cybercrime reporting
* a legal determination of fraud
* an absolute guarantee that an input is safe
* independently verified proof solely because a community report exists

Detection results are based on the platform's implemented rules, scoring logic and available community intelligence.

---

# 🔮 Future Enhancements

Potential future improvements include:

* External email notifications
* SMS alerts
* Push notifications
* Advanced analytics
* More detection categories
* Improved rule-management workflows
* Additional awareness content
* Official reporting integrations
* More sophisticated abuse prevention
* Expanded threat intelligence integrations

These are future enhancements and are not represented as currently implemented functionality.

---

# 📊 Project Modules

| Module                    | Status        |
| ------------------------- | ------------- |
| Authentication            | ✅ Implemented |
| Scam Scanner              | ✅ Implemented |
| Explainable Risk Engine   | ✅ Implemented |
| Scan History              | ✅ Implemented |
| User Dashboard            | ✅ Implemented |
| Community Intelligence    | ✅ Implemented |
| Awareness Center          | ✅ Implemented |
| Awareness Quiz            | ✅ Implemented |
| Guardian / Family Safety  | ✅ Implemented |
| Guardian Alerts           | ✅ Implemented |
| Admin Panel               | ✅ Implemented |
| Scam Rule Management      | ✅ Implemented |
| Community Moderation      | ✅ Implemented |
| User Management           | ✅ Implemented |
| Scan Activity             | ✅ Implemented |
| Admin Audit Logs          | ✅ Implemented |
| External Email/SMS Alerts | 🔮 Future     |
| Advanced Analytics        | 🔮 Future     |

---

# 👨‍💻 Development Philosophy

SurakshaScan is built around four principles:

### 1. Explainability

Users should understand why something was flagged.

### 2. Security

Authorization and data isolation must be enforced server-side.

### 3. Privacy

Only information required for the feature should be exposed.

### 4. Practicality

The platform should help users make safer decisions in real-world situations.

---

# 📄 License

Add the project's intended license here before public distribution.

Example:

```text
MIT License
```

Do not claim a license unless the repository actually uses that license.

---

# 🛡️ SurakshaScan

### Detect. Understand. Verify. Stay Safe.

**A unified platform for scam detection, cyber awareness, community intelligence and digital safety.**
