package Components;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CustomTextFormField extends JPanel {
    private JTextField textField;
    private JLabel placeholderLabel;
    private Timer animationTimer;
    private int animationStep = 0;

    public CustomTextFormField(String placeholder) {
        setLayout(null);
        setPreferredSize(new Dimension(300, 50));
        setBackground(Color.WHITE);

        // Placeholder label
        placeholderLabel = new JLabel(placeholder);
        placeholderLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        placeholderLabel.setForeground(Color.GRAY);
        placeholderLabel.setBounds(15, 15, 200, 20); // Initial position (inside the border)
        add(placeholderLabel);

        // Text field
        textField = new JTextField();
        textField.setBounds(10, 10, 280, 30);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1)); // Add a light gray border
        textField.setOpaque(true); // Keep the background opaque
        textField.setMargin(new Insets(5, 5, 5, 5)); // Add padding inside the text field
        add(textField);

        // Add focus listener for animation
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                startAnimation(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    startAnimation(false);
                }
            }
        });
    }

    private void startAnimation(boolean moveUp) {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        animationStep = 0;
        int startX = placeholderLabel.getX();
        int startY = placeholderLabel.getY();
        int targetX = moveUp ? 15 : 15;
        int targetY = moveUp ? -1 : 15; // Move slightly above the border when focused

        animationTimer = new Timer(10, e -> {
            animationStep++;
            int newX = startX + (targetX - startX) * animationStep / 10;
            int newY = startY + (targetY - startY) * animationStep / 10;

            placeholderLabel.setBounds(newX, newY, 200, 20);

            if (animationStep >= 10) {
                animationTimer.stop();
                placeholderLabel.setFont(new Font("Arial", moveUp ? Font.BOLD : Font.PLAIN, moveUp ? 12 : 14));
            }
        });

        animationTimer.start();
    }

    public String getText() {
        return textField.getText();
    }
}
