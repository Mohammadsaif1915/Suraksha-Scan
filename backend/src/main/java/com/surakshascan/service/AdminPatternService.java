package com.surakshascan.service;

import com.google.gson.JsonObject;
import com.surakshascan.dao.*;
import com.surakshascan.model.*;
import com.surakshascan.util.DatabaseConnection;
import java.sql.Connection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class AdminPatternService {
    private AdminPatternDAO patternDAO = new AdminPatternDAO();
    private AdminAuditLogDAO auditDAO = new AdminAuditLogDAO();

    private static final Set<String> VALID_CATEGORIES = new HashSet<>(Arrays.asList("sms","upi","link","call"));
    private static final Set<String> VALID_TYPES      = new HashSet<>(Arrays.asList("keyword","regex"));

    public List<ScamPattern> getAllPatterns() {
        return patternDAO.findAll();
    }

    public JsonObject createPattern(int adminId, JsonObject body) throws Exception {
        ScamPattern p = parseAndValidate(body, true);
        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            int newId = patternDAO.create(conn, p);
            auditDAO.log(conn, adminId, "RULE_CREATED", "scam_pattern", newId, "category=" + p.getCategory() + " type=" + p.getPatternType());
            conn.commit();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("patternId", newId);
            res.addProperty("message", "Pattern created");
            return res;
        } catch (Exception e) {
            conn.rollback(); throw e;
        } finally { conn.setAutoCommit(true); conn.close(); }
    }

    public JsonObject updatePattern(int adminId, int patternId, JsonObject body) throws Exception {
        ScamPattern existing = patternDAO.findById(patternId);
        if (existing == null) throw new IllegalArgumentException("Pattern not found");
        ScamPattern updated = parseAndValidate(body, false);
        updated.setPatternId(patternId);
        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            patternDAO.update(conn, updated);
            auditDAO.log(conn, adminId, "RULE_UPDATED", "scam_pattern", patternId, "weight=" + updated.getRiskWeight());
            conn.commit();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Pattern updated");
            return res;
        } catch (Exception e) {
            conn.rollback(); throw e;
        } finally { conn.setAutoCommit(true); conn.close(); }
    }

    public JsonObject setStatus(int adminId, int patternId, boolean active) throws Exception {
        ScamPattern existing = patternDAO.findById(patternId);
        if (existing == null) throw new IllegalArgumentException("Pattern not found");
        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            patternDAO.setActive(conn, patternId, active);
            auditDAO.log(conn, adminId, active ? "RULE_ENABLED" : "RULE_DISABLED", "scam_pattern", patternId, null);
            conn.commit();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", active ? "Pattern enabled" : "Pattern disabled");
            return res;
        } catch (Exception e) {
            conn.rollback(); throw e;
        } finally { conn.setAutoCommit(true); conn.close(); }
    }

    private ScamPattern parseAndValidate(JsonObject body, boolean requireAll) {
        String category   = getString(body, "category", "");
        String patternText= getString(body, "patternText", "");
        String patternType= getString(body, "patternType", "");
        int riskWeight    = body.has("riskWeight") ? body.get("riskWeight").getAsInt() : -1;
        String description= getString(body, "description", "");
        boolean isActive  = body.has("active") ? body.get("active").getAsBoolean() : true;

        if (!VALID_CATEGORIES.contains(category))
            throw new IllegalArgumentException("Invalid category. Allowed: sms, upi, link, call");
        if (!VALID_TYPES.contains(patternType))
            throw new IllegalArgumentException("Invalid patternType. Allowed: keyword, regex");
        if (patternText.isEmpty() || patternText.length() > 500)
            throw new IllegalArgumentException("patternText must be 1-500 chars");
        if (riskWeight < 1 || riskWeight > 100)
            throw new IllegalArgumentException("riskWeight must be 1-100");
        if (description.isEmpty() || description.length() > 1000)
            throw new IllegalArgumentException("description required, max 1000 chars");
        if ("regex".equals(patternType)) {
            try { Pattern.compile(patternText); }
            catch (Exception e) { throw new IllegalArgumentException("Invalid regular expression"); }
        }

        ScamPattern p = new ScamPattern();
        p.setCategory(category);
        p.setPatternText(patternText);
        p.setPatternType(patternType);
        p.setRiskWeight(riskWeight);
        p.setDescription(description);
        p.setActive(isActive);
        return p;
    }

    private String getString(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString().trim() : def;
    }
}
