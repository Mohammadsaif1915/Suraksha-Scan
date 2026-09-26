package com.surakshascan.dao;
import java.sql.*;
public class CommunityReportDAO {
    public boolean hasUserReported(Connection conn, int userId, String flaggedValue, String type) throws SQLException {
        String sql = "SELECT 1 FROM community_reports WHERE user_id = ? AND flagged_value = ? AND input_type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, flaggedValue);
            stmt.setString(3, type);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    public void createReport(Connection conn, int userId, String flaggedValue, String type) throws SQLException {
        String sql = "INSERT INTO community_reports (user_id, flagged_value, input_type) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, flaggedValue);
            stmt.setString(3, type);
            stmt.executeUpdate();
        }
    }
}
