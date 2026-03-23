package taskmanager;

import taskmanager.core.TaskManager;
import taskmanager.io.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginUI extends JFrame {
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private TaskManager manager;

    public LoginUI(TaskManager manager) {
        this.manager = manager;
        setTitle("Task Manager - Login");
        
        // Set larger font for better macOS rendering
        Font largeFont = new Font("SansSerif", Font.PLAIN, 16);
        usernameField.setFont(largeFont);
        passwordField.setFont(largeFont);
        
        // Create panels with better spacing
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username row
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(largeFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(usernameField, gbc);
        
        // Password row
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(largeFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        mainPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton login = new JButton("Login");
        JButton register = new JButton("Register");
        login.setFont(largeFont);
        register.setFont(largeFont);
        login.setPreferredSize(new Dimension(120, 36));
        register.setPreferredSize(new Dimension(120, 36));
        buttonPanel.add(login);
        buttonPanel.add(register);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainPanel.add(buttonPanel, gbc);

        // Add action listeners with better error handling
        login.addActionListener(e -> {
            try {
                if (UserManager.authenticate(usernameField.getText(), new String(passwordField.getPassword()))) {
                    dispose();
                    new DashboardUI(manager);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Error", 
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage(), 
                        "System Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        register.addActionListener(e -> {
            try {
                if (UserManager.register(usernameField.getText(), new String(passwordField.getPassword()))) {
                    JOptionPane.showMessageDialog(this, "User registered. Please login.", 
                            "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "User already exists or registration failed.", 
                            "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Registration error: " + ex.getMessage(), 
                        "System Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Setup window properties
        add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 300);
        setLocationRelativeTo(null);
        
        // Exit gracefully on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0); // Ensure clean exit
            }
        });
        
        setVisible(true);
    }
}
