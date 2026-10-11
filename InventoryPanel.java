package com.bbms.ui;

import com.bbms.model.BloodInventory;
import com.bbms.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI Panel: InventoryPanel
 * 
 * Manages Blood Inventory for all 8 standard blood groups:
 * A+, A-, B+, B-, AB+, AB-, O+, O-.
 * 
 * Features:
 * - Real-time stock view with color-coded status
 * - Quick Blood Availability Check tool
 * - Stock level calibration/update tool (enforces non-negative constraint)
 */
public class InventoryPanel extends JPanel {

    private final InventoryService inventoryService;

    private JTable tblInventory;
    private DefaultTableModel inventoryModel;
    private JLabel lblTotalStockSum;

    // Check availability components
    private JComboBox<String> cmbCheckGroup;
    private JSpinner spCheckQty;
    private JButton btnCheckAvailability;

    // Update stock components
    private JComboBox<String> cmbUpdateGroup;
    private JSpinner spNewQty;
    private JButton btnUpdateStock;

    public InventoryPanel() {
        this.inventoryService = new InventoryService();

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadInventoryData();
    }

    private void initComponents() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Blood Inventory & Stock Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.DARK_RED);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Stock");
        btnRefresh.addActionListener(e -> loadInventoryData());

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(btnRefresh, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: Table Card (Left/Center) + Tools Card (Right)
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        // Inventory Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Standard Blood Group Stocks (8 Groups)");
        lblTableTitle.setFont(UITheme.FONT_HEADER);

        lblTotalStockSum = new JLabel("Total Stock: 0 units");
        lblTotalStockSum.setFont(UITheme.FONT_BODY_BOLD);
        lblTotalStockSum.setForeground(UITheme.PRIMARY_RED);

        tableHeader.add(lblTableTitle, BorderLayout.WEST);
        tableHeader.add(lblTotalStockSum, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Blood Group", "Quantity Available", "Status"};
        inventoryModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblInventory = new JTable(inventoryModel);
        UITheme.styleTable(tblInventory);

        // Custom Cell Renderer for Status
        tblInventory.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (value != null) ? value.toString() : "";
                if ("Sufficient".equalsIgnoreCase(status)) {
                    setForeground(UITheme.SUCCESS_GREEN);
                    setFont(UITheme.FONT_BODY_BOLD);
                } else if ("Low Stock".equalsIgnoreCase(status)) {
                    setForeground(UITheme.WARNING_ORANGE);
                    setFont(UITheme.FONT_BODY_BOLD);
                } else {
                    setForeground(UITheme.DANGER_RED);
                    setFont(UITheme.FONT_BODY_BOLD);
                }
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        tableCard.add(new JScrollPane(tblInventory), BorderLayout.CENTER);
        contentPanel.add(tableCard, BorderLayout.CENTER);

        // Tools Panel (East)
        JPanel toolsPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        toolsPanel.setOpaque(false);
        toolsPanel.setPreferredSize(new Dimension(340, 500));

        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

        // Tool 1: Availability Checker
        JPanel checkCard = UITheme.createCardPanel();
        checkCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblCheckTitle = new JLabel("Check Blood Availability");
        lblCheckTitle.setFont(UITheme.FONT_HEADER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        checkCard.add(lblCheckTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        checkCard.add(new JLabel("Blood Group:"), gbc);
        cmbCheckGroup = new JComboBox<>(bloodGroups);
        cmbCheckGroup.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 1;
        checkCard.add(cmbCheckGroup, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        checkCard.add(new JLabel("Needed Units:"), gbc);
        spCheckQty = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spCheckQty.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 2;
        checkCard.add(spCheckQty, gbc);

        btnCheckAvailability = UITheme.createPrimaryButton("Check Availability");
        btnCheckAvailability.addActionListener(e -> handleCheckAvailability());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 6, 6);
        checkCard.add(btnCheckAvailability, gbc);

        toolsPanel.add(checkCard);

        // Tool 2: Manual Stock Update / Reconciliation
        JPanel updateCard = UITheme.createCardPanel();
        updateCard.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUpdateTitle = new JLabel("Manual Stock Update");
        lblUpdateTitle.setFont(UITheme.FONT_HEADER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        updateCard.add(lblUpdateTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        updateCard.add(new JLabel("Blood Group:"), gbc);
        cmbUpdateGroup = new JComboBox<>(bloodGroups);
        cmbUpdateGroup.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 1;
        updateCard.add(cmbUpdateGroup, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        updateCard.add(new JLabel("New Quantity:"), gbc);
        spNewQty = new JSpinner(new SpinnerNumberModel(10, 0, 500, 1));
        spNewQty.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 2;
        updateCard.add(spNewQty, gbc);

        btnUpdateStock = UITheme.createSecondaryButton("Update Stock");
        btnUpdateStock.addActionListener(e -> handleUpdateStock());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 6, 6);
        updateCard.add(btnUpdateStock, gbc);

        toolsPanel.add(updateCard);

        contentPanel.add(toolsPanel, BorderLayout.EAST);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void loadInventoryData() {
        inventoryModel.setRowCount(0);
        try {
            List<BloodInventory> list = inventoryService.getAllInventory();
            int total = 0;
            for (BloodInventory inv : list) {
                total += inv.getQuantity();
                inventoryModel.addRow(new Object[]{
                    inv.getBloodGroup(),
                    inv.getQuantity() + " units",
                    inv.getStockStatus()
                });
            }
            lblTotalStockSum.setText("Total Stock: " + total + " units");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching inventory: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckAvailability() {
        String bg = (String) cmbCheckGroup.getSelectedItem();
        int needed = (Integer) spCheckQty.getValue();

        int current = inventoryService.getStockQuantity(bg);
        boolean available = current >= needed;

        if (available) {
            JOptionPane.showMessageDialog(this,
                    "STOCK AVAILABLE!\n" +
                    "Blood Group: " + bg + "\n" +
                    "Requested Units: " + needed + " units\n" +
                    "Current Available: " + current + " units\n" +
                    "Remaining after issue would be: " + (current - needed) + " units",
                    "Availability Check - OK", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "INSUFFICIENT STOCK!\n" +
                    "Blood Group: " + bg + "\n" +
                    "Requested Units: " + needed + " units\n" +
                    "Current Available: " + current + " units (Shortage of " + (needed - current) + " units)",
                    "Availability Check - Shortage", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleUpdateStock() {
        String bg = (String) cmbUpdateGroup.getSelectedItem();
        int newQty = (Integer) spNewQty.getValue();

        if (newQty < 0) {
            JOptionPane.showMessageDialog(this, "Stock quantity cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Set blood stock for group " + bg + " directly to " + newQty + " units?",
                "Confirm Stock Update", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = inventoryService.updateStockQuantity(bg, newQty);
            if (success) {
                JOptionPane.showMessageDialog(this, "Blood stock for " + bg + " updated successfully to " + newQty + " units.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update stock.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
