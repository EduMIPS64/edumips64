package org.edumips64.core;

import static org.junit.Assert.assertEquals;

import org.edumips64.BaseTest;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Register class.
 */
public class RegisterTest extends BaseTest {
  private Register register;

  @Before
  public void setUp() {
    register = new Register("R0");
  }

  @Test
  public void testGetName() {
    assertEquals("R0", register.getName());
  }

  @Test
  public void testReset() throws Exception {
    register.writeDouble(123.456);
    register.incrWriteSemaphore();
    register.incrWAWSemaphore();
    
    register.reset();
    
    assertEquals(0, register.getWriteSemaphore());
    assertEquals(0, register.getWAWSemaphore());
  }

  @Test
  public void testWriteSemaphore() {
    assertEquals(0, register.getWriteSemaphore());
    
    register.incrWriteSemaphore();
    assertEquals(1, register.getWriteSemaphore());
    
    register.incrWriteSemaphore();
    assertEquals(2, register.getWriteSemaphore());
    
    register.decrWriteSemaphore();
    assertEquals(1, register.getWriteSemaphore());
    
    register.decrWriteSemaphore();
    assertEquals(0, register.getWriteSemaphore());
  }

  @Test
  public void testWAWSemaphore() {
    assertEquals(0, register.getWAWSemaphore());
    
    register.incrWAWSemaphore();
    assertEquals(1, register.getWAWSemaphore());
    
    register.incrWAWSemaphore();
    assertEquals(2, register.getWAWSemaphore());
    
    register.decrWAWSemaphore();
    assertEquals(1, register.getWAWSemaphore());
    
    register.decrWAWSemaphore();
    assertEquals(0, register.getWAWSemaphore());
  }

  @Test(expected = RuntimeException.class)
  public void testDecrWAWSemaphoreBelowZero() {
    register.decrWAWSemaphore();
  }

  @Test(expected = RuntimeException.class)
  public void testDecrWriteSemaphoreBelowZero() {
    register.decrWriteSemaphore();
  }

  @Test
  public void testToString() throws Exception {
    register.reset();
    assertEquals("0000000000000000", register.toString());
  }

  @Test
  public void testGetHexString() throws Exception {
    register.reset();
    assertEquals("0000000000000000", register.getHexString());
  }

  @Test
  public void testGetValue() {
    register.reset();
    assertEquals(0L, register.getValue());
  }

  /**
   * Regression test for the MIPS ABI register alias table.
   *
   * Prior to this test, R2 was incorrectly aliased to "v1" (a duplicate of R3),
   * instead of "v0" as defined by the MIPS O32/N32/N64 ABIs. The Web UI
   * (and Swing UI) display these aliases next to each register, so the bug
   * was user-visible.
   *
   * The expected mapping mirrors the MIPS register convention used elsewhere
   * in the codebase (see GUIRegisters.java and Parser.java).
   */
  @Test
  public void testRegisterAliases() {
    String[] expectedAliases = {
        "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
        "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
        "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
        "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"
    };
    for (int i = 0; i < expectedAliases.length; i++) {
      Register r = new Register("R" + i);
      assertEquals("Wrong alias for R" + i, expectedAliases[i], r.getAlias());
    }
  }
}
