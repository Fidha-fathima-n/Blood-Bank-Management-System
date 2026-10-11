package com.bbms.service;

import com.bbms.dao.DonationDAO;
import com.bbms.dao.InventoryDAO;
import com.bbms.dao.IssueDAO;
import com.bbms.model.Administrator;
import com.bbms.model.BloodInventory;
import com.bbms.model.BloodIssue;
import com.bbms.model.Donation;
import com.bbms.model.Donor;

import java.sql.Date;
import java.util.List;

/**
 * Service: InventoryService
 * 
 * Central business logic handler for blood operations:
 * - Automatic inventory increase on donation recording
 * - Automatic availability verification and stock deduction on blood issuing
 * - Inventory monitoring and threshold management
 */
public class InventoryService {

    private final InventoryDAO inventoryDAO;
    private final DonationDAO donationDAO;
    private final IssueDAO issueDAO;
    private final AuthService authService;

    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
        this.donationDAO = new DonationDAO();
        this.issueDAO = new IssueDAO();
        this.authService = AuthService.getInstance();
    }

    private void checkAuthorization() {
        if (!authService.isLoggedIn()) {
            throw new SecurityException("Unauthorized: Only an authenticated Administrator can perform inventory operations.");
        }
    }

    /**
     * Records a blood donation.
     * Automatically increments the inventory stock of the donor's blood group.
     */
    public boolean recordDonation(int donorId, Date date, int quantity) throws IllegalArgumentException {
        checkAuthorization();

        if (donorId <= 0) {
            throw new IllegalArgumentException("Please select a valid donor.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Donation quantity must be greater than zero.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Donation date is required.");
        }

        Donation donation = new Donation(donorId, date, quantity);
        return donationDAO.recordDonation(donation);
    }

    /**
     * Issues blood units.
     * Enforces the availability check first, then automatically deducts stock.
     */
    public boolean issueBlood(String bloodGroup, int quantity, Date date, String recipientInfo) throws IllegalArgumentException {
        checkAuthorization();

        if (!Donor.isValidBloodGroup(bloodGroup)) {
            throw new IllegalArgumentException("Please select a valid blood group.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Issue quantity must be strictly greater than zero.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Issue date is required.");
        }

        // 1. Verify availability
        int availableStock = inventoryDAO.getStock(bloodGroup);
        if (availableStock < quantity) {
            throw new IllegalArgumentException(String.format(
                "Insufficient blood stock! Requested: %d units of %s, but only %d units are available.",
                quantity, bloodGroup, availableStock
            ));
        }

        Administrator currentAdmin = authService.getCurrentAdmin();
        int adminId = (currentAdmin != null) ? currentAdmin.getId() : 1;

        BloodIssue issue = new BloodIssue(bloodGroup.trim(), quantity, date, adminId, recipientInfo);
        return issueDAO.recordIssue(issue);
    }

    /**
     * Checks stock availability for a given blood group and quantity.
     */
    public boolean checkAvailability(String bloodGroup, int quantity) {
        checkAuthorization();
        return inventoryDAO.isAvailable(bloodGroup, quantity);
    }

    /**
     * Fetches current stock quantity for a blood group.
     */
    public int getStockQuantity(String bloodGroup) {
        checkAuthorization();
        return inventoryDAO.getStock(bloodGroup);
    }

    /**
     * Manually overrides stock quantity (Admin only)
     */
    public boolean updateStockQuantity(String bloodGroup, int newQuantity) {
        checkAuthorization();
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Blood stock quantity cannot be negative.");
        }
        if (!Donor.isValidBloodGroup(bloodGroup)) {
            throw new IllegalArgumentException("Invalid blood group.");
        }
        return inventoryDAO.setStock(bloodGroup, newQuantity);
    }

    /**
     * Retrieves stock levels for all blood groups.
     */
    public List<BloodInventory> getAllInventory() {
        checkAuthorization();
        return inventoryDAO.getAllInventory();
    }

    /**
     * Retrieves all donation history records.
     */
    public List<Donation> getAllDonations() {
        checkAuthorization();
        return donationDAO.getAllDonations();
    }

    /**
     * Retrieves all blood issue records.
     */
    public List<BloodIssue> getAllIssues() {
        checkAuthorization();
        return issueDAO.getAllIssues();
    }

    public int getTotalUnitsAvailable() {
        checkAuthorization();
        return inventoryDAO.getTotalUnitsAvailable();
    }

    public int getTotalDonationsCount() {
        checkAuthorization();
        return donationDAO.getTotalDonationsCount();
    }

    public int getTotalUnitsDonated() {
        checkAuthorization();
        return donationDAO.getTotalUnitsDonated();
    }

    public int getTotalIssuesCount() {
        checkAuthorization();
        return issueDAO.getTotalIssuesCount();
    }

    public int getTotalUnitsIssued() {
        checkAuthorization();
        return issueDAO.getTotalUnitsIssued();
    }
}
