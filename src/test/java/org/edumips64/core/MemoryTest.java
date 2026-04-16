package org.edumips64.core;

import org.edumips64.BaseWithInstructionBuilderTest;
import org.edumips64.core.is.BUBBLE;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MemoryTest extends BaseWithInstructionBuilderTest {
  /* Regression test for Issue #84 */
  @Test
  public void testInstructionCount() throws Exception {
    // Add 5 BUBBLE instructions.
    for (int i = 0; i < 5; ++i) {
      memory.addInstruction(new BUBBLE(), i*4);
    }
    // Add 2 non-BUBBLE instructions.
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 24);
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 28);

    // Only non-bubble instructions should be counted.
    assertEquals(2, memory.getInstructionsNumber());
  }

  @Test
  public void testInstructionCountBetweenResets() throws Exception {
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 4);
    assertEquals(2, memory.getInstructionsNumber());

    memory.reset();
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    assertEquals(1, memory.getInstructionsNumber());
  }

  @Test
  public void testMultipleInsertsSameAddress() throws Exception {
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    memory.addInstruction(instructionBuilder.buildInstruction("SYSCALL"), 0);
    assertEquals(1, memory.getInstructionsNumber());
  }
}