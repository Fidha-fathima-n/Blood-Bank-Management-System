package com.bbms.ui;

import com.bbms.model.Donor;
import com.bbms.service.DonorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI Panel: DonorPanel
 * 
 * Manages Donor records: Add, Edit, Delete, View, and Search by blood group.
 * Strictly admin-controlled: Preserves donor privacy from unauthorized viewers.
 */
public class DonorPanel extends JPanel {

    private final DonorService donorService;

    // Form Components
    private JTextField txtDonorId;
    private JTextField txtName;
    private JComboBox<String> cmbBloodGroup;
    private JTextField txtContact;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // Search and Table
    private JComboBox<String> cmbFilterGroup;
    private JButton btnSearch;
    private JButton btnShowAll;
    private JTable tblDonors;
    private DefaultTableModel tableModel;

    public DonorPanel() {
        this.donorService = new DonorService();

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadDonors(null);
    }

    private void initComponents() {
        // Title Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Donor Management (Admin Controlled)");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.DARK_RED);

        JLabel lblSubtitle = new JLabel("Confidential donor records: Only authenticated administrators have access.");
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(lblTitle);
        headerText.add(lblSubtitle);

        topPanel.add(headerText, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Center: Left (Form Card) + Right (Search & Table Card)
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        // Form Card (West)
        JPanel formCard = UITheme.createCardPanel();
        formCard.setPreferredSize(new Dimension(340, 500));
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblFormTitle = new JLabel("Donor Details");
        lblFormTitle.setFont(UITheme.FONT_HEADER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(lblFormTitle, gbc);

        gbc.gridwidth = 1;
        // Donor ID (read-only)
        gbc.gridx = 0; gbc.gridy = 1;
        formCard.add(new JLabel("Donor ID:"), gbc);
        txtDonorId = UITheme.createTextField(10);
        txtDonorId.setEditable(false);
        txtDonorId.setBackground(new Color(240, 240, 240));
        gbc.gridx = 1; gbc.gridy = 1;
        formCard.add(txtDonorId, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 2;
        formCard.add(new JLabel("Full Name:"), gbc);
        txtName = UITheme.createTextField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        formCard.add(txtName, gbc);

        // Blood Group
        gbc.gridx = 0; gbc.gridy = 3;
        formCard.add(new JLabel("Blood Group:"), gbc);
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        cmbBloodGroup = new JComboBox<>(bloodGroups);
        cmbBloodGroup.setFont(UITheme.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 3;
        formCard.add(cmbBloodGroup, gbc);

        // Contact
        gbc.gridx = 0; gbc.gridy = 4;
        formCard.add(new JLabel("Contact Number:"), gbc);
        txtContact = UITheme.createTextField(15);
        gbc.gridx = 1; gbc.gridy = 4;
        formCard.add(txtContact, gbc);

        // Action Buttons Grid
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonPanel.setOpaque(false);

        btnAdd = UITheme.createPrimaryButton("Add Donor");
        btnAdd.addActionListener(e -> handleAddDonor());

        btnUpdate = UITheme.createSecondaryButton("Update");
        btnUpdate.addActionListener(e -> handleUpdateDonor());

        btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> handleDeleteDonor());

        btnClear = UITheme.createSecondaryButton("Clear");
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 6, 6, 6);
        formCard.add(buttonPanel, gbc);

        contentPanel.add(formCard, BorderLayout.WEST);

        // Table & Search Card (Center)
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        // Search Filter Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setOpaque(false);

        searchBar.add(new JLabel("Filter by Blood Group:"));
        String[] filterOptions = {"ALL", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        cmbFilterGroup = new JComboBox<>(filterOptions);
        cmbFilterGroup.setFont(UITheme.FONT_BODY);
        searchBar.add(cmbFilterGroup);

        btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> {
            String selected = (String) cmbFilterGroup.getSelectedItem();
            loadDonors(selected);
        });
        searchBar.add(btnSearch);

        btnShowAll = UITheme.createSecondaryButton("Show All");
        btnShowAll.addActionListener(e -> {
            cmbFilterGroup.setSelectedIndex(0);
            loadDonors(null);
        });
        searchBar.add(btnShowAll);

        tableCard.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"Donor ID", "Full Name", "Blood Group", "Contact Number"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblDonors = new JTable(tableModel);
        UITheme.styleTable(tblDonors);

        // Selection Listener to populate form
        tblDonors.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblDonors.getSelectedRow() != -1) {
                int row = tblDonors.getSelectedRow();
                txtDonorId.setText(tblDonors.getValueAt(row, 0).toString());
                txtName.setText(tblDonors.getValueAt(row, 1).toString());
                cmbBloodGroup.setSelectedItem(tblDonors.getValueAt(row, 2).toString());
                txtContact.setText(tblDonors.getValueAt(row, 3).toString());
            }
        });

        tableCard.add(new JScrollPane(tblDonors), BorderLayout.CENTER);
        contentPanel.add(tableCard, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void loadDonors(String bloodGroupFilter) {
        tableModel.setRowCount(0);
        try {
            List<Donor> list;
            if (bloodGroupFilter == null || bloodGroupFilter.equals("ALL")) {
                list = donorService.getAllDonors();
            } else {
                list = donorService.searchDonorsByBloodGroup(bloodGroupFilter);
            }

            for (Donor d : list) {
                tableModel.addRow(new Object[]{
                    d.getId(),
                    d.getName(),
                    d.getBloodGroup(),
                    d.getContact()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading donors: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddDonor() {
        try {
            String name = txtName.getText().trim();
            String bg = (String) cmbBloodGroup.getSelectedItem();
            String contact = txtContact.getText().trim();

            boolean success = donorService.addDonor(name, bg, contact);
            if (success) {
                JOptionPane.showMessageDialog(this, "Donor registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadDonors(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register donor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleUpdateDonor() {
        if (txtDonorId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a donor from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtDonorId.getText().trim());
            String name = txtName.getText().trim();
            String bg = (String) cmbBloodGroup.getSelectedItem();
            String contact = txtContact.getText().trim();

            boolean success = donorService.updateDonor(id, name, bg, contact);
            if (success) {
                JOptionPane.showMessageDialog(this, "Donor details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadDonors(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update donor record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid Donor ID.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDeleteDonor() {
        if (txtDonorId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a donor from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Donor #" + txtDonorId.getText() + " (" + txtName.getText() + ")?\nAssociated donation records will also be removed.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(txtDonorId.getText().trim());
            boolean success = donorService.deleteDonor(id);
            if (success) {
                JOptionPane.showMessageDialog(this, "Donor deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadDonors(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete donor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtDonorId.setText("");
        txtName.setText("");
        txtContact.setText("");
        cmbBloodGroup.setSelectedIndex(0);
        tblDonors.clearSelection();
    }
}
