package Pengeluaran;

import Components.RoundedButton;
import Components.CustomTextField;
import Components.CustomDatePicker;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Pembukuan.Pembukuan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class Pengeluaran extends JPanel {
    private CustomTextField txtTanggal, txtKeterangan, txtTotalPengeluaran;
    private CustomDatePicker customDatePicker;
    private Pembukuan pembukuanPanel;

    // Constructor
    public Pengeluaran(Pembukuan pembukuanPanel) {
        this.pembukuanPanel = pembukuanPanel;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Tanggal Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tanggal:"), gbc);
        gbc.gridx = 1;
        txtTanggal = new CustomTextField("Masukan Tanggal", 20, 15, Optional.empty());

        // Initialize CustomDatePicker with the CustomTextField
        customDatePicker = new CustomDatePicker(txtTanggal.getTextField(), false);

        // Show date picker when the user clicks on the text field
        txtTanggal.getTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                customDatePicker.showDatePicker(); // Show the custom date picker
            }
        });

        formPanel.add(txtTanggal, gbc);

        // Keterangan Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Keterangan:"), gbc);
        gbc.gridx = 1;
        txtKeterangan = new CustomTextField("Masukan Keterangan", 20, 15, Optional.empty());
        formPanel.add(txtKeterangan, gbc);

        // Total Pengeluaran Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Total Pengeluaran:"), gbc);
        gbc.gridx = 1;
        txtTotalPengeluaran = new CustomTextField("Masukan Total Pengeluaran", 20, 15, Optional.empty());
        formPanel.add(txtTotalPengeluaran, gbc);

        // Submit Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton submitButton = new RoundedButton("Tambah Pengeluaran");
        submitButton.setBackground(new Color(0, 150, 136));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(e -> {
            String tanggal = txtTanggal.getText();
            String keterangan = txtKeterangan.getText();
            String totalPengeluaranText = txtTotalPengeluaran.getText();

            // Validasi sederhana
            if (tanggal.isEmpty() || keterangan.isEmpty() || totalPengeluaranText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double totalPengeluaran = Double.parseDouble(totalPengeluaranText);

                UserSessionCache cache = new UserSessionCache();
                String uuid = (String) cache.getUUID();

                if (uuid != null) {
                    // Query untuk menyimpan pengeluaran
                    String query = "INSERT INTO pengeluaran (tanggal, keterangan, id_user) VALUES (?, ?, ?)";
                    QueryExecutor executor = new QueryExecutor();
                    int getId = (int) QueryExecutor.executeInsertQueryWithReturnID(query, new Object[]{tanggal, keterangan, uuid});

                    if (getId != 0) {
                        int idJenisPengeluaran = 1;
                        Object[] insert = new Object[]{getId, idJenisPengeluaran, keterangan, totalPengeluaran};
                        String queryPengeluaranDetail = "INSERT INTO pengeluaran_detail(id_pengeluaran, id_jenis_pengeluaran, keterangan, total) VALUES (?,?,?,?)";
                        boolean isInsertDetail = QueryExecutor.executeInsertQuery(queryPengeluaranDetail, insert);

                        if (isInsertDetail) {
                            JOptionPane.showMessageDialog(this, "Pengeluaran berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);

                            // Refresh tabel di Pembukuan
                            if (pembukuanPanel != null) {
                                pembukuanPanel.refreshTable();
                            }

                            // Tutup modal
                            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                            Components.ShowModalCenter.closeCenterModal(parentFrame);
                        } else {
                            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menambahkan pengeluaran.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Data Pengeluaran Gagal Simpan, User Belum Login", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Total pengeluaran harus berupa angka!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });
        formPanel.add(submitButton, gbc);

        add(formPanel);
    }

    // Static method to show modal
    public static void showModalCenter(JFrame parent, Pembukuan pembukuanPanel) {
        Pengeluaran panel = new Pengeluaran(pembukuanPanel);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }
}

