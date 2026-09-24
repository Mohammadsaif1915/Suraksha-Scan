package com.surakshascan.dao;

import com.google.gson.JsonObject;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;

public class AdminStatsDAO {

    public JsonObject getStats() {
        JsonObject stats = new JsonObject();
        try (Connection conn = DatabaseConnection.getConnection()) {
            stats.addProperty("totalUsers",        count(conn, "SELECT COUNT(*) FROM users"));
            stats.addProperty("totalScans",        count(conn, "SELECT COUNT(*) FROM user_reports"));
            stats.addProperty("highRiskScans",     count(conn, "SELECT COUNT(*) FROM user_reports WHERE verdict = 'HIGH RISK'"));
            stats.addProperty("communityFlags",    count(conn, "SELECT COUNT(*) FROM community_flags WHERE status = 'active'"));
            stats.addProperty("pendingFamilyLinks",count(conn, "SELECT COUNT(*) FROM family_links WHERE status = 'pending'"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    private int count(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
