package ru.rgs.logging.logback;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache extends LinkedHashMap<String, Integer> {
 	private static final long serialVersionUID = 1L;
	private static final String WRONG_CACHE_SIZE_SIZE_MUST_BE_MORE_THEN_0 = "Wrong cache size! Size must be more then 0.";
	private final int cacheSize;

    public LRUCache(int cacheSize) {
        super(cacheSize);
        if (cacheSize < 1) {
            throw new IllegalArgumentException(WRONG_CACHE_SIZE_SIZE_MUST_BE_MORE_THEN_0);
        }
        this.cacheSize = cacheSize;
    }
    
    /**
     * Add message and return message number. Don't insert null strings
     * @param trace
     * @return number. If message was added in the map for the first time - it 
     * returns 1 and so on
     */
    int addStackTrace(String trace) {
        if (trace == null) {
            return 0;
        }
        Integer index;
        synchronized (this) {
            index = super.get(trace);
            if (index == null) {
                index = 1;
            } else {
                index++;
            }
            super.put(trace, index);
        }
        return index;
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return (size() > cacheSize);
    }


}
