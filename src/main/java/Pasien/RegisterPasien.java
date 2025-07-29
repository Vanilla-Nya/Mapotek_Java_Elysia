package Pasien;

import java.awt.BorderLayout;
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
import javax.swing.JComponent;
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

public class RegisterPasien extends JPanel {
    private CustomTextField txtnik, txtAge, txtName, txtAddress, txtPhone, txtRFID, txtBPJS[];
    private Dropdown txtGender;
    private OnPasienAddedListener listener;
    private CustomDatePicker customDatePicker;

    public RegisterPasien(OnPasienAddedListener listener, DefaultTableModel model) {
        this.listener = listener;
        setLayout(new BorderLayout());

        // Panel Pasien Umum
        JPanel umumPanel = createFormPanel(model, false);

        // Panel Pasien BPJS
        JPanel bpjsPanel = createFormPanel(model, true);

        // TabbedPane
        String[] titles = { "Pasien Umum", "Pasien BPJS" };
        JComponent[] contents = { umumPanel, bpjsPanel };
        Components.CustomTabbedPane tabbedPane = new Components.CustomTabbedPane(titles, contents);

        add(tabbedPane, BorderLayout.CENTER);
    }

    // Method untuk membuat panel form, isBPJS=true jika panel BPJS
    private JPanel createFormPanel(DefaultTableModel model, boolean isBPJS) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        if (isBPJS) {
            int row = 0;
            // Dropdown metode pencarian
            gbc.gridx = 0;
            gbc.gridy = row++;
            formPanel.add(new JLabel("Metode Pencarian BPJS:"), gbc);
            gbc.gridx = 0;
            gbc.gridy = row++;
            Dropdown searchMethodDropdown = new Dropdown(false, true, null);
            searchMethodDropdown.setItems(List.of( "","NIK", "Nama, Tanggal Lahir, Gender"), false, true, null);
            formPanel.add(searchMethodDropdown, gbc);

            // Panel untuk input pencarian
            JPanel searchInputPanel = new JPanel(new GridBagLayout());
            GridBagConstraints searchGbc = new GridBagConstraints();
            searchGbc.insets = new Insets(2, 2, 2, 2);

            // Field untuk masing-masing metode
            CustomTextField txtNIK = new CustomTextField("Masukan NIK", 20, 15, Optional.empty());
            CustomTextField txtNama = new CustomTextField("Masukan Nama", 20, 15, Optional.empty());
            CustomTextField txtTanggalLahir = new CustomTextField("Tanggal Lahir", 20, 15, Optional.empty());
            Dropdown txtGenderSearch = new Dropdown(false, true, null);
            txtGenderSearch.setItems(List.of("Laki - Laki", "Perempuan"), false, true, null);

            // --- Default kosong ---
            searchInputPanel.removeAll();
            searchInputPanel.revalidate();
            searchInputPanel.repaint();
            // --- END ---

            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.gridwidth = 2;
            formPanel.add(searchInputPanel, gbc);

            // Listener dropdown untuk ganti input
            searchMethodDropdown.addActionListener(e -> {
                String selectedMethod = (String) searchMethodDropdown.getSelectedItem();
                searchInputPanel.removeAll();
                searchGbc.gridx = 0; searchGbc.gridy = 0;
                if ("NIK".equals(selectedMethod)) {
                    searchInputPanel.add(new JLabel("NIK:"), searchGbc);
                    searchGbc.gridx = 1;
                    searchInputPanel.add(txtNIK, searchGbc);
                } else if ("Nama, Tanggal Lahir, Gender".equals(selectedMethod)) {
                    searchInputPanel.add(new JLabel("Nama:"), searchGbc);
                    searchGbc.gridx = 1;
                    searchInputPanel.add(txtNama, searchGbc);
                    searchGbc.gridx = 0; searchGbc.gridy = 1;
                    searchInputPanel.add(new JLabel("Tanggal Lahir:"), searchGbc);
                    searchGbc.gridx = 1;
                    searchInputPanel.add(txtTanggalLahir, searchGbc);
                    searchGbc.gridx = 0; searchGbc.gridy = 2;
                    searchInputPanel.add(new JLabel("Gender:"), searchGbc);
                    searchGbc.gridx = 1;
                    searchInputPanel.add(txtGenderSearch, searchGbc);
                }
                // Jika selectedMethod kosong, biarkan panel kosong
                searchInputPanel.revalidate();
                searchInputPanel.repaint();
            });

            // Tombol cari ke API SATUSEHAT
            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.gridwidth = 2;
            RoundedButton btnCariBPJS = new RoundedButton("Cari Data BPJS");
            btnCariBPJS.setBackground(new Color(33, 150, 243));
            btnCariBPJS.setForeground(Color.WHITE);
            formPanel.add(btnCariBPJS, gbc);

            btnCariBPJS.addActionListener(e -> {
                String method = (String) searchMethodDropdown.getSelectedItem();
                if (method == null || method.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Silakan pilih metode pencarian BPJS terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Lanjutkan proses pencarian sesuai metode...
            });

            return formPanel;
        }

        int row = 1;
        gbc.gridx = 0;
        gbc.gridy = row++;
        formPanel.add(new JLabel("NIK: "), gbc);
        gbc.gridx = 1;
        txtnik = new CustomTextField("Masukan NIK", 20, 15, Optional.empty());
        ((AbstractDocument) txtnik.getTextField().getDocument()).setDocumentFilter(new TypeNumberHelper(16));
        formPanel.add(txtnik, gbc);

        // Add RFID KTP field below NIK
        gbc.gridx = 0;
        gbc.gridy = row++;
        formPanel.add(new JLabel("RFID KTP: "), gbc);
        gbc.gridx = 1;
        txtRFID = new CustomTextField("Masukan RFID KTP", 20, 15, Optional.empty());
        formPanel.add(txtRFID, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        formPanel.add(new JLabel("Nama Pasien:"), gbc);
        gbc.gridx = 1;
        txtName = new CustomTextField("Masukan Nama", 20, 15, Optional.empty());
        formPanel.add(txtName, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
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
        gbc.gridy = row++;
        formPanel.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1;
        txtGender = new Dropdown(false, true, null);
        txtGender.setItems(List.of("Laki - Laki", "Perempuan", "Tidak Bisa Dijelaskan"), false, true, null);
        formPanel.add(txtGender, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        formPanel.add(new JLabel("Alamat"), gbc);
        gbc.gridx = 1;
        txtAddress = new CustomTextField("Masukan Alamat", 20, 15, Optional.empty());
        formPanel.add(txtAddress, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        formPanel.add(new JLabel("No.Telp"), gbc);
        gbc.gridx = 1;
        txtPhone = new CustomTextField("Masukan No.telp", 20, 15, Optional.empty());
        ((AbstractDocument) txtPhone.getTextField().getDocument()).setDocumentFilter(new TypeNumberHelper(13));
        formPanel.add(txtPhone, gbc);

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = row;
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
            String bpjs = isBPJS ? txtBPJS[0].getText().trim() : null;

            // Validasi input
            if (isBPJS && (bpjs == null || bpjs.isEmpty())) {
                JOptionPane.showMessageDialog(this, "Nomor BPJS wajib diisi untuk pasien BPJS!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validasi input sebelum menambahkan
            if (id.isEmpty() || nik.isEmpty() || name.isEmpty() || age == null || gender.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(RegisterPasien.this, "Semua field harus diisi kecuali RFID!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Jika RFID kosong, atur ke null
            if (rfid.isEmpty()) {
                rfid = null;
            }

            if (bpjs != null && bpjs.isEmpty()) bpjs = null; // Jika kosong, simpan null

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
                        String Query = "INSERT INTO pasien (nik, rfid, nama, jenis_kelamin, tanggal_lahir, no_telepon, alamat, nomor_bpjs) VALUES (?,?,?,?,?,?,?,?)";
                        Object[] parameter = new Object[]{nik, rfid, name, gender, selectedBirthDate, phone, address, bpjs};
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

        return formPanel;
    }

    // Tambahkan static method untuk showModalCenter
    public static void showModalCenter(JFrame parent, OnPasienAddedListener listener, DefaultTableModel model) {
        RegisterPasien panel = new RegisterPasien(listener, model);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }
}
