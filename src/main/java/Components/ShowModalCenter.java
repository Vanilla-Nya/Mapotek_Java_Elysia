package Components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.Timer;

public class ShowModalCenter {

    private static JPanel currentModal;
    private static boolean isClosing = false;

    // Inner class untuk panel dengan alpha
    private static class FadePanel extends JPanel {
        private float alpha = 0f;
        public void setAlpha(float a) {
            // Clamp alpha agar selalu di antara 0 dan 1
            if (a < 0f) a = 0f;
            if (a > 1f) a = 1f;
            this.alpha = a;
            repaint();
        }
        public float getAlpha() {
            return this.alpha;
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            // super.paintComponent(g2); // HAPUS atau KOMENTARI baris ini!
            g2.dispose();
        }
        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paintChildren(g2);
            g2.dispose();
        }
    }

    public static JPanel showCenterModal(JFrame frame, JComponent content) {
        FadePanel modalPanel = new FadePanel();
        modalPanel.setLayout(new BoxLayout(modalPanel, BoxLayout.Y_AXIS));
        modalPanel.setBackground(Color.WHITE);

        modalPanel.add(content);

        // Pastikan layout dan ukuran sudah benar
        modalPanel.doLayout();
        modalPanel.validate();
        modalPanel.setSize(modalPanel.getPreferredSize());

        int x = (frame.getWidth() - modalPanel.getWidth()) / 2;
        int y = (frame.getHeight() - modalPanel.getHeight()) / 2;
        modalPanel.setLocation(x, y);
        modalPanel.setVisible(true);

        // Panel glassPane gelap
        JPanel glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glassPane.setOpaque(false);
        glassPane.setSize(frame.getSize());
        glassPane.setLocation(0, 0);
        glassPane.setVisible(true);

        // Tutup modal jika klik di luar modal
        glassPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point pt = SwingUtilities.convertPoint(glassPane, e.getPoint(), modalPanel.getParent());
                if (!modalPanel.getBounds().contains(pt)) {
                    closeCenterModal(frame);
                }
            }
        });

        // LayeredPane
        JLayeredPane layeredPane = frame.getLayeredPane();
        layeredPane.add(glassPane, JLayeredPane.MODAL_LAYER);

        // Posisikan modal di tengah (setelah modalPanel sudah tahu ukurannya)
        modalPanel.setSize(modalPanel.getPreferredSize());
        x = (frame.getWidth() - modalPanel.getWidth()) / 2;
        y = (frame.getHeight() - modalPanel.getHeight()) / 2;
        modalPanel.setLocation(x, y);
        modalPanel.setVisible(true);

        layeredPane.add(modalPanel, JLayeredPane.POPUP_LAYER);

        // Fade-in animasi
        Timer timer = new Timer(10, null);
        timer.addActionListener(e -> {
            float alpha = modalPanel.getAlpha() + 0.05f;
            if (alpha >= 1f) {
                alpha = 1f;
                timer.stop();
            }
            modalPanel.setAlpha(alpha);
        });
        modalPanel.setAlpha(0f);
        timer.start();

        currentModal = modalPanel;
        return modalPanel;
    }

    public static void closeCenterModal(JFrame frame) {
        if (currentModal != null && !isClosing) {
            isClosing = true;
            if (currentModal instanceof FadePanel) {
                FadePanel modalPanel = (FadePanel) currentModal;
                Timer timer = new Timer(10, null);
                timer.addActionListener(e -> {
                    float alpha = modalPanel.getAlpha() - 0.05f;
                    if (alpha <= 0f) {
                        modalPanel.setAlpha(0f); // Pastikan benar-benar 0 dan repaint terakhir
                        timer.stop();
                        // Hapus modal dan glassPane setelah repaint terakhir
                        // Delay sedikit agar repaint sempat tampil
                        Timer cleanupTimer = new Timer(20, ev -> {
                            JLayeredPane layeredPane = frame.getLayeredPane();
                            layeredPane.remove(modalPanel);
                            for (Component comp : layeredPane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)) {
                                if (comp instanceof JPanel) {
                                    layeredPane.remove(comp);
                                }
                            }
                            layeredPane.repaint();
                            currentModal = null;
                            isClosing = false;
                        });
                        cleanupTimer.setRepeats(false);
                        cleanupTimer.start();
                    } else {
                        modalPanel.setAlpha(alpha);
                    }
                });
                timer.start();
            } else {
                // Fallback jika bukan FadePanel
                JLayeredPane layeredPane = frame.getLayeredPane();
                layeredPane.remove(currentModal);
                for (Component comp : layeredPane.getComponentsInLayer(JLayeredPane.MODAL_LAYER)) {
                    if (comp instanceof JPanel) {
                        layeredPane.remove(comp);
                    }
                }
                layeredPane.repaint();
                currentModal = null;
                isClosing = false;
            }
        }
    }
}