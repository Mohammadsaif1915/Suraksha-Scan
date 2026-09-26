-- =============================================================
-- SurakshaScan — Live Database Patch
-- Purpose: Add missing UPI scam patterns to fix incorrect SAFE
--          verdicts for UPI IDs containing fraud/scam keywords.
-- Run this directly against your Aiven MySQL database.
-- =============================================================

-- UPI patterns missing from original seed
INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES

-- ❌ CRITICAL: Explicit scam-word UPIs (catches fraud@upi.sbi, scam@paytm, etc.)
('upi', '(?i)(fraud|scam|cheat|hack|steal|fake|phish|malicious|suspicious|criminal)', 'regex', 99,
 'CRITICAL: UPI handle explicitly contains a scam-indicator keyword (fraud/scam/cheat/fake/phish). No legitimate VPA ever contains these words.', true),

-- ❌ CRITICAL: Credential-harvesting keywords in UPI
('upi', '(?i)(otp|pin|password|credentials|login|secret|cvv)', 'regex', 95,
 'CRITICAL: UPI ID contains sensitive credential keywords. Legitimate payment handles never reference OTP/PIN/CVV.', true),

-- ⚠️ HIGH RISK: Account threat keywords
('upi', '(?i)(block|suspend|deactivate|expire|restrict|disable|freeze)', 'regex', 60,
 'Account threat keywords in UPI handle — used to create panic-urgency in victims.', true),

-- ⚠️ HIGH RISK: Fake loan/credit lure keywords
('upi', '(?i)(free|loan|credit|approved|sanctioned|disburse|instant)', 'regex', 55,
 'Financial lure keywords in UPI handle — common in fake loan approval and easy-credit scams.', true),

-- ⚠️ SUSPICIOUS: Impersonation keywords not already covered
('upi', '(?i)(kyc|verification|verified|authenticate|compliance)', 'regex', 65,
 'KYC/compliance-related UPI handle — NPCI/Banks never collect KYC via UPI collect requests.', true),

-- ⚠️ SUSPICIOUS: Courier/delivery fraud UPI
('upi', '(?i)(courier|delivery|parcel|shipment|customs|package|dispatch)', 'regex', 60,
 'Delivery/courier impersonation UPI ID — used in parcel scams where victim pays fake customs fee.', true);
