package Obat;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import Components.CustomTabbedPane;

public class ManagementObatDanObatExpierd extends JFrame {

    public ManagementObatDanObatExpierd() {
        setTitle("Dashboard - Obat Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Siapkan judul tab dan panel konten
        String[] titles = {"Obat", "Obat Expired", "Stock Menipis"};
        JPanel panelObat = new Obat();
        JPanel panelExpired = new ObatExpierd();
        JPanel panelStock = new StockObatMenipis();

        JPanel[] contents = {panelObat, panelExpired, panelStock};

        // Buat CustomTabbedPane
        CustomTabbedPane customTabbedPane = new CustomTabbedPane(titles, contents);

        add(customTabbedPane, BorderLayout.CENTER);

        // Setelah frame tampil, jalankan animasi tab pertama
        SwingUtilities.invokeLater(() -> {
            customTabbedPane.setContentAnimated(0);
            customTabbedPane.highlightButton(0);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManagementObatDanObatExpierd frame = new ManagementObatDanObatExpierd();
            frame.setVisible(true);
        });
    }
}
