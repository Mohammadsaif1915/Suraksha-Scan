package com.surakshascan.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.surakshascan.dao.CommunityFlagDAO;
import com.surakshascan.dao.ScamPatternDAO;
import com.surakshascan.model.CommunityFlag;
import com.surakshascan.model.ScamPattern;
import com.surakshascan.util.InputNormalizer;
import com.surakshascan.util.RiskThresholds;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

public class RuleEngine {
    private ScamPatternDAO scamPatternDAO = new ScamPatternDAO();
    private CommunityFlagDAO communityFlagDAO = new CommunityFlagDAO();

    public JsonObject evaluate(String inputType, String rawInput) {
        String normalizedInput = InputNormalizer.normalize(rawInput);
        List<ScamPattern> patterns = scamPatternDAO.findByCategory(inputType);
        
        int totalScore = 0;
        JsonArray matchedRulesArray = new JsonArray();
        
        for (ScamPattern pattern : patterns) {
            boolean matched = false;
            if ("regex".equalsIgnoreCase(pattern.getPatternType())) {
                try {
                    Pattern p = Pattern.compile(pattern.getPatternText(), Pattern.CASE_INSENSITIVE);
                    if (p.matcher(normalizedInput).find()) {
                        matched = true;
                    }
                } catch (Exception e) {}
            } else if ("keyword".equalsIgnoreCase(pattern.getPatternType())) {
                if (normalizedInput.contains(pattern.getPatternText().toLowerCase())) {
                    matched = true;
                }
            }
            if (matched) {
                totalScore += pattern.getRiskWeight();
                JsonObject matchObj = new JsonObject();
                matchObj.addProperty("category", pattern.getCategory());
                matchObj.addProperty("description", pattern.getDescription());
                matchObj.addProperty("scoreContribution", pattern.getRiskWeight());
                matchedRulesArray.add(matchObj);
            }
        }
        
        Set<String> identifiersToCheck = new HashSet<>();
        if ("link".equalsIgnoreCase(inputType)) {
            identifiersToCheck.add(InputNormalizer.extractDomain(rawInput));
        } else if ("upi".equalsIgnoreCase(inputType)) {
            identifiersToCheck.add(InputNormalizer.normalizeUpi(rawInput));
        } else if ("sms".equalsIgnoreCase(inputType)) {
            // Extract UPIs
            Matcher mUpi = Pattern.compile("[a-zA-Z0-9.\\-_]+@[a-zA-Z]+").matcher(rawInput);
            while (mUpi.find()) identifiersToCheck.add(InputNormalizer.normalizeUpi(mUpi.group()));
            // Extract Domains
            Matcher mDom = Pattern.compile("https?://([a-zA-Z0-9.-]+)").matcher(rawInput);
            while (mDom.find()) identifiersToCheck.add(InputNormalizer.extractDomain(mDom.group()));
            // Extract Phone (simple)
            Matcher mPhone = Pattern.compile("\\+?[0-9]{10,13}").matcher(InputNormalizer.normalizePhone(rawInput));
            while (mPhone.find()) identifiersToCheck.add(mPhone.group());
        }
        
        for (String idToCheck : identifiersToCheck) {
            CommunityFlag flag = communityFlagDAO.findByFlaggedValue(idToCheck);
            if (flag != null && flag.getReportCount() > 0) {
                int count = flag.getReportCount();
                int communityScore = 0;
                if (count >= 10) communityScore = 40;
                else if (count >= 5) communityScore = 30;
                else if (count >= 3) communityScore = 20;
                else if (count >= 1) communityScore = 10;
                
                totalScore += communityScore;
                
                JsonObject communityObj = new JsonObject();
                communityObj.addProperty("category", "community");
                communityObj.addProperty("description", "Community reports: this identifier has been reported by " + count + " user(s).");
                communityObj.addProperty("scoreContribution", communityScore);
                matchedRulesArray.add(communityObj);
                break; // Limit to one community flag application per scan to avoid score explosion
            }
        }
        
        String verdict;
        if (totalScore >= RiskThresholds.HIGH_RISK_MIN) verdict = "HIGH RISK";
        else if (totalScore >= RiskThresholds.SUSPICIOUS_MIN) verdict = "SUSPICIOUS";
        else verdict = "SAFE";
        
        String summary = verdict.equals("SAFE") ? 
            "No known scam indicators were detected." : 
            "This input contains multiple indicators commonly associated with scams.";
            
        JsonObject result = new JsonObject();
        result.addProperty("inputType", inputType);
        result.addProperty("verdict", verdict);
        result.addProperty("riskScore", totalScore);
        result.addProperty("summary", summary);
        result.add("matchedRules", matchedRulesArray);
        
        return result;
    }
}
