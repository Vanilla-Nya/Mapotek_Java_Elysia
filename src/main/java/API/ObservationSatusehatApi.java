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
            // Validasi input dasar
            if (idSatusehatPasien == null || idSatusehatPasien.isBlank() ||
                idEncounterSatusehat == null || idEncounterSatusehat.isBlank() ||
                idSatusehatDokter == null || idSatusehatDokter.isBlank() ||
                loincCode == null || loincCode.isBlank() ||
                value == null || value.isBlank()) {

                System.err.println("createObservation: missing required parameter");
                return false;
            }

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

            // Gunakan waktu lengkap ISO 8601 (waktu sekarang dengan zona) sekali
            String nowIso = ZonedDateTime.now().format(isoFormatter);
            observation.put("effectiveDateTime", nowIso);
            observation.put("issued", nowIso);

            // valueQuantity: kirim sebagai number
            double numericValue = Double.parseDouble(value);
            Map<String, Object> valueQuantity = new HashMap<>();
            valueQuantity.put("value", numericValue);
            if (unit != null && !unit.isBlank()) {
                valueQuantity.put("unit", unit);
            }
            valueQuantity.put("system", "http://unitsofmeasure.org");
            // optional: jangan paksa kode unit jika tidak diketahui
            if (unit != null && !unit.isBlank()) valueQuantity.put("code", unit);

            observation.put("valueQuantity", valueQuantity);

            API.ApiClient api = new API.ApiClient();
            String response = api.post("/Observation", observation);

            // Log full response untuk debugging
            System.out.println("Response Observation raw: " + response);

            // Pastikan ApiClient.post mengembalikan body (jika non-2xx, body error harus dikembalikan juga)
            org.json.JSONObject json = new org.json.JSONObject(response);
            if (json.has("id")) {
                System.out.println("Observation created id=" + json.getString("id"));
                return true;
            } else {
                // Tampilkan pesan error jika ada
                String statusMsg = json.optString("status_message", json.optString("message", json.optString("error", "")));
                System.err.println("Create Observation failed: " + statusMsg + " - full: " + json.toString());
                return false;
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean createBloodPressureObservation(String idSatusehatPasien, String idEncounterSatusehat, String idSatusehatDokter, double systolicValue, double diastolicValue) {
        try {
            if (idSatusehatPasien == null || idSatusehatPasien.isBlank() ||
                idEncounterSatusehat == null || idEncounterSatusehat.isBlank() ||
                idSatusehatDokter == null || idSatusehatDokter.isBlank()) {
                System.err.println("createBloodPressureObservation: missing required parameter");
                return false;
            }

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

            String nowIso = ZonedDateTime.now().format(isoFormatter);
            bpObservation.put("effectiveDateTime", nowIso);
            bpObservation.put("issued", nowIso);

            bpObservation.put("component", new Object[]{
                Map.of(
                    "code", Map.of("coding", new Object[]{
                        Map.of("system", "http://loinc.org", "code", "8480-6", "display", "Systolic blood pressure")
                    }),
                    "valueQuantity", Map.of(
                        "value", systolicValue,
                        "unit", "mmHg",
                        "system", "http://unitsofmeasure.org",
                        "code", "mm[Hg]"
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
                        "code", "mm[Hg]"
                    )
                )
            });

            API.ApiClient api = new API.ApiClient();
            String response = api.post("/Observation", bpObservation);
            System.out.println("Response Blood Pressure Observation raw: " + response);
            org.json.JSONObject json = new org.json.JSONObject(response);
            if (json.has("id")) {
                System.out.println("Blood pressure Observation created id=" + json.getString("id"));
                return true;
            } else {
                System.err.println("Create BP Observation failed: " + json.toString());
                return false;
            }
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
    }
}