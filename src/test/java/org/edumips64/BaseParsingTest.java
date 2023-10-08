package org.edumips64;

import org.edumips64.core.CPU;
import org.edumips64.core.Dinero;
import org.edumips64.core.IOManager;
import org.edumips64.core.Memory;
import org.edumips64.core.SymbolTable;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.is.InstructionInterface;
import org.edumips64.core.is.ParsedInstructionMetadata;
import org.edumips64.core.parser.Parser;
import org.edumips64.core.parser.ParserMultiException;
import org.edumips64.utils.io.LocalFileUtils;
import org.junit.Before;
import org.junit.Test;

// Base class for tests that need to run the parser.
public class BaseParsingTest extends BaseTest {
  protected Parser parser;
  protected Memory memory;
  protected SymbolTable symTab;

  @Before
  public void setUp() throws Exception {
    memory = new Memory();
    CPU cpu = new CPU(memory, config, new BUBBLE());
    symTab = new SymbolTable(memory);
    IOManager iom = new IOManager(new LocalFileUtils(), memory);
    Dinero dinero = new Dinero();
    InstructionBuilder instructionBuilder = new InstructionBuilder(memory, iom, cpu, dinero, config);
    parser = new Parser(new LocalFileUtils(), symTab, memory, instructionBuilder);
  }
  
  /** Allows easier testing of .data section contents by adding the ".data" prefix and the "\n.code\nSYSCALL 0" suffix. */
  protected void ParseData(String dataSectionContents) throws Exception {
    parser.doParsing(".data\n " + dataSectionContents + "\n.code\nSYSCALL 0");
  }

  /** Allows easier testing of .code section contents by adding the ".code" prefix and the "\nSYSCALL 0" suffix. */
  protected void ParseCode(String codeSectionContents) throws Exception {
    parser.doParsing(".code\n " + codeSectionContents + "\nSYSCALL 0");
  }

  /** Parse a double value */
  protected void ParseDouble(String doubleValue) throws Exception {
    ParseData(".double " + doubleValue);
  }
}