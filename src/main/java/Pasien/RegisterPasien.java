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
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;

import API.ApiClient;
import Components.CustomDatePicker;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import Components.ShowModalCenter;
import DataBase.QueryExecutor;
import Helpers.OnPasienAddedListener;
import Helpers.TypeNumberHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class RegisterPasien extends JPanel {
    private CustomTextField txtnik, txtAge, txtName, txtAddress, txtPhone, txtRFID, txtBPJS[];
    private Dropdown cbProvinsi, cbKota, cbKecamatan, cbKelurahan;
    private Dropdown txtGender;
    private OnPasienAddedListener listener;
    private CustomDatePicker customDatePicker;

    // Tambahkan di class RegisterPasien
    private String hasilNIK, hasilNama, hasilTanggalLahir, hasilGender, hasilAlamat, hasilIdSatuSehat;

    public RegisterPasien(OnPasienAddedListener listener, DefaultTableModel model) {
        this.listener = listener;
        setLayout(new BorderLayout());

        // Panel Pasien Umum
        JPanel formPanel = createFormPanel(model, false);
        add(formPanel, BorderLayout.CENTER);
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

            // Tambahkan komponen untuk menampilkan hasil
            JTextArea hasilArea = new JTextArea(6, 30);
            hasilArea.setEditable(false);
            hasilArea.setBackground(new Color(245, 245, 245));
            hasilArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            hasilArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            hasilArea.setLineWrap(true);
            hasilArea.setWrapStyleWord(true);

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
                // Kosongkan hasilArea setiap kali dropdown diganti
                hasilArea.setText("");
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

            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.gridwidth = 2;
            formPanel.add(hasilArea, gbc);

            // Setelah formPanel.add(hasilArea, gbc);
            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.gridwidth = 2;
            RoundedButton btnSimpanDB = new RoundedButton("Simpan ke Database");
            btnSimpanDB.setBackground(new Color(76, 175, 80));
            btnSimpanDB.setForeground(Color.WHITE);
            formPanel.add(btnSimpanDB, gbc);

            btnCariBPJS.addActionListener(e -> {
                String method = (String) searchMethodDropdown.getSelectedItem();
                if (method == null || method.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Silakan pilih metode pencarian BPJS terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if ("NIK".equals(method)) {
                    String nik = txtNIK.getText().trim();
                    if (nik.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Masukkan NIK terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    // Panggil API SATUSEHAT by NIK
                    new Thread(() -> {
                        ApiClient api = new ApiClient();
                        try {
                            String identifier = "https://fhir.kemkes.go.id/id/nik|" + nik;
                            String response = api.get(api.encodeUrl("/Patient?identifier=", identifier));
                            tampilkanDataPasien(response, hasilArea); // <-- gunakan method yang sama
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SwingUtilities.invokeLater(() -> 
                                JOptionPane.showMessageDialog(this, "Gagal mengambil data dari API SATUSEHAT", "Error", JOptionPane.ERROR_MESSAGE)
                            );
                        } finally {
                            try { api.close(); } catch (Exception ignore) {}
                        }
                    }).start();
                } else if ("Nama, Tanggal Lahir, Gender".equals(method)) {
                    String nama = txtNama.getText().trim();
                    String tglLahir = txtTanggalLahir.getText().trim();
                    String gender = (String) txtGenderSearch.getSelectedItem();

                    if (nama.isEmpty() || tglLahir.isEmpty() || gender == null || gender.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Contoh query FHIR by nama, tanggal lahir, gender
                    new Thread(() -> {
                        ApiClient api = new ApiClient();
                        try {
                            // Sesuaikan parameter query dengan spesifikasi API SATUSEHAT
                            String url = "/Patient?name=" + 
                                java.net.URLEncoder.encode(nama, "UTF-8") +
                                "&birthdate=" + java.net.URLEncoder.encode(tglLahir, "UTF-8") +
                                "&gender=" + (gender.equalsIgnoreCase("Laki - Laki") ? "male" : "female");
                            String response = api.get(url);
                            tampilkanDataPasien(response, hasilArea);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SwingUtilities.invokeLater(() -> 
                                JOptionPane.showMessageDialog(this, "Gagal mengambil data dari API SATUSEHAT", "Error", JOptionPane.ERROR_MESSAGE)
                            );
                        } finally {
                            try { api.close(); } catch (Exception ignore) {}
                        }
                    }).start();
                }
            });

            // Listener tombol simpan
            btnSimpanDB.addActionListener(e -> {
                // Validasi data hasil pencarian
                if (hasilNIK == null || hasilNama == null || hasilGender == null || hasilAlamat == null) {
                    JOptionPane.showMessageDialog(this, "Tidak ada data pasien yang bisa disimpan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String tanggalLahir = (hasilTanggalLahir == null || hasilTanggalLahir.isEmpty()) ? "1970-01-01" : hasilTanggalLahir;

                String Query = "INSERT INTO pasien (id_satusehat, nik, nama, jenis_kelamin, tanggal_lahir, alamat, nomor_bpjs, no_telepon, status) VALUES (?,?,?,?,?,?,?,?,?)";
                Object[] parameter = new Object[]{hasilIdSatuSehat, hasilNIK, hasilNama, hasilGender, tanggalLahir, hasilAlamat, 0, 0, "BPJS"};
                Long isInserted = QueryExecutor.executeInsertQueryWithReturnID(Query, parameter);
                if (isInserted != 404) {
                    JOptionPane.showMessageDialog(this, "Data pasien berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                    // Panggil listener agar tabel refresh
                    if (listener != null) {
                        listener.onPasienAdded(
                            isInserted.toString(),
                            hasilNIK,
                            hasilNama,
                            tanggalLahir,
                            hasilGender,
                            "0", // no_telepon (default)
                            hasilAlamat,
                            null // rfid (default)
                        );
                    }

                    // Tutup modal
                    ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(this));
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan data pasien!", "Error", JOptionPane.ERROR_MESSAGE);
                }
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

        gbc.gridx = 0;
        gbc.gridy = row++;
        formPanel.add(new JLabel("Provinsi:"), gbc);
        gbc.gridx = 1;
        cbProvinsi = new Dropdown(false, true, null);
        cbProvinsi.setItems(getListProvinsi(), false, true, null); // List<String> nama provinsi
        formPanel.add(cbProvinsi, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        formPanel.add(new JLabel("Kota/Kabupaten:"), gbc);
        gbc.gridx = 1;
        cbKota = new Dropdown(false, true, null);
        formPanel.add(cbKota, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        formPanel.add(new JLabel("Kecamatan:"), gbc);
        gbc.gridx = 1;
        cbKecamatan = new Dropdown(false, true, null);
        formPanel.add(cbKecamatan, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        formPanel.add(new JLabel("Kelurahan/Desa:"), gbc);
        gbc.gridx = 1;
        cbKelurahan = new Dropdown(false, true, null);
        formPanel.add(cbKelurahan, gbc);

        // Tambahkan ActionListener untuk dropdown provinsi, kota, kecamatan
        cbProvinsi.addActionListener(e -> {
            String kodeProvinsi = getKodeProvinsi(cbProvinsi.getSelectedItem().toString());
            if (kodeProvinsi.isEmpty()) {
                cbKota.setItems(List.of(), false, true, null);
                cbKecamatan.setItems(List.of(), false, true, null);
                cbKelurahan.setItems(List.of(), false, true, null);
                return;
            }
            cbKota.setItems(getListKota(kodeProvinsi), false, true, null);
            cbKecamatan.setItems(List.of(), false, true, null);
            cbKelurahan.setItems(List.of(), false, true, null);
        });

        cbKota.addActionListener(e -> {
            String kodeProvinsi = getKodeProvinsi(cbProvinsi.getSelectedItem().toString());
            String kodeKota = getKodeKota(cbKota.getSelectedItem().toString(), kodeProvinsi);
            if (kodeKota.isEmpty()) {
                cbKecamatan.setItems(List.of(), false, true, null);
                cbKelurahan.setItems(List.of(), false, true, null);
                return;
            }
            List<String> kecamatanList = getListKecamatan(kodeProvinsi, kodeKota);
            System.out.println("Isi kecamatan: " + kecamatanList);
            cbKecamatan.setItems(kecamatanList, false, true, null);
            cbKelurahan.setItems(List.of(), false, true, null);
        });

        cbKecamatan.addActionListener(e -> {
            String kodeProvinsi = getKodeProvinsi(cbProvinsi.getSelectedItem() != null ? cbProvinsi.getSelectedItem().toString() : "");
            String kodeKota = getKodeKota(cbKota.getSelectedItem() != null ? cbKota.getSelectedItem().toString() : "", kodeProvinsi);
            String kodeKecamatan = getKodeKecamatan(cbKecamatan.getSelectedItem() != null ? cbKecamatan.getSelectedItem().toString() : "", kodeProvinsi, kodeKota);

            if (kodeProvinsi.isEmpty() || kodeKota.isEmpty() || kodeKecamatan.isEmpty()) {
                cbKelurahan.setItems(List.of(), false, true, null);
                return;
            }

            String kodeKotaLokal = getKodeKotaLokal(cbKota.getSelectedItem().toString(), kodeProvinsi);
            String kodeKecamatanLokal = getKodeKecamatanLokal(cbKecamatan.getSelectedItem().toString(), kodeProvinsi, kodeKotaLokal);
            cbKelurahan.setItems(getListKelurahan(kodeProvinsi, kodeKotaLokal, kodeKecamatanLokal), false, true, null);
        });

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton submitButton = new RoundedButton("Tambahkan");
        submitButton.setBackground(new Color(0, 150, 136));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(e -> {
            String nik = txtnik.getText().trim();
            String name = txtName.getText().trim();
            String birthDate = txtAge.getText().trim();
            String gender = (String) txtGender.getSelectedItem();
            String address = txtAddress.getText().trim();
            String phone = txtPhone.getText().trim();

            if (nik.isEmpty() || name.isEmpty() || birthDate.isEmpty() || gender.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Ambil kode wilayah dari dropdown
            String kodeProvinsi = getKodeProvinsi(cbProvinsi.getSelectedItem() != null ? cbProvinsi.getSelectedItem().toString() : "");
            String kodeKota = getKodeKota(cbKota.getSelectedItem() != null ? cbKota.getSelectedItem().toString() : "", kodeProvinsi);
            String kodeKecamatan = getKodeKecamatan(cbKecamatan.getSelectedItem() != null ? cbKecamatan.getSelectedItem().toString() : "", kodeProvinsi, kodeKota);
            String kodeKelurahan = getKodeKelurahan(cbKelurahan.getSelectedItem() != null ? cbKelurahan.getSelectedItem().toString() : "", kodeProvinsi, kodeKota, kodeKecamatan);

            // Ambil string tanggal dari field input (misal dari date picker)
            String inputDate = txtAge.getText().trim();
            final String birthDateSatusehat;
            try {
                if (inputDate.contains("/")) {
                    // Format dd/MM/yyyy
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate date = LocalDate.parse(inputDate, inputFormatter);
                    birthDateSatusehat = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else if (inputDate.contains("-")) {
                    // Format yyyy-MM-dd
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date = LocalDate.parse(inputDate, inputFormatter);
                    birthDateSatusehat = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else {
                    throw new Exception("Format tanggal tidak dikenali!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Format tanggal lahir salah! Gunakan dd/MM/yyyy atau yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new Thread(() -> {
                ApiClient api = new ApiClient();
                try {
                    // 1. Cari pasien by NIK
                    String identifier = "https://fhir.kemkes.go.id/id/nik|" + nik;
                    String response = api.get(api.encodeUrl("/Patient?identifier=", identifier));
                    JSONObject json = new JSONObject(response);
                    JSONArray entries = json.optJSONArray("entry");

                    if (entries != null && entries.length() > 0) {
                        // Pasien ditemukan by NIK
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Pasien sudah terdaftar di SATUSEHAT!", "Info", JOptionPane.INFORMATION_MESSAGE));
                        // ...ambil id_satusehat, simpan ke DB lokal...
                    } else {
                        // 2. Cari pasien by Nama, Tgl Lahir, Gender
                        String url = "/Patient?name=" + java.net.URLEncoder.encode(name, "UTF-8")
                            + "&birthdate=" + java.net.URLEncoder.encode(birthDate, "UTF-8")
                            + "&gender=" + (gender.equalsIgnoreCase("Laki - Laki") ? "male" : "female");
                        String response2 = api.get(url);
                        JSONObject json2 = new JSONObject(response2);
                        JSONArray entries2 = json2.optJSONArray("entry");

                        if (entries2 != null && entries2.length() > 0) {
                            // Pasien ditemukan by Nama, Tgl Lahir, Gender
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Pasien sudah terdaftar di SATUSEHAT!", "Info", JOptionPane.INFORMATION_MESSAGE));
                            // ...ambil id_satusehat, simpan ke DB lokal...
                        } else {
                            // 3. Tidak ditemukan, lakukan POST ke SATUSEHAT
                            Map<String, Object> patient = new HashMap<>();
                            patient.put("resourceType", "Patient");
                            patient.put("identifier", new Object[]{
                                Map.of("system", "https://fhir.kemkes.go.id/id/nik", "value", nik)
                            });
                            patient.put("name", new Object[]{
                                Map.of("text", name)
                            });
                            patient.put("birthDate", birthDateSatusehat);
                            patient.put("gender", gender.equalsIgnoreCase("Laki - Laki") ? "male" : "female");

                            Map<String, Object> addressMap = new HashMap<>();
                            addressMap.put("use", "home");
                            addressMap.put("line", new Object[]{address}); // address = input user, misal "Jl. Contoh No. 123"
                            addressMap.put("city", "KOTA BANDUNG");
                            addressMap.put("postalCode", "40123");
                            addressMap.put("country", "ID");

                            // Extension array untuk kode wilayah administrasi
                            addressMap.put("extension", new Object[]{
                                Map.of(
                                    "url", "https://fhir.kemkes.go.id/r4/StructureDefinition/administrativeCode-province",
                                    "valueCode", kodeProvinsi // dari input user
                                ),
                                Map.of(
                                    "url", "https://fhir.kemkes.go.id/r4/StructureDefinition/administrativeCode-city",
                                    "valueCode", kodeKota // dari input user
                                ),
                                Map.of(
                                    "url", "https://fhir.kemkes.go.id/r4/StructureDefinition/administrativeCode-district",
                                    "valueCode", kodeKecamatan // dari input user
                                ),
                                Map.of(
                                    "url", "https://fhir.kemkes.go.id/r4/StructureDefinition/administrativeCode-village",
                                    "valueCode", kodeKelurahan // dari input user
                                )
                            });

                            // Masukkan ke array address di body Patient
                            patient.put("address", new Object[]{addressMap});

                            patient.put("telecom", new Object[]{
                                Map.of("system", "phone", "value", phone)
                            });
                            patient.put("multipleBirthBoolean", false);

                            String postResponse = api.post("/Patient", patient);
                            System.out.println("Response POST Patient: " + postResponse);

                            JSONObject postJson = new JSONObject(postResponse);
                            if (postJson.has("id")) {
                                String idSatusehat = postJson.getString("id");
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(this, 
                                        "Pasien berhasil didaftarkan ke SATUSEHAT!\nID SATUSEHAT: " + idSatusehat, 
                                        "Sukses", 
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                });
                                // ...ambil id_satusehat, simpan ke DB lokal...
                            } else {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Gagal mendaftarkan pasien ke SATUSEHAT!", "Error", JOptionPane.ERROR_MESSAGE));
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Gagal proses ke SATUSEHAT", "Error", JOptionPane.ERROR_MESSAGE));
                } finally {
                    try { api.close(); } catch (Exception ignore) {}
                }
            }).start();
        });
        formPanel.add(submitButton, gbc);

        return formPanel;
    }

    // Tambahkan static method untuk showModalCenter
    public static void showModalCenter(JFrame parent, OnPasienAddedListener listener, DefaultTableModel model) {
        RegisterPasien panel = new RegisterPasien(listener, model);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }

    public void tampilkanDataPasien(String responseString, JTextArea hasilArea) {
        JSONObject response = new JSONObject(responseString);

        JSONArray entries = response.optJSONArray("entry");
        if (entries != null && entries.length() > 0) {
            JSONObject resource = entries.getJSONObject(0).getJSONObject("resource");

            // Ambil id SATUSEHAT
            hasilIdSatuSehat = resource.optString("id", null);

            hasilNama = resource.getJSONArray("name").getJSONObject(0).optString("text");
            hasilTanggalLahir = resource.optString("birthDate");
            String genderApi = resource.optString("gender");
            // Mapping gender
            if ("male".equalsIgnoreCase(genderApi)) {
                hasilGender = "Laki - Laki";
            } else if ("female".equalsIgnoreCase(genderApi)) {
                hasilGender = "Perempuan";
            } else {
                hasilGender = "Tidak Bisa Dijelaskan";
            }

            hasilAlamat = "";
            if (resource.has("address")) {
                JSONObject address = resource.getJSONArray("address").getJSONObject(0);
                hasilAlamat = address.getJSONArray("line").getString(0);
            }

            hasilNIK = "";
            JSONArray identifiers = resource.getJSONArray("identifier");
            for (int i = 0; i < identifiers.length(); i++) {
                JSONObject id = identifiers.getJSONObject(i);
                if (id.getString("system").equals("https://fhir.kemkes.go.id/id/nik")) {
                    hasilNIK = id.getString("value");
                    break;
                }
            }

            String hasil = "=== Data Pasien ===\n"
                    + "Nama         : " + hasilNama + "\n"
                    + "Tanggal Lahir: " + hasilTanggalLahir + "\n"
                    + "Jenis Kelamin: " + hasilGender + "\n"
                    + "NIK          : " + hasilNIK + "\n"
                    + "Alamat       : " + hasilAlamat;
            System.out.println(hasil);

            SwingUtilities.invokeLater(() -> hasilArea.setText(hasil));
        } else {
            hasilIdSatuSehat = null;
            hasilNIK = hasilNama = hasilTanggalLahir = hasilGender = hasilAlamat = null;
            String hasil = "⚠️ Tidak ada data pasien ditemukan.";
            System.out.println(hasil);
            SwingUtilities.invokeLater(() -> hasilArea.setText(hasil));
        }
    }

    // Dummy data, ganti dengan data asli dari referensi wilayah
    private List<String> getListProvinsi() {
        try {
            String jsonStr = Files.readString(Paths.get("src/main/resources/WilayahIndonesia/provinsi/provinsi.json"));
            JSONObject obj = new JSONObject(jsonStr);
            List<String> provinsiList = new ArrayList<>();
            for (String key : obj.keySet()) {
                provinsiList.add(obj.getString(key));
            }
            return provinsiList;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    private String getKodeProvinsi(String namaProvinsi) {
        try {
            String jsonStr = Files.readString(Paths.get("src/main/resources/WilayahIndonesia/provinsi/provinsi.json"));
            JSONObject obj = new JSONObject(jsonStr);
            for (String key : obj.keySet()) {
                if (obj.getString(key).equalsIgnoreCase(namaProvinsi)) {
                    return key;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    private List<String> getListKota(String kodeProvinsi) {
        try {
            String path = "src/main/resources/WilayahIndonesia/kabupaten_kota/kab-" + kodeProvinsi + ".json";
            JSONObject obj = new JSONObject(Files.readString(Paths.get(path)));
            List<String> kotaList = new ArrayList<>();
            for (String key : obj.keySet()) {
                kotaList.add(obj.getString(key));
            }
            return kotaList;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    private String getKodeKota(String namaKota, String kodeProvinsi) {
        try {
            String jsonStr = Files.readString(Paths.get("src/main/resources/WilayahIndonesia/kabupaten_kota/kab-" + kodeProvinsi + ".json"));
            JSONObject obj = new JSONObject(jsonStr);
            for (String key : obj.keySet()) {
                if (obj.getString(key).equalsIgnoreCase(namaKota)) {
                    return key;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    private String getKodeKotaLokal(String namaKota, String kodeProvinsi) {
        try {
            String jsonStr = Files.readString(Paths.get("src/main/resources/WilayahIndonesia/kabupaten_kota/kab-" + kodeProvinsi + ".json"));
            JSONObject obj = new JSONObject(jsonStr);
            for (String key : obj.keySet()) {
                if (obj.getString(key).equalsIgnoreCase(namaKota)) {
                    return key; // kode lokal, misal "02"
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    private List<String> getListKecamatan(String kodeProvinsi, String kodeKota) {
        try {
            // Coba beberapa kemungkinan nama file
            String[] possiblePaths = {
                "src/main/resources/WilayahIndonesia/kecamatan/kec-" + kodeProvinsi + "-" + kodeKota + ".json",
                "src/main/resources/WilayahIndonesia/kecamatan/kec-" + kodeKota + ".json",
                "src/main/resources/WilayahIndonesia/kecamatan/kec-" + kodeProvinsi + "-" + kodeKota.substring(2) + ".json"
            };
            for (String path : possiblePaths) {
                if (Files.exists(Paths.get(path))) {
                    System.out.println("Path kecamatan: " + path);
                    JSONObject obj = new JSONObject(Files.readString(Paths.get(path)));
                    List<String> kecamatanList = new ArrayList<>();
                    for (String key : obj.keySet()) {
                        kecamatanList.add(obj.getString(key));
                    }
                    return kecamatanList;
                }
            }
            System.out.println("Tidak ditemukan file kecamatan untuk kode: " + kodeKota + " provinsi: " + kodeProvinsi);
            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    private String getKodeKecamatan(String namaKecamatan, String kodeProvinsi, String kodeKota) {
        try {
            // Mendapatkan kode kota lokal dari nama kota dan kode provinsi
            String kodeKotaLokal = getKodeKotaLokal(cbKota != null && cbKota.getSelectedItem() != null ? cbKota.getSelectedItem().toString() : "", kodeProvinsi);
            // Mendapatkan kode kecamatan lokal dari nama kecamatan, kode provinsi, dan kode kota lokal
            String kodeKecamatanLokal = getKodeKecamatanLokal(namaKecamatan, kodeProvinsi, kodeKotaLokal);
            String path = "src/main/resources/WilayahIndonesia/kelurahan_desa/keldesa-" + kodeProvinsi + "-" + kodeKotaLokal + "-" + kodeKecamatanLokal + ".json";
            JSONObject obj = new JSONObject(Files.readString(Paths.get(path)));
            for (String key : obj.keySet()) {
                if (obj.getString(key).equalsIgnoreCase(namaKecamatan)) {
                    return key;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    private String getKodeKecamatanLokal(String namaKecamatan, String kodeProvinsi, String kodeKotaLokal) {
        try {
            String path = "src/main/resources/WilayahIndonesia/kecamatan/kec-" + kodeProvinsi + "-" + kodeKotaLokal + ".json";
            JSONObject obj = new JSONObject(Files.readString(Paths.get(path)));
            for (String key : obj.keySet()) {
                if (obj.getString(key).equalsIgnoreCase(namaKecamatan)) {
                    return key; // kode lokal, misal "010"
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    private String getKodeKelurahan(String namaKelurahan, String kodeProvinsi, String kodeKota, String kodeKecamatan) {
        try {
            String path = "src/main/resources/WilayahIndonesia/kelurahan_desa/keldesa-" + kodeProvinsi + "-" + kodeKota + "-" + kodeKecamatan + ".json";
            JSONObject obj = new JSONObject(Files.readString(Paths.get(path)));
            for (String key : obj.keySet()) {
                if (obj.getString(key).equalsIgnoreCase(namaKelurahan)) {
                    return key;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    private List<String> getListKelurahan(String kodeProvinsi, String kodeKotaLokal, String kodeKecamatanLokal) {
        try {
            String path = "src/main/resources/WilayahIndonesia/kelurahan_desa/keldesa-" + kodeProvinsi + "-" + kodeKotaLokal + "-" + kodeKecamatanLokal + ".json";
            JSONObject obj = new JSONObject(Files.readString(Paths.get(path)));
            List<String> kelurahanList = new ArrayList<>();
            for (String key : obj.keySet()) {
                kelurahanList.add(obj.getString(key));
            }
            return kelurahanList;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
