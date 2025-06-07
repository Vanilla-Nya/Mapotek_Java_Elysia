package User;

import Components.CustomDatePicker;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;
import javax.swing.text.AbstractDocument;
import Helpers.TypeNumberHelper;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import java.util.Map;

public class EditUser extends JPanel {

    private CustomTextField txtName, txtphoneNum, txtAddress, txtRFID;
    private Dropdown cbGender, txtRole;
    private OnUserUpdatedListener listener;
    private CustomDatePicker customDatePicker;

    public EditUser(String userId, OnUserUpdatedListener listener) {
        QueryExecutor executor = new QueryExecutor();
        // Query to get user data by ID
        String query = "SELECT user.*, role.nama_role as role FROM user JOIN user_role ON user.id_user = user_role.id_user JOIN role ON user_role.id_role = role.id_role WHERE user.id_user = ?";
        
        // Execute the query with the userId parameter
        List<Map<String, Object>> result = executor.executeSelectQuery(query, new Object[]{userId});
        
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Exit the constructor if no user found
        }
        
        // Extract user data from the result
        Map<String, Object> userData = result.get(0);
        String id = (String) userData.get("id_user");
        String name = userData.get("nama_lengkap") != null ? (String) userData.get("nama_lengkap") : "";
        String role = userData.get("role") != null ? (String) userData.get("role") : "";
        String gender = userData.get("jenis_kelamin") != null ? (String) userData.get("jenis_kelamin") : "";
        String address = userData.get("alamat") != null ? (String) userData.get("alamat") : "";
        String phone = userData.get("no_telp") != null ? (String) userData.get("no_telp") : "";
        String rfid = userData.get("rfid") != null ? (String) userData.get("rfid") : "";
        
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

        // User Name 
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Name"), gbc);
        gbc.gridx = 1;
        txtName = new CustomTextField("Name", 20, 15, Optional.empty());
        formPanel.add(txtName, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Role"), gbc);
        gbc.gridx = 1;
        txtRole = new Dropdown(false, false, role);
        txtRole.setItems(List.of("Dokter", "Admin"), false, false, role); // Hanya "Dokter" dan "Admin"
        formPanel.add(txtRole, gbc);

        // Gender
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Gender"), gbc);
        gbc.gridx = 1;
        cbGender = new Dropdown(false, false, gender);
        cbGender.setItems(List.of("Laki-Laki", "Perempuan", "Tidak Bisa Dijelaskan"), false, false, gender);
        formPanel.add(cbGender, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Address"), gbc);
        gbc.gridx = 1;
        txtAddress = new CustomTextField("Address", 20, 15, Optional.empty());
        formPanel.add(txtAddress, gbc);

        // Telp Number
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Telp Number"), gbc);
        gbc.gridx = 1;
        txtphoneNum = new CustomTextField("Telp Number", 20, 15, Optional.empty());
        ((AbstractDocument) txtphoneNum.getTextField().getDocument()).setDocumentFilter(new TypeNumberHelper(13)); // Limit to 13 digits
        formPanel.add(txtphoneNum, gbc);

        // RFID field
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("RFID"), gbc);
        gbc.gridx = 1;
        txtRFID = new CustomTextField("RFID", 20, 15, Optional.empty());
        formPanel.add(txtRFID, gbc);

        // Submit button with RoundedButton
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton btnSave = new RoundedButton("Submit");
        btnSave.setBackground(new Color(0, 150, 136));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String updatedName = txtName.getText();
                String updateRole = (String) txtRole.getSelectedItem();
                String updatedGender = (String) cbGender.getSelectedItem();
                String updatedAddress = txtAddress.getText();
                String updatedPhone = txtphoneNum.getText();
                String updatedRFID = txtRFID.getText().trim();

                if (updatedRFID.isEmpty()) {
                    updatedRFID = null; // Atur RFID ke null jika kosong
                }

                UserSessionCache cache = new UserSessionCache();
                String uuid = cache.getUUID();

                if (uuid != null) {
                    // Validasi role
                    if (updateRole == null || (!updateRole.equals("Dokter") && !updateRole.equals("Admin"))) {
                        JOptionPane.showMessageDialog(EditUser.this, "Role harus Dokter atau Admin.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Step 2: Check if the role exists in the role table
                    String checkRoleQuery = "SELECT id_role FROM role WHERE nama_role = ?";
                    List<Map<String, Object>> roleCheckResult = executor.executeSelectQuery(checkRoleQuery, new Object[]{updateRole});

                    int roleId = (int) roleCheckResult.get(0).get("id_role");

                    // Step 3: Update the user_role table
                    String updateUserRoleQuery = "UPDATE user_role SET id_role = ? WHERE id_user = ?";
                    boolean roleUpdated = executor.executeUpdateQuery(updateUserRoleQuery, new Object[]{roleId, userId});

                    if (!roleUpdated) {
                        JOptionPane.showMessageDialog(EditUser.this, "Failed to update user role.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Step 4: Update the user table
                    String updateQuery = "UPDATE user SET username = ?, jenis_kelamin = ?, no_telp = ?, alamat = ?, rfid = ? WHERE id_user = ?";
                    boolean success = executor.executeUpdateQuery(updateQuery, new Object[]{
                        updatedName, updatedGender, updatedPhone, updatedAddress, updatedRFID, userId
                    });
                    
                    if (success) {
                        JOptionPane.showMessageDialog(EditUser.this, "User data updated successfully.");
                        if (listener != null) {
                            listener.onUserUpdated(updatedName, updateRole, updatedGender, updatedPhone, updatedAddress, updatedRFID);
                        }

                        // Tutup modal setelah proses selesai
                        Components.ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(EditUser.this));
                    } else {
                        JOptionPane.showMessageDialog(EditUser.this, "Failed to update user data.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(EditUser.this, "Failed to Edit User, User is Not Login", "Error", JOptionPane.ERROR_MESSAGE);
                    Components.ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(EditUser.this));
                }
            }
        });
        formPanel.add(btnSave, gbc);

        // Add form panel to main frame
        add(formPanel);

        // Set initial values
        txtName.setText(name);
        txtRole.setSelectedItem(role);
        cbGender.setSelectedItem(gender);
        txtphoneNum.setText(phone);
        txtAddress.setText(address);
        txtRFID.setText(rfid);
    }

    // Tambahkan static method untuk showModalCenter
    public static void showModalCenter(JFrame parent, String userId, OnUserUpdatedListener listener) {
        EditUser panel = new EditUser(userId, listener);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }

    // Listener interface for updating the user data
    public interface OnUserUpdatedListener {
        void onUserUpdated(String name, String role, String gender, String phone, String address, String rfid);
    }
}
