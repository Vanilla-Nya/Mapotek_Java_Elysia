package Components;

import javax.swing.*;
import java.awt.*;

public class CustomTabbedPane extends JPanel {
    private JPanel tabPanel;
    private JPanel contentPanel;
    private JButton[] tabButtons;
    private JComponent[] tabContents;
    private CardLayout cardLayout;
    private int currentIndex = -1; // Awal -1, agar animasi pertama berjalan

    public CustomTabbedPane(String[] titles, JComponent[] contents) {
        if (titles.length != contents.length || titles.length == 0) {
            throw new IllegalArgumentException("Titles and contents must have the same non-zero length");
        }

        setLayout(new BorderLayout());

        tabPanel = new JPanel(new GridLayout(1, titles.length));
        tabButtons = new JButton[titles.length];
        tabContents = contents;

        for (int i = 0; i < titles.length; i++) {
            JButton btn = new JButton(titles[i]);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            final int idx = i;
            btn.addActionListener(e -> {
                setContentAnimated(idx);
                highlightButton(idx);
            });
            tabButtons[i] = btn;
            tabPanel.add(btn);
        }

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        for (int i = 0; i < contents.length; i++) {
            contentPanel.add(contents[i], String.valueOf(i));
        }
        add(tabPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Tambahkan ini:
        contentPanel.removeAll();
        contentPanel.add(tabContents[0], BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        currentIndex = 0;
        highlightButton(0);
    }

    public void setContentAnimated(int newIndex) {
        if (newIndex == currentIndex) return;

        int width = contentPanel.getWidth();
        int height = contentPanel.getHeight();

        // Jika panel belum punya ukuran, tunda animasi sampai panel sudah terlihat
        if (width == 0 || height == 0) {
            SwingUtilities.invokeLater(() -> setContentAnimated(newIndex));
            return;
        }

        int direction = (currentIndex == -1 || newIndex > currentIndex) ? 1 : -1;
        JComponent oldComp;
        if (currentIndex == -1) {
            // Panel kosong sebagai oldComp untuk animasi pertama
            oldComp = new JPanel();
            oldComp.setOpaque(false);
        } else {
            oldComp = tabContents[currentIndex];
        }
        JComponent newComp = tabContents[newIndex];

        JPanel animationPanel = new JPanel(null);
        animationPanel.setBackground(contentPanel.getBackground());
        animationPanel.setPreferredSize(new Dimension(width, height));

        oldComp.setBounds(0, 0, width, height);
        newComp.setBounds(direction * width, 0, width, height);

        animationPanel.add(oldComp);
        animationPanel.add(newComp);

        contentPanel.removeAll();
        contentPanel.add(animationPanel);
        contentPanel.revalidate();
        contentPanel.repaint();

        final int totalStep = 24;
        Timer timer = new Timer(8, null); // ~120 FPS
        final int[] step = {0};

        timer.addActionListener(e -> {
            step[0]++;
            double progress = (double) step[0] / totalStep;
            // Ease-in-out cubic
            double eased = progress < 0.5
                ? 4 * progress * progress * progress
                : 1 - Math.pow(-2 * progress + 2, 3) / 2;
            int x = (int) (eased * width) * direction;
            oldComp.setLocation(x, 0);
            newComp.setLocation(x + (direction * width), 0);
            animationPanel.repaint();

            if (step[0] >= totalStep) {
                timer.stop();
                contentPanel.removeAll();
                contentPanel.add(newComp, BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
                currentIndex = newIndex;
            }
        });
        timer.start();
    }

    public void highlightButton(int selectedIdx) {
        for (int i = 0; i < tabButtons.length; i++) {
            if (i == selectedIdx) {
                tabButtons[i].setFont(tabButtons[i].getFont().deriveFont(Font.BOLD));
                tabButtons[i].setForeground(new Color(0, 150, 136));
                tabButtons[i].setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 150, 136)));
            } else {
                tabButtons[i].setFont(tabButtons[i].getFont().deriveFont(Font.PLAIN));
                tabButtons[i].setForeground(Color.GRAY);
                tabButtons[i].setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
            }
        }
    }
}
