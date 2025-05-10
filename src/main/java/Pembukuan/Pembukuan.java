package Pembukuan;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

// Tambahkan import untuk JDialog
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import Components.CustomChart;
import Components.CustomDatePicker;
import Components.CustomPanel;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.Dropdown;
import Components.ExpandableCard;
import Components.PieChart;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import Pengeluaran.Pengeluaran;
// Tambahkan import untuk ShowmodalBottomSheet
import Components.ShowmodalBottomSheet;

public class Pembukuan extends JPanel {

    private DefaultTableModel model;
    private Object[][] data = {};
    private CustomTextField startDatePicker;
    private CustomTextField endDatePicker;
    private Dropdown categoryDropdown;
    private CustomDatePicker customStartDatePicker, customEndDatePicker;
    private PieChart pieChart;
    private JPanel summaryPanel;
    private JLabel totalPemasukanLabel;
    private JLabel totalPengeluaranLabel;
    private JLabel totalKeuntunganLabel;
    private CustomChart customChart; // Komponen CustomChart
    private int[] incomeData; // Data pemasukan
    private int[] outcomeData; // Data pengeluaran
    private String[] xLabels; // Label untuk sumbu X
    private String[] yLabels = {"0", "20", "40", "60", "80", "100"}; // Label untuk sumbu Y

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

        // Inisialisasi categoryDropdown
        categoryDropdown = new Dropdown(false, false, "Semua");
        categoryDropdown.setItems(List.of("Semua", "Pemasukan", "Pengeluaran"), false, false, "Semua");

        // Create Summary Panel
        createSummaryPanel();

        // Table Setup for Data
        String[] columnNames = {"Tanggal", "Total Pemasukan", "Total Pengeluaran", "Aksi"};
        model = new DefaultTableModel(data, columnNames);
        loadData();
        CustomTable table = new CustomTable(model);
        table.setEnabled(false);

        // Apply the custom TableCellRenderer
        // Define a simple CustomTableCellRenderer class if it doesn't exist
                table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (column == 3) { // Customize the "Aksi" column
                            cell.setForeground(Color.BLUE);
                            cell.setFont(cell.getFont().deriveFont(Font.BOLD));
                        }
                        return cell;
                    }
                });

        // Add mouse listener for table actions
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());

                if (column == 3) { // Kolom "Aksi"
                    String tanggal = (String) model.getValueAt(row, 0);
                    showDetailDialog(tanggal);
                }
            }
        });

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
                showFilterBottomSheet(); // Call the method to display the bottom sheet
            }
        });

        // Tambahkan tombol ke footer panel
        footerPanel.add(filterModalButton);

        add(footerPanel, BorderLayout.SOUTH);

        // Inisialisasi array dengan ukuran default (misalnya 0)
        incomeData = new int[0];
        outcomeData = new int[0];
    }

    private void exportToWord(String filePath) throws IOException {
        // Ensure the file extension is .rtf
        if (!filePath.endsWith(".rtf")) {
            filePath = filePath.replace(".docx", ".rtf");
        }

        // Query all transaction details
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_transaksi_detail(?, ?)";
        List<Map<String, Object>> allDetails = executor.executeSelectQuery(query, new Object[]{startDatePicker.getText(), endDatePicker.getText()});

        // Variables to calculate totals
        double totalPemasukan = 0.0;
        double totalPengeluaran = 0.0;

        // Create a temporary text file
        String tempFilePath = filePath.replace(".rtf", ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilePath))) {
            // Write table headers
            writer.write("No\tTanggal\tDeskripsi\tJenis\tTotal");
            writer.newLine();

            // Write table data
            int no = 1;
            for (Map<String, Object> detail : allDetails) {
                String tanggal = detail.get("tanggal").toString();
                String deskripsi = detail.get("deskripsi").toString();
                String jenis = detail.get("jenis").toString();
                double total = ((Number) detail.get("total")).doubleValue();

                // Add to totals
                if (jenis.equalsIgnoreCase("Pemasukan")) {
                    totalPemasukan += total;
                } else if (jenis.equalsIgnoreCase("Pengeluaran")) {
                    totalPengeluaran += total;
                }

                writer.write(no++ + "\t" + tanggal + "\t" + deskripsi + "\t" + jenis + "\t" + formatToRupiah(total));
                writer.newLine();
            }

            // Calculate total keuntungan
            double totalKeuntungan = totalPemasukan - totalPengeluaran;

             // Write totals to the file (merge into 2 cells)
            writer.newLine();
            writer.write("Total Pemasukan:\t" + formatToRupiah(totalPemasukan)); // 2 cells: label and value
            writer.newLine();
            writer.write("Total Pengeluaran:\t" + formatToRupiah(totalPengeluaran)); // 2 cells: label and value
            writer.newLine();
            writer.write("Total Keuntungan:\t" + formatToRupiah(totalKeuntungan)); // 2 cells: label and value
        }

        // Convert the text file to a simple Word document format
        convertTextToWord(tempFilePath, filePath);

        // Delete the temporary text file after conversion
        File tempFile = new File(tempFilePath);
        if (tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (deleted) {
                System.out.println("Temporary file deleted.");
            } else {
                System.out.println("Failed to delete the temporary file.");
            }
        }
    }

    public static void convertTextToWord(String inputFile, String outputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        writer.write("{\\rtf1\\ansi\\deff0\n");

        // Set column widths (in twips)
        int[] cellWidths = {1000, 3000, 6000, 8000, 10000}; // 5 columns

        // Table header
        writer.write("\\trowd\\trgaph108\\trleft-108\n");
        for (int width : cellWidths) {
            writer.write("\\clbrdrt\\brdrs\\clbrdrl\\brdrs\\clbrdrb\\brdrs\\clbrdrr\\brdrs\\cellx" + width + "\n");
        }
        writer.write("\\intbl No\\cell Tanggal\\cell Deskripsi\\cell Jenis\\cell Total\\cell\\row\n");

        // Table content
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue; // ✅ Skip empty lines
            String[] cells = line.split("\t");

            if (cells.length == 2) {
                writer.write("\\trowd\\trgaph108\\trleft-108\n");
                writer.write(
                    // First merged cell spans 3 columns (width 6000)
                    "\\clbrdrt\\brdrs\\clbrdrl\\brdrs\\clbrdrb\\brdrs\\clbrdrr\\brdrs\\cellx6000" +
                    // Second merged cell spans last 2 columns (width 4000)
                    "\\clbrdrt\\brdrs\\clbrdrl\\brdrs\\clbrdrb\\brdrs\\clbrdrr\\brdrs\\cellx10000\n"
                );

                writer.write("\\intbl " + cells[0] + "\\cell ");
                writer.write("\\intbl " + cells[1] + "\\cell ");
                writer.write("\\row\n");
            } else {
                writer.write("\\trowd\\trgaph108\\trleft-108\n");
                for (int width : cellWidths) {
                    writer.write("\\clbrdrt\\brdrs\\clbrdrl\\brdrs\\clbrdrb\\brdrs\\clbrdrr\\brdrs\\cellx" + width + "\n");
                }
                for (String cell : cells) {
                    writer.write("\\intbl " + cell + "\\cell ");
                }
                writer.write("\\row\n");
            }
        }

        writer.write("}");
        reader.close();
        writer.close();
    }

    private String escapeRTF(String text) {
        return text.replace("\\", "\\\\")
               .replace("{", "\\{")
               .replace("}", "\\}")
               .replace("\t", "    "); // Replace tab with spaces
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
        data = new Object[0][];
        model.setDataVector(data, new String[]{"Tanggal", "Total Pemasukan", "Total Pengeluaran", "Aksi"});

        String startDate = startDatePicker.getText();
        String endDate = endDatePicker.getText();

        // Validasi input tanggal
        if (startDate.isEmpty() || endDate.isEmpty()) {
            incomeData = new int[0];
            outcomeData = new int[0];
            return; // Jika tanggal kosong, hentikan eksekusi
        }

        QueryExecutor executor = new QueryExecutor();
        Map<String, Map<String, Double>> groupedData = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        double totalPemasukan = 0.0;
        double totalPengeluaran = 0.0;

        try {
            // Query Pemasukan
            String queryPemasukan = "CALL all_pemasukan_harian(?, ?)";
            List<Map<String, Object>> resultPemasukan = executor.executeSelectQuery(queryPemasukan, new Object[]{startDate, endDate});
            for (Map<String, Object> result : resultPemasukan) {
                java.sql.Date sqlDate = (java.sql.Date) result.get("tanggal");
                String tanggal = dateFormat.format(sqlDate);
                double pemasukan = ((Number) result.get("total")).doubleValue();

                groupedData.putIfAbsent(tanggal, new HashMap<>());
                groupedData.get(tanggal).put("pemasukan", groupedData.get(tanggal).getOrDefault("pemasukan", 0.0) + pemasukan);

                totalPemasukan += pemasukan;
            }

            // Query Pengeluaran
            String queryPengeluaran = "CALL all_pengeluaran(?, ?)";
            List<Map<String, Object>> resultPengeluaran = executor.executeSelectQuery(queryPengeluaran, new Object[]{startDate, endDate});
            for (Map<String, Object> result : resultPengeluaran) {
                java.sql.Date sqlDate = (java.sql.Date) result.get("tanggal");
                String tanggal = dateFormat.format(sqlDate);
                double pengeluaran = ((Number) result.get("total_pengeluaran")).doubleValue();

                groupedData.putIfAbsent(tanggal, new HashMap<>());
                groupedData.get(tanggal).put("pengeluaran", groupedData.get(tanggal).getOrDefault("pengeluaran", 0.0) + pengeluaran);

                totalPengeluaran += pengeluaran;
            }

            // Populate the table
            List<String> sortedDates = new ArrayList<>(groupedData.keySet());
            Collections.sort(sortedDates); // Urutkan tanggal secara ascending

            incomeData = new int[sortedDates.size()];
            outcomeData = new int[sortedDates.size()];

            int index = 0;
            for (String tanggal : sortedDates) {
                double pemasukan = groupedData.get(tanggal).getOrDefault("pemasukan", 0.0);
                double pengeluaran = groupedData.get(tanggal).getOrDefault("pengeluaran", 0.0);

                model.addRow(new Object[]{tanggal, formatToRupiah(pemasukan), formatToRupiah(pengeluaran), "Lihat Detail"});

                incomeData[index] = (int) pemasukan;
                outcomeData[index] = (int) pengeluaran;
                index++;
            }

            // Update total labels
            double totalKeuntungan = totalPemasukan - totalPengeluaran;
            totalPemasukanLabel.setText(formatToRupiah(totalPemasukan));
            totalPengeluaranLabel.setText(formatToRupiah(totalPengeluaran));
            totalKeuntunganLabel.setText(formatToRupiah(totalKeuntungan));

            // Update PieChart
            pieChart.updateData(totalPemasukan, totalPengeluaran);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        data = new Object[0][];  // Clear existing data
        model.setDataVector(data, new String[]{"Tanggal", "Total Pemasukan", "Total Pengeluaran", "Aksi"});

        String startDate = startDatePicker.getText();
        String endDate = endDatePicker.getText();

        // Validasi input tanggal
        if (startDate.isEmpty() || endDate.isEmpty()) {
            // Jika tanggal kosong, jangan tampilkan pesan error, cukup hentikan eksekusi
            return;
        }

        QueryExecutor executor = new QueryExecutor();
        Map<String, Map<String, Double>> groupedData = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Format tanggal

        double totalPemasukan = 0.0;
        double totalPengeluaran = 0.0;

        try {
            // Query Pemasukan (Income)
            String queryPemasukan = "CALL all_pemasukan_harian(?, ?)";
            List<Map<String, Object>> resultPemasukan = executor.executeSelectQuery(queryPemasukan, new Object[]{startDate, endDate});
            for (Map<String, Object> result : resultPemasukan) {
                java.sql.Date sqlDate = (java.sql.Date) result.get("tanggal");
                String tanggal = dateFormat.format(sqlDate); // Konversi java.sql.Date ke String
                double pemasukan = ((Number) result.get("total")).doubleValue();

                groupedData.putIfAbsent(tanggal, new HashMap<>());
                groupedData.get(tanggal).put("pemasukan", groupedData.get(tanggal).getOrDefault("pemasukan", 0.0) + pemasukan);

                // Tambahkan ke total pemasukan
                totalPemasukan += pemasukan;
            }

            // Query Pengeluaran (Expenses)
            String queryPengeluaran = "CALL all_pengeluaran(?, ?)";
            List<Map<String, Object>> resultPengeluaran = executor.executeSelectQuery(queryPengeluaran, new Object[]{startDate, endDate});
            for (Map<String, Object> result : resultPengeluaran) {
                java.sql.Date sqlDate = (java.sql.Date) result.get("tanggal");
                String tanggal = dateFormat.format(sqlDate); // Konversi java.sql.Date ke String
                double pengeluaran = ((Number) result.get("total_pengeluaran")).doubleValue();

                groupedData.putIfAbsent(tanggal, new HashMap<>());
                groupedData.get(tanggal).put("pengeluaran", groupedData.get(tanggal).getOrDefault("pengeluaran", 0.0) + pengeluaran);

                // Tambahkan ke total pengeluaran
                totalPengeluaran += pengeluaran;
            }

            // Populate the table
            int index = 0; // Initialize the index variable
            for (String tanggal : groupedData.keySet()) {
                double pemasukan = groupedData.get(tanggal).getOrDefault("pemasukan", 0.0);
                double pengeluaran = groupedData.get(tanggal).getOrDefault("pengeluaran", 0.0);

                // Tambahkan data ke tabel
                model.addRow(new Object[]{tanggal, formatToRupiah(pemasukan), formatToRupiah(pengeluaran), "Lihat Detail"});

                // Tambahkan data ke grafik
                incomeData[index] = (int) pemasukan;
                outcomeData[index] = (int) pengeluaran;
                xLabels[index] = tanggal;
                index++;
            }

            // Hitung total keuntungan
            double totalKeuntungan = totalPemasukan - totalPengeluaran;

            // Perbarui label di summary panel
            totalPemasukanLabel.setText(formatToRupiah(totalPemasukan));
            totalPengeluaranLabel.setText(formatToRupiah(totalPengeluaran));
            totalKeuntunganLabel.setText(formatToRupiah(totalKeuntungan));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showFilterBottomSheet() {
        // Buat panel untuk konten filter
        JPanel filterPanel = new JPanel(new GridLayout(2, 1, 10, 10)); // Ubah menjadi 2 baris
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        filterPanel.setBackground(Color.WHITE);

        // Tambahkan komponen filter (Start Date dan End Date)
        filterPanel.add(createFilterComponent("Start Date", startDatePicker));
        filterPanel.add(createFilterComponent("End Date", endDatePicker));

        // Tambahkan tombol "Terapkan Filter"
        JButton applyButton = new JButton("Terapkan Filter");
        applyButton.setBackground(new Color(33, 150, 243));
        applyButton.setForeground(Color.WHITE);
        applyButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        applyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Tambahkan listener untuk tombol "Terapkan Filter"
        applyButton.addActionListener(e -> {
            String startDate = startDatePicker.getText();
            String endDate = endDatePicker.getText();

            // Validasi input tanggal
            if (startDate.isEmpty() || endDate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tanggal tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            refreshTable(); // Refresh tabel dengan filter yang diterapkan
            ShowmodalBottomSheet.closeBottomSheet((JFrame) SwingUtilities.getWindowAncestor(this)); // Tutup bottom sheet
        });

        // Tambahkan tombol ke panel filter
        filterPanel.add(applyButton);

        // Tampilkan bottom sheet
        ShowmodalBottomSheet.showBottomSheet((JFrame) SwingUtilities.getWindowAncestor(this), filterPanel);
    }

    private void createSummaryPanel() {
        // Panel untuk card summary (Total Pemasukan, Total Pengeluaran, Total Keuntungan)
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new GridLayout(1, 3, 20, 0)); // 3 cards in a row
        cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cardPanel.setBackground(Color.WHITE);

        // Card for Total Pemasukan
        JPanel pemasukanCard = createCard("Total Pemasukan", "Rp. 0");
        totalPemasukanLabel = (JLabel) pemasukanCard.getComponent(1); // Get the label for updating later

        // Card for Total Pengeluaran
        JPanel pengeluaranCard = createCard("Total Pengeluaran", "Rp. 0");
        totalPengeluaranLabel = (JLabel) pengeluaranCard.getComponent(1);

        // Card for Total Keuntungan
        JPanel keuntunganCard = createCard("Total Keuntungan", "Rp. 0");
        totalKeuntunganLabel = (JLabel) keuntunganCard.getComponent(1);

        cardPanel.add(pemasukanCard);
        cardPanel.add(pengeluaranCard);
        cardPanel.add(keuntunganCard);

        // Inisialisasi PieChart dengan data kosong
        pieChart = new PieChart(0, 0);
        pieChart.setPreferredSize(new Dimension(200, 200)); // Sesuaikan ukuran pie chart

        // Bungkus cardPanel dengan ExpandableCard
        ExpandableCard expandableCard = new ExpandableCard("", "", pieChart, "bottom");

        // Tambahkan cardPanel ke header ExpandableCard
        expandableCard.add(cardPanel, BorderLayout.NORTH);

        // Tambahkan ExpandableCard ke layout utama
        add(expandableCard, BorderLayout.NORTH);
    }

    private JPanel createCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(200, 100));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 150, 243));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(0, 150, 136));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private String formatToRupiah(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(amount).replace("Rp", "Rp."); // Replace default "Rp" with "Rp."
    }

    private void addDataToTable(Object[] rowData) {
        model.addRow(rowData); // Tambahkan baris ke model tabel
    }

    private void showDetailDialog(String tanggal) {
        JDialog detailDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Detail Transaksi - " + tanggal, Dialog.ModalityType.APPLICATION_MODAL);
        detailDialog.setSize(600, 400);
        detailDialog.setLayout(new BorderLayout());

        DefaultTableModel detailModel = new DefaultTableModel(new String[]{"Deskripsi", "Banyak", "Jenis"}, 0);
        JTable detailTable = new JTable(detailModel);

        QueryExecutor executor = new QueryExecutor();

        // Load Pemasukan
        String queryPemasukan = "CALL all_pemasukan_harian_detail(?)";
        List<Map<String, Object>> resultPemasukan = executor.executeSelectQuery(queryPemasukan, new Object[]{tanggal});
        for (Map<String, Object> result : resultPemasukan) {
            detailModel.addRow(new Object[]{
                result.get("deskripsi"), result.get("total"), "Pemasukan"
            });
        }

        // Load Pengeluaran
        String queryPengeluaran = "CALL all_pengeluaran_detail(?)";
        List<Map<String, Object>> resultPengeluaran = executor.executeSelectQuery(queryPengeluaran, new Object[]{tanggal});
        for (Map<String, Object> result : resultPengeluaran) {
            detailModel.addRow(new Object[]{
                result.get("keterangan"), result.get("total_pengeluaran"), "Pengeluaran"
            });
        }

        detailDialog.add(new JScrollPane(detailTable), BorderLayout.CENTER);

        JButton closeButton = new JButton("Tutup");
        closeButton.addActionListener(e -> detailDialog.dispose());
        detailDialog.add(closeButton, BorderLayout.SOUTH);

        detailDialog.setLocationRelativeTo(this);
        detailDialog.setVisible(true);
    }

    public void updateData(int[] incomeData, int[] outcomeData, String[] xLabels, String[] yLabels) {
        this.incomeData = incomeData;
        this.outcomeData = outcomeData;
        this.xLabels = xLabels;
        this.yLabels = yLabels;
        repaint(); // Render ulang chart dengan data baru
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
    
        // Periksa apakah array null atau kosong
        if (incomeData == null || outcomeData == null || incomeData.length == 0 || outcomeData.length == 0) {
            g2d.setColor(Color.BLACK);
            g2d.drawString("Tidak ada data untuk ditampilkan", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
    
        // Hitung total
        double totalPemasukan = Arrays.stream(incomeData).sum();
        double totalPengeluaran = Arrays.stream(outcomeData).sum();
        double total = totalPemasukan + totalPengeluaran;
    
        // Hitung sudut untuk setiap bagian
        int pemasukanAngle = (int) Math.round((totalPemasukan / total) * 360);
        int pengeluaranAngle = 360 - pemasukanAngle;
    
        // Gambar pie chart
        int diameter = Math.min(getWidth(), getHeight()) - 40; // Diameter lingkaran (kurangi margin)
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;
    
        // Pemasukan (warna hijau)
        g2d.setColor(new Color(0, 150, 136));
        g2d.fillArc(x, y, diameter, diameter, 0, pemasukanAngle);
    
        // Pengeluaran (warna merah)
        g2d.setColor(new Color(244, 67, 54));
        g2d.fillArc(x, y, diameter, diameter, pemasukanAngle, pengeluaranAngle);
    
        // Tambahkan label di luar lingkaran
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        int margin = 20; // Margin untuk label
        g2d.drawString("Pemasukan", x + diameter / 4, y - margin); // Label di atas
        g2d.drawString("Pengeluaran", x + 3 * diameter / 4 - 50, y + diameter + margin); // Label di bawah
    }
}
