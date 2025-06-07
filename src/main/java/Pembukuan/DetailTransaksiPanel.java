package Pembukuan;

import Components.CustomTable.CustomTable; // Import CustomTable
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

import DataBase.QueryExecutor;

public class DetailTransaksiPanel extends JPanel {

    public DetailTransaksiPanel(String tanggal) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        // Gunakan CustomTable sebagai pengganti JTable
        DefaultTableModel detailModel = new DefaultTableModel(new String[]{"Deskripsi", "Banyak", "Jenis"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Semua sel tidak bisa diedit
            }
        };
        CustomTable detailTable = new CustomTable(detailModel);

        // Nonaktifkan seleksi pada tabel
        detailTable.setRowSelectionAllowed(false);
        detailTable.setColumnSelectionAllowed(false);
        detailTable.setCellSelectionEnabled(false);

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

        // Tambahkan CustomTable ke JScrollPane
        add(new JScrollPane(detailTable), BorderLayout.CENTER);
    }

    public static void showModalCenter(JFrame parent, String tanggal) {
        DetailTransaksiPanel panel = new DetailTransaksiPanel(tanggal);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }
}