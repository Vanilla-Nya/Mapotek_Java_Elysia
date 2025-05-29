package Components;

import java.awt.*;
import java.awt.event.*;
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
        return showCenterModal(frame, content, false);
    }

    public static JPanel showCenterModal(JFrame frame, JComponent content, boolean requireConfirmation) {
        FadePanel modalPanel = new FadePanel();
        modalPanel.setLayout(new BoxLayout(modalPanel, BoxLayout.Y_AXIS));
        modalPanel.setBackground(Color.WHITE);

        modalPanel.add(content);
        modalPanel.setSize(modalPanel.getPreferredSize());
        int x = (frame.getWidth() - modalPanel.getWidth()) / 2;
        int y = (frame.getHeight() - modalPanel.getHeight()) / 2;
        modalPanel.setLocation(x, y);
        modalPanel.setAlpha(0f);
        modalPanel.setVisible(false);

        JPanel glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glassPane.setOpaque(false);
        glassPane.setSize(frame.getSize());
        glassPane.setLocation(0, 0);
        glassPane.setVisible(true);

        // Tambahkan logika untuk konfirmasi opsional
        glassPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point pt = SwingUtilities.convertPoint(glassPane, e.getPoint(), modalPanel.getParent());
                if (!modalPanel.getBounds().contains(pt)) {
                    if (requireConfirmation) {
                        // Tampilkan dialog konfirmasi
                        int result = JOptionPane.showConfirmDialog(
                            frame,
                            "Apakah Anda ingin membatalkan diagnosis ini?", // Pesan konfirmasi
                            "Konfirmasi Pembatalan", // Judul dialog
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );

                        // Jika pengguna memilih "Yes", tutup modal
                        if (result == JOptionPane.YES_OPTION) {
                            closeCenterModal(frame);
                        }
                    } else {
                        // Tutup modal langsung tanpa konfirmasi
                        closeCenterModal(frame);
                    }
                }
            }
        });

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                glassPane.setSize(frame.getSize());
            }
        });

        JLayeredPane layeredPane = frame.getLayeredPane();
        layeredPane.add(glassPane, JLayeredPane.MODAL_LAYER);
        layeredPane.add(modalPanel, JLayeredPane.POPUP_LAYER);

        layeredPane.revalidate();
        layeredPane.repaint();

        Timer showTimer = new Timer(30, evt -> {
            modalPanel.setVisible(true);
            modalPanel.requestFocusInWindow();
            Timer timer = new Timer(10, null);
            timer.addActionListener(e -> {
                float alpha = modalPanel.getAlpha();
                if (alpha < 1f) {
                    modalPanel.setAlpha(Math.min(1f, alpha + 0.08f));
                } else {
                    timer.stop();
                }
            });
            timer.start();
        });
        showTimer.setRepeats(false);
        showTimer.start();

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

    public static void closeCenterModal(JFrame frame, String message, String title) {
        if (currentModal != null && !isClosing) {
            // Tampilkan dialog konfirmasi
            int result = JOptionPane.showConfirmDialog(
                frame,
                message, // Pesan yang ditampilkan
                title,  // Judul dialog
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            // Jika pengguna memilih "Yes", lanjutkan untuk menutup modal
            if (result == JOptionPane.YES_OPTION) {
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

    public static void showModalCenter(JFrame parent, JComponent content) {
        JDialog dialog = new JDialog(parent, true); // Create a modal dialog
        dialog.setUndecorated(true); // Remove window decorations
        dialog.setLayout(new BorderLayout());
        dialog.add(content, BorderLayout.CENTER); // Add the content to the dialog

        // Center the dialog relative to the parent frame
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        // Add a fade-in effect (optional)
        Timer fadeInTimer = new Timer(10, null);
        fadeInTimer.addActionListener(new ActionListener() {
            float opacity = 0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.05f;
                if (opacity >= 1f) {
                    opacity = 1f;
                    fadeInTimer.stop();
                }
                dialog.setOpacity(opacity);
            }
        });
        fadeInTimer.start();

        dialog.setVisible(true); // Show the dialog
    }
}