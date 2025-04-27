import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

public class FormBayar {
    public static void main(String[] args) {
        // Membuat frame utama
        JFrame frame = new JFrame("Bayar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        // Panel utama dengan CardLayout
        JPanel mainPanel = new JPanel(new CardLayout());

        // Panel untuk form pembayaran dengan GridBagLayout
        JPanel panelBayar = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Margin antar komponen

        // Label Kasir
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelBayar.add(new JLabel("Kasir"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        JTextField txtKasir = createRoundedTextField("admin", 15);
        txtKasir.setEditable(false);
        panelBayar.add(txtKasir, gbc);

        // Label Tanggal
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelBayar.add(new JLabel("Tanggal"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        JTextField txtTanggal = createRoundedTextField("26/04/2025", 15);
        txtTanggal.setEditable(false);
        panelBayar.add(txtTanggal, gbc);

        // Label Pelanggan
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelBayar.add(new JLabel("Pelanggan"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        JTextField txtPelanggan = createRoundedTextField("", 15);
        txtPelanggan.setBackground(Color.WHITE); // Warna latar belakang putih
        panelBayar.add(txtPelanggan, gbc);

        // Sub Total
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelBayar.add(new JLabel("Sub Total"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        JTextField txtSubTotal = createRoundedTextField("Rp. 2,000", 15);
        txtSubTotal.setEditable(false);
        panelBayar.add(txtSubTotal, gbc);

        // Diskon
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelBayar.add(new JLabel("Diskon (%)"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        JSpinner spnDiskon = createRoundedSpinner(0, 0, 100, 1); // Nilai awal 0, minimum 0, maksimum 100, langkah 1
        panelBayar.add(spnDiskon, gbc);

        // Grand Total
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelBayar.add(new JLabel("Grand Total"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        JTextField txtGrandTotal = createRoundedTextField("Rp. 2,000", 15);
        txtGrandTotal.setEditable(false);
        txtGrandTotal.setOpaque(true); // Pastikan latar belakang terlihat
        txtGrandTotal.setBackground(Color.YELLOW); // Warna latar belakang kuning
        panelBayar.add(txtGrandTotal, gbc);

        // Dibayar
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelBayar.add(new JLabel("Dibayar"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        JTextField txtDibayar = createRoundedTextField("0", 15);
        txtDibayar.setBackground(Color.WHITE); // Warna latar belakang putih
        panelBayar.add(txtDibayar, gbc);

        // Kembali
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelBayar.add(new JLabel("Kembali"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        JTextField txtKembali = createRoundedTextField("-2,000", 15);
        txtKembali.setEditable(false);
        panelBayar.add(txtKembali, gbc);

        // Tombol Uang Pas
        gbc.gridx = 3; // Sama dengan posisi "Dibayar"
        gbc.gridy = 4; // Baris di bawah "Dibayar"
        gbc.anchor = GridBagConstraints.LINE_START;
        JButton btnUangPas = createRoundedButton("Uang Pas");
        panelBayar.add(btnUangPas, gbc);

        // Metode Pembayaran
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4; // Membuat label menempati seluruh lebar kolom
        gbc.anchor = GridBagConstraints.CENTER;
        panelBayar.add(new JLabel("Metode Pembayaran"), gbc);

        // Membuat panel untuk pilihan metode pembayaran
        JPanel panelMetodePembayaran = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // FlowLayout untuk menyusun elemen secara horizontal
        JRadioButton rbTunai = new JRadioButton("Tunai");
        rbTunai.setSelected(true);
        JRadioButton rbNonTunai = new JRadioButton("Non Tunai / Kredit");
        panelMetodePembayaran.add(rbTunai);
        panelMetodePembayaran.add(rbNonTunai);

        // Menambahkan radio button ke grup
        ButtonGroup bgMetode = new ButtonGroup();
        bgMetode.add(rbTunai);
        bgMetode.add(rbNonTunai);

        // Menambahkan panelMetodePembayaran ke GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 4; // Membuat panel menempati seluruh lebar kolom
        gbc.anchor = GridBagConstraints.CENTER;
        panelBayar.add(panelMetodePembayaran, gbc);

        // Tombol Simpan dan Batal
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // FlowLayout untuk menyusun tombol secara horizontal
        JButton btnSimpan = createRoundedButton("Simpan");
        JButton btnBatal = createRoundedButton("Batal");
        panelTombol.add(btnSimpan);
        panelTombol.add(btnBatal);

        // Menambahkan panelTombol ke GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 4; // Membuat panel menempati seluruh lebar kolom
        gbc.anchor = GridBagConstraints.CENTER;
        panelBayar.add(panelTombol, gbc);

        // Menambahkan panelBayar ke mainPanel
        mainPanel.add(panelBayar, "FormBayar");

        // Menambahkan mainPanel ke frame
        frame.add(mainPanel);

        // Menampilkan frame
        frame.setVisible(true);
    }

    // Membuat tombol rounded dengan latar belakang yang benar
    private static JButton createRoundedButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(getBackground().darker());
                } else {
                    g.setColor(getBackground());
                }
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Menggambar latar belakang rounded
                super.paintComponent(g);
            }

            @Override
            public void paintBorder(Graphics g) {
                g.setColor(getForeground());
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20); // Menggambar border rounded
            }

            @Override
            public boolean isOpaque() {
                return false; // Menghindari masalah dengan Look and Feel
            }
        };
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBackground(new Color(0, 122, 255)); // Warna background
        button.setForeground(Color.WHITE); // Warna teks
        button.setFont(new Font("Arial", Font.BOLD, 12)); // Font tombol
        return button;
    }

    // Membuat text field rounded
    private static JTextField createRoundedTextField(String text, int columns) {
        JTextField textField = new JTextField(text, columns);
        textField.setBorder(new RoundedBorder(5)); // Radius 15 untuk rounded
        textField.setOpaque(false);
        return textField;
    }

    // Membuat spinner rounded
    private static JSpinner createRoundedSpinner(int initialValue, int minValue, int maxValue, int stepSize) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialValue, minValue, maxValue, stepSize));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setBorder(new RoundedBorder(5)); // Radius untuk rounded
            textField.setOpaque(false);
            textField.setBackground(Color.WHITE); // Warna latar belakang putih
            textField.setColumns(5); // Lebar text field
        }
        spinner.setPreferredSize(new Dimension(150, 25)); // Ukuran spinner
        return spinner;
    }
}

class RoundedBorder extends AbstractBorder {
    private final int radius;

    public RoundedBorder(int radius) {
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }
}
