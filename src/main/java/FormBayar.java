// import java.awt.CardLayout;
// import java.awt.Color;
// import java.awt.Component;
// import java.awt.Dimension;
// import java.awt.FlowLayout;
// import java.awt.Font;
// import java.awt.Graphics;
// import java.awt.Graphics2D;
// import java.awt.GridBagConstraints;
// import java.awt.GridBagLayout;
// import java.awt.Insets;
// import java.awt.RenderingHints;
// import java.util.ArrayList;
// import java.util.List;

// import javax.swing.ButtonGroup;
// import javax.swing.JButton;
// import javax.swing.JComponent;
// import javax.swing.JFrame;
// import javax.swing.JLabel;
// import javax.swing.JOptionPane;
// import javax.swing.JPanel;
// import javax.swing.JRadioButton;
// import javax.swing.JSpinner;
// import javax.swing.JTextField;
// import javax.swing.SpinnerNumberModel;
// import javax.swing.border.AbstractBorder;
// import javax.swing.table.DefaultTableModel;

// public class FormBayar extends JPanel{
//     private List<String[]> dataTransaksi;
//     private double totalKeseluruhan;
//     private JFrame parentFrame;
//     private JTextField txtKasir, txtTanggal, txtPelanggan, txtSubTotal, txtGrandTotal, txtDibayar, txtKembali;

//     // Konstruktor baru
//     public FormBayar(List<String[]> dataTransaksi, double totalKeseluruhan, JFrame parentFrame) {
//         this.dataTransaksi = dataTransaksi;
//         this.totalKeseluruhan = totalKeseluruhan;
//         this.parentFrame = parentFrame;

//         // Panggil metode untuk inisialisasi UI
//         initializeUI();
//     }

//     // Metode untuk inisialisasi UI
//     private void initializeUI() {
//         JFrame frame = new JFrame("Bayar");
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         frame.setSize(700, 500);

//         // Panel utama dengan CardLayout
//         JPanel mainPanel = new JPanel(new CardLayout());

//         // Panel untuk form pembayaran dengan GridBagLayout
//         JPanel panelBayar = new JPanel(new GridBagLayout());
//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.insets = new Insets(5, 5, 5, 5); // Margin antar komponen

//         // Label Kasir
//         gbc.gridx = 0;
//         gbc.gridy = 0;
//         gbc.anchor = GridBagConstraints.LINE_END;
//         panelBayar.add(new JLabel("Kasir"), gbc);

//         gbc.gridx = 1;
//         gbc.gridy = 0;
//         gbc.anchor = GridBagConstraints.LINE_START;
//         JTextField txtKasir = createRoundedTextField("admin", 15);
//         txtKasir.setEditable(false);
//         panelBayar.add(txtKasir, gbc);

//         // Label Tanggal
//         gbc.gridx = 2;
//         gbc.gridy = 0;
//         gbc.anchor = GridBagConstraints.LINE_END;
//         panelBayar.add(new JLabel("Tanggal"), gbc);

//         gbc.gridx = 3;
//         gbc.gridy = 0;
//         gbc.anchor = GridBagConstraints.LINE_START;
//         JTextField txtTanggal = createRoundedTextField("26/04/2025", 15);
//         txtTanggal.setEditable(false);
//         panelBayar.add(txtTanggal, gbc);

//         // Label Pelanggan
//         gbc.gridx = 2;
//         gbc.gridy = 1;
//         gbc.anchor = GridBagConstraints.LINE_END;
//         panelBayar.add(new JLabel("Pelanggan"), gbc);

//         gbc.gridx = 3;
//         gbc.gridy = 1;
//         gbc.anchor = GridBagConstraints.LINE_START;
//         JTextField txtPelanggan = createRoundedTextField("", 15);
//         txtPelanggan.setBackground(Color.WHITE); // Warna latar belakang putih
//         panelBayar.add(txtPelanggan, gbc);

//         // Sub Total
//         gbc.gridx = 0;
//         gbc.gridy = 1;
//         gbc.anchor = GridBagConstraints.LINE_END;
//         panelBayar.add(new JLabel("Sub Total"), gbc);

//         gbc.gridx = 1;
//         gbc.gridy = 1;
//         gbc.anchor = GridBagConstraints.LINE_START;
//         JTextField txtSubTotal = createRoundedTextField("Rp. " + totalKeseluruhan, 15);
//         txtSubTotal.setEditable(false);
//         panelBayar.add(txtSubTotal, gbc);

//         // Diskon
//         gbc.gridx = 2;
//         gbc.gridy = 2;
//         gbc.anchor = GridBagConstraints.LINE_END;
//         panelBayar.add(new JLabel("Diskon (%)"), gbc);

//         gbc.gridx = 3;
//         gbc.gridy = 2;
//         gbc.anchor = GridBagConstraints.LINE_START;
//         JSpinner spnDiskon = createRoundedSpinner(0, 0, 100, 1); // Nilai awal 0, minimum 0, maksimum 100, langkah 1
//         panelBayar.add(spnDiskon, gbc);

//         // Grand Total
//         gbc.gridx = 0;
//         gbc.gridy = 2;
//         gbc.anchor = GridBagConstraints.LINE_END;
//         panelBayar.add(new JLabel("Grand Total"), gbc);

//         gbc.gridx = 1;
//         gbc.gridy = 2;
//         gbc.anchor = GridBagConstraints.LINE_START;
//         JTextField txtGrandTotal = createRoundedTextField("Rp. 2,000", 15);
//         txtGrandTotal.setEditable(false);
//         txtGrandTotal.setOpaque(true); // Pastikan latar belakang terlihat
//         txtGrandTotal.setBackground(Color.YELLOW); // Warna latar belakang kuning
//         panelBayar.add(txtGrandTotal, gbc);

//         // Dibayar
//         gbc.gridx = 2;
//         gbc.gridy = 3;
//         gbc.anchor = GridBagConstraints.LINE_END;
//         panelBayar.add(new JLabel("Dibayar"), gbc);

//         gbc.gridx = 3;
//         gbc.gridy = 3;
//         gbc.anchor = GridBagConstraints.LINE_START;
//         JTextField txtDibayar = createRoundedTextField("0", 15);
//         txtDibayar.setBackground(Color.WHITE); // Warna latar belakang putih
//         panelBayar.add(txtDibayar, gbc);

//         // Kembali
//         gbc.gridx = 0;
//         gbc.gridy = 3;
//         gbc.anchor = GridBagConstraints.LINE_END;
//         panelBayar.add(new JLabel("Kembali"), gbc);

//         gbc.gridx = 1;
//         gbc.gridy = 3;
//         gbc.anchor = GridBagConstraints.LINE_START;
//         JTextField txtKembali = createRoundedTextField("-2,000", 15);
//         txtKembali.setEditable(false);
//         panelBayar.add(txtKembali, gbc);

//         // Tombol Uang Pas
//         gbc.gridx = 3; // Sama dengan posisi "Dibayar"
//         gbc.gridy = 4; // Baris di bawah "Dibayar"
//         gbc.anchor = GridBagConstraints.LINE_START;
//         JButton btnUangPas = createRoundedButton("Uang Pas");
//         panelBayar.add(btnUangPas, gbc);

//         // Metode Pembayaran
//         gbc.gridx = 0;
//         gbc.gridy = 5;
//         gbc.gridwidth = 4; // Membuat label menempati seluruh lebar kolom
//         gbc.anchor = GridBagConstraints.CENTER;
//         panelBayar.add(new JLabel("Metode Pembayaran"), gbc);

//         // Membuat panel untuk pilihan metode pembayaran
//         JPanel panelMetodePembayaran = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // FlowLayout untuk menyusun elemen secara horizontal
//         JRadioButton rbTunai = new JRadioButton("Tunai");
//         rbTunai.setSelected(true);
//         JRadioButton rbNonTunai = new JRadioButton("Non Tunai / Kredit");
//         panelMetodePembayaran.add(rbTunai);
//         panelMetodePembayaran.add(rbNonTunai);

//         // Menambahkan radio button ke grup
//         ButtonGroup bgMetode = new ButtonGroup();
//         bgMetode.add(rbTunai);
//         bgMetode.add(rbNonTunai);

//         // Menambahkan panelMetodePembayaran ke GridBagLayout
//         gbc.gridx = 0;
//         gbc.gridy = 6;
//         gbc.gridwidth = 4; // Membuat panel menempati seluruh lebar kolom
//         gbc.anchor = GridBagConstraints.CENTER;
//         panelBayar.add(panelMetodePembayaran, gbc);

//         // Tombol Simpan dan Batal
//         JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // FlowLayout untuk menyusun tombol secara horizontal
//         JButton btnSimpan = createRoundedButton("Simpan");
//         btnSimpan.setBackground(new Color(0, 200, 0)); // Warna hijau untuk tombol Simpan
//         btnSimpan.addActionListener(e -> {
//             // Cetak teks dari semua JTextField
//             System.out.println(txtKasir.getText());
//             System.out.println(txtTanggal.getText());
//             System.out.println(txtPelanggan.getText());
//             System.out.println(txtSubTotal.getText());
//             System.out.println(txtGrandTotal.getText());
//             System.out.println(txtDibayar.getText());
//             System.out.println(txtKembali.getText());
//         });

//         JButton btnBatal = createRoundedButton("Batal");
//         btnBatal.setBackground(new Color(200, 0, 0)); // Warna merah untuk tombol Batal
//         btnBatal.addActionListener(e -> {
//             // Reset semua JTextField
//             txtPelanggan.setText("");
//             txtDibayar.setText("0");
//             txtKembali.setText("-2,000");
//         });

//         panelTombol.add(btnSimpan);
//         panelTombol.add(btnBatal);

//         // Menambahkan panelTombol ke GridBagLayout
//         gbc.gridx = 0;
//         gbc.gridy = 7;
//         gbc.gridwidth = 4; // Membuat panel menempati seluruh lebar kolom
//         gbc.anchor = GridBagConstraints.CENTER;
//         panelBayar.add(panelTombol, gbc);

//         // Menambahkan panelBayar ke mainPanel
//         mainPanel.add(panelBayar, "FormBayar");

//         // Menambahkan mainPanel ke frame
//         frame.add(mainPanel);

//         // Menampilkan frame
//         frame.setVisible(true);
//     }

//     // Membuat tombol rounded dengan latar belakang yang benar
//     private static JButton createRoundedButton(String text) {
//         JButton button = new JButton(text) {
//             @Override
//             protected void paintComponent(Graphics g) {
//                 if (getModel().isArmed()) {
//                     g.setColor(getBackground().darker());
//                 } else {
//                     g.setColor(getBackground());
//                 }
//                 g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Menggambar latar belakang rounded
//                 super.paintComponent(g);
//             }

//             @Override
//             public void paintBorder(Graphics g) {
//                 g.setColor(getForeground());
//                 g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20); // Menggambar border rounded
//             }

//             @Override
//             public boolean isOpaque() {
//                 return false; // Menghindari masalah dengan Look and Feel
//             }
//         };
//         button.setFocusPainted(false);
//         button.setContentAreaFilled(false);
//         button.setBorderPainted(false);
//         button.setBackground(new Color(0, 122, 255)); // Warna background
//         button.setForeground(Color.WHITE); // Warna teks
//         button.setFont(new Font("Arial", Font.BOLD, 12)); // Font tombol
//         return button;
//     }

//     // Subclass JTextField untuk mengabaikan spasi awal
//     private static class CustomTextField extends JTextField {
//         private final String prefix = " "; // Spasi di awal

//         public CustomTextField(String text, int columns) {
//             super(" " + text, columns); // Tambahkan spasi awal secara visual
//         }

//         @Override
//         public String getText() {
//             // Kembalikan teks tanpa spasi awal
//             return super.getText().trim();
//         }
//     }

//     // Modifikasi metode createRoundedTextField
//     private static JTextField createRoundedTextField(String text, int columns) {
//         JTextField textField = new CustomTextField(text, columns);
//         textField.setBorder(new RoundedBorder(5)); // Radius untuk rounded
//         textField.setOpaque(true); // Pastikan latar belakang terlihat
//         textField.setBackground(Color.WHITE); // Warna latar belakang putih
//         textField.setMargin(new Insets(5, 10, 5, 10)); // Memberikan jarak internal (atas, kiri, bawah, kanan)
//         return textField;
//     }

//     // Membuat spinner rounded
//     private static JSpinner createRoundedSpinner(int initialValue, int minValue, int maxValue, int stepSize) {
//         JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialValue, minValue, maxValue, stepSize));
//         JComponent editor = spinner.getEditor();
//         if (editor instanceof JSpinner.DefaultEditor) {
//             JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
//             textField.setBorder(new RoundedBorder(5)); // Radius untuk rounded
//             textField.setOpaque(false);
//             textField.setBackground(Color.WHITE); // Warna latar belakang putih
//             textField.setColumns(5); // Lebar text field
//         }
//         spinner.setPreferredSize(new Dimension(150, 25)); // Ukuran spinner
//         return spinner;
//     }

//     private void btnBayarActionPerformed(java.awt.event.ActionEvent evt) {                                         
//         DefaultTableModel model = (DefaultTableModel) tableTransaksi.getModel();

//         // Cek apakah tabel kosong
//         if (model.getRowCount() == 0) {
//             JOptionPane.showMessageDialog(this, "Tidak ada transaksi yang diproses!", "Peringatan", JOptionPane.WARNING_MESSAGE);
//             return; // Hentikan proses jika tabel kosong
//         }
//         List<String[]> dataTransaksi = new ArrayList<>();
//         int totalKeseluruhan = 0;

//         // Ambil data dari tabel transaksi
//         for (int i = 0; i < model.getRowCount(); i++) {
//             String namaPupuk = model.getValueAt(i, 0).toString();
//             String hargaPupuk = model.getValueAt(i, 1).toString();
//             String jumlahBeli = model.getValueAt(i, 2).toString();
//             String totalHarga = model.getValueAt(i, 3).toString();

//             // Simpan ke dalam list untuk dikirim ke FormBayar
//             dataTransaksi.add(new String[]{namaPupuk, hargaPupuk, jumlahBeli, totalHarga});

//             // Hitung total keseluruhan transaksi
//             try {
//                 String nilaiStr = totalHarga.replaceAll("[^0-9,]", "").replace(",", ".");
//                 if (!nilaiStr.isEmpty()) {
//                     totalKeseluruhan += Double.parseDouble(nilaiStr);
//                 }
//             } catch (NumberFormatException e) {
//                 System.out.println("Error parsing angka: " + totalHarga);
//             }
//         }

//         // Kirim data transaksi ke FormBayar
//         FormBayar bayarFrame = new FormBayar(dataTransaksi, totalKeseluruhan, this);
//         bayarFrame.setVisible(true);
//     }
// }

// class RoundedBorder extends AbstractBorder {
//     private final int radius;

//     public RoundedBorder(int radius) {
//         this.radius = radius;
//     }

//     @Override
//     public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
//         Graphics2D g2 = (Graphics2D) g;
//         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g2.setColor(c.getForeground());
//         g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
//     }
// }
