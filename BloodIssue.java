package com.bbms.model;

import java.sql.Date;

/**
 * Entity Class: BloodIssue
 * 
 * Represents a blood unit issue recorded by an Administrator.
 * Matches relational schema (Slide 13):
 * PK IssueID, FK BloodGroup, Quantity, Date, FK AdminID, RecipientInfo
 * 
 * Demonstrates ENCAPSULATION and domain integrity.
 */
public class BloodIssue {
    private int issueID;
    private String bloodGroup;
    private int quantity;
    private Date date;
    private int adminID;
    private String recipientInfo;
    private String adminUsername; // For display/reports

    public BloodIssue() {
    }

    public BloodIssue(int issueID, String bloodGroup, int quantity, Date date, int adminID, String recipientInfo) {
        this.issueID = issueID;
        this.bloodGroup = bloodGroup;
        setQuantity(quantity);
        this.date = date;
        this.adminID = adminID;
        this.recipientInfo = recipientInfo;
    }

    public BloodIssue(String bloodGroup, int quantity, Date date, int adminID, String recipientInfo) {
        this(0, bloodGroup, quantity, date, adminID, recipientInfo);
    }

    public int getIssueID() {
        return issueID;
    }

    public void setIssueID(int issueID) {
        this.issueID = issueID;
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
        if (quantity <= 0) {
            throw new IllegalArgumentException("Blood issue quantity must be strictly greater than zero.");
        }
        this.quantity = quantity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public String getRecipientInfo() {
        return recipientInfo;
    }

    public void setRecipientInfo(String recipientInfo) {
        this.recipientInfo = recipientInfo;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    /**
     * Domain validation method for issuing blood
     */
    public boolean issue() {
        return bloodGroup != null && !bloodGroup.trim().isEmpty() &&
               quantity > 0 && date != null && adminID > 0;
    }

    @Override
    public String toString() {
        return "Issue #" + issueID + " [Group: " + bloodGroup + ", Qty: " + quantity + " units, Date: " + date + ", AdminID: " + adminID + "]";
    }
}
