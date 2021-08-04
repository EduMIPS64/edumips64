package org.edumips64.core;

import org.edumips64.BaseTest;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.is.InstructionInterface;
import org.edumips64.core.is.ParsedInstructionMetadata;
import org.edumips64.core.parser.Parser;
import org.edumips64.core.parser.ParserMultiException;
import org.edumips64.utils.io.LocalFileUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParserTest extends BaseTest {
  private Parser parser;
  private Memory memory;

  @Before
  public void setUp() throws Exception {
    memory = new Memory();
    CPU cpu = new CPU(memory, config, new BUBBLE());
    SymbolTable symTab = new SymbolTable(memory);
    IOManager iom = new IOManager(new LocalFileUtils(), memory);
    Dinero dinero = new Dinero();
    InstructionBuilder instructionBuilder = new InstructionBuilder(memory, iom, cpu, dinero, config);
    parser = new Parser(new LocalFileUtils(), symTab, memory, instructionBuilder);
  }
  /** Allows easier testing of .data section contents by adding the ".data" prefix and the "\n.code\nSYSCALL 0" suffix. */
  private void ParseData(String dataSectionContents) throws Exception {
    parser.doParsing(".data\n " + dataSectionContents + "\n.code\nSYSCALL 0");
  }
  /** Allows easier testing of .code section contents by adding the ".code" prefix and the "\nSYSCALL 0" suffix. */
  private void ParseCode(String codeSectionContents) throws Exception {
    parser.doParsing(".code\n " + codeSectionContents + "\nSYSCALL 0");
  }

  /** Parse a double value */
  private void ParseDouble(String doubleValue) throws Exception {
    ParseData(".double " + doubleValue);
  }

  @Test
  public void EscapeSequencesTest() throws Exception {
    // Java has no raw srings, so all special characters need to be escaped twice.
    String expected = "\\\"\t\n\0";
    ParseData(".ascii \"\\\\\\\"\\t\\n\\0\"");
    MemoryElement el = memory.getCellByIndex(0);
    StringBuilder actual = new StringBuilder();
    for (int i = 0; i < 5; ++i) {
      actual.append((char) el.readByte(i));
    }
    assertEquals(actual.toString(), expected);
  }

  @Test(expected = ParserMultiException.class)
  public void InvalidEscapeSequencesTest() throws Exception {
    ParseData(".ascii \"\\x\"");
  }

  @Test(expected = ParserMultiException.class)
  public void InvalidPlaceholder() throws Exception {
    ParseData(".ascii \"%x\"");
  }

  @Test
  public void ParseHex() throws Exception {
    ParseData(".word 0x10");
    MemoryElement el = memory.getCellByIndex(0);
    assertEquals(el.readByte(0), 16);
  }

  @Test
  public void Spaces() throws Exception {
    // The user should be able to reserve space in small and larger amounts, specifying the amount in hexadecimal
    // if they so desire.
    ParseData(".space 0x10");
    ParseData(".space 16");
    ParseData(".space 8");
    ParseData(".space 1");
  }

  @Test(expected = ParserMultiException.class)
  public void NoSpaces() throws Exception {
    ParseData(".space");
  }

  @Test(expected = ParserMultiException.class)
  public void InvalidSpaces() throws Exception {
    ParseData(".space yo");
  }

  @Test
  public void SpecialFPTest() throws Exception {
    ParseDouble("POSITIVEINFINITY");  // +Inf
    ParseDouble("NEGATIVEINFINITY");  // -Inf
    ParseDouble("POSITIVEZERO");      // +0
    ParseDouble("NEGATIVEZERO");      // -0
    ParseDouble("SNAN");              // Signaling NaN
    ParseDouble("QNAN");              // Quiet NaN
  }

  @Test
  public void FPNumbersTest() throws Exception {
    ParseDouble("1.5");               // a positive number
    ParseDouble("-1.5");              // a negative number
    ParseDouble("1.7E308");           // a positive big number
    ParseDouble("-1.7E308");          // a negative big number
    ParseDouble("9.0E-324");          // a positive small number
    ParseDouble("-9.0E-324");         // a negative small number
    ParseDouble("-6.0E-324");         // a negative very small number
    ParseDouble("6.0E-324");          // a positive very small number
  }

  @Test(expected = ParserMultiException.class)
  public void FPOverflowPositiveNumberTest() throws Exception {
    parser.getFCSR().setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, true);
    ParseDouble("-1.8E308");
  }

  @Test(expected = ParserMultiException.class)
  public void FPOverflowNegativeNumberTest() throws Exception {
    parser.getFCSR().setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, true);
    ParseDouble("4.95E324");
  }

  @Test
  public void FPOverflowNoThrowOnDisabledExceptionsTest() throws Exception {
    parser.getFCSR().setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, false);
    ParseDouble("4.95E324");
  }

  @Test
  public void ParserMetadataTest() throws Exception {
    parser.doParsing(".code\nnop\nnop\n\nsyscall 0");
    assertEquals(3, memory.getInstructionsNumber());

    // First instruction: address 0, code line 2 (line 1 has the .code directive).
    InstructionInterface first = memory.getInstruction(0);
    ParsedInstructionMetadata firstMeta = first.getParsingMetadata();
    assertEquals(0, firstMeta.address);
    assertEquals(2, firstMeta.sourceLine);

    // Second instruction: address 4, code line 3.
    InstructionInterface second = memory.getInstruction(4);
    ParsedInstructionMetadata secondMeta = second.getParsingMetadata();
    assertEquals(4, secondMeta.address);
    assertEquals(3, secondMeta.sourceLine);

    // Third instruction: address 8, code line 5 (there is an empty line before it).
    InstructionInterface third = memory.getInstruction(8);
    ParsedInstructionMetadata thirdMeta = third.getParsingMetadata();
    assertEquals(8, thirdMeta.address);
    assertEquals(5, thirdMeta.sourceLine);
  }

  /** Tests for parsing immediate values. */
  @Test
  public void ImmediateNotOutOfBoundsTest() throws Exception {
    // Test that all the values just before overflow are parsed correctly.

    // 16-bit signed immediate.
    ParseCode("daddi r1, r0, 32767");
    ParseCode("daddi r1, r0, -32768");

    // 5-bit unsigned immediate
    ParseCode("sll r1, r0, 31");
    ParseCode("sll r1, r0, 0");

    // 3-bit unsigned immediate
    ParseCode("movf.d f1, f2, 7");
    ParseCode("movf.d f3, f4, 0");
  }

  @Test
  public void ImmediateCanStartWithHashTest() throws Exception {
    // 16-bit signed immediate.
    ParseCode("daddi r1, r0, #10");

    // 5-bit unsigned immediate
    ParseCode("sll r1, r0, #1");

    // 3-bit unsigned immediate
    ParseCode("movf.d f1, f2, #7");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate16BitOverflow() throws Exception {
    ParseCode("daddi r1, r0, 32768");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate16BitUnderflow() throws Exception {
    ParseCode("daddi r1, r0, -32769");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate5BitOverflow() throws Exception {
    ParseCode("sll r1, r0, 32");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate5BitUnderflow() throws Exception {
    ParseCode("sll r1, r0, -1");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate3BitOverflow() throws Exception {
    ParseCode("movf.d f1, f2, 8");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate3BitUnderflow() throws Exception {
    ParseCode("movf.d f3, f4, -1");
  }

  /** Regression test for issue #95 */
  @Test
  public void CRLFParsingTest() throws Exception {
    parser.doParsing(".data\r\n.double 8\r\n.code\r\nSYSCALL 0\r\n");
  }

  /** Regression tests for issue #1 */
  @Test
  public void MemoryNotOutOfBoundsTest() throws Exception {
    // Test that all the values just before overflow are parsed correctly.
    ParseData(".byte -128");
    ParseData(".byte 127");
    ParseData(".word16 -32768");
    ParseData(".word16 32767");
    ParseData(".word32 -2147483648");
    ParseData(".word32 2147483647");

    ParseData(".word -9223372036854775808");
    ParseData(".word 9223372036854775807");
    ParseData(".word64 -9223372036854775808");
    ParseData(".word64 9223372036854775807");
  }

  @Test(expected = ParserMultiException.class)
  public void OutOfBoundsByteTest() throws Exception {
    ParseData(".byte 128");
  }

  @Test(expected = ParserMultiException.class)
  public void NegativeOutOfBoundsByteTest() throws Exception {
    ParseData(".byte -129");
  }

  @Test(expected = ParserMultiException.class)
  public void OutOfBoundsHalfWordTest() throws Exception {
    ParseData(".word16 32768");
  }

  @Test(expected = ParserMultiException.class)
  public void NegativeOutOfBoundsHalfWordTest() throws Exception {
    ParseData(".word16 -32769");
  }

  @Test(expected = ParserMultiException.class)
  public void OutOfBoundsWordTest() throws Exception {
    ParseData(".word32 2147483648");
  }

  @Test(expected = ParserMultiException.class)
  public void NegativeOutOfBoundsWordTest() throws Exception {
    ParseData(".word32 -2147483649");
  }

  @Test(expected = ParserMultiException.class)
  public void OutOfBoundsDoubleWordTest() throws Exception {
    ParseData(".word 9223372036854775808");
  }

  @Test(expected = ParserMultiException.class)
  public void NegativeOutOfBoundsDoubleWordTest() throws Exception {
    ParseData(".word -9223372036854775809");
  }

  /** Tests for #175 */
  @Test()
  public void LargeValuesTest() throws Exception {
    ParseCode("LW r1, 32768(r0)");
    ParseCode("LW r1, -32767(r0)");
    ParseCode("SW r1, 32768(r0)");
    ParseCode("SW r1, -32767(r0)");
  }

  @Test(expected = ParserMultiException.class)
  public void OffsetTooLargePositiveTest() throws Exception {
    ParseCode("LW r1, 32769(r0)");
  }

  @Test(expected = ParserMultiException.class)
  public void OffsetTooLargeNegativeTest() throws Exception {
    ParseCode("LW r1, -32768(r0)");
  }
}