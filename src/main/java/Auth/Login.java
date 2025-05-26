package Auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
    
import com.formdev.flatlaf.FlatLightLaf;

import Auth.Auth_Animations.LoginSuccessAnimation;
import Components.CustomPanel;
import Components.CustomTextField;
import Components.RoundedButton;
import Components.RoundedPanel;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Main.Drawer;

/**
 *
 * @author asuna
 */ 


public class Login extends JPanel {

    // Tambahkan referensi ke AuthFrame (atau callback)
    private AuthFrame parentFrame;

    // Konstruktor menerima AuthFrame sebagai parameter
    public Login(AuthFrame parentFrame) {
        this.parentFrame = parentFrame;

        // Hilangkan setTitle, setSize, setDefaultCloseOperation, setLocationRelativeTo

        setLayout(new BorderLayout());

        // Set FlatLaf theme with custom rounded corners
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 20);
        UIManager.put("Component.arc", 15);
        UIManager.put("TextComponent.arc", 15);

        // Create a main container with GridBagLayout
        CustomPanel mainPanel = new CustomPanel(25);
        mainPanel.setCurved(true);
        mainPanel.setLayout(new GridBagLayout());
        add(mainPanel, BorderLayout.CENTER);

        // Create the left panel
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        leftPanel.setOpaque(false); // Make panel transparent
        leftPanel.setPreferredSize(new Dimension(400, getHeight())); // Set size to half of the full page
        leftPanel.setLayout(new GridBagLayout());

        // Create the right panel and position it
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(0, 0, 0, 0)); // Set background to transparent
        rightPanel.setOpaque(false); // Make panel transparent
        rightPanel.setPreferredSize(new Dimension(400, getHeight())); // Set size to half of the full page
        rightPanel.setLayout(new GridBagLayout());

        // Tambahkan label MAPOTEK ke rightPanel
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.insets = new Insets(0, 0, 0, 0);
        gbcRight.anchor = GridBagConstraints.CENTER;

        JLabel logoLabel = new JLabel("MAPOTEK");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 40));
        logoLabel.setForeground(new Color(0, 160, 136));
        rightPanel.add(logoLabel, gbcRight);

        // Create the login card panel and position it
        JPanel cardPanel = createLoginCard();
        cardPanel.setOpaque(false); // Ensure the panel is transparent

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Allow horizontal expansion
        gbc.weighty = 1.0; // Allow vertical expansion
        gbc.insets = new Insets(0, 0, 0, 0); // Add padding to the left to shift right
        gbc.anchor = GridBagConstraints.CENTER; // Center the card panel
        gbc.fill = GridBagConstraints.BOTH; // Make the card panel fill the space
        leftPanel.add(cardPanel, gbc);

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

    private JPanel createLoginCard() {
        JPanel cardPanel = new RoundedPanel(25, new Color(0, 0, 0, 0)); // Set background to transparent
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(0, 128, 96), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        cardPanel.setPreferredSize(new Dimension(300, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Sign In", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        cardPanel.add(lblTitle, gbc);

        CustomTextField txtUsernameOrRFID = new CustomTextField("Username or RFID", 20, 15, Optional.empty());
        gbc.gridy = 1;
        cardPanel.add(txtUsernameOrRFID, gbc);

        // JPasswordField for Password input
        CustomTextField txtPassword = new CustomTextField("Password", 20, 15, Optional.of(true));
        gbc.gridy = 2;
        cardPanel.add(txtPassword, gbc);

        // Register link
        JLabel registerlink = new JLabel("Belum Punya Akun? Register");
        registerlink.setForeground(Color.WHITE);
        registerlink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerlink.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 4;
        cardPanel.add(registerlink, gbc);

        registerlink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Panggil parentFrame.showRegister()
                if (parentFrame != null) {
                    parentFrame.showRegister();
                }
            }
        });

        // Use RoundedButton for the login button
        RoundedButton btnLogin = new RoundedButton("Login");
        btnLogin.setBackground(Color.WHITE);
        btnLogin.setForeground(new Color(0, 150, 136));
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setBorderColor(new Color(0, 150, 136)); // Match the app's theme color
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(btnLogin, gbc);

        txtUsernameOrRFID.setPreferredSize(new Dimension(200, 50));
        btnLogin.setPreferredSize(new Dimension(200, 50));

        btnLogin.addActionListener(e -> {
            // Catch Field
            String usernameOrRFID = txtUsernameOrRFID.getText();
            String password = new String(txtPassword.getPassword()); // Use `new String()` to get password from JPasswordField

            // Executor
            QueryExecutor executor = new QueryExecutor();
            String query;
            Object[] parameter;

            if (usernameOrRFID.matches("\\d{10}")) { // Check if input is a 10-digit numeric (RFID)
                // Login using RFID
                query = "CALL login_with_rfid(?)";
                parameter = new Object[]{usernameOrRFID};
            } else {
                // Login using username and password
                query = "CALL login(?, ?)";
                parameter = new Object[]{usernameOrRFID, password};
            }

            java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);
            if (!results.isEmpty()) {
                Map<String, Object> getData = results.get(0);
                Long code = (Long) getData.get("code");
                if (code.equals(200L)) {
                    String uuid = (String) getData.get("user_id");
                    String username = (String) getData.get("username");
                    UserSessionCache cache = new UserSessionCache();
                    cache.login(username, uuid);

                    String welcomeMessage = "Selamat Datang " + getData.get("nama_lengkap");

                    // Buat frame baru untuk animasi
                    JFrame animationFrame = new JFrame();
                    animationFrame.setUndecorated(true); // Hilangkan border frame
                    animationFrame.setSize(parentFrame.getSize());
                    animationFrame.setLocationRelativeTo(parentFrame);
                    animationFrame.add(new LoginSuccessAnimation(animationFrame, welcomeMessage));
                    animationFrame.setVisible(true);

                    // Sembunyikan frame login
                    parentFrame.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(Login.this, "Login gagal", (String) getData.get("message"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        txtUsernameOrRFID.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performLogin();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performLogin();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performLogin();
            }

            private void performLogin() {
                String usernameOrRFID = txtUsernameOrRFID.getText();
                if (usernameOrRFID.matches("\\d{10}")) { // Check if input is a 10-digit numeric (RFID)
                    // Catch Field
                    String password = txtPassword.getPassword(); // Use `getPassword()` to get password from CustomTextField

                    // Executor
                    QueryExecutor executor = new QueryExecutor();
                    String query;
                    Object[] parameter;

                    if (usernameOrRFID.matches("\\d{10}")) { // Check if input is a 10-digit numeric (RFID)
                        // Login using RFID
                        query = "CALL login_with_rfid(?)";
                        parameter = new Object[]{usernameOrRFID};
                    } else {
                        // Login using username and password
                        query = "CALL login(?, ?)";
                        parameter = new Object[]{usernameOrRFID, password};
                    }

                    java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);
                    if (!results.isEmpty()) {
                        Map<String, Object> getData = results.get(0);
                        Long code = (Long) getData.get("code");
                        if (code.equals(200L)) {
                            String uuid = (String) getData.get("user_id");
                            String username = (String) getData.get("username"); // Retrieve the username from the result
                            UserSessionCache cache = new UserSessionCache();
                            cache.login(username, uuid);
                            JOptionPane.showMessageDialog(Login.this, "Selamat Datang " + getData.get("nama_lengkap"), (String) getData.get("message"), JOptionPane.INFORMATION_MESSAGE);
                            new Drawer().setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(Login.this, "Login gagal", (String) getData.get("message"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        return cardPanel;
    }

    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, Color.WHITE),
                        new EmptyBorder(5, 0, 5, 0)
                ), title, TitledBorder.LEFT, TitledBorder.ABOVE_TOP, null, Color.WHITE
        );
    }    
}
