package org.edumips64.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the methods of FixedBitSet. Since it is an abstract base class,
 * the methods are tested through BitSet64.
 */
public class FixedBitSetTest {
  private BitSet64 bitset = new BitSet64();

  // Tests that strings containing characters other than 1 and 0 raise exceptions.
  @Test(expected = IrregularStringOfBitsException.class)
  public void testIrregularStringOfBits() throws Exception {
    bitset.setBits("blah01", 0);
  }

  /**
   * Tests that getBinString() outputs exactly the string given in input to setBits,
   * assuming the full width of the bitset is covered in the setBits call.
   */
  @Test()
  public void testGetIsReciprocalToSet() throws Exception {
    String binEleven64Bits = "0000000000000000000000000000000000000000000000000000000000001011";
    bitset.setBits(binEleven64Bits, 0);
    String actualBitString = bitset.getBinString();
    assertEquals(binEleven64Bits, actualBitString);
  }

  // Test all the unsigned byte values.
  @Test()
  public void testByteValues() throws Exception {
      // 32 zeros, used as a prefix for the expected string.
      String zeroPrefix = "00000000000000000000000000000000";
      for(int i = 1; i < 256; ++i) {
          String binary = Integer.toBinaryString(i);
          bitset.setBits(binary, 64 - binary.length());
          // Add the required padding, since getBinString always returns a string long N bits
          // (where N is the length of the bitset);
          String expected = zeroPrefix;
          for (int j = 0; j < Integer.numberOfLeadingZeros(i); ++j) {
             expected += '0';
          }
          expected += binary;
          assertEquals(expected, bitset.getBinString());
      }
  }
}