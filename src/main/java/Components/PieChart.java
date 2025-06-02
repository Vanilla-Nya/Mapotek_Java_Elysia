package Components;

import javax.swing.*;
import java.awt.*;

public class PieChart extends JPanel {
    private double pemasukan;
    private double pengeluaran;

    public PieChart(double pemasukan, double pengeluaran) {
        this.pemasukan = pemasukan;
        this.pengeluaran = pengeluaran;
        setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 20)); // Padding kiri 150
        setPreferredSize(new Dimension(400, 300)); // Ukuran panel minimum
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Hitung total
        double total = pemasukan + pengeluaran;

        // Hitung sudut untuk setiap bagian
        int pemasukanAngle = (int) Math.round((pemasukan / total) * 360);
        int pengeluaranAngle = 360 - pemasukanAngle;

        // Gambar bayangan
        int diameter = Math.min(getWidth(), getHeight()) - 20;
        int x = (getWidth() - diameter) / 2 + 50; // Geser lebih jauh ke kanan
        int y = (getHeight() - diameter) / 2;
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillArc(x + 5, y + 5, diameter, diameter, 0, 360);

        // Pemasukan (gradient hijau)
        GradientPaint greenGradient = new GradientPaint(x, y, new Color(0, 200, 150), x + diameter, y + diameter, new Color(0, 100, 80));
        g2d.setPaint(greenGradient);
        g2d.fillArc(x, y, diameter, diameter, 0, pemasukanAngle);

        // Pengeluaran (gradient merah)
        GradientPaint redGradient = new GradientPaint(x, y, new Color(255, 100, 100), x + diameter, y + diameter, new Color(200, 50, 50));
        g2d.setPaint(redGradient);
        g2d.fillArc(x, y, diameter, diameter, pemasukanAngle, pengeluaranAngle);

        // Tambahkan border
        g2d.setColor(Color.BLACK);
        g2d.drawArc(x, y, diameter, diameter, 0, pemasukanAngle);
        g2d.drawArc(x, y, diameter, diameter, pemasukanAngle, pengeluaranAngle);

        // Tambahkan label persentase di kiri chart
        String pemasukanText = String.format("Pemasukan: %.0f%%", (pemasukan / total) * 100);
        String pengeluaranText = String.format("Pengeluaran: %.0f%%", (pengeluaran / total) * 100);

        // Warna teks sesuai dengan bagian chart
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Teks untuk pemasukan (di kiri atas pie chart)
        g2d.setColor(new Color(0, 200, 150)); // Warna hijau untuk pemasukan
        g2d.drawString(pemasukanText, x - 120, y + diameter / 3 - 20); // Kurangi offset ke kiri

        // Teks untuk pengeluaran (di kiri bawah pie chart)
        g2d.setColor(new Color(255, 100, 100)); // Warna merah untuk pengeluaran
        g2d.drawString(pengeluaranText, x - 120, y + diameter / 3 + 20); // Kurangi offset ke kiri
    }

    public void updateData(double pemasukan, double pengeluaran) {
        this.pemasukan = pemasukan;
        this.pengeluaran = pengeluaran;
        repaint(); // Render ulang pie chart
    }

    public static void main(String[] args) {
        // Contoh penggunaan
        JFrame frame = new JFrame("Pie Chart Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Tambahkan PieChart ke JFrame dengan BorderLayout
        PieChart pieChart = new PieChart(71, 29);
        frame.setLayout(new BorderLayout());
        frame.add(pieChart, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
