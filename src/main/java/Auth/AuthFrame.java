package Auth;

import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;

public class AuthFrame extends JFrame {
    // private CardLayout cardLayout; // HAPUS
    private JPanel cardPanel;

    public AuthFrame() {
        setTitle("Mapotek Auth");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        // cardLayout = new CardLayout(); // HAPUS
        cardPanel = new JPanel(null); // null layout untuk animasi manual
        cardPanel.setBackground(Color.WHITE); // opsional, biar tidak transparan

        Login loginPanel = new Login(this);
        Register registerPanel = new Register(this);

        loginPanel.setBounds(0, 0, 1280, 720);
        registerPanel.setBounds(0, 0, 1280, 720);
        cardPanel.add(loginPanel);
        cardPanel.add(registerPanel);
        registerPanel.setLocation(cardPanel.getWidth(), 0); // pastikan register di luar layar

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
                from.setVisible(false);
                timer.stop();
            } else {
                to.setLocation(Math.max(0, x - 40), 0);
                from.setLocation(to.getLocation().x - width, 0);
                cardPanel.repaint();
            }
        });
        timer.start();
    }

    public void showLogin() {
        Component current = null;
        for (Component c : cardPanel.getComponents()) {
            if (c.isVisible()) {
                current = c;
                break;
            }
        }
        animatePanelSwitch((JPanel) current, (JPanel) cardPanel.getComponent(0)); // 0 = login
    }

    public void showRegister() {
        Component current = null;
        for (Component c : cardPanel.getComponents()) {
            if (c.isVisible()) {
                current = c;
                break;
            }
        }
        animatePanelSwitch((JPanel) current, (JPanel) cardPanel.getComponent(1)); // 1 = register
    }

    public void resetToLogin() {
        // Pastikan hanya panel login yang visible
        cardPanel.getComponent(0).setVisible(true);  // Login panel
        cardPanel.getComponent(1).setVisible(false); // Register panel
        cardPanel.getComponent(0).setLocation(0, 0);
        cardPanel.getComponent(1).setLocation(cardPanel.getWidth(), 0);
        cardPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuthFrame().setVisible(true));
    }
}