package org.edumips64.core;

import org.edumips64.utils.IrregularStringOfBitsException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the methods of FixedBitSet. Since it is an abstract base class,
 * the methods are tested through BitSet64.
 */
public class FixedBitSetTest {
    private BitSet64 bitset = new BitSet64();

    @Test(expected = IrregularStringOfBitsException.class)
    public void testIrregularStringOfBits() throws Exception {
        bitset.setBits("blah", 0);
    }
}