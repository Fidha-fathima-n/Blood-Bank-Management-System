package com.bbms.ui;

import com.bbms.model.BloodIssue;
import com.bbms.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * GUI Panel: IssuePanel
 * 
 * Manages Blood Issue Operations:
 * 1. Checks blood stock availability before issuing.
 * 2. Automatically decrements inventory upon successful issue.
 * 3. Records issue transactions in the Issues table with administrator audit trail.
 */
public class IssuePanel extends JPanel {

    private final InventoryService inventoryService;

    // Components
    private JComboBox<String> cmbBloodGroup;
    private JLabel lblAvailableUnits;
    private JSpinner spQuantity;
    private JTextField txtRecipientInfo;
    private JTextField txtDate;
    private JButton btnIssueBlood;
    private JButton btnRefresh;

    private JTable tblIssues;
    private DefaultTableModel issuesModel;

    public IssuePanel() {
        this.inventoryService = new InventoryService();

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadIssueHistory();
        updateAvailableStockLabel();
    }

    private void initComponents() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Issue Blood Units");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.DARK_RED);

        JLabel lblSub = new JLabel("System automatically verifies stock availability and deducts inventory on issue.");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        JPanel headerBox = new JPanel();
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        headerBox.setOpaque(false);
        headerBox.add(lblTitle);
        headerBox.add(lblSub);

        btnRefresh = UITheme.createSecondaryButton("Refresh Issues");
        btnRefresh.addActionListener(e -> {
            loadIssueHistory();
            updateAvailableStockLabel();
        });

        topPanel.add(headerBox, BorderLayout.WEST);
        topPanel.add(btnRefresh, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: Issue Form Card (West) + History Table (Center)
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        // Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setPreferredSize(new Dimension(360, 500));
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblCardTitle = new JLabel("New Blood Issue Request");
        lblCardTitle.setFont(UITheme.FONT_HEADER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(lblCardTitle, gbc);

        gbc.gridwidth = 1;
        // Blood Group
        gbc.gridx = 0; gbc.gridy = 1;
        formCard.add(new JLabel("Blood Group:"), gbc);

        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        cmbBloodGroup = new JComboBox<>(bloodGroups);
        cmbBloodGroup.setFont(UITheme.FONT_BODY);
        cmbBloodGroup.addActionListener(e -> updateAvailableStockLabel());
        gbc.gridx = 1; gbc.gridy = 1;
        formCard.add(cmbBloodGroup, gbc);

        // Live stock available display
        gbc.gridx = 0; gbc.gridy = 2;
        formCard.add(new JLabel("Available In Stock:"), gbc);

        lblAvailableUnits = new JLabel("0 units");
        lblAvailableUnits.setFont(UITheme.FONT_BODY_BOLD);
        lblAvailableUnits.setForeground(UITheme.PRIMARY_RED);
        gbc.gridx = 1; gbc.gridy = 2;
        formCard.add(lblAvailableUnits, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 3;
        formCard.add(new JLabel("Quantity (Units):"), gbc);

        spQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spQuantity.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 3;
        formCard.add(spQuantity, gbc);

        // Recipient / Hospital
        gbc.gridx = 0; gbc.gridy = 4;
        formCard.add(new JLabel("Hospital / Recipient:"), gbc);

        txtRecipientInfo = UITheme.createTextField(15);
        txtRecipientInfo.setText("City General Hospital");
        gbc.gridx = 1; gbc.gridy = 4;
        formCard.add(txtRecipientInfo, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 5;
        formCard.add(new JLabel("Issue Date:"), gbc);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtDate = UITheme.createTextField(12);
        txtDate.setText(sdf.format(new java.util.Date()));
        gbc.gridx = 1; gbc.gridy = 5;
        formCard.add(txtDate, gbc);

        // Issue Button
        btnIssueBlood = UITheme.createPrimaryButton("Check & Issue Blood");
        btnIssueBlood.addActionListener(e -> handleIssueBlood());
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 8, 8);
        formCard.add(btnIssueBlood, gbc);

        contentPanel.add(formCard, BorderLayout.WEST);

        // Table Card (Right)
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JLabel lblHistoryTitle = new JLabel("Blood Issue History & Audit Log");
        lblHistoryTitle.setFont(UITheme.FONT_HEADER);
        tableCard.add(lblHistoryTitle, BorderLayout.NORTH);

        String[] cols = {"Issue ID", "Blood Group", "Units", "Date", "Authorized By Admin", "Hospital / Recipient"};
        issuesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblIssues = new JTable(issuesModel);
        UITheme.styleTable(tblIssues);
        tableCard.add(new JScrollPane(tblIssues), BorderLayout.CENTER);

        contentPanel.add(tableCard, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void updateAvailableStockLabel() {
        String bg = (String) cmbBloodGroup.getSelectedItem();
        if (bg != null) {
            int current = inventoryService.getStockQuantity(bg);
            lblAvailableUnits.setText(current + " units");
            if (current <= 5) {
                lblAvailableUnits.setForeground(UITheme.DANGER_RED);
            } else {
                lblAvailableUnits.setForeground(UITheme.SUCCESS_GREEN);
            }
        }
    }

    public void loadIssueHistory() {
        issuesModel.setRowCount(0);
        try {
            List<BloodIssue> list = inventoryService.getAllIssues();
            for (BloodIssue iss : list) {
                issuesModel.addRow(new Object[]{
                    iss.getIssueID(),
                    iss.getBloodGroup(),
                    iss.getQuantity(),
                    iss.getDate().toString(),
                    (iss.getAdminUsername() != null ? iss.getAdminUsername() : "Admin #" + iss.getAdminID()),
                    (iss.getRecipientInfo() != null ? iss.getRecipientInfo() : "N/A")
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading issues history: " + e.getMessage());
        }
    }

    private void handleIssueBlood() {
        String bg = (String) cmbBloodGroup.getSelectedItem();
        int requestedQty = (Integer) spQuantity.getValue();
        String recipient = txtRecipientInfo.getText().trim();
        String dateStr = txtDate.getText().trim();

        if (recipient.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter hospital or recipient details for the issue log.", "Missing Details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date sqlDate;
        try {
            sqlDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Availability check before proceeding
        int currentStock = inventoryService.getStockQuantity(bg);
        if (currentStock < requestedQty) {
            JOptionPane.showMessageDialog(this,
                    "INSUFFICIENT BLOOD STOCK!\n" +
                    "Requested: " + requestedQty + " units of " + bg + "\n" +
                    "Currently Available: " + currentStock + " units\n\n" +
                    "The system cannot issue more blood than available inventory.",
                    "Issue Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm Blood Issue:\n" +
                "Blood Group: " + bg + "\n" +
                "Quantity: " + requestedQty + " units\n" +
                "Recipient: " + recipient + "\n\n" +
                "Stock will be automatically reduced from " + currentStock + " to " + (currentStock - requestedQty) + " units.",
                "Confirm Blood Issue", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = inventoryService.issueBlood(bg, requestedQty, sqlDate, recipient);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Blood issued successfully!\n" +
                            "Stock for " + bg + " updated to " + (currentStock - requestedQty) + " units.",
                            "Blood Issued", JOptionPane.INFORMATION_MESSAGE);

                    loadIssueHistory();
                    updateAvailableStockLabel();
                    spQuantity.setValue(1);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to issue blood. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
