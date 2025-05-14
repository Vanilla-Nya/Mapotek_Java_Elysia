package TransaksiDiagnosa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import DataBase.QueryExecutor;

public class ICDIXForm extends JFrame {

    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable table;
    private OnICDIXSelectedListener listener;
    private List<Object[]> cachedData = null;
    private Map<String, String> icdxDataMap = new HashMap<>();

    public ICDIXForm(OnICDIXSelectedListener listener) {
        this.listener = listener;

        setTitle("Pencarian ICDIX");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel pencarian
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Cari");
        searchButton.addActionListener(this::performSearch);
        searchPanel.add(new JLabel("Cari ICDIX:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Tabel hasil pencarian
        String[] columns = {"Kode ICDIX", "Deskripsi"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Tombol "Pilih"
        JButton selectButton = new JButton("Pilih");
        selectButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String kodeICDIX = (String) tableModel.getValueAt(selectedRow, 0);
                String deskripsi = (String) tableModel.getValueAt(selectedRow, 1);
                listener.onICDIXSelected(new Object[]{kodeICDIX, deskripsi});
                dispose(); // Tutup form
            } else {
                JOptionPane.showMessageDialog(this, "Pilih salah satu data ICDIX!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Tambahkan komponen ke frame
        add(searchPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private void loadDataToMap() {
        if (icdxDataMap.isEmpty()) {
            try {
                QueryExecutor executor = new QueryExecutor();
                String query = "SELECT code, display FROM icd_ix"; // Query untuk tabel ICD-IX
                List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

                for (Map<String, Object> row : results) {
                    String code = (String) row.get("code");
                    String display = (String) row.get("display");
                    icdxDataMap.put(code, display);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal mengambil data dari database!", "Error", JOptionPane.ERROR_MESSAGE);
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

    public interface OnICDIXSelectedListener {
        void onICDIXSelected(Object[] selectedData);
    }
}
