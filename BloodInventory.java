package com.bbms.model;

/**
 * Entity Class: BloodInventory
 * 
 * Demonstrates ENCAPSULATION:
 * Attributes bloodGroup and quantity are private and can only be mutated
 * through controlled business methods that enforce non-negative constraints.
 * 
 * UML Methods (Slide 14):
 * - updateStock(int addedQty)
 * - issueBlood(int deductQty)
 */
public class BloodInventory {
    private String bloodGroup;
    private int quantity;

    public BloodInventory() {
    }

    public BloodInventory(String bloodGroup, int quantity) {
        this.bloodGroup = bloodGroup;
        setQuantity(quantity);
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Blood stock quantity cannot be negative.");
        }
        this.quantity = quantity;
    }

    /**
     * Increases stock upon a valid blood donation.
     * @param addedQty positive number of units added
     */
    public void updateStock(int addedQty) {
        if (addedQty <= 0) {
            throw new IllegalArgumentException("Donation quantity must be greater than zero.");
        }
        this.quantity += addedQty;
    }

    /**
     * Decreases stock upon issuing blood after checking availability.
     * @param deductQty positive number of units to issue
     * @return boolean true if sufficient stock was available and deducted
     */
    public boolean issueBlood(int deductQty) {
        if (deductQty <= 0) {
            throw new IllegalArgumentException("Issue quantity must be greater than zero.");
        }
        if (this.quantity >= deductQty) {
            this.quantity -= deductQty;
            return true;
        }
        return false;
    }

    /**
     * Helper status evaluation based on blood unit thresholds
     */
    public String getStockStatus() {
        if (this.quantity == 0) {
            return "Out of Stock";
        } else if (this.quantity <= 5) {
            return "Critical Low";
        } else if (this.quantity <= 10) {
            return "Low Stock";
        } else {
            return "Sufficient";
        }
    }

    @Override
    public String toString() {
        return "Blood Group: " + bloodGroup + " | Stock: " + quantity + " units (" + getStockStatus() + ")";
    }
}
