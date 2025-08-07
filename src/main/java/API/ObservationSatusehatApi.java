package API;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

public class ObservationSatusehatApi {
    // Formatter ISO8601 lengkap + zona waktu
    private static final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static boolean createObservation(String idSatusehatPasien, String idEncounterSatusehat, String loincCode, String display, String value, String unit, String idSatusehatDokter) {
        try {
            Map<String, Object> observation = new HashMap<>();
            observation.put("resourceType", "Observation");
            observation.put("status", "final");

            observation.put("category", new Object[]{
                Map.of("coding", new Object[]{
                    Map.of(
                        "system", "http://terminology.hl7.org/CodeSystem/observation-category",
                        "code", "vital-signs",
                        "display", "Vital Signs"
                    )
                })
            });

            observation.put("code", Map.of(
                "coding", new Object[]{
                    Map.of(
                        "system", "http://loinc.org",
                        "code", loincCode,
                        "display", display
                    )
                }
            ));

            observation.put("subject", Map.of("reference", "Patient/" + idSatusehatPasien));
            observation.put("encounter", Map.of("reference", "Encounter/" + idEncounterSatusehat));
            observation.put("performer", new Object[]{
                Map.of("reference", "Practitioner/" + idSatusehatDokter)
            });

            // Gunakan waktu lengkap ISO 8601 (waktu sekarang dengan zona)
            String effectiveDateTime = ZonedDateTime.now().format(isoFormatter);
            String issuedDateTime = ZonedDateTime.now().format(isoFormatter);

            observation.put("effectiveDateTime", effectiveDateTime);
            observation.put("issued", issuedDateTime);

            observation.put("valueQuantity", Map.of(
                "value", Double.parseDouble(value),
                "unit", unit,
                "system", "http://unitsofmeasure.org",
                "code", "Cel" // untuk suhu
            ));

            API.ApiClient api = new API.ApiClient();
            String response = api.post("/Observation", observation);
            org.json.JSONObject json = new org.json.JSONObject(response);
            System.out.println("Response Observation: " + json);
            return json.has("id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean createBloodPressureObservation(String idSatusehatPasien, String idEncounterSatusehat, String idSatusehatDokter, double systolicValue, double diastolicValue) {
        try {
            Map<String, Object> bpObservation = new HashMap<>();
            bpObservation.put("resourceType", "Observation");
            bpObservation.put("status", "final");

            bpObservation.put("category", new Object[]{
                Map.of("coding", new Object[]{
                    Map.of(
                        "system", "http://terminology.hl7.org/CodeSystem/observation-category",
                        "code", "vital-signs",
                        "display", "Vital Signs"
                    )
                })
            });

            bpObservation.put("code", Map.of(
                "coding", new Object[]{
                    Map.of("system", "http://loinc.org", "code", "85354-9", "display", "Blood pressure panel")
                }
            ));

            bpObservation.put("subject", Map.of("reference", "Patient/" + idSatusehatPasien));
            bpObservation.put("encounter", Map.of("reference", "Encounter/" + idEncounterSatusehat));
            bpObservation.put("performer", new Object[]{Map.of("reference", "Practitioner/" + idSatusehatDokter)});

            // Tambahkan waktu valid ISO 8601
            String effectiveDateTime = ZonedDateTime.now().format(isoFormatter);
            bpObservation.put("effectiveDateTime", effectiveDateTime);

            // Tambahkan 2 komponen: sistolik dan diastolik
            bpObservation.put("component", new Object[]{
                Map.of(
                    "code", Map.of("coding", new Object[]{
                        Map.of("system", "http://loinc.org", "code", "8480-6", "display", "Systolic blood pressure")
                    }),
                    "valueQuantity", Map.of(
                        "value", systolicValue,
                        "unit", "mmHg",
                        "system", "http://unitsofmeasure.org",
                        "code", "mmHg"
                    )
                ),
                Map.of(
                    "code", Map.of("coding", new Object[]{
                        Map.of("system", "http://loinc.org", "code", "8462-4", "display", "Diastolic blood pressure")
                    }),
                    "valueQuantity", Map.of(
                        "value", diastolicValue,
                        "unit", "mmHg",
                        "system", "http://unitsofmeasure.org",
                        "code", "mmHg"
                    )
                )
            });

            API.ApiClient api = new API.ApiClient();
            String response = api.post("/Observation", bpObservation);
            org.json.JSONObject json = new org.json.JSONObject(response);
            System.out.println("Response Blood Pressure Observation: " + json);
            return json.has("id");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        Global.UserSessionCache cache = new Global.UserSessionCache();
        String idSatusehatDokter = cache.getIdSatusehat(); // Ambil dari session

        if (idSatusehatDokter == null || idSatusehatDokter.isEmpty()) {
            JOptionPane.showMessageDialog(null, "ID SATUSEHAT dokter belum tersedia. Tidak bisa kirim Observation!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Contoh penggunaan
        createObservation("123", "456", "789-8", "Blood Pressure", "120", "mmHg", idSatusehatDokter);
        createBloodPressureObservation("123", "456", idSatusehatDokter, 120, 80);
    }
}