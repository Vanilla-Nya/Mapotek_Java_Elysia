package DataBase.Scheduler;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BatchStatusScheduler {
    private static volatile boolean isRunning = false;
    public static void startScheduler() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);
        // Menjadwalkan tugas untuk dijalankan setiap 1 menit
        scheduler.scheduleAtFixedRate(() -> {
        if (isRunning) {
            System.out.println("Previous update still running, skipping this run");
            return;
        }
        isRunning = true;
        try {
            System.out.println("Batch update started at " + Instant.now());
            BatchStatusUpdater.updateBatchStatus();
            System.out.println("Batch update finished at " + Instant.now());
        } finally {
            isRunning = false;
            latch.countDown(); 
        }
        }, 0, 5, TimeUnit.MINUTES); // Delay awal: 0, Interval: 1 menit
        try {
            latch.await();  // waits until latch.countDown() is called
            System.out.println("Batch update finished first run.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static long calculateInitialDelay() {
        // Hitung waktu hingga pukul 00:00 hari berikutnya
        long currentTimeMillis = System.currentTimeMillis();
        long midnightMillis = currentTimeMillis - (currentTimeMillis % (24 * 60 * 60 * 1000)) + (24 * 60 * 60 * 1000);
        return (midnightMillis - currentTimeMillis) / (60 * 60 * 1000); // Konversi ke jam
    }
}