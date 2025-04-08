package Auth;

import Components.CustomTextField;
import Components.RoundedButton;
import Components.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;
import Components.TextButton;

public class OTPPage extends JPanel {

    public OTPPage() {
        // Menggunakan layout yang lebih fleksibel untuk memastikan elemen-elemen ditata dengan baik
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.CENTER_ALIGNMENT);

        // Membuat panel utama untuk menampung semua elemen
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Menambahkan label "Masukkan Kode OTP"
        JLabel otpLabel = new JLabel("Masukkan Kode OTP");
        otpLabel.setFont(new Font("Arial", Font.BOLD, 16));
        otpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        otpLabel.setForeground(Color.BLACK);
        mainPanel.add(otpLabel); // Menambahkan label ke panel utama

        // Membuat 4 TextField untuk kode OTP
        JPanel otpPanelContainer = new JPanel();
        otpPanelContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        for (int i = 0; i < 4; i++) {
            otpPanelContainer.add(createOtpPanel());
        }
        mainPanel.add(otpPanelContainer); // Menambahkan TextField OTP ke panel utama

        // Mengurangi jarak vertikal antara field dan tombol "Kirim Ulang OTP"
        mainPanel.add(Box.createVerticalStrut(5));

        // Menambahkan tombol "Kirim Ulang OTP" menggunakan TextButton
        mainPanel.add(createResendOtpButton());

        // Menambahkan mainPanel ke dalam panel utama
        add(mainPanel);
    }

    private JPanel createOtpPanel() {
        // Membuat RoundedPanel sebagai pembungkus TextField
        RoundedPanel otpPanel = new RoundedPanel(20, Color.WHITE);
        otpPanel.setPreferredSize(new Dimension(60, 60)); // Ukuran panel bundar

        // Membuat CustomTextField
        CustomTextField otpField = new CustomTextField("", 1, 10, Optional.empty());
        otpField.getTextField().setHorizontalAlignment(JTextField.CENTER);
        otpField.getTextField().setFont(new Font("Arial", Font.BOLD, 24));
        otpField.getTextField().setOpaque(false); // Transparan agar bundar terlihat
        otpField.getTextField().setBorder(BorderFactory.createEmptyBorder());

        // Mengatur input hanya angka atau huruf
        otpField.getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && !Character.isLetter(c)) {
                    e.consume(); // Hanya menerima angka atau huruf
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField source = (JTextField) e.getSource();
                if (source.getText().length() == 1) {
                    source.transferFocus(); // Pindah ke field berikutnya setelah 1 karakter
                }
            }
        });

        // Menambahkan TextField ke dalam panel bundar
        otpPanel.setLayout(new GridBagLayout());
        otpPanel.add(otpField.getTextField());

        return otpPanel;
    }

    private JPanel createResendOtpButton() {
        // Membuat TextButton untuk "Kirim Ulang OTP"
        TextButton resendButton = new TextButton() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                JLabel clickableText = (JLabel) getComponent(0); // Mengambil label di dalam TextButton
                clickableText.setText("Kirim Ulang OTP"); // Menyesuaikan teks label
            }
        };

        resendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Action saat tombol diklik
                System.out.println("OTP dikirim ulang!");
            }
        });

        // Mengembalikan panel TextButton
        return resendButton;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("OTP Page");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300); // Ukuran jendela
        frame.setContentPane(new OTPPage());
        frame.setVisible(true);
    }
}
