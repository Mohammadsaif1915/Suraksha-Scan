package com.surakshascan.service;
import com.google.gson.JsonObject;
import com.surakshascan.dao.CommunityFlagDAO;
import com.surakshascan.dao.CommunityReportDAO;
import com.surakshascan.model.CommunityFlag;
import com.surakshascan.util.DatabaseConnection;
import com.surakshascan.util.InputNormalizer;
import java.sql.Connection;
import java.sql.SQLException;

public class CommunityReportService {
    private CommunityFlagDAO flagDAO = new CommunityFlagDAO();
    private CommunityReportDAO reportDAO = new CommunityReportDAO();

    public JsonObject reportItem(int userId, String type, String value) throws Exception {
        if (type == null || value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Type and value are required.");
        }
        
        type = type.trim().toLowerCase();
        if (!type.equals("phone") && !type.equals("upi") && !type.equals("domain")) {
            throw new IllegalArgumentException("Invalid type. Allowed: phone, upi, domain.");
        }
        if (value.length() > 255) throw new IllegalArgumentException("Value too long.");

        String normalizedValue = "";
        if (type.equals("domain")) normalizedValue = InputNormalizer.extractDomain(value);
        else if (type.equals("upi")) normalizedValue = InputNormalizer.normalizeUpi(value);
        else if (type.equals("phone")) normalizedValue = InputNormalizer.normalizePhone(value);

        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("Invalid input value.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            if (reportDAO.hasUserReported(conn, userId, normalizedValue, type)) {
                conn.rollback();
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("status", 409);
                err.addProperty("message", "You have already reported this item");
                return err;
            }
            
            reportDAO.createReport(conn, userId, normalizedValue, type);
            flagDAO.incrementOrInsertFlag(conn, normalizedValue);
            
            CommunityFlag updatedFlag = flagDAO.findByFlaggedValue(conn, normalizedValue);
            conn.commit();
            
            JsonObject communityData = new JsonObject();
            communityData.addProperty("type", type);
            communityData.addProperty("reportCount", updatedFlag.getReportCount());
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("status", 201);
            result.addProperty("message", "Report submitted successfully");
            result.add("community", communityData);
            return result;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public JsonObject checkItem(String type, String value) {
        if (type == null || value == null) return createNotFound();
        type = type.trim().toLowerCase();
        
        String normalizedValue = "";
        if (type.equals("domain")) normalizedValue = InputNormalizer.extractDomain(value);
        else if (type.equals("upi")) normalizedValue = InputNormalizer.normalizeUpi(value);
        else if (type.equals("phone")) normalizedValue = InputNormalizer.normalizePhone(value);
        
        CommunityFlag flag = flagDAO.findByFlaggedValue(normalizedValue);
        if (flag != null) {
            JsonObject communityData = new JsonObject();
            communityData.addProperty("type", type);
            communityData.addProperty("reportCount", flag.getReportCount());
            communityData.addProperty("firstReported", flag.getFirstReported().toString());
            communityData.addProperty("lastReported", flag.getLastReported().toString());
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("found", true);
            result.add("community", communityData);
            return result;
        }
        return createNotFound();
    }
    
    private JsonObject createNotFound() {
        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.addProperty("found", false);
        return result;
    }
}
