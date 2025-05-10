package Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class BillPrinter {

    public static void printBill(String filePath, String patientName, String userName, 
                                  List<Object[]> drugData, BigDecimal total, 
                                  BigDecimal payment, BigDecimal change) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("====================");
            writer.newLine();

            // Add patient information
            writer.write("Name: " + patientName); // Patient's name
            writer.newLine();
            writer.write("User: " + userName); // Logged-in user's name
            writer.newLine();
            writer.write("====================");
            writer.newLine();

            // Add drug information
            for (Object[] drug : drugData) {
                String drugName = (String) drug[0];
                BigDecimal harga = (BigDecimal) drug[3];
                int jumlah = (int) drug[2];
                String signa = (String) drug[4];

                // Print drug name
                writer.write(drugName);
                writer.newLine();

                // Print jumlah and harga under the drug name
                writer.write(String.format("Jumlah: %d", jumlah));
                writer.newLine();
                writer.write(String.format("Harga: Rp. %s", harga));
                writer.newLine();

                // Print signa under the drug name, indented
                writer.write(String.format("  Signa: %s", signa));
                writer.newLine();

                // Add a separator between drugs
                writer.write("------------------------------------------------------------");
                writer.newLine();
            }

            // Add total, payment, and change
            writer.write(String.format("%-20s %30s", "Total:", "Rp. " + total));
            writer.newLine();
            writer.write(String.format("%-20s %30s", "Payment:", "Rp. " + payment));
            writer.newLine();
            writer.write(String.format("%-20s %30s", "Change:", "Rp. " + change));
            writer.newLine();

            writer.write("====================");
            writer.newLine();
            writer.write("Thank you for your payment!");
        }
    }
}
