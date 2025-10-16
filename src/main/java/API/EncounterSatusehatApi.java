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
    public static String createEncounter(String idSatusehatPasien, String namaPasien, 
                                     String idSatusehatDokter, String namaDokter, 
                                     boolean dokterMandiri) {
    try {
        API.ApiClient api = new API.ApiClient();
        String orgId = API.ApiClient.getOrgId();
        
        // ✅ ALWAYS ensure Location exists first
        String locationId = ensureLocationExists(api, System.getenv("SATUSEHAT_LOCATION_REF"), 
                                                  orgId, namaDokter, dokterMandiri);
        
        if (locationId == null) {
            System.err.println("❌ Cannot create Encounter: Location is required but not available");
            return null;
        }
        
        // Build Encounter
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
        encounter.put("identifier", new Object[]{
            Map.of("system", "http://sys-ids.kemkes.go.id/encounter/" + (orgId != null ? orgId : ""), 
                   "value", "P" + Instant.now().toEpochMilli())
        });
        encounter.put("statusHistory", new Object[]{
            Map.of("status", "arrived", "period", Map.of("start", periodStart))
        });

        // ✅ ALWAYS add location (now we know it exists)
        String locationDisplay = dokterMandiri ? "Praktik Mandiri " + namaDokter : "Ruang 1A";
        encounter.put("location", new Object[]{
            Map.of(
                "location", Map.of(
                    "reference", "Location/" + locationId,
                    "display", locationDisplay
                ),
                "status", "active",
                "period", Map.of("start", periodStart)
            )
        });

        // Set serviceProvider based on flow
        if (dokterMandiri) {
            encounter.put("serviceProvider", Map.of("reference", "Practitioner/" + idSatusehatDokter));
        } else {
            encounter.put("serviceProvider", Map.of("reference", "Organization/" + orgId));
        }

        // Send to API
        String response = api.post("/Encounter", encounter);
        System.out.println("Response Encounter raw: " + response);
        
        JSONObject json = new JSONObject(response);
        if (json.has("id")) {
            System.out.println("✅ Encounter created: " + json.getString("id"));
            return json.getString("id");
        }
        
        System.err.println("❌ Create Encounter failed: " + json.toString());
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
    private static String ensureLocationExists(API.ApiClient api, String envLocationRef, 
                                           String orgId, String namaDokter, 
                                           boolean dokterMandiri) {
    try {
        // Check existing Location from env
        if (envLocationRef != null && !envLocationRef.isBlank()) {
            String locId = envLocationRef.contains("/") 
                ? envLocationRef.substring(envLocationRef.indexOf('/') + 1) 
                : envLocationRef;
            
            String getLoc = api.get("/Location/" + locId);
            if (getLoc != null && getLoc.contains("\"resourceType\":\"Location\"")) {
                System.out.println("✅ Using existing Location: " + locId);
                return locId;
            }
            System.err.println("⚠️ Location not found: " + envLocationRef);
        }

        // Create new Location
        Map<String, Object> locationPayload = new HashMap<>();
        locationPayload.put("resourceType", "Location");
        locationPayload.put("status", "active");
        
        String locationName = dokterMandiri 
            ? "Praktik Mandiri " + namaDokter 
            : "Klinik Umum";
        locationPayload.put("name", locationName);
        locationPayload.put("mode", "instance");

        locationPayload.put("telecom", new Object[] {
            Map.of("system", "phone", "value", "08123456789", "use", "work")
        });

        locationPayload.put("address", Map.of(
            "use", "work",
            "type", "both",
            "text", "JL. WR. SUPRATMAN NO.26A",
            "city", "Bondowoso",
            "postalCode", "12210",
            "country", "ID"
        ));

        locationPayload.put("physicalType", Map.of(
            "coding", new Object[] {
                Map.of(
                    "system", "http://terminology.hl7.org/CodeSystem/location-physical-type",
                    "code", "bu",
                    "display", "Building"
                )
            }
        ));

        if (orgId != null && !orgId.isBlank()) {
            locationPayload.put("managingOrganization", Map.of("reference", "Organization/" + orgId));
            locationPayload.put("identifier", new Object[] {
                Map.of("system", "http://sys-ids.kemkes.go.id/location/" + orgId,
                       "value", "LOC" + Instant.now().toEpochMilli())
            });
        } else {
            locationPayload.put("identifier", new Object[] {
                Map.of("system", "http://sys-ids.kemkes.go.id/location",
                       "value", "LOC" + Instant.now().toEpochMilli())
            });
        }

        String createResp = api.post("/Location", locationPayload);
        if (createResp != null) {
            JSONObject j = new JSONObject(createResp);
            if (j.has("id")) {
                String newId = j.getString("id");
                System.out.println("✅ Created Location: " + newId);
                return newId;
            }
            System.err.println("❌ Failed creating Location: " + createResp);
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
