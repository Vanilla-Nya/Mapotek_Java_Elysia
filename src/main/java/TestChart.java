import javax.swing.JFrame;

import Components.CustomChart;

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
        frame.add(chart);

        // Set JFrame properties
        frame.setSize(600, 400); // Set the size of the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the app on exit
        frame.setVisible(true); // Make the window visible
    }
}
