package API;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class EncounterSatusehatApi {
    public static String createEncounter(String idSatusehatPasien, String namaPasien, String idSatusehatDokter, String namaDokter) {
        try {
            Map<String, Object> encounter = new HashMap<>();
            encounter.put("resourceType", "Encounter");
            encounter.put("status", "arrived");
            encounter.put("class", Map.of(
                "system", "http://terminology.hl7.org/CodeSystem/v3-ActCode",
                "code", "AMB",
                "display", "ambulatory"
            ));
            encounter.put("subject", Map.of(
                "reference", "Patient/" + idSatusehatPasien,
                "display", namaPasien
            ));
            encounter.put("participant", new Object[]{
                Map.of("individual", Map.of(
                    "reference", "Practitioner/" + idSatusehatDokter,
                    "display", namaDokter
                ))
            });

            // Tambahkan period sesuai waktu sekarang (WIB, +07:00)
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Jakarta"));
            String periodStart = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            ZonedDateTime endTime = now.plusMinutes(15);
            String periodEnd = endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            encounter.put("period", Map.of(
                "start", periodStart,
                "end", periodEnd
            ));

            // Tambahkan location sesuai permintaan
            encounter.put("location", new Object[]{
                Map.of(
                    "location", Map.of(
                        "reference", "Location/b017aa54-f1df-4ec2-9d84-8823815d7228",
                        "display", "Ruang 1A, Poliklinik Bedah Rawat Jalan Terpadu, Lantai 2, Gedung G"
                    )
                )
            });

            String orgId = API.ApiClient.getOrgId();

            // identifier
            encounter.put("identifier", new Object[]{
                Map.of(
                    "system", "http://sys-ids.kemkes.go.id/encounter/" + orgId,
                    "value", "P20240001" // Ganti dengan nomor encounter/kunjungan unik Anda
                )
            });

            // statusHistory
            encounter.put("statusHistory", new Object[]{
                Map.of(
                    "status", "arrived",
                    "period", Map.of("start", periodStart)
                )
            });

            // serviceProvider
            encounter.put("serviceProvider", Map.of(
                "reference", "Organization/" + orgId
            ));

            System.out.println("Creating Encounter: " + encounter);
            API.ApiClient api = new API.ApiClient();
            String response = api.post("/Encounter", encounter);
            org.json.JSONObject json = new org.json.JSONObject(response);
            System.out.println("Response Encounter: " + json);
            if (json.has("id")) {
                return json.getString("id"); // Kembalikan Encounter ID dari SATUSEHAT
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}