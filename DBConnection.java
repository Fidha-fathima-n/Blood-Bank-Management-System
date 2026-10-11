package com.bbms.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database Connection Manager (JDBC)
 * 
 * Provides centralized database connection management using MySQL Connector/J.
 * Configurable via db.properties or sensible default parameters.
 */
public class DBConnection {
    private static String URL = "jdbc:mysql://localhost:3306/blood_bank_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String USER = "root";
    private static String PASSWORD = "root";
    private static String DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        loadConfig();
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            try {
                // Fallback for older MySQL connector versions
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                System.err.println("Warning: MySQL JDBC Driver not found in classpath. " + ex.getMessage());
            }
        }
    }

    private static void loadConfig() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
                if (props.getProperty("db.url") != null) URL = props.getProperty("db.url");
                if (props.getProperty("db.user") != null) USER = props.getProperty("db.user");
                if (props.getProperty("db.password") != null) PASSWORD = props.getProperty("db.password");
                if (props.getProperty("db.driver") != null) DRIVER = props.getProperty("db.driver");
            } catch (Exception e) {
                System.err.println("Note: Using default database properties. " + e.getMessage());
            }
        }
    }

    /**
     * Obtains a new database connection
     * @return active java.sql.Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Safely closes JDBC resources
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Safely closes statement and connection
     */
    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    /**
     * Tests if database connection is currently working
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
