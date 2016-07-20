package org.edumips64.core;

import org.edumips64.BaseTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class CPUTest extends BaseTest {
  private CPU cpu;

  @Before
  public void setUp() throws Exception {
    Memory m = new Memory();
    cpu = new CPU(m, config);
  }

  @Test(expected = StoppedCPUException.class)
  public void testRunningWhenStoppedThrowsException() throws Exception {
    cpu.setStatus(CPU.CPUStatus.HALTED);
    cpu.step();
  }

  /** This test is not very useful, but it does improve test coverage. */
  @Test
  public void testToString() throws Exception {
    String cpuRepr = cpu.toString();
    assertThat(cpuRepr, containsString("Register 0"));
    assertThat(cpuRepr, containsString("FP Register 0"));
    assertThat(cpuRepr, containsString("ID"));
  }
}