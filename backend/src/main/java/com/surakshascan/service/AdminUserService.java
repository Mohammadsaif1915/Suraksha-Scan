package com.surakshascan.service;

import com.google.gson.JsonObject;
import com.surakshascan.dao.AdminUserDAO;
import com.surakshascan.dao.AdminAuditLogDAO;
import com.surakshascan.model.User;
import com.surakshascan.util.DatabaseConnection;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AdminUserService {
    private AdminUserDAO userDAO = new AdminUserDAO();
    private AdminAuditLogDAO auditDAO = new AdminAuditLogDAO();

    // Admins may only toggle normal <-> guardian; promoting to admin is prohibited
    private static final Set<String> CHANGEABLE_ROLES = new HashSet<>(Arrays.asList("normal","guardian"));

    public JsonObject setRole(int adminId, int targetUserId, String newRole) throws Exception {
        if (!CHANGEABLE_ROLES.contains(newRole))
            throw new IllegalArgumentException("Role change to 'admin' is not permitted via this interface.");

        User target = userDAO.findById(targetUserId);
        if (target == null) throw new IllegalArgumentException("User not found");
        if ("admin".equals(target.getRole()))
            throw new IllegalArgumentException("Admin accounts cannot have their role changed here.");

        // Protect last admin
        if ("admin".equals(target.getRole()) && userDAO.countAdmins() <= 1)
            throw new IllegalArgumentException("Cannot demote the last admin account.");

        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            userDAO.setRole(conn, targetUserId, newRole);
            auditDAO.log(conn, adminId, "USER_ROLE_CHANGED", "user", targetUserId,
                "from=" + target.getRole() + " to=" + newRole);
            conn.commit();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Role updated to " + newRole);
            return res;
        } catch (Exception e) {
            conn.rollback(); throw e;
        } finally { conn.setAutoCommit(true); conn.close(); }
    }
}
