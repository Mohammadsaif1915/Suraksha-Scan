package com.surakshascan.dao;

import com.surakshascan.model.AdminAuditLog;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminAuditLogDAO {

    public void log(Connection conn, int adminId, String action, String entityType, int entityId, String details) throws SQLException {
        String sql = "INSERT INTO admin_audit_logs (admin_id, action, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adminId);
            stmt.setString(2, action);
            stmt.setString(3, entityType);
            stmt.setInt(4, entityId);
            stmt.setString(5, details);
            stmt.executeUpdate();
        }
    }

    public List<AdminAuditLog> findAll(int limit, int offset) {
        List<AdminAuditLog> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.name AS admin_name FROM admin_audit_logs l " +
                     "JOIN users u ON l.admin_id = u.user_id " +
                     "ORDER BY l.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AdminAuditLog log = new AdminAuditLog();
                    log.setLogId(rs.getInt("log_id"));
                    log.setAdminId(rs.getInt("admin_id"));
                    log.setAdminName(rs.getString("admin_name"));
                    log.setAction(rs.getString("action"));
                    log.setEntityType(rs.getString("entity_type"));
                    log.setEntityId(rs.getInt("entity_id"));
                    log.setDetails(rs.getString("details"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM admin_audit_logs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
