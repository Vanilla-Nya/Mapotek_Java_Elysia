package API;

import java.util.HashMap;
import java.util.Map;

public class ProcedureSatusehatApi {
    public static boolean createProcedure(String idSatusehatPasien, String idEncounterSatusehat, String kode, String deskripsi) {
        try {
            Map<String, Object> procedure = new HashMap<>();
            procedure.put("resourceType", "Procedure");
            procedure.put("subject", Map.of("reference", "Patient/" + idSatusehatPasien));
            procedure.put("encounter", Map.of("reference", "Encounter/" + idEncounterSatusehat));
            procedure.put("code", Map.of(
                "coding", new Object[]{
                    Map.of(
                        "system", "http://hl7.org/fhir/sid/icd-9-cm",
                        "code", kode,
                        "display", deskripsi
                    )
                }
            ));
            procedure.put("status", "completed");
            procedure.put("subject", Map.of("reference", "Patient/" + idSatusehatPasien));
            procedure.put("encounter", Map.of("reference", "Encounter/" + idEncounterSatusehat));
            API.ApiClient api = new API.ApiClient();
            // Jika perlu membuat Encounter, buat objek encounter di sini dan post.
            // Namun, jika hanya ingin membuat Procedure, langsung post procedure.
            String response = api.post("/Procedure", procedure);
            org.json.JSONObject json = new org.json.JSONObject(response);
            System.out.println("Response Procedure: " + json);
            return json.has("id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}