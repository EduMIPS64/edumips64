package org.edumips64.core.fpu;

import org.edumips64.core.CPU;
import org.edumips64.core.FCSRRegister;
import org.edumips64.core.fpu.FPInstructionUtils;
import org.edumips64.core.fpu.FPUnderflowException;
import org.edumips64.core.fpu.FPOverflowException;
import org.edumips64.utils.IrregularStringOfBitsException;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.ConfigStore;

import org.junit.Test;
import org.junit.Before;

public class FPInstructionUtilsTest {
  private ConfigStore config = ConfigManager.getTmpConfig();
  private FPInstructionUtils fp;
  private FCSRRegister fcsr;
  
  @Before
  public void testSetup() {
    fcsr = new FCSRRegister();
    fp = new FPInstructionUtils(fcsr);
    ConfigManager.setConfig(config);
  }
  
  @Test(expected = FPOverflowException.class)
  public void OverflowBigNegativeTest() throws Exception {
    fcsr.setFPExceptions(CPU.FPExceptions.OVERFLOW, true);
    fp.doubleToBin("-1.8E308");
  }

  @Test(expected = FPOverflowException.class)
  public void OverflowBigPositiveTest() throws Exception {
    fcsr.setFPExceptions(CPU.FPExceptions.OVERFLOW, true);
    fp.doubleToBin("4.95E324");
  }

  @Test(expected = FPUnderflowException.class)
  public void UnderflowTest() throws Exception {
    fcsr.setFPExceptions(CPU.FPExceptions.UNDERFLOW, true);
    fp.doubleToBin("4.9E-325");
  }
}
