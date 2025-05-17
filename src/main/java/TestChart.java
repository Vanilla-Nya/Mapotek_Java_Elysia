import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import Components.CustomChart;
import Components.CustomTextFormField;
import Components.ShowmodalBottomSheet;
import Components.ShowModalCenter;

public class TestChart {
    public static void main(String[] args) {
        // Create a JFrame to hold the CustomChart
        JFrame frame = new JFrame("Income vs Outcome Chart");

        // Example data for income and outcome
        int[] incomeData = {30, 70, 50, 90, 60};
        int[] outcomeData = {20, 90, 40, 80, 70};

        // Custom labels for the x-axis and y-axis
        String[] xLabels = {"Jan", "Feb", "Mar", "Apr", "May"};
        String[] yLabels = {"0", "20", "40", "60", "80", "100"};

        // Create an instance of CustomChart with data and labels
        CustomChart chart = new CustomChart(incomeData, outcomeData, xLabels, yLabels);

        // Add the CustomChart to the JFrame
        frame.add(chart, BorderLayout.CENTER);

        // Add a button to show the modal bottom sheet
        JButton showModalButton = new JButton("Show Modal Bottom Sheet");
        frame.add(showModalButton, BorderLayout.SOUTH);

        // Add a button to show the center modal
        JButton showCenterModalButton = new JButton("Show Center Modal");
        frame.add(showCenterModalButton, BorderLayout.NORTH);

        // Add action listener to the button
        showModalButton.addActionListener(e -> {
            // Membuat konten dinamis untuk bottom sheet
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(Color.LIGHT_GRAY);

            JLabel titleLabel = new JLabel("Dynamic Content");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Tambahkan CustomTextFormField
            CustomTextFormField customTextField = new CustomTextFormField("Enter your text");
            customTextField.setAlignmentX(Component.CENTER_ALIGNMENT);

            
            CustomTextFormField customTextField1 = new CustomTextFormField("Enter your text");
            customTextField.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton actionButton = new JButton("Submit");
            actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            actionButton.addActionListener(actionEvent -> {
                String inputText = customTextField.getText();
                JOptionPane.showMessageDialog(frame, "You entered: " + inputText);
            });

            content.add(titleLabel);
            content.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
            content.add(customTextField);
            content.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
            content.add(customTextField1);
            content.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
            content.add(actionButton);

            // Tambahkan JScrollPane untuk konten
            JScrollPane scrollPane = new JScrollPane(content);
            scrollPane.setBorder(null); // Hilangkan border default
            scrollPane.setPreferredSize(new Dimension(frame.getWidth(), 300)); // Atur tinggi scroll
            ShowmodalBottomSheet.showBottomSheet(frame, scrollPane);
        });

        // Add action listener to the center modal button
        showCenterModalButton.addActionListener(e -> {
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(Color.WHITE);

            JLabel label = new JLabel("Ini modal di tengah!");
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton closeButton = new JButton("Tutup");
            closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            closeButton.addActionListener(ev -> ShowModalCenter.closeCenterModal(frame));

            content.add(Box.createRigidArea(new Dimension(0, 20)));
            content.add(label);
            content.add(Box.createRigidArea(new Dimension(0, 20)));
            content.add(closeButton);

            ShowModalCenter.showCenterModal(frame, content);
        });

        // Set JFrame properties
        frame.setSize(600, 400); // Set the size of the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the app on exit
        frame.setVisible(true); // Make the window visible
    }
}
