package Antrian;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import Components.Dropdown;
import Components.RoundedButton;
import Components.RoundedPanel;

public class EditAntrian extends JFrame{ 
    private JTextField idPasienField;
    private JTextField tanggalLahirField;
    private Dropdown statusDropdown; // Tambahkan dropdown untuk status

    public EditAntrian(DefaultTableModel model, int row) {
        setTitle("Edit Antrian");
        setSize(500, 350); // Adjusted frame size for extra dropdown
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Retrieve data from the table model
        String idPasien = (String) model.getValueAt(row, 1);
        String tanggalLahir = (String) model.getValueAt(row, 2);
        String currentStatus = (String) model.getValueAt(row, 3); // Asumsi status di kolom ke-3

        // Content Panel with form fields
        RoundedPanel contentPanel = new RoundedPanel(15, Color.WHITE);
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // ID Pasien Field
        JLabel idPasienLabel = new JLabel("ID Pasien:");
        idPasienLabel.setFont(new Font("Arial", Font.BOLD, 14));
        idPasienField = new JTextField(20);  
        idPasienField.setText(idPasien);
        idPasienField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(idPasienLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(idPasienField, gbc);
        
        // Tanggal Lahir Field
        JLabel tanggalLahirLabel = new JLabel("Tanggal Lahir:");
        tanggalLahirLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tanggalLahirField = new JTextField(20);  
        tanggalLahirField.setText(tanggalLahir);
        tanggalLahirField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(tanggalLahirLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(tanggalLahirField, gbc);

        // Status Dropdown Field
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusDropdown = new Dropdown(false, false, currentStatus);  
        statusDropdown.setItems(Arrays.asList("Pending", "Periksa", "Pembayaran", "Selesai"), false, false, currentStatus);
        statusDropdown.setSelectedItem(currentStatus); // Set status saat ini jika ada
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(statusDropdown, gbc);

        // Buttons (added to a separate panel for better alignment)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Make panel transparent to match the background
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Set horizontal gap and padding

        RoundedButton backButton = new RoundedButton("Kembali");
        backButton.setBackground(new Color(0, 51, 153));
        backButton.setForeground(Color.WHITE);
        backButton.setPreferredSize(new Dimension(100, 35)); // Set button size for consistency

        RoundedButton editButton = new RoundedButton("Edit");
        editButton.setBackground(new Color(255, 153, 0));
        editButton.setForeground(Color.WHITE);
        editButton.setPreferredSize(new Dimension(100, 35));

        // Back button action
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the frame
            }
        });

        // Edit button action
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newIdPasien = idPasienField.getText();
                String newTanggalLahir = tanggalLahirField.getText();
                String newStatus = (String) statusDropdown.getSelectedItem();

                if (!newIdPasien.isEmpty() && !newTanggalLahir.isEmpty()) {
                    model.setValueAt(newIdPasien, row, 1);
                    model.setValueAt(newTanggalLahir, row, 2);
                    model.setValueAt(newStatus, row, 3); // Update status in the model
                    dispose(); // Close the frame after saving
                } else {
                    JOptionPane.showMessageDialog(EditAntrian.this, "Harap isi semua data.");
                }
            }
        });

        // Add buttons to button panel
        buttonPanel.add(backButton);
        buttonPanel.add(editButton);

        // Add button panel to the main content panel with layout constraints
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Span across two columns for centering
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(buttonPanel, gbc);

        // Add components to the frame
        add(contentPanel, BorderLayout.CENTER);
        
        setVisible(true);
    }
}
