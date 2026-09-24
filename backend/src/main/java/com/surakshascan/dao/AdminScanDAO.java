package com.surakshascan.dao;

import com.surakshascan.model.UserReport;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminScanDAO {

    public List<UserReport> findAll(String verdict, String inputType, int limit, int offset) {
        List<UserReport> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.report_id, r.input_type, r.verdict, r.risk_score, r.created_at, u.name AS user_name " +
            "FROM user_reports r LEFT JOIN users u ON r.user_id = u.user_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (verdict != null && !verdict.isEmpty() && !verdict.equals("all")) {
            sql.append(" AND r.verdict = ?");
            params.add(verdict);
        }
        if (inputType != null && !inputType.isEmpty() && !inputType.equals("all")) {
            sql.append(" AND r.input_type = ?");
            params.add(inputType);
        }
        sql.append(" ORDER BY r.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit); params.add(offset);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) stmt.setInt(i + 1, (Integer) params.get(i));
                else stmt.setString(i + 1, (String) params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UserReport r = new UserReport();
                    r.setReportId(rs.getInt("report_id"));
                    r.setInputType(rs.getString("input_type"));
                    r.setVerdict(rs.getString("verdict"));
                    r.setRiskScore(rs.getInt("risk_score"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    r.setUserName(rs.getString("user_name"));
                    list.add(r);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countAll(String verdict, String inputType) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM user_reports WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (verdict != null && !verdict.isEmpty() && !verdict.equals("all")) {
            sql.append(" AND verdict = ?"); params.add(verdict);
        }
        if (inputType != null && !inputType.isEmpty() && !inputType.equals("all")) {
            sql.append(" AND input_type = ?"); params.add(inputType);
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setString(i + 1, (String) params.get(i));
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public UserReport findById(int id) {
        String sql = "SELECT r.*, u.name AS user_name FROM user_reports r LEFT JOIN users u ON r.user_id = u.user_id WHERE r.report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserReport r = new UserReport();
                    r.setReportId(rs.getInt("report_id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setUserName(rs.getString("user_name"));
                    r.setInputType(rs.getString("input_type"));
                    r.setRawInput(rs.getString("raw_input"));
                    r.setVerdict(rs.getString("verdict"));
                    r.setRiskScore(rs.getInt("risk_score"));
                    r.setMatchedRules(rs.getString("matched_rules"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    return r;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
