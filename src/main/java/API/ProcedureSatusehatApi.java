package API;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ProcedureSatusehatApi {
    private static final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Create Procedure resource in SATUSEHAT
     * @param idSatusehatPasien required
     * @param idEncounterSatusehat optional (can be null)
     * @param kode required (ICD code)
     * @param deskripsi optional
     * @param idSatusehatDokter optional practitioner reference
     * @return true when created (response contains id), false otherwise
     */
    public static boolean createProcedure(String idSatusehatPasien, String idEncounterSatusehat, String kode, String deskripsi, String idSatusehatDokter) {
        try {
            if (idSatusehatPasien == null || idSatusehatPasien.isBlank()) {
                System.err.println("createProcedure: idSatusehatPasien required");
                return false;
            }
            if (kode == null || kode.isBlank()) {
                System.err.println("createProcedure: kode required");
                return false;
            }

            Map<String, Object> procedure = new HashMap<>();
            procedure.put("resourceType", "Procedure");

            // subject & optional encounter
            procedure.put("subject", Map.of("reference", "Patient/" + idSatusehatPasien));
            if (idEncounterSatusehat != null && !idEncounterSatusehat.isBlank()) {
                procedure.put("encounter", Map.of("reference", "Encounter/" + idEncounterSatusehat));
            }

            // code: adjust system if you use ICD-10 in production
            procedure.put("code", Map.of(
                "coding", new Object[]{
                    Map.of(
                        "system", "http://hl7.org/fhir/sid/icd-10", // change if you need icd-9-cm or SNOMED
                        "code", kode,
                        "display", deskripsi != null ? deskripsi : kode
                    )
                },
                "text", deskripsi != null ? deskripsi : kode
            ));

            // status and performed time
            procedure.put("status", "completed");
            String nowIso = ZonedDateTime.now().format(isoFormatter);
            procedure.put("performedDateTime", nowIso);

            // optional performer
            if (idSatusehatDokter != null && !idSatusehatDokter.isBlank()) {
                procedure.put("performer", new Object[]{ Map.of("actor", Map.of("reference", "Practitioner/" + idSatusehatDokter)) });
            }

            API.ApiClient api = new API.ApiClient();
            String response = api.post("/Procedure", procedure);

            // log full response for debugging
            System.out.println("Response Procedure raw: " + response);
            org.json.JSONObject json = new org.json.JSONObject(response);
            if (json.has("id")) {
                System.out.println("Procedure created id=" + json.getString("id"));
                return true;
            } else {
                System.err.println("Create Procedure failed: " + json.toString());
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // overload supaya call yang lama dengan 4 arg tetap kompilasi
    public static boolean createProcedure(String idSatusehatPasien, String idEncounterSatusehat, String kode, String deskripsi) {
        return createProcedure(idSatusehatPasien, idEncounterSatusehat, kode, deskripsi, null);
    }
}