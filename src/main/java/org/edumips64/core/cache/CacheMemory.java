package org.edumips64.core.cache;

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigKey;

public class CacheMemory {

    private CacheType type;
    private CacheConfig config;
    private int numSets;
    private CacheSet[] sets;
    private int blockOffsetBits;
    private int indexBits;
    private CacheStats stats;

    public CacheMemory(CacheConfig config, CacheType type) {
        stats = new CacheStats();
        this.type = type;
        setConfig(config);
    }

    public void eraseCacheContent() {
        for (int i = 0; i < numSets; i++) {
            sets[i] = new CacheSet(config.associativity);
        }
    }

    public void setConfig(CacheConfig config) {
        this.config = config;
        this.numSets = config.size / (config.blockSize * config.associativity);
        sets = new CacheSet[numSets];
        for (int i = 0; i < numSets; i++) {
            sets[i] = new CacheSet(config.associativity);
        }
        // Calculate number of bits for block offset (log2(blockSize))
        blockOffsetBits = (int)(Math.log(config.blockSize) / Math.log(2));
        // Calculate number of index bits (log2(numSets)) if more than one set exists
        indexBits = (numSets > 1) ? (int)(Math.log(numSets) / Math.log(2)) : 0;
        this.stats.reset();
    }

    public void setConfig(ConfigStore config) {
        String prefix = (type == CacheType.L1_DATA) ? "L1D" : "L1I";
        int cacheSize = config.getInt(ConfigKey.valueOf(prefix + "_SIZE"));
        int blockSize = config.getInt(ConfigKey.valueOf(prefix + "_BLOCK_SIZE"));
        int associativity = config.getInt(ConfigKey.valueOf(prefix + "_ASSOCIATIVITY"));
        int penalty = config.getInt(ConfigKey.valueOf(prefix + "_PENALTY"));
        CacheConfig cc = new CacheConfig(cacheSize, blockSize, associativity, penalty);
        setConfig(cc);
    }

    public void resetStatus() {
        eraseCacheContent();
        this.stats.reset();
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

    @Override
    public String toString() {
        return "CacheMemory{" + type + config + '}';
    }

    public CacheStats getStats() {
        return stats;
    }

    public CacheType getType() {
        return type;
    }
}