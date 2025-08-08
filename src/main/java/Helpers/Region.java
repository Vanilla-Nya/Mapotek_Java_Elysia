package Helpers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Region {

    public final CloseableHttpClient httpClient = HttpClients.createDefault();
    public final String BASE_URL = "https://sig.bps.go.id/rest-bridging-dagri/getwilayah";

    public JSONArray getProvince() {
        try {
            HttpGet request = new HttpGet(BASE_URL + "?level=provinsi");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String jsonStr = EntityUtils.toString(entity);
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    return jsonArray;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public JSONArray getKota(String kodeProvinsi) {
        try {
            HttpGet request = new HttpGet(BASE_URL + "?level=kabupaten&parent=" + kodeProvinsi);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String jsonStr = EntityUtils.toString(entity);
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    return jsonArray;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public JSONArray getKecamatan(String kodeKota) {
        try {
            HttpGet request = new HttpGet(BASE_URL + "?level=kecamatan&parent=" + kodeKota);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String jsonStr = EntityUtils.toString(entity);
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    return jsonArray;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public JSONArray getKelurahan(String kodeKecamatan) {
        try {
            HttpGet request = new HttpGet(BASE_URL + "?level=desa&parent=" + kodeKecamatan);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String jsonStr = EntityUtils.toString(entity);
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    return jsonArray;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    // Dummy data, ganti dengan data asli dari referensi wilayah
    public List<String> getListProvinsi() {
        try {
            List<String> provinsiList = new ArrayList<>();
            JSONArray jsonArray = getProvince();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject provinsi = jsonArray.getJSONObject(i);
                    provinsiList.add(provinsi.getString("nama_dagri"));
                }
            }
            return provinsiList;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public String getKodeProvinsi(String namaProvinsi) {
        try {
            JSONArray jsonArray = getProvince();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject provinsi = jsonArray.getJSONObject(i);
                    if (provinsi.getString("nama_dagri").equalsIgnoreCase(namaProvinsi)) {
                        return provinsi.getString("kode_dagri");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<String> getListKota(String kodeProvinsi) {
        try {
            List<String> kotaList = new ArrayList<>();
            JSONArray jsonArray = getKota(kodeProvinsi);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject kota = jsonArray.getJSONObject(i);
                    kotaList.add(kota.getString("nama_dagri"));
                }
            }
            return kotaList;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public String getKodeKota(String namaKota, String kodeProvinsi) {
        try {
            JSONArray jsonArray = getKota(kodeProvinsi);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject kota = jsonArray.getJSONObject(i);
                    if (kota.getString("nama_dagri").equalsIgnoreCase(namaKota)) {
                        return kota.getString("kode_dagri");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getKodeKotaLokal(String namaKota, String kodeProvinsi) {
        try {
            JSONArray jsonArray = getKota(kodeProvinsi);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject kota = jsonArray.getJSONObject(i);
                    if (kota.getString("nama_dagri").equalsIgnoreCase(namaKota)) {
                        return kota.getString("kode_dagri");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<String> getListKecamatan(String kodeProvinsi, String kodeKota) {
        try {
            List<String> kecamatanList = new ArrayList<>();
            JSONArray jsonArray = getKecamatan(kodeKota);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject kota = jsonArray.getJSONObject(i);
                    kecamatanList.add(kota.getString("nama_dagri"));
                }
            }
            return kecamatanList;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public String getKodeKecamatan(String namaKecamatan, String kodeProvinsi, String kodeKota) {
        try {
            JSONArray jsonArray = getKecamatan(kodeKota);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject kota = jsonArray.getJSONObject(i);
                    if (kota.getString("nama_dagri").equalsIgnoreCase(namaKecamatan)) {
                        return kota.getString("kode_dagri");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getKodeKecamatanLokal(String namaKecamatan, String kodeProvinsi, String kodeKotaLokal) {
        try {
            JSONArray jsonArray = getKecamatan(kodeKotaLokal);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject kota = jsonArray.getJSONObject(i);
                    if (kota.getString("nama_dagri").equalsIgnoreCase(namaKecamatan)) {
                        return kota.getString("kode_dagri");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getKodeKelurahan(String namaKelurahan, String kodeProvinsi, String kodeKota, String kodeKecamatan) {
        try {
            JSONArray jsonArray = getKelurahan(kodeKecamatan);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject provinsi = jsonArray.getJSONObject(i);
                    if (provinsi.getString("nama_dagri").equalsIgnoreCase(namaKelurahan)) {
                        return provinsi.getString("kode_dagri");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<String> getListKelurahan(String kodeProvinsi, String kodeKotaLokal, String kodeKecamatanLokal) {
        try {
            List<String> kelurahanList = new ArrayList<>();
            JSONArray jsonArray = getKelurahan(kodeKecamatanLokal);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject provinsi = jsonArray.getJSONObject(i);
                    kelurahanList.add(provinsi.getString("nama_dagri"));
                }
            }
            return kelurahanList;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
