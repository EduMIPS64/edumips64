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
}
