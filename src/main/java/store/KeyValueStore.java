package store;

import java.util.concurrent.ConcurrentHashMap;

public class KeyValueStore {

    // Thread-safe key-value store
    private ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

    // Set a key to a value
    public void set(String key, String value) {
        store.put(key, value);
    }

    // Get the value for a key
    public String get(String key) {
        //handle for no key
        return store.get(key);
    }

    // Delete a key
    public void delete(String key) {
        store.remove(key);
    }

    // Check if key exists
    public boolean exists(String key) {
        return store.containsKey(key);
    }
}
