package com.surakshascan.dao;

import com.surakshascan.model.FamilyLink;
import com.surakshascan.model.User;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FamilyLinkDAO {

    public boolean createRequest(int guardianId, int dependentId) {
        String sql = "INSERT INTO family_links (guardian_id, dependent_id, status) VALUES (?, ?, 'pending')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, guardianId);
            stmt.setInt(2, dependentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public FamilyLink findById(int linkId) {
        String sql = "SELECT * FROM family_links WHERE link_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, linkId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return extractLink(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public FamilyLink findByUsers(int guardianId, int dependentId) {
        String sql = "SELECT * FROM family_links WHERE guardian_id = ? AND dependent_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, guardianId);
            stmt.setInt(2, dependentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return extractLink(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FamilyLink> findPendingForDependent(int dependentId) {
        return findLinks("SELECT fl.*, u.name, u.email FROM family_links fl JOIN users u ON fl.guardian_id = u.user_id WHERE fl.dependent_id = ? AND fl.status = 'pending'", dependentId, true);
    }

    public List<FamilyLink> findActiveForGuardian(int guardianId) {
        return findLinks("SELECT fl.*, u.name, u.email FROM family_links fl JOIN users u ON fl.dependent_id = u.user_id WHERE fl.guardian_id = ? AND fl.status = 'active'", guardianId, false);
    }

    public List<FamilyLink> findActiveForDependent(int dependentId) {
        return findLinks("SELECT fl.*, u.name, u.email FROM family_links fl JOIN users u ON fl.guardian_id = u.user_id WHERE fl.dependent_id = ? AND fl.status = 'active'", dependentId, true);
    }

    public boolean updateStatus(int linkId, String status) {
        String sql = "UPDATE family_links SET status = ? WHERE link_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, linkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteLink(int linkId) {
        String sql = "DELETE FROM family_links WHERE link_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, linkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<FamilyLink> findLinks(String sql, int userId, boolean fetchGuardian) {
        List<FamilyLink> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FamilyLink link = extractLink(rs);
                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    if (fetchGuardian) link.setGuardian(user);
                    else link.setDependent(user);
                    list.add(link);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private FamilyLink extractLink(ResultSet rs) throws SQLException {
        FamilyLink link = new FamilyLink();
        link.setLinkId(rs.getInt("link_id"));
        link.setGuardianId(rs.getInt("guardian_id"));
        link.setDependentId(rs.getInt("dependent_id"));
        link.setStatus(rs.getString("status"));
        return link;
    }
}
