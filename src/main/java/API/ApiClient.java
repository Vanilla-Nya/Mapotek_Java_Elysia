package API;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ApiClient {
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Gson gson = new Gson();

    // GET request
    public String get(String url) throws IOException {
        HttpGet request = new HttpGet("https://api-satusehat-stg.dto.kemkes.go.id/fhir-r4/v1" + url);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
        }
    }

    // POST request (Object as JSON)
    public String post(String url, Object jsonBody) throws IOException {
        HttpPost request = new HttpPost("https://api-satusehat-stg.dto.kemkes.go.id/fhir-r4/v1" + url);
        request.setHeader("Content-Type", "application/json");
        String json = gson.toJson(jsonBody); // Convert object to JSON string
        request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
        }
    }

    // DELETE request
    public String delete(String url) throws IOException {
        HttpDelete request = new HttpDelete("https://api-satusehat-stg.dto.kemkes.go.id/fhir-r4/v1" + url);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
        }
    }

    // Jangan lupa tutup httpClient saat aplikasi selesai
    public void close() throws IOException {
        httpClient.close();
    }

    public static void main(String[] args) {
        ApiClient api = new ApiClient();
        try {
            String response = api.get("https://api.example.com/data");
            System.out.println("GET Response: " + response);

            String postResponse = api.post("https://api.example.com/data", "{\"key\":\"value\"}");
            System.out.println("POST Response: " + postResponse);

            String deleteResponse = api.delete("https://api.example.com/data/1");
            System.out.println("DELETE Response: " + deleteResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                api.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}