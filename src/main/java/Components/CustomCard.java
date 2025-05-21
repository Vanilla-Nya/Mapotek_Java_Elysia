package Components;

import javax.swing.*;
import java.awt.*;

public class CustomCard extends JPanel {
    private int cornerRadius = 15;
    private Color backgroundColor = Color.WHITE;
    private Color borderColor = new Color(200, 200, 200);
    private int borderWidth = 2;

    public CustomCard(JComponent content) {
        this(null, content);
    }

    public CustomCard(String title, JComponent content) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBackground(backgroundColor);

        if (title != null && !title.isEmpty()) {
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setOpaque(false);
            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
            titlePanel.add(titleLabel, BorderLayout.CENTER);

            // Custom separator 80% width, center
            JSeparator separator = new JSeparator();
            JPanel separatorWrapper = new JPanel() {
                @Override
                public void doLayout() {
                    int parentWidth = CustomCard.this.getWidth();
                    int sepWidth = (int) (parentWidth * 0.8);
                    int sepX = (parentWidth - sepWidth) / 2;
                    separator.setBounds(sepX, 3, sepWidth, 2);
                    for (Component c : getComponents()) {
                        if (c != separator) c.setBounds(0, 0, 0, 0);
                    }
                }
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(CustomCard.this.getWidth(), 8);
                }
            };
            separatorWrapper.setOpaque(false);
            separatorWrapper.setLayout(null); // Absolute layout
            separatorWrapper.add(separator);

            titlePanel.add(separatorWrapper, BorderLayout.SOUTH);
            add(titlePanel, BorderLayout.NORTH);
        }

        // Panel isi (content) langsung, padding lebih kecil
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // padding atas & bawah lebih kecil
        contentPanel.add(content, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawRoundRect(
            borderWidth / 2,
            borderWidth / 2,
            getWidth() - borderWidth,
            getHeight() - borderWidth,
            cornerRadius,
            cornerRadius
        );
    }
}
