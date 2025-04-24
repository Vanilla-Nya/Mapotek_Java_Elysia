package Components;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.Timer;

public class CustomChart extends JPanel {
    private int[] incomeData; // Array to hold the income data
    private int[] outcomeData; // Array to hold the outcome data
    private String[] xLabels; // Labels for the x-axis
    private String[] yLabels; // Labels for the y-axis
    private int animationStep = 0; // Tracks the current step of the animation
    private Timer animationTimer;

    public CustomChart(int[] incomeData, int[] outcomeData, String[] xLabels, String[] yLabels) {
        this.incomeData = incomeData; // Initialize the income data
        this.outcomeData = outcomeData; // Initialize the outcome data
        this.xLabels = xLabels; // Initialize x-axis labels
        this.yLabels = yLabels; // Initialize y-axis labels

        // Initialize the animation timer
        animationTimer = new Timer(50, e -> {
            animationStep++; // Increment the animation step
            repaint(); // Repaint the chart to reflect the animation
            if (animationStep > incomeData.length - 1) {
                animationTimer.stop(); // Stop the animation when all points are drawn
            }
        });
        animationTimer.start(); // Start the animation
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Set background color
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the axes
        g.setColor(Color.BLACK);
        g.drawLine(50, getHeight() - 50, getWidth() - 50, getHeight() - 50); // X-axis
        g.drawLine(50, getHeight() - 50, 50, 50); // Y-axis

        // Draw the line chart for income and outcome
        if (incomeData != null && outcomeData != null) {
            int chartWidth = getWidth() - 100; // Leave space for labels
            int chartHeight = getHeight() - 100; // Leave space for labels
            int pointSpacing = chartWidth / (incomeData.length - 1); // Space between points

            // Scale data to fit the chart height
            int maxDataValue = 100; // Assuming data values are between 0 and 100
            int[] scaledIncomeData = new int[incomeData.length];
            int[] scaledOutcomeData = new int[outcomeData.length];
            for (int i = 0; i < incomeData.length; i++) {
                scaledIncomeData[i] = chartHeight - (int) ((incomeData[i] / (double) maxDataValue) * chartHeight);
                scaledOutcomeData[i] = chartHeight - (int) ((outcomeData[i] / (double) maxDataValue) * chartHeight);
            }

            // Draw the lines connecting the income points progressively
            g.setColor(Color.BLUE);
            for (int i = 0; i < animationStep && i < incomeData.length - 1; i++) {
                int x1 = 50 + i * pointSpacing;
                int y1 = 50 + scaledIncomeData[i];
                int x2 = 50 + (i + 1) * pointSpacing;
                int y2 = 50 + scaledIncomeData[i + 1];
                g.drawLine(x1, y1, x2, y2);
            }

            // Draw the lines connecting the outcome points progressively
            g.setColor(Color.RED);
            for (int i = 0; i < animationStep && i < outcomeData.length - 1; i++) {
                int x1 = 50 + i * pointSpacing;
                int y1 = 50 + scaledOutcomeData[i];
                int x2 = 50 + (i + 1) * pointSpacing;
                int y2 = 50 + scaledOutcomeData[i + 1];
                g.drawLine(x1, y1, x2, y2);
            }

            // Draw the points for income progressively
            g.setColor(Color.BLUE);
            for (int i = 0; i <= animationStep && i < incomeData.length; i++) {
                int x = 50 + i * pointSpacing;
                int y = 50 + scaledIncomeData[i];
                g.fillOval(x - 3, y - 3, 6, 6); // Draw a small circle for each point
            }

            // Draw the points for outcome progressively
            g.setColor(Color.RED);
            for (int i = 0; i <= animationStep && i < outcomeData.length; i++) {
                int x = 50 + i * pointSpacing;
                int y = 50 + scaledOutcomeData[i];
                g.fillOval(x - 3, y - 3, 6, 6); // Draw a small circle for each point
            }

            // Draw x-axis labels
            g.setColor(Color.BLACK);
            if (xLabels != null && xLabels.length == incomeData.length) {
                for (int i = 0; i < xLabels.length; i++) {
                    int x = 50 + i * pointSpacing;
                    g.drawString(xLabels[i], x - 10, getHeight() - 30); // Label each point
                }
            }

            // Draw y-axis labels
            if (yLabels != null) {
                int yStep = chartHeight / (yLabels.length - 1);
                for (int i = 0; i < yLabels.length; i++) {
                    int y = 50 + i * yStep;
                    g.drawString(yLabels[i], 20, y + 5); // Label each y-axis step
                }
            }
        }

        // Example static content
        g.setColor(Color.BLACK);
        g.drawString("Income vs Outcome Chart (Progressive Animation)", getWidth() / 2 - 100, 20);
    }
}
