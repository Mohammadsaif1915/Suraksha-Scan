# SurakshaScan — Deployment Checklist

> Use this checklist before deploying SurakshaScan to any environment.

---

## 1. Prerequisites

- [ ] **Java**: JDK 11 or higher installed
- [ ] **Maven**: 3.6+ installed
- [ ] **MySQL**: 8.0+ running and accessible
- [ ] **Servlet Container**: Apache Tomcat 9.x (or compatible container)
- [ ] **Network**: Ports 3306 (MySQL) and 8080/443 (Tomcat) accessible

---

## 2. Database Setup

- [ ] Create the MySQL database: `CREATE DATABASE surakshascan;`
- [ ] Import the complete schema: `mysql -u <user> -p surakshascan < database/schema.sql`
- [ ] Verify all tables exist: `users`, `scam_patterns`, `user_reports`, `community_flags`, `community_reports`, `family_links`, `guardian_alerts`, `admin_audit_logs`, `security_events`
- [ ] Confirm all foreign keys are intact
- [ ] Confirm all indexes are present
- [ ] Create initial admin user manually via SQL (do **not** use registration endpoint):
  ```sql
  INSERT INTO users (name, email, password_hash, role) 
  VALUES ('Admin', 'admin@yourdomain.com', '<PBKDF2_HASH>', 'admin');
  ```
  > Use the `PasswordUtil.hashPassword()` method or a test harness to generate the hash. Never store plaintext passwords.

---

## 3. Backend Configuration

- [ ] Open `backend/src/main/resources/application.properties`
- [ ] Set correct database credentials:
  ```
  db.url=jdbc:mysql://localhost:3306/surakshascan
  db.username=YOUR_DB_USER
  db.password=YOUR_DB_PASSWORD
  ```
- [ ] Ensure `application.properties` is **excluded from version control** (already in `.gitignore`)
- [ ] Do **not** commit database credentials to Git

---

## 4. Build

- [ ] Navigate to the `backend/` directory
- [ ] Run: `mvn clean package`
- [ ] Confirm build succeeds with `BUILD SUCCESS`
- [ ] Locate the generated WAR: `backend/target/surakshascan-backend.war`

---

## 5. Backend Deployment

- [ ] Copy `surakshascan-backend.war` to Tomcat's `webapps/` directory
- [ ] Start Tomcat: `./bin/startup.sh` (Linux) or `startup.bat` (Windows)
- [ ] Verify Tomcat started without errors in `logs/catalina.out`
- [ ] Test health endpoint: `curl http://localhost:8080/surakshascan-backend/api/health`
- [ ] Expected response: `{"success":true,"status":"UP",...}`

---

## 6. Frontend Deployment

- [ ] Copy all files from `frontend/` to Tomcat's ROOT webapp or a dedicated virtual host directory
- [ ] Alternatively, configure a reverse proxy (nginx/Apache) to serve frontend files
- [ ] Confirm all HTML, CSS, JS files are accessible
- [ ] Verify `js/api.js` `API_BASE_URL` constant matches the deployed backend context path

---

## 7. Session Configuration

- [ ] Session timeout is set to 30 minutes in `AuthServlet.java` (production-ready)
- [ ] If using a load balancer, configure sticky sessions or externalize session storage
- [ ] Verify session cookies use `HttpOnly` (default in Tomcat)
- [ ] **HTTPS**: Configure Tomcat SSL or front with HTTPS-terminating reverse proxy (recommended for production)

---

## 8. Security Verification

- [ ] Verify `SecurityFilter` is active by checking response headers: `X-Frame-Options`, `X-Content-Type-Options`, `Content-Security-Policy`
- [ ] Confirm unauthenticated access to `/api/dashboard/stats` returns `401`
- [ ] Confirm non-admin access to `/api/admin/stats` returns `403`
- [ ] Confirm rate limiting is active (6th login request within 1 min returns `429`)
- [ ] Confirm no stack traces are visible in API error responses

---

## 9. Admin Account

- [ ] Confirm admin account exists and can log in
- [ ] Test admin dashboard loads correctly
- [ ] Test analytics endpoint: `GET /api/admin/analytics?period=30d`
- [ ] Review recent security events on Security tab
- [ ] Verify at least one scam pattern is active

---

## 10. Application Testing

- [ ] Register a new user account
- [ ] Log in with the new account
- [ ] Run a test scan (SMS / UPI / LINK)
- [ ] Verify result appears in scan history
- [ ] Submit a community report
- [ ] Log out and confirm session is invalidated
- [ ] Log in as admin and review the scan in admin panel

---

## 11. Demo Data Review

- [ ] Review seed data in `database/schema.sql` (clearly marked as development patterns)
- [ ] Remove or update `[DEV ONLY]` patterns before public launch
- [ ] Do **not** create demo users with predictable passwords in production

---

## 12. Logs

- [ ] Review Tomcat access logs for unexpected 500 errors
- [ ] Review `security_events` table for any initialization issues
- [ ] Confirm `admin_audit_logs` table is empty or contains only expected entries

---

## 13. Health Check

- [ ] Confirm `/api/health` returns `UP` status
- [ ] If status is `DEGRADED`, investigate database connection issues before going live

---

> **Note:** This checklist reflects the requirements of a single-node educational deployment. A production multi-node deployment would additionally require: distributed session storage, distributed rate-limiting, centralized logging, database connection pooling (e.g., HikariCP), and a hardened Tomcat configuration.
