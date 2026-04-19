package store;

import java.util.concurrent.ConcurrentHashMap;

public class KeyValueStore {


    // Thread-safe key-value store
    private ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> expiryStore = new ConcurrentHashMap<String, Long>();

    // Set a key to a value
    public void set(String key, String value, boolean willExpire, long expiryTime) {
        store.put(key, value);
        if (willExpire){
            expiryStore.put(key,System.currentTimeMillis()+expiryTime);
        }
    }

    // Get the value for a key
    public String get(String key) {
        runLifeCheck(key);
        String value = store.get(key);
        return value == null ? "" : value;
    }

    // Delete a key
    public void delete(String key) {
        store.remove(key);
        expiryStore.remove(key);
    }

    //check if alive, remove if dead
    public void runLifeCheck(String key){
        if(expiryStore.containsKey(key) && System.currentTimeMillis() >= expiryStore.get(key)) {
            delete(key);
        }
    }
}


