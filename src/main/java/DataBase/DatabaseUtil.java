package DataBase;

import java.io.*;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseUtil {
    private static final String HOSTS_FILE = "db_hosts.txt";
    private static final String DB_NAME = "mapotek";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    private static final int DB_PORT = 3306;
    private static final int TIMEOUT = 100;
    private static Connection conn = null;

    // Get DB Connection
    public static Connection getConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) return conn;

        // Coba konek ke IP yang sudah pernah sukses
        Set<String> knownIps = loadKnownIps();
        knownIps.add("127.0.0.1"); // tambah localhost

        for (String ip : knownIps) {
            if (tryConnect(ip)) return conn;
        }

        // Kalau belum berhasil, lakukan scanning
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 1; i <= 254; i++) {
            String ip = "192.168.0." + i;
            if (knownIps.contains(ip)) continue;

            final String scanIp = ip;
            executor.submit(() -> {
                try {
                    InetAddress inet = InetAddress.getByName(scanIp);
                    if (inet.isReachable(TIMEOUT)) {
                        if (tryConnect(scanIp)) {
                            executor.shutdownNow();
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error checking " + scanIp + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (conn == null || conn.isClosed()) {
            throw new SQLException("❌ Gagal terhubung ke database.");
        }

        return conn;
    }

    private static boolean tryConnect(String ip) {
        String url = "jdbc:mysql://" + ip + ":" + DB_PORT + "/" + DB_NAME + "?connectTimeout=2000";
        try {
            conn = DriverManager.getConnection(url, DB_USER, DB_PASS);
            System.out.println("✅ Terhubung ke database di IP: " + ip);
            saveIp(ip);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Gagal konek ke " + ip + " - " + e.getMessage());
            return false;
        }
    }

    private static Set<String> loadKnownIps() {
        Set<String> ips = new HashSet<>();
        File file = new File(HOSTS_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    ips.add(line.trim());
                }
            } catch (IOException e) {
                System.err.println("Gagal baca " + HOSTS_FILE + ": " + e.getMessage());
            }
        }
        return ips;
    }

    private static synchronized void saveIp(String ip) {
        Set<String> existing = loadKnownIps();
        if (existing.contains(ip)) return;

        try (FileWriter fw = new FileWriter(HOSTS_FILE, true)) {
            fw.write(ip + "\n");
            System.out.println("📝 IP disimpan: " + ip);
        } catch (IOException e) {
            System.err.println("Gagal simpan IP: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("🔌 Koneksi database ditutup.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
