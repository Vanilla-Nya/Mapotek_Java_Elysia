package Components.CustomTable;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JPanel panel;
    private final List<JButton> buttons;
    private JButton currentButton;  // To track the current button being displayed
    private int row;

    // Constructor accepting a list of buttons
    public ActionCellEditor(List<JButton> buttons) {
        this.buttons = buttons;
        this.panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.row = row;  // Store the current row index
        panel.removeAll();  // Clear previous buttons

        // Dynamically assign buttons based on the row (you can adjust the logic)
        for (JButton button : buttons) {
            panel.add(button);  // Add buttons to the panel
        }

        panel.revalidate();  // Revalidate the panel to reflect changes
        return panel;  // Return the panel with buttons
    }

    @Override
    public Object getCellEditorValue() {
        // Return the button's text or any other value that you wish to use as the cell value
        return currentButton != null ? currentButton.getText() : null;
    }

    @Override
    public boolean stopCellEditing() {
        // This stops editing when a button is pressed
        fireEditingStopped();
        return true;
    }

    // Optionally add listeners for the buttons to handle actions (edit, delete, etc.)
    public void addActionListeners(TableModel model) {
        for (JButton button : buttons) {
            button.addActionListener(e -> {
                // You can customize actions based on the button clicked
                // Example: Handle edit/delete actions for a specific row
                if (button.getText().equals("Edit")) {
                    // Example: Handle edit logic for the row
                    System.out.println("Edit clicked for row " + row);
                } else if (button.getText().equals("Delete")) {
                    // Example: Handle delete logic for the row
                    System.out.println("Delete clicked for row " + row);
                }
            });
        }
    }
}
