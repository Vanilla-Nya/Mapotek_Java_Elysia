package Components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

public class RoundedButton extends JButton {

    private Color borderColor = new Color(0, 150, 136); // Default border color
    private int borderThickness = 2; // Default border thickness

    public RoundedButton(String text) {
        super(text);
        setDoubleBuffered(true); // Ensure double buffering is enabled
        setContentAreaFilled(false); // Make the button transparent
        setFocusPainted(false); // Remove focus rectangle
        setBorderPainted(false); // Remove button border
        setOpaque(false); // Make it opaque
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Draw the rounded rectangle background
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // Draw rounded rectangle

        // Draw the button text
        super.paintComponent(g);

        // Draw the border
        g.setColor(borderColor);
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30); // Draw rounded border
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        repaint(); // Repaint to apply the new background color
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint(); // Repaint to apply the new border color
    }

    public void setBorderThickness(int thickness) {
        this.borderThickness = thickness;
        repaint(); // Repaint to apply the new border thickness
    }
}
