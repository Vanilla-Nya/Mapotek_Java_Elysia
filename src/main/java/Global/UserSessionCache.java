/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Global;

/**
 *
 * @author Luna
 */
import java.time.LocalDateTime;
import java.util.HashMap;

public class UserSessionCache {

    // Simulating an in-memory cache using HashMap
    private static final HashMap<String, String> cache = new HashMap<>();

    // Method to simulate user login and cache their session data
    public void login(String username, String uuid) {

        // Save some session data (for example, username) in the cache
        cache.put("uuid", uuid);
        cache.put("username", username);
        cache.put("loginAt", LocalDateTime.now().toString());
        System.out.println("Added login data for UUID: " + uuid + " with username: " + username);
    }

    // Method to retrieve session data from the cache
    public String getUUID() {
        if (cache.containsKey("uuid")) {
            return cache.get("uuid");
        } else {
            return null;
        }
    }

    // Method to retrieve session username from the cache
    public String getusername() {
        if (cache.containsKey("username")) {
            return cache.get("username");
        } else {
            return null;
        }
    }

    public String getLoginAt() {
        if (cache.containsKey("uuid")) {
            return cache.get("loginAt");
        } else {
            return null;
        }
    }

    // Clear All Cache
    public void clearCache() {
        cache.clear();
    }
}
