package org.edumips64.core;

import org.edumips64.utils.io.WriteException;
import org.edumips64.utils.io.Writer;

import java.io.BufferedReader;
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
        // todo, use data from interface
        L1InstructionCache = new CacheMemory(1024,16,2, CacheMemory.CacheType.L1_INSTRUCTION);
        L1DataCache = new CacheMemory(1024,16,2, CacheMemory.CacheType.L1_DATA);
    }

    static class CacheMemory {
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

        public CacheMemory(int cacheSize, int blockSize, int associativity, CacheType type) {
            setConfig(cacheSize,blockSize,associativity,type);
        }
        public void setConfig(int cacheSize, int blockSize, int associativity, CacheType cacheType) {
            this.cacheSize = cacheSize;
            this.blockSize = blockSize;
            this.associativity = associativity;
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
        public void resetStats() {
            this.stats = new CacheStatistics(0, 0, 0, 0);
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
        var cache = new CacheMemory(1024, 16, 2, CacheMemory.CacheType.L1_DATA);
        CacheSimulator cache_sim = new CacheSimulator();
        cache_sim.SimulateTrace(cache,"code.s.xdin");
        System.out.println(cache.getStats());
    }

    record CacheStatistics(long readAccesses, long writeAccesses, long readMisses, long writeMisses) {
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


    public void SimulateTrace(CacheMemory cache, String traceFile) {

        cache.resetStats();
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
                if (cache.type == CacheMemory.CacheType.L1_DATA && (refType != 'r' && refType != 'w')) {
                    continue;
                }
                if (cache.type == CacheMemory.CacheType.L1_INSTRUCTION && refType != 'i') {
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
        cache.stats = new CacheStatistics(readAccesses, writeAccesses, readMisses, writeMisses);
        System.out.println(cache.stats);
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
        if (L1DataCache!=null) L1DataCache.resetStats();
        if (L1InstructionCache!=null) L1InstructionCache.resetStats();
    }

    /** Add a read Instruction
     * @param address address of the read Instruction
     */
    public void IF(String address) {
        dineroData.add("i " + address + " 4");
    }

    public void Load(String address, int nByte) {
        try {
            long addr = Long.parseLong(Converter.hexToLong("0x" + address));
            addr += offset;
            dineroData.add("r " + Converter.binToHex(Converter.intToBin(64, addr)) + " " + nByte);
        } catch (IrregularStringOfHexException | IrregularStringOfBitsException ex) {
            ex.printStackTrace();
        }
    }

    public void Store(String address, int nByte) {
        try {
            long addr = Long.parseLong(Converter.hexToLong("0x" + address));
            addr += offset;
            dineroData.add("w " + Converter.binToHex(Converter.intToBin(64, addr)) + " " + nByte);
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
