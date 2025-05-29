package Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ExpandableCard extends JPanel {
    private boolean isExpanded = false;
    private JPanel contentPanel;
    private JButton expandButton;
    private JLabel titleLabel; // Simpan sebagai variabel instance

    public ExpandableCard(String title, JPanel expandableContent, String expandButtonPosition) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        titleLabel = new JLabel(title, SwingConstants.CENTER); // Pusatkan teks judul
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 150, 243));

        // Tambahkan MouseListener ke titleLabel
        titleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Ubah kursor menjadi tangan
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleExpansion(); // Panggil toggleExpansion saat title diklik
            }
        });

        // Tambahkan titleLabel ke tengah headerPanel
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Expand/Collapse Button with Icon
        expandButton = new JButton("More", new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/arrow_up.png"))
                .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))); // Default icon with text
        expandButton.setHorizontalTextPosition(SwingConstants.LEFT); // Teks di sebelah kiri ikon
        expandButton.setFocusPainted(false);
        expandButton.setBorderPainted(false);
        expandButton.setContentAreaFilled(false);
        expandButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        expandButton.addActionListener(e -> toggleExpansion());

        // Add expand button based on position
        if ("right".equalsIgnoreCase(expandButtonPosition)) {
            headerPanel.add(expandButton, BorderLayout.EAST);
        } else if ("bottom".equalsIgnoreCase(expandButtonPosition)) {
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(Color.WHITE);
            bottomPanel.add(expandButton, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        add(headerPanel, BorderLayout.NORTH);

        // Content Panel (hidden by default)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setVisible(false);
        contentPanel.add(expandableContent, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void toggleExpansion() {
        isExpanded = !isExpanded;

        // Debugging log
        System.out.println("isExpanded: " + isExpanded);
        System.out.println("contentPanel.isVisible(): " + contentPanel.isVisible());
        System.out.println("contentPanel.getPreferredSize(): " + contentPanel.getPreferredSize());

        // Change the icon and text based on the state
        if (isExpanded) {
            expandButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/arrow_down.png"))
                    .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))); // Icon for collapse
            expandButton.setText("Less"); // Set text to "Less"
            animateExpansion();
        } else {
            expandButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/arrow_up.png"))
                    .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))); // Icon for expand
            expandButton.setText("More"); // Set text to "More"
            animateCollapse();
        }
    }

    private void animateExpansion() {
        contentPanel.setVisible(true); // Pastikan panel terlihat sebelum animasi dimulai
        int startHeight = 0;
        int endHeight = contentPanel.getPreferredSize().height;

        System.out.println("Expanding from height: " + startHeight + " to " + endHeight);

        new Thread(() -> {
            try {
                for (int height = startHeight; height <= endHeight; height += 10) {
                    final int currentHeight = height;
                    SwingUtilities.invokeLater(() -> {
                        contentPanel.setPreferredSize(new Dimension(contentPanel.getWidth(), currentHeight));
                        revalidate();
                        repaint();
                    });
                    Thread.sleep(10); // Kecepatan animasi (10ms per langkah)
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void animateCollapse() {
        int startHeight = contentPanel.getHeight();
        int endHeight = 0;

        System.out.println("Collapsing from height: " + startHeight + " to " + endHeight);

        new Thread(() -> {
            try {
                for (int height = startHeight; height >= endHeight; height -= 10) {
                    final int currentHeight = height;
                    SwingUtilities.invokeLater(() -> {
                        contentPanel.setPreferredSize(new Dimension(contentPanel.getWidth(), currentHeight));
                        revalidate();
                        repaint();
                    });
                    Thread.sleep(10); // Kecepatan animasi (10ms per langkah)
                }
                SwingUtilities.invokeLater(() -> {
                    contentPanel.setVisible(false); // Sembunyikan panel setelah animasi selesai
                    contentPanel.setPreferredSize(null); // Reset ukuran untuk memungkinkan ekspansi ulang
                });
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}