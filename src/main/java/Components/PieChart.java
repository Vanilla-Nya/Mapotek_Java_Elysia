package Components;

import javax.swing.*;
import java.awt.*;

public class PieChart extends JPanel {
    private double pemasukan;
    private double pengeluaran;

    public PieChart(double pemasukan, double pengeluaran) {
        this.pemasukan = pemasukan;
        this.pengeluaran = pengeluaran;
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

        // Gambar pie chart
        int diameter = Math.min(getWidth(), getHeight()) - 20; // Diameter lingkaran
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        // Pemasukan (warna hijau)
        g2d.setColor(new Color(0, 150, 136));
        g2d.fillArc(x, y, diameter, diameter, 0, pemasukanAngle);

        // Pengeluaran (warna merah)
        g2d.setColor(new Color(244, 67, 54));
        g2d.fillArc(x, y, diameter, diameter, pemasukanAngle, pengeluaranAngle);

        // Tambahkan label
        g2d.setColor(Color.BLACK);
        g2d.drawString("Pemasukan", x + diameter / 4, y + diameter / 2);
        g2d.drawString("Pengeluaran", x + 3 * diameter / 4, y + diameter / 2);
    }

    public void updateData(double pemasukan, double pengeluaran) {
        this.pemasukan = pemasukan;
        this.pengeluaran = pengeluaran;
        repaint(); // Render ulang pie chart
    }
}
