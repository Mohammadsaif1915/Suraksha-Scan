package com.surakshascan.service;

import com.google.gson.JsonObject;
import com.surakshascan.dao.UserReportDAO;
import com.surakshascan.model.UserReport;
import com.surakshascan.util.RiskThresholds;

public class ScanService {
    private RuleEngine ruleEngine = new RuleEngine();
    private UserReportDAO userReportDAO = new UserReportDAO();
    private GuardianService guardianService = new GuardianService();

    public JsonObject performScan(int userId, String inputType, String content) throws Exception {
        if (inputType == null || content == null) {
            throw new IllegalArgumentException("Input type and content are required.");
        }
        
        inputType = inputType.trim().toLowerCase();
        content = content.trim();
        
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty.");
        }
        if (content.length() > RiskThresholds.MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException("Content exceeds maximum allowed length.");
        }
        if (!inputType.equals("sms") && !inputType.equals("upi") && !inputType.equals("link") && !inputType.equals("image")) {
            throw new IllegalArgumentException("Unsupported input type.");
        }
        // 'image' is OCR-extracted text — treat as SMS for rule-engine matching
        if (inputType.equals("image")) {
            inputType = "sms";
        }

        JsonObject evaluationResult = ruleEngine.evaluate(inputType, content);
        
        UserReport report = new UserReport();
        report.setUserId(userId);
        report.setInputType(inputType);
        report.setRawInput(content);
        report.setRiskScore(evaluationResult.get("riskScore").getAsInt());
        report.setVerdict(evaluationResult.get("verdict").getAsString());
        report.setMatchedRules(evaluationResult.getAsJsonArray("matchedRules").toString());
        
        userReportDAO.createReport(report);
        
        if (report.getReportId() > 0 && (report.getVerdict().contains("HIGH_RISK") || report.getVerdict().contains("HIGH RISK"))) {
            try {
                guardianService.createAlertsForDependent(userId, report.getReportId(), report.getRiskScore(), report.getVerdict());
            } catch (Exception e) {
                e.printStackTrace(); // Do not fail the scan if alert fails
            }
        }
        
        JsonObject finalResponse = new JsonObject();
        finalResponse.addProperty("success", true);
        finalResponse.add("scan", evaluationResult);
        
        return finalResponse;
    }
}
