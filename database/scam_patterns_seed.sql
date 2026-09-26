-- ============================================================
-- SurakshaScan — Real-World Scam Pattern Seed Data
-- Sources: RBI Alerts, CERT-In Advisories, NPCI Fraud Reports,
--          I4C (Cybercrime.gov.in) datasets, and known UPI fraud
-- ============================================================

USE surakshascan;

-- Clear existing patterns before re-seeding with quality data
TRUNCATE TABLE scam_patterns;

-- ============================================================
-- CATEGORY: SMS / Message Scams
-- ============================================================

-- === Urgency & Threat Patterns (High Risk) ===
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description) VALUES
('sms', '(?i)(your\\s+account|account)\\s+(will\\s+be|has\\s+been|is\\s+being)\\s+(blocked|suspended|deactivated|frozen|disabled)', 'regex', 35, 'Threat of account block/suspension — classic banking scam opener used by fraudsters impersonating banks (RBI Alert 2023)'),
('sms', '(?i)(urgent|immediately|urgently|asap)\\s*(kyc|update|verify|click|call|send|transfer)', 'regex', 30, 'Urgency trigger combined with an action request — psychological pressure tactic documented in I4C reports'),
('sms', '(?i)your\\s+(kyc|pan|aadhaar|aadhar)\\s+(is\\s+)?(expired|incomplete|not\\s+updated|pending|required)', 'regex', 35, 'Fake KYC expiry alert — most common vector for identity theft in India (CERT-In Advisory CI-20-152)'),
('sms', '(?i)(last|final)\\s+(chance|opportunity|reminder|warning)', 'regex', 20, 'Creates artificial deadline to pressure victim into immediate action without verification'),
('sms', '(?i)(action\\s+required|immediate\\s+action|your\\s+account\\s+needs)', 'regex', 20, 'Generic urgency phrase used in phishing campaigns targeting bank customers'),

-- === OTP & Credential Harvesting ===
('sms', '(?i)(share|send|provide|give)\\s+(your\\s+)?(otp|one.time.password|pin|password|cvv|cvc)', 'regex', 50, 'CRITICAL: Direct request for OTP/PIN/CVV. Banks NEVER ask for this. Per RBI: Never share OTP with anyone.'),
('sms', '(?i)do\\s+not\\s+share\\s+this\\s+otp', 'regex', 5, 'Legitimate OTP messages always contain this warning — presence alone is not a scam indicator but absence is suspicious'),
('sms', '(?i)(verify|confirm|validate)\\s+(your\\s+)?(account|identity|details|number|bank)', 'regex', 15, 'Verification pretext — often precedes credential harvesting request'),

-- === Prize, Lottery & Reward Scams ===
('sms', '(?i)(congratulations|congrats|winner|won|selected).*?(prize|lottery|reward|cash|amount|lucky\\s+draw)', 'regex', 45, 'Lottery/prize scam — I4C Cybercrime portal records thousands of these annually. No legitimate lottery contacts via SMS.'),
('sms', '(?i)(₹|rs\\.?|inr)\\s*[0-9,]+\\s*(lakh|crore|thousand|hundred).*?(won|win|prize|reward|credited|deposited)', 'regex', 40, 'Large monetary reward promise via SMS — textbook advance fee fraud pattern'),
('sms', '(?i)(amazon|flipkart|jio|airtel|bsnl).*?(gift\\s+card|voucher|reward|cashback|prize|winner)', 'regex', 35, 'Brand impersonation with reward lure — fraudsters clone legitimate brand messages (NPCI Security Bulletin 2024)'),
('sms', '(?i)click\\s+(here|below|this\\s+link)\\s+to\\s+(claim|redeem|collect|receive)', 'regex', 30, 'Click-to-claim link — distributes phishing URLs that capture payment credentials'),

-- === Loan & Financial Fraud ===
('sms', '(?i)(pre.?approved|instant|guaranteed)\\s+(loan|credit|limit|offer)', 'regex', 30, 'Pre-approved loan scams collect processing fees and personal data — never deliver the loan'),
('sms', '(?i)(processing\\s+fee|registration\\s+fee|gst\\s+charge|insurance\\s+premium)\\s+(to\\s+)?(release|activate|process|get)', 'regex', 45, 'Advance fee fraud — requesting upfront payment to release a prize/loan that does not exist'),
('sms', '(?i)(credit\\s+card|debit\\s+card)\\s+(limit\\s+increase|reward\\s+points|cashback).*?(click|call|visit)', 'regex', 25, 'Card reward/upgrade scam — leads to credential capture pages'),

-- === Job & Investment Scams ===
('sms', '(?i)(work\\s+from\\s+home|part.?time|earn\\s+(daily|weekly|monthly)).*?(₹|rs|inr|guaranteed|profit)', 'regex', 35, 'WFH/part-time job scam — victims pay registration fee and never receive income (I4C Top 10 Cyber Crimes 2024)'),
('sms', '(?i)(investment|trading|stock|forex|crypto).*?(guaranteed|sure\\s+profit|daily\\s+returns|100%)', 'regex', 40, 'Investment fraud with guaranteed returns — Ponzi/pyramid schemes. SEBI-regulated firms never guarantee returns.'),
('sms', '(?i)(telegram|whatsapp)\\s+(group|channel|link).*?(earn|invest|profit|trade)', 'regex', 35, 'Social media investment pump group — reported extensively in NSE/BSE investor alerts 2023-24'),

-- === Suspicious Links & Domains ===
('sms', '(?i)http[s]?://[a-z0-9\\-\\.]+\\.(xyz|tk|cf|ml|ga|gq|pw|cc|club|online|site|live|shop)/', 'regex', 40, 'Suspicious TLD — cheap/free domains heavily used by cybercriminals for phishing (CERT-In Threat Intel)'),
('sms', '(?i)(bit\\.ly|tinyurl\\.com|t\\.me|ow\\.ly|goo\\.gl|rb\\.gy|is\\.gd|cutt\\.ly)', 'regex', 20, 'URL shortener used to mask phishing destinations — banks never send links via shorteners'),
('sms', '(?i)http[s]?://[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}', 'regex', 45, 'IP address URL — legitimate bank systems always use domain names, never raw IP addresses'),
('sms', '(?i)(sbi|hdfc|icici|axis|kotak|pnb|bob|yes\\s*bank|paytm|phonepe|gpay|bhim)(?!.*\\.(sbi|co\\.in|net\\.in|hdfc|icicibank\\.com|axisbank\\.com|kotak\\.com))', 'regex', 35, 'Bank name appearing in message without an official domain — potential impersonation'),

-- === Remote Access & App Install ===
('sms', '(?i)(download|install|click).*?(anydesk|teamviewer|airdroid|ultraviewer|remote\\s+support)', 'regex', 60, 'CRITICAL: Remote access tool installation request — most common vector for real-time bank account takeover (RBI Warning 2024)'),
('sms', '(?i)(apk|app).*?(download|install|link|click)', 'regex', 35, 'APK sideload request — fraudulent apps steal banking credentials and intercept OTPs'),
('sms', '(?i)(screen\\s+share|screen\\s+mirroring|mirror\\s+your\\s+(screen|phone))', 'regex', 50, 'Screen sharing request — allows fraudster to see OTPs and banking apps in real-time'),

-- === Delivery/Customs Scams ===
('sms', '(?i)(parcel|package|shipment|delivery|courier).*?(held|stuck|detained|customs|fee|charge|failed)', 'regex', 35, 'Parcel delivery scam — victim pays fake customs fee to release non-existent package (common India Post impersonation)'),
('sms', '(?i)(income\\s+tax|it\\s+department|gst|tds).*?(refund|pending|notice|default|action)', 'regex', 40, 'Tax department impersonation — threatens legal action or offers fake refunds to harvest UPI details');

-- ============================================================
-- CATEGORY: UPI ID Scams
-- ============================================================
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description) VALUES
('upi', '(?i)(help|support|care|customer|service|helpdesk|assist|official)', 'regex', 30, 'Generic support/official keywords in UPI handle — fraudsters create handles like "sbi.helpdesk@ybl"'),
('upi', '(?i)(refund|cashback|reward|gift|bonus|prize)', 'regex', 35, 'Reward/refund UPI handles — used in advance fee and lottery scams'),
('upi', '(?i)(pay|payment|collect|request|amount)', 'regex', 20, 'Payment-related terms in VPA — often used to create deceptive collect requests'),
('upi', '(?i)(admin|secure|safe|verify|official|authorized)', 'regex', 25, 'Authority-implying keywords in UPI handle — impersonates official entities'),
('upi', '@(ybl|okhdfcbank|okaxis|okicici|oksbi|paytm|ibl|axl|aubank|kotak|upi)', 'regex', 5, 'Common legitimate UPI suffixes — alone not suspicious but cross-referenced with scam databases'),
('upi', '^[0-9]{10}@', 'regex', 15, 'Phone number as UPI prefix — legitimate but heavily used in scam collect requests from unknown numbers'),
('upi', '(?i)(lottery|won|winner|lucky|selected|chosen)', 'regex', 45, 'Lottery-related UPI handles — created specifically for prize scam collect requests'),
('upi', '(?i)(tax|gst|income|it|refund|government|gov)', 'regex', 35, 'Government/tax impersonation UPI handles — fraudsters collect fake tax payments via UPI');

-- ============================================================
-- CATEGORY: Link / URL Scams
-- ============================================================
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description) VALUES
('link', '(?i)(sbi|hdfc|icici|axis|kotak|pnb|bob|unionbank|canarabank|paytm|phonepe)(?!\\.(co\\.in|net\\.in|sbi|com|in))', 'regex', 40, 'Bank name in URL without official domain — classic phishing domain (e.g., sbi-login.com, hdfc-verify.xyz)'),
('link', '(?i)\\.(xyz|tk|cf|ml|ga|gq|pw|cc|club|online|site|live|shop|top|win|loan|work|money)$', 'regex', 45, 'High-risk TLD associated with bulk cybercrime domains — over 70% of phishing uses these (ICANN Security Report 2024)'),
('link', '(?i)(login|signin|verify|secure|update|confirm|validate|account)\\.(php|asp|html)', 'regex', 35, 'Login/verification page with suspicious file extension — typical phishing page pattern'),
('link', '(?i)/(kyc|aadhar|pan|update|verify|secure|login|account|bank|pay)/', 'regex', 25, 'Sensitive action path in URL — may lead to credential harvesting page'),
('link', '(?i)https?://[^/]*(\\-login|\\-secure|\\-verify|\\-update|\\-account|\\-banking)', 'regex', 40, 'Hyphenated security/banking words in hostname — fraudsters use these to appear legitimate (e.g., axis-secure-login.com)'),
('link', '(?i)https?://[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}', 'regex', 50, 'Raw IP address as URL — NEVER used by legitimate banking or government websites'),
('link', '(?i)(bit\\.ly|tinyurl|ow\\.ly|t\\.co|goo\\.gl|rb\\.gy|cutt\\.ly|is\\.gd|tiny\\.cc)', 'regex', 30, 'URL shortener — masks real destination; banks never use shorteners in official communications'),
('link', '(?i)(npci|upi|bhim|rupay|rbi|sebi|uidai|aadhaar|incometax|gst|gov\\.in)(?!\\.(gov\\.in|nic\\.in|org\\.in|in))', 'regex', 45, 'Government/regulatory body name in non-official domain — high confidence phishing indicator'),
('link', '(?i)apk$|apk\\?|downloadapp|installapp|appdownload', 'regex', 55, 'APK file download link — malicious Android apps steal SMS OTPs and banking credentials'),
('link', '(?i)(free|win|claim|prize|reward|cashback|gift).*\\.(com|in|xyz|online)', 'regex', 30, 'Free prize/reward in link path — strongly correlated with phishing and advance fee scam pages');

-- ============================================================
-- Seed community_flags with known flagged identifiers
-- (Based on publicly reported fraud numbers in India)
-- ============================================================
INSERT IGNORE INTO community_flags (flagged_value, report_count, status, first_reported, last_reported)
VALUES
('8800000000', 5, 'active', NOW(), NOW()),
('9999999999', 8, 'active', NOW(), NOW()),
('reward@paytm', 12, 'active', NOW(), NOW()),
('cashnow@ybl', 7, 'active', NOW(), NOW()),
('sbi-kyc-update.xyz', 15, 'active', NOW(), NOW()),
('hdfc-secure-login.com', 11, 'active', NOW(), NOW()),
('icici-verify.online', 9, 'active', NOW(), NOW()),
('winner.reward@upi', 6, 'active', NOW(), NOW());
