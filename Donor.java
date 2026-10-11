package com.bbms.model;

/**
 * Concrete Class: Donor
 * 
 * Demonstrates:
 * 1. INHERITANCE: Inherits id, name, and contact from Person (extends Person).
 * 2. ENCAPSULATION: bloodGroup is private and exposed via validated accessors.
 * 3. POLYMORPHISM: Overrides the abstract displayDetails() method from Person.
 * 
 * Note: Donors have NO login capability. All donor records are strictly managed
 * by an authenticated Administrator to maintain donor privacy.
 */
public class Donor extends Person {
    private String bloodGroup;

    public Donor() {
        super();
    }

    public Donor(int id, String name, String bloodGroup, String contact) {
        super(id, name, contact);
        this.bloodGroup = bloodGroup;
    }

    public Donor(String name, String bloodGroup, String contact) {
        super(0, name, contact);
        this.bloodGroup = bloodGroup;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    /**
     * Business method modeled in UML diagram (Slide 14).
     * Validates donor details prior to persistence.
     * @return boolean true if donor data is valid
     */
    public boolean register() {
        return name != null && !name.trim().isEmpty() &&
               contact != null && !contact.trim().isEmpty() &&
               isValidBloodGroup(bloodGroup);
    }

    /**
     * Helper validation for blood groups
     */
    public static boolean isValidBloodGroup(String group) {
        if (group == null) return false;
        String[] valid = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        for (String v : valid) {
            if (v.equalsIgnoreCase(group.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Polymorphic implementation of displayDetails()
     */
    @Override
    public String displayDetails() {
        return String.format("[Donor #%d] %s | Blood Group: %s | Contact: %s",
                id, name, bloodGroup, contact);
    }
}
