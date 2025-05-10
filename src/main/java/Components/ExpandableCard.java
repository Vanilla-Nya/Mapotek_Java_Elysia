package Components;

import javax.swing.*;
import java.awt.*;

public class ExpandableCard extends JPanel {
    private boolean isExpanded = false;
    private JPanel contentPanel;
    private JButton expandButton;

    public ExpandableCard(String title, String value, JPanel expandableContent, String expandButtonPosition) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 150, 243));

        JLabel valueLabel = new JLabel(value, SwingConstants.RIGHT);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(0, 150, 136));

        // Expand/Collapse Button with Icon
        expandButton = new JButton(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/arrow_down.png"))
                .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))); // Default icon
        expandButton.setFocusPainted(false);
        expandButton.setBorderPainted(false);
        expandButton.setContentAreaFilled(false);
        expandButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        expandButton.addActionListener(e -> toggleExpansion());

        // Add components to header panel
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(valueLabel, BorderLayout.CENTER);

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

        // Change the icon based on the state
        if (isExpanded) {
            expandButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/arrow_up.png"))
                    .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))); // Icon for collapse
            animateExpansion();
        } else {
            expandButton.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/arrow_down.png"))
                    .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH))); // Icon for expand
            animateCollapse();
        }
    }

    private void animateExpansion() {
        contentPanel.setVisible(true); // Ensure panel is visible before animation starts
        int startHeight = 0;
        int endHeight = contentPanel.getPreferredSize().height;

        new Thread(() -> {
            try {
                for (int height = startHeight; height <= endHeight; height += 10) {
                    final int currentHeight = height;
                    SwingUtilities.invokeLater(() -> {
                        contentPanel.setPreferredSize(new Dimension(contentPanel.getWidth(), currentHeight));
                        revalidate();
                        repaint();
                    });
                    Thread.sleep(10);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void animateCollapse() {
        int startHeight = contentPanel.getHeight();
        int endHeight = 0;

        new Thread(() -> {
            try {
                for (int height = startHeight; height >= endHeight; height -= 10) {
                    final int currentHeight = height;
                    SwingUtilities.invokeLater(() -> {
                        contentPanel.setPreferredSize(new Dimension(contentPanel.getWidth(), currentHeight));
                        revalidate();
                        repaint();
                    });
                    Thread.sleep(10);
                }
                SwingUtilities.invokeLater(() -> {
                    contentPanel.setVisible(false);
                    contentPanel.setPreferredSize(null); // Reset size to allow re-expansion
                });
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}