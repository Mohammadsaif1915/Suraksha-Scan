package com.surakshascan.service;

import com.google.gson.JsonObject;
import com.surakshascan.dao.AdminCommunityDAO;
import com.surakshascan.dao.AdminAuditLogDAO;
import com.surakshascan.model.CommunityFlag;
import com.surakshascan.util.DatabaseConnection;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AdminCommunityService {
    private AdminCommunityDAO communityDAO = new AdminCommunityDAO();
    private AdminAuditLogDAO auditDAO = new AdminAuditLogDAO();

    private static final Set<String> VALID_STATUSES = new HashSet<>(Arrays.asList("active","removed"));

    public JsonObject setFlagStatus(int adminId, int flagId, String status) throws Exception {
        if (!VALID_STATUSES.contains(status))
            throw new IllegalArgumentException("Invalid status. Allowed: active, removed");
        CommunityFlag flag = communityDAO.findById(flagId);
        if (flag == null) throw new IllegalArgumentException("Flag not found");

        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            communityDAO.setStatus(conn, flagId, status);
            String action = "removed".equals(status) ? "COMMUNITY_FLAG_REMOVED" : "COMMUNITY_FLAG_RESTORED";
            auditDAO.log(conn, adminId, action, "community_flag", flagId, "value=" + flag.getFlaggedValue());
            conn.commit();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "removed".equals(status) ? "Flag removed" : "Flag restored");
            return res;
        } catch (Exception e) {
            conn.rollback(); throw e;
        } finally { conn.setAutoCommit(true); conn.close(); }
    }
}
