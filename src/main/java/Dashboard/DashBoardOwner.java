package Dashboard;

import Components.IncomeExpenseCard;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Main.Drawer;

import javax.swing.*;

import Auth.AuthFrame;

import java.awt.*;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

public class DashBoardOwner extends JPanel {

    private String username;
    private String roleName;

    public DashBoardOwner() {
        // Ambil data pengguna dari sesi
        UserSessionCache cache = new UserSessionCache();
        String uuid = (String) cache.getUUID();
        QueryExecutor executor = new QueryExecutor();

        // Ambil username
        username = cache.getusername();

        // Ambil role pengguna dari database
        String query = "SELECT id_role FROM user_role WHERE id_user = ? ORDER BY id_role DESC LIMIT 1";
        Object[] params = new Object[]{uuid};
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, params);

        int role = 0;
        if (!results.isEmpty()) {
            role = ((Number) results.get(0).get("id_role")).intValue();
        }

        // Tentukan nama role berdasarkan ID role
        switch (role) {
            case 1 -> roleName = "Dokter";
            case 2 -> roleName = "Admin";
            case 3 -> roleName = "Owner";
            default -> roleName = "Unknown";
        }

        // Panel setup
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ** Main Content Section **
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // ** Welcome Card **
        JPanel welcomeCard = createWelcomeCard(username);
        mainPanel.add(welcomeCard);
        mainPanel.add(Box.createVerticalStrut(20)); // Spasi antara Welcome Card dan Statistik Cards

        // ** Statistik Cards **
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setPreferredSize(new Dimension(0, 150));

        int pemasukan = getPemasukanDashboard();
        int pengeluaran = getPengeluaranDashboard();
        int expiredCount = getExpiredObatCount();
        int lowStockCount = getLowStockObatCount();

        statsPanel.add(new IncomeExpenseCard("Pemasukan Bulan Ini", pemasukan, pengeluaran));
        ImageIcon expiredIcon = new ImageIcon(new ImageIcon(
                getClass().getClassLoader().getResource("assets/Obat_Expierd.png"))
                .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)); // Ukuran ikon 30x30

        ImageIcon lowStockIcon = new ImageIcon(new ImageIcon(
                getClass().getClassLoader().getResource("assets/Stock_Menipis.png"))
                .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)); // Ukuran ikon 30x30

        statsPanel.add(createStatCard(String.valueOf(expiredCount), "Obat Expired", new Color(244, 67, 54), expiredIcon)); // Kartu tengah
        statsPanel.add(createStatCard(String.valueOf(lowStockCount), "Obat Hampir Habis", new Color(255, 152, 0), lowStockIcon)); // Kartu kanan

        mainPanel.add(statsPanel);
        mainPanel.add(Box.createVerticalStrut(20)); // Spasi antara Statistik Cards dan Panel Antrian

        // ** Panel Antrian **
        JPanel queuePanel = createQueuePanel();
        mainPanel.add(queuePanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createQueuePanel() {
        // Panel utama untuk Antrian
        JPanel queuePanel = new JPanel(new GridBagLayout());
        queuePanel.setBackground(Color.WHITE);
        queuePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                "Antrian",
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 16),
                Color.BLACK
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ** Antrian Saat Ini ** Panel
        JPanel currentQueuePanel = new JPanel();
        currentQueuePanel.setLayout(new BoxLayout(currentQueuePanel, BoxLayout.Y_AXIS));
        currentQueuePanel.setBackground(Color.WHITE);

        // Safely get the icon
        URL imageUrl = getClass().getResource("/assets/antrian_saatini.png");
        if (imageUrl != null) {
            ImageIcon queueIconImage = new ImageIcon(
                new ImageIcon(imageUrl).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)
            );
            JLabel queueIcon = new JLabel(queueIconImage);
            queueIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            currentQueuePanel.add(queueIcon);
        } else {
            System.out.println("Image not found: /assets/antrian_saatini.png");
        }

        // Ambil nomor antrian dan nama dari database
        Map<String, String> queueData = getQueueDataFromDatabase();
        String queueNumber = queueData.get("no_antrian");
        String atasNama = queueData.get("nama");

        JLabel queueLabel;
        if (queueNumber == null || queueNumber.isEmpty()) {
            queueLabel = new JLabel("Tidak ada antrian hari ini");
            queueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            queueLabel.setForeground(Color.GRAY);
        } else {
            queueLabel = new JLabel(queueNumber);
            queueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
            queueLabel.setForeground(new Color(244, 67, 54));
        }
        queueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel queueText = new JLabel("Antrian saat ini");
        queueText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        queueText.setForeground(Color.BLACK);
        queueText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel atasNamaLabel = new JLabel();
        if (atasNama == null || atasNama.isEmpty()) {
            atasNamaLabel.setText("Atas Nama: -");
        } else {
            atasNamaLabel.setText("Atas Nama: " + atasNama);
        }
        atasNamaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        atasNamaLabel.setForeground(Color.BLACK);
        atasNamaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        currentQueuePanel.add(Box.createVerticalStrut(10)); // Spasi
        currentQueuePanel.add(queueLabel);
        currentQueuePanel.add(Box.createVerticalStrut(5)); // Spasi
        currentQueuePanel.add(queueText);
        currentQueuePanel.add(Box.createVerticalStrut(5)); // Spasi
        currentQueuePanel.add(atasNamaLabel);

        gbc.gridx = 0;
        gbc.weightx = 0.45; // Lebar relatif
        queuePanel.add(currentQueuePanel, gbc);

        // ** Divider ** JSeparator
        JSeparator divider = new JSeparator(SwingConstants.VERTICAL);
        divider.setForeground(new Color(200, 200, 200));
        gbc.gridx = 1;
        gbc.weightx = 0.1; // Lebar relatif untuk divider
        queuePanel.add(divider, gbc);

        // ** Total Pasien Saat Ini ** Panel
        JPanel totalPatientPanel = new JPanel();
        totalPatientPanel.setLayout(new BoxLayout(totalPatientPanel, BoxLayout.Y_AXIS));
        totalPatientPanel.setBackground(Color.WHITE);

        // Tambahkan ikon untuk total pasien
        ImageIcon patientIconImage = new ImageIcon(
            new ImageIcon(getClass().getResource("/assets/pasien_saatini.png"))
                .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)
        );
        JLabel patientIcon = new JLabel(patientIconImage);
        patientIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Ambil total pasien dari database
        int totalPatients = getTotalPatientsToday();

        // Pastikan nilai default ditampilkan jika data tidak tersedia
        String totalPatientsText = totalPatients > 0 ? String.valueOf(totalPatients) : "0";

        JLabel patientLabel = new JLabel(totalPatientsText);
        patientLabel.setText(totalPatientsText);
        patientLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        patientLabel.setForeground(new Color(76, 175, 76));
        patientLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel patientText = new JLabel("Total pasien saat ini");
        patientText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        patientText.setForeground(Color.BLACK);
        patientText.setAlignmentX(Component.CENTER_ALIGNMENT);

        totalPatientPanel.add(Box.createVerticalStrut(10)); // Spasi
        totalPatientPanel.add(patientIcon); // Tambahkan ikon di atas
        totalPatientPanel.add(Box.createVerticalStrut(10)); // Spasi
        totalPatientPanel.add(patientLabel);
        totalPatientPanel.add(Box.createVerticalStrut(5)); // Spasi
        totalPatientPanel.add(patientText);

        gbc.gridx = 2;
        gbc.weightx = 0.45; // Lebar relatif
        queuePanel.add(totalPatientPanel, gbc);

        return queuePanel;
    }

    // Fungsi untuk mendapatkan nomor antrian dan nama dari database
    private Map<String, String> getQueueDataFromDatabase() {
        Map<String, String> queueData = new java.util.HashMap<>();
        try {
            QueryExecutor executor = new QueryExecutor();
            String query = "CALL get_noantrian_terbaru()";
            java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

            if (!results.isEmpty()) {
                queueData.put("no_antrian", (String) results.get(0).get("no_antrian")); // Ganti "no_antrian" sesuai nama kolom
                queueData.put("nama", (String) results.get(0).get("nama")); // Ganti "nama" sesuai nama kolom
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log error untuk debugging
        }
        return queueData;
    }

    // Fungsi untuk mendapatkan total pasien hari ini dari database
    private int getTotalPatientsToday() {
        int totalPatients = 0;
        try {
            QueryExecutor executor = new QueryExecutor();
            String query = "CALL get_totalpasientoday()";
            java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

            System.out.println("Results: " + results); // Debug log

            if (!results.isEmpty()) {
                Object totalAntrianObj = results.get(0).get("total_pasien_hari_ini"); // Ganti nama kolom   
                System.out.println("Total Antrian Object: " + totalAntrianObj); // Debug log
                if (totalAntrianObj != null) {
                    totalPatients = ((Number) totalAntrianObj).intValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalPatients;
    }

    private int getPemasukanDashboard() {
        int pemasukan = 0;
        try {
            QueryExecutor executor = new QueryExecutor();
            String query = "CALL get_pemasukandashboard()";
            java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

            if (!results.isEmpty()) {
                Object pemasukanObj = results.get(0).get("total_pemasukan"); // Ganti nama kolom sesuai database
                if (pemasukanObj != null) {
                    pemasukan = ((Number) pemasukanObj).intValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pemasukan;
    }

    private int getPengeluaranDashboard() {
        int pengeluaran = 0;
        try {
            QueryExecutor executor = new QueryExecutor();
            String query = "CALL get_pengeluarandashboard()";
            java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

            if (!results.isEmpty()) {
                Object pengeluaranObj = results.get(0).get("total_pengeluaran"); // Ganti nama kolom sesuai database
                if (pengeluaranObj != null) {
                    pengeluaran = ((Number) pengeluaranObj).intValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pengeluaran;
    }

    private int getExpiredObatCount() {
        int expiredCount = 0;
        try {
            QueryExecutor executor = new QueryExecutor();
            String query = "CALL count_all_obat_expierd()"; // Prosedur untuk menghitung obat expired
            java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

            if (!results.isEmpty()) {
                Object countObj = results.get(0).get("jumlah_data"); // Sesuaikan dengan nama kolom di query
                if (countObj != null) {
                    expiredCount = ((Number) countObj).intValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return expiredCount;
    }

    private int getLowStockObatCount() {
        int lowStockCount = 0;
        try {
            QueryExecutor executor = new QueryExecutor();
            String query = "CALL count_obat_menipis()"; // Prosedur untuk menghitung obat hampir habis
            java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

            if (!results.isEmpty()) {
                Object countObj = results.get(0).get("jumlah_obat_menipis"); // Sesuaikan dengan nama kolom di query
                if (countObj != null) {
                    lowStockCount = ((Number) countObj).intValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lowStockCount;
    }

    private JPanel createStatCard(String value, String label, Color color, ImageIcon icon) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Jika value adalah "0", tampilkan "Obat Aman"
        String displayValue = value.equals("0") ? "Obat Aman" : value;

        JLabel valueLabel = new JLabel(displayValue, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);

        JLabel textLabel = new JLabel(label, SwingConstants.CENTER);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textLabel.setForeground(Color.WHITE);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(iconLabel, BorderLayout.NORTH); // Tambahkan ikon di atas
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(textLabel, BorderLayout.SOUTH);

        return card;
    }

    private String getDayOfWeekGreeting() {
        // Get the current day of the week
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Map day of the week to greeting message
        return switch (dayOfWeek) {
            case Calendar.MONDAY -> "Have a nice Monday!";
            case Calendar.TUESDAY -> "Have a nice Tuesday!";
            case Calendar.WEDNESDAY -> "Have a nice Wednesday!";
            case Calendar.THURSDAY -> "Have a nice Thursday!";
            case Calendar.FRIDAY -> "Have a nice Friday!";
            case Calendar.SATURDAY -> "Have a nice Saturday!";
            case Calendar.SUNDAY -> "Have a nice Sunday!";
            default -> "Have a great day!";
        };
    }

    private JPanel createWelcomeCard(String username) {
        // Panel utama untuk Welcome Card
        JPanel welcomeCard = new JPanel(new BorderLayout());
        welcomeCard.setBackground(Color.WHITE);
        welcomeCard.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        welcomeCard.setPreferredSize(new Dimension(0, 100)); // Tinggi card diatur menjadi 100 piksel
        welcomeCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // Maksimum tinggi card tetap 100 piksel

        // ** Teks Selamat Datang **
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Welcome, " + roleName);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.BLACK);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        usernameLabel.setForeground(Color.GRAY);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dayGreetingLabel = new JLabel(getDayOfWeekGreeting());
        dayGreetingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16)); // Teks miring
        dayGreetingLabel.setForeground(Color.GRAY);
        dayGreetingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(welcomeLabel);
        textPanel.add(usernameLabel);
        textPanel.add(Box.createVerticalStrut(10)); // Spasi antar teks
        textPanel.add(dayGreetingLabel);

        welcomeCard.add(textPanel, BorderLayout.CENTER);

        // ** Tombol Aksi (Logout dan Absen) **
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        // Tombol Logout
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutButton.setBackground(new Color(244, 67, 54)); // Merah
        logoutButton.setForeground(Color.WHITE);

        logoutButton.addActionListener(e -> {
            // Bersihkan sesi pengguna
            UserSessionCache sessionCache = new UserSessionCache();
            sessionCache.clearCache();

            // Tutup panel DashBoardOwner dan kembali ke halaman login
            SwingUtilities.invokeLater(() -> {
                AuthFrame authFrame = new AuthFrame();
                authFrame.setVisible(true);
                authFrame.resetToLogin(); // Pastikan kembali ke panel login
            });

            // Jika DashBoardOwner berada di dalam JFrame, tutup JFrame-nya
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
        });

        // Tombol Absen
        JButton absenButton = new JButton("Absen");
        absenButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        absenButton.setBackground(new Color(33, 150, 243)); // Biru
        absenButton.setForeground(Color.WHITE);

        absenButton.addActionListener(e -> {
            // Dapatkan referensi ke Drawer
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof Drawer drawer) {
                // Panggil metode showContent untuk menampilkan halaman Absensi
                drawer.showContent("Absensi", drawer);
            }
        });

        buttonPanel.add(absenButton);
        buttonPanel.add(logoutButton);
        welcomeCard.add(buttonPanel, BorderLayout.EAST);

        return welcomeCard;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DashBoardOwner().setVisible(true);
        });
    }
}
