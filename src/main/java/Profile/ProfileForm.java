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

        String uuid = sessionCache.getUUID();
        String username = sessionCache.getusername();

        // Ambil data profile dari prosedur
        String namaLengkap = "";
        String jenisKelamin = "";
        String alamat = "";
        String noTelp = "";

        if (uuid != null) {
            QueryExecutor executor = new QueryExecutor();
            String sql = "CALL all_profile_user(?)";
            Object[] params = new Object[]{uuid};
            List<Map<String, Object>> results = executor.executeSelectQuery(sql, params);
            if (!results.isEmpty()) {
                Map<String, Object> row = results.get(0);
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

        // Panel kanan: Donate Me (hanya card rekening)
        JPanel donatePanel = new JPanel();
        donatePanel.setBackground(new Color(245, 245, 245));
        donatePanel.setLayout(new BoxLayout(donatePanel, BoxLayout.Y_AXIS));

        // Panel daftar rekening bank (akan dimasukkan ke dalam CustomCard)
        JPanel rekeningListPanel = new JPanel();
        rekeningListPanel.setOpaque(false);
        rekeningListPanel.setLayout(new BoxLayout(rekeningListPanel, BoxLayout.Y_AXIS));

        String[] norekList = {
            "1200873200 - Alfon Soetanto (BCA)",
            "90110351606 - Alfon Soetanto (Jenius)",
            "0312559285 - Alvin (Bank Jatim)",
            "1430033218254 - Alvin (Mandiri)"
        };

        for (String norek : norekList) {
            JPanel norekPanel = new JPanel();
            norekPanel.setOpaque(false);
            norekPanel.setLayout(new BoxLayout(norekPanel, BoxLayout.X_AXIS));
            JLabel norekLabel = new JLabel(norek);
            norekPanel.add(norekLabel);

            JButton copyButton = new JButton("Copy");
            copyButton.setFocusable(false);
            copyButton.setMargin(new Insets(1, 4, 1, 4));
            copyButton.setFont(copyButton.getFont().deriveFont(11f));
            copyButton.addActionListener(e -> {
                StringSelection selection = new StringSelection(norek);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                showToast(donatePanel, "Nomor rekening berhasil disalin!");
            });
            norekPanel.add(Box.createHorizontalStrut(6));
            norekPanel.add(copyButton);

            norekPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            norekPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // padding 0
            rekeningListPanel.add(norekPanel);
        }

        // Bungkus daftar rekening dengan CustomCard
        CustomCard rekeningCard = new CustomCard("Rekening Bank", rekeningListPanel);

        // Panel pembungkus agar card di tengah
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalGlue());
        rekeningCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(rekeningCard);
        centerPanel.add(Box.createVerticalGlue());

        // Tambahkan hanya card rekening ke donatePanel
        donatePanel.add(centerPanel);

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

        // Panel kanan
        c.gridx = 2;
        c.weightx = 0.5;
        mainPanel.add(donatePanel, c);

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
