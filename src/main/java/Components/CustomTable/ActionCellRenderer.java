package Components.CustomTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ActionCellRenderer extends JPanel implements TableCellRenderer {

    private final List<JButton> buttons;

    // Constructor accepting a list of buttons
    public ActionCellRenderer(List<JButton> buttons) {
        this.buttons = buttons;
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
        setBackground(Color.WHITE);  // Ensure the background color is white for table rows
        for (JButton button : buttons) {
            add(button);  // Add each button to the panel
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // If you want to highlight the row when selected, use:
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(Color.WHITE); // Default background color
        }

        // You can return the panel with buttons for any row
        return this;  // Return this panel to render it in the table cell
    }
}
