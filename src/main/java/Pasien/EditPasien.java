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
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.AbstractDocument;

import Components.CustomDatePicker;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import Helpers.TypeNumberHelper;

/**
 *
 */
public class EditPasien extends JPanel { // Ubah dari JFrame ke JPanel

    private CustomTextField txtnik, txtNamaPasien, txtUmur, txtNoTelp, txtAlamat, txtRFID;
    private Dropdown cbJenisKelamin;
    private OnPasienUpdatedListener listener;
    private CustomDatePicker customDatePicker;

    public EditPasien(String id, String nik, String name, String age, String gender, String phone, String address, String rfid, OnPasienUpdatedListener listener) {
        this.listener = listener;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 500)); // <-- Tambahkan ini

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // NIK Pasien
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("NIK:"), gbc);
        gbc.gridx = 1;
        txtnik = new CustomTextField("NIK", 20, 15, Optional.empty());
        formPanel.add(txtnik, gbc);

        // Nama Pasien
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Nama Pasien:"), gbc);
        gbc.gridx = 1;
        txtNamaPasien = new CustomTextField("Nama Pasien", 20, 15, Optional.empty());
        formPanel.add(txtNamaPasien, gbc);

        // Umur
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Umur:"), gbc);
        gbc.gridx = 1;
        txtUmur = new CustomTextField("Age", 20, 15, Optional.empty());
        customDatePicker = new CustomDatePicker(txtUmur.getTextField(), false);
        txtUmur.getTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                customDatePicker.showDatePicker(); // Show the date picker dialog
            }
        });
        formPanel.add(txtUmur, gbc);

        // Jenis Kelamin
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1;
        cbJenisKelamin = new Dropdown(false, false, gender);
        cbJenisKelamin.setItems(List.of("Laki - Laki", "Perempuan", "Tidak Bisa Dijelaskan"), false, false, gender);
        formPanel.add(cbJenisKelamin, gbc);

        // Alamat
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Alamat:"), gbc);
        gbc.gridx = 1;
        txtAlamat = new CustomTextField("Alamat", 20, 15, Optional.empty());
        formPanel.add(txtAlamat, gbc);

        // No Telp
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("No Telp:"), gbc);
        gbc.gridx = 1;
        txtNoTelp = new CustomTextField("No.TELP", 20, 15, Optional.empty());
        ((AbstractDocument) txtNoTelp.getTextField().getDocument()).setDocumentFilter(new TypeNumberHelper(13)); // Limit to 13 digits
        formPanel.add(txtNoTelp, gbc);

        // RFID field
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("RFID:"), gbc);
        gbc.gridx = 1;
        txtRFID = new CustomTextField("RFID", 20, 15, Optional.empty());
        formPanel.add(txtRFID, gbc);

        // Submit button with RoundedButton
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton btnSave = new RoundedButton("Simpan");
        btnSave.setBackground(new Color(0, 150, 136));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener((var e) -> {
            // Get the updated data from input fields
            String UpdateNIK = txtnik.getText();
            String updatedName = txtNamaPasien.getText();
            String updatedAge = txtUmur.getText();
            String updatedGender = (String) cbJenisKelamin.getSelectedItem();
            String updatedAddress = txtAlamat.getText();
            String updatedPhone = txtNoTelp.getText();
            String updatedRFID = txtRFID.getText();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate selectedBirthDate = null;
            try {
                selectedBirthDate = LocalDate.parse(updatedAge, formatter); // Parsing the date string
            } catch (Exception error) {
                JOptionPane.showMessageDialog(this, "Tanggal Lahir Tidak Valid", "Peringatan", JOptionPane.WARNING_MESSAGE);
                System.out.println(error);
                return; // Exit if the date is not valid
            }

            LocalDate currentDate = LocalDate.now();

            if (listener != null) {
                String Query = "UPDATE pasien SET nik = ?, nama = ?, jenis_kelamin = ?, tanggal_lahir = ?, no_telepon = ?, alamat = ?, rfid = ? WHERE id_pasien = ?";
                Object[] parameter = new Object[]{UpdateNIK, updatedName, updatedGender, updatedAge, updatedPhone, updatedAddress, updatedRFID, id};
                boolean isUpdated = QueryExecutor.executeUpdateQuery(Query, parameter);
                if (isUpdated) {
                    Period period = Period.between(selectedBirthDate, currentDate);
                    String BirthDate = period.getYears() + " Tahun " + period.getMonths() + " Bulan " + period.getDays() + " Hari";
                    listener.onPasienUpdated(UpdateNIK, updatedName, BirthDate, updatedGender, updatedPhone, updatedAddress, updatedRFID);
                    JOptionPane.showMessageDialog(this, "Update Success", "Success", JOptionPane.INFORMATION_MESSAGE);
                    System.out.println("Update successful!");
                } else {
                    JOptionPane.showMessageDialog(null, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Update failed.");
                }
            }

        });
        formPanel.add(btnSave, gbc);

        // Add form panel to main frame
        add(formPanel);

        // Set initial values
        txtNamaPasien.setText(name);
        // Parse the age string to extract years, months, and days
        String[] parts = age.split(" ");
        int years = Integer.parseInt(parts[0]);
        int months = Integer.parseInt(parts[2]);
        int days = Integer.parseInt(parts[4]);

        // Define the current date
        LocalDate currentDate = LocalDate.now();

        // Define the period representing the age
        Period agePeriod = Period.of(years, months, days);

        // Calculate the date of birth by subtracting the period from the current date
        LocalDate dateOfBirth = currentDate.minus(agePeriod);
        txtUmur.setText(dateOfBirth.toString());
        txtnik.setText(nik);
        cbJenisKelamin.getEditor().setItem(gender);
        txtNoTelp.setText(phone);
        txtAlamat.setText(address);
        txtRFID.setText(rfid);

        // Tambahkan static method untuk showModalCenter
    }

    public static void showModalCenter(JFrame parent, String id, String nik, String name, String age, String gender, String phone, String address, String rfid, OnPasienUpdatedListener listener) {
        EditPasien panel = new EditPasien(id, nik, name, age, gender, phone, address, rfid, listener);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }

    // Listener interface for updating the pasien data
    public interface OnPasienUpdatedListener {
        void onPasienUpdated(String nik, String name, String age, String gender, String phone, String address, String rfid);
    }
}
