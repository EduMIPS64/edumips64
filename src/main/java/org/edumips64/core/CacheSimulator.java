package org.edumips64.core;

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.io.WriteException;
import org.edumips64.utils.io.Writer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CacheSimulator {

    private CacheMemory L1InstructionCache;
    private CacheMemory L1DataCache;

    private LinkedList<String> dineroData = new LinkedList<>();
    // Offset of the data segment. This class writes a trace file that assumes
    // that the data segment starts immediately after the code segment ends.
    private int offset;

    public CacheSimulator() {
        // fake values to be replaced
        L1InstructionCache = new CacheMemory(1025,17,2,40, CacheMemory.CacheType.L1_INSTRUCTION);
        L1DataCache = new CacheMemory(1025,17,2,40, CacheMemory.CacheType.L1_DATA);
    }

    public CacheMemory getL1DataCache() {
        return L1DataCache;
    }
    public CacheMemory getL1InstructionCache() {
        return L1InstructionCache;
    }


    public static class CacheMemory {
        public enum CacheType {
            L1_DATA,
            L1_INSTRUCTION,
            L1_UNIFIED
        }

        private CacheType type;
        private int cacheSize;
        private int blockSize;
        private int associativity;
        private int penaly;
        private int numSets;
        private CacheSet[] sets;
        private int blockOffsetBits;
        private int indexBits;
        private CacheStatistics stats;

        public CacheMemory(int cacheSize, int blockSize, int associativity, int penaly, CacheType type) {
            stats = new CacheStatistics();
            setConfig(cacheSize,blockSize,associativity,penaly, type);
        }


        public void setConfig(int cacheSize, int blockSize, int associativity, int penaly, CacheType cacheType) {
            this.cacheSize = cacheSize;
            this.blockSize = blockSize;
            this.associativity = associativity;
            this.penaly = penaly;
            this.type = cacheType;
            this.numSets = cacheSize / (blockSize * associativity);
            sets = new CacheSet[numSets];
            for (int i = 0; i < numSets; i++) {
                sets[i] = new CacheSet(associativity);
            }
            // Calculate number of bits for block offset (log2(blockSize))
            blockOffsetBits = (int)(Math.log(blockSize) / Math.log(2));
            // Calculate number of index bits (log2(numSets)) if more than one set exists
            indexBits = (numSets > 1) ? (int)(Math.log(numSets) / Math.log(2)) : 0;
            resetStats();
        }

        public void setConfig(ConfigStore config) {
            String prefix = (type == CacheType.L1_DATA) ? "L1D" : "L1I";
            int cacheSize = config.getInt(ConfigKey.valueOf(prefix + "_SIZE"));
            int blockSize = config.getInt(ConfigKey.valueOf(prefix + "_BLOCK_SIZE"));
            int associativity = config.getInt(ConfigKey.valueOf(prefix + "_ASSOCIATIVITY"));
            int penaly = config.getInt(ConfigKey.valueOf(prefix + "_PENALTY"));
            setConfig(cacheSize, blockSize, associativity, penaly, type);
        }

        public void resetStats() {
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

        public CacheStatistics getStats() {
            return stats;
        }
    }

    public static void main(String[] args) {
        var cache = new CacheMemory(1024, 16, 2, 40, CacheMemory.CacheType.L1_DATA);
        CacheSimulator cache_sim = new CacheSimulator();
        //cache_sim.SimulateTrace(cache,"code.s.xdin");
        System.out.println(cache.getStats());
    }

   public static class CacheStatistics {
        private long readAccesses;
        private long writeAccesses;
        private long readMisses;
        private long writeMisses;

        public CacheStatistics() {
            reset();
        }

        public void reset() {
            this.readAccesses = 0;
            this.writeAccesses = 0;
            this.readMisses = 0;
            this.writeMisses = 0;
        }

        public long getReadAccesses() { return readAccesses; }
        public long getWriteAccesses() { return writeAccesses; }
        public long getReadMisses() { return readMisses; }
        public long getWriteMisses() { return writeMisses; }

        public void incrementReadAccesses() { readAccesses++; }
        public void incrementWriteAccesses() { writeAccesses++; }
        public void incrementReadMisses() { readMisses++; }
        public void incrementWriteMisses() { writeMisses++; }

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

    private void processDineroTraceEntry(CacheMemory cache, String line) {
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
        // Filter lines based on the specified cache type
        // mismatching cache types should not happen, this is a sanity check
        if (cache.type == CacheMemory.CacheType.L1_DATA && (refType != 'r' && refType != 'w')) {
            System.err.println("Invalid trace line: " + line+ " for cache type: "+cache.type);
            return;
        }
        if (cache.type == CacheMemory.CacheType.L1_INSTRUCTION && refType != 'i') {
            System.err.println("Invalid trace line: " + line+ " for cache type: "+cache.type);
            return;
        }

        String addressStr = parts[1];
        // Use Long.decode to support both hex (with 0x) and decimal
        long address = Long.decode("0x"+addressStr);
        int size = Integer.parseInt(parts[2]); // size is parsed but not used in this simple simulator

        // Process access based on operation type.
        // 'i' and 'r' are treated as reads; 'w' is treated as a write.
        if (refType == 'i' || refType == 'r') {
            cache.stats.incrementReadAccesses();
            boolean hit = cache.access(address, false);
            if (!hit) {
                cache.stats.incrementReadMisses();
            }
        } else if (refType == 'w') {
            cache.stats.incrementWriteAccesses();
            boolean hit = cache.access(address, true);
            if (!hit) {
                cache.stats.incrementWriteMisses();
            }
        }

    }

    /* For testing
    public void SimulateTrace(CacheMemory cache, String traceFile) {

        cache.resetStats();
        // Process the trace file line by line
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(traceFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        };
        try  {
            String line;
            while ((line = br.readLine()) != null) {
                processDineroTraceEntry(cache, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(cache.stats);

    }
     */

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
        if (L1DataCache!=null) L1DataCache.resetStats();
        if (L1InstructionCache!=null) L1InstructionCache.resetStats();
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
