package Dashboard;

import Antrian.AntrianPasien;
import Auth.Login;
import Components.CustomPanel;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Main.Drawer;

import java.awt.*;
import java.util.Calendar;
import java.util.Map;

import javax.swing.*;

import Auth.AuthFrame;

/**
 *
 * @author asuna
 */
public class Dashboard extends JFrame {

    public int role;
    public String NameRole;
    private Long patientCount;
    private JLabel patientCountLabel;
    private UserSessionCache cache;

    public Dashboard(Drawer drawer) {
        cache = new UserSessionCache();
        String uuid = (String) cache.getUUID();
        String username = cache.getusername();
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT id_role FROM user_role WHERE id_user = ? ORDER BY id_role DESC LIMIT 1";
        Object[] userRoleParams = new Object[]{uuid};
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, userRoleParams);
        if (!results.isEmpty()) {
            role = (int) results.get(0).get("id_role");
        }

        // Set role name
        switch (role) {
            case 1 ->
                NameRole = "User";
            case 2 ->
                NameRole = "Dokter";
            case 3 ->
                NameRole = "Admin";
            default ->
                NameRole = "Unknown";
        }

        // Frame setup
        setLayout(new BorderLayout());
        setTitle("Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // ** Left Sidebar ** (Already setup)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(250, getHeight()));

        // Add the Welcome Card to the left panel
        leftPanel.add(createWelcomeCard(username, drawer));  // Welcome card is simplified
        add(leftPanel, BorderLayout.WEST);

        // ** Main Content Panel ** (Queue Section + Jumlah Pasien Button under the table)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Queue Section
        CustomPanel queuePanel = new CustomPanel(20);
        queuePanel.setBackground(new Color(245, 245, 245));
        queuePanel.setLayout(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        queuePanel.setPreferredSize(new Dimension(0, (int) (getHeight() / 3)));

        // Title Label for "Antrian Sekarang"
        JLabel titleLabel = new JLabel("Antrian Sekarang", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 150, 243));
        titleLabel.setPreferredSize(new Dimension(0, 40));

        queuePanel.add(titleLabel, BorderLayout.NORTH);

        // Create AntrianPasien instance (table only)
        AntrianPasien antrianTablePanel = new AntrianPasien();
        JScrollPane scrollPane = (JScrollPane) antrianTablePanel.getComponent(1);
        JTable antrianTable = (JTable) scrollPane.getViewport().getView(); // Get the JTable from the viewport

        // Add the table to the queue panel
        queuePanel.add(scrollPane, BorderLayout.CENTER);

        // Add the queue panel to the main panel (center part)
        mainPanel.add(queuePanel, BorderLayout.CENTER);

        // ** Jumlah Pasien Square Box Section ** (Below the table)
        JPanel jumlahPasienBox = new JPanel();
        jumlahPasienBox.setLayout(new BoxLayout(jumlahPasienBox, BoxLayout.Y_AXIS));
        jumlahPasienBox.setBackground(new Color(33, 150, 243));  // Blue background for the box
        jumlahPasienBox.setPreferredSize(new Dimension(150, 150));  // Make it square (150px x 150px)
        jumlahPasienBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Label "Jumlah Pasien"
        JLabel labelJumlahPasien = new JLabel("Jumlah Pasien");
        labelJumlahPasien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelJumlahPasien.setForeground(Color.WHITE);
        labelJumlahPasien.setHorizontalAlignment(SwingConstants.CENTER);
        labelJumlahPasien.setPreferredSize(new Dimension(150, 40));

        // Label to show the patient count
        patientCountLabel = new JLabel(String.valueOf(patientCount));
        patientCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        patientCountLabel.setForeground(Color.WHITE);
        patientCountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add labels to the panel
        jumlahPasienBox.add(labelJumlahPasien);
        jumlahPasienBox.add(patientCountLabel);

        // Action listener to update the count on click
        jumlahPasienBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                updatePatientCount();  // Update patient count when clicked
            }
        });

        // Add the square box under the table (at the bottom of the main panel)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(jumlahPasienBox);

        // Add the bottom panel with the square box under the table
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add the main panel to the frame
        add(mainPanel, BorderLayout.CENTER);

        // Fetch initial patient count from the database
        updatePatientCount();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                queuePanel.setPreferredSize(new Dimension(0, getHeight() / 3));
                revalidate(); // Revalidate the layout after resizing
            }
        });
    }

    private void updatePatientCount() {
        // Query to get the count of patients from the database
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT COUNT(*) AS jumlah_antrian FROM antrian WHERE MONTH(tanggal_antrian) = MONTH(CURDATE()) AND YEAR(tanggal_antrian) = YEAR(CURDATE())";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

        // Update patientCount and the label
        if (!results.isEmpty()) {
            patientCount = (Long) results.get(0).get("jumlah_antrian");
            patientCountLabel.setText(String.valueOf(patientCount));  // Update the label
        }
    }

// Method to return only the content panel
    public JFrame getContentPanel() {
        return this; // Return this JPanel that contains the full dashboard content
    }

    private JPanel createWelcomeCard(String username, Drawer drawer) {
        // Create a JPanel with rounded corners
        JPanel welcomeCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Create rounded corners for the card using Graphics2D
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Set the color for the rounded border (black)
                g2d.setColor(Color.BLACK);  // Black color for the border
                g2d.setStroke(new BasicStroke(3));  // Thickness of the border (3px)

                // Draw the rounded rectangle border
                int radius = 30;  // Corner radius
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);  // Rounded rectangle
            }
        };

        // Set the background color of the card and preferred size
        welcomeCard.setBackground(new Color(245, 245, 245));  // Light background color
        welcomeCard.setPreferredSize(new Dimension(250, getHeight()));  // Full height of the left panel
        welcomeCard.setLayout(new BoxLayout(welcomeCard, BoxLayout.Y_AXIS));

        // Add padding around the content inside the card
        welcomeCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding inside the card

        // Top Section: Welcome message and username
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);  // Make the top section transparent to blend with the card's background

        // Title label for the welcome card
        JLabel titleLabel = new JLabel("Welcome", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 150, 243));  // Blue color for the title
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topSection.add(titleLabel);

        // Username label (optional if username is available)
        if (username != null) {
            JLabel usernameLabel = new JLabel(NameRole + " " + username, SwingConstants.CENTER);
            usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            usernameLabel.setForeground(Color.BLACK);
            usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            topSection.add(usernameLabel);

            // Get the current day of the week
            String dayOfWeek = getDayOfWeekGreeting();
            JLabel dayGreetingLabel = new JLabel(dayOfWeek, SwingConstants.CENTER);
            dayGreetingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            dayGreetingLabel.setForeground(Color.BLACK);
            dayGreetingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            topSection.add(dayGreetingLabel);  // Add the "Have a nice [Day]" message
        }

        // Add the top section to the card
        welcomeCard.add(topSection);

        // Add vertical glue to push the logout button to the bottom
        welcomeCard.add(Box.createVerticalGlue());

        // Bottom Section: Log out button (full width)
        JPanel bottomSection = new JPanel();
        bottomSection.setOpaque(false);  // Make the bottom section transparent to blend with the card's background
        bottomSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.X_AXIS));  // Align components horizontally

        // Log Out button (full width)
        JButton logoutButton = new JButton("Log Out");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutButton.setBackground(new Color(33, 150, 243));  // Use the same blue color
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setPreferredSize(new Dimension(250, 40));  // Full width of the card
        logoutButton.setMaximumSize(new Dimension(250, 40));   // Ensure button doesn't stretch wider than the card width
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(e -> {
            cache.clearCache();
            drawer.dispose();
            dispose();
            // Buka AuthFrame baru dan reset ke login
            SwingUtilities.invokeLater(() -> {
                AuthFrame auth = new AuthFrame();
                auth.setVisible(true);
                auth.resetToLogin(); // <--- Tambahkan baris ini
            });
        });

        // Add the logout button to the bottom section
        bottomSection.add(logoutButton);

        // Add the bottom section to the card
        welcomeCard.add(bottomSection);

        return welcomeCard;
    }

// Helper function to return a greeting based on the current day
    private String getDayOfWeekGreeting() {
        // Get the current day of the week
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Map day of the week to greeting message
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Have a nice Monday!";
            case Calendar.TUESDAY:
                return "Have a nice Tuesday!";
            case Calendar.WEDNESDAY:
                return "Have a nice Wednesday!";
            case Calendar.THURSDAY:
                return "Have a nice Thursday!";
            case Calendar.FRIDAY:
                return "Have a nice Friday!";
            case Calendar.SATURDAY:
                return "Have a nice Saturday!";
            case Calendar.SUNDAY:
                return "Have a nice Sunday!";
            default:
                return "Have a great day!";
        }
    }

    // Main method to launch the dashboard
    public static void main(String[] args, Drawer drawer) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Dashboard(drawer).setVisible(true);
            }
        });
    }
}
