package Components.CustomTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class CustomTable extends JTable {
    private boolean isRowResizable = false; // Default: tidak bisa di-resize
    private boolean isRowReorderable = false; // Default: tidak bisa di-reorder

    public CustomTable(DefaultTableModel model) {
        super(model);  // Pass the model to JTable's constructor

        // Set row height for a spacious look
        setRowHeight(35);
        setBackground(Color.white);

        // Atur apakah kolom bisa di-resize atau tidak
        getTableHeader().setResizingAllowed(isRowResizable);

        // Atur apakah kolom bisa di-reorder atau tidak
        getTableHeader().setReorderingAllowed(isRowReorderable);

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (table.getColumnCount() > 3 && 
                    "status".equals(table.getColumnModel().getColumn(3).getHeaderValue().toString().toLowerCase())) {
                    // Get Status
                    String status = (String) table.getValueAt(row, 3);

                    // Set Color Based On Status
                    if ("Belum Periksa".equals(status)) {
                        c.setBackground(new Color(144, 238, 144)); // Hijau muda (Light Green)
                        c.setForeground(Color.BLACK);
                    } else if ("Sedang Diperiksa".equals(status)) {
                        c.setBackground(new Color(173, 216, 230)); // Biru muda (Light Blue)
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }

                    // Tetap beri highlight jika baris dipilih
                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                        c.setForeground(table.getSelectionForeground());
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);

                return c;
            }
        });

        // Set column header style
        JTableHeader header = getTableHeader();

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                // Call default renderer to get header cell
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // Cast to JLabel to set the border
                JLabel label = (JLabel) component;

                // Create a border with only a bottom line
                Border bottomBorder = new LineBorder(Color.BLACK, 1);

                // Set Font
                label.setFont(new Font("Arial", Font.BOLD, 14));

                // Set Color
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(0, 153, 102));

                // Set Alignment
                label.setHorizontalAlignment(SwingConstants.CENTER);

                // Set the height explicitly (optional, as header already has a height set)
                label.setPreferredSize(new Dimension(label.getPreferredSize().width, 35)); // Header cell height

                // Set only the bottom border
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

                return label;
            }
        });

        // Set grid color and visibility for a cleaner look
        setShowGrid(true);
        setGridColor(Color.WHITE);
    }

    // Setter untuk mengatur apakah baris bisa di-resize
    public void setRowResizable(boolean resizable) {
        this.isRowResizable = resizable;
        getTableHeader().setResizingAllowed(resizable);
    }

    // Setter untuk mengatur apakah baris bisa di-reorder
    public void setRowReorderable(boolean reorderable) {
        this.isRowReorderable = reorderable;
        getTableHeader().setReorderingAllowed(reorderable);
    }
}
