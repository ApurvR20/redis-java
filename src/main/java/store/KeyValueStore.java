package store;

import java.util.concurrent.ConcurrentHashMap;

public class KeyValueStore {

    // Defining a class inside the main class, not sure how good it is
    class KeyInfo {

        private String value;
        private long expiryTime;

        public KeyInfo(String value, long lifespan){
            this.value = value;
            expiryTime = System.currentTimeMillis() + lifespan;
        }

        public String getValue(){
            return value;
        }

        public long getExpiryTime(){
            return expiryTime;
        }
    
    }

    // Thread-safe key-value store
    private ConcurrentHashMap<String, KeyInfo> store = new ConcurrentHashMap<>();

    // Set a key to a value
    public void set(String key, String value, long expiryTime) {
        KeyInfo info = new KeyInfo(value, expiryTime);
        store.put(key, info);
    }

    // Get the value for a key
    public String get(String key) {
        //handle for no key
        return store.get(key).getValue();
    }

    // Delete a key
    public void delete(String key) {
        store.remove(key);
    }

    // Check if key exists
    public boolean exists(String key) {
        return store.containsKey(key);
    }

    //check if alive
    public boolean isAlive(String key){

        if(exists(key) && System.currentTimeMillis() < store.get(key).getExpiryTime()){
            return true;
        }

        delete(key);
        return false;
    }
}


