package User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Map;
import java.util.Optional;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import Components.CustomCard;
import Components.CustomDialog;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.RoundedButton;
import Components.RoundedPanel;
import DataBase.QueryExecutor;

public class User extends JFrame {

    private JLabel lblID, lblNamaUser, lblJenisKelamin, lblAlamat, lblNoTelp;
    private DefaultTableModel model;
    private CustomTable userTable;
    private QueryExecutor executor;
    Object[][] data = {};

    public User() {
        executor = new QueryExecutor();

        // Inisialisasi model lebih awal
        model = new DefaultTableModel(
            new Object[0][0],
            new String[]{"No", "ID", "Roles", "Nama User", "Jenis Kelamin", "Alamat", "No.Telp"}
        );

        String queryPengeluaran = "CALL all_user";
        java.util.List<Map<String, Object>> resultPengeluaran = executor.executeSelectQuery(queryPengeluaran, new Object[]{});

        for (int i = 0; i < resultPengeluaran.size(); i++) {
            Object[] dataFromDatabase = new Object[]{
                i + 1,
                resultPengeluaran.get(i).get("id_user"),
                resultPengeluaran.get(i).get("highest_role"),
                resultPengeluaran.get(i).get("username"),
                resultPengeluaran.get(i).get("jenis_kelamin"),
                resultPengeluaran.get(i).get("alamat"),
                resultPengeluaran.get(i).get("no_telp")
            };
            Object[][] newData = new Object[data.length + 1][];
            System.arraycopy(data, 0, newData, 0, data.length);
            newData[data.length] = dataFromDatabase;
            data = newData;
        }
        model.setDataVector(data, new String[]{"No", "ID", "Roles", "Nama User", "Jenis Kelamin", "Alamat", "No.Telp"});

        setTitle("User Management");
        setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Header Panel (Title)
        JPanel headerPanel = createHeaderPanel();

        // Search Panel (Search and Add Buttons)
        JPanel searchPanel = createSearchPanel();

        // Data Panel (Displays user details)
        CustomCard detailPanel = createDetailPanel();

        // Table Panel (Displays list of users)
        JScrollPane tableScrollPane = createTablePanel();

        // Main Panel combining Data and Table Panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(detailPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        // Add components to the frame
        add(topPanel, BorderLayout.NORTH); // Hanya satu kali add ke NORTH
        add(mainPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("USER MANAGEMENT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add the "Tambahkan User" button to the right side
        RoundedButton addButton = new RoundedButton("Tambahkan User");
        addButton.setBackground(new Color(0, 153, 102));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setPreferredSize(new Dimension(150, 40));
        addButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            RegisterUser.showModalCenter(
                (JFrame) SwingUtilities.getWindowAncestor(addButton),
                (id, role, name, gender, address, phone) -> {
                    model.addRow(new Object[]{model.getRowCount() + 1, id, role, name, gender, address, phone});
                },
                model
            );
        }));

        // Search field with button (left side)
        CustomTextField searchField = new CustomTextField("Cari User", 20, 30, Optional.empty());
        RoundedButton searchButton = new RoundedButton("Cari");
        searchButton.setBackground(new Color(0, 153, 102));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(100, 40));

        // Panel to hold the search field and button together
        JPanel searchBoxPanel = new JPanel(new BorderLayout(5, 0));
        searchBoxPanel.setBackground(Color.WHITE);
        searchBoxPanel.add(searchField, BorderLayout.CENTER);
        searchBoxPanel.add(searchButton, BorderLayout.EAST);

        // Add components to the search panel
        searchPanel.add(searchBoxPanel, BorderLayout.WEST); // Search panel on left
        searchPanel.add(addButton, BorderLayout.EAST);      // "Tambahkan User" button on right

        // Search functionality 
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase();
            if (!searchText.isEmpty()) {
                filterTable(searchText);
            } else {
                resetTable();
            }
        });

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

        // Baris 0: ID (kiri), Jenis Kelamin (kanan)
        JLabel lblIDLabel = new JLabel("ID :");
        lblIDLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblID = new JLabel("-");
        lblID.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel lblJKLabel = new JLabel("Jenis Kelamin :");
        lblJKLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblJenisKelamin = new JLabel("-");
        lblJenisKelamin.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0; dataPanel.add(lblIDLabel, gbc);
        gbc.gridx = 1; dataPanel.add(lblID, gbc);
        gbc.gridx = 2; dataPanel.add(lblJKLabel, gbc);
        gbc.gridx = 3; dataPanel.add(lblJenisKelamin, gbc);

        // Baris 1: Nama (kiri), Alamat (kanan)
        JLabel lblNamaLabel = new JLabel("Nama :");
        lblNamaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblNamaUser = new JLabel("-");
        lblNamaUser.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel lblAlamatLabel = new JLabel("Alamat :");
        lblAlamatLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblAlamat = new JLabel("-");
        lblAlamat.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1; dataPanel.add(lblNamaLabel, gbc);
        gbc.gridx = 1; dataPanel.add(lblNamaUser, gbc);
        gbc.gridx = 2; dataPanel.add(lblAlamatLabel, gbc);
        gbc.gridx = 3; dataPanel.add(lblAlamat, gbc);

        // Baris 2: No.Telp (kiri)
        JLabel lblNoTelpLabel = new JLabel("No.Telp :");
        lblNoTelpLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblNoTelp = new JLabel("-");
        lblNoTelp.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 2; dataPanel.add(lblNoTelpLabel, gbc);
        gbc.gridx = 1; dataPanel.add(lblNoTelp, gbc);

        // Baris 3: tombol di kanan bawah (span 4 kolom)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        JButton editButton = new RoundedButton("EDIT");
        editButton.setBackground(new Color(255, 153, 51));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.addActionListener(e -> {
            if (model.getRowCount() == 0) { // Cek apakah tabel kosong
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk diedit!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = userTable.getSelectedRow();
            if (row != -1) {
                // Lanjutkan dengan logika EDIT
                String id = (String) model.getValueAt(row, 1);
                String name = (String) model.getValueAt(row, 3);
                String role = (String) model.getValueAt(row, 2);
                String gender = (String) model.getValueAt(row, 4);
                String address = (String) model.getValueAt(row, 5);
                String phone = (String) model.getValueAt(row, 6);
                SwingUtilities.invokeLater(() -> {
                    EditUser.showModalCenter(
                        (JFrame) SwingUtilities.getWindowAncestor(userTable),
                        id,
                        (updatedName, updatedRole, updatedGender, updatedPhone, updatedAddress, updatedRFID) -> {
                            model.setValueAt(updatedName, row, 3);
                            model.setValueAt(updatedRole, row, 2);
                            model.setValueAt(updatedGender, row, 4);
                            model.setValueAt(updatedAddress, row, 5);
                            model.setValueAt(updatedPhone, row, 6);
                        }
                    );
                });
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih user terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton hapusButton = new RoundedButton("HAPUS");
        hapusButton.setBackground(new Color(255, 51, 51));
        hapusButton.setForeground(Color.WHITE);
        hapusButton.setFocusPainted(false);
        hapusButton.addActionListener(e -> {
            if (model.getRowCount() == 0) { // Cek apakah tabel kosong
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = userTable.getSelectedRow();
            if (row != -1) {
                String id = (String) model.getValueAt(row, 1);
                CustomDialog confirmDialog = new CustomDialog(null, "Apakah Anda yakin ingin menghapus user ini?", "Konfirmasi Penghapusan");
                int response = confirmDialog.showDialog();
                if (response == JOptionPane.YES_OPTION) {
                    String deletedQuery = "UPDATE user SET user.delete = ? WHERE id_user = ?";
                    QueryExecutor.executeUpdateQuery(deletedQuery, new Object[]{1, id});
                    JOptionPane.showMessageDialog(this, "User berhasil dihapus.");
                    resetTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih user terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonGroup.add(editButton);
        buttonGroup.add(hapusButton);
        dataPanel.add(buttonGroup, gbc);

        return new CustomCard("DETAIL USER", dataPanel);
    }

    private JLabel createPlainDetailValueLabel(String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14)); // Set default font
        label.setForeground(Color.BLACK); // Teks dengan warna hitam
        return label;
    }

    private JLabel createDetailValueLabel(Font font) {
        JLabel label = new JLabel();
        label.setFont(font);
        label.setOpaque(true);
        label.setBackground(new Color(245, 245, 245));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return label;
    }

    private JLabel createDetailLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return label;
    }

    private JScrollPane createTablePanel() {
        // Table data and columns setup
        String[] columns = {"No", "ID", "Roles", "Nama User", "Jenis Kelamin", "Alamat", "No.Telp"};

        userTable = new CustomTable(model);

        // Listener untuk update detail panel saat baris dipilih
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow != -1) {
                    String id = (String) model.getValueAt(selectedRow, 1);
                    String role = (String) model.getValueAt(selectedRow, 2);
                    String name = (String) model.getValueAt(selectedRow, 3);
                    String gender = (String) model.getValueAt(selectedRow, 4);
                    String address = (String) model.getValueAt(selectedRow, 5);
                    String phone = (String) model.getValueAt(selectedRow, 6);
                    updateDetailPanel(id, role, name, gender, address, phone);
                } else {
                    resetDetailPanel(); // Reset detail panel jika tidak ada baris yang dipilih
                }
            }
        });

        setTableColumnWidths(userTable);

        return new JScrollPane(userTable);
    }

    // Fungsi untuk mengupdate Panel Detail Data User
    private void updateDetailPanel(String id, String role, String name, String gender, String address, String phone) {
        lblID.setText("ID: " + id);
        lblNamaUser.setText("Nama: " + name);
        lblJenisKelamin.setText("Jenis Kelamin: " + gender);
        lblAlamat.setText("Alamat: " + address);
        lblNoTelp.setText("No Telp: " + phone);
    }

    private void setTableColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(30);  // No column
        table.getColumnModel().getColumn(1).setPreferredWidth(50);  // ID column
        table.getColumnModel().getColumn(2).setPreferredWidth(70);  // Roles column
        table.getColumnModel().getColumn(3).setPreferredWidth(150);  // Nama User column
        table.getColumnModel().getColumn(4).setPreferredWidth(150);  // Jenis Kelamin column
        table.getColumnModel().getColumn(5).setPreferredWidth(250);  // Alamat column
        table.getColumnModel().getColumn(6).setPreferredWidth(200);  // No.Telp column
    }

    private void filterTable(String searchText) {
        // Implement filter functionality if needed
    }

    private void resetTable() {
        data = new Object[0][];
        // Implement reset functionality if needed
        String queryPengeluaran = "CALL all_user()";
        java.util.List<Map<String, Object>> resultPengeluaran = executor.executeSelectQuery(queryPengeluaran, new Object[]{});

        for (int i = 0; i < resultPengeluaran.size(); i++) {
            Object[] dataFromDatabase = new Object[]{
                i + 1, resultPengeluaran.get(i).get("id_user"), resultPengeluaran.get(i).get("highest_role"), resultPengeluaran.get(i).get("username"),
                resultPengeluaran.get(i).get("jenis_kelamin"), resultPengeluaran.get(i).get("alamat"), resultPengeluaran.get(i).get("no_telp")
            };
            // Create a new array with an additional row
            Object[][] newData = new Object[data.length + 1][];

            // Copy the old data to the new array
            System.arraycopy(data, 0, newData, 0, data.length);

            // Add the new row to the new array
            newData[data.length] = dataFromDatabase;

            // Send back to original
            data = newData;
        }
        // Update the table model with the refreshed data
        model.setDataVector(data, new String[]{"No", "ID", "Roles", "Nama User", "Jenis Kelamin", "Alamat", "No.Telp"});
        setTableColumnWidths(userTable);
        resetDetailPanel(); // Tambahkan ini
    }

    public void onUserAdded(String id, String role, String name, String gender, String address, String phone) {
        int newRowNumber = model.getRowCount() + 1;
        model.addRow(new Object[]{newRowNumber, id, role, name, gender, address, phone});
    }


    private void resetDetailPanel() {
        lblID.setText("ID: -");
        lblNamaUser.setText("Nama: -");
        lblJenisKelamin.setText("Jenis Kelamin: -");
        lblAlamat.setText("Alamat: -");
        lblNoTelp.setText("No Telp: -");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new User().setVisible(true));
    }
}
