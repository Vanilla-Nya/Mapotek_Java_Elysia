package Main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

import javax.sound.sampled.SourceDataLine;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import Absensi.Absensi;
import Absensi.AllAbsensi;
import Antrian.AntrianPasien;
import Auth.AuthFrame;
import Components.CustomTitleBarFrame;
import Dashboard.Dashboard;
import DataBase.QueryExecutor;
import DataBase.Scheduler.BatchStatusScheduler;
import Global.UserSessionCache;
import Obat.ManagementObatDanObatExpierd;
import Pasien.Pasien;
import Pembukuan.Pembukuan;
import Pemeriksaan.TablePemeriksaan;
import User.User;
import Profile.ProfileForm;

public class Drawer extends JFrame {

    private JFrame frame;
    public int role = 0;
    private final JPanel drawerPanel;
    private final JPanel mainPanel;
    private final JLabel contentLabel;
    private boolean isDrawerCollapsed = false;
    private final JButton toggleButton;
    private final JButton dashboardButton, pasienButton, obatButton, queueButton, pembukuanButton, pemeriksaanButton, userButton, absensiButton, allAbsensiButton, profileButton;
    private final java.util.List<JButton> drawerButtons = new java.util.ArrayList<>();

    // Single instance of Absensi class
    private Absensi absensiInstance;

    private final boolean[] isTextAnimationRunning = {false};

    private Timer drawerTimer;

    // Tambahkan di bagian atas kelas Drawer
    private boolean isDrawerAnimationRunning = false;

    // Tambahkan tombol Logout di deklarasi variabel
    private final JButton logoutButton;

    public Drawer() {
        BatchStatusScheduler.startScheduler();
        UserSessionCache cache = new UserSessionCache();
        String uuid = (String) cache.getUUID();
        System.out.println("UUID: " + uuid);
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
        toggleButton = createDrawerButton(" Menu");
        toggleButton.setIcon(new ImageIcon(new ImageIcon(
            getClass().getClassLoader().getResource("assets/bars-solid.png")
        ).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))); // Ikon ukuran 20x20

        logoutButton = createDrawerButton(" Logout");
        logoutButton.setIcon(new ImageIcon(new ImageIcon(
            getClass().getClassLoader().getResource("assets/house-solid.png")
        ).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Bersihkan sesi pengguna
                UserSessionCache sessionCache = new UserSessionCache();
                sessionCache.clearCache();

                // Tutup frame Drawer dan kembali ke halaman login
                dispose();
                SwingUtilities.invokeLater(() -> {
                    AuthFrame authFrame = new AuthFrame();
                    authFrame.setVisible(true);
                    authFrame.resetToLogin(); // Pastikan kembali ke panel login
                });
            }
        }); // Example action, replace as needed

        // Tambahkan ActionListener untuk toggle drawer
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
        profileButton = createDrawerButton(" Profile");

        // Add buttons to drawerButtons list for easy management
        drawerButtons.add(dashboardButton);
        drawerButtons.add(pasienButton);
        drawerButtons.add(obatButton);
        drawerButtons.add(queueButton);
        drawerButtons.add(pemeriksaanButton);
        drawerButtons.add(pembukuanButton);
        drawerButtons.add(userButton);
        drawerButtons.add(absensiButton);
        drawerButtons.add(allAbsensiButton);
        drawerButtons.add(profileButton);

        dashboardButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/house-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Dashboard
        pasienButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/hospital-user-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Pasien
        obatButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/pills-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Obat
        queueButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/list-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Antrian
        pemeriksaanButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/magnifying-glass-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Pemriksaan
        pembukuanButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/book-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Pembukuan
        userButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/user-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Management User
        absensiButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/user-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Absensi
        allAbsensiButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/user-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for All Absensi
        profileButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/user-solid.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));  // Set icon for Profile

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
        profileButton.addActionListener(e -> showContent("Profile", this));

        // Add Components to Drawer
        drawerPanel.add(toggleButton);
        drawerPanel.add(dashboardButton);
        drawerPanel.add(pasienButton);
        drawerPanel.add(obatButton);    
        drawerPanel.add(profileButton);
        drawerPanel.add(pemeriksaanButton);
        drawerPanel.add(pembukuanButton);
        drawerPanel.add(userButton);
        drawerPanel.add(absensiButton);
        drawerPanel.add(allAbsensiButton);

        // Tambahkan glue untuk mendorong tombol Logout ke bawah
        drawerPanel.add(Box.createVerticalGlue());

        // Tambahkan tombol Logout di bagian paling bawah
        drawerPanel.add(logoutButton);


        System.out.println("Role: " + role);

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
        mainPanel.setPreferredSize(new Dimension(1280 - 250, 720)); // 250 = lebar drawer

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
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // Padding atas, kiri, bawah, kanan
        button.setHorizontalAlignment(SwingConstants.LEFT); // Ikon tetap di kiri
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Lebar penuh, tinggi 50px

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
        } else if (section.equals("Profile")) {
            UserSessionCache sessionCache = new UserSessionCache();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    // Operasi berat di sini (misalnya, memuat data dari database)
                    return null;
                }

                @Override
                protected void done() {
                    // Tambahkan konten ke mainPanel setelah operasi selesai
                    mainPanel.removeAll();
                    mainPanel.add(new ProfileForm(sessionCache), BorderLayout.CENTER);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                }
            };
            worker.execute();
        } else if (section.equals("Pasien")) {
            mainPanel.add(new Pasien(role).getContentPane(), BorderLayout.CENTER);
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
        int targetWidth = isDrawerCollapsed ? 250 : 50; // Ukuran akhir drawer
        int currentWidth = drawerPanel.getWidth(); // Ukuran saat ini
        int step = isDrawerCollapsed ? 10 : -10; // Langkah perubahan ukuran

        Timer animationTimer = new Timer(10, null); // Timer dengan interval 10ms
        animationTimer.addActionListener(e -> {
            int newWidth = drawerPanel.getWidth() + step; // Hitung ukuran baru
            if ((step > 0 && newWidth >= targetWidth) || (step < 0 && newWidth <= targetWidth)) {
                newWidth = targetWidth; // Pastikan ukuran tidak melebihi target
                animationTimer.stop(); // Hentikan animasi
                isDrawerCollapsed = !isDrawerCollapsed; // Toggle status drawer

                // Perbarui tampilan tombol berdasarkan status drawer
                if (isDrawerCollapsed) {
                    for (JButton button : drawerButtons) {
                        button.setText(""); // Hapus teks
                        button.setHorizontalAlignment(SwingConstants.CENTER); // Ikon di tengah
                        button.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0)); // Padding untuk ikon di tengah
                    }
                    // Perbarui tombol Logout
                    logoutButton.setText(""); // Hapus teks
                    logoutButton.setHorizontalAlignment(SwingConstants.CENTER); // Ikon di tengah
                    logoutButton.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0)); // Padding untuk ikon di tengah

                    toggleButton.setText(""); // Hapus teks untuk toggleButton
                    toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
                    toggleButton.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
                } else {
                    for (JButton button : drawerButtons) {
                        button.setHorizontalAlignment(SwingConstants.LEFT); // Ikon dan teks sejajar ke kiri
                        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // Padding untuk ikon dan teks

                        // Tambahkan teks kembali ke tombol
                        if (button == dashboardButton) button.setText(" Dashboard");
                        else if (button == pasienButton) button.setText(" Pasien");
                        else if (button == obatButton) button.setText(" Obat");
                        else if (button == queueButton) button.setText(" Antrian");
                        else if (button == pemeriksaanButton) button.setText(" Pemeriksaan");
                        else if (button == pembukuanButton) button.setText(" Pembukuan");
                        else if (button == userButton) button.setText(" Management User");
                        else if (button == absensiButton) button.setText(" Absensi");
                        else if (button == allAbsensiButton) button.setText(" All Absensi");
                        else if (button == profileButton) button.setText(" Profile");
                    }
                    // Perbarui tombol Logout
                    logoutButton.setText(" Logout");
                    logoutButton.setHorizontalAlignment(SwingConstants.LEFT); // Ikon dan teks sejajar ke kiri
                    logoutButton.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // Padding untuk ikon dan teks

                    toggleButton.setText(" Menu");
                    toggleButton.setHorizontalAlignment(SwingConstants.LEFT);
                    toggleButton.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
                }
            }

            drawerPanel.setPreferredSize(new Dimension(newWidth, getHeight())); // Ubah ukuran drawer
            drawerPanel.revalidate(); // Perbarui tata letak
            drawerPanel.repaint(); // Perbarui tampilan
        });

        animationTimer.start(); // Mulai animasi
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Drawer().setVisible(true);
        });
    }
}
