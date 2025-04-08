/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 *
 * @author asuna
 */
public class CustomPanel extends JPanel {

    private final int radius;
    private boolean isCurved;

    // Constructor to specify radius for rounded corners and shadow
    public CustomPanel(int radius) {
        this.radius = radius;
        this.isCurved = false; // Default to basic panel
        setOpaque(false); // Make panel transparent for custom rendering
        setBorder(new RoundedBorder(radius)); // Apply rounded border
    }

    // Method to set the panel as curved
    public void setCurved(boolean isCurved) {
        this.isCurved = isCurved;
        repaint(); // Repaint the panel to reflect the change
    }

    // Paint the panel with a shadow effect (simulating elevation)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Create the graphics context
        Graphics2D g2d = (Graphics2D) g.create();

        // Set anti-aliasing for smooth edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isCurved) {
            // Shadow effect: draw a shadow first to simulate elevation
            g2d.setColor(new Color(0, 0, 0, 50)); // Semi-transparent black for shadow
            g2d.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, radius, radius); // Shadow offset

            // Draw the actual panel background
            g2d.setColor(Color.WHITE); // Panel background color
            g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            // Custom painting logic from CurvedPanel
            Color teal = new Color(0, 150, 136);
            g2d.setColor(teal);
            int width = getWidth();
            int height = getHeight();
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(width / 2 - 10, -100, width, height + 200);
        } else {
            // Basic panel painting logic
            g2d.setColor(Color.WHITE); // Panel background color
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        g2d.dispose(); // Release resources
    }
}
