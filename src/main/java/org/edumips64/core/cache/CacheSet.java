package org.edumips64.core.cache;

import java.util.*;

// A cache set holds a number of cache lines equal to the associativity.
public class CacheSet {
    private int associativity;
    private List<CacheLine> lines;
    private int timestamp = 0; // used to implement LRU

    public CacheSet(int associativity) {
        this.associativity = associativity;
        lines = new ArrayList<>();
    }

    // Simulate an access within this set.
    // Returns true if it is a hit; false if it is a miss (and the block is loaded).
    public boolean access(long tag, boolean isWrite) {
        timestamp++; // update time for LRU tracking

        // Check if the block is already in the set.
        for (CacheLine line : lines) {
            if (line.valid && line.tag == tag) {
                // Cache hit: update LRU timestamp
                line.lastUsed = timestamp;
                // For a write, mark the line as dirty (write-back)
                if (isWrite) {
                    line.dirty = true;
                }
                return true;
            }
        }

        // Cache miss: block must be loaded (write-allocate applies to both reads and writes)
        if (lines.size() < associativity) {
            // There is room in the set – add a new line.
            CacheLine newLine = new CacheLine(tag, true, isWrite, timestamp);
            lines.add(newLine);
        } else {
            // Set is full – use LRU policy to evict the least recently used line.
            CacheLine lru = lines.get(0);
            for (CacheLine line : lines) {
                if (line.lastUsed < lru.lastUsed) {
                    lru = line;
                }
            }
            // If the LRU line is dirty, in a real simulator we would perform a write-back.
            // (For this simulation, the write-back is assumed to be handled without extra stats.)
            lru.tag = tag;
            lru.valid = true;
            lru.dirty = isWrite; // mark as dirty if this is a write operation
            lru.lastUsed = timestamp;
        }
        return false; // miss occurred
    }
}