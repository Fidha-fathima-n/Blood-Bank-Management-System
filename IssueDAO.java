package com.bbms.dao;

import com.bbms.db.DBConnection;
import com.bbms.model.BloodIssue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object: IssueDAO
 * 
 * Manages blood issue transactions:
 * 1. Verifies blood stock availability first.
 * 2. Automatically decrements inventory stock.
 * 3. Logs issue transaction in Issues table.
 */
public class IssueDAO {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Issues blood units transactionally.
     * Checks availability, deducts stock, and records the issue.
     * 
     * @param issue BloodIssue details
     * @return true if successful; false if insufficient stock or error
     */
    public boolean recordIssue(BloodIssue issue) {
        Connection conn = null;
        PreparedStatement stmtIssue = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Check current inventory availability
            int currentStock = inventoryDAO.getStock(issue.getBloodGroup());
            if (currentStock < issue.getQuantity()) {
                conn.rollback();
                System.err.println("Insufficient blood units available for " + issue.getBloodGroup() +
                        ". Requested: " + issue.getQuantity() + ", Available: " + currentStock);
                return false;
            }

            // 2. Decrement inventory stock (-quantity)
            boolean stockDeducted = inventoryDAO.updateStock(conn, issue.getBloodGroup(), -issue.getQuantity());
            if (!stockDeducted) {
                conn.rollback();
                return false;
            }

            // 3. Insert record into Issues table
            String sql = "INSERT INTO Issues (BloodGroup, Quantity, Date, AdminID, RecipientInfo) VALUES (?, ?, ?, ?, ?)";
            stmtIssue = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmtIssue.setString(1, issue.getBloodGroup().trim());
            stmtIssue.setInt(2, issue.getQuantity());
            stmtIssue.setDate(3, issue.getDate());
            if (issue.getAdminID() > 0) {
                stmtIssue.setInt(4, issue.getAdminID());
            } else {
                stmtIssue.setNull(4, java.sql.Types.INTEGER);
            }
            stmtIssue.setString(5, issue.getRecipientInfo());

            int affected = stmtIssue.executeUpdate();
            if (affected == 0) {
                conn.rollback();
                return false;
            }

            // Commit transaction
            conn.commit();

            ResultSet rsKeys = stmtIssue.getGeneratedKeys();
            if (rsKeys.next()) {
                issue.setIssueID(rsKeys.getInt(1));
            }
            rsKeys.close();

            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("Transaction failed while issuing blood: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            DBConnection.close(conn, stmtIssue);
        }
    }

    /**
     * Retrieves all blood issue records with administrator username.
     */
    public List<BloodIssue> getAllIssues() {
        List<BloodIssue> list = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT i.IssueID, i.BloodGroup, i.Quantity, i.Date, i.AdminID, i.RecipientInfo, " +
                         "a.Username AS AdminName " +
                         "FROM Issues i " +
                         "LEFT JOIN Administrators a ON i.AdminID = a.AdminID " +
                         "ORDER BY i.IssueID DESC";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                BloodIssue issue = new BloodIssue(
                    rs.getInt("IssueID"),
                    rs.getString("BloodGroup"),
                    rs.getInt("Quantity"),
                    rs.getDate("Date"),
                    rs.getInt("AdminID"),
                    rs.getString("RecipientInfo")
                );
                issue.setAdminUsername(rs.getString("AdminName"));
                list.add(issue);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching issues: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return list;
    }

    /**
     * Returns total count of blood issue transactions.
     */
    public int getTotalIssuesCount() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Issues");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting issue count: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }

    /**
     * Computes total quantity of units issued.
     */
    public int getTotalUnitsIssued() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT SUM(Quantity) FROM Issues");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total issued units: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }
}
