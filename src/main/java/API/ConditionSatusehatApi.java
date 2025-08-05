package API;

import java.util.HashMap;
import java.util.Map;

public class ConditionSatusehatApi {
    public static boolean createCondition(String idSatusehatPasien, String idEncounterSatusehat, String kode, String deskripsi) {
        try {
            Map<String, Object> condition = new HashMap<>();
            condition.put("resourceType", "Condition");
            condition.put("subject", Map.of("reference", "Patient/" + idSatusehatPasien));
            condition.put("encounter", Map.of("reference", "Encounter/" + idEncounterSatusehat));
            condition.put("code", Map.of(
                "coding", new Object[]{
                    Map.of(
                        "system", "http://hl7.org/fhir/sid/icd-10",
                        "code", kode,
                        "display", deskripsi
                    )
                }
            ));
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

            API.ApiClient api = new API.ApiClient();
            String response = api.post("/Condition", condition);
            org.json.JSONObject json = new org.json.JSONObject(response);
            System.out.println("Response Condition: " + json);
            return json.has("id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}