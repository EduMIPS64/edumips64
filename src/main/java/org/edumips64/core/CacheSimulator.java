package org.edumips64.core;

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.io.WriteException;
import org.edumips64.utils.io.Writer;

// Import classes from cache package
import org.edumips64.core.cache.CacheConfig;
import org.edumips64.core.cache.CacheMemory;
import org.edumips64.core.cache.CacheStats;
import org.edumips64.core.cache.CacheType;

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

    // Re-export CacheType for backward compatibility
    public enum CacheType {
        L1_DATA,
        L1_INSTRUCTION,
        L1_UNIFIED;

        public org.edumips64.core.cache.CacheType toCacheType() {
            return org.edumips64.core.cache.CacheType.valueOf(this.name());
        }
    }


    // Re-export CacheConfig for backward compatibility
    public static class CacheConfig extends org.edumips64.core.cache.CacheConfig {
        public CacheConfig(int size, int blockSize, int associativity, int penalty) {
            super(size, blockSize, associativity, penalty);
        }
    }


    // Re-export CacheMemory for backward compatibility
    public static class CacheMemory extends org.edumips64.core.cache.CacheMemory {
        public CacheMemory(CacheConfig config, CacheSimulator.CacheType type) {
            super(config, type.toCacheType());
        }
        
        // Override getStats to return the wrapper Stats class
        @Override
        public Stats getStats() {
            CacheStats cacheStats = super.getStats();
            return Stats.of(cacheStats.getReadAccesses(), cacheStats.getReadMisses(), 
                           cacheStats.getWriteAccesses(), cacheStats.getWriteMisses());
        }
    }

   // Re-export Stats for backward compatibility
   public static class Stats extends org.edumips64.core.cache.CacheStats {
        public static Stats of(long readAccesses, long readMisses, long writeAccesses, long writeMisses) {
            Stats stats = new Stats();
            // Copy the values by calling the appropriate increments
            for (int i = 0; i < readAccesses; i++) stats.incrementReadAccesses();
            for (int i = 0; i < readMisses; i++) stats.incrementReadMisses();
            for (int i = 0; i < writeAccesses; i++) stats.incrementWriteAccesses();
            for (int i = 0; i < writeMisses; i++) stats.incrementWriteMisses();
            return stats;
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

        // Need to access the underlying cache to get type and stats
        org.edumips64.core.cache.CacheMemory cacheImpl = (org.edumips64.core.cache.CacheMemory) cache;
        
        if (refType == 'i' && cacheImpl.getType() == org.edumips64.core.cache.CacheType.L1_INSTRUCTION) {
            cacheImpl.getStats().incrementReadAccesses();
            boolean hit = cacheImpl.access(address, false);
            if (!hit) {
                cacheImpl.getStats().incrementReadMisses();
            }
            return;
        }

        if (refType == 'r' && cacheImpl.getType() == org.edumips64.core.cache.CacheType.L1_DATA) {
            cacheImpl.getStats().incrementReadAccesses();
            boolean hit = cacheImpl.access(address, true);
            if (!hit) {
                cacheImpl.getStats().incrementReadMisses();
            }
            return;
        }

        if (refType == 'w' && cacheImpl.getType() == org.edumips64.core.cache.CacheType.L1_DATA) {
            cacheImpl.getStats().incrementWriteAccesses();
            boolean hit = cacheImpl.access(address, true);
            if (!hit) {
                cacheImpl.getStats().incrementWriteMisses();
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
