package Pasien;

import Components.CustomCard;
import Components.CustomDialog;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.RoundedButton;
import Components.RoundedPanel;
import DataBase.QueryExecutor;
import Helpers.OnPasienAddedListener;
import Main.Drawer;
import Pasien.EditPasien.OnPasienUpdatedListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class Pasien extends JFrame implements OnPasienAddedListener, OnPasienUpdatedListener {

    private JLabel lblID, lblnik, lblNamaPasien, lblUmur, lblJenisKelamin, lblNoTelp, lblAlamat;
    private DefaultTableModel model;
    private JTextField searchField;
    private JScrollPane tableScrollPane;
    private int role;
    Object[][] data = {};

    // List to keep track of id_pasien for each row
    private List<String> idPasienList = new ArrayList<>();

    // Tambahkan sorter sebagai variabel kelas
    private TableRowSorter<DefaultTableModel> sorter;

    public Pasien(int role) {
        this.role = role;
        setLocationRelativeTo(null);
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_pasien()";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

        int no = 1;
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                var alamat = "".equals(result.get("alamat").toString()) ? "-" : result.get("alamat");
                var no_telp = "".equals(result.get("no_telp").toString()) ? "-" : result.get("no_telp");
                var rfid = result.get("rfid") != null ? result.get("rfid").toString() : "-";
                // Simpan id_pasien asli
                idPasienList.add(String.valueOf(result.get("id_pasien")));
                // Isi tabel: kolom 0 = nomor urut (tanpa id_pasien)
                Object[] dataFromDatabase = new Object[]{
                    no++, // nomor urut
                    result.get("nik"), result.get("nama_pasien"), result.get("umur"),
                    result.get("jenis_kelamin"), alamat, no_telp, rfid
                };
                Object[][] newData = new Object[data.length + 1][];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = dataFromDatabase;
                data = newData;
            }
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel (Title)
        JPanel headerPanel = createHeaderPanel();

        // Search Panel (Search and Add Buttons)
        JPanel searchPanel = createSearchPanel(role);

        // Data Panel (Displays patient details)
        CustomCard detailPanel = createDetailPanel();

        // Table Panel (Displays list of patients)
        tableScrollPane = createTablePanel(role);

        // Main Panel combining Data and Table Panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(detailPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add components to the frame
        add(headerPanel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Refresh layout on resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                getContentPane().revalidate();
                getContentPane().repaint();
            }
        });
    }

    private JScrollPane createTablePanel(int role) {
        String[] columnNames = new String[]{"No", "NIK", "Nama pasien", "Umur", "Jenis Kelamin", "Alamat", "No.Telp", "RFID"};
        model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Inisialisasi sorter untuk model tabel
        sorter = new TableRowSorter<>(model);
        CustomTable pasienTable = new CustomTable(model);
        pasienTable.setRowSorter(sorter);

        // MouseListener untuk menangani klik pada baris tabel
        pasienTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = pasienTable.getSelectedRow();
                if (row != -1) {
                    int modelRow = pasienTable.convertRowIndexToModel(row); // Konversi indeks tampilan ke model
                    lblnik.setText(String.valueOf(model.getValueAt(modelRow, 1)));
                    lblNamaPasien.setText(String.valueOf(model.getValueAt(modelRow, 2)));
                    lblUmur.setText(String.valueOf(model.getValueAt(modelRow, 3)));
                    lblJenisKelamin.setText(String.valueOf(model.getValueAt(modelRow, 4)));
                    lblAlamat.setText(String.valueOf(model.getValueAt(modelRow, 5)));
                    lblNoTelp.setText(String.valueOf(model.getValueAt(modelRow, 6)));
                }
            }
        });
        setTableColumnWidths(pasienTable, role);

        return new JScrollPane(pasienTable);
    }

    private void setTableColumnWidths(JTable table, int role) {
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("PASIEN MANAGEMENT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createSearchPanel(int role) {
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add the "Tambahkan Pasien" button to the right side
        RoundedButton addButton = new RoundedButton("+ Pasien");
        addButton.setBackground(new Color(0, 153, 102));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setPreferredSize(new Dimension(150, 40));
        addButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            RegisterPasien.showModalCenter(
                (JFrame) SwingUtilities.getWindowAncestor(addButton),
                Pasien.this,
                model
            );
        }));

        // Search field with button (left side)
        CustomTextField searchField = new CustomTextField("Cari Pasien", 20, 30, Optional.empty());
        RoundedButton searchButton = new RoundedButton("Cari");
        searchButton.setBackground(new Color(0, 153, 102));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(100, 40));
        // Search functionality
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase();
            if (!searchText.isEmpty()) {
                filterTable(searchText);
            } else {
                resetTable();
            }
        });

        // Panel to hold the search field and button together
        JPanel searchBoxPanel = new JPanel(new BorderLayout(5, 0));
        searchBoxPanel.setBackground(Color.WHITE);
        searchBoxPanel.add(searchField, BorderLayout.CENTER);
        searchBoxPanel.add(searchButton, BorderLayout.EAST);

        // Add components to the search panel
        searchPanel.add(searchBoxPanel, BorderLayout.WEST); // Search panel on left
        if (role != 1) {
            searchPanel.add(addButton, BorderLayout.EAST);      // "Tambahkan Pasien" button on right
        }

        return searchPanel;
    }

    private CustomCard createDetailPanel() {
        JPanel dataPanel = new JPanel(new GridBagLayout());
        dataPanel.setOpaque(false);
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Baris 0: NIK (kiri), Jenis Kelamin (kanan)
        JLabel lblNikLabel = new JLabel("NIK :");
        lblNikLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblnik = new JLabel("-");
        lblnik.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel lblJKLabel = new JLabel("Jenis Kelamin :");
        lblJKLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblJenisKelamin = new JLabel("-");
        lblJenisKelamin.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0; dataPanel.add(lblNikLabel, gbc);
        gbc.gridx = 1; dataPanel.add(lblnik, gbc);
        gbc.gridx = 2; dataPanel.add(lblJKLabel, gbc);
        gbc.gridx = 3; dataPanel.add(lblJenisKelamin, gbc);

        // Baris 1: Nama Pasien (kiri), Alamat (kanan)
        JLabel lblNamaLabel = new JLabel("Nama :");
        lblNamaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblNamaPasien = new JLabel("-");
        lblNamaPasien.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel lblAlamatLabel = new JLabel("Alamat :");
        lblAlamatLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblAlamat = new JLabel("-");
        lblAlamat.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1; dataPanel.add(lblNamaLabel, gbc);
        gbc.gridx = 1; dataPanel.add(lblNamaPasien, gbc);
        gbc.gridx = 2; dataPanel.add(lblAlamatLabel, gbc);
        gbc.gridx = 3; dataPanel.add(lblAlamat, gbc);

        // Baris 3: Umur (kiri), No.Telp (kanan)
        JLabel lblUmurLabel = new JLabel("Umur :");
        lblUmurLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblUmur = new JLabel("-");
        lblUmur.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel lblNoTelpLabel = new JLabel("No.Telp :");
        lblNoTelpLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblNoTelp = new JLabel("-");
        lblNoTelp.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 3; dataPanel.add(lblUmurLabel, gbc);
        gbc.gridx = 1; dataPanel.add(lblUmur, gbc);
        gbc.gridx = 2; dataPanel.add(lblNoTelpLabel, gbc);
        gbc.gridx = 3; dataPanel.add(lblNoTelp, gbc);

        // Baris 4: tombol di kanan bawah (span 4 kolom)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        JButton editButton = new RoundedButton("EDIT");
        editButton.setBackground(new Color(255, 153, 51));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.addActionListener(e -> {
            int row = getSelectedRowInTable();
            if (model.getRowCount() == 0) { // Cek apakah tabel kosong
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk diedit!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (row != -1) {
                String id = idPasienList.get(row); // id_pasien asli
                String nik = String.valueOf(model.getValueAt(row, 1));
                String name = String.valueOf(model.getValueAt(row, 2));
                String age = String.valueOf(model.getValueAt(row, 3));
                String gender = String.valueOf(model.getValueAt(row, 4));
                String address = String.valueOf(model.getValueAt(row, 5));
                String phone = String.valueOf(model.getValueAt(row, 6));
                String rfid = String.valueOf(model.getValueAt(row, 7));
                SwingUtilities.invokeLater(() -> {
                    EditPasien.showModalCenter(
                        (JFrame) SwingUtilities.getWindowAncestor(editButton),
                        id, nik, name, age, gender, phone, address, rfid,
                        Pasien.this
                    );
                });
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih data pasien terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton hapusButton = new RoundedButton("HAPUS");
        hapusButton.setBackground(new Color(255, 51, 51));
        hapusButton.setForeground(Color.WHITE);
        hapusButton.setFocusPainted(false);
        hapusButton.addActionListener(e -> {
            int row = getSelectedRowInTable();
            if (model.getRowCount() == 0) { // Cek apakah tabel kosong
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (row != -1) {
                String id = idPasienList.get(row); // id_pasien asli
                deletePasien(row, id);
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih data pasien terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonGroup.add(editButton);
        buttonGroup.add(hapusButton);
        dataPanel.add(buttonGroup, gbc);

        return new CustomCard("DETAIL PASIEN", dataPanel);
    }

    private JLabel createPlainDetailValueLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font("Arial", Font.PLAIN, 14)); // Set default font
        label.setForeground(Color.BLACK); // Teks dengan warna hitam
        return label;
    }

    // Helper function to get the selected row in the table
    private int getSelectedRowInTable() {
        // Access the table from the JScrollPane's viewport
        CustomTable pasienTable = (CustomTable) tableScrollPane.getViewport().getView();
        return pasienTable.getSelectedRow();
    }

    // Function to filter the table based on the search input
    private void filterTable(String searchText) {
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null); // Tampilkan semua baris
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText)); // Filter berdasarkan teks pencarian (case-insensitive)
        }
    }

    // Function to reset the table to show all rows again
    private void resetTable() {
        sorter.setRowFilter(null); // Reset filter untuk menampilkan semua baris
    }

    // Utility function to add components to GridBagLayout
    private void addComponent(JPanel panel, Component component, GridBagConstraints gbc, int x, int y, int width, int height) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        panel.add(component, gbc);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Pasien(0).setVisible(true));
    }

    @Override
    public void onPasienAdded(String id, String nik, String name, String age, String gender, String phone, String address, String rfid) {
        // Tambahkan data pasien ke tabel dengan RFID
        model.addRow(new Object[]{model.getRowCount() + 1, nik, name, age, gender, address, phone, rfid});
        
        // Tambahkan ID pasien baru ke idPasienList
        idPasienList.add(id);

        // Segarkan tabel
        refreshTable();
    }

    @Override
    public void onPasienUpdated(String nik, String name, String age, String gender, String phone, String address, String rfid) {
        // Segarkan tabel setelah pengeditan
        refreshTable();
    }

    private void deletePasien(int row, String pasienId) {
        CustomDialog confirmDialog = new CustomDialog(null, "Apakah Anda yakin ingin menandai pasien ini sebagai dihapus?", "Konfirmasi Penghapusan");
        int response = confirmDialog.showDialog();
        if (response == JOptionPane.YES_OPTION) {
            String deletedQuery = "UPDATE pasien SET is_deleted = 1 WHERE id_pasien = ?";
            boolean isUpdated = QueryExecutor.executeUpdateQuery(deletedQuery, new Object[]{pasienId});
            if (isUpdated) {
                model.removeRow(row);
                idPasienList.remove(row);
                JOptionPane.showMessageDialog(null, "Pasien berhasil ditandai sebagai dihapus.");
            } else {
                JOptionPane.showMessageDialog(null, "Gagal menandai pasien sebagai dihapus.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Penghapusan dibatalkan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean isTableEmpty() {
        return model.getRowCount() == 0;
    }

    private void refreshTable() {
        // Kosongkan tabel dan idPasienList
        model.setRowCount(0);
        idPasienList.clear();

        // Ambil data terbaru dari database
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_pasien()";
        List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

        int no = 1;
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                var alamat = "".equals(result.get("alamat").toString()) ? "-" : result.get("alamat");
                var no_telp = "".equals(result.get("no_telp").toString()) ? "-" : result.get("no_telp");
                var rfid = result.get("rfid") != null ? result.get("rfid").toString() : "-";

                // Simpan id_pasien asli
                idPasienList.add(String.valueOf(result.get("id_pasien")));

                // Tambahkan data ke tabel
                model.addRow(new Object[]{
                    no++, // nomor urut
                    result.get("nik"), result.get("nama_pasien"), result.get("umur"),
                    result.get("jenis_kelamin"), alamat, no_telp, rfid
                });
            }
        }
    }
}
