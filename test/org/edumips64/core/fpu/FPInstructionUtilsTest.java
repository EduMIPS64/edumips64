package org.edumips64.core.fpu;

import org.edumips64.utils.IrregularStringOfBitsException;
import org.junit.Test;

public class FPInstructionUtilsTest {
  @Test(expected = FPOverflowException.class)
  public void OverflowBigNegativeTest() throws Exception {
    FPInstructionUtils.doubleToBin("-1.8E308");
  }

  @Test(expected = FPOverflowException.class)
  public void OverflowBigPositiveTest() throws Exception {
    FPInstructionUtils.doubleToBin("4.95E324");
  }

  @Test(expected = FPUnderflowException.class)
  public void UnderflowTest() throws Exception {
    FPInstructionUtils.doubleToBin("4.9E-325");
  }
}