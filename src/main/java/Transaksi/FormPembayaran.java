package Transaksi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.GridLayout;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JSeparator;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import Antrian.AntrianPasien;
import Components.CustomRadioButton;
import Components.CustomTextField;
import Components.ShowModalCenter;
import Components.CustomTable.CustomTable;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Utils.BillPrinter;

public class FormPembayaran extends JPanel {

    private JLabel totalLabel;
    private JLabel changeLabel;
    private JTable drugTable;
    private DefaultTableModel tableModel;
    private JButton payButton;
    private CustomTextField paymentField;
    private BigDecimal total;
    private Object[] patientData;
    private List<Object[]> drugData;
    private UserSessionCache cache = new UserSessionCache();
    private String Username = (String) cache.getusername();

    public FormPembayaran(Object[] patientData, String idAntrian, String status, AntrianPasien antrianPasien) {
        this.patientData = patientData;

        // Inisialisasi total dengan nilai default
        this.total = BigDecimal.ZERO;

        // Ambil data obat dan hitung total
        drugData = getDrugData(idAntrian);
        this.total = calculateTotal(drugData);

        // Atur ukuran form
        this.setPreferredSize(new Dimension(1000, 680));

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel Input Pembayaran
        JPanel paymentPanel = new JPanel(new GridBagLayout());
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Detail Pembayaran"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Margin antar elemen
        gbc.fill = GridBagConstraints.BOTH;

        // Panel Kiri
        JPanel leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.insets = new Insets(5, 5, 5, 5);
        leftGbc.fill = GridBagConstraints.HORIZONTAL;
        leftGbc.anchor = GridBagConstraints.CENTER; // Posisikan elemen di tengah

        // Baris 1: Name
        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftPanel.add(new JLabel("Nama:"), leftGbc);

        leftGbc.gridx = 1;
        leftPanel.add(new JLabel((String) patientData[1]), leftGbc);

        // Baris 2: Total
        leftGbc.gridx = 0;
        leftGbc.gridy = 1;
        leftPanel.add(new JLabel("Total:"), leftGbc);

        leftGbc.gridx = 1;
        totalLabel = new JLabel("Rp. " + total);
        leftPanel.add(totalLabel, leftGbc);

        // Baris 3: Payment Method
        leftGbc.gridx = 0;
        leftGbc.gridy = 2;
        leftPanel.add(new JLabel("Metode Pembayaran:"), leftGbc);

        leftGbc.gridx = 1;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        CustomRadioButton cashRadio = new CustomRadioButton("Cash", true);
        CustomRadioButton cardRadio = new CustomRadioButton("Qris");
        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cashRadio);
        paymentGroup.add(cardRadio);
        radioPanel.add(cashRadio);
        radioPanel.add(cardRadio);
        leftPanel.add(radioPanel, leftGbc);

        // Panel Kanan
        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.insets = new Insets(5, 5, 5, 5);
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.anchor = GridBagConstraints.CENTER; // Posisikan elemen di tengah

        // Baris 1: Payment
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightPanel.add(new JLabel("Jumlah Uang:"), rightGbc);

        rightGbc.gridx = 1;
        paymentField = new CustomTextField("Masukan Jumlah Uang", 20, 10, Optional.of(false));
        rightPanel.add(paymentField, rightGbc);

        // Baris 2: Change
        rightGbc.gridx = 0;
        rightGbc.gridy = 1;
        rightPanel.add(new JLabel("Kembalian:"), rightGbc);

        rightGbc.gridx = 1;
        changeLabel = new JLabel("Rp. 0");
        rightPanel.add(changeLabel, rightGbc);

        // Divider (JSeparator)
        JSeparator divider = new JSeparator(SwingConstants.VERTICAL);
        divider.setPreferredSize(new Dimension(2, 150)); // Divider dengan tinggi 150px

        // Gabungkan Panel Kiri, Divider, dan Kanan
        gbc.gridx = 0;
        gbc.gridy = 0;
        paymentPanel.add(leftPanel, gbc);

        gbc.gridx = 1;
        paymentPanel.add(divider, gbc);

        gbc.gridx = 2;
        paymentPanel.add(rightPanel, gbc);

        // Tambahkan ActionListener untuk metode pembayaran
        cashRadio.addActionListener(e -> {
            paymentField.getTextField().setEditable(true); // Aktifkan input manual
            paymentField.setText(""); // Kosongkan field
            changeLabel.setText("Rp. 0"); // Reset kembalian
        });

        cardRadio.addActionListener(e -> {
            paymentField.getTextField().setEditable(false); // Nonaktifkan input manual
            paymentField.setText(total.toString()); // Set nilai otomatis sesuai total
            changeLabel.setText("Rp. 0"); // Reset kembalian
        });

        // Tambahkan DocumentListener untuk menghitung kembalian (hanya jika cash dipilih)
        paymentField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateChange();
            }

            private void updateChange() {
                try {
                    if (cashRadio.isSelected()) { // Hanya hitung kembalian jika cash dipilih
                        BigDecimal payment = new BigDecimal(paymentField.getText());
                        BigDecimal change = payment.subtract(total);
                        changeLabel.setText("Rp. " + change);
                    }
                } catch (NumberFormatException ex) {
                    changeLabel.setText("Rp. 0");
                }
            }
        });

        // Tabel Informasi Obat
        String[] columnNames = {"NAMA OBAT", "JENIS OBAT", "JUMLAH", "SIGNA", "HARGA OBAT", "HARGA JASA", "TOTAL"};
        tableModel = new DefaultTableModel(columnNames, 0);
        drugTable = new CustomTable(tableModel); // Gunakan CustomTable
        for (Object[] drug : drugData) {
            tableModel.addRow(drug);
        }
        JScrollPane tableScrollPane = new JScrollPane(drugTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // Payment Button
        payButton = new JButton("Pay");
        payButton.addActionListener((ActionEvent e) -> {
            try {
                // Pastikan payment dan change adalah BigDecimal
                BigDecimal payment = new BigDecimal(paymentField.getText());
                BigDecimal change = payment.subtract(total);

                // Validasi jumlah pembayaran
                if (payment.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(this, "Jumlah Uang masih Kurang!", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (change.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, "Jumlah Uang masih Kurang!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Tampilkan kembalian
                    changeLabel.setText("Rp. " + change);

                    // Tampilkan pesan sukses
                    JOptionPane.showMessageDialog(this, "Pembayaran Sukses!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Update status ke "Selesai"
                    updateStatusToSelesai(idAntrian);

                    // Masukkan data ke pemasukan_harian
                    insertToPemasukanHarian(idAntrian);

                    // Refresh tabel di AntrianPasien
                    antrianPasien.refreshTableData();

                    // Tanyakan apakah pengguna ingin mencetak tagihan
                    int printChoice = JOptionPane.showConfirmDialog(this, "Do you want to print the bill?", "Print Bill", JOptionPane.YES_NO_OPTION);
                    if (printChoice == JOptionPane.YES_OPTION) {
                        // Tentukan file path untuk tagihan
                        String filePath = "bill.txt"; // Sesuaikan path file jika diperlukan
                        try {
                            // Generate file tagihan
                            printBill(filePath, payment, change);

                            // Cetak tagihan ke printer
                            printBillToPrinter(filePath);
                        } catch (IOException | PrinterException ex) {
                            JOptionPane.showMessageDialog(this, "Error printing bill: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    // Tutup form setelah selesai
                    JOptionPane.showMessageDialog(this, "Form akan ditutup.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    ShowModalCenter.closeCenterModal(parentFrame); // Menutup modal
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Jumlah Uang Tidak Valid!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add components to the frame
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.insets = new Insets(10, 10, 10, 10);
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.anchor = GridBagConstraints.CENTER; // Posisikan di tengah
        mainPanel.add(paymentPanel, mainGbc);

        add(mainPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        // Add both buttons (Pay and Refresh) to the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(payButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void openFormPembayaran(Object[] patientData, String idAntrian, String status, AntrianPasien antrianPasien) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        FormPembayaran formPembayaran = new FormPembayaran(patientData, idAntrian, status, antrianPasien);
        ShowModalCenter.showCenterModal(parentFrame, formPembayaran, true); // Jika membutuhkan konfirmasi
    }

    private List<Object[]> getDrugData(String idAntrian) {
        List<Object[]> drugData = new ArrayList<>();
        QueryExecutor executor = new QueryExecutor();
        String query = "Call invoice(?)"; // Pastikan query ini mengambil data yang diperlukan
        Object[] parameter = new Object[]{idAntrian};
        List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);

        for (Map<String, Object> result : results) {
            BigDecimal hargaJasa = result.get("harga_jasa") != null ? new BigDecimal(result.get("harga_jasa").toString()) : BigDecimal.ZERO;
            BigDecimal total = result.get("total") != null ? new BigDecimal(result.get("total").toString()) : BigDecimal.ZERO;
            BigDecimal hargaObat = total.subtract(hargaJasa); // Hitung harga obat sebagai total - harga jasa
            String signa = result.get("signa") != null ? result.get("signa").toString() : "";
            int jumlah = result.get("jumlah") != null ? Integer.parseInt(result.get("jumlah").toString()) : 0;

            Object[] drug = new Object[]{
                result.get("nama_obat"),
                result.get("nama_jenis_obat"),
                jumlah,
                signa,
                hargaObat, // Harga obat
                hargaJasa, // Harga jasa
                total // Total
            };
            drugData.add(drug);
        }
        return drugData;
    }

    private BigDecimal calculateTotal(List<Object[]> drugData) {
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] drug : drugData) {
            BigDecimal totalPerItem = (BigDecimal) drug[6]; // Ambil nilai total dari indeks 6
            if (totalPerItem != null) {
                total = total.add(totalPerItem); // Tambahkan ke total keseluruhan
            }
        }
        return total;
    }

    private void printBill(String filePath, BigDecimal payment, BigDecimal change) throws IOException {
        // Pastikan parameter yang diteruskan adalah BigDecimal
        if (!(payment instanceof BigDecimal) || !(change instanceof BigDecimal)) {
            throw new IllegalArgumentException("Payment and change must be of type BigDecimal.");
        }

        BillPrinter.printBill(
                filePath,
                (String) patientData[1], // Patient's name
                Username, // Logged-in user's name
                drugData, // Drug data
                total, // Total amount
                payment, // Payment amount
                change // Change amount
        );
    }

    private void printBillToPrinter(String filePath) throws PrinterException, IOException {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        if (printService != null) {
            printerJob.setPrintService(printService);
            printerJob.setJobName("Bill Print Job");

            // Create a Printable object
            PrintableBill printableBill = new PrintableBill(filePath);

            // Set the Printable object to the PrinterJob
            printerJob.setPrintable(printableBill);

            // Set custom paper sizesadadsadsadsawqeqe3
            PageFormat pageFormat = printerJob.defaultPage();
            Paper paper = new Paper();
            double width = 7.5 * 72 / 2.54; // 7.5 cm to inches
            double height = 11 * 72; // 11 inches
            paper.setSize(width, height);
            paper.setImageableArea(0, 0, width, height);
            pageFormat.setPaper(paper);

            // Set the PageFormat to the PrinterJob
            printerJob.setPrintable(printableBill, pageFormat);

            // Print the document
            if (printerJob.printDialog()) {
                printerJob.print();
            }
        } else {
            throw new PrinterException("No default print service found.");
        }
    }

    // Inner class to handle the printing of the bill
    private class PrintableBill implements Printable {

        private String filePath;
        private Image logo;

        public PrintableBill(String filePath) {
            this.filePath = filePath;
            try {
                // Load the logo image using getClass().getClassLoader().getResource
                logo = new ImageIcon(getClass().getClassLoader().getResource("assets/MAPOTEK LOGO.png")).getImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Set fixed-width font
            g2d.setFont(new Font("Courier New", Font.PLAIN, 6));

            int y = 0; // Current vertical position
            int lineHeight = g2d.getFontMetrics().getHeight(); // Line height
            int pageHeight = (int) pageFormat.getImageableHeight(); // Printable area height

            // Draw the logo image in the center (only on the first page)
            if (pageIndex == 0 && logo != null) {
                int logoWidth = 150; // Adjust logo width
                int logoHeight = 75; // Adjust logo height
                int x = (int) (pageFormat.getImageableWidth() - logoWidth) / 2;
                g2d.drawImage(logo, x, y, logoWidth, logoHeight, null);
                y += logoHeight + 10; // Add some space after the logo
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                int currentPage = 0;

                // Skip lines for previous pages
                while (currentPage < pageIndex) {
                    for (int i = 0; i < pageHeight / lineHeight; i++) {
                        if (reader.readLine() == null) {
                            return NO_SUCH_PAGE; // No more content
                        }
                    }
                    currentPage++;
                }

                // Print lines for the current page 
                while ((line = reader.readLine()) != null) {
                    if (y + lineHeight > pageHeight) {
                        return PAGE_EXISTS; // Move to the next page
                    }
                    g2d.drawString(line, 0, y);
                    y += lineHeight;
                }
            } catch (IOException e) {
                throw new PrinterException("Error reading bill file: " + e.getMessage());
            }

            return PAGE_EXISTS;
        }
    }

    private void updateStatusToSelesai(String idAntrian) {
        try {
            QueryExecutor executor = new QueryExecutor();
            // Use the correct column name (e.g., id_antrian)
            String query = "UPDATE antrian SET status_antrian = 'Selesai' WHERE id_antrian = ?";
            Object[] parameters = new Object[]{idAntrian};
            executor.executeUpdateQuery(query, parameters);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertToPemasukanHarian(String noAntrian) {
        try {
            QueryExecutor executor = new QueryExecutor();

            // Query to get id_pemeriksaan based on no_antrian
            String selectQuery = "SELECT id_pemeriksaan FROM pemeriksaan WHERE no_antrian = ?";
            Object[] selectParams = new Object[]{noAntrian};
            List<Map<String, Object>> results = executor.executeSelectQuery(selectQuery, selectParams);

            if (!results.isEmpty()) {
                // Get the id_pemeriksaan from the query result
                int idPemeriksaan = (Integer) results.get(0).get("id_pemeriksaan");

                // Insert into pemasukan_harian
                String insertQuery = "INSERT INTO pemasukan_harian (id_pemeriksaan, tanggal) VALUES (?, NOW())";
                Object[] insertParams = new Object[]{idPemeriksaan};
                executor.executeUpdateQuery(insertQuery, insertParams);
            } else {
                JOptionPane.showMessageDialog(this, "No id_pemeriksaan found for the given no_antrian.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inserting data into pemasukan_harian: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
