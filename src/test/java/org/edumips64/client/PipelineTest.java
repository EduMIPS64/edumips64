/* PipelineTest.java
 *
 * Unit tests for the client Pipeline class.
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.edumips64.client;

import org.edumips64.BaseWithInstructionBuilderTest;
import org.edumips64.core.is.BUBBLE;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the client Pipeline class that represents the CPU pipeline stages
 * for JavaScript consumption.
 */
public class PipelineTest extends BaseWithInstructionBuilderTest {

  @Test
  public void testPipelineCreation() {
    Pipeline pipeline = new Pipeline();
    
    // Initially all fields should be null (uninitialized)
    assertNull("IF stage should be null initially", pipeline.IF);
    assertNull("ID stage should be null initially", pipeline.ID);
    assertNull("EX stage should be null initially", pipeline.EX);
    assertNull("MEM stage should be null initially", pipeline.MEM);
    assertNull("WB stage should be null initially", pipeline.WB);
  }

  @Test
  public void testPipelineFPUStagesCreation() {
    Pipeline pipeline = new Pipeline();
    
    // All FPU stages should be null initially
    assertNull("FPDivider should be null initially", pipeline.FPDivider);
    assertNull("FPAdder1 should be null initially", pipeline.FPAdder1);
    assertNull("FPAdder2 should be null initially", pipeline.FPAdder2);
    assertNull("FPAdder3 should be null initially", pipeline.FPAdder3);
    assertNull("FPAdder4 should be null initially", pipeline.FPAdder4);
    assertNull("FPMultiplier1 should be null initially", pipeline.FPMultiplier1);
    assertNull("FPMultiplier2 should be null initially", pipeline.FPMultiplier2);
    assertNull("FPMultiplier3 should be null initially", pipeline.FPMultiplier3);
    assertNull("FPMultiplier4 should be null initially", pipeline.FPMultiplier4);
    assertNull("FPMultiplier5 should be null initially", pipeline.FPMultiplier5);
    assertNull("FPMultiplier6 should be null initially", pipeline.FPMultiplier6);
    assertNull("FPMultiplier7 should be null initially", pipeline.FPMultiplier7);
  }

  @Test
  public void testPipelineWithInstructions() throws Exception {
    Pipeline pipeline = new Pipeline();
    
    // Create instructions for each stage
    Instruction ifInstr = Instruction.FromInstruction(instructionBuilder.buildInstruction("NOP"));
    Instruction idInstr = Instruction.FromInstruction(instructionBuilder.buildInstruction("DADD"));
    Instruction exInstr = Instruction.FromInstruction(instructionBuilder.buildInstruction("DSUB"));
    Instruction memInstr = Instruction.FromInstruction(instructionBuilder.buildInstruction("AND"));
    Instruction wbInstr = Instruction.FromInstruction(instructionBuilder.buildInstruction("OR"));
    
    // Assign to pipeline stages
    pipeline.IF = ifInstr;
    pipeline.ID = idInstr;
    pipeline.EX = exInstr;
    pipeline.MEM = memInstr;
    pipeline.WB = wbInstr;
    
    // Verify assignments
    assertEquals("IF should contain NOP", "NOP", pipeline.IF.Name);
    assertEquals("ID should contain DADD", "DADD", pipeline.ID.Name);
    assertEquals("EX should contain DSUB", "DSUB", pipeline.EX.Name);
    assertEquals("MEM should contain AND", "AND", pipeline.MEM.Name);
    assertEquals("WB should contain OR", "OR", pipeline.WB.Name);
  }

  @Test
  public void testPipelineWithBubble() {
    Pipeline pipeline = new Pipeline();
    
    // Assign a BUBBLE instruction
    BUBBLE bubble = new BUBBLE();
    Instruction bubbleInstr = Instruction.FromInstruction(bubble);
    
    pipeline.IF = bubbleInstr;
    
    assertNotNull("BUBBLE instruction should be assigned", pipeline.IF);
    assertEquals("BUBBLE name should be a space", " ", pipeline.IF.Name);
  }

  @Test
  public void testPipelineWithFPUInstructions() throws Exception {
    Pipeline pipeline = new Pipeline();
    
    // Create instructions for FPU stages
    Instruction fpInstr = Instruction.FromInstruction(instructionBuilder.buildInstruction("NOP"));
    
    // Assign to FPU pipeline stages
    pipeline.FPDivider = fpInstr;
    pipeline.FPAdder1 = fpInstr;
    pipeline.FPMultiplier1 = fpInstr;
    
    assertNotNull("FPDivider should be assigned", pipeline.FPDivider);
    assertNotNull("FPAdder1 should be assigned", pipeline.FPAdder1);
    assertNotNull("FPMultiplier1 should be assigned", pipeline.FPMultiplier1);
  }

  @Test
  public void testPipelineMixedNullAndInstructions() throws Exception {
    Pipeline pipeline = new Pipeline();
    
    // Assign some stages, leave others null
    Instruction instr = Instruction.FromInstruction(instructionBuilder.buildInstruction("NOP"));
    
    pipeline.IF = instr;
    pipeline.EX = instr;
    
    assertNotNull("IF should not be null", pipeline.IF);
    assertNull("ID should still be null", pipeline.ID);
    assertNotNull("EX should not be null", pipeline.EX);
    assertNull("MEM should still be null", pipeline.MEM);
    assertNull("WB should still be null", pipeline.WB);
  }

  @Test
  public void testPipelineAllFPUAdderStages() throws Exception {
    Pipeline pipeline = new Pipeline();
    
    Instruction instr1 = Instruction.FromInstruction(instructionBuilder.buildInstruction("NOP"));
    Instruction instr2 = Instruction.FromInstruction(instructionBuilder.buildInstruction("DADD"));
    Instruction instr3 = Instruction.FromInstruction(instructionBuilder.buildInstruction("DSUB"));
    Instruction instr4 = Instruction.FromInstruction(instructionBuilder.buildInstruction("AND"));
    
    pipeline.FPAdder1 = instr1;
    pipeline.FPAdder2 = instr2;
    pipeline.FPAdder3 = instr3;
    pipeline.FPAdder4 = instr4;
    
    assertEquals("FPAdder1 should have NOP", "NOP", pipeline.FPAdder1.Name);
    assertEquals("FPAdder2 should have DADD", "DADD", pipeline.FPAdder2.Name);
    assertEquals("FPAdder3 should have DSUB", "DSUB", pipeline.FPAdder3.Name);
    assertEquals("FPAdder4 should have AND", "AND", pipeline.FPAdder4.Name);
  }

  @Test
  public void testPipelineAllFPUMultiplierStages() throws Exception {
    Pipeline pipeline = new Pipeline();
    
    Instruction instr = Instruction.FromInstruction(instructionBuilder.buildInstruction("NOP"));
    
    pipeline.FPMultiplier1 = instr;
    pipeline.FPMultiplier2 = instr;
    pipeline.FPMultiplier3 = instr;
    pipeline.FPMultiplier4 = instr;
    pipeline.FPMultiplier5 = instr;
    pipeline.FPMultiplier6 = instr;
    pipeline.FPMultiplier7 = instr;
    
    assertNotNull("FPMultiplier1 should be assigned", pipeline.FPMultiplier1);
    assertNotNull("FPMultiplier2 should be assigned", pipeline.FPMultiplier2);
    assertNotNull("FPMultiplier3 should be assigned", pipeline.FPMultiplier3);
    assertNotNull("FPMultiplier4 should be assigned", pipeline.FPMultiplier4);
    assertNotNull("FPMultiplier5 should be assigned", pipeline.FPMultiplier5);
    assertNotNull("FPMultiplier6 should be assigned", pipeline.FPMultiplier6);
    assertNotNull("FPMultiplier7 should be assigned", pipeline.FPMultiplier7);
  }
}
