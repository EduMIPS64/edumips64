package org.edumips64.core.cache;

import java.util.Objects;

public class CacheStats {
    private long readAccesses;
    private long writeAccesses;
    private long readMisses;
    private long writeMisses;

    public CacheStats() {
        reset();
    }

    public static CacheStats of(long readAccesses, long readMisses, long writeAccesses, long writeMisses) {
        CacheStats stats = new CacheStats();
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
        CacheStats stats = (CacheStats) o;
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