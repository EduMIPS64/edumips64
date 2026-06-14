package org.edumips64.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

  // Tests for writeDouble and readDouble methods added during RegisterFP refactoring

  @Test
  public void testWriteDoubleAndReadDouble() throws Exception {
    bitset.writeDouble(3.14);
    assertEquals("3.14", bitset.readDouble());
  }

  @Test
  public void testWriteDoubleZero() throws Exception {
    bitset.writeDouble(0.0);
    assertEquals("Positive zero", bitset.readDouble());
  }

  @Test
  public void testWriteDoubleNegative() throws Exception {
    bitset.writeDouble(-1.5);
    assertEquals("-1.5", bitset.readDouble());
  }

  @Test
  public void testWriteDoubleOne() throws Exception {
    bitset.writeDouble(1.0);
    assertEquals("1.0", bitset.readDouble());
  }

  @Test
  public void testWriteDoubleLargeNumber() throws Exception {
    bitset.writeDouble(1.0E100);
    assertEquals("1.0E100", bitset.readDouble());
  }

  @Test
  public void testReadDoubleAfterReset() throws Exception {
    bitset.reset(false);
    assertEquals("Positive zero", bitset.readDouble());
  }

  // Issue #1822: writeHalfUnsigned must zero-extend values in 0..65535.
  @Test
  public void testWriteHalfUnsignedZeroExtendsLargeValue() throws Exception {
    // 40000 = 0x9C40. The result must be zero-extended (upper 48 bits all zero),
    // not sign-extended.
    bitset.writeHalfUnsigned(40000);
    String expected = "0000000000000000000000000000000000000000000000001001110001000000";
    assertEquals(expected, bitset.getBinString());
  }

  @Test
  public void testWriteHalfUnsignedMaxValue() throws Exception {
    bitset.writeHalfUnsigned(65535);
    String expected = "0000000000000000000000000000000000000000000000001111111111111111";
    assertEquals(expected, bitset.getBinString());
  }

  @Test(expected = IrregularWriteOperationException.class)
  public void testWriteHalfUnsignedRejectsAbove65535() throws Exception {
    bitset.writeHalfUnsigned(65536);
  }

  @Test(expected = IrregularWriteOperationException.class)
  public void testWriteHalfUnsignedRejectsNegative() throws Exception {
    bitset.writeHalfUnsigned(-1);
  }

  // Issue #1822 (minor): writeHalf(value, offset) had an off-by-one bound
  // (it accepted up to 65536, but the 16-bit maximum is 65535).
  @Test(expected = IrregularWriteOperationException.class)
  public void testWriteHalfWithOffsetRejectsAbove65535() throws Exception {
    bitset.writeHalf(65536, 0);
  }
}