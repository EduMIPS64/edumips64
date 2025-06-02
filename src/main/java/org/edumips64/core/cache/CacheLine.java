package org.edumips64.core.cache;

// A cache line represents a block in the cache.
public class CacheLine {
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