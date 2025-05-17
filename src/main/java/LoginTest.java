import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginTest extends JFrame {
    private int eyeX = 100, eyeY = 100; // posisi mata default
    private int mouseX = 100, mouseY = 100;
    private String status = "normal"; // normal, gagal, berhasil

    public LoginTest() {
        setTitle("Login Animasi Hewan");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Gambar kepala
                g.setColor(Color.ORANGE);
                g.fillOval(80, 80, 200, 200);

                // Gambar mata (bergerak mengikuti mouse)
                int dx = mouseX - (eyeX + 20);
                int dy = mouseY - (eyeY + 20);
                double dist = Math.sqrt(dx*dx + dy*dy);
                int maxMove = 10;
                int moveX = (int)(dx / (dist == 0 ? 1 : dist) * maxMove);
                int moveY = (int)(dy / (dist == 0 ? 1 : dist) * maxMove);

                g.setColor(Color.WHITE);
                g.fillOval(eyeX, eyeY, 40, 40); // mata kiri
                g.fillOval(eyeX + 60, eyeY, 40, 40); // mata kanan

                g.setColor(Color.BLACK);
                g.fillOval(eyeX + 15 + moveX, eyeY + 15 + moveY, 10, 10); // pupil kiri
                g.fillOval(eyeX + 75 + moveX, eyeY + 15 + moveY, 10, 10); // pupil kanan

                // Mulut (ekspresi)
                if (status.equals("normal")) {
                    g.drawArc(eyeX + 20, eyeY + 60, 60, 30, 0, -180); // senyum tipis
                } else if (status.equals("gagal")) {
                    g.drawArc(eyeX + 20, eyeY + 80, 60, 30, 0, 180); // mulut sedih
                } else if (status.equals("berhasil")) {
                    g.drawArc(eyeX + 20, eyeY + 60, 60, 30, 0, -180); // senyum lebar
                    g.drawLine(eyeX + 40, eyeY + 90, eyeX + 80, eyeY + 90); // garis tambahan
                }
            }
        };

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                panel.repaint();
            }
        });

        JTextField userField = new JTextField(10);
        JPasswordField passField = new JPasswordField(10);
        JButton loginBtn = new JButton("Login");

        loginBtn.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (user.equals("user") && pass.equals("pass")) {
                status = "berhasil";
            } else {
                status = "gagal";
            }
            panel.repaint();
        });

        JPanel formPanel = new JPanel();
        formPanel.add(new JLabel("User:"));
        formPanel.add(userField);
        formPanel.add(new JLabel("Pass:"));
        formPanel.add(passField);
        formPanel.add(loginBtn);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginTest().setVisible(true));
    }
}