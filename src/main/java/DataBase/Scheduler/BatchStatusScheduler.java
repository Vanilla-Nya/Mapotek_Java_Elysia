package DataBase.Scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BatchStatusScheduler {

    public static void startScheduler() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Menjadwalkan tugas untuk dijalankan setiap 1 menit
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Menjalankan pembaruan status batch...");
            BatchStatusUpdater.updateBatchStatus(); // Tugas yang dijalankan
        }, 0, 1, TimeUnit.MINUTES); // Delay awal: 0, Interval: 1 menit
    }

    private static long calculateInitialDelay() {
        // Hitung waktu hingga pukul 00:00 hari berikutnya
        long currentTimeMillis = System.currentTimeMillis();
        long midnightMillis = currentTimeMillis - (currentTimeMillis % (24 * 60 * 60 * 1000)) + (24 * 60 * 60 * 1000);
        return (midnightMillis - currentTimeMillis) / (60 * 60 * 1000); // Konversi ke jam
    }
}