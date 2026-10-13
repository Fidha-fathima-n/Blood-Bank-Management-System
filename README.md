# Blood Bank Management System (BBMS)
**Java & Object-Oriented Programming (OOPS) Mini Project**  
*College of Engineering Muttathara*  

---

## 📌 Project Overview
The **Blood Bank Management System (BBMS)** is an **admin-controlled** system available in both a **Modern Web Application** edition and a **Java Swing Desktop Application** edition, powered by **Core Java backend architecture** and a **MySQL** relational database connected via **JDBC**.

### Key Architectural Rules (Presentation Slides Aligned)
- **Strictly Admin-Controlled**: There is **NO Donor Login** and **NO Patient Module/Login**. Donors exist purely as confidential records managed by authenticated administrators.
- **Donor Privacy**: Donor personal information (contact number, donation history) is shielded from unauthorized access and is only accessible after an Administrator logs in.
- **Automated Stock Synchronization**:
  - **Donation Recording**: Automatically increases the live inventory stock for the donor's blood group.
  - **Blood Issue**: Verifies stock availability first. If stock is insufficient, the issue is denied; if available, stock is automatically decremented and logged.
- **8 Standard Blood Groups**: `A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, `O-`.
- **Relational Tables**: `Administrators`, `Donors`, `Inventory`, `Donations`, `Issues`. (No Patients table).

---

## 🌐 Editions Available

### 1. Modern Web Edition (Browser-based)
- **Access via**: `http://localhost:8080` (or double-click `web/index.html` for instant offline preview)
- **Features**: Responsive layout, live KPI summary cards, interactive stock distribution grid, donor CRUD modal dialogs, real-time availability checker, automated stock deduction, executive audit reports with print and CSV/text export.
- **Backend**: Embedded Java HTTP Server ([`WebServer.java`](file:///c:/Users/mefid/Downloads/oops/src/com/bbms/web/WebServer.java)) exposing REST API endpoints ([`ApiHandler.java`](file:///c:/Users/mefid/Downloads/oops/src/com/bbms/web/ApiHandler.java)).

### 2. Desktop Swing Edition
- **Access via**: `run.bat`
- **Features**: Native Java Swing GUI window with tabbed navigation and medical palette styling.

---

## 🧬 OOP Principles Implemented (Viva Reference)

| OOP Concept | Implementation in Code | Viva Explanation |
| :--- | :--- | :--- |
| **Abstraction** | [`Person.java`](file:///c:/Users/mefid/Downloads/oops/src/com/bbms/model/Person.java)<br>`public abstract class Person`<br>`public abstract String displayDetails();` | `Person` defines a template with common attributes (`id`, `name`, `contact`) and cannot be directly instantiated. Internal implementation details are hidden behind clean contracts. |
| **Inheritance** | [`Donor.java`](file:///c:/Users/mefid/Downloads/oops/src/com/bbms/model/Donor.java) and [`Administrator.java`](file:///c:/Users/mefid/Downloads/oops/src/com/bbms/model/Administrator.java)<br>`class Donor extends Person`<br>`class Administrator extends Person` | Code reusability: both `Donor` and `Administrator` inherit shared attributes (`id`, `name`, `contact`) from `Person` without duplicating code. |
| **Encapsulation** | Entity classes [`BloodInventory.java`](file:///c:/Users/mefid/Downloads/oops/src/com/bbms/model/BloodInventory.java), [`Donation.java`](file:///c:/Users/mefid/Downloads/oops/src/com/bbms/model/Donation.java), [`BloodIssue.java`](file:///c:/Users/mefid/Downloads/oops/src/com/bbms/model/BloodIssue.java) | All fields are declared `private` or `protected` and modified solely through getters, setters, and business methods. E.g., `setQuantity()` enforces non-negative checks (`quantity >= 0`). |
| **Polymorphism** | `displayDetails()` method overriding<br>Constructor overloading in models | **Runtime Polymorphism**: `Donor` and `Administrator` provide their own concrete implementation of `displayDetails()`. At runtime, calling `person.displayDetails()` dynamically dispatches to the subclass implementation. |

---

## 🗄 Relational Database Schema (Slide 13)

The database schema consists of **5 relational tables** (`blood_bank_db`):

```sql
1. Administrators
   ├── AdminID (PK, INT AUTO_INCREMENT)
   ├── Username (VARCHAR 50, UNIQUE)
   ├── Password (VARCHAR 100)
   └── Role (VARCHAR 50)

2. Donors
   ├── DonorID (PK, INT AUTO_INCREMENT)
   ├── Name (VARCHAR 100)
   ├── BloodGroup (VARCHAR 5)
   └── Contact (VARCHAR 20)

3. Inventory
   ├── BloodGroup (PK, VARCHAR 5)  -- A+, A-, B+, B-, AB+, AB-, O+, O-
   └── TotalQuantity (INT, >= 0)

4. Donations
   ├── DonationID (PK, INT AUTO_INCREMENT)
   ├── DonorID (FK -> Donors.DonorID)
   ├── Date (DATE)
   └── Quantity (INT)

5. Issues
   ├── IssueID (PK, INT AUTO_INCREMENT)
   ├── BloodGroup (FK -> Inventory.BloodGroup)
   ├── Quantity (INT)
   ├── Date (DATE)
   ├── AdminID (FK -> Administrators.AdminID)
   └── RecipientInfo (VARCHAR 150)
```

*(Note: There is intentionally **no Patient table**, matching the system specification).*

---

## 📂 Project Directory Structure

```
c:\Users\mefid\Downloads\oops/
│
├── schema.sql                 # MySQL creation script with tables and seed data
├── db.properties              # MySQL database connection configuration
├── compile.bat                # Batch script to compile all Java source files
├── run.bat                    # Batch script to run Swing Desktop GUI
├── run_web.bat                # Batch script to run Java Web Server & open website
├── server.py                  # Lightweight Python HTTP server option
├── server.js                  # Lightweight Node.js HTTP server option
├── README.md                  # Comprehensive documentation and Viva guide
│
├── lib/                       # Place mysql-connector-j-*.jar here
│   └── readme.txt
│
├── web/                       # Modern Frontend Web Application
│   ├── index.html             # Responsive Single-Page Application (SPA)
│   ├── css/
│   │   └── style.css          # Medical theme styling, cards, modals, print layout
│   └── js/
│       └── app.js             # Client state management, validation, API sync
│
└── src/
    └── com/
        └── bbms/
            ├── Main.java                      # Desktop entry point
            │
            ├── model/                         # Core OOP Domain Entities
            │   ├── Person.java                # Abstract base class (Abstraction)
            │   ├── Donor.java                 # Extends Person (Inheritance & Polymorphism)
            │   ├── Administrator.java         # Extends Person (Inheritance & Polymorphism)
            │   ├── BloodInventory.java        # Encapsulated stock management
            │   ├── Donation.java              # Donation transaction entity
            │   └── BloodIssue.java            # Blood issue transaction entity
            │
            ├── db/                            # JDBC Connection & Initializer
            │   ├── DBConnection.java          # Thread-safe JDBC connection provider
            │   └── DBInitializer.java         # Auto-initialization of schema & seed data
            │
            ├── dao/                           # Data Access Object Layer
            │   ├── AdminDAO.java              # Admin authentication & records
            │   ├── DonorDAO.java              # Donor CRUD & blood group search
            │   ├── InventoryDAO.java          # Stock queries & atomic updates
            │   ├── DonationDAO.java           # Donation records & auto stock increment
            │   └── IssueDAO.java              # Blood issue & auto stock deduction
            │
            ├── service/                       # Business Logic Layer
            │   ├── AuthService.java           # Admin session management & privacy guard
            │   ├── DonorService.java          # Donor validation and business rules
            │   ├── InventoryService.java      # Donation & issue stock orchestration
            │   └── ReportService.java         # Executive report generator and file export
            │
            ├── web/                           # Embedded Web Server & REST API
            │   ├── WebServer.java             # Lightweight JDK HTTP server (Port 8080)
            │   └── ApiHandler.java            # REST API controller mapping /api/*
            │
            └── ui/                            # Java Swing User Interface
                ├── UITheme.java               # Modern medical theme (fonts, colors, tables)
                ├── LoginFrame.java            # Admin login screen
                ├── MainDashboard.java         # Master dashboard with tabbed navigation
                ├── DashboardHomePanel.java    # KPI summary cards & stock breakdown
                ├── DonorPanel.java            # Donor registration, view, edit, delete, search
                ├── DonationPanel.java         # Record donation with auto-stock increment
                ├── InventoryPanel.java        # 8 Blood groups stock & availability checker
                ├── IssuePanel.java            # Blood issue with availability check & auto-deduction
                └── ReportsPanel.java          # Executive report viewer, print & export
```

---

## 🚀 How to Run the Website

### Option 1: Full-Stack Web Edition (Java Server + MySQL + Browser)
1. Ensure MySQL is running and `db.properties` credentials match.
2. Ensure `mysql-connector-j-*.jar` is in the `lib/` directory.
3. Double-click `run_web.bat` (or run `run_web.bat` in command prompt).
4. Your browser will automatically open:
   ```
   http://localhost:8080
   ```
5. Log in using default administrator credentials:
   - **Username**: `admin`
   - **Password**: `admin123`

### Option 2: Instant Offline In-Browser Preview (Zero Dependencies)
- Simply double-click [`web/index.html`](file:///c:/Users/mefid/Downloads/oops/web/index.html) in Windows File Explorer to open it in Chrome/Edge/Firefox.
- Features dual-mode in-browser local persistence so the website works immediately even without running a server or MySQL!

### Option 3: Python Lightweight Server
```bash
python server.py
```
Open `http://localhost:8080` in your browser.

### Option 4: Node.js Lightweight Server
```bash
node server.js
```
Open `http://localhost:8080` in your browser.

### Option 5: Desktop Swing GUI Edition
- Double-click `run.bat` to launch the native desktop GUI window.

---

## 🔑 Default Administrator Credentials
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: Super Administrator

*(Secondary officer account: `staff_admin` / `staff123`)*

---


