package com.bbms.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Initializer Utility
 * 
 * Automatically verifies schema presence and creates the 5 required tables
 * with seed inventory and administrator credentials if they are not already created.
 * This ensures the application runs smoothly out of the box.
 */
public class DBInitializer {

    public static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();

            // 1. Create Administrators table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Administrators (" +
                "  AdminID INT AUTO_INCREMENT PRIMARY KEY," +
                "  Username VARCHAR(50) NOT NULL UNIQUE," +
                "  Password VARCHAR(100) NOT NULL," +
                "  Role VARCHAR(50) NOT NULL DEFAULT 'Administrator'" +
                ") ENGINE=InnoDB;"
            );

            // 2. Create Donors table (No Patient table)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Donors (" +
                "  DonorID INT AUTO_INCREMENT PRIMARY KEY," +
                "  Name VARCHAR(100) NOT NULL," +
                "  BloodGroup VARCHAR(5) NOT NULL," +
                "  Contact VARCHAR(20) NOT NULL" +
                ") ENGINE=InnoDB;"
            );

            // 3. Create Inventory table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Inventory (" +
                "  BloodGroup VARCHAR(5) PRIMARY KEY," +
                "  TotalQuantity INT NOT NULL DEFAULT 0" +
                ") ENGINE=InnoDB;"
            );

            // 4. Create Donations table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Donations (" +
                "  DonationID INT AUTO_INCREMENT PRIMARY KEY," +
                "  DonorID INT NOT NULL," +
                "  Date DATE NOT NULL," +
                "  Quantity INT NOT NULL," +
                "  FOREIGN KEY (DonorID) REFERENCES Donors(DonorID) ON DELETE CASCADE" +
                ") ENGINE=InnoDB;"
            );

            // 5. Create Issues table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Issues (" +
                "  IssueID INT AUTO_INCREMENT PRIMARY KEY," +
                "  BloodGroup VARCHAR(5) NOT NULL," +
                "  Quantity INT NOT NULL," +
                "  Date DATE NOT NULL," +
                "  AdminID INT," +
                "  RecipientInfo VARCHAR(150)," +
                "  FOREIGN KEY (BloodGroup) REFERENCES Inventory(BloodGroup)," +
                "  FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID) ON DELETE SET NULL" +
                ") ENGINE=InnoDB;"
            );

            // Seed default Admin if empty
            ResultSet rsAdmin = stmt.executeQuery("SELECT COUNT(*) FROM Administrators");
            if (rsAdmin.next() && rsAdmin.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO Administrators (Username, Password, Role) VALUES ('admin', 'admin123', 'Super Administrator')");
                stmt.executeUpdate("INSERT INTO Administrators (Username, Password, Role) VALUES ('staff_admin', 'staff123', 'Blood Bank Officer')");
                System.out.println("Default administrator accounts seeded (admin / admin123).");
            }
            rsAdmin.close();

            // Seed Inventory for 8 standard blood groups if empty
            ResultSet rsInv = stmt.executeQuery("SELECT COUNT(*) FROM Inventory");
            if (rsInv.next() && rsInv.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO Inventory (BloodGroup, TotalQuantity) VALUES " +
                        "('A+', 15), ('A-', 8), ('B+', 20), ('B-', 5), ('AB+', 12), ('AB-', 4), ('O+', 25), ('O-', 6)");
                System.out.println("Default inventory initialized with 8 blood groups.");
            }
            rsInv.close();

            // Seed sample donors if empty
            ResultSet rsDonors = stmt.executeQuery("SELECT COUNT(*) FROM Donors");
            if (rsDonors.next() && rsDonors.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO Donors (Name, BloodGroup, Contact) VALUES " +
                        "('Rahul Sharma', 'O+', '9876543210'), " +
                        "('Anjali Menon', 'A+', '9845123456'), " +
                        "('Karthik Nair', 'B+', '9744112233'), " +
                        "('Sneha Roy', 'AB+', '9966554433'), " +
                        "('Mohammed Arif', 'O-', '9447012345')");
                
                // Add sample donation records
                stmt.executeUpdate("INSERT INTO Donations (DonorID, Date, Quantity) VALUES " +
                        "(1, '2026-09-15', 2), (2, '2026-09-18', 1), (3, '2026-09-22', 2), (4, '2026-09-28', 1), (5, '2026-10-02', 1)");

                // Add sample issue records
                stmt.executeUpdate("INSERT INTO Issues (BloodGroup, Quantity, Date, AdminID, RecipientInfo) VALUES " +
                        "('O+', 1, '2026-10-03', 1, 'City General Hospital (ICU)'), " +
                        "('A+', 1, '2026-10-05', 1, 'Government Medical College (Trauma)')");
                System.out.println("Sample donors, donations, and issues seeded.");
            }
            rsDonors.close();

        } catch (SQLException e) {
            System.err.println("Database auto-initialization message: " + e.getMessage());
        } finally {
            DBConnection.close(conn, stmt);
        }
    }
}
