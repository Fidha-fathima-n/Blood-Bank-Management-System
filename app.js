/**
 * Blood Bank Management System (BBMS) - Client Application
 * 
 * Features:
 * - Admin Authentication & Session Management (Preserves Donor Privacy)
 * - Real-time Stock Syncing (Donation -> Stock Increment, Issue -> Stock Decrement)
 * - Availability Verification & Non-negative quantity validation
 * - Dual Mode: Connects to Java WebServer REST API (/api/*) with seamless
 *   in-browser local state fallback if opened directly without a server.
 */

// Initial Seed Data (matches Slide 13 Schema & seed SQL)
const SEED_DATA = {
  admin: { username: "admin", name: "Administrator", role: "Super Administrator" },
  inventory: [
    { bloodGroup: "A+", quantity: 15 },
    { bloodGroup: "A-", quantity: 8 },
    { bloodGroup: "B+", quantity: 20 },
    { bloodGroup: "B-", quantity: 5 },
    { bloodGroup: "AB+", quantity: 12 },
    { bloodGroup: "AB-", quantity: 4 },
    { bloodGroup: "O+", quantity: 25 },
    { bloodGroup: "O-", quantity: 6 }
  ],
  donors: [
    { id: 1, name: "Rahul Sharma", bloodGroup: "O+", contact: "9876543210" },
    { id: 2, name: "Anjali Menon", bloodGroup: "A+", contact: "9845123456" },
    { id: 3, name: "Karthik Nair", bloodGroup: "B+", contact: "9744112233" },
    { id: 4, name: "Sneha Roy", bloodGroup: "AB+", contact: "9966554433" },
    { id: 5, name: "Mohammed Arif", bloodGroup: "O-", contact: "9447012345" }
  ],
  donations: [
    { id: 1, donorId: 1, donorName: "Rahul Sharma", bloodGroup: "O+", quantity: 2, date: "2026-09-15" },
    { id: 2, donorId: 2, donorName: "Anjali Menon", bloodGroup: "A+", quantity: 1, date: "2026-09-18" },
    { id: 3, donorId: 3, donorName: "Karthik Nair", bloodGroup: "B+", quantity: 2, date: "2026-09-22" },
    { id: 4, donorId: 4, donorName: "Sneha Roy", bloodGroup: "AB+", quantity: 1, date: "2026-09-28" },
    { id: 5, donorId: 5, donorName: "Mohammed Arif", bloodGroup: "O-", quantity: 1, date: "2026-10-02" }
  ],
  issues: [
    { id: 1, bloodGroup: "O+", quantity: 1, date: "2026-10-03", adminName: "admin", recipient: "City General Hospital (ICU)" },
    { id: 2, bloodGroup: "A+", quantity: 1, date: "2026-10-05", adminName: "admin", recipient: "Government Medical College (Trauma)" }
  ]
};

class BloodBankApp {
  constructor() {
    this.currentUser = null;
    this.isServerConnected = false;
    this.donors = [];
    this.inventory = [];
    this.donations = [];
    this.issues = [];

    this.init();
  }

  init() {
    this.setupDatePickers();
    this.setupEventListeners();
    this.checkSession();
  }

  setupDatePickers() {
    const today = new Date().toISOString().split("T")[0];
    const donationDateInput = document.getElementById("donation-date");
    const issueDateInput = document.getElementById("issue-date");
    if (donationDateInput) donationDateInput.value = today;
    if (issueDateInput) issueDateInput.value = today;
  }

  setupEventListeners() {
    // Login form
    document.getElementById("login-form").addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleLogin();
    });

    // Logout button
    document.getElementById("btn-logout").addEventListener("click", () => {
      this.handleLogout();
    });

    // Sidebar navigation
    document.querySelectorAll(".nav-link").forEach((link) => {
      link.addEventListener("click", (e) => {
        const view = link.getAttribute("data-view");
        this.switchView(view);
      });
    });

    // Add Donor form
    document.getElementById("add-donor-form").addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleAddDonor();
    });

    // Edit Donor form
    document.getElementById("edit-donor-form").addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleEditDonor();
    });

    // Record Donation form
    document.getElementById("record-donation-form").addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleRecordDonation();
    });

    // Check Availability form
    document.getElementById("check-availability-form").addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleCheckAvailability();
    });

    // Update Stock form
    document.getElementById("update-stock-form").addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleUpdateStock();
    });

    // Issue Blood form
    document.getElementById("issue-blood-form").addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleIssueBlood();
    });
  }

  // =========================================================================
  // Session & Authentication
  // =========================================================================
  checkSession() {
    const savedUser = sessionStorage.getItem("bbms_admin_user");
    if (savedUser) {
      try {
        this.currentUser = JSON.parse(savedUser);
        this.onLoginSuccess();
      } catch (e) {
        sessionStorage.removeItem("bbms_admin_user");
      }
    }
  }

  async handleLogin() {
    const username = document.getElementById("login-username").value.trim();
    const password = document.getElementById("login-password").value;

    if (!username || !password) {
      this.showToast("Please enter both username and password.", "warning");
      return;
    }

    try {
      // Try Java REST endpoint
      const res = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
      });

      if (res.ok) {
        const data = await res.json();
        if (data.success) {
          this.currentUser = data.admin;
          this.isServerConnected = true;
          this.onLoginSuccess();
          return;
        }
      }
    } catch (err) {
      // Offline / Static file fallback
    }

    // Fallback authentication for direct HTML preview
    if ((username === "admin" && password === "admin123") || (username === "staff_admin" && password === "staff123")) {
      this.currentUser = {
        username: username,
        name: username === "admin" ? "Super Administrator" : "Blood Bank Officer",
        role: username === "admin" ? "Super Administrator" : "Staff Administrator"
      };
      this.onLoginSuccess();
    } else {
      this.showToast("Invalid admin credentials. Access Denied.", "error");
    }
  }

  onLoginSuccess() {
    sessionStorage.setItem("bbms_admin_user", JSON.stringify(this.currentUser));
    document.getElementById("login-overlay").style.display = "none";
    document.getElementById("app-container").style.display = "flex";

    document.getElementById("admin-display-name").textContent = this.currentUser.name || this.currentUser.username;
    document.getElementById("admin-display-role").textContent = this.currentUser.role || "Administrator";
    document.getElementById("admin-avatar").textContent = (this.currentUser.username || "A").charAt(0).toUpperCase();

    this.showToast(`Welcome back, ${this.currentUser.username}! Logged in successfully.`, "success");
    this.loadInitialData();
  }

  handleLogout() {
    if (confirm("Are you sure you want to log out of the Blood Bank Management System?")) {
      sessionStorage.removeItem("bbms_admin_user");
      this.currentUser = null;
      document.getElementById("app-container").style.display = "none";
      document.getElementById("login-overlay").style.display = "flex";
      this.showToast("Logged out successfully. Donor data secured.", "info");
    }
  }

  // =========================================================================
  // Data Loading & Syncing
  // =========================================================================
  async loadInitialData() {
    try {
      const res = await fetch("/api/dashboard");
      if (res.ok) {
        const data = await res.json();
        this.donors = data.donors || [];
        this.inventory = data.inventory || [];
        this.donations = data.donations || [];
        this.issues = data.issues || [];
        this.isServerConnected = true;
        this.renderAll();
        return;
      }
    } catch (e) {
      // In-browser local storage mode
    }

    this.loadFromLocalStorage();
    this.renderAll();
  }

  loadFromLocalStorage() {
    const local = localStorage.getItem("bbms_data");
    if (local) {
      try {
        const parsed = JSON.parse(local);
        this.donors = parsed.donors || SEED_DATA.donors;
        this.inventory = parsed.inventory || SEED_DATA.inventory;
        this.donations = parsed.donations || SEED_DATA.donations;
        this.issues = parsed.issues || SEED_DATA.issues;
        return;
      } catch (err) {}
    }
    // Initialize with fresh seed
    this.donors = [...SEED_DATA.donors];
    this.inventory = [...SEED_DATA.inventory];
    this.donations = [...SEED_DATA.donations];
    this.issues = [...SEED_DATA.issues];
    this.saveToLocalStorage();
  }

  saveToLocalStorage() {
    localStorage.setItem("bbms_data", JSON.stringify({
      donors: this.donors,
      inventory: this.inventory,
      donations: this.donations,
      issues: this.issues
    }));
  }

  refreshAllData() {
    this.loadInitialData();
    this.showToast("Data refreshed.", "info");
  }

  renderAll() {
    this.renderKPIs();
    this.renderStockGrid("dashboard-stock-grid");
    this.renderStockGrid("inventory-full-cards");
    this.renderRecentDonations();
    this.renderDonorsTable(this.donors);
    this.populateDonationDonorSelect();
    this.renderDonationsHistory();
    this.renderIssuesHistory();
    this.updateIssueAvailableBadge();
    this.loadReport();
  }

  // =========================================================================
  // View Navigation
  // =========================================================================
  switchView(viewName) {
    document.querySelectorAll(".page-view").forEach((view) => view.classList.remove("active"));
    document.querySelectorAll(".nav-link").forEach((link) => link.classList.remove("active"));

    const targetView = document.getElementById(`view-${viewName}`);
    const targetLink = document.querySelector(`.nav-link[data-view="${viewName}"]`);

    if (targetView) targetView.classList.add("active");
    if (targetLink) targetLink.classList.add("active");

    const titles = {
      dashboard: "Dashboard Overview",
      donors: "Donor Record Management",
      donations: "Record Blood Donation",
      inventory: "Blood Inventory & Stock Control",
      issue: "Issue Blood Units",
      reports: "System Reports & Audit"
    };
    document.getElementById("topbar-page-title").textContent = titles[viewName] || "Blood Bank System";

    if (viewName === "reports") {
      this.loadReport();
    }
  }

  // =========================================================================
  // View 1: Dashboard Rendering
  // =========================================================================
  renderKPIs() {
    const totalDonors = this.donors.length;
    const totalStock = this.inventory.reduce((acc, cur) => acc + cur.quantity, 0);
    const totalDonations = this.donations.length;
    const totalIssued = this.issues.reduce((acc, cur) => acc + cur.quantity, 0);

    document.getElementById("kpi-donors").textContent = totalDonors;
    document.getElementById("kpi-stock").textContent = `${totalStock} units`;
    document.getElementById("kpi-donations").textContent = totalDonations;
    document.getElementById("kpi-issued").textContent = `${totalIssued} units`;

    // Low stock alert banner evaluation (Slide 19)
    const criticalGroups = this.inventory.filter((i) => i.quantity <= 5);
    const alertBanner = document.getElementById("low-stock-alert-banner");
    const alertMessage = document.getElementById("low-stock-alert-message");
    if (alertBanner && alertMessage) {
      if (criticalGroups.length > 0) {
        const details = criticalGroups.map((g) => `<strong>${g.bloodGroup}</strong> (${g.quantity} units)`).join(", ");
        alertMessage.innerHTML = `Blood inventory is at critical levels for: ${details}. Prioritize donor collection or emergency reserve protocol.`;
        alertBanner.style.display = "block";
      } else {
        alertBanner.style.display = "none";
      }
    }
  }

  renderStockGrid(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = "";

    this.inventory.forEach((item) => {
      let statusClass = "status-sufficient";
      let statusText = "Sufficient";

      if (item.quantity === 0) {
        statusClass = "status-critical";
        statusText = "Out of Stock";
      } else if (item.quantity <= 5) {
        statusClass = "status-critical";
        statusText = "Critical Low";
      } else if (item.quantity <= 10) {
        statusClass = "status-low";
        statusText = "Low Stock";
      }

      const percentage = Math.min(100, Math.max(5, Math.round((item.quantity / 30) * 100)));
      const barColor = item.quantity <= 5 ? '#dc2626' : (item.quantity <= 10 ? '#f59e0b' : '#16a34a');

      const card = document.createElement("div");
      card.className = "blood-box";
      card.innerHTML = `
        <div class="blood-type">${item.bloodGroup}</div>
        <div class="blood-qty">${item.quantity} units</div>
        <div style="background: #e2e8f0; height: 6px; border-radius: 3px; margin: 8px 4px 6px; overflow: hidden;" title="${item.quantity} units in stock">
          <div style="background: ${barColor}; width: ${percentage}%; height: 100%; border-radius: 3px; transition: width 0.3s ease;"></div>
        </div>
        <div class="blood-status-tag ${statusClass}">${statusText}</div>
      `;
      container.appendChild(card);
    });
  }

  renderRecentDonations() {
    const tbody = document.getElementById("dashboard-recent-donations");
    if (!tbody) return;
    tbody.innerHTML = "";

    const recent = [...this.donations].reverse().slice(0, 5);
    if (recent.length === 0) {
      tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:var(--text-muted);">No donations recorded yet.</td></tr>`;
      return;
    }

    recent.forEach((d) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><strong>${d.donorName || "Donor #" + d.donorId}</strong></td>
        <td><span style="color:var(--primary-red); font-weight:700;">${d.bloodGroup}</span></td>
        <td>${d.quantity} units</td>
        <td>${d.date}</td>
      `;
      tbody.appendChild(tr);
    });
  }

  // =========================================================================
  // View 2: Donor Management (CRUD + Search)
  // =========================================================================
  renderDonorsTable(donorList) {
    const tbody = document.getElementById("donors-table-body");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (donorList.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--text-muted);">No matching donor records found.</td></tr>`;
      return;
    }

    donorList.forEach((d) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>#${d.id}</td>
        <td><strong>${d.name}</strong></td>
        <td><span style="font-weight:700; color:var(--primary-red);">${d.bloodGroup}</span></td>
        <td>📞 ${d.contact}</td>
        <td>
          <div class="action-btns">
            <button class="btn btn-secondary btn-sm" onclick="app.openEditDonorModal(${d.id})">✏️ Edit</button>
            <button class="btn btn-danger btn-sm" onclick="app.handleDeleteDonor(${d.id})">🗑️ Delete</button>
          </div>
        </td>
      `;
      tbody.appendChild(tr);
    });
  }

  filterDonors() {
    const bloodFilter = document.getElementById("donor-blood-filter").value;
    const searchVal = document.getElementById("donor-search-input").value.trim().toLowerCase();

    let filtered = this.donors;
    if (bloodFilter !== "ALL") {
      filtered = filtered.filter((d) => d.bloodGroup === bloodFilter);
    }
    if (searchVal) {
      filtered = filtered.filter((d) =>
        d.name.toLowerCase().includes(searchVal) || d.contact.toLowerCase().includes(searchVal)
      );
    }
    this.renderDonorsTable(filtered);
  }

  openAddDonorModal() {
    document.getElementById("donor-name").value = "";
    document.getElementById("donor-contact").value = "";
    document.getElementById("donor-blood-group").selectedIndex = 0;
    this.openModal("modal-add-donor");
  }

  async handleAddDonor() {
    const name = document.getElementById("donor-name").value.trim();
    const bloodGroup = document.getElementById("donor-blood-group").value;
    const contact = document.getElementById("donor-contact").value.trim();

    if (!name || name.length < 2) {
      this.showToast("Donor name must be at least 2 characters.", "warning");
      return;
    }
    if (!contact || contact.length < 7) {
      this.showToast("Please provide a valid contact number.", "warning");
      return;
    }

    try {
      if (this.isServerConnected) {
        const res = await fetch("/api/donors", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name, bloodGroup, contact })
        });
        if (res.ok) {
          const result = await res.json();
          this.closeModal("modal-add-donor");
          this.showToast("Donor registered successfully!", "success");
          this.loadInitialData();
          return;
        }
      }
    } catch (e) {}

    // In-memory fallback
    const newId = this.donors.length > 0 ? Math.max(...this.donors.map((d) => d.id)) + 1 : 1;
    this.donors.push({ id: newId, name, bloodGroup, contact });
    this.saveToLocalStorage();
    this.closeModal("modal-add-donor");
    this.showToast(`Donor ${name} (${bloodGroup}) registered!`, "success");
    this.renderAll();
  }

  openEditDonorModal(donorId) {
    const donor = this.donors.find((d) => d.id === donorId);
    if (!donor) return;

    document.getElementById("edit-donor-id").value = donor.id;
    document.getElementById("edit-donor-id-display").value = `#${donor.id}`;
    document.getElementById("edit-donor-name").value = donor.name;
    document.getElementById("edit-donor-blood-group").value = donor.bloodGroup;
    document.getElementById("edit-donor-contact").value = donor.contact;

    this.openModal("modal-edit-donor");
  }

  async handleEditDonor() {
    const id = parseInt(document.getElementById("edit-donor-id").value);
    const name = document.getElementById("edit-donor-name").value.trim();
    const bloodGroup = document.getElementById("edit-donor-blood-group").value;
    const contact = document.getElementById("edit-donor-contact").value.trim();

    try {
      if (this.isServerConnected) {
        const res = await fetch("/api/donors", {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ id, name, bloodGroup, contact })
        });
        if (res.ok) {
          this.closeModal("modal-edit-donor");
          this.showToast("Donor record updated!", "success");
          this.loadInitialData();
          return;
        }
      }
    } catch (e) {}

    // Local fallback
    const index = this.donors.findIndex((d) => d.id === id);
    if (index !== -1) {
      this.donors[index] = { id, name, bloodGroup, contact };
      this.saveToLocalStorage();
      this.closeModal("modal-edit-donor");
      this.showToast("Donor record updated!", "success");
      this.renderAll();
    }
  }

  async handleDeleteDonor(donorId) {
    const donor = this.donors.find((d) => d.id === donorId);
    if (!donor) return;

    if (!confirm(`Are you sure you want to delete Donor #${donor.id} (${donor.name})?`)) {
      return;
    }

    try {
      if (this.isServerConnected) {
        const res = await fetch(`/api/donors?id=${donorId}`, { method: "DELETE" });
        if (res.ok) {
          this.showToast("Donor deleted.", "info");
          this.loadInitialData();
          return;
        }
      }
    } catch (e) {}

    this.donors = this.donors.filter((d) => d.id !== donorId);
    this.saveToLocalStorage();
    this.showToast("Donor deleted.", "info");
    this.renderAll();
  }

  // =========================================================================
  // View 3: Record Donation (Automatic Stock Increment)
  // =========================================================================
  populateDonationDonorSelect() {
    const select = document.getElementById("donation-donor-select");
    if (!select) return;

    select.innerHTML = `<option value="">-- Choose a Donor --</option>`;
    this.donors.forEach((d) => {
      const opt = document.createElement("option");
      opt.value = d.id;
      opt.textContent = `#${d.id} - ${d.name} (${d.bloodGroup})`;
      select.appendChild(opt);
    });
    this.onDonationDonorChange();
  }

  onDonationDonorChange() {
    const select = document.getElementById("donation-donor-select");
    const bgInput = document.getElementById("donation-blood-group");
    const donorId = parseInt(select.value);

    const donor = this.donors.find((d) => d.id === donorId);
    if (donor) {
      bgInput.value = donor.bloodGroup;
    } else {
      bgInput.value = "";
    }
  }

  renderDonationsHistory() {
    const tbody = document.getElementById("donations-history-body");
    if (!tbody) return;
    tbody.innerHTML = "";

    const list = [...this.donations].reverse();
    if (list.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--text-muted);">No donations recorded yet.</td></tr>`;
      return;
    }

    list.forEach((d) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>#${d.id}</td>
        <td><strong>${d.donorName || "Donor #" + d.donorId}</strong></td>
        <td><span style="font-weight:700; color:var(--primary-red);">${d.bloodGroup}</span></td>
        <td>${d.quantity} units</td>
        <td>${d.date}</td>
      `;
      tbody.appendChild(tr);
    });
  }

  async handleRecordDonation() {
    const donorId = parseInt(document.getElementById("donation-donor-select").value);
    const quantity = parseInt(document.getElementById("donation-quantity").value);
    const date = document.getElementById("donation-date").value;

    if (!donorId) {
      this.showToast("Please select a donor.", "warning");
      return;
    }
    if (quantity <= 0) {
      this.showToast("Donation quantity must be greater than zero.", "warning");
      return;
    }

    const donor = this.donors.find((d) => d.id === donorId);
    if (!donor) return;

    try {
      if (this.isServerConnected) {
        const res = await fetch("/api/donations", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ donorId, quantity, date })
        });
        if (res.ok) {
          this.showToast(`Donation recorded! +${quantity} units added to ${donor.bloodGroup} stock.`, "success");
          this.loadInitialData();
          document.getElementById("record-donation-form").reset();
          this.setupDatePickers();
          return;
        }
      }
    } catch (e) {}

    // Local Automatic Stock Increment logic
    const newDonationId = this.donations.length > 0 ? Math.max(...this.donations.map((d) => d.id)) + 1 : 1;
    this.donations.push({
      id: newDonationId,
      donorId: donor.id,
      donorName: donor.name,
      bloodGroup: donor.bloodGroup,
      quantity: quantity,
      date: date
    });

    // Auto increment inventory stock for donor's blood group
    const invItem = this.inventory.find((i) => i.bloodGroup === donor.bloodGroup);
    if (invItem) {
      invItem.quantity += quantity;
    }

    this.saveToLocalStorage();
    this.showToast(`Donation recorded! +${quantity} units automatically added to ${donor.bloodGroup} stock.`, "success");
    this.renderAll();
    document.getElementById("record-donation-form").reset();
    this.setupDatePickers();
  }

  // =========================================================================
  // View 4: Inventory & Availability
  // =========================================================================
  handleCheckAvailability() {
    const bloodGroup = document.getElementById("check-blood-group").value;
    const needed = parseInt(document.getElementById("check-quantity").value);

    const inv = this.inventory.find((i) => i.bloodGroup === bloodGroup);
    const current = inv ? inv.quantity : 0;
    const resultBox = document.getElementById("availability-result");
    resultBox.style.display = "block";

    if (current >= needed) {
      resultBox.innerHTML = `
        <div style="background:var(--status-success-bg); color:var(--status-success-text); padding:14px; border-radius:var(--radius-sm); border:1px solid #16a34a;">
          <strong>✅ STOCK AVAILABLE</strong><br>
          Blood Group: <strong>${bloodGroup}</strong><br>
          Requested: <strong>${needed} units</strong> | Available: <strong>${current} units</strong><br>
          Remaining after issue: <strong>${current - needed} units</strong>
        </div>
      `;
    } else {
      resultBox.innerHTML = `
        <div style="background:var(--status-danger-bg); color:var(--status-danger-text); padding:14px; border-radius:var(--radius-sm); border:1px solid #dc2626;">
          <strong>❌ INSUFFICIENT BLOOD STOCK</strong><br>
          Blood Group: <strong>${bloodGroup}</strong><br>
          Requested: <strong>${needed} units</strong> | Currently in stock: <strong>${current} units</strong><br>
          Shortage of: <strong>${needed - current} units</strong>
        </div>
      `;
    }
  }

  async handleUpdateStock() {
    const bloodGroup = document.getElementById("update-blood-group").value;
    const newQty = parseInt(document.getElementById("update-quantity").value);

    if (newQty < 0) {
      this.showToast("Blood stock quantity cannot be negative.", "warning");
      return;
    }

    if (!confirm(`Set ${bloodGroup} blood stock directly to ${newQty} units?`)) {
      return;
    }

    try {
      if (this.isServerConnected) {
        const res = await fetch("/api/inventory/update", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ bloodGroup, quantity: newQty })
        });
        if (res.ok) {
          this.showToast(`Stock for ${bloodGroup} calibrated to ${newQty} units.`, "success");
          this.loadInitialData();
          return;
        }
      }
    } catch (e) {}

    const item = this.inventory.find((i) => i.bloodGroup === bloodGroup);
    if (item) {
      item.quantity = newQty;
      this.saveToLocalStorage();
      this.showToast(`Stock for ${bloodGroup} calibrated to ${newQty} units.`, "success");
      this.renderAll();
    }
  }

  // =========================================================================
  // View 5: Issue Blood (Checks Availability First & Auto Decrements)
  // =========================================================================
  updateIssueAvailableBadge() {
    const bg = document.getElementById("issue-blood-group").value;
    const inv = this.inventory.find((i) => i.bloodGroup === bg);
    const stock = inv ? inv.quantity : 0;
    const badge = document.getElementById("issue-current-stock-badge");

    if (!badge) return;
    badge.textContent = `Available Stock: ${stock} units for ${bg}`;

    if (stock <= 5) {
      badge.style.background = "var(--status-danger-bg)";
      badge.style.color = "var(--status-danger-text)";
    } else {
      badge.style.background = "var(--status-success-bg)";
      badge.style.color = "var(--status-success-text)";
    }
  }

  renderIssuesHistory() {
    const tbody = document.getElementById("issues-history-body");
    if (!tbody) return;
    tbody.innerHTML = "";

    const list = [...this.issues].reverse();
    if (list.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No blood issue logs recorded.</td></tr>`;
      return;
    }

    list.forEach((iss) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>#${iss.id}</td>
        <td><span style="font-weight:700; color:var(--primary-red);">${iss.bloodGroup}</span></td>
        <td>${iss.quantity} units</td>
        <td>${iss.date}</td>
        <td>🔒 ${iss.adminName || "admin"}</td>
        <td>${iss.recipient || "Hospital"}</td>
      `;
      tbody.appendChild(tr);
    });
  }

  async handleIssueBlood() {
    const bloodGroup = document.getElementById("issue-blood-group").value;
    const quantity = parseInt(document.getElementById("issue-quantity").value);
    const recipient = document.getElementById("issue-recipient").value.trim();
    const date = document.getElementById("issue-date").value;

    if (!recipient) {
      this.showToast("Please provide hospital / recipient reference.", "warning");
      return;
    }
    if (quantity <= 0) {
      this.showToast("Issue quantity must be greater than zero.", "warning");
      return;
    }

    // Availability verification before proceeding
    const inv = this.inventory.find((i) => i.bloodGroup === bloodGroup);
    const currentStock = inv ? inv.quantity : 0;

    if (currentStock < quantity) {
      this.showToast(
        `INSUFFICIENT STOCK! Requested: ${quantity} units, Available: only ${currentStock} units. Cannot issue.`,
        "error"
      );
      return;
    }

    if (!confirm(`Confirm issuing ${quantity} units of ${bloodGroup} to "${recipient}"?\nStock will be automatically reduced to ${currentStock - quantity} units.`)) {
      return;
    }

    try {
      if (this.isServerConnected) {
        const res = await fetch("/api/issues", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ bloodGroup, quantity, date, recipient })
        });
        if (res.ok) {
          this.showToast(`Blood issued successfully! Inventory for ${bloodGroup} decremented.`, "success");
          this.loadInitialData();
          document.getElementById("issue-blood-form").reset();
          this.setupDatePickers();
          return;
        }
      }
    } catch (e) {}

    // Local stock deduction and issue logging
    inv.quantity -= quantity;

    const newIssueId = this.issues.length > 0 ? Math.max(...this.issues.map((i) => i.id)) + 1 : 1;
    this.issues.push({
      id: newIssueId,
      bloodGroup: bloodGroup,
      quantity: quantity,
      date: date,
      adminName: this.currentUser ? this.currentUser.username : "admin",
      recipient: recipient
    });

    this.saveToLocalStorage();
    this.showToast(`Blood issued! Stock for ${bloodGroup} reduced to ${inv.quantity} units.`, "success");
    this.renderAll();
    document.getElementById("issue-blood-form").reset();
    this.setupDatePickers();
  }

  // =========================================================================
  // View 6: Executive Reports & Export
  // =========================================================================
  loadReport() {
    const reportBox = document.getElementById("report-view-content");
    if (!reportBox) return;

    const now = new Date().toLocaleString();
    const adminUser = this.currentUser ? this.currentUser.username : "admin";
    const adminRole = this.currentUser ? this.currentUser.role : "Super Administrator";

    const totalDonors = this.donors.length;
    const totalStock = this.inventory.reduce((a, b) => a + b.quantity, 0);
    const totalDonations = this.donations.length;
    const totalDonatedUnits = this.donations.reduce((a, b) => a + b.quantity, 0);
    const totalIssues = this.issues.length;
    const totalIssuedUnits = this.issues.reduce((a, b) => a + b.quantity, 0);

    let report = "";
    report += "========================================================================================\n";
    report += "                          BLOOD BANK MANAGEMENT SYSTEM                                  \n";
    report += "                          EXECUTIVE ADMINISTRATIVE REPORT                               \n";
    report += "========================================================================================\n";
    report += `Generated On: ${now}\n`;
    report += `Generated By: ${adminUser} (${adminRole})\n\n`;

    report += "----------------------------------------------------------------------------------------\n";
    report += "1. KEY PERFORMANCE METRICS & SUMMARY\n";
    report += "----------------------------------------------------------------------------------------\n";
    report += `Total Registered Donors    : ${totalDonors}\n`;
    report += `Total Blood Units in Stock : ${totalStock} units\n`;
    report += `Total Donations Recorded   : ${totalDonations} events (${totalDonatedUnits} units)\n`;
    report += `Total Blood Issues Recorded: ${totalIssues} events (${totalIssuedUnits} units)\n\n`;

    report += "----------------------------------------------------------------------------------------\n";
    report += "2. LIVE BLOOD INVENTORY (8 STANDARD BLOOD GROUPS)\n";
    report += "----------------------------------------------------------------------------------------\n";
    report += "Blood Group     | Stock Units     | Status\n";
    report += "----------------------------------------------------------------------------------------\n";
    this.inventory.forEach((i) => {
      let status = "Sufficient";
      if (i.quantity === 0) status = "Out of Stock";
      else if (i.quantity <= 5) status = "Critical Low";
      else if (i.quantity <= 10) status = "Low Stock";
      report += `${i.bloodGroup.padEnd(15)} | ${String(i.quantity).padEnd(15)} | ${status}\n`;
    });
    report += "\n";

    report += "----------------------------------------------------------------------------------------\n";
    report += "3. CONFIDENTIAL DONOR DIRECTORY (ADMIN ONLY)\n";
    report += "----------------------------------------------------------------------------------------\n";
    report += "Donor ID | Full Name                 | Blood Group  | Contact Number\n";
    report += "----------------------------------------------------------------------------------------\n";
    this.donors.forEach((d) => {
      report += `#${String(d.id).padEnd(7)} | ${d.name.padEnd(25)} | ${d.bloodGroup.padEnd(12)} | ${d.contact}\n`;
    });
    report += "\n";

    report += "----------------------------------------------------------------------------------------\n";
    report += "4. RECENT BLOOD DONATIONS AUDIT LOG\n";
    report += "----------------------------------------------------------------------------------------\n";
    report += "Donation ID  | Donor Name              | Blood Group  | Units  | Date\n";
    report += "----------------------------------------------------------------------------------------\n";
    this.donations.forEach((dn) => {
      report += `#${String(dn.id).padEnd(11)} | ${(dn.donorName || "Donor #" + dn.donorId).padEnd(23)} | ${dn.bloodGroup.padEnd(12)} | ${String(dn.quantity).padEnd(6)} | ${dn.date}\n`;
    });
    report += "\n";

    report += "----------------------------------------------------------------------------------------\n";
    report += "5. RECENT BLOOD ISSUES AUDIT LOG\n";
    report += "----------------------------------------------------------------------------------------\n";
    report += "Issue ID   | Blood Group  | Units  | Date        | Recipient / Hospital\n";
    report += "----------------------------------------------------------------------------------------\n";
    this.issues.forEach((is) => {
      report += `#${String(is.id).padEnd(9)} | ${is.bloodGroup.padEnd(12)} | ${String(is.quantity).padEnd(6)} | ${is.date.padEnd(11)} | ${is.recipient || "N/A"}\n`;
    });
    report += "\n========================================================================================\n";
    report += "                                END OF OFFICIAL REPORT                                  \n";
    report += "========================================================================================\n";

    reportBox.textContent = report;
  }

  exportReport(type) {
    const reportText = document.getElementById("report-view-content").textContent;
    let content = reportText;
    let mime = "text/plain";
    let filename = `BBMS_Report_${new Date().toISOString().slice(0, 10)}.txt`;

    if (type === "csv") {
      mime = "text/csv";
      filename = `BBMS_Inventory_${new Date().toISOString().slice(0, 10)}.csv`;
      let csv = "BloodGroup,TotalQuantity\n";
      this.inventory.forEach((i) => {
        csv += `"${i.bloodGroup}",${i.quantity}\n`;
      });
      content = csv;
    }

    const blob = new Blob([content], { type: mime });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    this.showToast(`Report downloaded: ${filename}`, "success");
  }

  // =========================================================================
  // Modal & Toast Helpers
  // =========================================================================
  openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add("show");
  }

  closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove("show");
  }

  showToast(message, type = "info") {
    const container = document.getElementById("toast-container");
    if (!container) return;

    const icons = {
      success: "✅",
      error: "❌",
      warning: "⚠️",
      info: "ℹ️"
    };

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${icons[type] || ""}</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = "0";
      toast.style.transition = "opacity 0.3s ease";
      setTimeout(() => toast.remove(), 300);
    }, 3500);
  }
}

// Global instance
const app = new BloodBankApp();
