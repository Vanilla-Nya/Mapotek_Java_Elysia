package User;

import Components.CustomDatePicker;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Helpers.OnUserAddedListener;
import Helpers.TypeNumberHelper;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;

public class RegisterUser extends JPanel {

    private CustomTextField txtName, txtAddress, txtPhone, txtPassword, txtRFID;
    private Dropdown txtRole, txtGender;
    private OnUserAddedListener listener;
    private CustomDatePicker customDatePicker;

    public RegisterUser(OnUserAddedListener listener, DefaultTableModel model) {
        this.listener = listener;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(450, 500)); // Tambahkan ini

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Role field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        txtRole = new Dropdown(false, true, null);
        txtRole.setItems(List.of("Dokter", "Admin"), false, true, null); // Hanya "Dokter" dan "Admin"
        formPanel.add(txtRole, gbc);

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1;
        txtName = new CustomTextField("Masukan Nama", 20, 15, Optional.empty());
        formPanel.add(txtName, gbc);

        // Gender field
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1;
        txtGender = new Dropdown(false, true, null);
        txtGender.setItems(List.of("Laki-Laki", "Perempuan", "Tidak Bisa Dijelaskan"), false, true, null);
        formPanel.add(txtGender, gbc);

        // Address field
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Alamat:"), gbc);
        gbc.gridx = 1;
        txtAddress = new CustomTextField("Masukan Alamat", 20, 15, Optional.empty());
        formPanel.add(txtAddress, gbc);

        // Phone field
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("No.Telp:"), gbc);
        gbc.gridx = 1;
        txtPhone = new CustomTextField("Masukan No.Telp", 20, 15, Optional.empty());
        ((AbstractDocument) txtPhone.getTextField().getDocument()).setDocumentFilter(new TypeNumberHelper(13));
        formPanel.add(txtPhone, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new CustomTextField("Enter Password", 20, 15, Optional.of(true));  // Password field
        formPanel.add(txtPassword, gbc);

        // RFID field
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("RFID:"), gbc);
        gbc.gridx = 1;
        txtRFID = new CustomTextField("Enter RFID", 20, 15, Optional.empty());
        formPanel.add(txtRFID, gbc);

        // Submit button with RoundedButton
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton submitButton = new RoundedButton("Tambahkan");
        submitButton.setBackground(new Color(0, 150, 136));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserSessionCache cache = new UserSessionCache();
                String uuid = cache.getUUID();

                // Get data from text fields
                String id = String.valueOf(model.getRowCount() + 1);
                String role = (String) txtRole.getSelectedItem();
                String name = txtName.getText();
                String gender = (String) txtGender.getSelectedItem();
                String address = txtAddress.getText();
                String phone = txtPhone.getText();
                String password = txtPassword.getText();
                String rfid = txtRFID.getText().trim();

                // Jika RFID kosong, atur ke null
                if (rfid.isEmpty()) {
                    rfid = null;
                }

                if (uuid != null) {
                    // Validasi input lainnya
                    if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                        JOptionPane.showMessageDialog(RegisterUser.this, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Validasi role
                    if (role == null || (!role.equals("Dokter") && !role.equals("Admin"))) {
                        JOptionPane.showMessageDialog(RegisterUser.this, "Role harus Dokter atau Admin.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Step 1: Insert the user into the 'user' table
                    QueryExecutor queryExecutor = new QueryExecutor();
                    String insertUserQuery = "INSERT INTO user (nama_lengkap, username, jenis_kelamin, alamat, no_telp, password, rfid) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    boolean userInserted = queryExecutor.executeInsertQuery(insertUserQuery, new Object[]{name, name, gender, address, phone, password, rfid});

                    if (!userInserted) {
                        JOptionPane.showMessageDialog(RegisterUser.this, "Failed to add user.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Step 2: Get the generated user ID
                    String getLastInsertIdQuery = "SELECT id_user as userId from user where username = ?";
                    List<Map<String, Object>> result = queryExecutor.executeSelectQuery(getLastInsertIdQuery, new Object[]{name});
                    String userId = (String) result.get(0).get("userId");

                    // Step 3: Check if the user-role combination already exists
                    String checkUserRoleQuery = "SELECT COUNT(*) as count FROM user_role WHERE id_user = ? AND id_role = (SELECT id_role FROM role WHERE nama_role = ?)";
                    List<Map<String, Object>> checkResult = queryExecutor.executeSelectQuery(checkUserRoleQuery, new Object[]{userId, role});
                    int count = ((Long) checkResult.get(0).get("count")).intValue();

                    if (count > 0) {
                        JOptionPane.showMessageDialog(RegisterUser.this, "User-role combination already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Step 4: Insert into user_role table
                    String insertUserRoleQuery = "INSERT INTO user_role (id_user, id_role) SELECT ?, id_role FROM role WHERE nama_role = ?";
                    boolean userRoleInserted = queryExecutor.executeInsertQuery(insertUserRoleQuery, new Object[]{userId, role});

                    if (userRoleInserted) {
                        JOptionPane.showMessageDialog(RegisterUser.this, "User added successfully!");
                        if (listener != null) {
                            listener.onUserAdded(userId, role, name, gender, address, phone);
                        }

                        // Tutup modal setelah proses selesai
                        Components.ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(RegisterUser.this));
                    } else {
                        JOptionPane.showMessageDialog(RegisterUser.this, "Failed to assign role to user.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(RegisterUser.this, "Failed to Create User, User is Not Login", "Error", JOptionPane.ERROR_MESSAGE);
                    Components.ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(RegisterUser.this));
                }
            }
        });
        formPanel.add(submitButton, gbc);

        // Add form panel to main frame
        add(formPanel);
    }

    public static void showModalCenter(JFrame parent, OnUserAddedListener listener, DefaultTableModel model) {
        RegisterUser panel = new RegisterUser(listener, model);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }
}
