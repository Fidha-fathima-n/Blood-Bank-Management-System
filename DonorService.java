package com.bbms.service;

import com.bbms.dao.DonorDAO;
import com.bbms.model.Donor;

import java.util.Collections;
import java.util.List;

/**
 * Service: DonorService
 * 
 * Enforces business logic and validation for Donor operations:
 * - Admin authorization check for privacy preservation
 * - Validation of name, blood group, and contact number
 * - CRUD operations delegation to DonorDAO
 */
public class DonorService {
    private final DonorDAO donorDAO;
    private final AuthService authService;

    public DonorService() {
        this.donorDAO = new DonorDAO();
        this.authService = AuthService.getInstance();
    }

    /**
     * Checks if current user is authorized to access donor records.
     */
    private void checkAuthorization() {
        if (!authService.isLoggedIn()) {
            throw new SecurityException("Unauthorized: Only an authenticated Administrator can access donor data.");
        }
    }

    /**
     * Registers a new donor after validating all required fields.
     */
    public boolean addDonor(String name, String bloodGroup, String contact) throws IllegalArgumentException {
        checkAuthorization();
        validateDonorFields(name, bloodGroup, contact);

        Donor donor = new Donor(name.trim(), bloodGroup.trim(), contact.trim());
        return donorDAO.addDonor(donor);
    }

    /**
     * Updates an existing donor's information.
     */
    public boolean updateDonor(int donorId, String name, String bloodGroup, String contact) throws IllegalArgumentException {
        checkAuthorization();
        if (donorId <= 0) {
            throw new IllegalArgumentException("Invalid Donor ID.");
        }
        validateDonorFields(name, bloodGroup, contact);

        Donor donor = new Donor(donorId, name.trim(), bloodGroup.trim(), contact.trim());
        return donorDAO.updateDonor(donor);
    }

    /**
     * Deletes a donor by ID.
     */
    public boolean deleteDonor(int donorId) {
        checkAuthorization();
        if (donorId <= 0) {
            throw new IllegalArgumentException("Invalid Donor ID.");
        }
        return donorDAO.deleteDonor(donorId);
    }

    /**
     * Retrieves all registered donors (Admin only).
     */
    public List<Donor> getAllDonors() {
        if (!authService.isLoggedIn()) {
            return Collections.emptyList();
        }
        return donorDAO.getAllDonors();
    }

    /**
     * Searches donors by blood group (e.g., A+, O-).
     */
    public List<Donor> searchDonorsByBloodGroup(String bloodGroup) {
        checkAuthorization();
        if (bloodGroup == null || bloodGroup.trim().isEmpty() || bloodGroup.equalsIgnoreCase("ALL")) {
            return donorDAO.getAllDonors();
        }
        return donorDAO.getDonorsByBloodGroup(bloodGroup.trim());
    }

    /**
     * Retrieves a single donor by ID.
     */
    public Donor getDonorById(int donorId) {
        checkAuthorization();
        return donorDAO.getDonorById(donorId);
    }

    /**
     * Returns total count of donors.
     */
    public int getTotalDonorsCount() {
        if (!authService.isLoggedIn()) return 0;
        return donorDAO.getTotalDonorsCount();
    }

    /**
     * Validates donor input parameters.
     */
    private void validateDonorFields(String name, String bloodGroup, String contact) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Donor name is required.");
        }
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("Donor name must be at least 2 characters.");
        }
        if (!Donor.isValidBloodGroup(bloodGroup)) {
            throw new IllegalArgumentException("Invalid blood group selected. Must be one of: A+, A-, B+, B-, AB+, AB-, O+, O-");
        }
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact number is required.");
        }
        String cleanPhone = contact.trim().replaceAll("[\\s\\-\\(\\)]", "");
        if (cleanPhone.length() < 7 || cleanPhone.length() > 15) {
            throw new IllegalArgumentException("Contact number must be between 7 and 15 digits.");
        }
    }
}
