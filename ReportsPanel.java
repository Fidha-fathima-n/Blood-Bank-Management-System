package com.bbms.ui;

import com.bbms.service.ReportService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * GUI Panel: ReportsPanel
 * 
 * Generates and presents comprehensive administrative reports:
 * - Live Inventory levels
 * - Donor registry
 * - Donation history logs
 * - Issue history logs
 * 
 * Supports on-screen review, formatted printing, and file export.
 */
public class ReportsPanel extends JPanel {

    private final ReportService reportService;
    private JTextArea txtReportArea;
    private JButton btnRefresh;
    private JButton btnExport;
    private JButton btnPrint;

    public ReportsPanel() {
        this.reportService = new ReportService();

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadReport();
    }

    private void initComponents() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System Reports & Operational Audit");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.DARK_RED);

        JLabel lblSub = new JLabel("Official administrative summary of donors, live inventory, donations, and issues.");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        JPanel headerBox = new JPanel();
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        headerBox.setOpaque(false);
        headerBox.add(lblTitle);
        headerBox.add(lblSub);

        // Buttons
        JPanel actionBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnBar.setOpaque(false);

        btnRefresh = UITheme.createSecondaryButton("Refresh Report");
        btnRefresh.addActionListener(e -> loadReport());

        btnExport = UITheme.createPrimaryButton("Export to File (.txt)");
        btnExport.addActionListener(e -> handleExportToFile());

        btnPrint = UITheme.createSecondaryButton("Print Report");
        btnPrint.addActionListener(e -> handlePrint());

        actionBtnBar.add(btnRefresh);
        actionBtnBar.add(btnExport);
        actionBtnBar.add(btnPrint);

        topPanel.add(headerBox, BorderLayout.WEST);
        topPanel.add(actionBtnBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: Report Viewer Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        txtReportArea = new JTextArea();
        txtReportArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtReportArea.setEditable(false);
        txtReportArea.setBackground(new Color(252, 252, 253));
        txtReportArea.setForeground(new Color(30, 30, 30));
        txtReportArea.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(txtReportArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadReport() {
        String report = reportService.generateExecutiveReport();
        txtReportArea.setText(report);
        txtReportArea.setCaretPosition(0);
    }

    private void handleExportToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report File");
        fileChooser.setSelectedFile(new File("Blood_Bank_Report_" + System.currentTimeMillis() + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            boolean success = reportService.exportReportToFile(fileToSave);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Report exported successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to write report file.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handlePrint() {
        try {
            boolean complete = txtReportArea.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Printing complete.", "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
