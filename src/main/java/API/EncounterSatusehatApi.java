package API;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class EncounterSatusehatApi {

    // createEncounter: dokterMandiri -> special flow
    public static String createEncounter(String idSatusehatPasien, String namaPasien, String idSatusehatDokter, String namaDokter, boolean dokterMandiri) {
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

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Jakarta"));
            String periodStart = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            String periodEnd = now.plusMinutes(15).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            encounter.put("period", Map.of("start", periodStart, "end", periodEnd));

            String orgId = API.ApiClient.getOrgId();
            encounter.put("identifier", new Object[]{
                Map.of("system", "http://sys-ids.kemkes.go.id/encounter/" + (orgId != null ? orgId : ""), "value", "P" + Instant.now().toEpochMilli())
            });

            encounter.put("statusHistory", new Object[]{
                Map.of("status", "arrived", "period", Map.of("start", periodStart))
            });

            API.ApiClient api = new API.ApiClient();

            if (!dokterMandiri) {
                // existing normal flow: use Organization serviceProvider + Location if available
                encounter.put("serviceProvider", Map.of("reference", "Organization/" + orgId));
                String envLocationRef = System.getenv("SATUSEHAT_LOCATION_REF");
                if (envLocationRef != null && !envLocationRef.isBlank()) {
                    String locId = envLocationRef.contains("/") ? envLocationRef.substring(envLocationRef.indexOf('/') + 1) : envLocationRef;
                    String locResp = api.get("/Location/" + locId);
                    if (locResp != null && locResp.contains("\"resourceType\":\"Location\"")) {
                        encounter.put("location", new Object[]{ Map.of("location", Map.of("reference", "Location/" + locId, "display", "Ruang 1A")) });
                    } else {
                        System.err.println("Location not found in SATUSEHAT: " + envLocationRef);
                    }
                }
                String response = api.post("/Encounter", encounter);
                System.out.println("Response Encounter raw: " + response);
                JSONObject json = new JSONObject(response);
                if (json.has("id")) return json.getString("id");
                System.err.println("Create Encounter failed: " + json.toString());
                return null;
            }

            // dokter mandiri flow: set serviceProvider to Practitioner/{id} and try without location
            encounter.put("serviceProvider", Map.of("reference", "Practitioner/" + idSatusehatDokter));
            String response = api.post("/Encounter", encounter);
            System.out.println("Response Encounter raw: " + response);
            JSONObject json = new JSONObject(response);
            if (json.has("id")) return json.getString("id");

            // If server complains about missing/invalid location, try to create Location and retry
            if (isMissingLocationError(json)) {
                System.out.println("Server requires Location -> attempting to create Location and retry");
                String newLocId = ensureLocationExists(api, System.getenv("SATUSEHAT_LOCATION_REF"), orgId);
                if (newLocId != null) {
                    encounter.put("location", new Object[]{ Map.of("location", Map.of("reference", "Location/" + newLocId, "display", "Praktik Mandiri")) });
                    // retry
                    String retryResp = api.post("/Encounter", encounter);
                    System.out.println("Response Encounter retry raw: " + retryResp);
                    JSONObject rj = new JSONObject(retryResp);
                    if (rj.has("id")) return rj.getString("id");
                    System.err.println("Create Encounter retry failed: " + rj.toString());
                } else {
                    System.err.println("Failed to create/find Location for dokter mandiri");
                }
            } else {
                System.err.println("Create Encounter failed: " + json.toString());
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // detect if OperationOutcome indicates missing location requirement
    private static boolean isMissingLocationError(JSONObject outcome) {
        try {
            if (outcome.has("issue")) {
                for (Object o : outcome.getJSONArray("issue")) {
                    JSONObject iss = (JSONObject) o;
                    String expr = iss.optString("expression", "");
                    String details = iss.has("details") ? iss.getJSONObject("details").optString("text", "") : iss.optString("diagnostics", "");
                    if (expr.contains("Encounter.location") || details.toLowerCase().contains("location")) return true;
                }
            }
        } catch (Exception ignore) {}
        return false;
    }

    // ensure Location exists: verify env id, else create one (uses orgId if available)
    private static String ensureLocationExists(API.ApiClient api, String envLocationRef, String orgId) {
        try {
            if (envLocationRef != null && !envLocationRef.isBlank()) {
                String locId = envLocationRef.contains("/") ? envLocationRef.substring(envLocationRef.indexOf('/') + 1) : envLocationRef;
                String getLoc = api.get("/Location/" + locId);
                if (getLoc != null && getLoc.contains("\"resourceType\":\"Location\"")) {
                    return locId;
                } else {
                    System.err.println("Location not found on SATUSEHAT: " + envLocationRef + " - response: " + getLoc);
                }
            }

            // create Location (requires Organization/{orgId} to exist on server ideally)
            Map<String, Object> locationPayload = new HashMap<>();
            locationPayload.put("resourceType", "Location");
            locationPayload.put("status", "active");
            locationPayload.put("name", "Praktik Mandiri " + (orgId != null ? orgId : ""));
            if (orgId != null && !orgId.isBlank()) {
                locationPayload.put("managingOrganization", Map.of("reference", "Organization/" + orgId));
                locationPayload.put("identifier", new Object[]{ Map.of("system", "http://sys-ids.kemkes.go.id/location/" + orgId, "value", "LOC" + Instant.now().toEpochMilli()) });
            } else {
                locationPayload.put("identifier", new Object[]{ Map.of("system", "http://sys-ids.kemkes.go.id/location", "value", "LOC" + Instant.now().toEpochMilli()) });
            }

            String createResp = api.post("/Location", locationPayload);
            if (createResp != null) {
                JSONObject j = new JSONObject(createResp);
                if (j.has("id")) {
                    return j.getString("id");
                } else {
                    System.err.println("Failed to create Location: " + createResp);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Backward-compatible overload: pemanggilan lama dengan 4 argumen.
     * Default dokterMandiri = false (ubah jika ingin default lain).
     */
    public static String createEncounter(String idSatusehatPasien, String namaPasien, String idSatusehatDokter, String namaDokter) {
        return createEncounter(idSatusehatPasien, namaPasien, idSatusehatDokter, namaDokter, false);
    }
}
