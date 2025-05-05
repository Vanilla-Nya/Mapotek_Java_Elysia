package Obat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Components.CustomDatePicker;
import Components.CustomTextField;
import DataBase.QueryExecutor;

public class Restock {

    public static void showRestockDialog(String idObat, String idDetailObat, String namaObat, String jenisObat, String stokLama, String tanggalExpiredLama, String hargaBeliLama, String hargaJualLama) {
        // Panel kiri: Data lama
        JPanel leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        leftPanel.add(new JLabel("Nama Obat (Lama):"), gbc);
        gbc.gridx = 1;
        JTextField namaObatField = new JTextField(namaObat);
        namaObatField.setEditable(false);
        leftPanel.add(namaObatField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        leftPanel.add(new JLabel("Jenis Obat (Lama):"), gbc);
        gbc.gridx = 1;
        JTextField jenisObatField = new JTextField(jenisObat);
        jenisObatField.setEditable(false);
        leftPanel.add(jenisObatField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        leftPanel.add(new JLabel("Tanggal Expired (Lama):"), gbc);
        gbc.gridx = 1;
        JTextField tanggalExpiredLamaField = new JTextField(tanggalExpiredLama);
        tanggalExpiredLamaField.setEditable(false);
        leftPanel.add(tanggalExpiredLamaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        leftPanel.add(new JLabel("Stok (Lama):"), gbc);
        gbc.gridx = 1;
        JTextField stokLamaField = new JTextField(stokLama);
        stokLamaField.setEditable(false);
        leftPanel.add(stokLamaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        leftPanel.add(new JLabel("Harga Beli (Lama):"), gbc);
        gbc.gridx = 1;
        JTextField hargaBeliLamaField = new JTextField(hargaBeliLama);
        hargaBeliLamaField.setEditable(false);
        leftPanel.add(hargaBeliLamaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        leftPanel.add(new JLabel("Harga Jual (Lama):"), gbc);
        gbc.gridx = 1;
        JTextField hargaJualLamaField = new JTextField(hargaJualLama);
        hargaJualLamaField.setEditable(false);
        leftPanel.add(hargaJualLamaField, gbc);

        // Panel kanan: Input data baru
        JPanel rightPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        rightPanel.add(new JLabel("Stok (Baru):"), gbc);
        gbc.gridx = 1;
        JTextField stokBaruField = new JTextField();
        rightPanel.add(stokBaruField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        rightPanel.add(new JLabel("Tanggal Expired (Baru):"), gbc);
        gbc.gridx = 1;
        CustomTextField tanggalExpiredBaruField = new CustomTextField("Pilih tanggal expired", 20, 15, Optional.empty());
        CustomDatePicker datePicker = new CustomDatePicker(tanggalExpiredBaruField.getTextField(), true);
        tanggalExpiredBaruField.getTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                datePicker.showDatePicker(); // Tampilkan DatePicker
            }
        });
        rightPanel.add(tanggalExpiredBaruField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        rightPanel.add(new JLabel("Harga Beli (Baru):"), gbc);
        gbc.gridx = 1;
        JTextField hargaBeliBaruField = new JTextField();
        rightPanel.add(hargaBeliBaruField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        rightPanel.add(new JLabel("Harga Jual (Baru):"), gbc);
        gbc.gridx = 1;
        JTextField hargaJualBaruField = new JTextField();
        rightPanel.add(hargaJualBaruField, gbc);

        // Panel utama: Gabungkan panel kiri dan kanan
        JPanel mainPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(leftPanel, gbc);
        gbc.gridx = 1;
        mainPanel.add(rightPanel, gbc);

        // Tampilkan dialog
        int result = JOptionPane.showConfirmDialog(null, mainPanel, "Restock Obat", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int stokBaru = Integer.parseInt(stokBaruField.getText());
                String tanggalExpiredBaru = tanggalExpiredBaruField.getText();
                double hargaBeliBaru = Double.parseDouble(hargaBeliBaruField.getText());
                double hargaJualBaru = Double.parseDouble(hargaJualBaruField.getText());

                QueryExecutor executor = new QueryExecutor();

                // 1. Tambahkan data baru ke detail_obat
                String insertQuery = "INSERT INTO detail_obat (tanggal_expired, stock, harga_beli, harga_jual, status_batch) " +
                                     "VALUES (?, ?, ?, ?, 'aktif')";
                executor.executeInsertQuery(insertQuery, new Object[]{idObat, tanggalExpiredBaru, stokBaru, hargaBeliBaru, hargaJualBaru});

                // 2. Perbarui status entri lama menjadi 'diganti'
                String updateQuery = "UPDATE detail_obat SET status_batch = 'diganti' WHERE id_detail_obat = ?";
                executor.executeUpdateQuery(updateQuery, new Object[]{idDetailObat});

                JOptionPane.showMessageDialog(null, "Restock berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Masukkan data yang valid.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
