package Transaksi;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import Antrian.AntrianPasien;
import Components.CustomTextField;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Utils.BillPrinter;

public class FormPembayaran extends JFrame {

    private JLabel totalLabel;
    private JLabel changeLabel;
    private JTable drugTable;
    private DefaultTableModel tableModel;
    private JButton payButton;
    private CustomTextField paymentField;
    private BigDecimal total;
    private Object[] patientData;
    private List<Object[]> drugData;
    private String userLoginName;

    public FormPembayaran(Object[] patientData, String idAntrian, String status, AntrianPasien antrianPasien) {
        this.patientData = patientData;

        // Retrieve the logged-in user's name from UserSessionCache
        UserSessionCache userSessionCache = new UserSessionCache();
        userLoginName = userSessionCache.getusername();

        setTitle("Form Pembayaran");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Patient Information and Total Amount Panel
        JPanel infoTotalPanel = new JPanel(new GridLayout(2, 1, 2, 2)); // Reduced gaps
        infoTotalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Reduced borders
        
        JPanel patientInfoPanel = new JPanel(new GridLayout(1, 2, 2, 2)); // Reduced gaps
        patientInfoPanel.add(new JLabel("Name:"));
        patientInfoPanel.add(new JLabel((String) patientData[1]));
        infoTotalPanel.add(patientInfoPanel);

        total = calculateTotal(getDrugData(idAntrian));
        totalLabel = new JLabel("Total: Rp." + total);
        JPanel totalPanel = new JPanel(new GridLayout(1, 2, 2, 2)); // Reduced gaps
        totalPanel.add(new JLabel("Total:"));
        totalPanel.add(totalLabel);
        infoTotalPanel.add(totalPanel);

        // Payment Input Panel
        JPanel paymentPanel = new JPanel(new GridLayout(3, 2, 2, 2)); // Reduced gaps
        paymentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Reduced borders
        paymentPanel.add(new JLabel("Payment:"));
        paymentField = new CustomTextField("Enter payment amount", 20, 10, Optional.of(false));
        paymentPanel.add(paymentField);

        paymentPanel.add(new JLabel("Change:"));
        changeLabel = new JLabel("Rp. 0");
        paymentPanel.add(changeLabel);

        // Add DocumentListener to paymentField to update changeLabel automatically
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
                    BigDecimal payment = new BigDecimal(paymentField.getText());
                    BigDecimal change = payment.subtract(total);
                    changeLabel.setText("Rp. " + change);
                } catch (NumberFormatException ex) {
                    changeLabel.setText("Rp. 0");
                }
            }
        });

        // Drug Information Table
        String[] columnNames = {"NAMA OBAT", "JENIS OBAT", "JUMLAH", "HARGA", "SIGNA", "HARGA JASA", "TOTAL"};
        tableModel = new DefaultTableModel(columnNames, 0);
        drugTable = new JTable(tableModel);
        drugData = getDrugData(idAntrian);
        for (Object[] drug : drugData) {
            tableModel.addRow(drug);
        }
        JScrollPane tableScrollPane = new JScrollPane(drugTable);

        // Payment Button
        payButton = new JButton("Pay");
        payButton.addActionListener((ActionEvent e) -> {
            try {
                BigDecimal payment = new BigDecimal(paymentField.getText());
                BigDecimal change = payment.subtract(total);

                if (payment.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(this, "Payment amount must be greater than zero!", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (change.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, "Insufficient payment!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    changeLabel.setText("Rp. " + change);

                    JOptionPane.showMessageDialog(this, "Payment Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Update the status to "Selesai"
                    updateStatusToSelesai(idAntrian);

                    // Refresh the table in AntrianPasien
                    antrianPasien.refreshTableData();

                    // Ask the user if they want to print the bill
                    int printChoice = JOptionPane.showConfirmDialog(this, "Do you want to print the bill?", "Print Bill", JOptionPane.YES_NO_OPTION);
                    if (printChoice == JOptionPane.YES_OPTION) {
                        // Specify the file path for the bill
                        String filePath = "bill.txt"; // Adjust the file path as needed
                        try {
                            // Generate the bill file
                            printBill(filePath, payment, change);

                            // Print the bill to the printer
                            printBillToPrinter(filePath);
                        } catch (IOException | PrinterException ex) {
                            JOptionPane.showMessageDialog(this, "Error printing bill: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    // Close the payment form
                    dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid payment amount!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Enable or disable the "Pay" button based on the status
        if (!"Sudah Diperiksa".equals(status)) {
            payButton.setEnabled(false);
        }

        // Enable the "Pay" button for testing purposes
        payButton.setEnabled(true);

        // Add components to the frame
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(infoTotalPanel, BorderLayout.NORTH);
        mainPanel.add(paymentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        // Add both buttons (Pay and Refresh) to the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(payButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private List<Object[]> getDrugData(String idAntrian) {
        // Retrieve drug data for the patient from the database
        List<Object[]> drugData = new ArrayList<>();
        QueryExecutor executor = new QueryExecutor();
        String query = "Call invoice(?)";
        Object[] parameter = new Object[]{idAntrian};
        List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);

        for (Map<String, Object> result : results) {
            Object[] drug = new Object[]{
                result.get("nama_obat"),
                result.get("nama_jenis_obat"),
                result.get("jumlah"),
                result.get("total"),
                result.get("signa"),
                result.get("harga_jasa"),
                result.get("total"),
                result.get("nama")
            };
            drugData.add(drug);
        }
        return drugData;
    }

    private BigDecimal calculateTotal(List<Object[]> drugData) {
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] drug : drugData) {
            BigDecimal harga = (BigDecimal) drug[3];
            int jumlah = (int) drug[2];
            total = total.add(harga.multiply(BigDecimal.valueOf(jumlah)));
        }
        return total;
    }

    private void printBill(String filePath, BigDecimal payment, BigDecimal change) throws IOException {
        BillPrinter.printBill(
            filePath,
            (String) patientData[1], // Patient's name
            userLoginName,           // Logged-in user's name
            drugData,                // Drug data
            total,                   // Total amount
            payment,                 // Payment amount
            change                   // Change amount
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

            // Set custom paper size
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
}