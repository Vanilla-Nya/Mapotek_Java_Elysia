package Pasien;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;

import Components.CustomDatePicker;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import Components.ShowModalCenter;
import DataBase.QueryExecutor;
import Helpers.OnPasienAddedListener;
import Helpers.TypeNumberHelper;

/**
 *
 * @author asuna
 */
public class RegisterPasien extends JPanel {

    private CustomTextField txtnik, txtAge, txtName, txtAddress, txtPhone, txtRFID;
    private Dropdown txtGender;
    private OnPasienAddedListener listener;
    private CustomDatePicker customDatePicker;

    public RegisterPasien(OnPasienAddedListener listener, DefaultTableModel model) {
        this.listener = listener;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(450, 400)); // Tambahkan ini

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("NIK: "), gbc);
        gbc.gridx = 1;
        txtnik = new CustomTextField("Masukan NIK", 20, 15, Optional.empty());
        ((AbstractDocument) txtnik.getTextField().getDocument()).setDocumentFilter(new TypeNumberHelper(16));
        formPanel.add(txtnik, gbc);

        // Add RFID KTP field below NIK
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("RFID KTP: "), gbc);
        gbc.gridx = 1;
        txtRFID = new CustomTextField("Masukan RFID KTP", 20, 15, Optional.empty());
        formPanel.add(txtRFID, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Nama Pasien:"), gbc);
        gbc.gridx = 1;
        txtName = new CustomTextField("Masukan Nama", 20, 15, Optional.empty());
        formPanel.add(txtName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Tanggal Lahir:"), gbc);
        gbc.gridx = 1;
        txtAge = new CustomTextField("Tanggal Lahir ", 20, 15, Optional.empty());
        customDatePicker = new CustomDatePicker(txtAge.getTextField(), false);
        txtAge.getTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                customDatePicker.showDatePicker(); // Show the date picker dialog
            }
        });
        formPanel.add(txtAge, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1;
        txtGender = new Dropdown(false, true, null);
        txtGender.setItems(List.of("Laki - Laki", "Perempuan", "Tidak Bisa Dijelaskan"), false, true, null);
        formPanel.add(txtGender, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Alamat"), gbc);
        gbc.gridx = 1;
        txtAddress = new CustomTextField("Masukan Alamat", 20, 15, Optional.empty());
        formPanel.add(txtAddress, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("No.Telp"), gbc);
        gbc.gridx = 1;
        txtPhone = new CustomTextField("Masukan No.telp", 20, 15, Optional.empty());
        ((AbstractDocument) txtPhone.getTextField().getDocument()).setDocumentFilter(new TypeNumberHelper(13));
        formPanel.add(txtPhone, gbc);

        // Submit button with RoundedButton
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton submitButton = new RoundedButton("Tambahkan");
        submitButton.setBackground(new Color(0, 150, 136));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(e -> {
            String id = String.valueOf(model.getRowCount() + 1);
            String nik = txtnik.getText();
            String rfid = txtRFID.getText().trim(); // Ambil nilai RFID
            String name = txtName.getText();
            String age = txtAge.getText();
            String gender = (String) txtGender.getSelectedItem();
            String address = txtAddress.getText();
            String phone = txtPhone.getText();

            // Validasi input sebelum menambahkan
            if (id.isEmpty() || nik.isEmpty() || name.isEmpty() || age == null || gender.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(RegisterPasien.this, "Semua field harus diisi kecuali RFID!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Jika RFID kosong, atur ke null
            if (rfid.isEmpty()) {
                rfid = null;
            }

            // Use DateTimeFormatter to parse the string into LocalDate
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate selectedBirthDate = null;
            try {
                selectedBirthDate = LocalDate.parse(age, formatter); // Parsing the date string
            } catch (Exception error) {
                JOptionPane.showMessageDialog(this, "Tanggal Lahir Tidak Valid", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return; // Exit if the date is not valid
            }

            LocalDate currentDate = LocalDate.now();

            if (selectedBirthDate.isAfter(currentDate)) {
                JOptionPane.showMessageDialog(this, "Tanggal Lahir Tidak Valid", "Peringatan", JOptionPane.WARNING_MESSAGE);
            } else {
                // Notify the listener with updated data
                if (listener != null) {
                    String checknik = "SELECT nik, nama FROM pasien WHERE nik = ?";
                    Object[] parameterCheck = new Object[]{nik};
                    java.util.List<Map<String, Object>> resultCheck = new QueryExecutor().executeSelectQuery(checknik, parameterCheck);
                    if (resultCheck.isEmpty()) {
                        String Query = "INSERT INTO pasien (nik, rfid, nama, jenis_kelamin, tanggal_lahir, no_telepon, alamat) VALUES (?,?,?,?,?,?,?)";
                        Object[] parameter = new Object[]{nik, rfid, name, gender, selectedBirthDate, phone, address};
                        Long isInserted = QueryExecutor.executeInsertQueryWithReturnID(Query, parameter);
                        if (isInserted != 404) {
                            Period period = Period.between(selectedBirthDate, currentDate);
                            String BirthDate = period.getYears() + " Tahun " + period.getMonths() + " Bulan " + period.getDays() + " Hari";
                            listener.onPasienAdded(isInserted.toString(), nik, name, BirthDate, gender, phone, address, rfid);
                            JOptionPane.showMessageDialog(this, "Insert Success with Name: " + name, "Success", JOptionPane.INFORMATION_MESSAGE);
                            System.out.println("Insert successful!");

                            // Tutup modal setelah proses selesai
                            ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(this));
                        } else {
                            JOptionPane.showMessageDialog(null, "Insert failed.", "Error", JOptionPane.ERROR_MESSAGE);
                            System.out.println("Insert failed.");
                        }
                    } else {
                        Map<String, Object> data = resultCheck.get(0);
                        JOptionPane.showMessageDialog(null, "NIK Sudah Terdaftar dengan Nama: " + data.get("nama"), "NIK Sudah Terdaftar", JOptionPane.ERROR_MESSAGE);
                        System.out.println("Insert failed.");
                    }
                }
            }
        });
        formPanel.add(submitButton, gbc);

        add(formPanel);
    }

    // Tambahkan static method untuk showModalCenter
    public static void showModalCenter(JFrame parent, OnPasienAddedListener listener, DefaultTableModel model) {
        RegisterPasien panel = new RegisterPasien(listener, model);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }
}
