package com.bbms.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * UI Styling & Theme Helper
 * 
 * Provides consistent styling, colors, and typography across all Java Swing screens.
 * Designed with a clean, medical-grade Blood Bank palette.
 */
public class UITheme {

    // Color Palette
    public static final Color PRIMARY_RED    = new Color(178, 34, 34);     // Firebrick Red
    public static final Color DARK_RED       = new Color(139, 0, 0);       // Dark Blood Red
    public static final Color LIGHT_BG       = new Color(248, 249, 250);   // Off-white / light slate
    public static final Color CARD_BG        = Color.WHITE;
    public static final Color TEXT_DARK      = new Color(33, 37, 41);
    public static final Color TEXT_MUTED     = new Color(108, 117, 125);
    public static final Color BORDER_COLOR   = new Color(222, 226, 230);
    public static final Color SUCCESS_GREEN  = new Color(40, 167, 69);
    public static final Color WARNING_ORANGE = new Color(255, 153, 0);
    public static final Color DANGER_RED     = new Color(220, 53, 69);
    public static final Color ACCENT_BLUE    = new Color(23, 116, 179);

    // Typography
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER     = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_METRIC_NUM = new Font("Segoe UI", Font.BOLD, 26);

    /**
     * Styles standard primary buttons
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(PRIMARY_RED);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /**
     * Styles secondary / action buttons
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        return btn;
    }

    /**
     * Styles danger/delete buttons
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(DANGER_RED);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }

    /**
     * Styles standard text fields
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }

    /**
     * Styles standard password fields
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return pf;
    }

    /**
     * Creates a styled card container panel
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return card;
    }

    /**
     * Styles tables for clean data display
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(28);
        table.setGridColor(new Color(235, 238, 240));
        table.setSelectionBackground(new Color(245, 220, 220));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(new Color(241, 243, 245));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(100, 32));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_RED));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // Center columns except strings if needed
        table.setDefaultRenderer(Integer.class, centerRenderer);
    }
}
