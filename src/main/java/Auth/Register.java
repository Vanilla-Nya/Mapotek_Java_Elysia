package Auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.util.Map;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

import Components.CustomPanel;
import Components.CustomTextField;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Helpers.TypeNumberHelper;
import Main.Drawer;

public class Register extends JPanel {

    private CustomTextField fullNameField, nomerteleponField, usernameField, passwordField, confirmCustomTextField, rfidField;
    private AuthFrame parentFrame;

    public Register(AuthFrame parentFrame) {
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout());

        // Main container with curved panel
        CustomPanel mainPanel = new CustomPanel(25);
        mainPanel.setCurved(true);
        mainPanel.setLayout(new GridBagLayout());
        add(mainPanel);

        // Right panel for logo
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        rightPanel.setOpaque(false); // Make panel transparent
        rightPanel.setPreferredSize(new Dimension(200, getHeight()));
        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.insets = new Insets(0, 0, 0, 0); // Adjust insets to center the logo
        gbcRight.anchor = GridBagConstraints.CENTER;
        JLabel logoLabel = new JLabel("MAPOTEK");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 40));
        logoLabel.setForeground(new Color(0, 160, 136));
        rightPanel.add(logoLabel, gbcRight);

        // Left panel for registration form
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        leftPanel.setOpaque(false); // Make panel transparent
        leftPanel.setPreferredSize(new Dimension(400, getHeight())); // Set size to half of the full page
        leftPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 20); // Add padding to the right to push components to the left
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER; // Center the components

        // Title
        JLabel titleLabel = new JLabel("BUAT AKUN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(false); // Make title transparent
        titleLabel.setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        leftPanel.add(titleLabel, gbc);

        // Full name field
        fullNameField = new CustomTextField("Masukkan Nama Lengkap", 20, 15, Optional.empty());
        fullNameField.setOpaque(false); // Make text field transparent
        fullNameField.getTextField().setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        leftPanel.add(fullNameField, gbc);

        // Phone number field
        nomerteleponField = new CustomTextField("No.Telp", 20, 15, Optional.empty());
        nomerteleponField.setOpaque(false); // Make text field transparent
        nomerteleponField.getTextField().setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        ((AbstractDocument) nomerteleponField.getTextField().getDocument()).setDocumentFilter(new TypeNumberHelper(13));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        leftPanel.add(nomerteleponField, gbc);

        // Username field
        usernameField = new CustomTextField("Masukkan Username", 20, 15, Optional.empty());
        usernameField.setOpaque(false); // Make text field transparent
        usernameField.getTextField().setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        leftPanel.add(usernameField, gbc);

        // Password field
        passwordField = new CustomTextField("Masukkan Password", 20, 15, Optional.of(true));
        passwordField.setOpaque(false); // Make text field transparent
        passwordField.getTextField().setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        leftPanel.add(passwordField, gbc);

        // Confirm Password field
        confirmCustomTextField = new CustomTextField("Konfirmasi Password", 20, 15, Optional.of(true));
        confirmCustomTextField.setOpaque(false); // Make text field transparent
        confirmCustomTextField.getTextField().setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        leftPanel.add(confirmCustomTextField, gbc);

        // RFID field
        rfidField = new CustomTextField("Masukkan RFID", 20, 15, Optional.empty());
        rfidField.setOpaque(false); // Make text field transparent
        rfidField.getTextField().setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        gbc.gridx = 0;
        gbc.gridy = 6; // Adjust the position accordingly
        gbc.gridwidth = 2;
        leftPanel.add(rfidField, gbc);

        // Login link
        JLabel loginLink = new JLabel("sudah punya akun? login");
        loginLink.setForeground(Color.WHITE);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        leftPanel.add(loginLink, gbc);

        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (parentFrame != null) {
                    parentFrame.showLogin();
                }
            }
        });

        // Register button
        RoundedButton registerButton = new RoundedButton("Registrasi");
        registerButton.setBackground(new Color(76, 175, 80));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 16));
        registerButton.setOpaque(false); // Make button transparent
        registerButton.setContentAreaFilled(false); // Make button transparent
        registerButton.addActionListener(e -> {
            // Executor
            QueryExecutor executor = new QueryExecutor();
            String name = fullNameField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmCustomTextField.getText();
            String noTelp = nomerteleponField.getText();
            String rfid = rfidField.getText(); // Get RFID value

            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || noTelp.isEmpty()) {
                JOptionPane.showMessageDialog(Register.this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(Register.this, "Password dan Confirm Password Harus Sama", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Step 1: Insert the user into the 'user' table
                QueryExecutor queryExecutor = new QueryExecutor();  // Create instance of QueryExecutor
                String insertUserQuery = "INSERT INTO user (nama_lengkap, username, jenis_kelamin, alamat, no_telp, password, rfid) VALUES (?, ?, ?, ?, ?, ?, ?)";
                boolean userInserted = QueryExecutor.executeInsertQuery(insertUserQuery, new Object[]{name, username, "Tidak Bisa Dijelaskan", "", noTelp, password, rfid});

                if (!userInserted) {
                    JOptionPane.showMessageDialog(Register.this, "Failed to add user.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Step 2: Get the generated user ID (assuming the user ID is auto-incremented)
                String getLastInsertIdQuery = "SELECT id_user as userId from user where username = ?";
                java.util.List<Map<String, Object>> result = queryExecutor.executeSelectQuery(getLastInsertIdQuery, new Object[]{name});
                String userId = (String) result.get(0).get("userId");

                // Step 3: Insert into user_role table
                String insertUserRoleQuery = "INSERT INTO user_role (id_user, id_role) SELECT ?, id_role FROM role WHERE nama_role = ?";
                try {
                    boolean userRoleInserted = QueryExecutor.executeInsertQuery(insertUserRoleQuery, new Object[]{userId, "Dokter"});

                    if (userRoleInserted) {
                        String query = "CALL login(?, ?)";
                        Object[] parameter = new Object[]{username, password};
                        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);
                        if (!results.isEmpty()) {
                            Map<String, Object> getData = results.get(0);
                            System.err.println(getData);
                            Long code = (Long) getData.get("code");
                            if (code.equals(200L)) {
                                String uuid = (String) getData.get("user_id");
                                String idSatusehat = (String) getData.get("id_satusehat");
                                UserSessionCache cache = new UserSessionCache();
                                cache.login(username, uuid, idSatusehat);
                                JOptionPane.showMessageDialog(Register.this, "Selamat Datang " + getData.get("nama_lengkap"), (String) getData.get("message"), JOptionPane.INFORMATION_MESSAGE);
                                new Drawer().setVisible(true);
                                if (parentFrame != null) {
                                    parentFrame.setVisible(false); // atau parentFrame.dispose();
                                }
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(Register.this, "Register Gagal.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (HeadlessException error) {
                    System.out.println(error);
                }
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        leftPanel.add(registerButton, gbc);

        // Add panels to main panel
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.insets = new Insets(0, 0, 0, 0);
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.gridwidth = 1; // Make the left panel span across the left half
        mainGbc.anchor = GridBagConstraints.WEST; // Align the left panel to the left
        mainGbc.fill = GridBagConstraints.BOTH; // Make the left panel fill the space
        mainGbc.weightx = 0.5; // Set weight for horizontal resizing
        mainGbc.weighty = 1.0; // Set weight for vertical resizing
        mainPanel.add(leftPanel, mainGbc);

        mainGbc.gridx = 1;
        mainGbc.gridy = 0;
        mainGbc.gridwidth = 1; // Make the right panel span across the right half
        mainGbc.anchor = GridBagConstraints.EAST; // Align the right panel to the right
        mainGbc.fill = GridBagConstraints.BOTH; // Make the right panel fill the space
        mainGbc.weightx = 0.5; // Set weight for horizontal resizing
        mainGbc.weighty = 1.0; // Set weight for vertical resizing
        mainPanel.add(rightPanel, mainGbc);
    }
}
