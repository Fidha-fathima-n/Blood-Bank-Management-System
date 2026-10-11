package com.bbms.dao;

import com.bbms.db.DBConnection;
import com.bbms.model.Administrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object: AdminDAO
 * 
 * Handles database operations for Administrator authentication and records.
 * Ensures only verified administrators can enter the system.
 */
public class AdminDAO {

    /**
     * Authenticates an administrator by username and password.
     * @param username admin username
     * @param password admin password
     * @return Administrator object if authentication succeeds, else null
     */
    public Administrator authenticate(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT AdminID, Username, Password, Role FROM Administrators WHERE Username = ? AND Password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username.trim());
            stmt.setString(2, password);

            rs = stmt.executeQuery();
            if (rs.next()) {
                Administrator admin = new Administrator();
                admin.setId(rs.getInt("AdminID"));
                admin.setName(rs.getString("Username")); // Uses Person.name for display
                admin.setUsername(rs.getString("Username"));
                admin.setPassword(rs.getString("Password"));
                admin.setRole(rs.getString("Role"));
                admin.setContact("Internal Admin");
                return admin;
            }
        } catch (SQLException e) {
            System.err.println("Admin authentication error: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return null;
    }

    /**
     * Retrieves admin details by ID
     */
    public Administrator getAdminById(int adminId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT AdminID, Username, Password, Role FROM Administrators WHERE AdminID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, adminId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Administrator admin = new Administrator();
                admin.setId(rs.getInt("AdminID"));
                admin.setName(rs.getString("Username"));
                admin.setUsername(rs.getString("Username"));
                admin.setPassword(rs.getString("Password"));
                admin.setRole(rs.getString("Role"));
                return admin;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching admin by ID: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return null;
    }
}
