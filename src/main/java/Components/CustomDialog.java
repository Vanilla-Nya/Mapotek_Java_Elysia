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

public class CustomDialog extends JDialog {

    private int response = JOptionPane.CLOSED_OPTION;  // Default response if dialog is closed

    public CustomDialog(JFrame parent, String message, String title) {
        super(parent, title, true);  // Modal dialog

        // Set layout and basic size
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setSize(350, 180);
        setLocationRelativeTo(parent);  // Center dialog on parent frame
        setUndecorated(true);  // Remove title bar
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.GRAY));  // Optional border for the dialog

        // Create a rounded border for the panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(248, 248, 248));  // Light gray background
        add(panel, BorderLayout.CENTER);

        // Message panel
        // Using JTextArea for message text
        JTextArea messageArea = new JTextArea(message);
        messageArea.setWrapStyleWord(true);  // Enable word wrapping
        messageArea.setLineWrap(true);       // Enable line wrapping
        messageArea.setOpaque(false);        // Make background transparent
        messageArea.setEditable(false);      // Make it non-editable
        messageArea.setFont(new Font("Roboto", Font.PLAIN, 14));
        messageArea.setForeground(Color.BLACK);
        messageArea.setFocusable(false);     // Disable focus
        panel.add(messageArea, BorderLayout.CENTER);

        // Button panel with Yes and No buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));  // Center-align buttons
        buttonPanel.setBackground(new Color(248, 248, 248));  // Same background as the main panel

        JButton yesButton = new RoundedButton("IYA");
        yesButton.setPreferredSize(new Dimension(100, 40));
        yesButton.setBackground(new Color(0, 123, 255));
//        styleButton(yesButton, new Color(0, 123, 255));  // Blue color for "Yes" (Material Design color)

        JButton noButton = new RoundedButton("TIDAK");
        noButton.setPreferredSize(new Dimension(100, 40));
        noButton.setBackground(new Color(220, 220, 220));
//        styleButton(noButton, new Color(220, 220, 220));  // Light grey color for "No"

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // ActionListener for Yes button
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                response = JOptionPane.YES_OPTION;
                dispose();  // Close the dialog
            }
        });

        // ActionListener for No button
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                response = JOptionPane.NO_OPTION;
                dispose();  // Close the dialog
            }
        });
    }

    // Apply Material Design style to buttons (e.g., rounded corners, flat style, color)
    private void styleButton(JButton button, Color backgroundColor) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Roboto", Font.BOLD, 14));
        button.setFocusable(false);
//        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding inside button
        button.setOpaque(true);  // Ensure the background color is visible

        // Button hover effect (Change background color on hover)
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
    }

    // Method to show the dialog and return the user's response
    public int showDialog() {
        setVisible(true);  // Display dialog
        return response;   // Return the response (Yes/No or Closed)
    }
}
