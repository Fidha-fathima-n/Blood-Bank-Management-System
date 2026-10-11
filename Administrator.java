package com.bbms.model;

/**
 * Concrete Class: Administrator
 * 
 * Demonstrates:
 * 1. INHERITANCE: Inherits id, name, and contact from Person (extends Person).
 * 2. ENCAPSULATION: username, password, and role are private and accessed via getters/setters.
 * 3. POLYMORPHISM: Overrides the abstract displayDetails() method from Person.
 * 
 * The Administrator is the SOLE active user of the system.
 * Only authenticated administrators can view donor data, record donations,
 * manage inventory, issue blood, and view reports.
 */
public class Administrator extends Person {
    private String username;
    private String password;
    private String role;

    public Administrator() {
        super();
        this.role = "Administrator";
    }

    public Administrator(int id, String name, String contact, String username, String password, String role) {
        super(id, name, contact);
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Administrator(int id, String username, String password, String role) {
        super(id, username, "N/A");
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Business method modeled in UML diagram (Slide 14): login validation
     */
    public boolean login(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }

    /**
     * Business method modeled in UML diagram (Slide 14)
     */
    public boolean manageDonors() {
        return true; // Admin has authorization to manage donors
    }

    /**
     * Business method modeled in UML diagram (Slide 14)
     */
    public boolean monitorStock() {
        return true; // Admin has authorization to monitor blood inventory
    }

    /**
     * Polymorphic implementation of displayDetails()
     */
    @Override
    public String displayDetails() {
        return String.format("[Admin #%d] %s (%s) | Contact: %s | Role: %s",
                id, username, (name != null ? name : "Admin"), contact, role);
    }
}
