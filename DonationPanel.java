package com.bbms.ui;

import com.bbms.model.Donation;
import com.bbms.model.Donor;
import com.bbms.service.DonorService;
import com.bbms.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI Panel: DonationPanel
 * 
 * Allows the Administrator to log blood donations made by registered donors.
 * Automatically increments blood inventory upon recording.
 */
public class DonationPanel extends JPanel {

    private final DonorService donorService;
    private final InventoryService inventoryService;

    // Components
    private JComboBox<DonorComboItem> cmbDonors;
    private JLabel lblSelectedBloodGroup;
    private JSpinner spQuantity;
    private JTextField txtDate;
    private JButton btnRecordDonation;
    private JButton btnRefresh;

    private JTable tblDonations;
    private DefaultTableModel donationsModel;

    // Helper item for donor combo box
    private static class DonorComboItem {
        final Donor donor;

        DonorComboItem(Donor donor) {
            this.donor = donor;
        }

        @Override
        public String toString() {
            if (donor == null) return "-- Select a Donor --";
            return String.format("#%d - %s (%s)", donor.getId(), donor.getName(), donor.getBloodGroup());
        }
    }

    public DonationPanel() {
        this.donorService = new DonorService();
        this.inventoryService = new InventoryService();

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadDonorsList();
        loadDonationHistory();
    }

    private void initComponents() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Record Blood Donation");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.DARK_RED);

        JLabel lblSub = new JLabel("Recording a donation will automatically increment the corresponding blood group's stock.");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        JPanel headerBox = new JPanel();
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        headerBox.setOpaque(false);
        headerBox.add(lblTitle);
        headerBox.add(lblSub);

        btnRefresh = UITheme.createSecondaryButton("Refresh Data");
        btnRefresh.addActionListener(e -> {
            loadDonorsList();
            loadDonationHistory();
        });

        topPanel.add(headerBox, BorderLayout.WEST);
        topPanel.add(btnRefresh, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: Form Card (Left) + Donation History (Right)
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        // Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setPreferredSize(new Dimension(360, 500));
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblCardTitle = new JLabel("New Donation Entry");
        lblCardTitle.setFont(UITheme.FONT_HEADER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(lblCardTitle, gbc);

        gbc.gridwidth = 1;
        // Donor selection
        gbc.gridx = 0; gbc.gridy = 1;
        formCard.add(new JLabel("Select Donor:"), gbc);

        cmbDonors = new JComboBox<>();
        cmbDonors.setFont(UITheme.FONT_BODY);
        cmbDonors.addActionListener(e -> updateSelectedDonorInfo());
        gbc.gridx = 1; gbc.gridy = 1;
        formCard.add(cmbDonors, gbc);

        // Blood Group display
        gbc.gridx = 0; gbc.gridy = 2;
        formCard.add(new JLabel("Blood Group:"), gbc);

        lblSelectedBloodGroup = new JLabel("-");
        lblSelectedBloodGroup.setFont(UITheme.FONT_BODY_BOLD);
        lblSelectedBloodGroup.setForeground(UITheme.PRIMARY_RED);
        gbc.gridx = 1; gbc.gridy = 2;
        formCard.add(lblSelectedBloodGroup, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 3;
        formCard.add(new JLabel("Quantity (Units):"), gbc);

        spQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        spQuantity.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 3;
        formCard.add(spQuantity, gbc);

        // Date (YYYY-MM-DD)
        gbc.gridx = 0; gbc.gridy = 4;
        formCard.add(new JLabel("Donation Date:"), gbc);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtDate = UITheme.createTextField(12);
        txtDate.setText(sdf.format(new java.util.Date()));
        gbc.gridx = 1; gbc.gridy = 4;
        formCard.add(txtDate, gbc);

        // Informative note
        JLabel lblAutoStock = new JLabel("Stock is automatically updated in inventory.");
        lblAutoStock.setFont(UITheme.FONT_SMALL);
        lblAutoStock.setForeground(UITheme.SUCCESS_GREEN);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 8, 5, 8);
        formCard.add(lblAutoStock, gbc);

        // Record Button
        btnRecordDonation = UITheme.createPrimaryButton("Record Blood Donation");
        btnRecordDonation.addActionListener(e -> handleRecordDonation());
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 8, 8, 8);
        formCard.add(btnRecordDonation, gbc);

        contentPanel.add(formCard, BorderLayout.WEST);

        // Table Card (Right)
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JLabel lblHistoryTitle = new JLabel("Donation History Audit Trail");
        lblHistoryTitle.setFont(UITheme.FONT_HEADER);
        tableCard.add(lblHistoryTitle, BorderLayout.NORTH);

        String[] cols = {"Donation ID", "Donor ID", "Donor Name", "Blood Group", "Quantity (Units)", "Date"};
        donationsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblDonations = new JTable(donationsModel);
        UITheme.styleTable(tblDonations);
        tableCard.add(new JScrollPane(tblDonations), BorderLayout.CENTER);

        contentPanel.add(tableCard, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void loadDonorsList() {
        cmbDonors.removeAllItems();
        cmbDonors.addItem(new DonorComboItem(null));

        List<Donor> list = donorService.getAllDonors();
        for (Donor d : list) {
            cmbDonors.addItem(new DonorComboItem(d));
        }
        updateSelectedDonorInfo();
    }

    private void updateSelectedDonorInfo() {
        DonorComboItem selected = (DonorComboItem) cmbDonors.getSelectedItem();
        if (selected != null && selected.donor != null) {
            lblSelectedBloodGroup.setText(selected.donor.getBloodGroup());
        } else {
            lblSelectedBloodGroup.setText("-");
        }
    }

    public void loadDonationHistory() {
        donationsModel.setRowCount(0);
        try {
            List<Donation> list = inventoryService.getAllDonations();
            for (Donation d : list) {
                donationsModel.addRow(new Object[]{
                    d.getDonationID(),
                    d.getDonorID(),
                    (d.getDonorName() != null ? d.getDonorName() : "N/A"),
                    d.getBloodGroup(),
                    d.getQuantity(),
                    d.getDate().toString()
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading donation history: " + e.getMessage());
        }
    }

    private void handleRecordDonation() {
        DonorComboItem selected = (DonorComboItem) cmbDonors.getSelectedItem();
        if (selected == null || selected.donor == null) {
            JOptionPane.showMessageDialog(this, "Please select a registered donor first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = (Integer) spQuantity.getValue();
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateStr = txtDate.getText().trim();
        Date sqlDate;
        try {
            sqlDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-10-07).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean success = inventoryService.recordDonation(selected.donor.getId(), sqlDate, quantity);
            if (success) {
                int updatedStock = inventoryService.getStockQuantity(selected.donor.getBloodGroup());
                JOptionPane.showMessageDialog(this,
                        "Blood donation recorded successfully!\n" +
                        "Donor: " + selected.donor.getName() + "\n" +
                        "Blood Group: " + selected.donor.getBloodGroup() + "\n" +
                        "Units Added: " + quantity + " units\n" +
                        "Updated Stock for " + selected.donor.getBloodGroup() + ": " + updatedStock + " units",
                        "Donation Success", JOptionPane.INFORMATION_MESSAGE);

                loadDonationHistory();
                cmbDonors.setSelectedIndex(0);
                spQuantity.setValue(1);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record donation.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
