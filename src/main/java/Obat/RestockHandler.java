package Obat;

import java.util.Map;

import javax.swing.JOptionPane;

import DataBase.QueryExecutor;

public class RestockHandler {

    public static void handleRestock(String namaObat, int stokBaru, String tanggalExpired) {
        QueryExecutor executor = new QueryExecutor();

        // Ambil stok lama dari database berdasarkan nama obat
        String queryGetStock = "SELECT stock FROM detail_obat WHERE id_obat = ? AND is_deleted = 0 ORDER BY created_at DESC LIMIT 1";
        // Ambil ID obat berdasarkan nama obat
        String queryGetId = "SELECT id_obat FROM obat WHERE nama_obat = ? AND is_deleted = 0 LIMIT 1";
        Object[] idParams = {namaObat};
        java.util.List<Map<String, Object>> idResults = executor.executeSelectQuery(queryGetId, idParams);

        if (idResults.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Obat tidak ditemukan di database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idObat = (int) idResults.get(0).get("id_obat");

        Object[] params = {idObat}; // Pastikan idObat adalah ID obat yang valid
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(queryGetStock, params);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Obat tidak ditemukan di database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ambil data stok lama
        Map<String, Object> obatData = results.get(0);
        int stokLama = (int) obatData.get("stock");

        // Bandingkan stok baru dengan stok lama
        if (stokBaru > stokLama) {
            // Jika stok baru lebih besar, masukkan ke tabel Detail_obat (obat masuk)
            int jumlahMasuk = stokBaru - stokLama;
            String queryInsertMasuk = "INSERT INTO detail_obat (id_obat, tanggal_expired, stock, is_deleted, created_at) VALUES (?, ?, ?, 0, NOW())";
            boolean success = executor.executeInsertQuery(queryInsertMasuk, new Object[]{idObat, tanggalExpired, jumlahMasuk});

            if (success) {
                JOptionPane.showMessageDialog(null, "Stok berhasil ditambahkan ke obat masuk.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Gagal menambahkan stok ke obat masuk.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (stokBaru < stokLama) {
            // Jika stok baru lebih kecil, masukkan ke tabel Pemeriksaan_obat (obat keluar)
            int jumlahKeluar = stokLama - stokBaru;
            String queryInsertKeluar = "INSERT INTO pemeriksaan_obat (id_obat, signa, jumlah, created_at) VALUES (?, 'Keluar', ?, NOW())";
            boolean success = executor.executeInsertQuery(queryInsertKeluar, new Object[]{idObat, jumlahKeluar});

            if (success) {
                JOptionPane.showMessageDialog(null, "Stok berhasil ditambahkan ke obat keluar.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Gagal menambahkan stok ke obat keluar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Stok tidak berubah.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}