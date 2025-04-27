package org.edumips64.core;

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.io.WriteException;
import org.edumips64.utils.io.Writer;

import java.util.*;

public class CacheSimulator {

    private static final int DEFAULT_CACHE_SIZE = 1024;
    private static final int DEFAULT_BLOCK_SIZE = 16;
    private static final int DEFAULT_ASSOCIATIVITY = 1;
    private static final int DEFAULT_PENALTY = 40;

    final private CacheMemory L1InstructionCache;
    final private CacheMemory L1DataCache;


    private LinkedList<String> dineroData = new LinkedList<>();
    // Offset of the data segment. This class writes a trace file that assumes
    // that the data segment starts immediately after the code segment ends.
    private int offset;

    public CacheSimulator() {
        L1InstructionCache = new CacheMemory(
            new CacheConfig(DEFAULT_CACHE_SIZE, DEFAULT_BLOCK_SIZE, DEFAULT_ASSOCIATIVITY, DEFAULT_PENALTY),
            CacheType.L1_INSTRUCTION
        );
        L1DataCache = new CacheMemory(
            new CacheConfig(DEFAULT_CACHE_SIZE, DEFAULT_BLOCK_SIZE, DEFAULT_ASSOCIATIVITY, DEFAULT_PENALTY),
            CacheType.L1_DATA
        );
    }

    public CacheMemory getL1DataCache() {
        return L1DataCache;
    }
    public CacheMemory getL1InstructionCache() {
        return L1InstructionCache;
    }

    public enum CacheType {
        L1_DATA,
        L1_INSTRUCTION,
        L1_UNIFIED
    }


    public static class CacheConfig {
        public int size, blockSize, associativity, penalty;

        public CacheConfig(int size, int blockSize, int associativity, int penalty) {
            this.size = size;
            this.blockSize = blockSize;
            this.associativity = associativity;
            this.penalty = penalty;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheConfig that = (CacheConfig) o;
            return size == that.size &&
                    blockSize == that.blockSize &&
                    associativity == that.associativity &&
                    penalty == that.penalty;
        }

        @Override
        public int hashCode() {
            return Objects.hash(size, blockSize, associativity, penalty);
        }

        @Override
        public String toString() {
            return "{size:" + size + ", bsize:" + blockSize +" assoc:"+ associativity+'}';
        }
    }


    public static class CacheMemory {

        private CacheSimulator.CacheType type;
        private CacheConfig config;
        private int numSets;
        private CacheSet[] sets;
        private int blockOffsetBits;
        private int indexBits;
        public Stats stats;

        public CacheMemory(CacheConfig config, CacheSimulator.CacheType type) {
            stats = new Stats();
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
            String prefix = (type == CacheSimulator.CacheType.L1_DATA) ? "L1D" : "L1I";
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

        public Stats getStats() {
            return stats;
        }
    }

   public static class Stats {
        private long readAccesses;
        private long writeAccesses;
        private long readMisses;
        private long writeMisses;

        public Stats() {
            reset();
        }

        public static Stats of(long readAccesses, long readMisses, long writeAccesses, long writeMisses) {
            Stats stats = new Stats();
            stats.readAccesses = readAccesses;
            stats.readMisses = readMisses;
            stats.writeAccesses = writeAccesses;
            stats.writeMisses = writeMisses;
            return stats;
        }

        public void reset() {
            this.readAccesses = 0;
            this.readMisses = 0;
            this.writeAccesses = 0;
            this.writeMisses = 0;
        }

        public long getReadAccesses() { return readAccesses; }
        public long getReadMisses() { return readMisses; }
       public long getWriteAccesses() { return writeAccesses; }
        public long getWriteMisses() { return writeMisses; }

        public void incrementReadAccesses() { readAccesses++; }
        public void incrementWriteAccesses() { writeAccesses++; }
        public void incrementReadMisses() { readMisses++; }
        public void incrementWriteMisses() { writeMisses++; }

       @Override
       public boolean equals(Object o) {
           if (this == o) return true;
           if (o == null || getClass() != o.getClass()) return false;
           Stats stats = (Stats) o;
           return readAccesses == stats.readAccesses && writeAccesses == stats.writeAccesses && readMisses == stats.readMisses && writeMisses == stats.writeMisses;
       }

       @Override
       public int hashCode() {
           return Objects.hash(readAccesses, writeAccesses, readMisses, writeMisses);
       }

       @Override
        public String toString() {
            return "CacheStatistics{" +
                    "readAccesses=" + readAccesses +
                    ", readMisses=" + readMisses +
                    ", writeAccesses=" + writeAccesses +
                    ", writeMisses=" + writeMisses +
                    '}';
        }
    }


    // A cache set holds a number of cache lines equal to the associativity.
    static class CacheSet {
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
    static class CacheLine {
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

    public static void processDineroTraceEntry(CacheMemory cache, String line) {
        if (line.trim().isEmpty())
            return;

        // Each line should contain 3 space-separated fields:
        // <refType> <address> <size>
        String[] parts = line.split("\\s+");
        if (parts.length != 3) {
            System.err.println("Invalid trace line: " + line);
            return;
        }

        char refType = parts[0].charAt(0);
        String addressStr = parts[1];
        // Use Long.decode to support both hex (with 0x) and decimal
        long address = Long.decode("0x"+addressStr);
        int size = Integer.parseInt(parts[2]); // size is parsed but not used in this simple simulator

        if (refType == 'i' && cache.type == CacheType.L1_INSTRUCTION) {
            cache.stats.incrementReadAccesses();
            boolean hit = cache.access(address, false);
            if (!hit) {
                cache.stats.incrementReadMisses();
            }
            return;
        }

        if (refType == 'r' && cache.type == CacheType.L1_DATA) {
            cache.stats.incrementReadAccesses();
            boolean hit = cache.access(address, true);
            if (!hit) {
                cache.stats.incrementReadMisses();
            }
            return;
        }

        if (refType == 'w' && cache.type == CacheType.L1_DATA) {
            cache.stats.incrementWriteAccesses();
            boolean hit = cache.access(address, true);
            if (!hit) {
                cache.stats.incrementWriteMisses();
            }
            return;
        }
    }

    /** Sets the data offset.
     * @param dataOffset offset of the data section. Should be after the code
     *                   section. Typically this is the number of instructions
     *                   times 4 (each instruction takes 32 bits).
     */
    public void setDataOffset(int dataOffset) {
        // Align the dataOffset to 64 bit if needed.
        offset = dataOffset + dataOffset % 8;
    }

    public void reset() {
        offset = 0;
        dineroData = new LinkedList <>();
        if (L1DataCache!=null) L1DataCache.resetStatus();
        if (L1InstructionCache!=null) L1InstructionCache.resetStatus();
    }

    /** Add a read Instruction
     * @param address address of the read Instruction
     */
    public void IF(String address) {
        String entry = "i " + address + " 4";
        dineroData.add(entry);
        processDineroTraceEntry(this.getL1InstructionCache(),entry);
    }

    public void Load(String address, int nByte) {
        try {
            long addr = Long.parseLong(Converter.hexToLong("0x" + address));
            addr += offset;
            String entry = "r " + Converter.binToHex(Converter.intToBin(64, addr)) + " " + nByte;
            dineroData.add(entry);
            processDineroTraceEntry(this.getL1DataCache(),entry);
        } catch (IrregularStringOfHexException | IrregularStringOfBitsException ex) {
            ex.printStackTrace();
        }
    }

    public void Store(String address, int nByte) {
        try {
            long addr = Long.parseLong(Converter.hexToLong("0x" + address));
            addr += offset;
            String entry = "w " + Converter.binToHex(Converter.intToBin(64, addr)) + " " + nByte;
            dineroData.add(entry);
            processDineroTraceEntry(this.getL1DataCache(),entry);
        } catch (IrregularStringOfHexException | IrregularStringOfBitsException ex) {
            ex.printStackTrace();
        }

    }

    /** Writes the trace data to a Writer
     *  @param buff the Writer to output the data to
     */
    public void writeTraceData(Writer buff) throws java.io.IOException, WriteException {
        for (int i = 0; i < dineroData.size(); i++) {
            String tmp = dineroData.get(i) + "\n";
            buff.write(tmp);
        }
    }
}
