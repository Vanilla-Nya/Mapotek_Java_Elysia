/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Components;

import java.awt.*;
import javax.swing.JTextPane;

/**
 *
 * @author asuna
 */
public class UnderlineLabel extends JTextPane {
    private int spaceBelowText; // Variable to control space below the text

    public UnderlineLabel(String text, int spaceBelowText) {
        super();
        setText(text); // Set the initial text
        setOpaque(false); // Make it transparent
        setEditable(false); // Set it to non-editable to behave like a label
        setFont(new Font("Arial", Font.PLAIN, 16)); // Set the font as needed
        setForeground(Color.BLACK); // Set the text color
        this.spaceBelowText = spaceBelowText; // Set the space below the text
        setContentType("text/html"); // Allow HTML content for rich text
        setText("<html>" + text.replace("\n", "<br/>") + "</html>"); // Wrap text with HTML
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Get the font metrics to determine the text height
        FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getHeight();

        // Calculate the Y position for the underline
        int underlineY = textHeight - fm.getDescent() + spaceBelowText; // Adjusted position for the underline

        // Get the document's text to calculate widths
        String[] lines = getText().split("\n");
        for (int i = 0; i < lines.length; i++) {
            int lineWidth = fm.stringWidth(lines[i]);
            int lineY = underlineY + (i * textHeight); // Y position for each line
            g.drawLine(0, lineY, lineWidth, lineY);
        }
    }
}
