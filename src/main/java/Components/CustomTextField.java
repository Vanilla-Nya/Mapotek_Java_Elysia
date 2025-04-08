package Components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CustomTextField extends JPanel {

    private JTextField textField;
    private JLabel placeholderLabel;
    private int cornerRadius;
    private boolean isPassword;
    private boolean isPasswordVisible;
    private JButton togglePasswordButton;
    private TextChangeListener listener; // Listener for notifying changes

    public CustomTextField(String placeholderText, int columns, int cornerRadius, Optional<Boolean> isPassword) {
        this.isPassword = isPassword.orElse(false);
        this.cornerRadius = cornerRadius;
        this.isPasswordVisible = !this.isPassword; // If it's not a password field, it's visible by default
        setLayout(new GridBagLayout());
        setOpaque(false); // Background will be custom-drawn

        Dimension fieldSize = new Dimension(200, 30); // Fixed width and height

        // Create GridBagConstraints for positioning the components
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Allow component to stretch horizontally
        gbc.weightx = 1; // Give it horizontal resizing ability

        // TextField setup
        if (this.isPassword) {
            textField = new JPasswordField(columns);
        } else {
            textField = new JTextField(columns);
        }

        // Ensure consistent height
        textField.setPreferredSize(fieldSize);
        textField.setMinimumSize(fieldSize); // Force a minimum size
        textField.setMaximumSize(fieldSize);
        textField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Add padding
        textField.setOpaque(false); // Transparent to match rounded background
        textField.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        textField.setForeground(Color.BLACK);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));

        // Placeholder setup
        placeholderLabel = new JLabel(placeholderText);
        placeholderLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Add padding
        placeholderLabel.setForeground(Color.GRAY);
        placeholderLabel.setFont(new Font("Arial", Font.ITALIC, 14));

        // Toggle placeholder visibility based on focus
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                placeholderLabel.setVisible(textField.getText().isEmpty());
            }

            @Override
            public void focusLost(FocusEvent e) {
                placeholderLabel.setVisible(textField.getText().isEmpty());
            }
        });

        // DocumentListener for real-time updates
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePlaceholderVisibility();
                notifyListener();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePlaceholderVisibility();
                notifyListener();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePlaceholderVisibility();
                notifyListener();
            }

            private void updatePlaceholderVisibility() {
                // Show placeholder if the text field is empty
                placeholderLabel.setVisible(textField.getText().isEmpty());
            }

            private void notifyListener() {
                if (listener != null) {
                    try {
                        String value = textField.getText();
                        listener.onTextChange(value); // Notify with the new value
                    } catch (NumberFormatException e) {
                        listener.onTextChange("0"); // Send 0 if the input is invalid
                    }
                }
            }

            // Method to retrieve the current text
            public String getText() {
                return textField.getText();
            }

            // Method to programmatically set the text
            public void setText(String text) {
                textField.setText(text);
            }

            // Method to clear the text
            public void clearText() {
                textField.setText("");
            }
        });

        // Set initial visibility of placeholder based on the initial text
        placeholderLabel.setVisible(textField.getText().isEmpty());
        placeholderLabel.setVerticalAlignment(SwingConstants.CENTER); // Align vertically

        // Add placeholder and text field to panel with GridBagLayout
        add(placeholderLabel, gbc);  // Add placeholder first (it will be behind the textField)
        add(textField, gbc);         // Add textField on top of the placeholder

        if (this.isPassword) {
            // Create a toggle button for password visibility
            togglePasswordButton = new JButton("👁️"); // Use an eye emoji as a simple toggle button
            togglePasswordButton.setBackground(Color.WHITE);
            togglePasswordButton.setForeground(Color.BLACK);
            togglePasswordButton.setBorder(BorderFactory.createEmptyBorder());
            togglePasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            togglePasswordButton.setFocusable(false);

            // Action listener for the toggle button
            togglePasswordButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    togglePasswordVisibility();
                }
            });

            // Add the toggle button to the panel, aligning it to the right of the text field
            GridBagConstraints gbcToggle = new GridBagConstraints();
            gbcToggle.gridx = 1; // Position the button on the right side of the text field
            gbcToggle.gridy = 0;
            gbcToggle.insets = new Insets(0, 5, 0, 10); // Add some padding
            add(togglePasswordButton, gbcToggle);
        }

        // Force layout update
        revalidate();
        repaint();
    }

    public void setTextChangeListener(TextChangeListener listener) {
        this.listener = listener;
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password characters
            if (textField instanceof JPasswordField) {
                ((JPasswordField) textField).setEchoChar('•'); // Hide password
            }
            isPasswordVisible = false;
        } else {
            // Show password characters
            if (textField instanceof JPasswordField) {
                ((JPasswordField) textField).setEchoChar((char) 0); // Show password
            }
            isPasswordVisible = true;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 40); // Set fixed height for the entire component
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded background
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Draw border
        g2d.setColor(new Color(200, 200, 200)); // Light gray border
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
    }

    public String getText() {
        return textField.getText();
    }

    public String getPassword() {
        if (isPassword && textField instanceof JPasswordField) {
            // Safely cast and get the password as a char array
            char[] passwordChars = ((JPasswordField) textField).getPassword();
            return new String(passwordChars); // Convert char[] to String
        } else {
            return getText(); // For non-password fields, return the text
        }
    }

    public void setText(String text) {
        textField.setText(text);
        placeholderLabel.setVisible(text.isEmpty());
    }

    public JTextField getTextField() {
        return textField;
    }

    public interface TextChangeListener {

        void onTextChange(String newValue);
    }
}
