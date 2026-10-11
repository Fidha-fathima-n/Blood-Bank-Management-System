package com.bbms.service;

import com.bbms.dao.AdminDAO;
import com.bbms.model.Administrator;

/**
 * Service: AuthService
 * 
 * Manages the administrative session.
 * Protects donor personal information and critical operations:
 * No sensitive data or operations can be performed unless an admin is logged in.
 */
public class AuthService {
    private static AuthService instance;
    private final AdminDAO adminDAO;
    private Administrator currentAdmin;

    private AuthService() {
        this.adminDAO = new AdminDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Authenticates administrator.
     * @return Administrator object on success, or null on failure.
     */
    public Administrator login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        Administrator admin = adminDAO.authenticate(username, password);
        if (admin != null) {
            this.currentAdmin = admin;
        }
        return admin;
    }

    /**
     * Terminates the administrative session.
     */
    public void logout() {
        this.currentAdmin = null;
    }

    /**
     * Checks if an administrator is currently authenticated.
     */
    public boolean isLoggedIn() {
        return this.currentAdmin != null;
    }

    /**
     * Retrieves currently authenticated administrator.
     */
    public Administrator getCurrentAdmin() {
        return this.currentAdmin;
    }
}
