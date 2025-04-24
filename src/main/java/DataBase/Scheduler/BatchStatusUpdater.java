package DataBase.Scheduler;

import DataBase.QueryExecutor;

public class BatchStatusUpdater {

    public static void updateBatchStatus() {
        String expiredQuery = "UPDATE detail_obat SET status_batch = 'expired' WHERE tanggal_expired < CURDATE() AND status_batch = 'aktif'";
        String habisQuery = "UPDATE detail_obat SET status_batch = 'habis' WHERE stock = 0 AND status_batch = 'aktif'";

        try {
            // Jalankan query untuk memperbarui status expired
            boolean expiredUpdated = QueryExecutor.executeUpdateQuery(expiredQuery, null);
            System.out.println("Expired rows updated: " + expiredUpdated);

            // Jalankan query untuk memperbarui status habis
            boolean habisUpdated = QueryExecutor.executeUpdateQuery(habisQuery, null);
            System.out.println("Habis rows updated: " + habisUpdated);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}