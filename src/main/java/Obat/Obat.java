package Obat;

import Components.CustomDialog;
import Components.CustomTextField;
import Components.CustomTable.CustomTable;
import Components.RoundedButton;
import Components.RoundedPanel;
import DataBase.QueryExecutor;
import Helpers.OnObatAddedListener;
import Helpers.OnObatUpdateListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import java.awt.event.MouseEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static Pemeriksaan.TablePemeriksaan.mapToArray;

public class Obat extends JPanel implements OnObatAddedListener, OnObatUpdateListener {

    private JLabel idLabel, hargaLabel, namaObatLabel, stockLabel, jenisObatLabel;
    private DefaultTableModel tableModel;
    private JTable obatTable;
    private int selectedRow = -1;
    Object[][] data = {};
    Object[][] fullData = {};

    public Obat() {
        // Frame setup
        setLayout(new BorderLayout());
        setSize(800, 600);
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat()";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    data.length + 1, result.get("barcode"), result.get("nama_obat"), result.get("nama_jenis_obat"),
                    result.get("harga"), result.get("stock"), ""
                };

                // Create a new array with an additional row
                Object[][] newData = new Object[data.length + 1][];

                // Copy the old data to the new array
                System.arraycopy(data, 0, newData, 0, data.length);

                // Add the new row to the new array
                newData[data.length] = dataFromDatabase;

                // Send back to original
                data = newData;

                // Create a new array with an additional row
                Object[][] newDataFull = new Object[fullData.length + 1][];

                // Copy the old data to the new array
                System.arraycopy(fullData, 0, newDataFull, 0, fullData.length);

                // Add the new row to the new array
                newDataFull[fullData.length] = mapToArray(result);

                // Send back to original
                fullData = newDataFull;
            }
        }

        setLayout(new BorderLayout());

        // Header Panel (Title)
        JPanel headerPanel = createHeaderPanel();

        // Top Panel (Search and Add Buttons)
        JPanel topPanel = createTopPanel();

        // Data Panel (Displays selected obat details)
        RoundedPanel dataPanel = createDataPanel();

        // Table Panel (Displays list of obats)
        JScrollPane tableScrollPane = createTablePanel();

        // Main Panel combining Data and Table Panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(dataPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add components to the frame
        add(headerPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("OBAT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add the "Tambahkan Obat" button to the right side
        RoundedButton addButton = new RoundedButton("Tambahkan Obat");
        addButton.setBackground(new Color(0, 153, 102));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setPreferredSize(new Dimension(150, 40));
        addButton.addActionListener(e -> SwingUtilities.invokeLater(() -> new RegisterObat(Obat.this, Obat.this).setVisible(true)));

        // Search field with button (left side)
        CustomTextField searchField = new CustomTextField("Cari Obat", 20, 30, Optional.empty());
        RoundedButton searchButton = new RoundedButton("Cari");
        searchButton.setBackground(new Color(0, 153, 102));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(100, 40));

        // Panel to hold the search field and button together
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().toLowerCase();
            Object[][] filteredData = Arrays.stream(data)
                    .filter(row -> ((String) row[1]).toLowerCase().contains(searchTerm) // Check if 'BARCODE' contains search term
                            || ((String) row[2]).toLowerCase().contains(searchTerm)) // Check if 'NAMA OBAT' contains search term
                    .toArray(Object[][]::new);

            // Add the "AKSI" column with action buttons back to the filtered data
            Object[][] dataWithActions = new Object[filteredData.length][7]; // 7 columns including the "AKSI" column

            for (int i = 0; i < filteredData.length; i++) {
                dataWithActions[i] = Arrays.copyOf(filteredData[i], 7); // Copy data to the new array and ensure we have 7 columns
                dataWithActions[i][6] = "Action"; // Placeholder for the "AKSI" column (we will update this with buttons)
            }

            // Update the table model with the filtered data
            tableModel.setDataVector(filteredData, new String[]{"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA", "STOCK", "AKSI"});

            // Reapply the button rendering and editing to the "AKSI" column
            obatTable.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
            obatTable.getColumn("AKSI").setCellEditor(new ActionCellEditor(tableModel));
            setTableColumnWidths(obatTable);
        });

        searchField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch();
            }

            private void performSearch() {
                String searchTerm = searchField.getText().toLowerCase();
                Object[][] filteredData = Arrays.stream(data)
                        .filter(row -> ((String) row[1]).toLowerCase().contains(searchTerm) // Check if 'BARCODE' contains search term
                                || ((String) row[2]).toLowerCase().contains(searchTerm)) // Check if 'NAMA OBAT' contains search term
                        .toArray(Object[][]::new);

                // Add the "AKSI" column with action buttons back to the filtered data
                Object[][] dataWithActions = new Object[filteredData.length][7]; // 7 columns including the "AKSI" column

                for (int i = 0; i < filteredData.length; i++) {
                    dataWithActions[i] = Arrays.copyOf(filteredData[i], 7); // Copy data to the new array and ensure we have 7 columns
                    dataWithActions[i][6] = "Action"; // Placeholder for the "AKSI" column (we will update this with buttons)
                }

                // Update the table model with the filtered data
                tableModel.setDataVector(filteredData, new String[]{"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA", "STOCK", "AKSI"});

                // Reapply the button rendering and editing to the "AKSI" column
                obatTable.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
                obatTable.getColumn("AKSI").setCellEditor(new ActionCellEditor(tableModel));
                setTableColumnWidths(obatTable);
            }
        });

        // Add components to the top panel
        topPanel.add(searchPanel, BorderLayout.WEST);  // Search panel on left
        topPanel.add(addButton, BorderLayout.EAST);    // "Tambahkan Obat" button on right
        return topPanel;
    }

    private RoundedPanel createDataPanel() {
        // Data Panel setup
        RoundedPanel dataPanel = new RoundedPanel(15, Color.WHITE);
        dataPanel.setLayout(new GridLayout(5, 2, 10, 10));
        dataPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create Labels for data
        hargaLabel = createDataLabel("HARGA : ");
        namaObatLabel = createDataLabel("NAMA OBAT : ");
        stockLabel = createDataLabel("STOCK : ");
        jenisObatLabel = createDataLabel("JENIS OBAT : ");

        // Add labels to the data panel
        dataPanel.add(namaObatLabel);
        dataPanel.add(new JLabel(""));  // Placeholder for Nama Obat value
        dataPanel.add(jenisObatLabel);
        dataPanel.add(new JLabel(""));  // Placeholder for Jenis Obat value
        dataPanel.add(hargaLabel);
        dataPanel.add(new JLabel(""));  // Placeholder for Harga value
        dataPanel.add(stockLabel);
        dataPanel.add(new JLabel(""));  // Placeholder for Stock value

        return dataPanel;
    }

    private JScrollPane createTablePanel() {
        // Table data and columns setup
        String[] columns = {"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA", "STOCK", "AKSI"};

        // Table model
        tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Action column (buttons) should be editable
            }
        };

        obatTable = new CustomTable(tableModel);
        obatTable.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
        obatTable.getColumn("AKSI").setCellEditor(new ActionCellEditor(tableModel));

        // Add Mouse Listener to update data panel when a row is selected
        obatTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = obatTable.getSelectedRow();
                if (selectedRow != -1) {
                    updateDataPanel(obatTable, selectedRow);
                }
            }
        });

        // Adjust table column widths for better readability
        setTableColumnWidths(obatTable);

        // Scroll pane for the table
        return new JScrollPane(obatTable);
    }

    private JLabel createDataLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private void updateDataPanel(JTable table, int row) {
        // Get values from the selected row and safely convert them to Strings
        String barcode = String.valueOf(table.getValueAt(row, 1));  // Column 1: BARCODE
        String namaObat = String.valueOf(table.getValueAt(row, 2));  // Column 2: NAMA OBAT
        String jenisObat = String.valueOf(table.getValueAt(row, 3));  // Column 3: JENIS OBAT
        String harga = String.valueOf(table.getValueAt(row, 4));      // Column 4: HARGA
        String stock = String.valueOf(table.getValueAt(row, 5));      // Column 5: STOCK

        // Update the labels in the data panel with values from the selected row
        namaObatLabel.setText("NAMA OBAT : " + namaObat);
        jenisObatLabel.setText("JENIS OBAT : " + jenisObat);
        hargaLabel.setText("HARGA : " + harga);
        stockLabel.setText("STOCK : " + stock);
    }

    private void setTableColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // NO column
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // BARCODE column
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // NAMA OBAT column
        table.getColumnModel().getColumn(3).setPreferredWidth(75);  // JENIS OBAT column
        table.getColumnModel().getColumn(4).setPreferredWidth(75);  // HARGA column
        table.getColumnModel().getColumn(5).setPreferredWidth(75);  // STOCK column
        table.getColumnModel().getColumn(6).setPreferredWidth(150); // AKSI column (Buttons)
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Obat().setVisible(true));
    }

    @Override
    public void onObatAddedListener(String namaObat, String jenisObat, String harga, String stock) {
        tableModel.addRow(new Object[]{obatTable.getRowCount() + 1, namaObat, jenisObat, harga, stock});
    }

    @Override
    public void onObatUpdate(String Harga, String Stock) {
        // Handle updating the row in the table when the "Edit" button is clicked
        if (selectedRow != -1) {
            // Update the selected row with the new data (Harga and Stock)
            tableModel.setValueAt(Harga, selectedRow, 3); // Update HARGA column
            tableModel.setValueAt(Stock, selectedRow, 4); // Update STOCK column
        }

        // Optionally, you can refresh the entire table data if needed
        refreshTableData();
    }

    public void refreshTableData() {
        // Query the database again or get the updated data from elsewhere
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat()";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

        // Clear the current data in the table
        data = new Object[0][];
        fullData = new Object[0][];

        // Re-populate the table data with the updated results
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    data.length + 1, result.get("barcode"), result.get("nama_obat"), result.get("nama_jenis_obat"),
                    result.get("harga"), result.get("stock"), ""
                };

                // Create a new array with an additional row
                Object[][] newData = new Object[data.length + 1][];

                // Copy old data to the new array
                System.arraycopy(data, 0, newData, 0, data.length);

                // Add the new row to the new array
                newData[data.length] = dataFromDatabase;

                // Send back to original
                data = newData;

                // Create a new array with an additional row
                Object[][] newDataFull = new Object[fullData.length + 1][];

                // Copy the old data to the new array
                System.arraycopy(fullData, 0, newDataFull, 0, fullData.length);

                // Add the new row to the new array
                newDataFull[fullData.length] = mapToArray(result);

                // Send back to original
                fullData = newDataFull;
            }
        }

        // Update the table model with the refreshed data
        tableModel.setDataVector(data, new String[]{"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA", "STOCK", "AKSI"});

        // Reapply the button rendering and editing to the "AKSI" column
        obatTable.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
        obatTable.getColumn("AKSI").setCellEditor(new ActionCellEditor(tableModel));
        setTableColumnWidths(obatTable);
    }

    // Renderer for "AKSI" column
    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
            JButton editButton = new RoundedButton("EDIT");
            editButton.setBackground(new Color(255, 153, 51));
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            add(editButton);
            setBackground(Color.WHITE);

            JButton hapusButton = new RoundedButton("HAPUS");
            hapusButton.setBackground(new Color(255, 51, 51));
            hapusButton.setForeground(Color.WHITE);
            hapusButton.setFocusPainted(false);
            add(hapusButton);
            setBackground(Color.WHITE);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    public void addDrugToTable(String name, String type, int quantity, int price) {
        // Add the new drug data to the table
        Object[] newRow = {name, type, quantity, price, ""};
        tableModel.addRow(newRow);
    }

    // Editor for "AKSI" column
    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        JPanel panel;
        int row;
        DefaultTableModel model;

        public ActionCellEditor(DefaultTableModel model) {
            this.model = model;
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));

            JButton editButton = new RoundedButton("EDIT");
            editButton.setBackground(new Color(255, 153, 51));
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Ensure row index is valid before attempting to edit
                    if (row >= 0) {
                        // Get data from the selected row
                        String namaObat = (String) model.getValueAt(row, 2);  // NAMA OBAT
                        String jenisObat = (String) model.getValueAt(row, 3);  // JENIS OBAT
                        String harga = String.valueOf(model.getValueAt(row, 4));  // HARGA
                        String stock = String.valueOf(model.getValueAt(row, 5));  // STOCK
                        String barcode = (String) model.getValueAt(row, 1);  // BARCODE

                        // Launch the EditObat form with the selected data
                        new EditObat(namaObat, jenisObat, harga, stock, barcode, obatTable, row);

                        stopCellEditing();  // Stop editing after opening the edit window
                    } else {
                        System.out.println("Invalid row index for editing: " + row);
                    }
                }
            });
            panel.add(editButton);

            JButton hapusButton = new RoundedButton("HAPUS");
            hapusButton.setBackground(new Color(255, 51, 51));
            hapusButton.setForeground(Color.WHITE);
            hapusButton.setFocusPainted(false);
            hapusButton.addActionListener((ActionEvent e) -> {
                // Create and show the confirmation dialog
                CustomDialog confirmDialog = new CustomDialog(null, "Apakah Anda yakin ingin menghapus obat ini?", "Konfirmasi Penghapusan");
                // Get the user's response
                int response = confirmDialog.showDialog();

                // If the user clicks "Yes"
                if (response == JOptionPane.YES_OPTION) {
                    // Check if the row index is valid before attempting to remove
                    if (row >= 0 && row < model.getRowCount()) {
                        JTable table = (JTable) panel.getParent();// Get the ID of the pasien from the first column (adjust the column index if necessary)
                        int id = (int) fullData[row][2];  // Assuming column 0 holds the 'id_pasien'

                        QueryExecutor executor = new QueryExecutor();
                        String getIdDetailObat = "SELECT id_detail_obat FROM detail_obat WHERE id_obat = ?";
                        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(getIdDetailObat, new Object[]{id});
                        Boolean[] isDone = new Boolean[results.size()];
                        if (!results.isEmpty()) {
                            int index = 0;
                            for (Map<String, Object> result : results) {
                                // Query to mark the pasien as deleted (update 'is_deleted' column)
                                String deletedQuery = "UPDATE detail_obat SET is_deleted = 1 WHERE id_detail_obat = ?";

                                // Execute the update query
                                boolean isUpdated = QueryExecutor.executeUpdateQuery(deletedQuery, new Object[]{result.get("id_detail_obat")});
                                isDone[index] = isUpdated;
                                index++;
                            }
                        }
                        boolean allTrue = true;
                        for (Boolean done : isDone) {
                            if (!done) {
                                allTrue = false;
                                break;
                            }
                        }
                        if (allTrue) {
                            // If the update was successful, remove the row from the table
//                            model.removeRow(row);
                            refreshTableData();
                            JOptionPane.showMessageDialog(null, "Obat berhasil ditandai sebagai dihapus.");
                        } else {
                            // If something went wrong with the update
                            JOptionPane.showMessageDialog(null, "Gagal menandai obat sebagai dihapus.", "Error", JOptionPane.ERROR_MESSAGE);
                        }

                        // Check if the table was in an editing state and stop editing
                        if (table.isEditing()) {
                            table.getCellEditor().stopCellEditing();  // Stop editing the cell if it is being edited
                            System.out.println("Cell editing stopped.");
                        }

                        // Log the row index and row count before removal for debugging
                        System.out.println("Attempting to remove row: " + row);

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
                } else {
                    // If the user clicked "No", simply log that the deletion was canceled
                    System.out.println("Deletion canceled by user.");
                }
            });
            panel.add(hapusButton);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Ensure row index is set properly
            if (row >= 0 && row < table.getRowCount()) {
                this.row = row;  // Set the row index when the cell enters editing mode
            } else {
                System.out.println("Invalid row index passed to cell editor");
            }
            return panel;
        }

        public Object getCellEditorValue() {
            return null;
        }
    }
}
