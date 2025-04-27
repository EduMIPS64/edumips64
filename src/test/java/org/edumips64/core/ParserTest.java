package org.edumips64.core;

import org.edumips64.BaseParsingTest;
import org.edumips64.core.is.InstructionInterface;
import org.edumips64.core.is.ParsedInstructionMetadata;
import org.edumips64.core.parser.ParserMultiException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParserTest extends BaseParsingTest {
  @Test
  public void EscapeSequencesTest() throws Exception {
    // Java has no raw srings, so all special characters need to be escaped twice.
    String expected = "\\\"\t\n\0";
    parseData(".ascii \"\\\\\\\"\\t\\n\\0\"");
    MemoryElement el = memory.getCellByIndex(0);
    StringBuilder actual = new StringBuilder();
    for (int i = 0; i < 5; ++i) {
      actual.append((char) el.readByte(i));
    }
    assertEquals(actual.toString(), expected);
  }

  @Test(expected = ParserMultiException.class)
  public void InvalidEscapeSequencesTest() throws Exception {
    parseData(".ascii \"\\x\"");
  }

  @Test(expected = ParserMultiException.class)
  public void InvalidPlaceholder() throws Exception {
    parseData(".ascii \"%x\"");
  }

  @Test
  public void ParseHex() throws Exception {
    parseData(".word 0x10");
    MemoryElement el = memory.getCellByIndex(0);
    assertEquals(el.readByte(0), 16);
  }

  @Test
  public void Spaces() throws Exception {
    // The user should be able to reserve space in small and larger amounts, specifying the amount in hexadecimal
    // if they so desire.
    parseData(".space 0x10");
    parseData(".space 16");
    parseData(".space 8");
    parseData(".space 1");
  }

  @Test(expected = ParserMultiException.class)
  public void NoSpaces() throws Exception {
    parseData(".space");
  }

  @Test(expected = ParserMultiException.class)
  public void InvalidSpaces() throws Exception {
    parseData(".space yo");
  }

  @Test
  public void SpecialFPTest() throws Exception {
    parseDouble("POSITIVEINFINITY");  // +Inf
    parseDouble("NEGATIVEINFINITY");  // -Inf
    parseDouble("POSITIVEZERO");      // +0
    parseDouble("NEGATIVEZERO");      // -0
    parseDouble("SNAN");              // Signaling NaN
    parseDouble("QNAN");              // Quiet NaN
  }

  @Test
  public void FPNumbersTest() throws Exception {
    parseDouble("1.5");               // a positive number
    parseDouble("-1.5");              // a negative number
    parseDouble("1.7E308");           // a positive big number
    parseDouble("-1.7E308");          // a negative big number
    parseDouble("9.0E-324");          // a positive small number
    parseDouble("-9.0E-324");         // a negative small number
    parseDouble("-6.0E-324");         // a negative very small number
    parseDouble("6.0E-324");          // a positive very small number
  }

  @Test(expected = ParserMultiException.class)
  public void FPOverflowPositiveNumberTest() throws Exception {
    parser.getFCSR().setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, true);
    parseDouble("-1.8E308");
  }

  @Test(expected = ParserMultiException.class)
  public void FPOverflowNegativeNumberTest() throws Exception {
    parser.getFCSR().setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, true);
    parseDouble("4.95E324");
  }

  @Test
  public void FPOverflowNoThrowOnDisabledExceptionsTest() throws Exception {
    parser.getFCSR().setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, false);
    parseDouble("4.95E324");
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
    parseCode("daddi r1, r0, 32767");
    parseCode("daddi r1, r0, -32768");

    // 5-bit unsigned immediate
    parseCode("sll r1, r0, 31");
    parseCode("sll r1, r0, 0");

    // 3-bit unsigned immediate
    parseCode("movf.d f1, f2, 7");
    parseCode("movf.d f3, f4, 0");
  }

  @Test
  public void ImmediateCanStartWithHashTest() throws Exception {
    // 16-bit signed immediate.
    parseCode("daddi r1, r0, #10");

    // 5-bit unsigned immediate
    parseCode("sll r1, r0, #1");

    // 3-bit unsigned immediate
    parseCode("movf.d f1, f2, #7");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate16BitOverflow() throws Exception {
    parseCode("daddi r1, r0, 32768");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate16BitUnderflow() throws Exception {
    parseCode("daddi r1, r0, -32769");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate5BitOverflow() throws Exception {
    parseCode("sll r1, r0, 32");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate5BitUnderflow() throws Exception {
    parseCode("sll r1, r0, -1");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate3BitOverflow() throws Exception {
    parseCode("movf.d f1, f2, 8");
  }

  @Test(expected = ParserMultiException.class)
  public void Immediate3BitUnderflow() throws Exception {
    parseCode("movf.d f3, f4, -1");
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
    parseData(".byte -128");
    parseData(".byte 127");
    parseData(".word16 -32768");
    parseData(".word16 32767");
    parseData(".word32 -2147483648");
    parseData(".word32 2147483647");

    parseData(".word -9223372036854775808");
    parseData(".word 9223372036854775807");
    parseData(".word64 -9223372036854775808");
    parseData(".word64 9223372036854775807");
  }

  @Test(expected = ParserMultiException.class)
  public void OutOfBoundsByteTest() throws Exception {
    parseData(".byte 128");
  }

  @Test(expected = ParserMultiException.class)
  public void NegativeOutOfBoundsByteTest() throws Exception {
    parseData(".byte -129");
  }

  @Test(expected = ParserMultiException.class)
  public void OutOfBoundsHalfWordTest() throws Exception {
    parseData(".word16 32768");
  }

  @Test(expected = ParserMultiException.class)
  public void NegativeOutOfBoundsHalfWordTest() throws Exception {
    parseData(".word16 -32769");
  }

  @Test(expected = ParserMultiException.class)
  public void OutOfBoundsWordTest() throws Exception {
    parseData(".word32 2147483648");
  }

  @Test(expected = ParserMultiException.class)
  public void NegativeOutOfBoundsWordTest() throws Exception {
    parseData(".word32 -2147483649");
  }

  @Test(expected = ParserMultiException.class)
  public void OutOfBoundsDoubleWordTest() throws Exception {
    parseData(".word 9223372036854775808");
  }

  @Test(expected = ParserMultiException.class)
  public void NegativeOutOfBoundsDoubleWordTest() throws Exception {
    parseData(".word -9223372036854775809");
  }

  /** Tests for #175 */
  @Test()
  public void LargeValuesTest() throws Exception {
    parseCode("LW r1, 32768(r0)");
    parseCode("LW r1, -32767(r0)");
    parseCode("SW r1, 32768(r0)");
    parseCode("SW r1, -32767(r0)");
  }

  @Test(expected = ParserMultiException.class)
  public void OffsetTooLargePositiveTest() throws Exception {
    parseCode("LW r1, 32769(r0)");
  }

  @Test(expected = ParserMultiException.class)
  public void OffsetTooLargeNegativeTest() throws Exception {
    parseCode("LW r1, -32768(r0)");
  }

  /** Regression tests for #1048 */
  @Test(expected = ParserMultiException.class)
  public void MissingParmeterTest() throws Exception {
    // Fully missing parameter. Before the fix for #1048, this would throw StringIndexOutOfBoundsException.
    parseCode("LW r1");
  }

  /** Regression tests for #1317 */
  @Test(expected = ParserMultiException.class)
  public void unlimitedHexStringTest() throws Exception {
    parseData(".word 0xdeadbeeffffffffffffffffff");
  }

  @Test
  public void hexStringLimitData() throws Exception {
    parseData(".word 0xffffffffffffffff");
    parseData(".word32 0xffffffff");
    parseData(".word16 0xffff");
    parseData(".byte 0xff");
  }

  @Test
  public void immediate16BitsHexLimitTest() throws Exception {
    parseCode("daddi r1,r0,0xffff");
  }

}