package Profile;

import Global.UserSessionCache;
import DataBase.QueryExecutor;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import Components.CustomCard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Map;

public class ProfileForm extends JPanel {
    public ProfileForm(UserSessionCache sessionCache) {
        setLayout(new BorderLayout());

        // Panel kiri: Profile
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(Color.WHITE);

        // Ambil UUID user dari session
        String uuid = sessionCache.getUUID();

        // Query langganan terbaru milik user
        String sql = "SELECT MIN(tanggal_mulai) AS tanggal_mulai, MAX(tanggal_berakhir) AS tanggal_berakhir FROM langganan WHERE status = 'aktif' AND is_expired = 0";
        QueryExecutor executor = new QueryExecutor();
        List<Map<String, Object>> results = executor.executeSelectQuery(sql, new Object[]{});

        // Ambil data profile dari prosedur
        String namaLengkap = "";
        String jenisKelamin = "";
        String alamat = "";
        String noTelp = "";

        if (uuid != null) {
            QueryExecutor queryExecutor = new QueryExecutor();
            String sqlProfile = "CALL all_profile_user(?)";
            Object[] params = new Object[]{uuid};
            List<Map<String, Object>> resultsProfile = queryExecutor.executeSelectQuery(sqlProfile, params);
            if (!resultsProfile.isEmpty()) {
                Map<String, Object> row = resultsProfile.get(0);
                namaLengkap = row.getOrDefault("nama_lengkap", "").toString();
                jenisKelamin = row.getOrDefault("jenis_kelamin", "").toString();
                alamat = row.getOrDefault("alamat", "").toString();
                noTelp = row.getOrDefault("no_telp", "").toString();
            }
        }

        // Judul
        JLabel titleLabel = new JLabel("Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 150, 243));

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        profilePanel.add(titleLabel, gbc);

        // Nama Lengkap
        gbc.gridwidth = 1; gbc.gridy++;
        profilePanel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        profilePanel.add(new JTextField(namaLengkap, 15) {{ setEditable(false); }}, gbc);

        // Jenis Kelamin
        gbc.gridx = 0; gbc.gridy++;
        profilePanel.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1;
        profilePanel.add(new JTextField(jenisKelamin, 15) {{ setEditable(false); }}, gbc);

        // Alamat
        gbc.gridx = 0; gbc.gridy++;
        profilePanel.add(new JLabel("Alamat:"), gbc);
        gbc.gridx = 1;
        profilePanel.add(new JTextField(alamat, 15) {{ setEditable(false); }}, gbc);

        // No Telp
        gbc.gridx = 0; gbc.gridy++;
        profilePanel.add(new JLabel("No Telp:"), gbc);
        gbc.gridx = 1;
        profilePanel.add(new JTextField(noTelp, 15) {{ setEditable(false); }}, gbc);

        // Panel kanan: Langganan
        JPanel langgananPanel = new JPanel();
        langgananPanel.setBackground(new Color(245, 245, 245));
        langgananPanel.setLayout(new BoxLayout(langgananPanel, BoxLayout.Y_AXIS));

        // Ambil data langganan dari database
        String tanggalMulai = "-";
        String tanggalBerakhir = "-";
        long sisaHari = 0;

        if (!results.isEmpty()) {
            Map<String, Object> row = results.get(0);
            java.sql.Date mulai = (java.sql.Date) row.get("tanggal_mulai");
            java.sql.Date berakhir = (java.sql.Date) row.get("tanggal_berakhir");
            if (mulai != null && berakhir != null) {
                tanggalMulai = mulai.toString();
                tanggalBerakhir = berakhir.toString();
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate expired = berakhir.toLocalDate();
                sisaHari = java.time.temporal.ChronoUnit.DAYS.between(today, expired);
                if (sisaHari < 0) sisaHari = 0;
            }
        }

        // Label info langganan
        JLabel lblJudul = new JLabel("Status Langganan");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblJudul.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMulai = new JLabel("Tanggal Mulai: " + tanggalMulai);
        JLabel lblBerakhir = new JLabel("Tanggal Berakhir: " + tanggalBerakhir);
        JLabel lblSisa = new JLabel("Sisa Hari: " + sisaHari);

        lblMulai.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBerakhir.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSisa.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tombol perpanjang
        JButton btnPerpanjang = new JButton("Perpanjang 30 Hari");
        btnPerpanjang.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPerpanjang.addActionListener(e -> {
            // Ambil tanggal_berakhir terakhir dari database
            String sqlLast = "SELECT MAX(tanggal_berakhir) AS tanggal_berakhir FROM langganan WHERE status = 'aktif' AND is_expired = 0";
            List<Map<String, Object>> res = executor.executeSelectQuery(sqlLast, new Object[]{});
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate mulaiBaru, berakhirBaru;

            if (!res.isEmpty() && res.get(0).get("tanggal_berakhir") != null) {
                java.sql.Date berakhir = (java.sql.Date) res.get(0).get("tanggal_berakhir");
                java.time.LocalDate lastExpired = berakhir.toLocalDate();
                if (today.isAfter(lastExpired)) {
                    // Sudah expired, mulai dari hari ini
                    mulaiBaru = today;
                    berakhirBaru = today.plusDays(30);
                } else {
                    // Masih aktif, tambah dari tanggal_berakhir terakhir
                    mulaiBaru = lastExpired;
                    berakhirBaru = lastExpired.plusDays(30);
                }
            } else {
                // Belum pernah langganan, mulai dari hari ini
                mulaiBaru = today;
                berakhirBaru = today.plusDays(30);
            }

            // Insert langganan baru
            String insertSql = "INSERT INTO langganan (tanggal_mulai, tanggal_berakhir, status) VALUES (?, ?, 'aktif')";
            executor.executeUpdateQuery(insertSql, new Object[]{
                java.sql.Date.valueOf(mulaiBaru),
                java.sql.Date.valueOf(berakhirBaru)
            });
            JOptionPane.showMessageDialog(this, "Langganan berhasil diperpanjang!");

            // Refresh panel agar status langganan ter-update
            SwingUtilities.invokeLater(() -> {
                removeAll();
                add(new ProfileForm(sessionCache), BorderLayout.CENTER);
                revalidate();
                repaint();
            });
        });

        // Tambahkan ke panel langganan
        langgananPanel.add(Box.createVerticalGlue());
        langgananPanel.add(lblJudul);
        langgananPanel.add(Box.createVerticalStrut(10));
        langgananPanel.add(lblMulai);
        langgananPanel.add(lblBerakhir);
        langgananPanel.add(lblSisa);
        langgananPanel.add(Box.createVerticalStrut(15));
        langgananPanel.add(btnPerpanjang);
        langgananPanel.add(Box.createVerticalGlue());

        // Panel pembungkus agar card di tengah
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalGlue());
        langgananPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(langgananPanel);
        centerPanel.add(Box.createVerticalGlue());

        // Panel utama untuk membagi dua bagian
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,0,0,0);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 1.0;

        // Panel kiri
        c.gridx = 0;
        mainPanel.add(profilePanel, c);

        // Garis vertikal sebagai pemisah
        c.gridx = 1;
        c.weightx = 0;
        JPanel divider = new JPanel();
        divider.setPreferredSize(new Dimension(3, 0));
        divider.setBackground(new Color(120, 120, 120));
        mainPanel.add(divider, c);

        // Panel kanan ganti ke centerPanel (langganan)
        c.gridx = 2;
        c.weightx = 0.5;
        mainPanel.add(centerPanel, c);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void showToast(Component parent, String message) {
        JWindow toast = new JWindow();
        JLabel label = new JLabel(message);
        label.setOpaque(true);
        label.setBackground(new Color(60, 63, 65));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        toast.add(label);
        toast.pack();

        // Posisi di kanan bawah parent
        Point parentLoc = parent.getLocationOnScreen();
        int x = parentLoc.x + parent.getWidth() - toast.getWidth() - 30;
        int y = parentLoc.y + parent.getHeight() - toast.getHeight() - 30;
        toast.setLocation(x, y);

        toast.setVisible(true);

        // Timer untuk auto-close
        new Timer(1500, e -> toast.dispose()).start();
    }
}
