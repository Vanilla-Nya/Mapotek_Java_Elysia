package Auth;

import Components.CustomTextField;
import Components.RoundedButton;
import Components.RoundedPanel;
import Auth.Login;
import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class UbahPassword extends JFrame {

    public UbahPassword() {
        // Set frame properties
        setTitle("Ubah Password");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLayout(new BorderLayout());

        // Left panel for logo
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(200, getHeight()));
        leftPanel.setLayout(new GridBagLayout());
        JLabel logoLabel = new JLabel("MAPOTEK");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        logoLabel.setForeground(new Color(0, 150, 136));
        leftPanel.add(logoLabel);

        // Right panel for registration form
        RoundedPanel rightPanel = new RoundedPanel(20, new Color(0, 150, 136));
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Ubah Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(titleLabel, gbc);

        // Username field
        CustomTextField username = new CustomTextField("Masukan Password Baru", 20, 15, Optional.empty());
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        rightPanel.add(username, gbc);

        // Nomer Telepon field
        CustomTextField nomertelepone = new CustomTextField("Konfirmasi Password", 20, 15, Optional.empty());
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        rightPanel.add(nomertelepone, gbc);

        // Panel untuk tombol
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Supaya panel transparan, mengikuti latar belakang
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0)); // Tata letak grid dengan 2 kolom dan jarak horizontal 10px

        // Next button
        RoundedButton nextbutton = new RoundedButton("Next");
        nextbutton.setBackground(new Color(76, 175, 80));
        nextbutton.setForeground(Color.WHITE);
        nextbutton.setFont(new Font("Arial", Font.BOLD, 16));
        buttonPanel.add(nextbutton);
        nextbutton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Ambil teks dari text field
                String passwordBaru = username.getText().trim();
                String konfirmasiPassword = nomertelepone.getText().trim();

                // Cek apakah field kosong
                if (passwordBaru.isEmpty() || konfirmasiPassword.isEmpty()) {
                    // Tampilkan notifikasi error
                    JOptionPane.showMessageDialog(
                            UbahPassword.this,
                            "Password dan Konfirmasi Password harus diisi!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } else if (!passwordBaru.equals(konfirmasiPassword)) {
                    // Cek apakah password dan konfirmasi password sama
                    JOptionPane.showMessageDialog(
                            UbahPassword.this,
                            "Password dan Konfirmasi Password tidak cocok!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    // Tampilkan notifikasi berhasil
                    JOptionPane.showMessageDialog(
                            UbahPassword.this,
                            "Password berhasil diubah!",
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    // Arahkan ke halaman Login
                    new Login().setVisible(true);
                    dispose(); // Tutup jendela UbahPassword
                }
            }
        });

        // Back button
        RoundedButton backButton = new RoundedButton("Back");
        backButton.setBackground(new Color(76, 175, 80));
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        buttonPanel.add(backButton);

        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Open the Login window when the login link is clicked
                new Login().setVisible(true);
                dispose(); // Close the Register window
            }
        });

        // Tambahkan panel tombol ke GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Panel tombol menempati seluruh lebar textfield
        gbc.insets = new Insets(10, 0, 0, 0); // Jarak atas untuk pemisahan
        rightPanel.add(buttonPanel, gbc);

        // Add panels to frame
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Make frame visible
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UbahPassword::new);
    }
}
