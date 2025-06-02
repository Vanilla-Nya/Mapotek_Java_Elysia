package Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IncomeExpenseCard extends JPanel {
    private boolean isExpanded = false; // Status ekspansi
    private JPanel defaultContent; // Konten default (teks pemasukan & pengeluaran)
    private JPanel pieChartContent; // Konten pie chart
    private float alpha = 1.0f; // Transparansi untuk animasi

    public IncomeExpenseCard(String title, double pemasukan, double pengeluaran) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        setBackground(Color.WHITE);

        // ** Header Section **
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 150, 243));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // ** Default Content (Pemasukan & Pengeluaran) **
        defaultContent = new JPanel(new GridLayout(1, 2));
        defaultContent.setBackground(Color.WHITE);

        // Panel untuk pengeluaran
        JPanel expensePanel = new JPanel();
        expensePanel.setLayout(new BoxLayout(expensePanel, BoxLayout.Y_AXIS));
        expensePanel.setBackground(Color.WHITE);

        JLabel expenseLabel = new JLabel("Pengeluaran", SwingConstants.CENTER);
        expenseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        expenseLabel.setForeground(new Color(244, 67, 54));
        expenseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel expenseValue = new JLabel("Rp " + String.format("%,.0f", pengeluaran), SwingConstants.CENTER);
        expenseValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        expenseValue.setForeground(new Color(244, 67, 54));
        expenseValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        expensePanel.add(expenseLabel);
        expensePanel.add(expenseValue);

        // Panel untuk pemasukan
        JPanel incomePanel = new JPanel();
        incomePanel.setLayout(new BoxLayout(incomePanel, BoxLayout.Y_AXIS));
        incomePanel.setBackground(Color.WHITE);

        JLabel incomeLabel = new JLabel("Pemasukan", SwingConstants.CENTER);
        incomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        incomeLabel.setForeground(new Color(76, 175, 80));
        incomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel incomeValue = new JLabel("Rp " + String.format("%,.0f", pemasukan), SwingConstants.CENTER);
        incomeValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        incomeValue.setForeground(new Color(76, 175, 80));
        incomeValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        incomePanel.add(incomeLabel);
        incomePanel.add(incomeValue);

        // Tambahkan panel ke defaultContent
        defaultContent.add(expensePanel); // Pengeluaran di kiri
        defaultContent.add(incomePanel); // Pemasukan di kanan

        // ** Pie Chart Content **
        pieChartContent = new PieChart(pemasukan, pengeluaran);
        pieChartContent.setPreferredSize(new Dimension(200, 200));

        // Tambahkan konten default sebagai konten awal
        add(defaultContent, BorderLayout.CENTER);

        // Tambahkan MouseListener untuk toggle konten
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleContentWithFade();
            }
        });
    }

    private void toggleContentWithFade() {
        Timer fadeOutTimer = new Timer(20, null);
        fadeOutTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha = Math.max(0.0f, alpha - 0.1f); // Pastikan alpha tidak kurang dari 0.0
                if (alpha <= 0.0f) {
                    fadeOutTimer.stop();
                    switchContent();
                    fadeIn();
                } else {
                    repaint();
                }
            }
        });
        fadeOutTimer.start();
    }

    private void fadeIn() {
        Timer fadeInTimer = new Timer(20, null);
        fadeInTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha = Math.min(1.0f, alpha + 0.1f); // Pastikan alpha tidak lebih dari 1.0
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    fadeInTimer.stop();
                }
                repaint();
            }
        });
        fadeInTimer.start();
    }

    private void switchContent() {
        // Hapus konten lama
        remove(isExpanded ? pieChartContent : defaultContent);

        // Tambahkan konten baru
        if (isExpanded) {
            add(defaultContent, BorderLayout.CENTER);
        } else {
            add(pieChartContent, BorderLayout.CENTER);
        }

        // Ubah status ekspansi
        isExpanded = !isExpanded;

        // Render ulang komponen
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.0f, Math.min(1.0f, alpha))));
    }
}