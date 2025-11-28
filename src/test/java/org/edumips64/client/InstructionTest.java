/* InstructionTest.java
 *
 * Unit tests for the client Instruction class.
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
import org.edumips64.core.is.InstructionInterface;
import org.edumips64.core.is.ParsedInstructionMetadata;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the client Instruction class that wraps InstructionInterface for JavaScript.
 */
public class InstructionTest extends BaseWithInstructionBuilderTest {

  @Test
  public void testFromInstructionWithNull() {
    Instruction result = Instruction.FromInstruction(null);
    assertNull("FromInstruction with null should return null", result);
  }

  @Test
  public void testFromInstructionWithBubble() {
    BUBBLE bubble = new BUBBLE();
    Instruction result = Instruction.FromInstruction(bubble);
    
    assertNotNull("FromInstruction should return non-null for BUBBLE", result);
    assertEquals("BUBBLE name should be a space", " ", result.Name);
    assertEquals("BUBBLE fullname should be a space", " ", result.Code);
    assertNull("BUBBLE should have null comment", result.Comment);
    assertNotNull("Binary representation should not be null", result.BinaryRepresentation);
    assertEquals("OpCode should be first 6 bits of binary representation", 
                 result.BinaryRepresentation.substring(0, 6), result.OpCode);
  }

  @Test
  public void testFromInstructionWithRealInstruction() throws Exception {
    InstructionInterface nop = instructionBuilder.buildInstruction("NOP");
    
    Instruction result = Instruction.FromInstruction(nop);
    
    assertNotNull("FromInstruction should return non-null for NOP", result);
    assertEquals("NOP", result.Name);
    assertNotNull("Binary representation should not be null", result.BinaryRepresentation);
    assertEquals(32, result.BinaryRepresentation.length());
    assertEquals("OpCode should be first 6 bits", 
                 result.BinaryRepresentation.substring(0, 6), result.OpCode);
  }

  @Test
  public void testFromInstructionWithMetadata() throws Exception {
    InstructionInterface syscall = instructionBuilder.buildInstruction("SYSCALL");
    // Set parsing metadata - need to cast to Instruction to access setParsingMetadata
    org.edumips64.core.is.Instruction instr = (org.edumips64.core.is.Instruction) syscall;
    instr.setParsingMetadata(new ParsedInstructionMetadata(10, 0x100));
    
    Instruction result = Instruction.FromInstruction(syscall);
    
    assertNotNull("FromInstruction should return non-null", result);
    assertEquals("Address should match metadata", 0x100, result.Address);
    assertEquals("Line should match metadata", 10, result.Line);
  }

  @Test
  public void testFromInstructionWithoutMetadata() throws Exception {
    InstructionInterface nop = instructionBuilder.buildInstruction("NOP");
    // NOP created without parsing has null metadata
    
    Instruction result = Instruction.FromInstruction(nop);
    
    assertNotNull("FromInstruction should return non-null", result);
    // When metadata is null, Address and Line should remain at default values (0)
    assertEquals("Address should be 0 when no metadata", 0, result.Address);
    assertEquals("Line should be 0 when no metadata", 0, result.Line);
  }

  @Test
  public void testFromInstructionSerialNumber() throws Exception {
    InstructionInterface instr1 = instructionBuilder.buildInstruction("NOP");
    InstructionInterface instr2 = instructionBuilder.buildInstruction("NOP");
    
    Instruction result1 = Instruction.FromInstruction(instr1);
    Instruction result2 = Instruction.FromInstruction(instr2);
    
    // Serial numbers should be different for different instruction instances
    assertNotEquals("Different instructions should have different serial numbers",
                    result1.SerialNumber, result2.SerialNumber);
  }

  @Test
  public void testFromInstructionWithComment() throws Exception {
    InstructionInterface syscall = instructionBuilder.buildInstruction("SYSCALL");
    // Simulate parsing by setting a comment
    org.edumips64.core.is.Instruction instr = (org.edumips64.core.is.Instruction) syscall;
    instr.setComment("This is a test comment");
    
    Instruction result = Instruction.FromInstruction(syscall);
    
    assertNotNull("FromInstruction should return non-null", result);
    assertEquals("Comment should be preserved", "This is a test comment", result.Comment);
  }

  @Test
  public void testFromInstructionWithLabel() throws Exception {
    InstructionInterface syscall = instructionBuilder.buildInstruction("SYSCALL");
    syscall.setLabel("myLabel");
    
    Instruction result = Instruction.FromInstruction(syscall);
    
    assertNotNull("FromInstruction should return non-null", result);
    // Note: The client Instruction class doesn't have a Label field,
    // but the Name field should still contain the instruction name
    assertEquals("SYSCALL", result.Name);
  }

  @Test
  public void testFromInstructionBinaryRepresentation() throws Exception {
    InstructionInterface dadd = instructionBuilder.buildInstruction("DADD");
    
    Instruction result = Instruction.FromInstruction(dadd);
    
    assertNotNull("FromInstruction should return non-null", result);
    assertNotNull("Binary representation should not be null", result.BinaryRepresentation);
    assertEquals("Binary representation should be 32 bits", 32, result.BinaryRepresentation.length());
    // Verify OpCode is the first 6 characters
    assertTrue("OpCode should only contain 0s and 1s", 
               result.OpCode.matches("[01]{6}"));
  }

  @Test
  public void testFromInstructionWithVariousTypes() throws Exception {
    // Test different instruction types
    String[] instructionNames = {"DADD", "DSUB", "AND", "OR", "XOR", "SLL", "SRL", "NOP"};
    
    for (String name : instructionNames) {
      InstructionInterface instr = instructionBuilder.buildInstruction(name);
      Instruction result = Instruction.FromInstruction(instr);
      
      assertNotNull("FromInstruction should return non-null for " + name, result);
      assertEquals("Name should match for " + name, name, result.Name);
      assertNotNull("Binary representation should not be null for " + name, result.BinaryRepresentation);
      assertEquals("Binary representation should be 32 bits for " + name, 
                   32, result.BinaryRepresentation.length());
    }
  }

  @Test
  public void testFromInstructionOpCodeExtraction() throws Exception {
    InstructionInterface instr = instructionBuilder.buildInstruction("DADD");
    
    Instruction result = Instruction.FromInstruction(instr);
    
    // OpCode should be exactly 6 bits
    assertEquals("OpCode should be 6 bits", 6, result.OpCode.length());
    
    // OpCode should match the first 6 bits of the binary representation
    String expectedOpCode = result.BinaryRepresentation.substring(0, 6);
    assertEquals("OpCode should match first 6 bits of BinaryRepresentation", 
                 expectedOpCode, result.OpCode);
  }

  @Test
  public void testFromInstructionCodeVsName() throws Exception {
    // Code (fullname) should be different from Name for some instructions
    InstructionInterface dadd = instructionBuilder.buildInstruction("DADD");
    org.edumips64.core.is.Instruction instr = (org.edumips64.core.is.Instruction) dadd;
    instr.setFullName("DADD R1, R2, R3");
    
    Instruction result = Instruction.FromInstruction(dadd);
    
    assertEquals("Name should be the instruction name", "DADD", result.Name);
    assertEquals("Code should be the full instruction", "DADD R1, R2, R3", result.Code);
  }

  @Test
  public void testFromInstructionWithZeroAddress() throws Exception {
    InstructionInterface instr = instructionBuilder.buildInstruction("NOP");
    org.edumips64.core.is.Instruction coreInstr = (org.edumips64.core.is.Instruction) instr;
    coreInstr.setParsingMetadata(new ParsedInstructionMetadata(1, 0));
    
    Instruction result = Instruction.FromInstruction(instr);
    
    assertEquals("Address should be 0", 0, result.Address);
    assertEquals("Line should be 1", 1, result.Line);
  }

  @Test
  public void testFromInstructionWithLargeAddress() throws Exception {
    InstructionInterface instr = instructionBuilder.buildInstruction("NOP");
    org.edumips64.core.is.Instruction coreInstr = (org.edumips64.core.is.Instruction) instr;
    // Test with a large address
    coreInstr.setParsingMetadata(new ParsedInstructionMetadata(1000, 0xFFFC));
    
    Instruction result = Instruction.FromInstruction(instr);
    
    assertEquals("Large address should be preserved", 0xFFFC, result.Address);
    assertEquals("Large line number should be preserved", 1000, result.Line);
  }

  @Test
  public void testFromInstructionSerialNumberIncreases() throws Exception {
    // Create multiple instructions and verify serial numbers increase
    InstructionInterface instr1 = instructionBuilder.buildInstruction("NOP");
    InstructionInterface instr2 = instructionBuilder.buildInstruction("NOP");
    InstructionInterface instr3 = instructionBuilder.buildInstruction("NOP");
    
    Instruction result1 = Instruction.FromInstruction(instr1);
    Instruction result2 = Instruction.FromInstruction(instr2);
    Instruction result3 = Instruction.FromInstruction(instr3);
    
    // Serial numbers should be monotonically increasing
    assertTrue("Serial numbers should increase", result1.SerialNumber < result2.SerialNumber);
    assertTrue("Serial numbers should increase", result2.SerialNumber < result3.SerialNumber);
  }
}
