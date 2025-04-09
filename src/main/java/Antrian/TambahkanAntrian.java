package Antrian;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import Components.RoundedPanel;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Utils.TableUtils;

public class TambahkanAntrian extends JFrame {

    private Dropdown idPasienDropdown;
    private CustomTextField namaPasienDropdown;

    // Labels for displaying patient data (the "card")
    private JLabel idPasienCardLabel;
    private JLabel namaPasienCardLabel;
    private JLabel umurCardLabel;
    private JLabel jenisKelaminCardLabel;
    private JLabel alamatCardLabel;
    private JLabel noTelpCardLabel;
    private String namaPasien;
    private Integer idPasien;
    Object[][] data = new Object[][]{};
    java.util.List namaPasienOption = new ArrayList<>();
    private DefaultTableModel model;
    UserSessionCache cache = new UserSessionCache();
    String uuid = cache.getUUID();

    public TambahkanAntrian(DefaultTableModel model, AntrianPasien antrianPasien) {
        this.model = model;
        QueryExecutor namapasien = new QueryExecutor();
        namaPasienDropdown = new CustomTextField("Masukkan NIK Atau Nama", 0, 0, Optional.empty());

        setTitle("Tambahkan Antrian");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Panel with rounded corners
        RoundedPanel mainPanel = new RoundedPanel(15, Color.WHITE);
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding around the panel

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nama Pasien Dropdown
        JLabel namaPasienLabel = new JLabel("NIK / Nama / KTP :");
        namaPasienLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(namaPasienLabel, gbc);

        // Track the previous selection to avoid duplicate prints
        final String[] previousSelection = {null};

        // Add KeyListener to detect Enter key press and trigger search
        namaPasienDropdown.getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    // Perform search when Enter is pressed
                    String text = namaPasienDropdown.getText();
                    searchDatabase(text);
                }
            }
        });

        // Add KeyListener to detect RFID input
        namaPasienDropdown.getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Assuming RFID input is 10 characters long
                if (namaPasienDropdown.getText().length() == 16) {
                    String rfid = namaPasienDropdown.getText();
                }
            }
        });

        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(namaPasienDropdown, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Transparent to see the rounded panel background

        RoundedButton kembaliButton = new RoundedButton("Kembali");
        kembaliButton.setBackground(new Color(51, 102, 255)); // Blue background
        kembaliButton.setForeground(Color.WHITE); // White text

        RoundedButton simpanButton = new RoundedButton("Simpan");
        simpanButton.setBackground(new Color(0, 153, 0)); // Green background
        simpanButton.setForeground(Color.WHITE); // White text

        // Back button action
        kembaliButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the frame
            }
        });

        // Action for the save button
        simpanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (namaPasienDropdown.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Harus memasukkan data NIK/Nama terlebih dahulu/Scan RFID KTP!",
                            "Peringatan",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if (idPasien == null) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Data pasien tidak ditemukan. Mohon masukkan NIK/Nama yang valid.",
                            "Peringatan",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                String tanggalSekarang = LocalDate.now().toString();
                String jamSekarang = LocalTime.now().toString().split("\\.")[0];
                String jenisPasien = "UMUM";
                String Status = "Belum Periksa";

                QueryExecutor executor = new QueryExecutor();
                String query = "CALL tambah_antrian(?, ?, ?, ?, ?, ?)";
                Object[] parameter = new Object[]{idPasien, jenisPasien, tanggalSekarang, jamSekarang, Status, uuid};
                java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);

                if (!results.isEmpty()) {
                    // Refresh the table in AntrianPasien
                    antrianPasien.refreshTableData();
                }

                dispose(); // Close the TambahkanAntrian window
            }
        });

        buttonPanel.add(kembaliButton);
        buttonPanel.add(simpanButton);

        // Add buttons to main panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);

        // Create a panel to display the patient card
        JPanel patientCardPanel = new JPanel();
        patientCardPanel.setLayout(new BoxLayout(patientCardPanel, BoxLayout.Y_AXIS));
        patientCardPanel.setBorder(BorderFactory.createTitledBorder("Data Pasien"));
        patientCardPanel.setPreferredSize(new Dimension(350, 200));

        // Create labels for the card data
        idPasienCardLabel = new JLabel("ID Pasien: ");
        namaPasienCardLabel = new JLabel("Nama Pasien: ");
        umurCardLabel = new JLabel("Umur: ");
        jenisKelaminCardLabel = new JLabel("Jenis Kelamin: ");
        alamatCardLabel = new JLabel("Alamat: ");
        noTelpCardLabel = new JLabel("No. Telepon: ");

        // Add the labels to the patient card panel
        patientCardPanel.add(idPasienCardLabel);
        patientCardPanel.add(namaPasienCardLabel);
        patientCardPanel.add(umurCardLabel);
        patientCardPanel.add(jenisKelaminCardLabel);
        patientCardPanel.add(alamatCardLabel);
        patientCardPanel.add(noTelpCardLabel);

        // Add the patient card panel to the main panel
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(patientCardPanel, gbc);

        // Add main panel to frame
        add(mainPanel);
        setVisible(true);
    }

    // Getter methods to access the text from the text fields
    public void searchDatabase(String nik) {
        QueryExecutor executor = new QueryExecutor();
        String Query = """
                       SELECT id_pasien,
                           nik,
                           nama AS nama_pasien,
                           CONCAT(
                               TIMESTAMPDIFF(YEAR, pasien.tanggal_lahir, CURDATE()), ' Tahun ',
                               TIMESTAMPDIFF(MONTH, pasien.tanggal_lahir, CURDATE()) % 12, ' Bulan ',
                               DATEDIFF(CURDATE(), DATE_ADD(pasien.tanggal_lahir, INTERVAL TIMESTAMPDIFF(YEAR, pasien.tanggal_lahir, CURDATE()) YEAR)) % 30, ' Hari'
                           ) AS umur,
                           jenis_kelamin,
                           alamat,
                           no_telepon AS no_telp FROM pasien WHERE nik = ? OR nama = ? OR rfid = ? LIMIT 1""";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(Query, new Object[]{nik, nik, nik});

        System.out.println(results);
        if (!results.isEmpty()) {
            Map<String, Object> data = results.get(0);
            idPasien = (int) data.get("id_pasien");
            namaPasien = (String) data.get("nama_pasien");
            idPasienCardLabel.setText("ID Pasien: " + data.get("id_pasien"));
            namaPasienCardLabel.setText("Nama Pasien: " + data.get("nama_pasien"));
            umurCardLabel.setText("Umur: " + data.get("umur"));
            jenisKelaminCardLabel.setText("Jenis Kelamin: " + data.get("jenis_kelamin"));
            alamatCardLabel.setText("Alamat: " + data.get("alamat"));
            noTelpCardLabel.setText("No. Telepon: " + data.get("no_telp"));
        } else {
            idPasienCardLabel.setText("Maaf, Pasien Tidak Ditemukan");
            namaPasienCardLabel.setText("");
            umurCardLabel.setText("");
            jenisKelaminCardLabel.setText("");
            alamatCardLabel.setText("");
            noTelpCardLabel.setText("");
        }
    }

    public void refreshTableData() {
        String query = "CALL all_antrian(?)";
        Object[] parameters = new Object[]{uuid};
        String[] columnNames = {"tanggal_antrian", "no_antrian", "nama_pasien", "status_antrian", "aksi"};

        // Refresh the table data
        TableUtils.refreshTable(model, query, parameters, columnNames);
    }

    // Getter methods to access the text from the text fields
    public String getIdPasienText() {
        return (String) idPasienDropdown.getSelectedItem();
    }

//    public String getNamaPasienText() {
//        return (String) namaPasienDropdown.getSelectedItem();
//    }
}
