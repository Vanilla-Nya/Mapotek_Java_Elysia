package Pasien;

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
import java.util.Map;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class Pasien extends JFrame implements OnPasienAddedListener, OnPasienUpdatedListener {

    private JLabel lblID, lblnik, lblNamaPasien, lblUmur, lblJenisKelamin, lblNoTelp, lblAlamat;
    private DefaultTableModel model;
    private JTextField searchField;
    private JScrollPane tableScrollPane;
    private int role;
    Object[][] data = {};

    public Pasien(int role) {
        this.role = role;
        setSize(1000, 600);
        setLocationRelativeTo(null);
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_pasien()";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                var alamat = "".equals(result.get("alamat").toString()) ? "-" : result.get("alamat");
                var no_telp = "".equals(result.get("no_telp").toString()) ? "-" : result.get("no_telp");
                var rfid = result.get("rfid") != null ? result.get("rfid").toString() : "-"; // Handle null RFID
                Object[] dataFromDatabase = new Object[]{
                    result.get("id_pasien"), result.get("nik"), result.get("nama_pasien"), result.get("umur"),
                    result.get("jenis_kelamin"), alamat, no_telp, rfid, ""
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
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel (Title)
        JPanel headerPanel = createHeaderPanel();

        // Search Panel (Search and Add Buttons)
        JPanel searchPanel = createSearchPanel(role);

        // Data Panel (Displays patient details)
        RoundedPanel detailPanel = createDetailPanel();

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
    }

    private JScrollPane createTablePanel(int role) {
        // Table data and columns setup
        String[] columnNames;
        if (role == 1) {
            columnNames = new String[]{"ID", "NIK", "Nama pasien", "Umur", "Jenis Kelamin", "Alamat", "No.Telp", "RFID"};
        } else {
            columnNames = new String[]{"ID", "NIK", "Nama pasien", "Umur", "Jenis Kelamin", "Alamat", "No.Telp", "RFID", "Aksi"};
        }

        // Table model
        model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Only "Aksi" column is editable
            }
        };

        CustomTable pasienTable = new CustomTable(model);
        if (role != 1) {
            pasienTable.getColumn("Aksi").setCellRenderer(new ActionCellRenderer());
            pasienTable.getColumn("Aksi").setCellEditor(new ActionCellEditor());
        }

        // MouseListener to handle click on table rows
        pasienTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = pasienTable.getSelectedRow();
                if (row != -1) {
                    // Get data from selected row
                    String id = (String) String.valueOf(model.getValueAt(row, 0));
                    String nik = (String) model.getValueAt(row, 1);
                    String name = (String) model.getValueAt(row, 2);
                    String age = (String) model.getValueAt(row, 3);
                    String gender = (String) model.getValueAt(row, 4);
                    String address = (String) model.getValueAt(row, 5);
                    String phone = (String) model.getValueAt(row, 6);
                    String rfid = (String) model.getValueAt(row, 7);

                    // Update labels with selected data, including labels with prefixes
                    lblID.setText(id);
                    lblnik.setText(nik);
                    lblNamaPasien.setText(name);
                    lblUmur.setText(age);
                    lblJenisKelamin.setText(gender);
                    lblAlamat.setText(address);
                    lblNoTelp.setText(phone);
                }
            }
        });
        // Adjust table column widths
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
        if (role != 1) {
            table.getColumnModel().getColumn(8).setPreferredWidth(200);
        }
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
        addButton.addActionListener(e -> SwingUtilities.invokeLater(() -> new RegisterPasien(Pasien.this, model).setVisible(true)));

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

    private RoundedPanel createDetailPanel() {
        // Panel detail data pasien dengan RoundedPanel
        RoundedPanel detailPanel = new RoundedPanel(15, Color.WHITE);
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create two sub-panels (left and right)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(4, 2, 5, 10));  // 3 rows, 2 columns for labels and values
        leftPanel.setBackground(Color.WHITE);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(3, 2, 50, 10));  // 3 rows, 2 columns for labels and values
        rightPanel.setBackground(Color.WHITE);

        // Labels and values for right panel
        rightPanel.add(new JLabel("Jenis Kelamin: "));
        rightPanel.add(lblJenisKelamin = createPlainDetailValueLabel());

        rightPanel.add(new JLabel("Alamat: "));
        rightPanel.add(lblAlamat = createPlainDetailValueLabel());

        rightPanel.add(new JLabel("No.Telp: "));
        rightPanel.add(lblNoTelp = createPlainDetailValueLabel());

        // Labels and values for left panel
        leftPanel.add(new JLabel("ID: "));
        leftPanel.add(lblID = createPlainDetailValueLabel());

        leftPanel.add(new JLabel("NIK: "));
        leftPanel.add(lblnik = createPlainDetailValueLabel());

        leftPanel.add(new JLabel("Nama Pasien: "));
        leftPanel.add(lblNamaPasien = createPlainDetailValueLabel());

        leftPanel.add(new JLabel("Umur: "));
        leftPanel.add(lblUmur = createPlainDetailValueLabel());

        // Add both panels to the main detailPanel
        detailPanel.add(leftPanel, BorderLayout.WEST);
        detailPanel.add(rightPanel, BorderLayout.EAST);

        return detailPanel;
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
        for (int i = 0; i < model.getRowCount(); i++) {
            String namaPasien = (String) model.getValueAt(i, 1); // Column "Nama pasien"
            if (namaPasien.toLowerCase().contains(searchText)) {
                model.fireTableRowsUpdated(i, i); // Refresh matching rows
            } else {
                model.removeRow(i); // Remove non-matching rows
                i--; // Adjust index to avoid skipping rows
            }
        }
    }

    // Function to reset the table to show all rows again
    private void resetTable() {
        model.setRowCount(0);
        // Add back the data to the table here, if needed.
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

    public void onPasienAdded(String id, String nik, String name, String age, String gender, String phone, String address) {
        // Handle the new pasien details when added
        model.addRow(new Object[]{id, nik, name, age, gender, address, phone, "Edit"});
    }

    @Override
    public void onPasienUpdated(String nik, String name, String age, String gender, String phone, String address, String rfid) {
        // Handle updating the patient in your model or UI
        int selectedRow = getSelectedRowInTable(); // Get the selected row in the table

        if (selectedRow != -1) {
            // Update the selected row with the new data from EditPasien
            model.setValueAt(nik, selectedRow, 1);
            model.setValueAt(name, selectedRow, 2); // Update name
            model.setValueAt(age, selectedRow, 3); // Update age
            model.setValueAt(gender, selectedRow, 4); // Update gender
            model.setValueAt(address, selectedRow, 5); // Update address
            model.setValueAt(phone, selectedRow, 6); // Update phone number
            model.setValueAt(rfid, selectedRow, 7); // Update RFID
        }
    }

    // Inner class for ActionCellRenderer (for rendering the edit button)
    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        private final JButton editButton, deleteButton;

        public ActionCellRenderer() {
            editButton = new RoundedButton("EDIT");
            editButton.setBackground(new Color(255, 153, 51));
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            add(editButton);
            deleteButton = new RoundedButton("Delete");
            deleteButton.setBackground(Color.RED);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            add(deleteButton);
            setBackground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }

        public JButton getEditButton() {
            return editButton;
        }
    }

    // Inner class for ActionCellEditor (for handling button clicks)
    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        JPanel panel;
        private int row;

        public ActionCellEditor() {
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Clear previous buttons
            panel.removeAll();
            this.row = row;
            RoundedButton editButton = new RoundedButton("EDIT");
            editButton.setBackground(new Color(255, 153, 51));
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);

            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (row != -1) {
                        String id = (String) String.valueOf(model.getValueAt(row, 0));
                        String nik = (String) model.getValueAt(row, 1);
                        String name = (String) model.getValueAt(row, 2);
                        String age = (String) model.getValueAt(row, 3);
                        String gender = (String) model.getValueAt(row, 4);
                        String address = (String) model.getValueAt(row, 5);
                        String phone = "-".equals((String) model.getValueAt(row, 6)) ? "0" : (String) model.getValueAt(row, 6);
                        String rfid = (String) model.getValueAt(row, 7);

                        SwingUtilities.invokeLater(() -> {
                            new EditPasien(id, nik, name, age, gender, phone, address, rfid, Pasien.this);
                        });
                    }
                }
            });
            RoundedButton deleteButton = new RoundedButton("Delete");
            deleteButton.setBackground(Color.RED);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.addActionListener(e -> {
                // Check if the row index is valid before attempting to remove
                if (row >= 0 && row < model.getRowCount()) {

                    // Check if the table was in an editing state and stop editing
                    if (table.isEditing()) {
                        table.getCellEditor().stopCellEditing();  // Stop editing the cell if it is being edited
                        System.out.println("Cell editing stopped.");
                    }

                    // Log the row index and row count before removal for debugging
                    System.out.println("Attempting to remove row: " + row);

                    // Call the deletePasien method to handle the deletion
                    deletePasien(row);

                    // Refresh the table view after the row is removed
                    table.revalidate();
                    table.repaint();

                    // Handle edge case if the last row was removed
                    if (model.getRowCount() == 0) {
                        System.out.println("Last row deleted, table is empty.");
                    } else {
                        // After removing the last row, we might want to focus or highlight the new "last row"
                        int lastRowIndex = model.getRowCount() - 1;
                        table.setRowSelectionInterval(lastRowIndex, lastRowIndex);
                    }
                } else {
                    System.out.println("Invalid row index for deletion: " + row);
                }
            });
            panel.add(editButton);
            panel.add(deleteButton);
            panel.revalidate();
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
    }

    private void deletePasien(int row) {
        // Create and show the confirmation dialog
        CustomDialog confirmDialog = new CustomDialog(null, "Apakah Anda yakin ingin menandai pasien ini sebagai dihapus?", "Konfirmasi Penghapusan");

        // Get the user's response
        int response = confirmDialog.showDialog();

        if (response == JOptionPane.YES_OPTION) {
            // Get the ID of the pasien from the first column (adjust the column index if necessary)
            String pasienId = (String) model.getValueAt(row, 0);  // Assuming column 0 holds the 'id_pasien'

            // Query to mark the pasien as deleted (update 'is_deleted' column)
            String deletedQuery = "UPDATE pasien SET is_deleted = 1 WHERE id_pasien = ?";

            // Execute the update query
            boolean isUpdated = QueryExecutor.executeUpdateQuery(deletedQuery, new Object[]{pasienId});

            if (isUpdated) {
                // If the update was successful, remove the row from the table
                model.removeRow(row);
                JOptionPane.showMessageDialog(null, "Pasien berhasil ditandai sebagai dihapus.");
            } else {
                // If something went wrong with the update
                JOptionPane.showMessageDialog(null, "Gagal menandai pasien sebagai dihapus.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Handle cancel or close action
            JOptionPane.showMessageDialog(null, "Penghapusan dibatalkan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
