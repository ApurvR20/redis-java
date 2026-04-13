package helpers;
import store.KeyValueStore;


public class Expiry {
    
    long getCurrentTime(){
        return System.currentTimeMillis();
    }

    public static String setExpiry(KeyValueStore kv,String key, String time) {
        
        return "";
    }
}
