package Auth;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class AuthFrame extends JFrame {
    // private CardLayout cardLayout; // HAPUS
    private JPanel cardPanel;

    public AuthFrame() {
        setTitle("Mapotek Auth");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        cardPanel = new JPanel(null); // null layout untuk animasi manual
        cardPanel.setBackground(Color.WHITE);

        Login loginPanel = new Login(this); // Pastikan 'this' adalah instance AuthFrame
        Register registerPanel = new Register(this);

        loginPanel.setBounds(0, 0, 1280, 720);
        registerPanel.setBounds(0, 0, 1280, 720);
        cardPanel.add(loginPanel);
        cardPanel.add(registerPanel);

        // Atur posisi dan visibilitas awal
        SwingUtilities.invokeLater(() -> {
            loginPanel.setVisible(true);  // Tampilkan login panel
            registerPanel.setVisible(false); // Sembunyikan register panel
            registerPanel.setLocation(cardPanel.getWidth(), 0); // Pastikan register di luar layar
            cardPanel.repaint();
            cardPanel.revalidate();
        });

        add(cardPanel);
        // cardLayout.show(cardPanel, "login"); // HAPUS
    }

    public void animatePanelSwitch(JPanel from, JPanel to) {
        int width = cardPanel.getWidth();
        to.setLocation(width, 0);
        to.setVisible(true);

        Timer timer = new Timer(5, null);
        timer.addActionListener(e -> {
            int x = to.getLocation().x;
            if (x <= 0) {
                to.setLocation(0, 0);
                from.setLocation(-width, 0);
                from.setVisible(false); // Pastikan panel sebelumnya disembunyikan
                timer.stop();
            } else {
                to.setLocation(Math.max(0, x - 40), 0);
                from.setLocation(to.getLocation().x - width, 0);
                cardPanel.repaint(); // Perbarui tampilan
            }
        });
        timer.start();
    }

    public void showLogin() {
        for (Component c : cardPanel.getComponents()) {
            c.setVisible(false); // Sembunyikan semua panel
        }
        cardPanel.getComponent(0).setVisible(true); // Tampilkan login panel
        animatePanelSwitch((JPanel) cardPanel.getComponent(1), (JPanel) cardPanel.getComponent(0));
    }

    public void showRegister() {
        for (Component c : cardPanel.getComponents()) {
            c.setVisible(false); // Sembunyikan semua panel
        }
        cardPanel.getComponent(1).setVisible(true); // Tampilkan register panel
        animatePanelSwitch((JPanel) cardPanel.getComponent(0), (JPanel) cardPanel.getComponent(1));
    }

    public void resetToLogin() {
        // Pastikan hanya panel login yang visible
        cardPanel.getComponent(0).setVisible(true);  // Login panel
        cardPanel.getComponent(1).setVisible(false); // Register panel
        cardPanel.getComponent(0).setLocation(0, 0);
        cardPanel.getComponent(1).setLocation(cardPanel.getWidth(), 0);
        cardPanel.repaint();
        cardPanel.revalidate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuthFrame().setVisible(true));
    }
}