package com.bbms.ui;

import com.bbms.model.BloodInventory;
import com.bbms.service.DonorService;
import com.bbms.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI Panel: DashboardHomePanel
 * 
 * Provides an executive dashboard overview with live KPI cards,
 * blood stock distribution overview, and quick action shortcuts.
 */
public class DashboardHomePanel extends JPanel {

    private final DonorService donorService;
    private final InventoryService inventoryService;
    private final MainDashboard parentDashboard;

    private JLabel lblTotalDonors;
    private JLabel lblTotalStock;
    private JLabel lblTotalDonations;
    private JLabel lblTotalIssued;

    private JTable tblStockSummary;
    private DefaultTableModel stockModel;

    public DashboardHomePanel(MainDashboard parentDashboard) {
        this.parentDashboard = parentDashboard;
        this.donorService = new DonorService();
        this.inventoryService = new InventoryService();

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System Overview & Live Blood Stock");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.DARK_RED);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Overview");
        btnRefresh.addActionListener(e -> refreshData());

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(btnRefresh, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Content: Cards + Stock Table
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);

        // 4 KPI Cards Grid
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        kpiPanel.setOpaque(false);
        kpiPanel.setPreferredSize(new Dimension(800, 105));

        lblTotalDonors = new JLabel("0");
        lblTotalStock = new JLabel("0");
        lblTotalDonations = new JLabel("0");
        lblTotalIssued = new JLabel("0");

        kpiPanel.add(createKpiCard("Registered Donors", lblTotalDonors, new Color(52, 152, 219)));
        kpiPanel.add(createKpiCard("Blood Units in Stock", lblTotalStock, UITheme.PRIMARY_RED));
        kpiPanel.add(createKpiCard("Donations Recorded", lblTotalDonations, new Color(46, 204, 113)));
        kpiPanel.add(createKpiCard("Units Issued", lblTotalIssued, new Color(243, 156, 18)));

        centerPanel.add(kpiPanel, BorderLayout.NORTH);

        // Split Area: Live Stock Table + Quick Actions
        JPanel bodyPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        bodyPanel.setOpaque(false);

        // Live Stock Table Card
        JPanel stockCard = UITheme.createCardPanel();
        stockCard.setLayout(new BorderLayout(10, 10));

        JLabel lblStockTitle = new JLabel("Current Blood Stock by Group (8 Groups)");
        lblStockTitle.setFont(UITheme.FONT_HEADER);
        stockCard.add(lblStockTitle, BorderLayout.NORTH);

        String[] cols = {"Blood Group", "Available Units", "Status"};
        stockModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblStockSummary = new JTable(stockModel);
        UITheme.styleTable(tblStockSummary);
        stockCard.add(new JScrollPane(tblStockSummary), BorderLayout.CENTER);

        bodyPanel.add(stockCard);

        // Quick Shortcuts Card
        JPanel quickActionsCard = UITheme.createCardPanel();
        quickActionsCard.setLayout(new BoxLayout(quickActionsCard, BoxLayout.Y_AXIS));

        JLabel lblQuickTitle = new JLabel("Admin Quick Actions");
        lblQuickTitle.setFont(UITheme.FONT_HEADER);
        lblQuickTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblQuickDesc = new JLabel("Direct shortcuts to core system operations:");
        lblQuickDesc.setFont(UITheme.FONT_SMALL);
        lblQuickDesc.setForeground(UITheme.TEXT_MUTED);
        lblQuickDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        quickActionsCard.add(lblQuickTitle);
        quickActionsCard.add(Box.createVerticalStrut(4));
        quickActionsCard.add(lblQuickDesc);
        quickActionsCard.add(Box.createVerticalStrut(15));

        quickActionsCard.add(createActionRow("Register or Manage Donors", "Add, edit, search donor details", 1));
        quickActionsCard.add(Box.createVerticalStrut(10));
        quickActionsCard.add(createActionRow("Record New Blood Donation", "Logs donation & automatically adds stock", 2));
        quickActionsCard.add(Box.createVerticalStrut(10));
        quickActionsCard.add(createActionRow("Manage Blood Inventory", "Monitor thresholds & update stock", 3));
        quickActionsCard.add(Box.createVerticalStrut(10));
        quickActionsCard.add(createActionRow("Issue Blood Units", "Checks availability & automatically deducts", 4));
        quickActionsCard.add(Box.createVerticalStrut(10));
        quickActionsCard.add(createActionRow("Generate System Reports", "Printable and exportable audit reports", 5));

        bodyPanel.add(quickActionsCard);

        centerPanel.add(bodyPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblHeader = new JLabel(title);
        lblHeader.setFont(UITheme.FONT_SUBHEADER);
        lblHeader.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_METRIC_NUM);
        valueLabel.setForeground(UITheme.TEXT_DARK);

        card.add(lblHeader, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionRow(String actionTitle, String desc, int tabIndex) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(actionTitle);
        lblTitle.setFont(UITheme.FONT_BODY_BOLD);
        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(UITheme.FONT_SMALL);
        lblDesc.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(lblTitle);
        textPanel.add(lblDesc);

        JButton btnGo = UITheme.createSecondaryButton("Open ->");
        btnGo.setFont(UITheme.FONT_SMALL);
        btnGo.addActionListener(e -> parentDashboard.navigateToTab(tabIndex));

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(btnGo, BorderLayout.EAST);
        return panel;
    }

    public void refreshData() {
        try {
            int totalDonors = donorService.getTotalDonorsCount();
            int totalStock = inventoryService.getTotalUnitsAvailable();
            int totalDonations = inventoryService.getTotalDonationsCount();
            int totalIssued = inventoryService.getTotalUnitsIssued();

            lblTotalDonors.setText(String.valueOf(totalDonors));
            lblTotalStock.setText(totalStock + " units");
            lblTotalDonations.setText(String.valueOf(totalDonations));
            lblTotalIssued.setText(totalIssued + " units");

            // Update Stock Table
            stockModel.setRowCount(0);
            List<BloodInventory> inventoryList = inventoryService.getAllInventory();
            for (BloodInventory inv : inventoryList) {
                stockModel.addRow(new Object[]{
                    inv.getBloodGroup(),
                    inv.getQuantity() + " units",
                    inv.getStockStatus()
                });
            }
        } catch (Exception e) {
            System.err.println("Error refreshing dashboard data: " + e.getMessage());
        }
    }
}
