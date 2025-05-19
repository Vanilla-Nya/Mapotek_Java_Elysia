package Profile;

import Global.UserSessionCache;
import DataBase.QueryExecutor;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ProfileForm extends JPanel {
    public ProfileForm(UserSessionCache sessionCache) {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

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
        add(titleLabel, gbc);

        // Nama Lengkap
        gbc.gridwidth = 1; gbc.gridy++;
        add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        add(new JTextField(namaLengkap, 15) {{ setEditable(false); }}, gbc);

        // Jenis Kelamin
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1;
        add(new JTextField(jenisKelamin, 15) {{ setEditable(false); }}, gbc);

        // Alamat
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Alamat:"), gbc);
        gbc.gridx = 1;
        add(new JTextField(alamat, 15) {{ setEditable(false); }}, gbc);

        // No Telp
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("No Telp:"), gbc);
        gbc.gridx = 1;
        add(new JTextField(noTelp, 15) {{ setEditable(false); }}, gbc);
    }

    // Untuk testing standalone
    public static void main(String[] args) {
        // Simulasi login
        UserSessionCache sessionCache = new UserSessionCache();
        sessionCache.login("luna", "123-uuid");

        JFrame frame = new JFrame("Profile Form");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new ProfileForm(sessionCache));
        frame.setSize(400, 250);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
