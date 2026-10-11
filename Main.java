package com.bbms;

import com.bbms.db.DBInitializer;
import com.bbms.ui.LoginFrame;

import javax.swing.*;

/**
 * Main Application Entry Point
 * 
 * Project: Blood Bank Management System (BBMS)
 * Institution: College of Engineering Muttathara
 * Subject: Object-Oriented Programming (OOPS Mini Project)
 * 
 * Workflow:
 * 1. Sets native Look and Feel.
 * 2. Auto-initializes MySQL Database tables and default records (DBInitializer).
 * 3. Launches LoginFrame (Admin-Controlled Access).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    BLOOD BANK MANAGEMENT SYSTEM (BBMS)");
        System.out.println("    Admin-Controlled Java Swing & JDBC Application");
        System.out.println("=================================================");

        // Set native system Look and Feel for clean modern aesthetics
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to Swing default if system L&F unavailable
        }

        // Initialize Database & Seed Default Records if not present
        try {
            System.out.println("Connecting to database and verifying schema...");
            DBInitializer.initializeDatabase();
            System.out.println("Database ready.");
        } catch (Exception e) {
            System.err.println("Database startup notice: " + e.getMessage());
            System.err.println("Ensure MySQL server is running and credentials in db.properties match your setup.");
        }

        // Launch GUI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
