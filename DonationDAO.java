package com.bbms.dao;

import com.bbms.db.DBConnection;
import com.bbms.model.Donation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object: DonationDAO
 * 
 * Handles donation logs and maintains transactional consistency:
 * Recording a donation automatically increases the corresponding blood group's stock.
 */
public class DonationDAO {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Records a new blood donation.
     * Uses a JDBC transaction to ensure that the donation record and the
     * inventory increment occur atomically.
     * 
     * @param donation Donation details
     * @return true if both donation and stock increment succeeded
     */
    public boolean recordDonation(Donation donation) {
        Connection conn = null;
        PreparedStatement stmtDonation = null;
        PreparedStatement stmtDonor = null;
        ResultSet rsDonor = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Fetch donor's blood group
            String donorSql = "SELECT BloodGroup, Name FROM Donors WHERE DonorID = ?";
            stmtDonor = conn.prepareStatement(donorSql);
            stmtDonor.setInt(1, donation.getDonorID());
            rsDonor = stmtDonor.executeQuery();

            if (!rsDonor.next()) {
                conn.rollback();
                System.err.println("Donor not found for ID: " + donation.getDonorID());
                return false;
            }
            String bloodGroup = rsDonor.getString("BloodGroup");

            // 2. Insert into Donations table
            String donationSql = "INSERT INTO Donations (DonorID, Date, Quantity) VALUES (?, ?, ?)";
            stmtDonation = conn.prepareStatement(donationSql, Statement.RETURN_GENERATED_KEYS);
            stmtDonation.setInt(1, donation.getDonorID());
            stmtDonation.setDate(2, donation.getDate());
            stmtDonation.setInt(3, donation.getQuantity());
            int affected = stmtDonation.executeUpdate();

            if (affected == 0) {
                conn.rollback();
                return false;
            }

            // 3. Atomically increment blood stock in Inventory
            boolean stockUpdated = inventoryDAO.updateStock(conn, bloodGroup, donation.getQuantity());
            if (!stockUpdated) {
                conn.rollback();
                return false;
            }

            // Commit transaction
            conn.commit();

            ResultSet rsKeys = stmtDonation.getGeneratedKeys();
            if (rsKeys.next()) {
                donation.setDonationID(rsKeys.getInt(1));
            }
            rsKeys.close();

            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("Transaction failed while recording donation: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            DBConnection.close(null, stmtDonor, rsDonor);
            DBConnection.close(conn, stmtDonation);
        }
    }

    /**
     * Retrieves all donation history with donor names and blood groups.
     */
    public List<Donation> getAllDonations() {
        List<Donation> list = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT d.DonationID, d.DonorID, d.Date, d.Quantity, " +
                         "dn.Name AS DonorName, dn.BloodGroup " +
                         "FROM Donations d " +
                         "JOIN Donors dn ON d.DonorID = dn.DonorID " +
                         "ORDER BY d.DonationID DESC";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Donation d = new Donation(
                    rs.getInt("DonationID"),
                    rs.getInt("DonorID"),
                    rs.getDate("Date"),
                    rs.getInt("Quantity")
                );
                d.setDonorName(rs.getString("DonorName"));
                d.setBloodGroup(rs.getString("BloodGroup"));
                list.add(d);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching donations: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return list;
    }

    /**
     * Counts total donations made.
     */
    public int getTotalDonationsCount() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Donations");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total donations count: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }

    /**
     * Computes total quantity of units donated.
     */
    public int getTotalUnitsDonated() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT SUM(Quantity) FROM Donations");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total donated units: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }
}
