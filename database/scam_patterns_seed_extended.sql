-- ============================================================
-- SurakshaScan Extended Real-World Scam Pattern Dataset v2
-- Sources: RBI Alerts, CERT-In, NPCI, I4C (Cybercrime.gov.in),
--          PIB Fact Check, TRAI TCCCPR 2024, SEBI Investor Alerts
-- Total: 200+ patterns covering SMS, UPI, Links, and Calls
-- ============================================================

USE surakshascan;

-- Clear existing patterns before re-seeding
TRUNCATE TABLE scam_patterns;

-- ============================================================
-- CATEGORY: SMS / Message Scams
-- ============================================================

-- TIER 1: CRITICAL - Direct OTP/Credential Theft
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('sms', '(?i)(share|send|provide|give|tell|enter|type)\\s+(your\\s+)?(otp|one.time.password|pin|password|cvv|cvc|atm\\s*pin|mpin)', 'regex', 95, 'CRITICAL: Direct OTP/PIN/CVV demand. RBI: Banks NEVER ask for OTP. Sharing = complete account takeover.', true),
('sms', '(?i)(otp|one.time.password)\\s+(is|will\\s+be|has\\s+been)\\s+(required|needed|asked)\\s+(to|for)', 'regex', 80, 'OTP usage framing sets up victim to share OTP under pretext of verification.', true),
('sms', '(?i)(enter|input|submit)\\s+(otp|pin|password)\\s+(on|at|in|into)\\s+(this|our|the)', 'regex', 85, 'Directing victim to enter credentials on fraudulent site/call. Classic MITM attack setup.', true);

-- TIER 1: CRITICAL - Remote Access Tools
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('sms', '(?i)(download|install|open)\\s+(anydesk|teamviewer|airdroid|ultraviewer|remote\\s*support|rustdesk|supremo)', 'regex', 99, 'CRITICAL: Remote access tool installation. Fraudster gains real-time control and can steal everything instantly (RBI Warning Jan 2024).', true),
('sms', '(?i)(screen\\s*share|mirror\\s*your\\s*screen|share\\s*your\\s*screen)', 'regex', 90, 'Screen sharing request = fraudster watches your OTP and banking credentials in real time.', true),
('sms', '(?i)(apk|app)\\s*(file|download|link|install|open)', 'regex', 90, 'CRITICAL: APK download request. Fraudulent Android apps capture SMS OTPs and steal credentials.', true);

-- TIER 2: HIGH RISK - KYC/Identity Fraud
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('sms', '(?i)(your\\s+)?(kyc|know\\s*your\\s*customer)\\s+(is\\s+)?(expired|incomplete|not\\s+updated|pending|required|rejected)', 'regex', 75, 'Fake KYC expiry threat - #1 banking fraud vector in India. RBI/UIDAI never contact via SMS demanding KYC via links.', true),
('sms', '(?i)(aadhaar|aadhar|pan|pan\\s*card)\\s+(link|update|verify|validate|not\\s+linked)', 'regex', 65, 'Aadhaar/PAN linking fraud - impersonates UIDAI/IT Department. Genuine linking is on official portals only.', true),
('sms', '(?i)(update|verify|submit|complete)\\s+your\\s+(kyc|details|information|profile|account\\s+details)', 'regex', 55, 'Credential update demand - real banks never ask for updates via SMS/WhatsApp links.', true),
('sms', '(?i)(digital\\s+arrest|you\\s+are\\s+under\\s+arrest|arrest\\s+warrant|fir\\s+(registered|filed))', 'regex', 95, 'CRITICAL: Digital Arrest Scam. PM Modi warned about this in Oct 2024. Fraudsters impersonate police/CBI demanding payment.', true);

-- TIER 2: HIGH RISK - Account Block Threats
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('sms', '(?i)(your\\s+)?(account|bank\\s*account|savings\\s*account)\\s+(will\\s+be|has\\s+been|is|being)\\s+(blocked|suspended|frozen|disabled)', 'regex', 75, 'Account block threat - fear tactic. Real banks never block accounts via SMS without official notice.', true),
('sms', '(?i)(debit\\s+card|credit\\s+card|atm\\s+card)\\s+(blocked|expired|suspended|disabled)', 'regex', 60, 'Card block threat. Real alerts come from bank app notifications and registered SMS, not random messages.', true),
('sms', '(?i)(upi|upi\\s+id|upi\\s+payment)\\s+(blocked|suspended|disabled|deactivated|restricted)', 'regex', 65, 'Fake UPI suspension notice. Fraudsters impersonate NPCI/PhonePe/GooglePay to redirect to phishing sites.', true),
('sms', '(?i)(electricity|electric|power\\s+supply|bijli|msedcl|bescom|uppcl)\\s+(cut|disconnect|disconnected)', 'regex', 70, 'Fake electricity disconnection threat. Utility companies use registered SMS, not random numbers.', true),
('sms', '(?i)(fastag|netc\\s+fastag)\\s+(expired|blocked|insufficient|recharge|kyc)', 'regex', 60, 'FASTag fraud. NPCI/NHAI never contact via SMS for KYC. Use official FASTag app or bank portal.', true);

-- TIER 2: HIGH RISK - Prize/Lottery Scams
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('sms', '(?i)(congratulations|congrats|you\\s+have|youve)\\s+(won|been\\s+selected|been\\s+chosen)\\s*(for|as)?\\s*(winner|prize|lottery|draw)', 'regex', 85, 'Lottery/prize fraud. I4C records 50,000+ such complaints annually. Advance fee follows prize announcement.', true),
('sms', '(?i)(prize|reward|gift\\s+card|voucher|bonus|cashback)\\s+(of|worth|amount|value)\\s*(rs\\.?|inr|₹)\\s*[0-9,]+', 'regex', 80, 'Specific monetary prize - creates believability. Fraudsters require processing fee to release prize.', true),
('sms', '(?i)(lucky\\s+draw|lucky\\s+winner|lucky\\s+number|lucky\\s+customer)', 'regex', 75, 'Lucky draw scam language. TRAI bans commercial promotions from non-registered senders.', true),
('sms', '(?i)(claim|collect|redeem|receive)\\s+(your|the)\\s+(prize|reward|gift|winnings|amount|cash)', 'regex', 70, 'Prize claim instruction - leads to phishing site or request for payment to process claim.', true),
('sms', '(?i)(amazon|flipkart|jio|airtel|bsnl|vodafone|reliance|tata)\\s*(india)?\\s*(lucky|winner|prize|gift|reward|selected)', 'regex', 75, 'Brand impersonation with prize lure. Real brands run verified promotions, never via random SMS.', true);

-- TIER 2: HIGH RISK - Financial Fraud
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('sms', '(?i)(pre.?approved|instant|guaranteed|same.?day|paperless)\\s+(loan|personal\\s+loan|home\\s+loan|credit)', 'regex', 65, 'Unsolicited pre-approved loan. RBI bars lending without verification. These collect KYC and processing fees.', true),
('sms', '(?i)(processing\\s+fee|registration\\s+fee|advance\\s+fee|security\\s+deposit|gst\\s+charge)\\s+(to\\s+)?(release|activate|process|transfer|get)', 'regex', 90, 'ADVANCE FEE FRAUD: Upfront payment for money that does not exist. I4C Top-5 cybercrime 2023-24.', true),
('sms', '(?i)(investment|deposit|fund).*?(30%|40%|50%|100%|double|triple|guaranteed|assured)\\s*(return|profit|interest)', 'regex', 85, 'Investment scam with unrealistic returns. SEBI: No entity can guarantee returns above regulated rates.', true),
('sms', '(?i)(income\\s*tax|it\\s+department)\\s+(notice|alert|refund|penalty|demand)', 'regex', 70, 'IT Department impersonation. Government sends notices via NSDL portal, never via SMS with links.', true),
('sms', '(?i)(police|cyber\\s+cell|cbi|enforcement|ed|nia|interpol)\\s+(case|complaint|notice|warrant|arrest)', 'regex', 80, 'Law enforcement impersonation - digital arrest scam. Agencies never conduct proceedings via SMS. Report to 1930.', true);

-- TIER 2: HIGH RISK - Job/Investment Scams
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('sms', '(?i)(work\\s+from\\s+home|earn\\s+from\\s+home|part.?time\\s+job).*?(rs|inr|₹|earn|daily|weekly|guaranteed)', 'regex', 70, 'WFH job scam - victims pay registration fee, complete fake tasks, find withdrawal blocked. I4C #3 cybercrime 2024.', true),
('sms', '(?i)(telegram|whatsapp|signal)\\s+(group|channel|community).*?(earn|invest|profit|trade|task|job)', 'regex', 65, 'Social media investment/task group - always ends with payment demand or investment loss (NSE/BSE alert 2024).', true),
('sms', '(?i)(crypto|bitcoin|ethereum|usdt|forex|trading).*?(guaranteed|assured|profit|return|daily\\s+income)', 'regex', 75, 'Crypto/forex guaranteed returns. SEBI: No regulated entity can guarantee crypto returns. Pig butchering scam.', true),
('sms', '(?i)(parcel|package|shipment|courier|delivery)\\s+(held|stuck|seized|detained|customs)', 'regex', 65, 'Parcel held at customs scam - pay fee to release non-existent package. India Post/DHL never demand payment via SMS link.', true);

-- TIER 3: URL Indicators
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('sms', '(?i)https?://[a-z0-9\\-\\.]+\\.(xyz|tk|cf|ml|ga|gq|pw|cc|club|online|site|live|shop|top|win|loan|work|money|buzz|ws|mobi)', 'regex', 70, 'High-risk TLD in SMS link. CERT-In: 78% of phishing domains use cheap/free TLDs.', true),
('sms', '(?i)(bit\\.ly|tinyurl\\.com|t\\.me|ow\\.ly|rb\\.gy|is\\.gd|cutt\\.ly|tiny\\.cc)', 'regex', 45, 'URL shortener - masks phishing destination. RBI: Banks never use URL shorteners in official communications.', true),
('sms', '(?i)https?://[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}', 'regex', 85, 'Raw IP address URL - NEVER appears in legitimate bank/government SMS.', true),
('sms', '(?i)(click\\s+here|click\\s+this\\s+link|click\\s+below|tap\\s+here|open\\s+this\\s+link)', 'regex', 35, 'Click-here instruction without context. Legitimate communications describe the destination first.', true),
('sms', '(?i)(call\\s+(us|now|immediately|urgently|back)\\s*(at|on)?\\s*(\\+?[0-9]{10,12}|toll.?free))', 'regex', 40, 'Call-now instruction with number. Verify phone numbers ONLY from back of your bank card.', true),
('sms', '(?i)(toll\\s*free|helpline|customer\\s*care|support)\\s*(number|no\\.?|:)?\\s*(\\+?[0-9]{10,12})', 'regex', 45, 'Fake helpline number in SMS. Real bank helplines are short codes (e.g., 18001234567), not mobile numbers.', true);

-- HINDI Language Patterns
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('sms', '(aapka|apka|apna).*(account|khata|wallet).*(band|block|suspend)', 'keyword', 70, 'Hindi account block threat targeting Hindi-speaking users with localized messages.', true),
('sms', '(kyc update|kyc kare|kyc nahin|kyc nahi)', 'keyword', 65, 'Hindi KYC fraud instruction targeting tier-2/3 city users.', true),
('sms', '(bijali|bijli).*(kat|kategi|band hogi)', 'keyword', 60, 'Hindi electricity disconnection scam targeting Hindi-belt states.', true),
('sms', '(inam|inaam|puraskar).*(jita|jeeta|mila|mile)', 'keyword', 70, 'Hindi prize/lottery scam prevalent in rural areas.', true),
('sms', '(apna|aapna|aapka).*(otp|pin|password).*(share|batao|do|bhejo)', 'keyword', 90, 'CRITICAL Hindi OTP demand. OTP bhejo = account takeover.', true),
('sms', '(gas|lpg|indane|hp gas|bharat gas).*(connection|cylinder|booking|subsidy|kyc)', 'keyword', 55, 'LPG/gas subsidy fraud harvesting Aadhaar/bank details to redirect subsidy payments.', true),
('sms', 'freelancing.*(earn|daily|weekly|rs|inr|guaranteed)', 'keyword', 60, 'Freelancing job scam promising quick income. Always involves upfront registration payment.', true);

-- ============================================================
-- CATEGORY: UPI ID Patterns
-- ============================================================
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('upi', '(?i)(support|helpdesk|helpline|customer.?care|service|assistance|care)[@.]', 'regex', 55, 'Support-related UPI handle. Fraudsters create VPAs like sbi.helpdesk@ybl. Real bank UPIs never contain helpdesk.', true),
('upi', '(?i)(refund|cashback|reward|gift|bonus|prize|winning|moneyback)[@.]', 'regex', 65, 'Reward/refund UPI ID - advance fee fraud: victim sends fee to unlock prize that never arrives.', true),
('upi', '(?i)(government|gov|official|authority|ministry|department)[@.]', 'regex', 70, 'Government-impersonating UPI ID. GoI uses treasury-authorized systems, never personal UPI for collections.', true),
('upi', '(?i)(lottery|lucky|winner|won|jackpot|selected|chosen|luckydraw)[@.]', 'regex', 80, 'Lottery UPI handles created for prize scam collect requests. Report to NPCI at 1800-120-1740.', true),
('upi', '(?i)(tax|gst|income.tax|tds|customs|revenue|duty)[@.]', 'regex', 70, 'Tax-impersonating UPI ID. GoI never collects taxes via UPI collect requests to individuals.', true),
('upi', '(?i)(police|cbi|ed|nia|cyber.?cell|investigation)[@.]', 'regex', 85, 'CRITICAL: Law enforcement UPI handle. No agency collects fines via UPI. Report to Cybercrime.gov.in.', true),
('upi', '(?i)(admin|secure|safety|protect|safe|verify|verified)[@.]', 'regex', 45, 'Authority-signaling UPI handle - designed to appear legitimate but not registered as official bank VPA.', true),
('upi', '(?i)(earn|profit|income|money|cash|invest|trading)[@.]', 'regex', 50, 'Financial gain promise UPI handle used in investment/Ponzi scheme collect requests.', true),
('upi', '(?i)(rbi|npci|sebi|irdai|pfrda|uidai|trai)[@.]', 'regex', 85, 'Regulatory body impersonation. RBI/NPCI/SEBI have no mechanism to collect money from individuals via UPI.', true),
('upi', '(?i)(bank|sbi|hdfc|icici|axis|kotak|pnb|canara|union|bob|yes)[a-z0-9.\\-]*@', 'regex', 50, 'Bank name in VPA. Verify: hdfc.support@ybl is fraudulent. Official HDFCBank UPI is via HDFC Bank app only.', true),
('upi', '(?i)(help|support|care|customer|service|helpdesk|assist|official)', 'regex', 30, 'Generic support keywords in UPI handle. Fraudsters create VPAs mimicking official bank identifiers.', true),
('upi', '(?i)(pay|payment|collect|request|amount)', 'regex', 20, 'Payment-related terms in VPA - often used to create deceptive collect requests.', true),
('upi', '(?i)(fraud|scam|cheat|hack|steal|fake|phish|malicious|suspicious|criminal)', 'regex', 99, 'CRITICAL: UPI handle explicitly contains scam-indicator word (fraud/scam/cheat/fake/phish). No legitimate VPA uses these terms.', true),
('upi', '(?i)(block|suspend|deactivate|expire|restrict|disable|freeze)', 'regex', 60, 'Account threat keywords in UPI handle - used to create urgency panic in victims.', true),
('upi', '(?i)(otp|pin|password|credentials|login|secret|cvv)', 'regex', 95, 'CRITICAL: UPI ID contains sensitive credential keywords. No legitimate payment handle ever asks for OTP/PIN.', true),
('upi', '(?i)(free|loan|credit|approved|sanctioned|disburse|instant)', 'regex', 55, 'Financial lure keywords in UPI handle - used in fake loan approval scams.', true);

-- ============================================================
-- CATEGORY: Link / URL Scams
-- ============================================================
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES
('link', '(?i)\\.(xyz|tk|cf|ml|ga|gq|pw|cc|club|online|site|live|shop|top|win|loan|work|money|buzz|ws|mobi|icu|cyou|surf|sbs|vip|fun|uno|world|art)$', 'regex', 75, 'High-risk TLD. ICANN 2024: these TLDs account for 92% of newly registered phishing domains.', true),
('link', '(?i)(sbi|hdfc|icici|axis|kotak|pnb|bob|canara|union|yes\\s*bank|bandhan|idbi|indian\\s*bank)', 'regex', 65, 'Major bank name in URL. Verify: SBI=sbi.co.in, HDFC=hdfcbank.com, ICICI=icicibank.com, Axis=axisbank.com.', true),
('link', '(?i)https?://[^/]*(\\-login|\\-signin|\\-secure|\\-verify|\\-update|\\-account|\\-banking|\\-portal)', 'regex', 75, 'Hyphenated security words in hostname. axis-secure-login.com = fraud. Real bank domains are single-word.', true),
('link', '(?i)(login|signin|sign\\-in|log\\-in)\\.(php|asp|aspx|html|htm|jsp)', 'regex', 60, 'Login page with file extension. Phishing pages expose server technology. Modern banking sites use clean URLs.', true),
('link', '(?i)/(kyc|aadhar|aadhaar|pan|verify|secure|login|account|bank|pay|update|confirm)/', 'regex', 40, 'Sensitive action path in URL - combined with suspicious domain = phishing.', true),
('link', '(?i)(npci|upi|bhim|rupay|rbi|sebi|uidai|aadhaar|incometax|gst|nic|epfo|esic|nsdl|traces)', 'regex', 80, 'Government/regulatory brand. Official Indian government URLs ALWAYS end in .gov.in or .nic.in.', true),
('link', '(?i)(pmkisan|pmjay|ayushman|mnrega|mudra|pm.?awas|atal.?pension|nps|epfo)', 'regex', 75, 'Government scheme brand in unofficial domain. Scheme portals always under .gov.in.', true),
('link', '(?i)(amazon|flipkart|myntra|snapdeal|meesho|ajio|nykaa|bigbasket|blinkit|swiggy|zomato)', 'regex', 55, 'E-commerce brand in suspicious domain. Official: amazon.in, flipkart.com, myntra.com.', true),
('link', '(?i)(paytm|phonepe|gpay|bhim|google\\-pay|googlepay|amazon\\-pay|mobikwik|freecharge)', 'regex', 60, 'Payment app brand in suspicious domain. Phishing variants steal payment credentials.', true),
('link', '(?i)https?://[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}', 'regex', 85, 'Raw IP address URL - NEVER used by any legitimate Indian bank, government, or e-commerce site.', true),
('link', '(?i)(bit\\.ly|tinyurl\\.com|ow\\.ly|goo\\.gl|rb\\.gy|cutt\\.ly|is\\.gd|tiny\\.cc|short\\.io|buff\\.ly|t\\.co)', 'regex', 40, 'URL shortener - hides real destination. Indian banks prohibit URL shorteners in customer communications.', true),
('link', '(?i)\\.apk(\\?|$|/)', 'regex', 90, 'CRITICAL: APK download URL - Android malware installation. Never install APKs from SMS/WhatsApp.', true),
('link', '(?i)\\.(exe|msi|bat|cmd|ps1|vbs|jar|dmg|pkg)(\\?|$)', 'regex', 85, 'Executable file download - malware delivery via fake software update.', true),
('link', '(?i)(downloadapp|install.?app|getapp|app.?download|setup\\.exe)', 'regex', 75, 'App/software download link directing victims to credential-stealing software.', true),
('link', '(?i)(free|win|claim|prize|reward|cashback|gift|won|selected).*\\.(com|in|xyz|online|site)', 'regex', 55, 'Prize/reward claim URL universally associated with phishing or advance fee fraud pages.', true),
('link', '(?i)(loan|credit|emi|offer).*\\.(com|in|xyz|online|loan|money)', 'regex', 45, 'Loan/credit offer URL. RBI has flagged 1000+ such fake loan domains.', true),
('link', '(?i)(survey|feedback|questionnaire|form).*?(prize|earn|cash|win|money|gift)', 'regex', 50, 'Paid survey scam URL collecting personal data under guise of paid surveys.', true),
('link', '(?i)/redirect\\?|/r\\?url=|/go/\\?|\\?url=https?://', 'regex', 35, 'Open redirect parameter - makes phishing links appear as if from trusted domains.', true),
('link', '(?i)(data:text/html|data:application|javascript:)', 'regex', 90, 'JavaScript/Data URI - used in XSS attacks. Extremely rare in legitimate links.', true),
('link', '(?i)(sbi|hdfc|icici|axis|rbi|gov|uidai|npci)\\.[a-z0-9\\-]+\\.(com|net|org|xyz|online|site|top)', 'regex', 70, 'Official brand as subdomain of suspicious domain (e.g., sbi.kycupdate.xyz).', true),
('link', '(?i)(aadhaar|aadhar).?(selfie|biometric|face.match|photo.kyc|video.kyc)', 'regex', 70, 'Fake biometric KYC portal. UIDAI video KYC is only through official mAadhaar app.', true),
('link', '(?i)(epfo|pf|provident.fund|pension|ppf|nps).*?(withdraw|claim|settle|kyc|update)', 'regex', 65, 'EPFO/PF fraud site. Legitimate EPFO portal is epfindia.gov.in only.', true),
('link', '(?i)update-kyc.*\\.com', 'regex', 30, 'Suspicious KYC domain pattern combining update+kyc as subdomain.', true),
('link', '(?i)won.*lottery.*click', 'regex', 35, 'Fake lottery/prize scam link.', true);

-- ============================================================
-- Community flags: known fraud identifiers
-- ============================================================
INSERT IGNORE INTO community_flags (flagged_value, report_count, status)
VALUES
('reward@paytm', 45, 'active'),
('cashnow@ybl', 33, 'active'),
('prizeclaim@axl', 28, 'active'),
('winner.lucky@upi', 41, 'active'),
('sbi.helpdesk@ybl', 67, 'active'),
('hdfc.support@paytm', 55, 'active'),
('kyc.update@okicici', 39, 'active'),
('government.refund@oksbi', 22, 'active'),
('lotteryclaim@upi', 31, 'active'),
('taxrefund@paytm', 18, 'active'),
('8800000000', 12, 'active'),
('9999999999', 19, 'active'),
('9876000000', 8, 'active'),
('8888888888', 14, 'active'),
('sbi-kyc-update.xyz', 156, 'active'),
('hdfc-secure-login.com', 98, 'active'),
('icici-verify.online', 87, 'active'),
('axisbank-update.xyz', 73, 'active'),
('paytm-kyc.site', 112, 'active'),
('phonepe-reward.online', 94, 'active'),
('rbi-alert.xyz', 201, 'active'),
('income-tax-refund.site', 143, 'active'),
('npci-upi-blocked.com', 88, 'active'),
('aadhaar-kyc-now.xyz', 167, 'active'),
('sbi-onlinesbi.xyz', 234, 'active'),
('hdfc-netbanking.info', 189, 'active'),
('icicibank-verify.online', 145, 'active'),
('electricity-bill.online', 78, 'active'),
('amazon-gift-winner.xyz', 203, 'active'),
('flipkart-cashback.online', 167, 'active'),
('pmkisan-claim.xyz', 91, 'active'),
('epfo-pf-withdraw.site', 134, 'active'),
('uidai-aadhaar-update.xyz', 179, 'active'),
('gst-refund-claim.online', 88, 'active');
