package Profile;

import Global.UserSessionCache;
import DataBase.QueryExecutor;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import Components.CustomCard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.util.List;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.security.MessageDigest;

public class ProfileForm extends JPanel {

    java.time.LocalDate mulaiBaru, berakhirBaru;

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
        final String[] namaLengkapArr = {""};
        final String[] emailArr = {""};
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
                namaLengkapArr[0] = row.getOrDefault("nama_lengkap", "").toString();
                emailArr[0] = row.getOrDefault("email", "").toString();
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
        profilePanel.add(new JTextField(namaLengkapArr[0], 15) {{ setEditable(false); }}, gbc);

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

            try {
                String merchantOrderId = "ORDER-" + System.currentTimeMillis();
                int amount = 150000; // nominal langganan
                String customerName = namaLengkapArr[0];
                String customerEmail = emailArr[0]; // ambil dari profile user

                String reference = createDuitkuTransaction(merchantOrderId, amount, customerName, customerEmail);

                // Simpan merchantOrderId untuk keperluan cek status pembayaran
                this.merchantOrderId = merchantOrderId;
                sessionCache.setOrderId(merchantOrderId); // Simpan merchantOrderId ke sessionCache
            } catch (Exception ex) {
                ex.printStackTrace();
                showToast(this, "Gagal membuat transaksi Duitku!");
            }

            // Refresh panel agar status langganan ter-update
            SwingUtilities.invokeLater(() -> {
                removeAll();
                add(new ProfileForm(sessionCache), BorderLayout.CENTER);
                System.out.println(merchantOrderId);
                revalidate();
                repaint();
            });
        });

        // Tombol cek status pembayaran
        JButton btnCekStatus = new JButton("Cek Status Pembayaran");
        btnCekStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCekStatus.addActionListener(e -> {
            String merchantOrderId = sessionCache.getOrderId(); // atau this.merchantOrderId jika tidak refresh panel
            if (merchantOrderId == null || merchantOrderId.isEmpty()) {
                System.out.println(merchantOrderId);
                JOptionPane.showMessageDialog(this, "Order ID tidak ditemukan. Silakan lakukan pembayaran terlebih dahulu.");
                return;
            }
            try {
                String merchantCode = "DS24853"; // Ganti dengan merchant code Duitku Anda
                String apiKey = "605d62af77cf1f18830c961b80cdbe9f"; // Ganti dengan API key Duitku Anda
                
                // Create signature for Duitku
                String signature = generateDuitkuSignature(merchantCode, merchantOrderId, apiKey);
                
                URL url = new URL("https://sandbox.duitku.com/webapi/api/merchant/transactionStatus");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{"
                + "\"merchantCode\":\"" + merchantCode + "\","
                + "\"merchantOrderId\":\"" + merchantOrderId + "\","
                + "\"signature\":\"" + signature + "\""
                + "}";

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes());
                }

                System.out.println(merchantOrderId);

                int responseCode = conn.getResponseCode();
                InputStream is;
                if (responseCode == 200) {
                    is = conn.getInputStream();
                } else {
                    is = conn.getErrorStream();
                }

                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = is.read()) != -1) sb.append((char) ch);
                is.close();

                System.out.println("Duitku status response: " + sb.toString());
                System.out.println("HTTP Response Code: " + responseCode);

                org.json.JSONObject resp = new org.json.JSONObject(sb.toString());
                String statusCode = resp.optString("statusCode", "");
                String statusMessage = resp.optString("statusMessage", "");

                if ("00".equals(statusCode) && "SUCCESS".equals(statusMessage)) {
                    // Insert langganan ke database
                    try {
                        String sqlInsert = "INSERT INTO langganan (order_id, tanggal_mulai, tanggal_berakhir, status) VALUES (?, ?, ?, 'aktif')";
                        
                        // Ambil tanggal langganan baru sebelum insert
                        String sqlLast = "SELECT MAX(tanggal_berakhir) AS tanggal_berakhir FROM langganan WHERE status = 'aktif' AND is_expired = 0";
                        List<Map<String, Object>> res = executor.executeSelectQuery(sqlLast, new Object[]{});
                        java.time.LocalDate today = java.time.LocalDate.now();
                        java.time.LocalDate mulaiBaru, berakhirBaru;
                        boolean masihAktif = false;

                        if (!res.isEmpty() && res.get(0).get("tanggal_berakhir") != null) {
                            java.sql.Date berakhir = (java.sql.Date) res.get(0).get("tanggal_berakhir");
                            java.time.LocalDate lastExpired = berakhir.toLocalDate();
                            if (today.isAfter(lastExpired)) {
                                mulaiBaru = today;
                                berakhirBaru = today.plusDays(30);
                                masihAktif = true;
                            } else {
                                mulaiBaru = lastExpired;
                                berakhirBaru = lastExpired.plusDays(30);
                            }
                        } else {
                            mulaiBaru = today;
                            berakhirBaru = today.plusDays(30);
                        }

                        if (masihAktif) {
                            JOptionPane.showMessageDialog(this, "Langganan Anda masih aktif. Silakan perpanjang setelah masa aktif habis.");
                            return;
                        }

                        // Cek apakah merchantOrderId sudah pernah dipakai
                        String sqlCheckOrder = "SELECT COUNT(*) AS total FROM langganan WHERE order_id = ?";
                        List<Map<String, Object>> resOrder = executor.executeSelectQuery(sqlCheckOrder, new Object[]{merchantOrderId});
                        int total = 0;
                        if (!resOrder.isEmpty()) {
                            total = ((Number) resOrder.get(0).get("total")).intValue();
                        }
                        if (total > 0) {
                            JOptionPane.showMessageDialog(this, "Transaksi ini sudah pernah digunakan untuk perpanjang langganan.");
                            return;
                        }

                        // Insert langganan
                        executor.executeUpdateQuery(sqlInsert, new Object[]{
                            merchantOrderId,
                            java.sql.Date.valueOf(mulaiBaru),
                            java.sql.Date.valueOf(berakhirBaru)
                        });
                        JOptionPane.showMessageDialog(this, "Pembayaran sukses! Langganan aktif.");
                        SwingUtilities.invokeLater(() -> {
                            removeAll();
                            add(new ProfileForm(sessionCache), BorderLayout.CENTER);
                            revalidate();
                            repaint();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Gagal insert langganan: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Pembayaran belum selesai atau gagal.\nStatus Code: " + statusCode + "\nStatus Message: " + statusMessage);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal cek status pembayaran!");
            }
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
        langgananPanel.add(btnCekStatus);
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

    // Helper method to generate MD5 hash for Duitku signature
    private String generateMD5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Generate signature for Duitku transaction status
    private String generateDuitkuSignature(String merchantCode, String merchantOrderId, String apiKey) throws Exception {
        String signatureString = merchantCode + merchantOrderId + apiKey;
        return generateMD5(signatureString);
    }

    // Generate signature for Duitku create transaction
    private String generateCreateTransactionSignature(String merchantCode, String merchantOrderId, int paymentAmount, String apiKey) throws Exception {
        String signatureString = merchantCode + merchantOrderId + paymentAmount + apiKey;
        return generateMD5(signatureString);
    }

    private String createDuitkuTransaction(String merchantOrderId, int amount, String customerName, String customerEmail) throws Exception {
        String merchantCode = "DS24853"; 
        String apiKey = "605d62af77cf1f18830c961b80cdbe9f"; 
        String paymentMethod = "A1"; 
        
        // Generate signature
        String signature = generateCreateTransactionSignature(merchantCode, merchantOrderId, amount, apiKey);
        
        URL url = new URL("https://sandbox.duitku.com/webapi/api/merchant/v2/inquiry");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = "{"
        + "\"merchantCode\":\"" + merchantCode + "\","
        + "\"paymentAmount\":" + amount + ","
        + "\"paymentMethod\":\"" + paymentMethod + "\","
        + "\"merchantOrderId\":\"" + merchantOrderId + "\","
        + "\"productDetails\":\"Perpanjang Langganan 30 Hari\","
        + "\"customerName\":\"" + customerName + "\","
        + "\"email\":\"" + customerEmail + "\","
        + "\"callbackUrl\":\"https://your-domain.com/callback\","
        + "\"returnUrl\":\"https://your-domain.com/return\","
        + "\"signature\":\"" + signature + "\""
        + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        InputStream is = conn.getInputStream();
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = is.read()) != -1) sb.append((char) ch);
        is.close();

        System.out.println("Duitku response: " + sb.toString());
        System.out.println("HTTP Response Code: " + conn.getResponseCode());

        // Parse response JSON
        org.json.JSONObject resp = new org.json.JSONObject(sb.toString());
        String statusCode = resp.optString("statusCode", "");
        
        if ("00".equals(statusCode)) {
            String reference = resp.optString("reference", "");
            String vaNumber = resp.optString("vaNumber", "");
            String paymentMethodDuitku = resp.optString("paymentMethod", "");
            String amountDuitku = resp.optString("amount", "");
            
            // Show virtual account details to user
            String message = "Pembayaran Virtual Account berhasil dibuat!\n\n" +
                           "Metode Pembayaran: " + paymentMethod + "\n" +
                           "Nomor Virtual Account: " + vaNumber + "\n" +
                           "Jumlah Transfer: Rp " + String.format("%,d", amount) + "\n" +
                           "Reference: " + reference + "\n\n" +
                           "Silakan transfer ke nomor VA di atas,\n" +
                           "lalu klik 'Cek Status Pembayaran' untuk konfirmasi.";
            
            JOptionPane.showMessageDialog(this, message, "Informasi Pembayaran", JOptionPane.INFORMATION_MESSAGE);
            
            // Copy VA number to clipboard for user convenience
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(vaNumber), null);
            
            showToast(this, "Nomor VA telah disalin ke clipboard!");
            
            return reference;
        } else {
            throw new Exception("Failed to create Duitku transaction: " + resp.optString("statusMessage", "Unknown error"));
        }
    }

    public String merchantOrderId; // Simpan merchantOrderId di sini
}