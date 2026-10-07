import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class Q1.4_LFUCache {
    private final int capacity;
    private int minFrequency;
    
    // key -> value
    private final Map<Integer, Integer> cache;
    // key -> frequency
    private final Map<Integer, Integer> keyFrequencies;
    // frequency -> keys (LinkedHashSet preserves LRU order)
    private final Map<Integer, LinkedHashSet<Integer>> frequencyList;

    public Q1.4_LFUCache(int capacity) {
        this.capacity = capacity;
        this.minFrequency = 0;
        this.cache = new HashMap<>();
        this.keyFrequencies = new HashMap<>();
        this.frequencyList = new HashMap<>();
    }

    public int get(int key) {
        if (!cache.containsKey(key)) {
            return -1;
        }
        updateFrequency(key);
        return cache.get(key);
    }

    public void put(int key, int value) {
        if (capacity == 0) return;

        if (cache.containsKey(key)) {
            cache.put(key, value);
            updateFrequency(key);
            return;
        }

        if (cache.size() >= capacity) {
            evictLFU();
        }

        cache.put(key, value);
        keyFrequencies.put(key, 1);
        frequencyList.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        minFrequency = 1;
    }

    private void updateFrequency(int key) {
        int currentFreq = keyFrequencies.get(key);
        keyFrequencies.put(key, currentFreq + 1);

        frequencyList.get(currentFreq).remove(key);
        if (currentFreq == minFrequency && frequencyList.get(currentFreq).isEmpty()) {
            minFrequency++;
        }

        frequencyList.computeIfAbsent(currentFreq + 1, k -> new LinkedHashSet<>()).add(key);
    }

    private void evictLFU() {
        LinkedHashSet<Integer> keys = frequencyList.get(minFrequency);
        int keyToEvict = keys.iterator().next(); // Gets the first element (LRU)
        keys.remove(keyToEvict);
        
        cache.remove(keyToEvict);
        keyFrequencies.remove(keyToEvict);
    }
}