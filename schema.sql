-- ====================================================================
-- BLOOD BANK MANAGEMENT SYSTEM (BBMS)
-- Database: blood_bank_db
-- Relational Schema matching Project Presentation (Slide 13)
-- ====================================================================

-- 1. Create Database if not exists
CREATE DATABASE IF NOT EXISTS blood_bank_db;
USE blood_bank_db;

-- 2. Drop tables if existing (in reverse order of foreign keys)
DROP TABLE IF EXISTS Issues;
DROP TABLE IF EXISTS Donations;
DROP TABLE IF EXISTS Inventory;
DROP TABLE IF EXISTS Donors;
DROP TABLE IF EXISTS Administrators;

-- ====================================================================
-- TABLE 1: Administrators
-- Represents system administrators (only users with login access)
-- ====================================================================
CREATE TABLE Administrators (
    AdminID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(100) NOT NULL,
    Role VARCHAR(50) NOT NULL DEFAULT 'Administrator'
) ENGINE=InnoDB;

-- ====================================================================
-- TABLE 2: Donors
-- Stores donor personal records. Protected for admin access only.
-- NO PATIENT table exists in this system.
-- ====================================================================
CREATE TABLE Donors (
    DonorID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    BloodGroup VARCHAR(5) NOT NULL,
    Contact VARCHAR(20) NOT NULL
) ENGINE=InnoDB;

-- ====================================================================
-- TABLE 3: Inventory
-- Stores live available blood stock for all 8 standard blood groups
-- ====================================================================
CREATE TABLE Inventory (
    BloodGroup VARCHAR(5) PRIMARY KEY,
    TotalQuantity INT NOT NULL DEFAULT 0 CHECK (TotalQuantity >= 0)
) ENGINE=InnoDB;

-- ====================================================================
-- TABLE 4: Donations
-- Logs each blood donation made by a registered donor.
-- Automatically increments the corresponding Inventory stock.
-- ====================================================================
CREATE TABLE Donations (
    DonationID INT AUTO_INCREMENT PRIMARY KEY,
    DonorID INT NOT NULL,
    Date DATE NOT NULL,
    Quantity INT NOT NULL CHECK (Quantity > 0),
    FOREIGN KEY (DonorID) REFERENCES Donors(DonorID) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ====================================================================
-- TABLE 5: Issues
-- Logs blood units issued to hospitals/recipients after availability check.
-- Automatically deducts from the corresponding Inventory stock.
-- ====================================================================
CREATE TABLE Issues (
    IssueID INT AUTO_INCREMENT PRIMARY KEY,
    BloodGroup VARCHAR(5) NOT NULL,
    Quantity INT NOT NULL CHECK (Quantity > 0),
    Date DATE NOT NULL,
    AdminID INT,
    RecipientInfo VARCHAR(150),
    FOREIGN KEY (BloodGroup) REFERENCES Inventory(BloodGroup),
    FOREIGN KEY (AdminID) REFERENCES Administrators(AdminID) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ====================================================================
-- SEED DATA
-- ====================================================================

-- Default Administrators
INSERT INTO Administrators (Username, Password, Role) VALUES
('admin', 'admin123', 'Super Administrator'),
('staff_admin', 'staff123', 'Blood Bank Officer');

-- Initialize Inventory with all 8 Blood Groups
INSERT INTO Inventory (BloodGroup, TotalQuantity) VALUES
('A+', 15),
('A-', 8),
('B+', 20),
('B-', 5),
('AB+', 12),
('AB-', 4),
('O+', 25),
('O-', 6);

-- Sample Donors
INSERT INTO Donors (Name, BloodGroup, Contact) VALUES
('Rahul Sharma', 'O+', '9876543210'),
('Anjali Menon', 'A+', '9845123456'),
('Karthik Nair', 'B+', '9744112233'),
('Sneha Roy', 'AB+', '9966554433'),
('Mohammed Arif', 'O-', '9447012345');

-- Sample Donations (matching initial stock records)
INSERT INTO Donations (DonorID, Date, Quantity) VALUES
(1, '2026-09-15', 2),
(2, '2026-09-18', 1),
(3, '2026-09-22', 2),
(4, '2026-09-28', 1),
(5, '2026-10-02', 1);

-- Sample Blood Issues
INSERT INTO Issues (BloodGroup, Quantity, Date, AdminID, RecipientInfo) VALUES
('O+', 1, '2026-10-03', 1, 'City General Hospital (ICU)'),
('A+', 1, '2026-10-05', 1, 'Government Medical College (Trauma)');

-- End of schema.sql
