package org.edumips64.core.fpu;

import org.edumips64.BaseTest;
import org.edumips64.core.FCSRRegister;
import org.junit.Before;
import org.junit.Test;

import org.edumips64.core.fpu.FPInvalidOperationException;
import org.edumips64.core.fpu.FPOverflowException;
import org.edumips64.core.fpu.FPUnderflowException;

import static org.junit.Assert.assertEquals;

/** Unit tests covering multiplication edge cases originally tested by fpu-mul.s */
public class DoubleMultiplicationTest extends BaseTest {
  private FPInstructionUtils fp;
  private FCSRRegister fcsr;

  @Before
  public void testSetup() {
    fcsr = new FCSRRegister();
    // Disable all FPU exceptions as in the original assembly file
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION, false);
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, false);
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.UNDERFLOW, false);
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.DIVIDE_BY_ZERO, false);
    fp = new FPInstructionUtils(fcsr);
  }

  private String d(String s) throws Exception {
    return fp.doubleToBin(s);
  }

  // ----------------------------------------------------------------------
  // Tests with FPU exceptions disabled
  // ----------------------------------------------------------------------

  @Test
  public void infinities() throws Exception {
    String posInf = d("POSITIVEINFINITY");
    String negInf = d("NEGATIVEINFINITY");

    assertEquals(
        posInf,
        fp.doubleMultiplication(posInf, posInf)); // ♾️ * ♾️ = ♾️
    assertEquals(
        negInf,
        fp.doubleMultiplication(posInf, negInf)); // ♾️ * -♾️ = -♾️
    assertEquals(
        posInf,
        fp.doubleMultiplication(negInf, negInf)); // -♾️ * -♾️ = ♾️
    assertEquals(
        negInf,
        fp.doubleMultiplication(negInf, posInf)); // -♾️ * ♾️ = -♾️
  }

  @Test
  public void infinityAndNumbers() throws Exception {
    String posInf = d("POSITIVEINFINITY");
    String negInf = d("NEGATIVEINFINITY");
    String posNum = d("1.5");
    String negNum = d("-1.5");

    assertEquals(
        posInf,
        fp.doubleMultiplication(posInf, posNum)); // ♾️ * 1.5 = ♾️
    assertEquals(
        negInf,
        fp.doubleMultiplication(posInf, negNum)); // ♾️ * -1.5 = -♾️
    assertEquals(
        negInf,
        fp.doubleMultiplication(negInf, posNum)); // -♾️ * 1.5 = -♾️
    assertEquals(
        posInf,
        fp.doubleMultiplication(negInf, negNum)); // -♾️ * -1.5 = ♾️
  }

  @Test
  public void zeros() throws Exception {
    String posZero = d("POSITIVEZERO");
    String negZero = d("NEGATIVEZERO");
    assertEquals(
        negZero,
        fp.doubleMultiplication(posZero, negZero)); // 0 * -0 = -0
  }

  @Test
  public void qnanPropagation() throws Exception {
    String qnan = d("QNAN");
    String num = d("1.5");
    assertEquals(
        qnan,
        fp.doubleMultiplication(qnan, num)); // qNaN * 1.5 = qNaN
    assertEquals(
        qnan,
        fp.doubleMultiplication(num, qnan)); // 1.5 * qNaN = qNaN
  }

  @Test
  public void snanPropagation() throws Exception {
    String snan = d("SNAN");
    String num = d("1.5");
    // SNAN becomes QNAN
    String qnan = d("QNAN");
    assertEquals(
        qnan,
        fp.doubleMultiplication(snan, num)); // sNaN * 1.5 = qNaN
    assertEquals(
        qnan,
        fp.doubleMultiplication(num, snan)); // 1.5 * sNaN = qNaN
  }

  @Test
  public void overflow() throws Exception {
    String bigPos = d("1.7E308");
    String bigNeg = d("-1.7E308");
    String posInf = d("POSITIVEINFINITY");
    String negInf = d("NEGATIVEINFINITY");
    assertEquals(
        posInf,
        fp.doubleMultiplication(bigPos, bigPos)); // bigPos * bigPos -> ♾️
    assertEquals(
        negInf,
        fp.doubleMultiplication(bigPos, bigNeg)); // bigPos * -bigPos -> -♾️
  }

  @Test
  public void underflow() throws Exception {
    String smallPos = d("9.0E-324");
    String verySmallPos = d("6.0E-324");
    String smallNeg = d("-9.0E-324");
    String posZero = d("POSITIVEZERO");
    String negZero = d("NEGATIVEZERO");
    assertEquals(
        posZero,
        fp.doubleMultiplication(smallPos, verySmallPos)); // tiny * tiny -> 0
    assertEquals(
        negZero,
        fp.doubleMultiplication(smallNeg, verySmallPos)); // -tiny * tiny -> -0
  }

  // ----------------------------------------------------------------------
  // Tests with FPU exceptions enabled
  // ----------------------------------------------------------------------

  @Test(expected = FPInvalidOperationException.class)
  public void qnanLeftException() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION, true);
    String qnan = d("QNAN");
    String num = d("1.5");
    fp.doubleMultiplication(qnan, num);
  }

  @Test(expected = FPInvalidOperationException.class)
  public void qnanRightException() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION, true);
    String qnan = d("QNAN");
    String num = d("1.5");
    fp.doubleMultiplication(num, qnan);
  }

  @Test(expected = FPInvalidOperationException.class)
  public void snanLeftException() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION, true);
    String snan = d("SNAN");
    String num = d("1.5");
    fp.doubleMultiplication(snan, num);
  }

  @Test(expected = FPInvalidOperationException.class)
  public void snanRightException() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION, true);
    String snan = d("SNAN");
    String num = d("1.5");
    fp.doubleMultiplication(num, snan);
  }

  @Test(expected = FPOverflowException.class)
  public void overflowPositivePositiveException() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, true);
    String bigPos = d("1.7E308");
    fp.doubleMultiplication(bigPos, bigPos);
  }

  @Test(expected = FPOverflowException.class)
  public void overflowPositiveNegativeException() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, true);
    String bigPos = d("1.7E308");
    String bigNeg = d("-1.7E308");
    fp.doubleMultiplication(bigPos, bigNeg);
  }

  @Test(expected = FPUnderflowException.class)
  public void underflowPositiveException() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.UNDERFLOW, true);
    String smallPos = d("9.0E-324");
    String verySmallPos = d("6.0E-324");
    fp.doubleMultiplication(smallPos, verySmallPos);
  }

  @Test(expected = FPUnderflowException.class)
  public void underflowNegativeException() throws Exception {
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.UNDERFLOW, true);
    String smallNeg = d("-9.0E-324");
    String verySmallPos = d("6.0E-324");
    fp.doubleMultiplication(smallNeg, verySmallPos);
  }
}
