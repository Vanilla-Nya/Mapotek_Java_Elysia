package Components;

import javax.swing.JRadioButton;

public class CustomRadioButton extends JRadioButton {

    public CustomRadioButton(String text, boolean isSelected) {
        super(text);
        setSelected(isSelected);
        setFocusPainted(false); // Menghilangkan border fokus
    }

    public CustomRadioButton(String text) {
        this(text, false);
    }
}
