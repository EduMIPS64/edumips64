package org.edumips64.core.fpu;

import static org.junit.Assert.assertEquals;

import org.edumips64.BaseTest;
import org.edumips64.core.FCSRRegister;

import org.edumips64.core.IrregularStringOfBitsException;
import org.junit.Test;
import org.junit.Before;

public class FPInstructionUtilsTest extends BaseTest {
  private FPInstructionUtils fp;
  private FCSRRegister fcsr;
  
  @Before
  public void testSetup() {
    fcsr = new FCSRRegister();
    fp = new FPInstructionUtils(fcsr);
  }
  
  @Test(expected = FPOverflowException.class)
  public void OverflowBigNegativeTest() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, true);
    fp.doubleToBin("-1.8E308");
  }

  @Test(expected = FPOverflowException.class)
  public void OverflowBigPositiveTest() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, true);
    fp.doubleToBin("4.95E324");
  }

  @Test(expected = FPUnderflowException.class)
  public void UnderflowTest() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.UNDERFLOW, true);
    fp.doubleToBin("4.9E-325");
  }

  /** Regression tests for issue #77 */
  @Test(expected = IrregularStringOfBitsException.class)
  public void TryToParseAWord() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION, true);
    fp.doubleToBin("wrong");
  }

  @Test(expected = IrregularStringOfBitsException.class)
  public void TryToParseAWrongNumber() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION, true);
    fp.doubleToBin("--10");
  }

  @Test
  public void testBinToDoubleZero() throws Exception {
    String zeroBits = "0000000000000000000000000000000000000000000000000000000000000000";
    assertEquals("Positive zero", FPInstructionUtils.binToDouble(zeroBits));
  }

  @Test
  public void testBinToDoubleOne() throws Exception {
    // IEEE 754 representation of 1.0
    String oneBits = "0011111111110000000000000000000000000000000000000000000000000000";
    assertEquals("1.0", FPInstructionUtils.binToDouble(oneBits));
  }
 
  @Test
  public void testBinToDoubleNegative() throws Exception {
    // IEEE 754 representation of -1.0
    String negOneBits = "1011111111110000000000000000000000000000000000000000000000000000";
    assertEquals("-1.0", FPInstructionUtils.binToDouble(negOneBits));
  }
  @Test
  public void testBinToDoubleSmallest() throws Exception {
    // Smallest positive normalized double
    String smallestBits = "0000000000010000000000000000000000000000000000000000000000000000";
    assertEquals("2.2250738585072014E-308", FPInstructionUtils.binToDouble(smallestBits));
  }

  @Test
  public void testBinToDoubleLargest() throws Exception {
    // Largest finite double
    String largestBits = "0111111111101111111111111111111111111111111111111111111111111111";
    assertEquals("1.7976931348623157E308", FPInstructionUtils.binToDouble(largestBits));
  }

  @Test
  public void testBinToDoubleInvalidLength() throws Exception {
    assertEquals("", FPInstructionUtils.binToDouble("101")); // Too short
  }

  @Test
  public void testBinToDoubleInvalidChars() throws Exception {
    String s = FPInstructionUtils.binToDouble("001234567890000000000000000000000000000000000000000000000000000000");
    assertEquals("", s);
  }

  @Test
  public void testBinToDoubleSpecialValues() throws Exception {
    // Positive infinity
    String posInfBits = "0111111111110000000000000000000000000000000000000000000000000000";
    assertEquals("Positive infinity", FPInstructionUtils.binToDouble(posInfBits));
    
    // Negative infinity
    String negInfBits = "1111111111110000000000000000000000000000000000000000000000000000";
    assertEquals("Negative infinity", FPInstructionUtils.binToDouble(negInfBits));
  }

  @Test
  public void testBinToDoubleNaN() throws Exception {
    // NaN representation
    String nanBits = "0111111111111000000000000000000000000000000000000000000000000000";
    assertEquals("Signaling NaN", FPInstructionUtils.binToDouble(nanBits));
  }
}
