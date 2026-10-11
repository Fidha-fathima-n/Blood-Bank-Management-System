package com.bbms.model;

import java.sql.Date;

/**
 * Entity Class: Donation
 * 
 * Represents a blood donation made by a registered donor.
 * Demonstrates ENCAPSULATION and domain logic.
 * 
 * UML Class Diagram (Slide 14):
 * - int donationID, quantity
 * - Date date
 * + record()
 */
public class Donation {
    private int donationID;
    private int donorID;
    private Date date;
    private int quantity;
    
    // Supplementary display attributes (obtained via joins)
    private String donorName;
    private String bloodGroup;

    public Donation() {
    }

    public Donation(int donationID, int donorID, Date date, int quantity) {
        this.donationID = donationID;
        this.donorID = donorID;
        this.date = date;
        setQuantity(quantity);
    }

    public Donation(int donorID, Date date, int quantity) {
        this(0, donorID, date, quantity);
    }

    public int getDonationID() {
        return donationID;
    }

    public void setDonationID(int donationID) {
        this.donationID = donationID;
    }

    public int getDonorID() {
        return donorID;
    }

    public void setDonorID(int donorID) {
        this.donorID = donorID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Donation quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    /**
     * Business method modeled in UML diagram (Slide 14)
     * Validates donation parameters.
     */
    public boolean record() {
        return donorID > 0 && date != null && quantity > 0;
    }

    @Override
    public String toString() {
        return "Donation #" + donationID + " [Donor ID: " + donorID + ", Qty: " + quantity + " units, Date: " + date + "]";
    }
}
