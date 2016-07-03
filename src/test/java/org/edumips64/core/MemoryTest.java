package org.edumips64.core;

import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.io.LocalFileUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MemoryTest {
  private ConfigStore config = ConfigManager.getTmpConfig();
  private Memory m;
  private InstructionBuilder instructionBuilder;
  private IOManager iom;
  private Dinero dinero;
  private CPU cpu;

  @Before
  public void setUp() throws Exception {
    m = new Memory();
    cpu = new CPU(m, config);
    ConfigManager.setConfig(config);
    iom = new IOManager(new LocalFileUtils(), m);
    dinero = new Dinero(m);
    instructionBuilder = new InstructionBuilder(m, iom, cpu, dinero);
  }

  /* Regression test for Issue #84 */
  @Test
  public void testInstructionCount() throws Exception {
    // Add 5 BUBBLE instructions.
    for (int i = 0; i < 5; ++i) {
      m.addInstruction(instructionBuilder.buildInstruction("BUBBLE"), i*4);
    }
    // Add 2 non-BUBBLE instructions.
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 24);
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 28);

    // Only non-bubble instructions should be counted.
    assertEquals(2, m.getInstructionsNumber());
  }

  @Test
  public void testInstructionCountBetweenResets() throws Exception {
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 4);
    assertEquals(2, m.getInstructionsNumber());

    m.reset();
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    assertEquals(1, m.getInstructionsNumber());
  }

  @Test
  public void testMultipleInsertsSameAddress() throws Exception {
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    assertEquals(1, m.getInstructionsNumber());
  }
}