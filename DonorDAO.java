package com.bbms.dao;

import com.bbms.db.DBConnection;
import com.bbms.model.Donor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object: DonorDAO
 * 
 * Manages all persistence operations for the Donors table.
 * All methods require an active administrative session.
 */
public class DonorDAO {

    /**
     * Adds a new donor to the database.
     * @param donor Donor entity
     * @return true if inserted successfully
     */
    public boolean addDonor(Donor donor) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO Donors (Name, BloodGroup, Contact) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, donor.getName().trim());
            stmt.setString(2, donor.getBloodGroup().trim());
            stmt.setString(3, donor.getContact().trim());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    donor.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding donor: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return false;
    }

    /**
     * Updates an existing donor's details.
     */
    public boolean updateDonor(Donor donor) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE Donors SET Name = ?, BloodGroup = ?, Contact = ? WHERE DonorID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, donor.getName().trim());
            stmt.setString(2, donor.getBloodGroup().trim());
            stmt.setString(3, donor.getContact().trim());
            stmt.setInt(4, donor.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating donor: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt);
        }
        return false;
    }

    /**
     * Deletes a donor by ID. (Cascades to donations if configured)
     */
    public boolean deleteDonor(int donorId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM Donors WHERE DonorID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, donorId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting donor: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt);
        }
        return false;
    }

    /**
     * Fetches all registered donors.
     */
    public List<Donor> getAllDonors() {
        List<Donor> list = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT DonorID, Name, BloodGroup, Contact FROM Donors ORDER BY DonorID DESC");
            while (rs.next()) {
                Donor d = new Donor(
                    rs.getInt("DonorID"),
                    rs.getString("Name"),
                    rs.getString("BloodGroup"),
                    rs.getString("Contact")
                );
                list.add(d);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching donors: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return list;
    }

    /**
     * Searches donors filtered by blood group.
     */
    public List<Donor> getDonorsByBloodGroup(String bloodGroup) {
        List<Donor> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT DonorID, Name, BloodGroup, Contact FROM Donors WHERE BloodGroup = ? ORDER BY DonorID DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, bloodGroup.trim());
            rs = stmt.executeQuery();
            while (rs.next()) {
                Donor d = new Donor(
                    rs.getInt("DonorID"),
                    rs.getString("Name"),
                    rs.getString("BloodGroup"),
                    rs.getString("Contact")
                );
                list.add(d);
            }
        } catch (SQLException e) {
            System.err.println("Error searching donors by blood group: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return list;
    }

    /**
     * Finds a single donor by DonorID.
     */
    public Donor getDonorById(int donorId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT DonorID, Name, BloodGroup, Contact FROM Donors WHERE DonorID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, donorId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return new Donor(
                    rs.getInt("DonorID"),
                    rs.getString("Name"),
                    rs.getString("BloodGroup"),
                    rs.getString("Contact")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching donor by ID: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return null;
    }

    /**
     * Returns total registered donor count.
     */
    public int getTotalDonorsCount() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Donors");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting donor count: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }

    /**
     * Counts how many donations a particular donor has made.
     */
    public int getDonationCountByDonor(int donorId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM Donations WHERE DonorID = ?");
            stmt.setInt(1, donorId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting donor donation count: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
        return 0;
    }
}
