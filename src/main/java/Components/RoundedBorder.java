/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Components;

import java.awt.*;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author asuna
 */
public class RoundedBorder extends AbstractBorder {

    private final int radius;

    // Constructor to specify the radius of the rounded corners
    public RoundedBorder(int radius) {
        this.radius = radius;
    }

    // Paint the custom border with rounded corners
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set the color and stroke for the border
        g2d.setColor(Color.LIGHT_GRAY);  // Border color
        g2d.setStroke(new BasicStroke(1));  // Border thickness

        // Draw the rounded rectangle as the border
        g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);

        g2d.dispose();
    }
}
