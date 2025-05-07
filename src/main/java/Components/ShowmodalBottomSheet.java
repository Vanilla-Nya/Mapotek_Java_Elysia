package Components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class ShowmodalBottomSheet {

    private static JPanel currentBottomSheet; // Simpan referensi ke bottom sheet yang sedang aktif
    private static JLayeredPane layeredPane; // Simpan referensi ke layered pane
    private static MouseAdapter outsideClickListener;

    public static JPanel showBottomSheet(JFrame frame, JComponent content) {
        // Membuat panel untuk bottom sheet
        JPanel bottomSheet = new JPanel();
        bottomSheet.setLayout(new BoxLayout(bottomSheet, BoxLayout.Y_AXIS));
        bottomSheet.setBackground(Color.WHITE);

        // Atur ukuran minimum bottom sheet agar cukup besar
        bottomSheet.setPreferredSize(new Dimension(frame.getWidth(), 300)); // Tinggi 300px
        bottomSheet.setSize(frame.getWidth(), 300); // Lebar sama dengan frame utama

        // Menambahkan konten dinamis ke bottom sheet
        bottomSheet.add(content);

        // Tambahkan panel transparan untuk memberikan efek gelap
        JPanel glassPane = new JPanel() {   
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Warna semi-transparan untuk efek gelap
                g.setColor(new Color(0, 0, 0, 150)); // Hitam dengan transparansi 150 (0-255)
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glassPane.setOpaque(false); // Transparan
        glassPane.setSize(frame.getSize()); // Ukuran sesuai dengan frame utama
        glassPane.setLocation(0, 0); // Mulai dari koordinat (0, 0)
        glassPane.setVisible(true);

        // Tambahkan MouseListener untuk menangkap semua klik
        glassPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Tangkap semua klik dan jangan teruskan ke komponen lain
                closeBottomSheet(frame); // Tutup bottom sheet jika pengguna mengklik pada glassPane
            }
        });

        // Tambahkan glassPane ke layeredPane
        layeredPane = frame.getLayeredPane();
        layeredPane.add(glassPane, JLayeredPane.MODAL_LAYER);

        // Tambahkan bottom sheet ke layeredPane
        layeredPane.add(bottomSheet, JLayeredPane.POPUP_LAYER);
        bottomSheet.setLocation(0, frame.getHeight()); // Mulai dari bawah frame
        bottomSheet.setVisible(true);

        // Simpan referensi ke bottom sheet yang sedang aktif
        currentBottomSheet = bottomSheet;

        // Animasi slide dari bawah
        new Thread(() -> {
            try {
                int startY = frame.getHeight(); // Posisi awal (di bawah frame)
                int endY = frame.getHeight() - bottomSheet.getHeight(); // Posisi akhir (di dalam frame)
                for (int y = startY; y >= endY; y -= 10) { // Gerakan ke atas dengan langkah 10px
                    final int currentY = y;
                    SwingUtilities.invokeLater(() -> bottomSheet.setLocation(0, currentY)); // Perbarui posisi di EDT
                    Thread.sleep(10); // Delay untuk menciptakan efek animasi
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();

        return bottomSheet;
    }

    public static void closeBottomSheet(JFrame frame) {
        if (currentBottomSheet != null) {
            JLayeredPane layeredPane = frame.getLayeredPane();

            // Animasi slide ke bawah untuk menutup bottom sheet
            new Thread(() -> {
                try {
                    int startY = frame.getHeight() - currentBottomSheet.getHeight(); // Posisi awal (di dalam frame)
                    int endY = frame.getHeight(); // Posisi akhir (di bawah frame)
                    for (int y = startY; y <= endY; y += 10) { // Gerakan ke bawah dengan langkah 10px
                        final int currentY = y;
                        SwingUtilities.invokeLater(() -> {
                            if (currentBottomSheet != null) {
                                currentBottomSheet.setLocation(0, currentY); // Perbarui posisi di EDT
                            }
                        });
                        Thread.sleep(10); // Delay untuk menciptakan efek animasi
                    }
                    // Hapus bottom sheet dari JLayeredPane setelah animasi selesai
                    SwingUtilities.invokeLater(() -> {
                        if (currentBottomSheet != null) {
                            layeredPane.remove(currentBottomSheet);
                            layeredPane.repaint(); // Render ulang frame
                            currentBottomSheet = null; // Reset referensi
                        }
                        // Hapus panel transparan
                        for (Component comp : layeredPane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)) {
                            if (comp instanceof JPanel) {
                                layeredPane.remove(comp);
                            }
                        }
                        layeredPane.repaint();
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }
}
