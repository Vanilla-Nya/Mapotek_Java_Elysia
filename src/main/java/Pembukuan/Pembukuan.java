package Pembukuan;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import Components.CustomDatePicker;
import Components.CustomPanel;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import Pengeluaran.Pengeluaran;

// Tambahkan import untuk JDialog
import javax.swing.JDialog;

// Tambahkan import untuk ShowmodalBottomSheet
import Components.ShowmodalBottomSheet;

public class Pembukuan extends JPanel {

    private DefaultTableModel model;
    private Object[][] data = {};
    private CustomTextField startDatePicker;
    private CustomTextField endDatePicker;
    private Dropdown categoryDropdown;
    private CustomDatePicker customStartDatePicker, customEndDatePicker;

    public Pembukuan() {
        QueryExecutor executor = new QueryExecutor();

        // Set the layout and background
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));  // Soft light background
        setPreferredSize(new Dimension(1280, 720));

        // Header Panel with Title and Subtitle
        CustomPanel headerPanel = new CustomPanel(20);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(1280, 100));
        headerPanel.setBackground(new Color(33, 150, 243));  // Modern blue color

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Pembukuan", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Pengeluaran dan Pemasukan", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Initialize the custom text fields for date pickers
        startDatePicker = new CustomTextField("Pilih Tanggal", 10, 15, Optional.of(false));
        endDatePicker = new CustomTextField("Pilih Tanggal", 10, 15, Optional.of(false));

        // Create CustomDatePicker for start and end date
        customStartDatePicker = new CustomDatePicker(startDatePicker.getTextField(), false);
        customEndDatePicker = new CustomDatePicker(endDatePicker.getTextField(), false);

        // Add mouse listener to show date picker when clicked
        startDatePicker.getTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                customStartDatePicker.showDatePicker();  // Show start date picker
            }
        });

        endDatePicker.getTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                customEndDatePicker.showDatePicker();  // Show end date picker
            }
        });

        // Filter Panel with Modern Design
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setPreferredSize(new Dimension(1280, 150));

        filterPanel.add(createFilterComponent("Tanggal Mulai:", startDatePicker));
        filterPanel.add(createFilterComponent("Tanggal Selesai:", endDatePicker));

        // Category Dropdown
        categoryDropdown = new Dropdown(false, false, "Semua");
        categoryDropdown.setItems(List.of("Semua", "Pemasukan", "Pengeluaran"), false, false, "Semua");
        categoryDropdown.setBackground(Color.WHITE);
        categoryDropdown.setPreferredSize(new Dimension(150, 30));
        filterPanel.add(createFilterComponent("Kategori:", categoryDropdown));

        // Terapkan Filter Button
        RoundedButton filterButton = new RoundedButton("Terapkan Filter");
        filterButton.setBackground(new Color(33, 150, 243));
        filterButton.setForeground(Color.WHITE);
        filterButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filterButton.setMargin(new Insets(10, 20, 10, 20));
        filterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterPanel.add(filterButton);

        // Add filter panel to main layout
        add(filterPanel, BorderLayout.NORTH);

        // Table Setup for Data
        String[] columnNames = {"Tanggal", "Deskripsi", "Banyak", "Jenis"};
        model = new DefaultTableModel(data, columnNames);
        loadData();
        CustomTable table = new CustomTable(model);
        table.setEnabled(false);

        // Apply the custom TableCellRenderer
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        // Table Customization
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(1200, 400));
        add(tableScrollPane, BorderLayout.CENTER);

        // Footer Panel with Action Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        footerPanel.setBackground(Color.WHITE);

        // Add Transaction Button
        RoundedButton addButton = new RoundedButton("Tambahkan Pengeluaran");
        addButton.setBackground(new Color(0, 150, 136));
        addButton.setForeground(Color.WHITE);

        // Add Action Listener to "Add Transaction" Button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Pengeluaran pengeluaranForm = new Pengeluaran(Pembukuan.this);
                pengeluaranForm.setVisible(true);
            }
        });

        // Export to Word Button
        RoundedButton exportButton = new RoundedButton("Ekspor ke Word");
        exportButton.setBackground(new Color(33, 150, 243));
        exportButton.setForeground(Color.WHITE);

        // Add Action Listener for Export to Word
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if table has data before exporting
                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(Pembukuan.this, "No data to export!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create a file chooser to let user choose where to save the Word document
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Word Document");
                fileChooser.setSelectedFile(new File("report.docx"));

                int result = fileChooser.showSaveDialog(Pembukuan.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        // Call the exportToWord method and pass the selected file path
                        exportToWord(selectedFile.getAbsolutePath());
                        JOptionPane.showMessageDialog(Pembukuan.this, "Word document berhasil diekspor!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Pembukuan.this, "Error exporting Word document: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        footerPanel.add(addButton);
        footerPanel.add(exportButton);

        // Modifikasi tombol "Filter Data" di footer panel
        RoundedButton filterModalButton = new RoundedButton("Filter Data");
        filterModalButton.setBackground(new Color(33, 150, 243));
        filterModalButton.setForeground(Color.WHITE);
        filterModalButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filterModalButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        filterModalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFilterBottomSheet(); // Panggil metode untuk menampilkan bottom sheet
            }
        });

        // Tambahkan tombol ke footer panel
        footerPanel.add(filterModalButton);

        add(footerPanel, BorderLayout.SOUTH);

        // Add Action Listener for Filter Button
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if both the start and end date pickers are filled
                String startDate = startDatePicker.getText();
                String endDate = endDatePicker.getText();

                if (startDate.isEmpty() || endDate.isEmpty()) {
                    // If either the start date or end date is empty, show an alert
                    JOptionPane.showMessageDialog(
                            Pembukuan.this,
                            "Harap pilih kedua tanggal (mulai dan selesai) untuk menerapkan filter.",
                            "Peringatan",
                            JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    // If both dates are filled, refresh the table based on the filter
                    refreshTable();  // Refresh table based on new filter values
                }
            }
        });
    }

    private void exportToWord(String filePath) throws IOException {
        // Ensure the file extension is .rtf
        if (!filePath.endsWith(".rtf")) {
            filePath = filePath.replace(".docx", ".rtf");
        }

        // Create a temporary text file
        String tempFilePath = filePath.replace(".rtf", ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilePath))) {
            // Write table headers
            writer.write("Tanggal\tDeskripsi\tBanyak\tJenis");
            writer.newLine();

            // Write table data
            for (int row = 0; row < model.getRowCount(); row++) {
                writer.write(model.getValueAt(row, 0).toString() + "\t");
                writer.write(model.getValueAt(row, 1).toString() + "\t");
                writer.write(model.getValueAt(row, 2).toString() + "\t");
                writer.write(model.getValueAt(row, 3).toString());
                writer.newLine();
            }
        }

        // Convert the text file to a simple Word document format
        convertTextToWord(tempFilePath, filePath);
    }

    private void convertTextToWord(String textFilePath, String wordFilePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(textFilePath));
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(wordFilePath))) {
            writer.write("{\\rtf1\\ansi\\deff0");
            writer.newLine();
            writer.write("{\\colortbl ;\\red255\\green99\\blue71;\\red34\\green139\\blue34;}");
            writer.newLine();

            double totalKeuntungan = 0.0;

            for (String line : lines) {
                writer.write("\\trowd\\trgaph108\\trleft-108");
                writer.newLine();
                writer.write("\\clbrdrt\\brdrs\\brdrw10\\clbrdrl\\brdrs\\brdrw10\\clbrdrb\\brdrs\\brdrw10\\clbrdrr\\brdrs\\brdrw10\\cellx2000");
                writer.write("\\clbrdrt\\brdrs\\brdrw10\\clbrdrl\\brdrs\\brdrw10\\clbrdrb\\brdrs\\brdrw10\\clbrdrr\\brdrs\\brdrw10\\cellx4000");
                writer.write("\\clbrdrt\\brdrs\\brdrw10\\clbrdrl\\brdrs\\brdrw10\\clbrdrb\\brdrs\\brdrw10\\clbrdrr\\brdrs\\brdrw10\\cellx6000");
                writer.write("\\clbrdrt\\brdrs\\brdrw10\\clbrdrl\\brdrs\\brdrw10\\clbrdrb\\brdrs\\brdrw10\\clbrdrr\\brdrs\\brdrw10\\cellx8000");
                writer.newLine();

                String[] cells = line.split("\t");

                // Determine the background color based on the "Jenis" column
                String jenis = cells[3];
                String bgColor = "";
                if ("Pengeluaran".equalsIgnoreCase(jenis)) {
                    bgColor = "\\clcbpat1";  // Red background
                } else if ("Pemasukan".equalsIgnoreCase(jenis)) {
                    bgColor = "\\clcbpat2";  // Green background
                }

                // Apply the background color to the "Jenis" cell only
                writer.write("\\intbl " + cells[0] + "\\cell ");
                writer.write("\\intbl " + cells[1] + "\\cell ");
                writer.write("\\intbl " + cells[2] + "\\cell ");
                writer.write(bgColor + "\\intbl " + cells[3] + "\\cell ");
                writer.write("\\row");
                writer.newLine();

                // Calculate total keuntungan
                if (!line.startsWith("Tanggal")) { // Skip header row
                    try {
                        double banyak = Double.parseDouble(cells[2].replace(",", ""));
                        if ("Pemasukan".equalsIgnoreCase(cells[3])) {
                            totalKeuntungan += banyak;
                        } else if ("Pengeluaran".equalsIgnoreCase(cells[3])) {
                            totalKeuntungan -= banyak;
                        }
                    } catch (NumberFormatException e) {
                        // Handle the case where the number format is incorrect
                        System.err.println("Error parsing number: " + cells[2]);
                    }
                }
            }

            // Add summary row for total keuntungan with merged cells
            writer.write("\\trowd\\trgaph108\\trleft-108");
            writer.newLine();
            writer.write("\\clbrdrt\\brdrs\\brdrw10\\clbrdrl\\brdrs\\brdrw10\\clbrdrb\\brdrs\\brdrw10\\clbrdrr\\brdrs\\brdrw10\\cellx2000");
            writer.write("\\clbrdrt\\brdrs\\brdrw10\\clbrdrl\\brdrs\\brdrw10\\clbrdrb\\brdrs\\brdrw10\\clbrdrr\\brdrs\\brdrw10\\cellx8000"); // Merge cells 2, 3, 4
            writer.newLine();
            writer.write("\\intbl\\qc Total Keuntungan\\cell \\intbl\\qc " + decimalFormat.format(totalKeuntungan) + "\\cell \\row");
            writer.newLine();

            writer.write("}");
        }

        // Delete the temporary text file
        Files.delete(Paths.get(textFilePath));
    }

    private JPanel createFilterComponent(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    public void refreshTable() {
        // Clear the existing data
        model.setRowCount(0);
        loadData();  // Reload the data from the database
        model.fireTableDataChanged();
    }

    private void loadData() {
        data = new Object[0][];  // Clear existing data
        model.setDataVector(data, new String[]{"Tanggal", "Deskripsi", "Banyak", "Jenis"});

        String startDate = startDatePicker.getText();
        String endDate = endDatePicker.getText();
        String selectedCategory = (String) categoryDropdown.getSelectedItem();

        QueryExecutor executor = new QueryExecutor();

        // Query Pemasukan (Income)
        if ("Pemasukan".equals(selectedCategory) || "Semua".equals(selectedCategory)) {
            String queryPemasukan = "CALL all_pemasukan_harian(?, ?)";
            List<Map<String, Object>> resultPemasukan = executor.executeSelectQuery(queryPemasukan, new Object[]{startDate, endDate});
            for (Map<String, Object> result : resultPemasukan) {
                Object[] dataFromDatabase = new Object[]{
                    result.get("tanggal"), result.get("deskripsi"),
                    result.get("total"), result.get("jenis")
                };
                addDataToTable(dataFromDatabase);
            }
        }

        // Query Pengeluaran (Expenses)
        if ("Pengeluaran".equals(selectedCategory) || "Semua".equals(selectedCategory)) {
            String queryPengeluaran = "CALL all_pengeluaran(?, ?)";
            List<Map<String, Object>> resultPengeluaran = executor.executeSelectQuery(queryPengeluaran, new Object[]{startDate, endDate});
            for (Map<String, Object> result : resultPengeluaran) {
                Object[] dataFromDatabase = new Object[]{
                    result.get("tanggal"), result.get("keterangan"),
                    result.get("total_pengeluaran"), result.get("jenis")
                };
                addDataToTable(dataFromDatabase);
            }
        }
    }

    // Method to add rows to the table model
    private void addDataToTable(Object[] data) {
        model.addRow(data);
    }

    private static class CustomTableCellRenderer extends JLabel implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");

            // Color the entire row based on the "Jenis" column
            String jenis = (String) table.getValueAt(row, 3);  // Get value from the "Jenis" column (index 3)
            if ("Pengeluaran".equalsIgnoreCase(jenis)) {
                // Red background for "Pengeluaran" rows (expenses)
                setBackground(new Color(255, 99, 71));
                setForeground(Color.WHITE);  // White text color
            } else if ("Pemasukan".equalsIgnoreCase(jenis)) {
                // Green background for "Pemasukan" rows (income)
                setBackground(new Color(34, 139, 34));
                setForeground(Color.WHITE);  // White text color
            } else {
                // Default background for other rows
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);  // Default text color
            }

            setOpaque(true);  // Make sure the background color is applied
            return this;
        }
    }

    // Tambahkan metode untuk menampilkan filter di bottom sheet
    private void showFilterBottomSheet() {
        // Panel untuk komponen filter
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tambahkan komponen filter ke dalam panel
        filterPanel.add(createFilterComponent("Tanggal Mulai:", startDatePicker));
        filterPanel.add(createFilterComponent("Tanggal Selesai:", endDatePicker));
        filterPanel.add(createFilterComponent("Kategori:", categoryDropdown));

        // Tombol untuk menerapkan filter
        RoundedButton applyFilterButton = new RoundedButton("Terapkan Filter");
        applyFilterButton.setBackground(new Color(33, 150, 243));
        applyFilterButton.setForeground(Color.WHITE);
        applyFilterButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        applyFilterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        applyFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String startDate = startDatePicker.getText();
                String endDate = endDatePicker.getText();

                if (startDate.isEmpty() || endDate.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(Pembukuan.this),
                            "Harap pilih kedua tanggal (mulai dan selesai) untuk menerapkan filter.",
                            "Peringatan",
                            JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    refreshTable(); // Refresh tabel berdasarkan filter

                    // Tutup bottom sheet
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(Pembukuan.this);
                    ShowmodalBottomSheet.closeBottomSheet(parentFrame);
                }
            }
        });

        // Tambahkan tombol ke panel
        filterPanel.add(Box.createVerticalStrut(20)); // Spasi antar komponen
        filterPanel.add(applyFilterButton);

        // Tampilkan bottom sheet
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ShowmodalBottomSheet.showBottomSheet(parentFrame, filterPanel);
    }
}
