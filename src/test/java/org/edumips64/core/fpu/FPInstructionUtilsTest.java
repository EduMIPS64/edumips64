package org.edumips64.core.fpu;

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
}
