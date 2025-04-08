package Main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import Absensi.Absensi;
import Absensi.AllAbsensi;
import Antrian.AntrianPasien;
import Components.CustomTitleBarFrame;
import Dashboard.Dashboard;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Obat.ManagementObatDanObatExpierd;
import Pasien.Pasien;
import Pembukuan.Pembukuan;
import Pemeriksaan.TablePemeriksaan;
import User.User;

public class Drawer extends JFrame {

    private JFrame frame;
    public int role = 0;
    private final JPanel drawerPanel;
    private final JPanel mainPanel;
    private final JLabel contentLabel;
    private boolean isDrawerCollapsed = false;
    private final JButton toggleButton;
    private final JButton dashboardButton, pasienButton, obatButton, queueButton, pembukuanButton, pemeriksaanButton, userButton, absensiButton, allAbsensiButton;

    // Single instance of Absensi class
    private Absensi absensiInstance;

    public Drawer() {
        UserSessionCache cache = new UserSessionCache();
        String uuid = (String) cache.getUUID();
        QueryExecutor executor = new QueryExecutor();
        String Query = "SELECT id_role FROM user_role WHERE id_user = ? ORDER BY id_role DESC LIMIT 1";
        Object[] userrole = new Object[]{uuid};
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(Query, userrole);
        if (!results.isEmpty()) {
            role = (int) results.get(0).get("id_role");
        }

        setUndecorated(true); // Remove the default title bar
        CustomTitleBarFrame customTitleBar = new CustomTitleBarFrame(
                "Mapotek", // Title text
                Color.WHITE, // Title bar background color
                Color.RED, // Title text color
                Color.RED, // Close button color
                this::dispose, // Close action (disposes the dialog)
                this::minimizeWindow // Minimize action
        );
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Set the shape of the JFrame to have rounded corners
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40)); // 40 is the corner radius

        // Sidebar / Drawer Panel
        drawerPanel = new JPanel();
        drawerPanel.setLayout(new BoxLayout(drawerPanel, BoxLayout.Y_AXIS));
        drawerPanel.setBackground(new Color(58, 64, 74));
        drawerPanel.setPreferredSize(new Dimension(250, getHeight()));

        // Toggle Button for Drawer Collapse/Expand
        toggleButton = new JButton("");
        toggleButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/bars-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        toggleButton.setFocusPainted(false);
        toggleButton.setForeground(Color.WHITE);
        toggleButton.setBackground(new Color(58, 64, 74));
        toggleButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        toggleButton.addActionListener(e -> toggleDrawer());

        // Buttons for the Drawer
        dashboardButton = createDrawerButton(" Dashboard");
        pasienButton = createDrawerButton(" Pasien");
        obatButton = createDrawerButton(" Obat");
        queueButton = createDrawerButton(" Antrian");
        pemeriksaanButton = createDrawerButton(" Pemeriksaan");
        pembukuanButton = createDrawerButton(" Pembukuan");
        userButton = createDrawerButton(" Management User");
        absensiButton = createDrawerButton(" Absensi");
        allAbsensiButton = createDrawerButton(" All Absensi");

        dashboardButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/house-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Dashboard
        pasienButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/hospital-user-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Pasien
        obatButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/pills-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Obat
        queueButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/list-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Antrian
        pemeriksaanButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/magnifying-glass-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Pemriksaan
        pembukuanButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/book-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Pembukuan
        userButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/user-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Management User
        absensiButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/user-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Absensi
        allAbsensiButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/user-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for All Absensi

        // Add Action Listeners to Drawer Buttons
        dashboardButton.addActionListener(e -> showContent("Dashboard", this));
        pasienButton.addActionListener(e -> showContent("Pasien", this));
        obatButton.addActionListener(e -> showContent("Obat", this));
        queueButton.addActionListener(e -> showContent("Antrian", this));
        pemeriksaanButton.addActionListener(e -> showContent("Pemeriksaan", this));
        pembukuanButton.addActionListener(e -> showContent("Pembukuan", this));
        userButton.addActionListener(e -> showContent("User", this));
        absensiButton.addActionListener(e -> showContent("Absensi", this));
        allAbsensiButton.addActionListener(e -> showContent("AllAbsensi", this));

        // Add Components to Drawer
        drawerPanel.add(toggleButton);
        drawerPanel.add(Box.createRigidArea(new Dimension(0, 10)));  // Spacing
        drawerPanel.add(dashboardButton);
        drawerPanel.add(pasienButton);
        drawerPanel.add(obatButton);
        drawerPanel.add(queueButton);
        drawerPanel.add(pemeriksaanButton);
        drawerPanel.add(pembukuanButton);
        drawerPanel.add(userButton);
        drawerPanel.add(absensiButton);
        drawerPanel.add(allAbsensiButton);

        switch (role) {
            case 1 -> {
                toggleButton.setVisible(true);
                dashboardButton.setVisible(true);
                pasienButton.setVisible(true);
                obatButton.setVisible(false);
                queueButton.setVisible(true);
                pemeriksaanButton.setVisible(false);
                pembukuanButton.setVisible(false);
                userButton.setVisible(false);
                absensiButton.setVisible(true);
                allAbsensiButton.setVisible(false);
            }
            case 2 -> {
                toggleButton.setVisible(true);
                dashboardButton.setVisible(true);
                pasienButton.setVisible(false);
                obatButton.setVisible(false);
                queueButton.setVisible(true);
                pemeriksaanButton.setVisible(true);
                pembukuanButton.setVisible(false);
                userButton.setVisible(false);
                absensiButton.setVisible(true);
                allAbsensiButton.setVisible(false);
            }
            case 3 -> {
                toggleButton.setVisible(true);
                dashboardButton.setVisible(true);
                pasienButton.setVisible(true);
                obatButton.setVisible(true);
                queueButton.setVisible(true);
                pemeriksaanButton.setVisible(true);
                pembukuanButton.setVisible(true);
                userButton.setVisible(true);
                absensiButton.setVisible(true);
                allAbsensiButton.setVisible(true);
            }
            default -> {
            }
        }
        // Main Content Panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        contentLabel = new JLabel("Welcome to the Dashboard", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // 20px padding top/bottom, 30px left/right
        mainPanel.add(contentLabel, BorderLayout.CENTER);

        // Add Drawer and Main Panel to Frame
        add(customTitleBar, BorderLayout.PAGE_START);
        add(drawerPanel, BorderLayout.WEST);
        add(mainPanel);

        setResizable(false);

        // Set the default content to "Dashboard" when the application starts
        showContent("Dashboard", this);
    }

    // Method to minimize the window
    private void minimizeWindow(java.awt.event.ActionEvent e) {
        setState(JFrame.ICONIFIED);  // This minimizes the window
    }

    // Method to create a styled button for the drawer
    private JButton createDrawerButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(58, 64, 74));
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Button hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 80, 95));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(58, 64, 74));
            }
        });
        return button;
    }

    // Method to update content based on selection
    private void showContent(String section, Drawer drawer) {
        mainPanel.removeAll(); // Clear previous content

        if (section.equals("Dashboard")) {
            mainPanel.add(new Dashboard(this).getContentPane(), BorderLayout.CENTER);
        } else if (section.equals("Pasien")) {
            mainPanel.add(new Pasien().getContentPane(), BorderLayout.CENTER);
        } else if (section.equals("Obat")) {
            ManagementObatDanObatExpierd managementObat = new ManagementObatDanObatExpierd();
            mainPanel.add(managementObat.getContentPane(), BorderLayout.CENTER);
        } else if (section.equals("Antrian")) {
            mainPanel.add(new AntrianPasien(), BorderLayout.CENTER);
        } else if (section.equals("Pemeriksaan")) {
            mainPanel.add(new TablePemeriksaan().getContentPane(), BorderLayout.CENTER);
        } else if (section.equals("Pembukuan")) {
            mainPanel.add(new Pembukuan(), BorderLayout.CENTER);
        } else if (section.equals("User")) {
            mainPanel.add(new User().getContentPane(), BorderLayout.CENTER);
        } else if (section.equals("Absensi")) {
            if (absensiInstance == null) {
                absensiInstance = new Absensi();
            }
            mainPanel.add(absensiInstance.getContentPane(), BorderLayout.CENTER);
        } else if (section.equals("AllAbsensi")) {
            mainPanel.add(new AllAbsensi().getContentPane(), BorderLayout.CENTER);
        } else {
            contentLabel.setText("Currently Viewing: " + section);
            mainPanel.add(contentLabel, BorderLayout.CENTER);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Method to toggle the drawer's collapsed state
    private void toggleDrawer() {

        if (isDrawerCollapsed) {
            drawerPanel.setPreferredSize(new Dimension(250, getHeight()));  // Expand drawer
            toggleButton.setText("");  // Change to collapse icon/text
            dashboardButton.setText(" Dashboard");
            pasienButton.setText(" Pasien");
            obatButton.setText(" Obat");
            queueButton.setText(" Antrian");
            pemeriksaanButton.setText(" Pemeriksaan");
            pembukuanButton.setText(" Pembukuan");
            userButton.setText(" Management User");
            absensiButton.setText(" Absensi");
            allAbsensiButton.setText(" All Absensi");

        } else {
            drawerPanel.setPreferredSize(new Dimension(50, getHeight()));

            // Hide text in collapsed state
            dashboardButton.setText("");  // Hide text in collapsed state
            pasienButton.setText("");
            obatButton.setText("");
            queueButton.setText("");
            pemeriksaanButton.setText("");
            pembukuanButton.setText("");
            userButton.setText("");
            absensiButton.setText("");
            allAbsensiButton.setText("");

            // Optionally, set the button size if needed to fit the icons
            dashboardButton.setPreferredSize(new Dimension(40, 40));
            pasienButton.setPreferredSize(new Dimension(40, 40));
            obatButton.setPreferredSize(new Dimension(40, 40));
            queueButton.setPreferredSize(new Dimension(40, 40));
            pemeriksaanButton.setPreferredSize(new Dimension(40, 40));
            pembukuanButton.setPreferredSize(new Dimension(40, 40));
            userButton.setPreferredSize(new Dimension(40, 40));
            absensiButton.setPreferredSize(new Dimension(40, 40));
            allAbsensiButton.setPreferredSize(new Dimension(40, 40));
        }
        isDrawerCollapsed = !isDrawerCollapsed;
        revalidate();  // Refresh the layout
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Drawer().setVisible(true);
        });
    }
}
