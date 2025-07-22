package Auth.Auth_Animations;

import javax.swing.*;

import Auth.AuthFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginSuccessAnimation extends JPanel {
    private JPanel animationPanel;
    private JLabel mapotekLabel;
    private JLabel logoLabel;
    private JLabel welcomeLabel;
    private JFrame parentFrame;
    private Runnable onAnimationFinished;

    public LoginSuccessAnimation(JFrame parentFrame, String welcomeMessage, Runnable onAnimationFinished) {
        this.parentFrame = parentFrame;
        this.onAnimationFinished = onAnimationFinished;
        setLayout(null);

        // Panel animasi
        animationPanel = new JPanel();
        animationPanel.setLayout(null);
        animationPanel.setBackground(new Color(0, 0, 0, 150));
        animationPanel.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());
        add(animationPanel);

        // Logo MAPOTEK
        logoLabel = new JLabel(new ImageIcon(
            new ImageIcon(getClass().getClassLoader().getResource("assets/logo.png"))
                .getImage()
                .getScaledInstance(300, 300, Image.SCALE_SMOOTH) // Ubah ukuran logo menjadi 300x300
        ));
        logoLabel.setBounds((animationPanel.getWidth() - 300) / 2, animationPanel.getHeight() / 2 - 150, 300, 300);
        logoLabel.setVisible(true);
        animationPanel.add(logoLabel);

        // Label Selamat Datang
        welcomeLabel = new JLabel(welcomeMessage, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBounds(0, animationPanel.getHeight() - 100, animationPanel.getWidth(), 30);
        welcomeLabel.setVisible(true); // Langsung terlihat
        animationPanel.add(welcomeLabel);

        // Tulisan MAPOTEK
        mapotekLabel = new JLabel("MAPOTEK", SwingConstants.CENTER); // Teks di tengah
        mapotekLabel.setFont(new Font("Arial", Font.BOLD, 48));
        mapotekLabel.setForeground(new Color(0, 160, 136));
        mapotekLabel.setBounds((animationPanel.getWidth() - 300) / 2, animationPanel.getHeight() / 2 + 100, 300, 50); // Lebar tetap 300
        animationPanel.add(mapotekLabel);

        // Mulai animasi
        startAnimation(); // Ensure this is called
    }

    private void startAnimation() {
        Timer fadeTimer = new Timer(10, null); // Timer untuk animasi fade tulisan "MAPOTEK"

        // Variabel untuk animasi fade
        final int fadeStep = 5; // Kecepatan fade (semakin kecil semakin lambat)
        final int totalWidth = animationPanel.getWidth(); // Lebar total panel
        final int[] currentWidth = {0}; // Lebar saat ini untuk efek fade

        fadeTimer.addActionListener(e -> {
            if (currentWidth[0] < totalWidth) {
                // Perbarui lebar tulisan "MAPOTEK" untuk efek fade
                currentWidth[0] += fadeStep;
                mapotekLabel.setBounds((animationPanel.getWidth() - currentWidth[0]) / 2, animationPanel.getHeight() / 2 + 100, currentWidth[0], 50);
            } else {
                fadeTimer.stop();

                // Tutup frame animasi
                SwingUtilities.invokeLater(() -> {
                    parentFrame.dispose(); // Tutup frame animasi
                    if (onAnimationFinished != null) onAnimationFinished.run();
                });
            }

            // Paksa panel untuk diperbarui
            animationPanel.repaint();
        });

        fadeTimer.start(); // Mulai animasi fade tulisan "MAPOTEK"
    }
}