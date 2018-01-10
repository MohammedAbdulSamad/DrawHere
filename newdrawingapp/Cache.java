package sam.newdrawingapp;

import android.util.LruCache;

/**
 * Created by Sam on 1/3/2018.
 */

public class Cache {
    private static Cache instance;
    private LruCache<Object, Object> lru;

    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private final int cacheSize = maxMemory / 8;

    private Cache() {
        lru = new LruCache<Object,Object>(cacheSize);
    }

    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

    public LruCache<Object,Object> getLru() {
        return lru;
    }
}
