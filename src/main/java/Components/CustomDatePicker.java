package Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class CustomDatePicker extends JPanel {
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> dayComboBox;
    private JPanel datePickerPanel;
    private JTextField textField;

    // Constructor to initialize the Date Picker
    public CustomDatePicker(JTextField textField, boolean isExpired) {
        this.textField = textField;
        datePickerPanel = new JPanel();
        datePickerPanel.setLayout(new FlowLayout());
        datePickerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Create Year Dropdown with values from 1900 to the current year
        Calendar currentDate = Calendar.getInstance();
        int currentYear = currentDate.get(Calendar.YEAR);
        Integer[] years = new Integer[currentYear - 1899];
        if (isExpired) {
            for (int i = 1900; i <= currentYear; i++) {
                years[i - 1900] = currentYear + (i - 1899);
            }
        } else {
            for (int i = 1900; i <= currentYear; i++) {
                years[i - 1900] = i;
            }
        }
        yearComboBox = new JComboBox<>(years);
        
        // Create Month Dropdown
        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        monthComboBox = new JComboBox<>(months);
        
        // Create Day Dropdown (initially empty, will update based on selected month)
        dayComboBox = new JComboBox<>();
        
        // Add Action Listeners to update days when month or year is changed
        monthComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDays();
            }
        });
        
        yearComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDays();
            }
        });
        
        // Initial call to populate days based on the current month and year
        updateDays();

        // Add the components to the panel
        datePickerPanel.add(new JLabel("Year:"));
        datePickerPanel.add(yearComboBox);
        datePickerPanel.add(new JLabel("Month:"));
        datePickerPanel.add(monthComboBox);
        datePickerPanel.add(new JLabel("Day:"));
        datePickerPanel.add(dayComboBox);
    }

    // Method to update the day dropdown based on the selected month and year
    private void updateDays() {
        int selectedYear = (Integer) yearComboBox.getSelectedItem();
        int selectedMonth = monthComboBox.getSelectedIndex(); // 0-based index for months
        int daysInMonth = getDaysInMonth(selectedYear, selectedMonth);
        
        // Clear existing day entries in the combo box
        dayComboBox.removeAllItems();
        
        // Add days to the combo box based on the selected month
        for (int i = 1; i <= daysInMonth; i++) {
            dayComboBox.addItem(i);
        }
    }

    // Method to return the number of days in a month, considering leap years
    private int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    // This method can be called to retrieve the date picker panel to be added to your GUI
    public JPanel getDatePickerPanel() {
        return datePickerPanel;
    }

    // Method to get the selected date as a string (you can modify this format as needed)
    public String getSelectedDate() {
        int year = (Integer) yearComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex() + 1; // Convert 0-based index to 1-based month
        int day = (Integer) dayComboBox.getSelectedItem();
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    // Show the Date Picker when clicking the JTextField
    public void showDatePicker() {
        // Create a JDialog to show the date picker panel
        JDialog datePickerDialog = new JDialog((Frame) null, "Select Date", true);
        datePickerDialog.setLayout(new BorderLayout());
        
        // Wrap the date picker panel in a JPanel with padding
        JPanel paddedPanel = new JPanel(new BorderLayout());
        paddedPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around the content panel
        paddedPanel.add(datePickerPanel, BorderLayout.CENTER);

        // Add the padded panel to the dialog
        datePickerDialog.add(paddedPanel, BorderLayout.CENTER);

        // Add a button to confirm the date selection
        JButton selectButton = new RoundedButton("Select Date");
        selectButton.setBorder(new EmptyBorder(5, 5, 5, 5)); // Padding for the select button
        selectButton.setBackground(new Color(0, 150, 136));
        selectButton.setForeground(Color.WHITE);
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the selected date into the JTextField
                textField.setText(getSelectedDate());
                datePickerDialog.dispose(); // Close the date picker dialog
            }
        });
        
        // Wrap the date picker panel in a JPanel with padding
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around the content panel
        buttonPanel.add(selectButton, BorderLayout.CENTER);
        
        datePickerDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set the size of the dialog and make it visible
        datePickerDialog.pack();
        datePickerDialog.setLocationRelativeTo(textField); // Show the dialog near the text field
        datePickerDialog.setVisible(true);
    }
}
