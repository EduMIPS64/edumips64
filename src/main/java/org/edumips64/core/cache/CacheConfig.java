package org.edumips64.core.cache;

import java.util.Objects;

public class CacheConfig {
    public int size, blockSize, associativity;

    public CacheConfig(int size, int blockSize, int associativity) {
        this.size = size;
        this.blockSize = blockSize;
        this.associativity = associativity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheConfig that = (CacheConfig) o;
        return size == that.size &&
                blockSize == that.blockSize &&
                associativity == that.associativity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, blockSize, associativity);
    }

    @Override
    public String toString() {
        return "{size:" + size + ", bsize:" + blockSize + ", assoc:" + associativity + '}';
    }
}