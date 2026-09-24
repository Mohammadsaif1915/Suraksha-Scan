package com.surakshascan.dao;
import com.surakshascan.model.CommunityFlag;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;

public class CommunityFlagDAO {
    public CommunityFlag findByFlaggedValue(String value) {
        String sql = "SELECT * FROM community_flags WHERE flagged_value = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return extractFlag(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public CommunityFlag findByFlaggedValue(Connection conn, String value) throws SQLException {
        String sql = "SELECT * FROM community_flags WHERE flagged_value = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return extractFlag(rs);
            }
        }
        return null;
    }
    
    public void incrementOrInsertFlag(Connection conn, String value) throws SQLException {
        CommunityFlag flag = findByFlaggedValue(conn, value);
        if (flag != null) {
            String sql = "UPDATE community_flags SET report_count = report_count + 1, last_reported = CURRENT_TIMESTAMP WHERE flagged_value = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, value);
                stmt.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO community_flags (flagged_value, report_count, first_reported, last_reported) VALUES (?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, value);
                stmt.executeUpdate();
            }
        }
    }

    private CommunityFlag extractFlag(ResultSet rs) throws SQLException {
        CommunityFlag flag = new CommunityFlag();
        flag.setFlagId(rs.getInt("flag_id"));
        flag.setFlaggedValue(rs.getString("flagged_value"));
        flag.setReportCount(rs.getInt("report_count"));
        flag.setFirstReported(rs.getTimestamp("first_reported"));
        flag.setLastReported(rs.getTimestamp("last_reported"));
        return flag;
    }
}
