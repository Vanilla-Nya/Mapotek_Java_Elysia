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
        setSize(800, 600);
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

        // Atur lebar kolom
        table.getColumnModel().getColumn(0).setPreferredWidth(70); // Lebar kolom "Kode ICDX" lebih kecil
        table.getColumnModel().getColumn(1).setPreferredWidth(500); // Lebar kolom "Deskripsi" lebih besar

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

    private void loadDataToMap() {
        if (icdxDataMap.isEmpty()) {
            try {
                QueryExecutor executor = new QueryExecutor();
                String query = "SELECT code, name_en FROM icds"; // Query untuk tabel ICDS
                List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

                for (Map<String, Object> row : results) {
                    String code = (String) row.get("code");
                    String nameEn = (String) row.get("name_en");
                    icdxDataMap.put(code, nameEn);
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

    public interface OnICDXSelectedListener {
        void onICDXSelected(Object[] selectedData);
    }
}
