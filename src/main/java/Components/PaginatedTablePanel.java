package Components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import Components.CustomTable.CustomTable;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;

public class PaginatedTablePanel extends JPanel {
    private CustomTable table;
    private DefaultTableModel fullModel;
    private JScrollPane scrollPane;
    private JPanel paginationPanel;
    private JComboBox<Integer> rowsPerPageCombo;
    private JButton prevButton, nextButton;
    private JLabel pageLabel;
    private int currentPage = 1;
    private int rowsPerPage = 10;
    private int totalPage = 1;

    public PaginatedTablePanel(DefaultTableModel model) {
        setLayout(new BorderLayout());
        this.fullModel = model;

        table = new CustomTable(new DefaultTableModel());
        scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(Short.MAX_VALUE, 320)); // Batasi tinggi tabel
        add(scrollPane, BorderLayout.CENTER);

        // Pagination controls
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        pageLabel = new JLabel();
        rowsPerPageCombo = new JComboBox<>(new Integer[]{5, 10, 20, 50, 100});
        rowsPerPageCombo.setSelectedItem(10);

        paginationPanel.add(new JLabel("Rows per page:"));
        paginationPanel.add(rowsPerPageCombo);
        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextButton);

        add(paginationPanel, BorderLayout.SOUTH);

        // Listeners
        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTable();
            }
        });
        nextButton.addActionListener(e -> {
            if (currentPage < totalPage) {
                currentPage++;
                updateTable();
            }
        });
        rowsPerPageCombo.addActionListener(e -> {
            rowsPerPage = (Integer) rowsPerPageCombo.getSelectedItem();
            currentPage = 1;
            updateTable();
        });

        updateTable();
    }

    private void updateTable() {
        int totalRows = fullModel.getRowCount();
        rowsPerPage = (Integer) rowsPerPageCombo.getSelectedItem();
        totalPage = (int) Math.ceil((double) totalRows / rowsPerPage);

        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage, totalRows);

        // Copy data for current page
        Vector<Vector<Object>> pageData = new Vector<>();
        for (int i = start; i < end; i++) {
            pageData.add((Vector<Object>) fullModel.getDataVector().get(i));
        }

        // Ambil nama kolom
        Vector<String> columnNames = new Vector<>();
        for (int i = 0; i < fullModel.getColumnCount(); i++) {
            columnNames.add(fullModel.getColumnName(i));
        }

        // Set data to table
        DefaultTableModel pageModel = new DefaultTableModel(pageData, columnNames);
        table.setModel(pageModel);

        // Update label
        pageLabel.setText("Page " + currentPage + " of " + totalPage);

        // Enable/disable buttons
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPage);
    }

    public CustomTable getTable() {
        return table;
    }
}
