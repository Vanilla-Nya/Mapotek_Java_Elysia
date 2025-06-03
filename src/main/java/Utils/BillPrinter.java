package Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BillPrinter {

    public static BigDecimal hargaJasa; 

    public static void printBill(String filePath, String patientName, String userName, 
                                  List<Object[]> drugData, BigDecimal total, 
                                  BigDecimal payment, BigDecimal change) throws IOException {
        // Create a NumberFormat instance for Indonesian locale
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        rupiahFormat.setMaximumFractionDigits(0); // Remove decimal places

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("============================================================");
            writer.newLine();

            // Add patient information
            writer.write("Nama Pasien  : " + patientName); // Patient's name
            writer.newLine();
            writer.write("Nama Pelayan : " + userName); // Logged-in user's name
            writer.newLine();
            writer.write("============================================================");
            writer.newLine();

            // Add drug information
            for (Object[] drug : drugData) {
                String drugName = (String) drug[0];
                BigDecimal hargaObat = (BigDecimal) drug[4]; // Harga obat
                hargaJasa = (BigDecimal) drug[5]; // Harga jasa, jika ada
                int jumlah = (int) drug[2];
                String signa = (String) drug[3];

                // Cetak informasi obat
                writer.write(drugName);
                writer.newLine();
                writer.write(String.format("Jumlah: %d", jumlah));
                writer.newLine();
                writer.write(String.format("Harga Obat: %s", rupiahFormat.format(hargaObat)));
                writer.newLine();
                writer.write(String.format("  Signa: %s", signa));
                writer.newLine();
                writer.write("------------------------------------------------------------");
                writer.newLine();
            }

            // Add harga jasa, total, payment, and change
            writer.write(String.format("%-20s %30s", "Harga Jasa: ", rupiahFormat.format(hargaJasa)));
            writer.newLine();
            writer.write(String.format("%-20s %30s", "Total:", rupiahFormat.format(total)));
            writer.newLine();
            writer.write(String.format("%-20s %30s", "Payment:", rupiahFormat.format(payment)));
            writer.newLine();
            writer.write(String.format("%-20s %30s", "Change:", rupiahFormat.format(change)));
            writer.newLine();

            writer.write("============================================================");
            writer.newLine();
            writer.write("Kami Peduli Kesehatan Anda, Semoga Kesehatan Anda Segera Pulih, Tetap Semangat!");
        }
    }
}
