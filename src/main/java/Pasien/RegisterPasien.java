package Pasien;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import API.ApiClient;
import Components.CustomDatePicker;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import Components.ShowModalCenter;
import DataBase.QueryExecutor;
import Helpers.OnPasienAddedListener;
import Helpers.Region;
import Helpers.TypeNumberHelper;

public class RegisterPasien extends JPanel {
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Region region = new Region();
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
            searchMethodDropdown.setItems(List.of("", "NIK", "Nama, Tanggal Lahir, Gender"), false, true, null);
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
                searchGbc.gridx = 0;
                searchGbc.gridy = 0;
                if ("NIK".equals(selectedMethod)) {
                    searchInputPanel.add(new JLabel("NIK:"), searchGbc);
                    searchGbc.gridx = 1;
                    searchInputPanel.add(txtNIK, searchGbc);
                } else if ("Nama, Tanggal Lahir, Gender".equals(selectedMethod)) {
                    searchInputPanel.add(new JLabel("Nama:"), searchGbc);
                    searchGbc.gridx = 1;
                    searchInputPanel.add(txtNama, searchGbc);
                    searchGbc.gridx = 0;
                    searchGbc.gridy = 1;
                    searchInputPanel.add(new JLabel("Tanggal Lahir:"), searchGbc);
                    searchGbc.gridx = 1;
                    searchInputPanel.add(txtTanggalLahir, searchGbc);
                    searchGbc.gridx = 0;
                    searchGbc.gridy = 2;
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
                            SwingUtilities.invokeLater(()
                                    -> JOptionPane.showMessageDialog(this, "Gagal mengambil data dari API SATUSEHAT", "Error", JOptionPane.ERROR_MESSAGE)
                            );
                        } finally {
                            try {
                                api.close();
                            } catch (Exception ignore) {
                            }
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
                            String url = "/Patient?name="
                                    + java.net.URLEncoder.encode(nama, "UTF-8")
                                    + "&birthdate=" + java.net.URLEncoder.encode(tglLahir, "UTF-8")
                                    + "&gender=" + (gender.equalsIgnoreCase("Laki - Laki") ? "male" : "female");
                            String response = api.get(url);
                            tampilkanDataPasien(response, hasilArea);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SwingUtilities.invokeLater(()
                                    -> JOptionPane.showMessageDialog(this, "Gagal mengambil data dari API SATUSEHAT", "Error", JOptionPane.ERROR_MESSAGE)
                            );
                        } finally {
                            try {
                                api.close();
                            } catch (Exception ignore) {
                            }
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

                // Ambil kode wilayah dari dropdown
                String kodeProvinsi = region.getKodeProvinsi(cbProvinsi.getSelectedItem() != null ? cbProvinsi.getSelectedItem().toString() : "");
                String kodeKota = region.getKodeKota(cbKota.getSelectedItem() != null ? cbKota.getSelectedItem().toString() : "", kodeProvinsi);
                String kodeKecamatan = region.getKodeKecamatan(cbKecamatan.getSelectedItem() != null ? cbKecamatan.getSelectedItem().toString() : "",kodeProvinsi,kodeKota);
                String kodeKelurahan = region.getKodeKelurahan(cbKelurahan.getSelectedItem() != null ? cbKelurahan.getSelectedItem().toString() : "", kodeProvinsi, kodeKota, kodeKecamatan);
                // Gabungkan kode wilayah
                String alamatGabungan = String.join(",", kodeProvinsi, kodeKota, kodeKecamatan, kodeKelurahan);

                String Query = "INSERT INTO pasien (id_satusehat, nik, nama, jenis_kelamin, tanggal_lahir, alamat, nomor_bpjs, no_telepon, status) VALUES (?,?,?,?,?,?,?,?,?)";
                Object[] parameter = new Object[]{
                    hasilIdSatuSehat, hasilNIK, hasilNama, hasilGender, tanggalLahir,
                    alamatGabungan, // <-- alamat dari kode wilayah
                    0, 0, "BPJS"
                };
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
        cbProvinsi.setItems(region.getListProvinsi(), false, true, null); // List<String> nama provinsi
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
            String kodeProvinsi = region.getKodeProvinsi(cbProvinsi.getSelectedItem().toString());
            if (kodeProvinsi.isEmpty()) {
                cbKota.setItems(List.of(), false, true, null);
                cbKecamatan.setItems(List.of(), false, true, null);
                cbKelurahan.setItems(List.of(), false, true, null);
                return;
            }
            cbKota.setItems(region.getListKota(kodeProvinsi), false, true, null);
            cbKecamatan.setItems(List.of(), false, true, null);
            cbKelurahan.setItems(List.of(), false, true, null);
        });

        cbKota.addActionListener(e -> {
            String kodeProvinsi = region.getKodeProvinsi(cbProvinsi.getSelectedItem().toString());
            String kodeKota = region.getKodeKota(cbKota.getSelectedItem().toString(), kodeProvinsi);
            if (kodeKota.isEmpty()) {
                cbKecamatan.setItems(List.of(), false, true, null);
                cbKelurahan.setItems(List.of(), false, true, null);
                return;
            }
            List<String> kecamatanList = region.getListKecamatan(kodeProvinsi, kodeKota);
            cbKecamatan.setItems(kecamatanList, false, true, null);
            cbKelurahan.setItems(List.of(), false, true, null);
        });

        cbKecamatan.addActionListener(e -> {
            String kodeProvinsi = region.getKodeProvinsi(cbProvinsi.getSelectedItem() != null ? cbProvinsi.getSelectedItem().toString() : "");
            String kodeKotaLokal = region.getKodeKotaLokal(cbKota.getSelectedItem() != null ? cbKota.getSelectedItem().toString() : "", kodeProvinsi);
            String kodeKecamatanLokal = region.getKodeKecamatanLokal(cbKecamatan.getSelectedItem() != null ? cbKecamatan.getSelectedItem().toString() : "", kodeProvinsi, kodeKotaLokal);


            if (kodeProvinsi.isEmpty() || kodeKotaLokal.isEmpty() || kodeKecamatanLokal.isEmpty()) {
                cbKelurahan.setItems(List.of(), false, true, null);
                return;
            }

            cbKelurahan.setItems(region.getListKelurahan(kodeProvinsi, kodeKotaLokal, kodeKecamatanLokal), false, true, null);
            System.out.println("DEBUG: kodeProvinsi=" + kodeProvinsi + ", kodeKotaLokal=" + kodeKotaLokal + ", kodeKecamatanLokal=" + kodeKecamatanLokal + ", kelurahanList=" + kodeKecamatanLokal);
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
            String phone = txtPhone.getText().trim();

            if (nik.isEmpty() || name.isEmpty() || birthDate.isEmpty() || gender.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Ambil kode wilayah dari dropdown
            String namaProvinsi   = cbProvinsi.getSelectedItem() != null ? cbProvinsi.getSelectedItem().toString() : "";
            String namaKota       = cbKota.getSelectedItem() != null ? cbKota.getSelectedItem().toString() : "";
            String namaKecamatan  = cbKecamatan.getSelectedItem() != null ? cbKecamatan.getSelectedItem().toString() : "";
            String namaKelurahan  = cbKelurahan.getSelectedItem() != null ? cbKelurahan.getSelectedItem().toString() : "";

            String kodeProvinsi   = region.getKodeProvinsi(namaProvinsi).replace(".", "");
            String kodeKota       = region.getKodeKota(namaKota, kodeProvinsi).replace(".", "");
            String kodeKecamatanRaw = region.getKodeKecamatan(namaKecamatan, kodeProvinsi, kodeKota);
            // Setelah dapat hasil, baru hapus titik
            String kodeKelurahan  = region.getKodeKelurahan(namaKelurahan, kodeProvinsi, kodeKota, kodeKecamatanRaw).replace(".", "");
            String kodeKecamatan = kodeKelurahan.length() >= 6 ? kodeKelurahan.substring(0, 6) : ""; // Ambil 6 digit pertama

            System.out.println("kodeProvinsi: " + kodeProvinsi);
            System.out.println("kodeKota: " + kodeKota);
            System.out.println("kodeKecamatan: " + kodeKecamatan);
            System.out.println("kodeKelurahan: " + kodeKelurahan);
            System.out.println("getKodeKecamatan() called with namaKecamatan=[" + namaKecamatan + "], kodeProvinsi=[" + kodeProvinsi + "], kodeKota=[" + kodeKota + "]");

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
                        JSONObject resource = entries.getJSONObject(0).getJSONObject("resource");
                        // panggil helper untuk simpan/update
                        saveOrUpdatePatientFromResource(resource);
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
                            JSONObject resource = entries2.getJSONObject(0).getJSONObject("resource");
                            saveOrUpdatePatientFromResource(resource);
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

                            // Extension array untuk kode wilayah administrasi
                            // Gabungkan nama wilayah sebagai alamatGabungan
                            String alamatGabungan = String.join(", ",
                                cbProvinsi.getSelectedItem() != null ? cbProvinsi.getSelectedItem().toString() : "",
                                cbKota.getSelectedItem() != null ? cbKota.getSelectedItem().toString() : "",
                                cbKecamatan.getSelectedItem() != null ? cbKecamatan.getSelectedItem().toString() : "",
                                cbKelurahan.getSelectedItem() != null ? cbKelurahan.getSelectedItem().toString() : ""
                            );
                            Map<String, Object> addressMap = new HashMap<>();
                            addressMap.put("use", "home");
                            addressMap.put("line", new Object[]{ alamatGabungan }); // atau gabungan nama wilayah
                            addressMap.put("city", cbKota.getSelectedItem().toString());
                            addressMap.put("country", "ID");
                            // addressMap.put("postalCode", "12950"); // jika ada

                            addressMap.put("extension", new Object[]{
                                Map.of(
                                    "url", "https://fhir.kemkes.go.id/r4/StructureDefinition/administrativeCode",
                                    "extension", new Object[]{
                                        Map.of("url", "province", "valueCode", kodeProvinsi),
                                        Map.of("url", "city", "valueCode", kodeKota),
                                        Map.of("url", "district", "valueCode", kodeKecamatan),
                                        Map.of("url", "village", "valueCode", kodeKelurahan)
                                    }
                                )
                            });

                            // Optionally, you can add address line or other fields if needed
                            // addressMap.put("line", new Object[]{alamatGabungan});

                            patient.put("address", new Object[]{addressMap});
                            patient.put("telecom", new Object[]{
                                Map.of("system", "phone", "value", phone)
                            });
                            patient.put("multipleBirthBoolean", false);

                            // Sebelum mengirim ke API, print JSON body-nya
                            System.out.println("=== JSON BODY YANG DIKIRIM ===");
                            System.out.println(new JSONObject(patient).toString(2)); // pretty print 2 spasi

                            String postResponse = api.post("/Patient", patient);
                            System.out.println("Response POST Patient: " + postResponse);

                            JSONObject postJson = new JSONObject(postResponse);
                            if (postJson.has("id")) {
                                String idSatusehat = postJson.getString("id");
                                String patientFull = api.get("/Patient/" + idSatusehat);
                                JSONObject fullJson = new JSONObject(patientFull);
                                if (fullJson.has("resource")) {
                                    saveOrUpdatePatientFromResource(fullJson.getJSONObject("resource"));
                                } else {
                                    // kadang direct resource returned di root
                                    saveOrUpdatePatientFromResource(fullJson);
                                }
                            } else {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Gagal mendaftarkan pasien ke SATUSEHAT!", "Error", JOptionPane.ERROR_MESSAGE));
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Gagal proses ke SATUSEHAT", "Error", JOptionPane.ERROR_MESSAGE));
                } finally {
                    try {
                        api.close();
                    } catch (Exception ignore) {
                    }
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

            // Ambil data dari resource
            String idSatusehat = resource.optString("id", null);
            String namaSatusehat = resource.getJSONArray("name").getJSONObject(0).optString("text");
            String tanggalLahirSatusehat = resource.optString("birthDate", "");
            String genderApi = resource.optString("gender", "");
            String jenisKelamin = "Tidak Bisa Dijelaskan";
            if ("male".equalsIgnoreCase(genderApi)) jenisKelamin = "Laki - Laki";
            else if ("female".equalsIgnoreCase(genderApi)) jenisKelamin = "Perempuan";

            String nikSatusehat = "";
            if (resource.has("identifier")) {
                JSONArray identifiers = resource.getJSONArray("identifier");
                for (int i = 0; i < identifiers.length(); i++) {
                    JSONObject idObj = identifiers.getJSONObject(i);
                    if ("https://fhir.kemkes.go.id/id/nik".equals(idObj.optString("system"))) {
                        nikSatusehat = idObj.optString("value", "");
                        break;
                    }
                }
            }

            String alamatSatusehat = "";
            if (resource.has("address")) {
                try {
                    alamatSatusehat = resource.getJSONArray("address").getJSONObject(0).optString("line", "");
                } catch (Exception ignore) {}
            }

            final String finalIdSatusehat = idSatusehat;
            final String finalNik = nikSatusehat;
            final String finalNama = namaSatusehat;
            final String finalTgl = tanggalLahirSatusehat.isEmpty() ? "1970-01-01" : tanggalLahirSatusehat;
            final String finalGender = jenisKelamin;
            final String finalAlamat = alamatSatusehat;

            // Simpan/Update ke DB lokal
            SwingUtilities.invokeLater(() -> {
                try {
                    // Cek apakah sudah ada berdasarkan id_satusehat atau nik
                    String sqlCheck = "SELECT id FROM pasien WHERE id_satusehat = ? OR nik = ?";
                    QueryExecutor queryExecutor = new QueryExecutor();
                    List<Map<String, Object>> checkRes = queryExecutor.executeSelectQuery(sqlCheck, new Object[]{finalIdSatusehat, finalNik});
                    if (checkRes != null && !checkRes.isEmpty()) {
                        // Sudah ada -> lakukan update (opsional)
                        String sqlUpdate = "UPDATE pasien SET id_satusehat = ?, nik = ?, nama = ?, jenis_kelamin = ?, tanggal_lahir = ?, alamat = ? WHERE id = ?";
                        Object existingId = checkRes.get(0).get("id");
                        QueryExecutor.executeUpdateQuery(sqlUpdate, new Object[]{
                            finalIdSatusehat, finalNik, finalNama, finalGender, finalTgl, finalAlamat, existingId
                        });
                        // Panggil listener dengan data terupdate
                        if (listener != null) {
                            listener.onPasienAdded(existingId.toString(), finalNik, finalNama, finalTgl, finalGender, "0", finalAlamat, null);
                        }
                        JOptionPane.showMessageDialog(this, "Pasien sudah ada di SATUSEHAT. Data lokal diperbarui.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // Belum ada -> insert
                        String sqlInsert = "INSERT INTO pasien (id_satusehat, nik, nama, jenis_kelamin, tanggal_lahir, alamat, nomor_bpjs, no_telepon, status) VALUES (?,?,?,?,?,?,?,?,?)";
                        Object[] params = new Object[]{ finalIdSatusehat, finalNik, finalNama, finalGender, finalTgl, finalAlamat, 0, 0, "BPJS" };
                        Long newId = QueryExecutor.executeInsertQueryWithReturnID(sqlInsert, params);
                        if (newId != 404) {
                            if (listener != null) {
                                listener.onPasienAdded(newId.toString(), finalNik, finalNama, finalTgl, finalGender, "0", finalAlamat, null);
                            }
                            JOptionPane.showMessageDialog(this, "Data pasien berhasil disimpan ke database lokal.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                            // Tutup modal jika perlu
                            ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(this));
                        } else {
                            JOptionPane.showMessageDialog(this, "Gagal menyimpan data pasien ke database lokal.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error saat menyimpan pasien: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            hasilIdSatuSehat = null;
            hasilNIK = hasilNama = hasilTanggalLahir = hasilGender = hasilAlamat = null;
            String hasil = "⚠️ Tidak ada data pasien ditemukan.";
            System.out.println(hasil);
            SwingUtilities.invokeLater(() -> hasilArea.setText(hasil));
        }
    }

    // tambahkan method ini di dalam class RegisterPasien
    private void saveOrUpdatePatientFromResource(JSONObject resource) {
        String idSatusehat = resource.optString("id", null);
        String namaSatusehat = resource.optJSONArray("name") != null
                ? resource.getJSONArray("name").getJSONObject(0).optString("text", "")
                : "";
        String tanggalLahirSatusehat = resource.optString("birthDate", "");
        String genderApi = resource.optString("gender", "");
        String jenisKelamin = "Tidak Bisa Dijelaskan";
        if ("male".equalsIgnoreCase(genderApi)) jenisKelamin = "Laki - Laki";
        else if ("female".equalsIgnoreCase(genderApi)) jenisKelamin = "Perempuan";

        String nikSatusehat = "";
        if (resource.has("identifier")) {
            JSONArray identifiers = resource.getJSONArray("identifier");
            for (int i = 0; i < identifiers.length(); i++) {
                JSONObject idObj = identifiers.getJSONObject(i);
                if ("https://fhir.kemkes.go.id/id/nik".equals(idObj.optString("system"))) {
                    nikSatusehat = idObj.optString("value", "");
                    break;
                }
            }
        }

        String alamatSatusehat = "";
        if (resource.has("address")) {
            try {
                alamatSatusehat = resource.getJSONArray("address").getJSONObject(0).optString("line", "");
            } catch (Exception ignore) {}
        }

        // Ambil input form jika ada (prioritas)
        String formTanggal = null;
        try {
            if (txtAge != null) {
                String inputDate = txtAge.getText().trim();
                if (!inputDate.isEmpty()) {
                    if (inputDate.contains("/")) {
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate date = LocalDate.parse(inputDate, inputFormatter);
                        formTanggal = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    } else {
                        LocalDate date = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        formTanggal = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                }
            }
        } catch (Exception ignore) {}

        String formGender = null;
        try {
            if (txtGender != null && txtGender.getSelectedItem() != null) {
                formGender = txtGender.getSelectedItem().toString();
            }
        } catch (Exception ignore) {}

        final String finalIdSatusehat = idSatusehat;
        final String finalNik = nikSatusehat;
        final String finalNama = namaSatusehat;
        final String finalTgl = (formTanggal != null && !formTanggal.isEmpty()) ? formTanggal
                : (tanggalLahirSatusehat.isEmpty() ? "1970-01-01" : tanggalLahirSatusehat);
        final String finalGender = (formGender != null && !formGender.isEmpty()) ? formGender : jenisKelamin;
        final String finalAlamat = alamatSatusehat;

        SwingUtilities.invokeLater(() -> {
            try {
                // Cek apakah sudah ada berdasarkan id_satusehat atau nik
                String sqlCheck = "SELECT * FROM pasien WHERE id_satusehat = ? OR nik = ? LIMIT 1";
                QueryExecutor queryExecutor = new QueryExecutor();
                List<Map<String, Object>> checkRes = queryExecutor.executeSelectQuery(sqlCheck, new Object[]{finalIdSatusehat, finalNik});
                if (checkRes != null && !checkRes.isEmpty()) {
                    // Update berdasarkan id_satusehat/nik (tidak mengasumsikan kolom PK bernama 'id')
                    String sqlUpdate = "UPDATE pasien SET id_satusehat = ?, nik = ?, nama = ?, jenis_kelamin = ?, tanggal_lahir = ?, alamat = ? WHERE id_satusehat = ? OR nik = ?";
                    QueryExecutor.executeUpdateQuery(sqlUpdate, new Object[]{
                        finalIdSatusehat, finalNik, finalNama, finalGender, finalTgl, finalAlamat,
                        finalIdSatusehat, finalNik
                    });

                    // Ambil kembali record untuk mendapatkan PK lokal (jika perlu untuk listener)
                    List<Map<String, Object>> after = queryExecutor.executeSelectQuery(sqlCheck, new Object[]{finalIdSatusehat, finalNik});
                    Object localId = null;
                    if (after != null && !after.isEmpty()) {
                        Map<String, Object> row = after.get(0);
                        // cari kolom PK umum
                        String[] candidates = {"id", "id_pasien", "pasien_id", "pk"};
                        for (String c : candidates) {
                            if (row.containsKey(c)) {
                                localId = row.get(c);
                                break;
                            }
                        }
                        if (localId == null) {
                            // fallback: ambil value dari kolom pertama
                            localId = row.values().iterator().next();
                        }
                    }

                    if (listener != null) {
                        listener.onPasienAdded(localId != null ? localId.toString() : "", finalNik, finalNama, finalTgl, finalGender, "0", finalAlamat, null);
                    }
                    JOptionPane.showMessageDialog(this, "Pasien sudah ada di SATUSEHAT. Data lokal diperbarui.", "Info", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Insert baru, simpan id_satusehat ke kolom id_satusehat
                    String sqlInsert = "INSERT INTO pasien (id_satusehat, nik, nama, jenis_kelamin, tanggal_lahir, alamat, nomor_bpjs, no_telepon, status) VALUES (?,?,?,?,?,?,?,?,?)";
                    Object[] params = new Object[]{ finalIdSatusehat, finalNik, finalNama, finalGender, finalTgl, finalAlamat, 0, 0, "BPJS" };
                    Long newId = QueryExecutor.executeInsertQueryWithReturnID(sqlInsert, params);
                    if (newId != 404) {
                        if (listener != null) {
                            listener.onPasienAdded(newId.toString(), finalNik, finalNama, finalTgl, finalGender, "0", finalAlamat, null);
                        }
                        JOptionPane.showMessageDialog(this, "Data pasien berhasil disimpan ke database lokal.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(this));
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal menyimpan data pasien ke database lokal.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saat menyimpan pasien: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
