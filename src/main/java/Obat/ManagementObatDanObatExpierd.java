package Obat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class ManagementObatDanObatExpierd extends JFrame {

    public ManagementObatDanObatExpierd() {
        // Set up the main frame
        setTitle("Dashboard - Obat Management");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Set the layout for the main frame, use BorderLayout
        setLayout(new BorderLayout());

        // Create a modern tabbed pane with a clean design
        JTabbedPane mainTabbedPane = createTabbedPane();

        // Add the tabbed pane to the top section (North) of the frame
        add(mainTabbedPane, BorderLayout.NORTH);

        // Add content to the center panel (optional)
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(240, 240, 240)); // Light gray background
        add(centerPanel, BorderLayout.CENTER);
    }

    private JTabbedPane createTabbedPane() {
        // Create the tabbed pane with a more modern style
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Set background and foreground to be more modern
        tabbedPane.setBackground(new Color(245, 245, 245)); // Light gray background for tabs
        tabbedPane.setForeground(new Color(64, 64, 64));  // Dark gray text color

        // Correct icon paths (without extra quotes around paths)
        tabbedPane.addTab("Obat", new ImageIcon("assets/pills-solid.png"), new Obat());
        tabbedPane.setToolTipTextAt(0, "Manage all your medicines");

        tabbedPane.addTab("Obat Expired", new ImageIcon("assets/pills-solid-red.png"), new ObatExpierd());
        tabbedPane.setToolTipTextAt(1, "View expired medicines");

        tabbedPane.addTab("Stock Menipis", new ImageIcon("assets/pills-solid-red.png"), new StockObatMenipis());
        tabbedPane.setToolTipTextAt(1, "View Stock Menipis");

        // Modernize the tab selection appearance
        tabbedPane.setSelectedIndex(0); // Set the first tab as default
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); // Allow tabs to scroll if there are many

        return tabbedPane;
    }

    public static void main(String[] args) {
        // Run the application
        SwingUtilities.invokeLater(() -> {
            ManagementObatDanObatExpierd frame = new ManagementObatDanObatExpierd();
            frame.setVisible(true);
        });
    }
}
