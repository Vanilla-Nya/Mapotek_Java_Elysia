/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package mapotek;

import Auth.Login;
import Auth.Register;
import Components.RoundedButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author asuna
 */
public class Mapotek extends JFrame {

    public Mapotek() {
        // Set the title of the window
        setTitle("Mapotek App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null); // Center the window

        // Create the main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Create left panel for logo
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setPreferredSize(new Dimension(400, 400)); // Set to half the width of the frame

        // Load the logo image
        URL resourceUrl = getClass().getClassLoader().getResource("assets/logo.png");
        if (resourceUrl != null) {

            JLabel imageLabel = new JLabel();
            imageLabel.setLayout(new BorderLayout());
            ImageIcon originalIcon = new ImageIcon(resourceUrl);
            // Scale the image to fit the JLabel
            Image scaledImage = originalIcon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            // Set the scaled image to the label
            imageLabel.setIcon(scaledIcon);
            logoPanel.add(imageLabel);
        } else {
            System.out.println("Logo resource not found!");
        }
        mainPanel.add(logoPanel, BorderLayout.WEST);

        // Create right panel for text and buttons
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(0, 150, 136)); // Adjust color as needed
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Welcome text
        JTextPane welcomePane = new JTextPane();
        welcomePane.setContentType("text/plain");
        welcomePane.setText("""
            Selamat Datang
            Di Mapotek App""");
        welcomePane.setEditable(false);
        welcomePane.setOpaque(false);
        welcomePane.setForeground(Color.WHITE);
        welcomePane.setFont(new Font("Arial", Font.PLAIN, 32));
        welcomePane.setPreferredSize(new Dimension(400, 100)); // Adjust the size as needed

        // Center the text by using a simple method
        welcomePane.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the pane
        welcomePane.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Remove the caret (cursor) from JTextPane
        welcomePane.setCaretPosition(0); // Set caret position to the start
        welcomePane.setFocusable(false);   // Make JTextPane non-focusable

        // Center the text using StyledDocument
        StyledDocument docPane = welcomePane.getStyledDocument();
        SimpleAttributeSet centerLabelPane = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerLabelPane, StyleConstants.ALIGN_CENTER);
        docPane.setParagraphAttributes(0, docPane.getLength(), centerLabelPane, false);

        // Set constraints for the JTextPane
        gbc.gridy = 0; // Row 0 for descriptionPane
        gbc.gridx = 0; // Column 0
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow the JTextPane to take up all horizontal space
        gbc.weighty = 0.1; // Weight for vertical space (minimal)
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 0, 5); // No top, no left, 5 bottom, no right
        rightPanel.add(welcomePane, gbc);

        // Add a horizontal separator
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.black);
        gbc.gridy = 1; // Row 1 for separator
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make the separator fill horizontally
        gbc.weighty = 0.1; // Minimal height for the separator
        gbc.insets = new Insets(0, 5, 5, 5); // No top, no left, 5 bottom, no right
        rightPanel.add(separator, gbc);

        // Centered description text using JTextPane
        JTextPane descriptionPane = new JTextPane();
        descriptionPane.setContentType("text/plain");
        descriptionPane.setText("""
            Mapotek adalah solusi manajemen apotek
            yang dirancang untuk memudahkan
            pengelolaan stok obat, resep pasien,
            dan transaksi penjualan.""");
        descriptionPane.setEditable(false);
        descriptionPane.setOpaque(false);
        descriptionPane.setForeground(Color.WHITE);
        descriptionPane.setFont(new Font("Arial", Font.PLAIN, 24));

        // Center the text by using a simple method
        descriptionPane.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the pane
        descriptionPane.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Remove the caret (cursor) from JTextPane
        descriptionPane.setCaretPosition(0); // Set caret position to the start
        descriptionPane.setFocusable(false);   // Make JTextPane non-focusable

        // Center the text using StyledDocument
        StyledDocument doc = descriptionPane.getStyledDocument();
        SimpleAttributeSet centerLabel = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerLabel, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), centerLabel, false);

        // Set constraints for the JTextPane
        gbc.gridy = 2; // Row 0 for descriptionPane
        gbc.gridx = 0; // Column 0
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; // Allow the JTextPane to take up all horizontal space
        gbc.weighty = 0.1; // Weight for vertical space (minimal)
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 5, 0); // No top, no left, 5 bottom, no right
        rightPanel.add(descriptionPane, gbc);

        // Login button
        JButton loginButton = new RoundedButton("Login");
        loginButton.setBackground(Color.WHITE); // Primary color
        loginButton.setForeground(new Color(0, 150, 136));
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1; // Minimal vertical space
        gbc.insets = new Insets(10, 10, 10, 10); // Add some bottom spacing for the button

        rightPanel.add(loginButton, gbc);

        // Register button
        JButton registerButton = new RoundedButton("Register");
        registerButton.setBackground(Color.WHITE); // Primary color
        registerButton.setForeground(new Color(0, 150, 136));
        registerButton.setFont(new Font("Arial", Font.BOLD, 16));
        registerButton.setFocusPainted(false);
        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Open the Login window when the login link is clicked
                new Register().setVisible(true);
                dispose(); // Close the Register window
            }
        });
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1; // Minimal vertical space
        gbc.insets = new Insets(10, 10, 10, 10); // Add some bottom spacing for the button
        rightPanel.add(registerButton, gbc);

        // Add panels to the main panel
        mainPanel.add(rightPanel);

        // Add main panel to the frame
        add(mainPanel);

        // Add action listeners for buttons
        loginButton.addActionListener(e -> {
            System.out.println("Login button clicked");
            Login login = new Login();
            login.setVisible(true);
            this.dispose();
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Navigate to register screen (not implemented here)
                System.out.println("Register button clicked");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Mapotek welcomeScreen = new Mapotek();
            welcomeScreen.setVisible(true);
        });
    }
}
