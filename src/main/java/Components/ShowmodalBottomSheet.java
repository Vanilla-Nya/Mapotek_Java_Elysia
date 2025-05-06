package Components;

import java.awt.*;
import javax.swing.*;

public class ShowmodalBottomSheet {

    private static JPanel currentBottomSheet; // Simpan referensi ke bottom sheet yang sedang aktif

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

        // Tambahkan bottom sheet ke frame utama
        JLayeredPane layeredPane = frame.getLayeredPane();
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
                        SwingUtilities.invokeLater(() -> currentBottomSheet.setLocation(0, currentY)); // Perbarui posisi di EDT
                        Thread.sleep(10); // Delay untuk menciptakan efek animasi
                    }
                    // Hapus bottom sheet dari JLayeredPane setelah animasi selesai
                    SwingUtilities.invokeLater(() -> {
                        layeredPane.remove(currentBottomSheet);
                        layeredPane.repaint(); // Render ulang frame
                        currentBottomSheet = null; // Reset referensi
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }
}
