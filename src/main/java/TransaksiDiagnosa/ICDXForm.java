package TransaksiDiagnosa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ICDXForm extends JFrame {

    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable table;
    private OnICDXSelectedListener listener;
    private List<Object[]> cachedData = null;
    private Map<String, String> icdxDataMap = new HashMap<>();

    public ICDXForm(OnICDXSelectedListener listener) {
        this.listener = listener;

        setTitle("Pencarian ICDX");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel pencarian
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Cari");
        searchButton.addActionListener(this::performSearch);
        searchPanel.add(new JLabel("Cari ICDX:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Tabel hasil pencarian
        String[] columns = {"Kode ICDX", "Deskripsi"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Tombol "Pilih"
        JButton selectButton = new JButton("Pilih");
        selectButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String kodeICDX = (String) tableModel.getValueAt(selectedRow, 0);
                String deskripsi = (String) tableModel.getValueAt(selectedRow, 1);
                listener.onICDXSelected(new Object[]{kodeICDX, deskripsi});
                dispose(); // Tutup form
            } else {
                JOptionPane.showMessageDialog(this, "Pilih salah satu data ICDX!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Tambahkan komponen ke frame
        add(searchPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private void loadDataToCache() {
        if (cachedData == null) {
            cachedData = readICDXFromExcel("assets/ICD-10.xlsx");
        }
    }

    private void loadDataToMap() {
        if (icdxDataMap.isEmpty()) {
            List<Object[]> data = readICDXFromExcel("assets/ICD-10.xlsx");
            for (Object[] row : data) {
                icdxDataMap.put((String) row[0], (String) row[1]);
            }
        }
    }

    private void performSearch(ActionEvent e) {
        String keyword = searchField.getText().trim();
        if (!keyword.isEmpty()) {
            loadDataToMap(); // Pastikan data sudah dimuat ke map

            // Filter data dari map
            List<Object[]> filteredResults = new ArrayList<>();
            for (Map.Entry<String, String> entry : icdxDataMap.entrySet()) {
                if (entry.getKey().contains(keyword) || entry.getValue().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredResults.add(new Object[]{entry.getKey(), entry.getValue()});
                }
            }

            // Tampilkan hasil pencarian di tabel
            tableModel.setRowCount(0); // Hapus data lama
            for (Object[] row : filteredResults) {
                tableModel.addRow(row);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Masukkan kata kunci untuk mencari ICDX!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

    private List<Object[]> readICDXFromExcel(String filePath) {
        List<Object[]> results = new ArrayList<>();
        try (InputStream fis = getClass().getClassLoader().getResourceAsStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
    
            // Ambil sheet pertama
            Sheet sheet = workbook.getSheetAt(0);
    
            // Iterasi melalui baris (mulai dari baris kedua untuk melewati header)
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Lewati header
    
                // Ambil data dari kolom
                Cell kodeCell = row.getCell(0);
                Cell deskripsiCell = row.getCell(1);
    
                String kode = kodeCell.getStringCellValue();
                String deskripsi = deskripsiCell.getStringCellValue();
    
                results.add(new Object[]{kode, deskripsi});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal membaca file Excel!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return results;
    }

    public interface OnICDXSelectedListener {
        void onICDXSelected(Object[] selectedData);
    }
}
