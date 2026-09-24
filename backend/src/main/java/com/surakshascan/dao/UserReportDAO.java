package com.surakshascan.dao;

import com.google.gson.JsonObject;
import com.surakshascan.model.UserReport;
import com.surakshascan.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserReportDAO {
    public boolean createReport(UserReport report) {
        String sql = "INSERT INTO user_reports (user_id, input_type, raw_input, risk_score, verdict, matched_rules) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, report.getUserId());
            stmt.setString(2, report.getInputType());
            stmt.setString(3, report.getRawInput());
            stmt.setInt(4, report.getRiskScore());
            stmt.setString(5, report.getVerdict());
            stmt.setString(6, report.getMatchedRules());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        report.setReportId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<UserReport> findByUserId(int userId, int limit, int offset) {
        List<UserReport> reports = new ArrayList<>();
        String sql = "SELECT report_id, input_type, risk_score, verdict, created_at FROM user_reports WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UserReport r = new UserReport();
                    r.setReportId(rs.getInt("report_id"));
                    r.setInputType(rs.getString("input_type"));
                    r.setRiskScore(rs.getInt("risk_score"));
                    r.setVerdict(rs.getString("verdict"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    reports.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public UserReport findByIdAndUserId(int reportId, int userId) {
        String sql = "SELECT * FROM user_reports WHERE report_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reportId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserReport r = new UserReport();
                    r.setReportId(rs.getInt("report_id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setInputType(rs.getString("input_type"));
                    r.setRawInput(rs.getString("raw_input"));
                    r.setRiskScore(rs.getInt("risk_score"));
                    r.setVerdict(rs.getString("verdict"));
                    r.setMatchedRules(rs.getString("matched_rules"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    return r;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countTotalByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM user_reports WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public JsonObject countByVerdict(int userId) {
        JsonObject counts = new JsonObject();
        counts.addProperty("safe", 0);
        counts.addProperty("suspicious", 0);
        counts.addProperty("highRisk", 0);

        String sql = "SELECT verdict, COUNT(*) as cnt FROM user_reports WHERE user_id = ? GROUP BY verdict";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String v = rs.getString("verdict");
                    int c = rs.getInt("cnt");
                    if ("SAFE".equalsIgnoreCase(v)) counts.addProperty("safe", c);
                    else if ("SUSPICIOUS".equalsIgnoreCase(v)) counts.addProperty("suspicious", c);
                    else if ("HIGH RISK".equalsIgnoreCase(v) || "HIGH_RISK".equalsIgnoreCase(v)) counts.addProperty("highRisk", c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }
}
