package TransaksiDiagnosa;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import Components.CustomDialog;
import Components.CustomPanel;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.RoundedBorder;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import Global.UserSessionCache;

public class TransaksiDiagnosa extends JFrame {

    private DefaultTableModel model;
    Object[][] data = {};
    private CustomTextField hargaJasa;
    private CustomPanel mainPanel;
    private RoundedButton nextButton, finalButton;
    private CustomPanel diagnosisPanel, drugDataPanel;
    private OnPemeriksaanUpdatedListener listener;
    private JScrollPane scrollPane;
    private CustomPanel patientDataPanel;
    private JLabel totalLabel;
    private double total;
    private int previousValue = 0;
    private long id_pemeriksaan;
    java.util.List idList = new ArrayList<>();
    private Object[] patientData;
    private List<Object[]> drugData;
    private String id_antrian;

    public TransaksiDiagnosa(OnPemeriksaanUpdatedListener listener, Object[] dataFromParent) {
        this.listener = listener;

        id_antrian = dataFromParent[0].toString(); // Ambil id_antrian dari dataFromParent

        QueryExecutor executor = new QueryExecutor();
        setTitle("Medical Diagnosis Form");
        setSize(720, 720); // Increase size for more space
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main Panel with BoxLayout
        mainPanel = new CustomPanel(30);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(240, 240, 240)); // Light gray background for the main panel
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create Patient Data Panel
        patientDataPanel = new CustomPanel(30);
        patientDataPanel.setLayout(new BoxLayout(patientDataPanel, BoxLayout.Y_AXIS));  // Use BoxLayout for vertical layout
        patientDataPanel.add(Box.createVerticalStrut(10));

        // Add padding (insets) to the panel
        Border border = BorderFactory.createCompoundBorder(new RoundedBorder(30), BorderFactory.createEmptyBorder(0, 20, 0, 20));;
//        patientDataPanel.setBorder(border);

        // Title for Patient Data (centered)
        JLabel patientTitleLabel = new JLabel("Patient Information");
        patientTitleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        patientTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // Center the title
        patientDataPanel.add(patientTitleLabel);

        // Create a panel for patient info with left alignment
        JPanel patientInfoPanel = new JPanel();
        patientInfoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));  // Left-align patient info

        // Example patient data labels (aligned to the left)
        JLabel nameLabel = new JLabel("Name: " + dataFromParent[3]);
        patientInfoPanel.add(nameLabel);

        JLabel ageLabel = new JLabel("Age: " + dataFromParent[7]);
        patientInfoPanel.add(ageLabel);

        if (dataFromParent.length > 9) {
            JLabel genderLabel = new JLabel("Gender: " + dataFromParent[5]);
            patientInfoPanel.add(genderLabel);
        } else {
            System.err.println("Data tidak memiliki elemen pada indeks 9.");
        }

        patientInfoPanel.setBackground(Color.WHITE);
        patientInfoPanel.setMaximumSize(new Dimension(500, 50));
        // Add the patient info panel to the patient data panel
        patientDataPanel.add(patientInfoPanel);

        patientDataPanel.add(Box.createVerticalStrut(10));  // Space before next section

        // Add the Patient Data panel to the main panel
        mainPanel.add(patientDataPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Create Diagnosis Panel
        diagnosisPanel = new CustomPanel(30);
        diagnosisPanel.setBorder(border);
        diagnosisPanel.add(Box.createVerticalStrut(10));
        diagnosisPanel.setLayout(new BoxLayout(diagnosisPanel, BoxLayout.Y_AXIS));
        diagnosisPanel.setBackground(Color.WHITE);

        JLabel diagnosisLabel = new JLabel("Diagnosis Information");
        diagnosisLabel.setFont(new Font("Arial", Font.BOLD, 22));
        diagnosisLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // Center the title
        diagnosisPanel.add(diagnosisLabel);
        diagnosisPanel.add(Box.createVerticalStrut(10));

        // Multi-line diagnosis input (using JTextArea)
        JTextArea diagnosisTextArea = new JTextArea(2, 5);
        // Set the JTextArea border to null to remove the default border
        diagnosisTextArea.setBorder(BorderFactory.createEmptyBorder());
        diagnosisTextArea.setBorder(new RoundBorder(15));
        diagnosisTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane diagnosisTextAreaScroll = new JScrollPane(diagnosisTextArea);
        diagnosisTextAreaScroll.setBorder(BorderFactory.createEmptyBorder());
        diagnosisTextAreaScroll.setMaximumSize(new Dimension(600, 120));
        diagnosisPanel.add(diagnosisTextAreaScroll);

        // "Next" button
        nextButton = new RoundedButton("Next");
        nextButton.setFont(new Font("Arial", Font.BOLD, 14));
        nextButton.setBackground(new Color(76, 175, 80));  // Green background for Next button
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        diagnosisPanel.add(Box.createVerticalStrut(10));  // Space before button
        diagnosisPanel.add(nextButton);
        diagnosisPanel.add(Box.createVerticalStrut(10));

        // Create Drug Data Panel
        drugDataPanel = new CustomPanel(30);
        drugDataPanel.setBorder(border);
        drugDataPanel.setLayout(new BoxLayout(drugDataPanel, BoxLayout.Y_AXIS));  // Use BoxLayout for vertical layout
        drugDataPanel.setBackground(Color.WHITE);
        drugDataPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        drugDataPanel.setPreferredSize(new Dimension(680, 400));
        drugDataPanel.add(Box.createVerticalStrut(10));

        // Title for DrugData (centered)
        JLabel drugLabel = new JLabel("Drug Recommendations");
        drugLabel.setFont(new Font("Arial", Font.BOLD, 22));
        drugLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        drugDataPanel.add(drugLabel);
        drugDataPanel.add(Box.createVerticalStrut(10));

        // Create a panel for Drug info with left alignment
        JPanel drugInfoPanel = new JPanel();
        drugInfoPanel.setLayout(new BoxLayout(drugInfoPanel, BoxLayout.Y_AXIS));  // Left-align patient info
        String[] columnNames = {"NAMA OBAT", "JENIS OBAT", "JUMLAH", "HARGA", "SIGNA", "AKSI"};
        model = new DefaultTableModel(data, columnNames) {
            public boolean isCellEditable(int row, int column) {
                return column == 5;  // Only "AKSI" column is editable
            }
        };
        CustomTable table = new CustomTable(model);
        table.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
        table.getColumn("AKSI").setCellEditor(new ActionCellEditor(model));
        table.getColumn("AKSI").setMinWidth(150);

        JScrollPane drugPanelChild = new JScrollPane(table);

        drugInfoPanel.add(drugPanelChild);

        drugInfoPanel.setBackground(Color.WHITE);
        drugInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        drugInfoPanel.setMaximumSize(new Dimension(680, 200));
        // Add the patient info panel to the patient data panel
        drugDataPanel.add(drugInfoPanel);
        drugDataPanel.add(Box.createVerticalStrut(10));

        RoundedButton addButton = new RoundedButton("Tambah");
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setBackground(new Color(33, 150, 243));  // Blue background for Finish button
        addButton.setForeground(Color.WHITE);
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(e -> {
            // Instantiate and show the AddDrugs form
            AddDrugs addDrugsForm = new AddDrugs(this);
            addDrugsForm.setVisible(true);
        });

        hargaJasa = new CustomTextField("Harga Jasa", 20, 30, Optional.empty());
        totalLabel = new JLabel("Total: Rp.0");

        hargaJasa.setTextChangeListener(newValue -> {
            int value = Integer.parseInt(newValue);
            total = total - previousValue + value;
            previousValue = value;
            totalLabel.setText("Total: Rp." + total);
        });

        finalButton = new RoundedButton("Finish");
        finalButton.setFont(new Font("Arial", Font.BOLD, 14));
        finalButton.setBackground(new Color(33, 150, 243));  // Blue background for Finish button
        finalButton.setForeground(Color.WHITE);
        finalButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        finalButton.addActionListener((ActionEvent e) -> {
            try {
                // Validasi hargaJasa
                String hargaJasaText = hargaJasa.getText().trim();
                if (hargaJasaText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Harga Jasa harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double hargaJasaValue;
                try {
                    hargaJasaValue = Double.parseDouble(hargaJasaText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Harga Jasa harus berupa angka yang valid!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Masukkan data ke tabel pemeriksaan
                String idPemeriksaan = "P" + System.currentTimeMillis(); // Generate ID pemeriksaan
                String diagnosis = diagnosisTextArea.getText().trim();
                insertPemeriksaan(idPemeriksaan, id_antrian, diagnosis, hargaJasaValue);

                // Masukkan data ke tabel pemeriksaan_obat
                for (int i = 0; i < model.getRowCount(); i++) {
                    String namaObat = (String) model.getValueAt(i, 0); // Nama obat
                    int jumlah = (Integer) model.getValueAt(i, 2); // Jumlah
                    double harga = (Double) model.getValueAt(i, 3); // Harga

                    // Dapatkan ID obat berdasarkan nama obat (implementasikan fungsi getIdObatByName jika diperlukan)
                    String idObat = getIdObatByName(namaObat);
                    if (idObat != null) {
                        insertPemeriksaanObat(idPemeriksaan, idObat, jumlah, harga);
                    } else {
                        System.err.println("ID Obat tidak ditemukan untuk nama obat: " + namaObat);
                    }
                }

                // Update status antrian menjadi "Selesai Diperiksa"
                String updateStatusQuery = "UPDATE antrian SET status_antrian = ? WHERE id_antrian = ?";
                Object[] updateStatusParams = new Object[]{"Selesai Diperiksa", id_antrian};
                boolean isStatusUpdated = QueryExecutor.executeUpdateQuery(updateStatusQuery, updateStatusParams);

                if (isStatusUpdated) {
                    System.out.println("Status antrian berhasil diperbarui menjadi 'Selesai Diperiksa'.");
                    JOptionPane.showMessageDialog(this, "Proses selesai. Data berhasil disimpan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose(); // Menutup form TransaksiDiagnosa
                } else {
                    System.err.println("Gagal memperbarui status antrian.");
                    JOptionPane.showMessageDialog(this, "Gagal memperbarui status antrian.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                System.err.println("Unexpected error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));  // Use BoxLayout for vertical layout
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(finalButton);

        JPanel totalPanel = new JPanel();
        totalPanel.setSize(new Dimension(400, 20));
        totalPanel.setLayout(new GridBagLayout());  // Using GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        totalPanel.setBackground(Color.WHITE);

        // Place hargaJasa on the left
        gbc.gridx = 0;
        gbc.weightx = 0.5;  // Give some weight for left alignment
        gbc.anchor = GridBagConstraints.WEST;
        totalPanel.add(hargaJasa, gbc);

        // Place totalLabel on the right
        gbc.gridx = 1;
        gbc.weightx = 0.5;  // Give some weight for right alignment
        gbc.anchor = GridBagConstraints.EAST;
        totalPanel.add(totalLabel, gbc);

        drugDataPanel.add(totalPanel);
        drugDataPanel.add(Box.createVerticalStrut(10));
        drugDataPanel.add(buttonPanel);

        drugDataPanel.add(Box.createVerticalStrut(10));
        drugDataPanel.setMaximumSize(new Dimension(680, 300));

        // Initially set the drug panel to be invisible
        drugDataPanel.setVisible(false);

        // Add panels to the main panel
        mainPanel.add(diagnosisPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(drugDataPanel);

        // Add the scrollable panel to the frame
        scrollPane = new JScrollPane(mainPanel);
        add(scrollPane);
        setBackground(Color.WHITE);

        setLocationRelativeTo(null);
        setVisible(true);

        // Action listener for the "Next" button
        nextButton.addActionListener((ActionEvent e) -> {
            // Switch to the drug recommendation panel by making it visible
            drugDataPanel.setVisible(true);
            nextButton.setVisible(false);
            finalButton.setEnabled(true); // Enable the finish button
        });
    }

    public Object[] getPatientData() {
        return patientData;
    }

    public List<Object[]> getDrugData() {
        return drugData;
    }

    // Custom border class for rounded corners
    static class RoundBorder implements Border {

        private int radius;

        public RoundBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius + 2, this.radius + 2, this.radius + 2, this.radius + 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            // Set color and draw rounded rectangle border
            g.setColor(Color.LIGHT_GRAY);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    // Renderer for "AKSI" column
    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        public ActionCellRenderer() {
            JButton hapusButton = new RoundedButton("HAPUS");
            hapusButton.setBackground(new Color(255, 51, 51));
            hapusButton.setForeground(Color.WHITE);
            hapusButton.setFocusPainted(false);
            add(hapusButton);
            setBackground(Color.WHITE);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private int getCurrentStock(String drugName) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String existingDrugName = (String) model.getValueAt(i, 0);  // Get the drug name in the first column
            if (existingDrugName.equalsIgnoreCase(drugName)) {
                // Return the current stock (quantity) from the 3rd column
                return (Integer) model.getValueAt(i, 2);
            }
        }
        return 0;  // If drug doesn't exist, return 0 as the default stock value
    }

    public void addOrUpdateDrug(int id, String name, String type, int quantity, double price, String signa) {
        // Check if the drug already exists in the table
        if (isDrugExists(name)) {
            // If the drug exists, check if the new quantity is less than or equal to the current stock
            if (quantity <= getCurrentStock(name)) {
                // If stock input is less than or equal to the existing stock, display an error message and return
                JOptionPane.showMessageDialog(this, "The stock input is less than or equal to the existing stock.", "Error", JOptionPane.ERROR_MESSAGE);
                return;  // Stop the process here, don't add the drug again
            }
            // If the drug exists and the quantity is more, update the stock
            updateDrugStock(name, quantity);
        } else {
            // If the drug doesn't exist, add it as a new row with the specified stock, price, and signa
            Object[] newRow = {name, type, quantity, price, signa, ""};  // Default price 0.0 for new drug
            idList.add(id); // Saat menambahkan data ke tabel, tambahkan juga id_antrian ke idList
            model.addRow(newRow);
        }

        // Optionally, update the total price after adding/updating the drug
        updateTotalPrice();
    }

    private boolean isDrugExists(String drugName) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String existingDrugName = (String) model.getValueAt(i, 0); // Get the drug name in the first column
            if (existingDrugName.equalsIgnoreCase(drugName)) {
                return true;  // Drug exists in the table
            }
        }
        return false;  // Drug does not exist
    }

    private void updateDrugStock(String drugName, int quantityToAdd) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String existingDrugName = (String) model.getValueAt(i, 0); // Get the drug name in the first column
            if (existingDrugName.equalsIgnoreCase(drugName)) {
                // Update the stock by adding the new quantity to the existing quantity
                int currentQuantity = (Integer) model.getValueAt(i, 2);  // Get the current quantity (assumed to be in the 3rd column)
                model.setValueAt(currentQuantity + quantityToAdd, i, 2); // Set the new quantity
                return;  // Exit the method after updating
            }
        }
    }

    private void updateTotalPrice() {
        for (int i = 0; i < model.getRowCount(); i++) {
            double price = (Double) model.getValueAt(i, 3);  // Get the price from the 4th column (as Double)
            total += price;  // Total = price * quantity
        }
        totalLabel.setText("Total: Rp." + total);
    }

    // Editor for "AKSI" column
    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        JPanel panel;
        int row;
        DefaultTableModel model;

        public ActionCellEditor(DefaultTableModel model) {
            this.model = model;
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));

            JButton hapusButton = new RoundedButton("HAPUS");
            hapusButton.setBackground(new Color(255, 51, 51));
            hapusButton.setForeground(Color.WHITE);
            hapusButton.setFocusPainted(false);
            hapusButton.addActionListener((ActionEvent e) -> {
                // Create and show the confirmation dialog
                CustomDialog confirmDialog = new CustomDialog(null, "Apakah Anda yakin ingin menghapus obat ini?", "Konfirmasi Penghapusan");
                // Get the user's response
                int response = confirmDialog.showDialog();

                // If the user clicks "Yes"
                if (response == JOptionPane.YES_OPTION) {
                    // Check if the row index is valid before attempting to remove
                    if (row >= 0 && row < model.getRowCount()) {
                        JTable table = (JTable) panel.getParent();

                        // Check if the table was in an editing state and stop editing
                        if (table.isEditing()) {
                            table.getCellEditor().stopCellEditing();  // Stop editing the cell if it is being edited
                            System.out.println("Cell editing stopped.");
                        }

                        // Log the row index and row count before removal for debugging
                        System.out.println("Attempting to remove row: " + row);

                        // Proceed with row removal if index is valid
                        model.removeRow(row);

                        // Refresh the table view after the row is removed
                        table.revalidate();
                        table.repaint();

                        // Handle edge case if the last row was removed
                        if (model.getRowCount() == 0) {
                            System.out.println("Last row deleted, table is empty.");
                        } else {
                            // After removing the last row, we might want to focus or highlight the new "last row"
                            int lastRowIndex = model.getRowCount() - 1;
                            table.setRowSelectionInterval(lastRowIndex, lastRowIndex);
                        }
                    } else {
                        System.out.println("Invalid row index for deletion: " + row);
                    }
                } else {
                    // If the user clicked "No", simply log that the deletion was canceled
                    System.out.println("Deletion canceled by user.");
                }
            });
            panel.add(hapusButton);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Ensure row index is set properly
            if (row >= 0 && row < table.getRowCount()) {
                this.row = row;  // Set the row index when the cell enters editing mode
            } else {
                System.out.println("Invalid row index passed to cell editor");
            }
            return panel;
        }

        public Object getCellEditorValue() {
            return null;
        }
    }

    // Listener interface for updating the pemeriksaan data
    public interface OnPemeriksaanUpdatedListener {

        void onPasienUpdated(String noAntrian, String idPasien, String nama_pasien, String status);
    }

    private String getIdDetailObatByClosestExpiry(String idObat) {
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT id_detail_obat, harga_jual FROM detail_obat WHERE id_obat = ? AND tanggal_expired > CURDATE() ORDER BY tanggal_expired ASC LIMIT 1";
        Object[] parameter = new Object[]{idObat};
        List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);

        if (!results.isEmpty()) {
            return results.get(0).get("id_detail_obat").toString();
        } else {
            System.err.println("ID Detail Obat tidak ditemukan untuk ID Obat: " + idObat);
            return null; // Jika tidak ditemukan, kembalikan null
        }
    }

    private void insertPemeriksaanObat(String idPemeriksaan, String idObat, int jumlah, double harga) {
        QueryExecutor executor = new QueryExecutor();
        String query = "INSERT INTO pemeriksaan_obat (id_pemeriksaan, id_obat, jumlah, harga) VALUES (?, ?, ?, ?)";
        Object[] parameters = new Object[]{idPemeriksaan, idObat, jumlah, harga};

        boolean isInserted = executor.executeUpdateQuery(query, parameters);
        if (isInserted) {
            System.out.println("Data berhasil dimasukkan ke tabel pemeriksaan_obat.");
        } else {
            System.err.println("Gagal memasukkan data ke tabel pemeriksaan_obat.");
        }
    }

    private void insertDetailPemeriksaan(String idPemeriksaan, String diagnosis, double hargaJasa) {
        QueryExecutor executor = new QueryExecutor();
        String query = "INSERT INTO detail_pemeriksaan (id_pemeriksaan, diagnosis, harga_jasa) VALUES (?, ?, ?)";
        Object[] parameters = new Object[]{idPemeriksaan, diagnosis, hargaJasa};

        boolean isInserted = executor.executeUpdateQuery(query, parameters);
        if (isInserted) {
            System.out.println("Data berhasil dimasukkan ke tabel detail_pemeriksaan.");
        } else {
            System.err.println("Gagal memasukkan data ke tabel detail_pemeriksaan.");
        }
    }

    private String getIdObatByName(String namaObat) {
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT id_obat FROM obat WHERE nama_obat = ?";
        Object[] parameters = new Object[]{namaObat};

        List<Map<String, Object>> results = executor.executeSelectQuery(query, parameters);
        if (!results.isEmpty()) {
            return results.get(0).get("id_obat").toString();
        } else {
            System.err.println("ID Obat tidak ditemukan untuk nama obat: " + namaObat);
            return null;
        }
    }

    private void insertPemeriksaan(String idPemeriksaan, String idAntrian, String diagnosis, double hargaJasa) {
        QueryExecutor executor = new QueryExecutor();
        String query = "INSERT INTO pemeriksaan (id_pemeriksaan, id_antrian, diagnosis, harga_jasa) VALUES (?, ?, ?, ?)";
        Object[] parameters = new Object[]{idPemeriksaan, idAntrian, diagnosis, hargaJasa};

        boolean isInserted = executor.executeUpdateQuery(query, parameters);
        if (isInserted) {
            System.out.println("Data berhasil dimasukkan ke tabel pemeriksaan.");
        } else {
            System.err.println("Gagal memasukkan data ke tabel pemeriksaan.");
        }
    }
}
