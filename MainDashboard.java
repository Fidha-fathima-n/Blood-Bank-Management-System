package com.bbms.ui;

import com.bbms.model.Administrator;
import com.bbms.service.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * GUI: MainDashboard
 * 
 * Central Administrative Workspace for the Blood Bank Management System.
 * Houses tabbed navigation for:
 * 1. Overview Dashboard
 * 2. Manage Donors (CRUD + Blood Group Search)
 * 3. Record Donation (Auto-updates Blood Stock)
 * 4. Blood Inventory (Live Stock & Availability Checks)
 * 5. Issue Blood (Pre-check & Auto-deduction)
 * 6. Reports (Executive Summary & Export)
 */
public class MainDashboard extends JFrame {

    private final AuthService authService;
    private JTabbedPane tabbedPane;

    private DashboardHomePanel homePanel;
    private DonorPanel donorPanel;
    private DonationPanel donationPanel;
    private InventoryPanel inventoryPanel;
    private IssuePanel issuePanel;
    private ReportsPanel reportsPanel;

    public MainDashboard() {
        this.authService = AuthService.getInstance();
        initComponents();
    }

    private void initComponents() {
        setTitle("Blood Bank Management System - Administrator Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.LIGHT_BG);

        // Header Bar
        JPanel headerBar = new JPanel(new BorderLayout(15, 0));
        headerBar.setBackground(UITheme.PRIMARY_RED);
        headerBar.setPreferredSize(new Dimension(1100, 65));
        headerBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Brand Title
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        brandBox.setOpaque(false);

        JLabel lblLogo = new JLabel("🩸");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblTitle = new JLabel("BLOOD BANK MANAGEMENT SYSTEM");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        brandBox.add(lblLogo);
        brandBox.add(lblTitle);
        headerBar.add(brandBox, BorderLayout.WEST);

        // Admin Info & Logout
        JPanel adminBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        adminBox.setOpaque(false);

        Administrator admin = authService.getCurrentAdmin();
        String adminText = (admin != null) ? "Logged in: " + admin.getUsername() + " (" + admin.getRole() + ")" : "Logged in: Administrator";
        JLabel lblAdminInfo = new JLabel(adminText);
        lblAdminInfo.setFont(UITheme.FONT_BODY_BOLD);
        lblAdminInfo.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.FONT_BODY_BOLD);
        btnLogout.setBackground(UITheme.DARK_RED);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnLogout.addActionListener(e -> handleLogout());

        adminBox.add(lblAdminInfo);
        adminBox.add(btnLogout);
        headerBar.add(adminBox, BorderLayout.EAST);

        rootPanel.add(headerBar, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        // Instantiate Sub-panels
        homePanel = new DashboardHomePanel(this);
        donorPanel = new DonorPanel();
        donationPanel = new DonationPanel();
        inventoryPanel = new InventoryPanel();
        issuePanel = new IssuePanel();
        reportsPanel = new ReportsPanel();

        // Add tabs
        tabbedPane.addTab("  📊 Dashboard  ", homePanel);
        tabbedPane.addTab("  👥 Manage Donors  ", donorPanel);
        tabbedPane.addTab("  🩸 Record Donation  ", donationPanel);
        tabbedPane.addTab("  📦 Blood Inventory  ", inventoryPanel);
        tabbedPane.addTab("  🏥 Issue Blood  ", issuePanel);
        tabbedPane.addTab("  📑 Reports  ", reportsPanel);

        // Tab change listener to auto-refresh data across modules
        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            switch (selected) {
                case 0:
                    homePanel.refreshData();
                    break;
                case 1:
                    donorPanel.loadDonors(null);
                    break;
                case 2:
                    donationPanel.loadDonorsList();
                    donationPanel.loadDonationHistory();
                    break;
                case 3:
                    inventoryPanel.loadInventoryData();
                    break;
                case 4:
                    issuePanel.loadIssueHistory();
                    break;
                case 5:
                    reportsPanel.loadReport();
                    break;
            }
        });

        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        // Footer Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setPreferredSize(new Dimension(1100, 25));
        footer.setBackground(new Color(240, 240, 240));
        footer.setBorder(BorderFactory.createEmptyBorder(3, 15, 3, 15));

        JLabel lblFooter = new JLabel("Blood Bank Management System | Java Swing + MySQL JDBC | College of Engineering Muttathara");
        lblFooter.setFont(UITheme.FONT_SMALL);
        lblFooter.setForeground(UITheme.TEXT_MUTED);
        footer.add(lblFooter, BorderLayout.WEST);

        JLabel lblSecurity = new JLabel("🔒 Admin Controlled | Donor Privacy Preserved");
        lblSecurity.setFont(UITheme.FONT_SMALL);
        lblSecurity.setForeground(UITheme.TEXT_MUTED);
        footer.add(lblSecurity, BorderLayout.EAST);

        rootPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    /**
     * Programmatic tab switching helper used by shortcut buttons
     */
    public void navigateToTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabIndex);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of the system?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            this.dispose();

            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
}
