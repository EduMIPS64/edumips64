package org.edumips64.core;

import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.ConfigStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MemoryTest {
  private ConfigStore config = ConfigManager.getTmpConfig();
  private Memory m = Memory.getInstance();

  @Before
  public void setUp() throws Exception {
    ConfigManager.setConfig(config);
  }

  /* Regression test for Issue #84 */
  @Test
  public void testInstructionCount() throws Exception {
    // Add 5 BUBBLE instructions.
    for (int i = 0; i < 5; ++i) {
      m.addInstruction(InstructionBuilder.buildInstruction("BUBBLE"), i*4);
    }
    // Add 2 non-BUBBLE instructions.
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 24);
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 28);

    // Only non-bubble instructions should be counted.
    assertEquals(2, m.getInstructionsNumber());
  }

  @Test
  public void testInstructionCountBetweenResets() throws Exception {
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 4);
    assertEquals(2, m.getInstructionsNumber());

    m.reset();
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 0);
    assertEquals(1, m.getInstructionsNumber());
  }

  @Test
  public void testMultipleInsertsSameAddress() throws Exception {
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 0);
    m.addInstruction(InstructionBuilder.buildInstruction("SYSCALL"), 0);
    assertEquals(1, m.getInstructionsNumber());
  }
}