package com.bbms.model;

/**
 * Abstract Base Class: Person
 * 
 * Demonstrates:
 * 1. ABSTRACTION: Cannot be directly instantiated. Defines the fundamental contract
 *    and common attributes for all human actors in the system.
 * 2. ENCAPSULATION: Attributes are protected/private and accessed via getters and setters.
 * 
 * Subclasses: Donor, Administrator
 * (Note: Per system requirements, there is NO Patient entity.)
 */
public abstract class Person {
    // Protected fields accessible to inherited classes (Donor, Administrator)
    protected int id;
    protected String name;
    protected String contact;

    /**
     * Default constructor
     */
    public Person() {
    }

    /**
     * Parameterized constructor
     * @param id Identifier for person
     * @param name Full name
     * @param contact Phone number or contact info
     */
    public Person(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    // Getters and Setters (Encapsulation)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * Abstract method enforcing POLYMORPHISM.
     * Concrete subclasses must provide their own specific implementation
     * of displaying and summarizing person details.
     */
    public abstract String displayDetails();

    @Override
    public String toString() {
        return "ID: " + id + " | Name: " + name + " | Contact: " + contact;
    }
}
