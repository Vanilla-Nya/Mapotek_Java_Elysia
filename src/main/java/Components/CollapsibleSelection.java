package Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class CollapsibleSelection {
    public static void main(String[] args) {
        // Set up the main frame
        JFrame frame = new JFrame("Collapsible Drawer Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create the drawer panel
        JPanel drawerPanel = createDrawerPanel();

        // Add the drawer panel to the frame (on the left side)
        frame.add(drawerPanel, BorderLayout.WEST);

        // Set the frame size and make it visible
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    // Create the drawer panel with collapsible sections
    private static JPanel createDrawerPanel() {
        // Create the panel to hold the collapsible sections (drawer-like)
        JPanel drawerPanel = new JPanel();
        drawerPanel.setLayout(new BoxLayout(drawerPanel, BoxLayout.Y_AXIS));
        drawerPanel.setPreferredSize(new Dimension(200, 300)); // Width of the drawer

        // Create collapsible sections (Obat section)
        CollapsibleSection obatSection = new CollapsibleSection("Obat", Arrays.asList("Obat", "Obat Expired"));
        CollapsibleSection otherSection = new CollapsibleSection("Other", Arrays.asList("Sub-item 1", "Sub-item 2", "Sub-item 3"));

        // Add collapsible sections to the drawer panel
        drawerPanel.add(obatSection);
        drawerPanel.add(otherSection);

        return drawerPanel;
    }
}

class CollapsibleSection extends JPanel {
    
    private JButton mainButton;
    private JPanel subButtonsPanel;

    // Constructor to create a collapsible section
    public CollapsibleSection(String title, java.util.List<String> subButtonTitles) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Stack components vertically

        // Create the main collapse/expand button
        mainButton = new JButton(title);
        
        // Create a panel to hold the sub-buttons
        subButtonsPanel = new JPanel();
        subButtonsPanel.setLayout(new BoxLayout(subButtonsPanel, BoxLayout.Y_AXIS));
        subButtonsPanel.setVisible(false); // Initially hidden

        // Add sub-buttons dynamically based on input
        for (String subButtonTitle : subButtonTitles) {
            JButton subButton = new JButton(subButtonTitle);
            subButtonsPanel.add(subButton);
        }

        // Add the main button and the sub-buttons panel to the collapsible section
        add(mainButton);
        add(subButtonsPanel);

        // Action listener to handle collapse and expand functionality
        mainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Toggle visibility of the sub-buttons panel
                if (subButtonsPanel.isVisible()) {
                    subButtonsPanel.setVisible(false);
                    mainButton.setText(title); // Change text back to title
                } else {
                    subButtonsPanel.setVisible(true);
                    mainButton.setText("Collapse " + title); // Change text to show it's expanded
                }

                // Revalidate and repaint to reflect the changes
                revalidate();
                repaint();
            }
        });
    }
}