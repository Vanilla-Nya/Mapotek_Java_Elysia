package Antrian;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.BorderLayout;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import Components.RoundedPanel;
import Components.ShowModalCenter;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Utils.TableUtils;

public class TambahkanAntrian extends JPanel {

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

    private JPanel cardsPanel = new JPanel();
    private Map<String, Object> selectedPasien = null;

    public TambahkanAntrian(DefaultTableModel model, AntrianPasien antrianPasien) {
        this.model = model;
        QueryExecutor namapasien = new QueryExecutor();
        namaPasienDropdown = new CustomTextField("Masukkan NIK Atau Nama", 0, 0, Optional.empty());

        // Panel with rounded corners
        RoundedPanel mainPanel = new RoundedPanel(15, Color.WHITE);
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding around the panel
        mainPanel.setPreferredSize(new Dimension(800, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH; // Tambahkan ini agar semua komponen ke atas

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
                ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(TambahkanAntrian.this));
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

                // Close the TambahkanAntrian window
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

        // Tambahkan komponen "pengisi" agar konten tetap di atas
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1; // Komponen pengisi
        mainPanel.add(Box.createVerticalGlue(), gbc);

        // Add the mainPanel to this panel
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(cardsPanel, gbc);
    }

    // Getter methods to access the text from the text fields
    public void searchDatabase(String keyword) {
        QueryExecutor executor = new QueryExecutor();
        String Query = """
        SELECT id_pasien, nik, nama AS nama_pasien,
            CONCAT(
                TIMESTAMPDIFF(YEAR, pasien.tanggal_lahir, CURDATE()), ' Tahun ',
                TIMESTAMPDIFF(MONTH, pasien.tanggal_lahir, CURDATE()) % 12, ' Bulan ',
                DATEDIFF(CURDATE(), DATE_ADD(pasien.tanggal_lahir, INTERVAL TIMESTAMPDIFF(YEAR, pasien.tanggal_lahir, CURDATE()) YEAR)) % 30, ' Hari'
            ) AS umur,
            jenis_kelamin, alamat, no_telepon AS no_telp
        FROM pasien
        WHERE nik = ? OR nama LIKE ? OR rfid = ?
        LIMIT 10
    """;
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(Query, new Object[]{
            keyword, "%" + keyword + "%", keyword
        });

        cardsPanel.removeAll();
        if (!results.isEmpty()) {
            for (Map<String, Object> data : results) {
                JPanel card = new JPanel();
                card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.add(new JLabel("ID: " + data.get("id_pasien")));
                card.add(new JLabel("Nama: " + data.get("nama_pasien")));
                card.add(new JLabel("Umur: " + data.get("umur")));
                card.add(new JLabel("Alamat: " + data.get("alamat")));
                card.add(new JLabel("No. Telp: " + data.get("no_telp")));

                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        selectedPasien = data;
                        idPasien = (int) data.get("id_pasien");
                        // Highlight card terpilih (opsional)
                        for (java.awt.Component comp : cardsPanel.getComponents()) {
                            comp.setBackground(Color.WHITE);
                        }
                        card.setBackground(new Color(200, 230, 255));
                    }
                });
                cardsPanel.add(card);
            }
        } else {
            cardsPanel.add(new JLabel("Tidak ada pasien ditemukan."));
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
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
