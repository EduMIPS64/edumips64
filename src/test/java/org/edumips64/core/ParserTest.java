package org.edumips64.core;

import org.edumips64.BaseTest;
import org.edumips64.core.is.InstructionBuilder;
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
    CPU cpu = new CPU(memory, config);
    SymbolTable symTab = new SymbolTable(memory);
    IOManager iom = new IOManager(new LocalFileUtils(), memory);
    Dinero dinero = new Dinero(memory);
    InstructionBuilder instructionBuilder = new InstructionBuilder(memory, iom, cpu, dinero, config);
    parser = new Parser(new LocalFileUtils(), symTab, memory, instructionBuilder);
  }
  /** Allows easier testing of .data section contents by adding the ".data" prefix and the "\n.code\nSYSCALL 0" suffix. */
  private void ParseData(String dataSectionContents) throws Exception {
    parser.doParsing(".data\n " + dataSectionContents + "\n.code\nSYSCALL 0");
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
    parser.getFCSR().setFPExceptions(CPU.FPExceptions.OVERFLOW, true);
    ParseDouble("-1.8E308");
  }

  @Test(expected = ParserMultiException.class)
  public void FPOverflowNegativeNumberTest() throws Exception {
    parser.getFCSR().setFPExceptions(CPU.FPExceptions.OVERFLOW, true);
    ParseDouble("4.95E324");
  }

  @Test
  public void FPOverflowNoThrowOnDisabledExceptionsTest() throws Exception {
    parser.getFCSR().setFPExceptions(CPU.FPExceptions.OVERFLOW, false);
    ParseDouble("4.95E324");
  }

  /** Regression test for issue #95 */
  @Test
  public void CRLFParsingTest() throws Exception {
    parser.doParsing(".data\r\n.double 8\r\n.code\r\nSYSCALL 0\r\n");
  }

  /** Regression tests for issue #1 */
  @Test
  public void NotOutOfBoundsTest() throws Exception {
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
}