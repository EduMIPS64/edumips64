package org.edumips64.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CacheSimulator {

    public static void main(String[] args) {
        /* Ignored for quickly testing

        if (args.length != 5) {
            System.out.println("Usage: java CacheSimulator <cacheSize> <blockSize> <associativity> <cacheType> <traceFile>");
            System.exit(1);
        }
         */
        var cache = new CacheSimulator();
        String args2[] = {"1024", "16", "2", "d", "code.s.xdin"};
        cache.Simulate(args2);
    }

    public void Simulate(String[] args) {

        // Parse command line arguments
        int cacheSize = Integer.parseInt(args[0]);
        int blockSize = Integer.parseInt(args[1]);
        int associativity = Integer.parseInt(args[2]);
        String cacheType = args[3].toLowerCase(); // expected "data" or "instruction"
        String traceFile = args[4];

        // Initialize the cache
        Cache cache = new Cache(cacheSize, blockSize, associativity);

        // Statistics counters
        long readAccesses = 0, writeAccesses = 0, readMisses = 0, writeMisses = 0;

        // Process the trace file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(traceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;

                // Each line should contain 3 space-separated fields:
                // <refType> <address> <size>
                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    System.err.println("Invalid trace line: " + line);
                    continue;
                }

                char refType = parts[0].charAt(0);
                // Filter lines based on the specified cache type
                if (cacheType.equals("data") && (refType != 'r' && refType != 'w')) {
                    continue;
                }
                if (cacheType.equals("instruction") && refType != 'i') {
                    continue;
                }

                String addressStr = parts[1];
                // Use Long.decode to support both hex (with 0x) and decimal
                long address = Long.decode("0x"+addressStr);
                int size = Integer.parseInt(parts[2]); // size is parsed but not used in this simple simulator

                // Process access based on operation type.
                // 'i' and 'r' are treated as reads; 'w' is treated as a write.
                if (refType == 'i' || refType == 'r') {
                    readAccesses++;
                    boolean hit = cache.access(address, false);
                    if (!hit) {
                        readMisses++;
                    }
                } else if (refType == 'w') {
                    writeAccesses++;
                    boolean hit = cache.access(address, true);
                    if (!hit) {
                        writeMisses++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Output the results as a data structure
        CacheStatistics stats = new CacheStatistics(readAccesses, writeAccesses, readMisses, writeMisses);
        System.out.println(stats);
    }
}

class CacheStatistics {
    public long readAccesses;
    public long writeAccesses;
    public long readMisses;
    public long writeMisses;

    public CacheStatistics(long readAccesses, long writeAccesses, long readMisses, long writeMisses) {
        this.readAccesses = readAccesses;
        this.writeAccesses = writeAccesses;
        this.readMisses = readMisses;
        this.writeMisses = writeMisses;
    }

    @Override
    public String toString() {
        return "CacheStatistics{" +
                "readAccesses=" + readAccesses +
                ", writeAccesses=" + writeAccesses +
                ", readMisses=" + readMisses +
                ", writeMisses=" + writeMisses +
                '}';
    }
}

// The Cache class divides the cache into sets.
class Cache {
    private int cacheSize;
    private int blockSize;
    private int associativity;
    private int numSets;
    private CacheSet[] sets;
    private int blockOffsetBits;
    private int indexBits;

    public Cache(int cacheSize, int blockSize, int associativity) {
        this.cacheSize = cacheSize;
        this.blockSize = blockSize;
        this.associativity = associativity;
        // Calculate the number of sets in the cache:
        // numSets = cacheSize / (blockSize * associativity)
        this.numSets = cacheSize / (blockSize * associativity);
        sets = new CacheSet[numSets];
        for (int i = 0; i < numSets; i++) {
            sets[i] = new CacheSet(associativity);
        }
        // Calculate number of bits for block offset (log2(blockSize))
        blockOffsetBits = (int)(Math.log(blockSize) / Math.log(2));
        // Calculate number of index bits (log2(numSets)) if more than one set exists
        indexBits = (numSets > 1) ? (int)(Math.log(numSets) / Math.log(2)) : 0;
    }

    // Simulate a cache access. The parameter isWrite indicates if the access is a write.
    // Returns true for a cache hit, false for a miss.
    public boolean access(long address, boolean isWrite) {
        // Remove block offset bits from address
        long tag = address >> blockOffsetBits;
        int index = 0;
        // If there is more than one set, extract the index bits and adjust the tag accordingly.
        if (numSets > 1) {
            index = (int)(tag & ((1 << indexBits) - 1));
            tag = tag >> indexBits;
        }
        CacheSet set = sets[index];
        return set.access(tag, isWrite);
    }
}

// A cache set holds a number of cache lines equal to the associativity.
class CacheSet {
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

// A cache line represents a block in the cache.
class CacheLine {
    public long tag;
    public boolean valid;
    public boolean dirty;
    public int lastUsed; // timestamp for LRU

    public CacheLine(long tag, boolean valid, boolean dirty, int lastUsed) {
        this.tag = tag;
        this.valid = valid;
        this.dirty = dirty;
        this.lastUsed = lastUsed;
    }
}
