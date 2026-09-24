package com.surakshascan.dao;

import com.surakshascan.model.CommunityFlag;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminCommunityDAO {

    public List<CommunityFlag> findAll(int limit, int offset) {
        List<CommunityFlag> list = new ArrayList<>();
        String sql = "SELECT * FROM community_flags ORDER BY last_reported DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM community_flags";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public CommunityFlag findById(int id) {
        String sql = "SELECT * FROM community_flags WHERE flag_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return extract(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void setStatus(Connection conn, int id, String status) throws SQLException {
        String sql = "UPDATE community_flags SET status = ? WHERE flag_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    private CommunityFlag extract(ResultSet rs) throws SQLException {
        CommunityFlag f = new CommunityFlag();
        f.setFlagId(rs.getInt("flag_id"));
        f.setFlaggedValue(rs.getString("flagged_value"));
        f.setReportCount(rs.getInt("report_count"));
        f.setFirstReported(rs.getTimestamp("first_reported"));
        f.setLastReported(rs.getTimestamp("last_reported"));
        try { f.setStatus(rs.getString("status")); } catch (SQLException ignored) {}
        return f;
    }
}
