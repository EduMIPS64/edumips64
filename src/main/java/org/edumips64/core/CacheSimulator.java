package org.edumips64.core;

import org.edumips64.utils.io.WriteException;
import org.edumips64.utils.io.Writer;
import org.edumips64.core.cache.CacheConfig;
import org.edumips64.core.cache.CacheMemory;
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

        if (refType == 'i' && cache.getType() == CacheType.L1_INSTRUCTION) {
            cache.stats.incrementReadAccesses();
            boolean hit = cache.access(address, false);
            if (!hit) {
                cache.stats.incrementReadMisses();
            }
            return;
        }

        if (refType == 'r' && cache.getType() == CacheType.L1_DATA) {
            cache.stats.incrementReadAccesses();
            boolean hit = cache.access(address, true);
            if (!hit) {
                cache.stats.incrementReadMisses();
            }
            return;
        }

        if (refType == 'w' && cache.getType() == CacheType.L1_DATA) {
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
