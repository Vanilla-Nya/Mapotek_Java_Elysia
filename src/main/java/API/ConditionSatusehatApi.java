package API;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ConditionSatusehatApi {
    private static final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Buat Condition di SATUSEHAT
     * @param idSatusehatPasien required, id Patient di satusehat
     * @param idEncounterSatusehat optional, id Encounter di satusehat (boleh null jika tidak ada)
     * @param kode required, kode ICD (mis. "A00")
     * @param deskripsi optional, teks deskripsi diagnosis
     * @return true kalau berhasil (response punya id), false sebaliknya
     */
    public static boolean createCondition(String idSatusehatPasien, String idEncounterSatusehat, String kode, String deskripsi) {
        try {
            // validasi input
            if (idSatusehatPasien == null || idSatusehatPasien.isBlank()) {
                System.err.println("createCondition: idSatusehatPasien required");
                return false;
            }
            if (kode == null || kode.isBlank()) {
                System.err.println("createCondition: kode (ICD) required");
                return false;
            }

            Map<String, Object> condition = new HashMap<>();
            condition.put("resourceType", "Condition");

            // status minimal; production biasanya butuh clinicalStatus & verificationStatus
            condition.put("clinicalStatus", Map.of("coding", new Object[]{ Map.of("system", "http://terminology.hl7.org/CodeSystem/condition-clinical", "code", "active") }));
            condition.put("verificationStatus", Map.of("coding", new Object[]{ Map.of("system", "http://terminology.hl7.org/CodeSystem/condition-ver-status", "code", "confirmed") }));

            condition.put("subject", Map.of("reference", "Patient/" + idSatusehatPasien));

            if (idEncounterSatusehat != null && !idEncounterSatusehat.isBlank()) {
                condition.put("encounter", Map.of("reference", "Encounter/" + idEncounterSatusehat));
            }

            // kode ICD
            condition.put("code", Map.of(
                "coding", new Object[]{
                    Map.of(
                        "system", "http://hl7.org/fhir/sid/icd-10",
                        "code", kode,
                        "display", (deskripsi != null ? deskripsi : kode)
                    )
                },
                "text", (deskripsi != null ? deskripsi : kode)
            ));

            // kategori (optional)
            condition.put("category", new Object[]{
                Map.of(
                    "coding", new Object[]{
                        Map.of(
                            "system", "http://terminology.hl7.org/CodeSystem/condition-category",
                            "code", "encounter-diagnosis",
                            "display", "Encounter Diagnosis"
                        )
                    }
                )
            });

            // onset: set sekarang sebagai contoh (boleh diganti dengan parameter)
            String nowIso = ZonedDateTime.now().format(isoFormatter);
            condition.put("onsetDateTime", nowIso);

            API.ApiClient api = new API.ApiClient();
            String response = api.post("/Condition", condition);

            System.out.println("Response Condition raw: " + response);
            org.json.JSONObject json = new org.json.JSONObject(response);

            if (json.has("id")) {
                System.out.println("Condition created id=" + json.getString("id"));
                return true;
            } else {
                // tampilkan pesan error/response untuk debugging
                String msg = json.optString("status_message", json.optString("message", json.toString()));
                System.err.println("Create Condition failed: " + msg);
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}