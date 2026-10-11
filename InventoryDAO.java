package com.bbms.dao;

import com.bbms.db.DBConnection;
import com.bbms.model.BloodInventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object: InventoryDAO
 * 
 * Manages the live blood stock for all 8 standard blood groups:
 * A+, A-, B+, B-, AB+, AB-, O+, O-.
 * Enforces atomic stock increments on donations and stock decrements on issues.
 */
public class InventoryDAO {

    /**
     * Retrieves stock levels for all blood groups.
     */
    public List<BloodInventory> getAllInventory() {
        List<BloodInventory> list = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT BloodGroup, TotalQuantity FROM Inventory ORDER BY BloodGroup ASC");
            while (rs.next()) {
                list.add(new BloodInventory(
                    rs.getString("BloodGroup"),
                    rs.getInt("TotalQuantity")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching inventory: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return list;
    }

    /**
     * Gets available quantity for a specific blood group.
     */
    public int getStock(String bloodGroup) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT TotalQuantity FROM Inventory WHERE BloodGroup = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, bloodGroup.trim());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("TotalQuantity");
            }
        } catch (SQLException e) {
            System.err.println("Error getting stock for " + bloodGroup + ": " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }

    /**
     * Checks if requested blood quantity is available.
     */
    public boolean isAvailable(String bloodGroup, int requestedQuantity) {
        if (requestedQuantity <= 0) return false;
        int currentStock = getStock(bloodGroup);
        return currentStock >= requestedQuantity;
    }

    /**
     * Updates blood stock by adding/subtracting delta.
     * @param conn active Connection (allows transactional usage)
     * @param bloodGroup blood group to update
     * @param delta positive for addition, negative for deduction
     */
    public boolean updateStock(Connection conn, String bloodGroup, int delta) throws SQLException {
        String sql = "UPDATE Inventory SET TotalQuantity = TotalQuantity + ? WHERE BloodGroup = ? AND (TotalQuantity + ?) >= 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, delta);
            stmt.setString(2, bloodGroup.trim());
            stmt.setInt(3, delta);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Non-transactional convenience stock modification
     */
    public boolean updateStock(String bloodGroup, int delta) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            return updateStock(conn, bloodGroup, delta);
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, null);
        }
    }

    /**
     * Directly overrides stock quantity (with validation > 0)
     */
    public boolean setStock(String bloodGroup, int newQuantity) {
        if (newQuantity < 0) return false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE Inventory SET TotalQuantity = ? WHERE BloodGroup = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, newQuantity);
            stmt.setString(2, bloodGroup.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error setting stock: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(conn, stmt);
        }
    }

    /**
     * Computes the sum of all blood units across all groups.
     */
    public int getTotalUnitsAvailable() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT SUM(TotalQuantity) FROM Inventory");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total units: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }
}
