package org.edumips64.core.fpu;

import static org.junit.Assert.assertEquals;

import org.edumips64.BaseTest;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for RegisterFP class.
 */
public class RegisterFPTest extends BaseTest {
  private RegisterFP registerFP;

  @Before
  public void setUp() {
    registerFP = new RegisterFP("F0");
  }

  @Test
  public void testGetName() {
    assertEquals("F0", registerFP.getName());
  }

  @Test
  public void testWriteAndReadDouble() throws Exception {
    registerFP.writeDouble(3.14);
    assertEquals("3.14", registerFP.getFPDoubleValueAsString());
  }

  @Test
  public void testWriteAndReadDoubleZero() throws Exception {
    registerFP.writeDouble(0.0);
    assertEquals("Positive zero", registerFP.getFPDoubleValueAsString());
  }

  @Test
  public void testWriteAndReadDoubleNegative() throws Exception {
    registerFP.writeDouble(-42.5);
    assertEquals("-42.5", registerFP.getFPDoubleValueAsString());
  }

  @Test
  public void testReset() throws Exception {
    registerFP.writeDouble(123.456);
    registerFP.incrWriteSemaphore();
    registerFP.incrWAWSemaphore();
    
    registerFP.reset();
    
    assertEquals(0, registerFP.getWriteSemaphore());
    assertEquals(0, registerFP.getWAWSemaphore());
    assertEquals("Positive zero", registerFP.getFPDoubleValueAsString());
  }

  @Test
  public void testExtendsRegister() {
    // RegisterFP should inherit all Register functionality
    assertEquals(0, registerFP.getWriteSemaphore());
    registerFP.incrWriteSemaphore();
    assertEquals(1, registerFP.getWriteSemaphore());
    
    assertEquals(0, registerFP.getWAWSemaphore());
    registerFP.incrWAWSemaphore();
    assertEquals(1, registerFP.getWAWSemaphore());
  }
}
