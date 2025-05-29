package Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Stepper extends JPanel {
    private int currentStep = 0;
    private ArrayList<String> stepTitles = new ArrayList<>();
    private ArrayList<JPanel> stepContents = new ArrayList<>();
    private JPanel contentPanel;
    private StepIconsPanel stepIconsPanel;
    private JButton nextButton;
    private JButton backButton;

    public Stepper() {
        setLayout(new BorderLayout());

        // Step icons panel (progress bar with icons and lines)
        stepIconsPanel = new StepIconsPanel();
        stepIconsPanel.setLayout(new GridLayout(1, 0, 10, 0)); // Horizontal layout
        stepIconsPanel.setBackground(Color.WHITE);
        stepIconsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(stepIconsPanel, BorderLayout.NORTH);

        // Content panel for steps
        contentPanel = new JPanel(new CardLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Navigation buttons
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButton = new JButton("Back");
        nextButton = new JButton("Next");

        backButton.addActionListener(e -> {
            if (currentStep > 0) {
                showStep(currentStep - 1);
            }
        });

        nextButton.addActionListener(e -> {
            if (currentStep < stepContents.size() - 1) {
                showStep(currentStep + 1);
            }
        });

        navigationPanel.add(backButton);
        navigationPanel.add(nextButton);
        add(navigationPanel, BorderLayout.SOUTH);

        updateButtonState();
    }

    public void addStep(String title, JPanel stepContent) {
        stepTitles.add(title);
        stepContents.add(stepContent);

        // Add step to content panel
        contentPanel.add(stepContent, "Step" + (stepContents.size() - 1));

        // Add step icon to step icons panel
        JPanel stepIconPanel = new JPanel();
        stepIconPanel.setLayout(new BorderLayout());
        stepIconPanel.setOpaque(false);

        JLabel stepIcon = new JLabel(String.valueOf(stepContents.size()), JLabel.CENTER);
        stepIcon.setOpaque(true);
        stepIcon.setBackground(currentStep == stepContents.size() - 1 ? Color.BLUE : Color.LIGHT_GRAY);
        stepIcon.setForeground(Color.WHITE);
        stepIcon.setPreferredSize(new Dimension(30, 30));
        stepIcon.setFont(new Font("Arial", Font.BOLD, 14));
        stepIcon.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel stepLabel = new JLabel(title, JLabel.CENTER);
        stepLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        stepIconPanel.add(stepIcon, BorderLayout.CENTER);
        stepIconPanel.add(stepLabel, BorderLayout.SOUTH);

        // Add click listener to the step icon
        int stepIndex = stepContents.size() - 1;
        stepIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showStep(stepIndex);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                stepIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stepIcon.setCursor(Cursor.getDefaultCursor());
            }
        });

        stepIconsPanel.addStepIcon(stepIconPanel);
        stepIconsPanel.revalidate();
        stepIconsPanel.repaint();

        if (stepContents.size() == 1) {
            showStep(0); // Show the first step by default
        }

        updateButtonState();
    }

    private void showStep(int stepIndex) {
        if (stepIndex >= 0 && stepIndex < stepContents.size()) {
            int previousStep = currentStep;
            currentStep = stepIndex;

            // Animasi transisi
            JPanel previousPanel = stepContents.get(previousStep);
            JPanel nextPanel = stepContents.get(currentStep);

            Timer timer = new Timer(10, null);
            timer.addActionListener(e -> {
                int stepDistance = 10; // Jarak perpindahan per frame
                Point location = previousPanel.getLocation();
                if (previousStep < currentStep) {
                    // Animasi ke kanan
                    previousPanel.setLocation(location.x - stepDistance, location.y);
                    nextPanel.setLocation(location.x + contentPanel.getWidth() - stepDistance, location.y);
                } else {
                    // Animasi ke kiri
                    previousPanel.setLocation(location.x + stepDistance, location.y);
                    nextPanel.setLocation(location.x - contentPanel.getWidth() + stepDistance, location.y);
                }

                // Hentikan animasi jika selesai
                if (Math.abs(previousPanel.getLocation().x) >= contentPanel.getWidth()) {
                    timer.stop();
                    CardLayout cl = (CardLayout) contentPanel.getLayout();
                    cl.show(contentPanel, "Step" + stepIndex);

                    // Reset posisi panel
                    previousPanel.setLocation(0, 0);
                    nextPanel.setLocation(0, 0);

                    // Update step icons
                    stepIconsPanel.updateStepIcons(currentStep);
                    updateButtonState();
                }
            });

            timer.start();
        }
    }

    private void updateButtonState() {
        backButton.setEnabled(currentStep > 0);
        nextButton.setEnabled(currentStep < stepContents.size() - 1);
    }

    // Inner class for step icons panel with lines
    private class StepIconsPanel extends JPanel {
        private ArrayList<JPanel> stepIconPanels = new ArrayList<>();

        public void addStepIcon(JPanel stepIconPanel) {
            stepIconPanels.add(stepIconPanel);
            add(stepIconPanel);
        }

        public void updateStepIcons(int currentStep) {
            for (int i = 0; i < stepIconPanels.size(); i++) {
                JPanel stepIconPanel = stepIconPanels.get(i);
                JLabel stepIcon = (JLabel) stepIconPanel.getComponent(0);
                stepIcon.setBackground(i == currentStep ? Color.BLUE : Color.LIGHT_GRAY);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke(2));

            // Draw lines between step icons
            for (int i = 0; i < stepIconPanels.size() - 1; i++) {
                JPanel stepIconPanel1 = stepIconPanels.get(i);
                JPanel stepIconPanel2 = stepIconPanels.get(i + 1);

                int x1 = stepIconPanel1.getX() + stepIconPanel1.getWidth() / 2;
                int y1 = stepIconPanel1.getY() + stepIconPanel1.getHeight() / 2;
                int x2 = stepIconPanel2.getX() + stepIconPanel2.getWidth() / 2;
                int y2 = stepIconPanel2.getY() + stepIconPanel2.getHeight() / 2;

                g2.drawLine(x1, y1, x2, y2);
            }
        }
    }
}
