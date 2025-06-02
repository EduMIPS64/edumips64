package org.edumips64.core.cache;

import java.util.Objects;

public class CacheConfig {
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