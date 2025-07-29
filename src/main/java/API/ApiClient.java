package API;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import Global.UserSessionCache;

public class ApiClient {
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Gson gson = new Gson();
    private final String baseUrl = "https://api-satusehat-stg.dto.kemkes.go.id/fhir-r4/v1";
    private final String authUrl = "https://api-satusehat-stg.dto.kemkes.go.id/oauth2/v1";
    private final String consentUrl = "https://api-satusehat-stg.dto.kemkes.go.id/consent/v1";
    private final java.util.Map<String, String> requiredAuthentication = java.util.Map.of(
        "Org_id", "7b4db35e-ea4e-4b46-b389-095472942d34",
        "client_id", "rsqvpGQYeTGqbgpLHgWSVsbfcCADWJzsTVnUBMxlTXLYgAyt",
        "client_secret", "w5m5AM61EIzJuSwlhSS8OOyuE1EaTrQXuFxp0uAKf02pcWAReXyTb96Ze2NTGNQ1"
    );

    // GET request
    public String get(String url) throws IOException {
        UserSessionCache cache = new UserSessionCache();
        if (cache.getToken() == null) {
            getToken();
        }
        System.out.println(baseUrl + url);
        final String token = cache.getToken();
        HttpGet request = new HttpGet(baseUrl + url);
        request.setHeader("Authorization", "Bearer " + token);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
        }
    }

    // POST request (Object as JSON)
    public String post(String url, Object jsonBody) throws IOException {
        UserSessionCache cache = new UserSessionCache();
        if (cache.getToken() == null) {
            getToken();
        }
        String token = cache.getToken();
        HttpPost request = new HttpPost(baseUrl + url);
        request.setHeader("Authorization", "Bearer " + token);
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
        UserSessionCache cache = new UserSessionCache();
        if (cache.getToken() == null) {
            getToken();
        }
        String token = cache.getToken();
        HttpDelete request = new HttpDelete(baseUrl + url);
        request.setHeader("Authorization", "Bearer " + token);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
        }
    }

    // Auth request
    public String getToken() {
        try {
            // Create HttpClient instance
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // Create POST request
            HttpPost post = new HttpPost(authUrl + "/accesstoken?grant_type=client_credentials");

            // Convert Map to NameValuePair list
            List<NameValuePair> formParams = new ArrayList<>();
            for (Map.Entry<String, String> entry : requiredAuthentication.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            // Set form entity with URL-encoded parameters
            post.setEntity(new UrlEncodedFormEntity(formParams));

            // Send the request
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                System.out.println("Status Code: " + response.getStatusLine().getStatusCode());
                String responseBody = EntityUtils.toString(response.getEntity());

                // Parse JSON response
                JSONObject json = new JSONObject(responseBody);
                String accessToken = json.optString("access_token");
                new UserSessionCache().setToken(accessToken);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "Error";
    }

    public String encodeUrl(String regularPath, String encodedPath) {
        return regularPath + URLEncoder.encode(encodedPath, StandardCharsets.UTF_8);
    }

    // Jangan lupa tutup httpClient saat aplikasi selesai
    public void close() throws IOException {
        httpClient.close();
    }

    public static void main(String[] args) {
        ApiClient api = new ApiClient();
        try {
            final String response = api.get(api.encodeUrl("/Patient?identifier=", "https://fhir.kemkes.go.id/id/nik|9271060312000001"));
            System.out.println(response);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                api.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}