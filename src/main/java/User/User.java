package User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
        QueryExecutor executor = new QueryExecutor();
        String queryPengeluaran = "CALL all_user";
        java.util.List<Map<String, Object>> resultPengeluaran = executor.executeSelectQuery(queryPengeluaran, new Object[]{});

        for (int i = 0; i < resultPengeluaran.size(); i++) {
            Object[] dataFromDatabase = new Object[]{
                i + 1, resultPengeluaran.get(i).get("id_user"), resultPengeluaran.get(i).get("highest_role"), resultPengeluaran.get(i).get("username"),
                resultPengeluaran.get(i).get("jenis_kelamin"), resultPengeluaran.get(i).get("alamat"), resultPengeluaran.get(i).get("no_telp"), ""
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

        setTitle("User Management");
        setSize(800, 600);
        setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Header Panel (Title)
        JPanel headerPanel = createHeaderPanel();

        // Search Panel (Search and Add Buttons)
        JPanel searchPanel = createSearchPanel();

        // Data Panel (Displays user details)
        RoundedPanel detailPanel = createDetailPanel();

        // Table Panel (Displays list of users)
        JScrollPane tableScrollPane = createTablePanel();

        // Main Panel combining Data and Table Panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(detailPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add components to the frame
        add(headerPanel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
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
                    model.addRow(new Object[]{model.getRowCount() + 1, id, role, name, gender, address, phone, ""});
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

    private RoundedPanel createDetailPanel() {
        // Panel detail data user dengan RoundedPanel
        RoundedPanel detailPanel = new RoundedPanel(15, Color.WHITE);
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create two sub-panels (left and right)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(3, 1, 5, 10));  // 3 rows, 2 columns for labels and values
        leftPanel.setBackground(Color.WHITE);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(3, 1, 5, 10));  // 3 rows, 2 columns for labels and values
        rightPanel.setBackground(Color.WHITE);

        // Labels and values for left panel
        rightPanel.add(lblJenisKelamin = createPlainDetailValueLabel("Jenis Kelamin:"));
        rightPanel.add(lblAlamat = createPlainDetailValueLabel("Alamat:"));
        rightPanel.add(lblNoTelp = createPlainDetailValueLabel("No Telp:"));

        // Labels and values for right panel
        leftPanel.add(lblID = createPlainDetailValueLabel("ID:"));
        leftPanel.add(lblNamaUser = createPlainDetailValueLabel("Nama:"));

        // Add both panels to the main detailPanel
        detailPanel.add(leftPanel, BorderLayout.WEST);
        detailPanel.add(rightPanel, BorderLayout.EAST);

        return detailPanel;
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
        String[] columns = {"No", "ID", "Roles", "Nama User", "Jenis Kelamin", "Alamat", "No.Telp", "Aksi"};

        // Table model
        model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only "Aksi" column is editable
            }
        };

        userTable = new CustomTable(model);
        userTable.getColumnModel().getColumn(7).setCellRenderer(new ActionCellRenderer());
        userTable.getColumnModel().getColumn(7).setCellEditor(new ActionCellEditor());

        // Add listener to detect row selection and update user detail panel
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

                    // Update the details panel with selected user's data
                    updateDetailPanel(id, role, name, gender, address, phone);
                }
            }
        });

        // Adjust table column widths
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
        table.getColumnModel().getColumn(7).setPreferredWidth(200);  // Aksi column
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
                resultPengeluaran.get(i).get("jenis_kelamin"), resultPengeluaran.get(i).get("alamat"), resultPengeluaran.get(i).get("no_telp"), ""
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
        model.setDataVector(data, new String[]{"No", "ID", "Roles", "Nama User", "Jenis Kelamin", "Alamat", "No.Telp", "Aksi"});

        // Reapply the button rendering and editing to the "AKSI" column
        userTable.getColumnModel().getColumn(7).setCellRenderer(new ActionCellRenderer());
        userTable.getColumnModel().getColumn(7).setCellEditor(new ActionCellEditor());
        setTableColumnWidths(userTable);
    }

    public void onUserAdded(String id, String role, String name, String gender, String address, String phone) {
        int newRowNumber = model.getRowCount() + 1;
        model.addRow(new Object[]{newRowNumber, id, role, name, gender, address, phone, ""});
    }

    // Renderer untuk kolom "Aksi"
    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
            JButton editButton = new RoundedButton("EDIT");
            editButton.setBackground(new Color(255, 153, 51));
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            add(editButton);
            setBackground(Color.WHITE);

            JButton deleteButton = new RoundedButton("HAPUS");
            deleteButton.setBackground(new Color(255, 51, 51));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Editor untuk kolom "Aksi"
    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        JPanel panel;
        int row;

        public ActionCellEditor() {
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));

            // Edit Button
            JButton editButton = new RoundedButton("EDIT");
            editButton.setBackground(new Color(255, 153, 51));
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            editButton.addActionListener(e -> {
                editUser(row); // Call editUser method when the Edit button is pressed
                stopCellEditing(); // Stop editing the cell
            });
            panel.add(editButton);

            // Delete Button
            JButton deleteButton = new RoundedButton("HAPUS");
            deleteButton.setBackground(new Color(255, 51, 51));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.addActionListener(e -> {
                deleteUser(row);
            });
            panel.add(deleteButton);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row; // Set the row number
            return panel; // Return the panel containing buttons
        }

        @Override
        public Object getCellEditorValue() {
            return null; // No value is actually set since it's just actions
        }

        private void editUser(int row) {
            // Stop cell editing sebelum modal muncul (sudah dilakukan di action listener)
            SwingUtilities.invokeLater(() -> {
                EditUser.showModalCenter(
                    (JFrame) SwingUtilities.getWindowAncestor(userTable),
                    (String) model.getValueAt(row, 1),
                    (updatedName, updatedRole, jenis_kelamin, updatedPhone, updatedAddress, updatedRFID) -> {
                        // Update the table with the updated values
                        model.setValueAt(updatedName, row, 3);
                        model.setValueAt(updatedRole, row, 2);
                        model.setValueAt(jenis_kelamin, row, 4);
                        model.setValueAt(updatedAddress, row, 5);
                        model.setValueAt(updatedPhone, row, 6);
                        model.setValueAt(updatedRFID, row, 7); // Assuming RFID is added to the table model
                    }
                );
            });
        }

        private void deleteUser(int row) {
            // Create and show the confirmation dialog
            CustomDialog confirmDialog = new CustomDialog(null, "Apakah Anda yakin ingin menghapus user ini?", "Konfirmasi Penghapusan");
            // Get the user's response

            int response = confirmDialog.showDialog();
            if (response == JOptionPane.YES_OPTION) {
                String deletedQuery = "UPDATE user SET user.delete = ? WHERE id_user = ?";
                QueryExecutor.executeUpdateQuery(deletedQuery, new Object[]{1, (String) model.getValueAt(row, 1)});
                JOptionPane.showMessageDialog(null, "User berhasil dihapus.");
                resetTable();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new User().setVisible(true));
    }
}
