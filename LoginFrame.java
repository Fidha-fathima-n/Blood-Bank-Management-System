package com.bbms.ui;

import com.bbms.model.Administrator;
import com.bbms.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * GUI: LoginFrame
 * 
 * Secure Administrator Login Window.
 * Ensures donor records, inventory logs, and issue actions remain strictly confidential
 * and are accessible only after successful authentication.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private final AuthService authService;

    public LoginFrame() {
        this.authService = AuthService.getInstance();
        initComponents();
    }

    private void initComponents() {
        setTitle("Blood Bank Management System - Administrator Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 430);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.LIGHT_BG);

        // Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UITheme.PRIMARY_RED);
        headerPanel.setPreferredSize(new Dimension(480, 85));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("BLOOD BANK MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Admin-Controlled System | OOPS Project");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(255, 220, 220));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblSub);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Card Panel
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setBackground(UITheme.LIGHT_BG);

        JPanel card = UITheme.createCardPanel();
        card.setPreferredSize(new Dimension(390, 260));
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblLoginTitle = new JLabel("Administrator Login");
        lblLoginTitle.setFont(UITheme.FONT_HEADER);
        lblLoginTitle.setForeground(UITheme.DARK_RED);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(lblLoginTitle, gbc);

        JLabel lblNotice = new JLabel("Donor data is protected. Please authenticate to continue.");
        lblNotice.setFont(UITheme.FONT_SMALL);
        lblNotice.setForeground(UITheme.TEXT_MUTED);
        gbc.gridy = 1;
        card.add(lblNotice, gbc);

        gbc.gridwidth = 1;
        // Username
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 2;
        card.add(lblUser, gbc);

        txtUsername = UITheme.createTextField(16);
        txtUsername.setText("admin"); // Default convenience
        gbc.gridx = 1; gbc.gridy = 2;
        card.add(txtUsername, gbc);

        // Password
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        gbc.gridx = 0; gbc.gridy = 3;
        card.add(lblPass, gbc);

        txtPassword = UITheme.createPasswordField(16);
        txtPassword.setText("admin123"); // Default convenience
        gbc.gridx = 1; gbc.gridy = 3;
        card.add(txtPassword, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);

        btnLogin = UITheme.createPrimaryButton("Login");
        btnLogin.addActionListener(this::handleLogin);
        btnPanel.add(btnLogin);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);
        card.add(btnPanel, gbc);

        // Hint footer
        JLabel lblHint = new JLabel("Default: admin / admin123");
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(UITheme.TEXT_MUTED);
        gbc.gridy = 5;
        card.add(lblHint, gbc);

        cardWrapper.add(card);
        mainPanel.add(cardWrapper, BorderLayout.CENTER);

        // Enter key shortcut
        getRootPane().setDefaultButton(btnLogin);

        setContentPane(mainPanel);
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Administrator admin = authService.login(username, password);
        if (admin != null) {
            JOptionPane.showMessageDialog(this,
                    "Welcome back, " + admin.getUsername() + "!\nLogin successful as " + admin.getRole() + ".",
                    "Authentication Successful", JOptionPane.INFORMATION_MESSAGE);
            
            // Open Main Dashboard
            SwingUtilities.invokeLater(() -> {
                MainDashboard dashboard = new MainDashboard();
                dashboard.setVisible(true);
            });
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.\nPlease check your credentials and try again.",
                    "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }
}
