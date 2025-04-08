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
import java.util.UUID;

public class Pengeluaran extends JFrame {
    private CustomTextField txtTanggal, txtKeterangan, txtTotalPengeluaran;
    private CustomDatePicker customDatePicker;
    private Pembukuan pembukuanPanel;

    // Constructor
    public Pengeluaran(Pembukuan pembukuanPanel) {
        setTitle("Tambah Pengeluaran");
        setSize(450, 400);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
            double totalPengeluaran = Double.parseDouble(txtTotalPengeluaran.getText());
            String jenis = "Pengeluaran";

            // Simple validation
            if (tanggal.isEmpty() || keterangan.isEmpty() || txtTotalPengeluaran.getText().isEmpty()) {
                JOptionPane.showMessageDialog(Pengeluaran.this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            UserSessionCache cache = new UserSessionCache();
            String uuid = (String) cache.getUUID();

            if (uuid != null) {
                // Prepare the insert query
                String query = "INSERT INTO pengeluaran (tanggal, keterangan, id_user) VALUES (?, ?, ?)";

                // Execute the insert query
                QueryExecutor executor = new QueryExecutor();
                int getId = (int) QueryExecutor.executeInsertQueryWithReturnID(query, new Object[]{tanggal, keterangan, uuid});

                // Check if the insertion was successful
                if (getId != 0) {
                    int idjenispengeluraran = 1;
                    Object[] insert = new Object[]{getId, idjenispengeluraran, keterangan, totalPengeluaran};
                    String queryPengeleuaranDetail = "INSERT INTO pengeluaran_detail(id_pengeluaran, id_jenis_pengeluaran, keterangan, total) VALUES (?,?,?,?)";
                    boolean isInsertDetail = QueryExecutor.executeInsertQuery(queryPengeleuaranDetail, insert);

                    if (isInsertDetail) {
                        JOptionPane.showMessageDialog(this, "Pengeluaran berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose();  // Close the current Pengeluaran form
                        
                        // Notify Pembukuan to refresh the table
                        if (pembukuanPanel != null) {
                            pembukuanPanel.refreshTable(); // Call the refreshTable method
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menambahkan pengeluaran.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }                
            } else {
                JOptionPane.showMessageDialog(null, "Dsta Obat Gagal Simpan, User Belum Login", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        });
        formPanel.add(submitButton, gbc);

        add(formPanel);
        setLocationRelativeTo(null); // Center the form on the screen

        // Make sure the form doesn't auto-focus on any text field
        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow(); // Request focus for the frame (not any specific component)
        });

        setVisible(true);
    }
}

