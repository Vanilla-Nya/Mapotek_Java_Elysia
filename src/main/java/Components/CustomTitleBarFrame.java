/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Components;

/**
 *
 * @author asuna
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * CustomTitleBar is a reusable title bar that can be used in any JFrame or JDialog.
 * It includes a title label and a close button.
 */
public class CustomTitleBarFrame extends JPanel {
    private int mouseX, mouseY;
    private JPanel buttonPanel;  // Panel to hold both buttons

    /**
     * Constructor for the CustomTitleBar.
     * 
     * @param title The title to display in the title bar.
     * @param titleBarColor The background color of the title bar.
     * @param titleColor The color of the title text.
     * @param closeButtonColor The background color of the close button.
     * @param closeAction The action to perform when the close button is clicked.
     */
    public CustomTitleBarFrame(String title, Color titleBarColor, Color titleColor, Color closeButtonColor, Runnable closeAction, ActionListener minimizeAction) {
        // Set the layout and preferred size for the title bar
        setLayout(new BorderLayout());
        setBackground(titleBarColor);
        setPreferredSize(new Dimension(800, 40)); // Set a default height for the title bar

        // Title label
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(titleColor);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.CENTER);
        
        // Create a panel to hold minimize and close buttons together
        buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(100, 40));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));  // Horizontal layout
        buttonPanel.setOpaque(false);  // Make the panel transparent

        // Minimize button
        JButton minimizeButton = new JButton("-");
        minimizeButton.setBackground(Color.WHITE);
        minimizeButton.setForeground(closeButtonColor);
        minimizeButton.setBorderPainted(false);
        minimizeButton.setFocusPainted(false);
        minimizeButton.setFont(new Font("Arial", Font.BOLD, 14));
        minimizeButton.addActionListener(minimizeAction);
        
        // Close button
        JButton closeButton = new JButton("X");
        closeButton.setBackground(Color.WHITE);
        closeButton.setForeground(closeButtonColor);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.addActionListener(e -> closeAction.run()); // Trigger close action

        buttonPanel.add(minimizeButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.EAST);  // Buttons on the left side
        // Mouse listener for dragging functionality
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); // Change cursor on mouse press
            }
        });

        // Mouse motion listener to move the window when dragging
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen() - mouseX;
                int y = e.getYOnScreen() - mouseY;
                Container parent = getTopLevelAncestor();
                if (parent instanceof JFrame) {
                    JFrame frame = (JFrame) parent;
                    frame.setLocation(x, y); // Move the frame on screen
                }
                // For JDialog or other containers, handle accordingly
            }
        });
    }
}