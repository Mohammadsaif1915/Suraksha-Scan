package com.surakshascan.dao;

import com.surakshascan.model.GuardianAlert;
import com.surakshascan.model.User;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuardianAlertDAO {

    public boolean createAlert(GuardianAlert alert) {
        String sql = "INSERT INTO guardian_alerts (link_id, report_id, guardian_id, dependent_id, alert_type, title, message, risk_score, verdict) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alert.getLinkId());
            stmt.setInt(2, alert.getReportId());
            stmt.setInt(3, alert.getGuardianId());
            stmt.setInt(4, alert.getDependentId());
            stmt.setString(5, alert.getAlertType());
            stmt.setString(6, alert.getTitle());
            stmt.setString(7, alert.getMessage());
            stmt.setInt(8, alert.getRiskScore());
            stmt.setString(9, alert.getVerdict());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<GuardianAlert> findByGuardianId(int guardianId) {
        List<GuardianAlert> alerts = new ArrayList<>();
        String sql = "SELECT a.*, u.name as dep_name FROM guardian_alerts a JOIN users u ON a.dependent_id = u.user_id WHERE a.guardian_id = ? ORDER BY a.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, guardianId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GuardianAlert a = extractAlert(rs);
                    User dep = new User();
                    dep.setName(rs.getString("dep_name"));
                    a.setDependent(dep);
                    alerts.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alerts;
    }

    public GuardianAlert findByIdForGuardian(int alertId, int guardianId) {
        String sql = "SELECT a.*, u.name as dep_name FROM guardian_alerts a JOIN users u ON a.dependent_id = u.user_id WHERE a.alert_id = ? AND a.guardian_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alertId);
            stmt.setInt(2, guardianId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    GuardianAlert a = extractAlert(rs);
                    User dep = new User();
                    dep.setName(rs.getString("dep_name"));
                    a.setDependent(dep);
                    return a;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean markAsRead(int alertId, int guardianId) {
        String sql = "UPDATE guardian_alerts SET is_read = TRUE WHERE alert_id = ? AND guardian_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alertId);
            stmt.setInt(2, guardianId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countUnread(int guardianId) {
        String sql = "SELECT COUNT(*) FROM guardian_alerts WHERE guardian_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, guardianId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private GuardianAlert extractAlert(ResultSet rs) throws SQLException {
        GuardianAlert a = new GuardianAlert();
        a.setAlertId(rs.getInt("alert_id"));
        a.setLinkId(rs.getInt("link_id"));
        a.setReportId(rs.getInt("report_id"));
        a.setGuardianId(rs.getInt("guardian_id"));
        a.setDependentId(rs.getInt("dependent_id"));
        a.setAlertType(rs.getString("alert_type"));
        a.setTitle(rs.getString("title"));
        a.setMessage(rs.getString("message"));
        a.setRiskScore(rs.getInt("risk_score"));
        a.setVerdict(rs.getString("verdict"));
        a.setRead(rs.getBoolean("is_read"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
