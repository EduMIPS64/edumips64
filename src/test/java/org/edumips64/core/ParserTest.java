package org.edumips64.core;

import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.utils.io.LocalFileUtils;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.ConfigStore;
import org.junit.Before;
import org.junit.Test;

public class ParserTest {
  private Parser parser;
  private Memory memory;
  private SymbolTable symTab;
  private IOManager iom;
  private InstructionBuilder instructionBuilder;
  private Dinero dinero;
  private ConfigStore config = ConfigManager.getTmpConfig();
  private CPU cpu;

  @Before
  public void setUp() throws Exception {
     ConfigManager.setConfig(config);
     memory = new Memory();
     cpu = new CPU(memory);
     symTab = new SymbolTable(memory);
     iom = new IOManager(new LocalFileUtils(), memory);
     dinero = new Dinero(memory);
     instructionBuilder = new InstructionBuilder(memory, iom, cpu, dinero);
     parser = new Parser(new LocalFileUtils(), symTab, memory, instructionBuilder);
  }
  /** Allows easier testing of .data section contents by adding the ".data" prefix and the "\n.code\nSYSCALL 0" suffix. */
  void ParseData(String dataSectionContents) throws Exception {
    parser.doParsing(".data\n " + dataSectionContents + "\n.code\nSYSCALL 0");
  }

  /** Parse a double value */
  void ParseDouble(String doubleValue) throws Exception {
    ParseData(".double " + doubleValue);
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
}