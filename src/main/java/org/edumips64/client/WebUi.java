package org.edumips64.client;

import com.google.gwt.core.client.EntryPoint;

import jsinterop.annotations.JsType;

import org.edumips64.core.*;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.NullFileUtils;

import java.util.logging.Logger;

@JsType(namespace = "jsedumips64")
public class WebUi implements EntryPoint {
  private CPU cpu;
  private Parser parser;
  private ConfigStore config;
  private FileUtils fu;
  private SymbolTable symTab;
  private Memory memory;
  private InstructionBuilder instructionBuilder;

  // Executes the program. Returns an empty string on success, or an error message.
  public String runProgram(String code) {
    Logger logger = Logger.getLogger("simulator");
    logger.info("Running program: " + code);
    try {
      cpu.reset();
      Dinero.getInstance().reset();
      symTab.reset();
      logger.info("About to parse it.");
      parser.doParsing(code);
      logger.info("Parsed. Running.");
      cpu.setStatus(CPU.CPUStatus.RUNNING);
      while (true) {
        cpu.step();
      }
    } catch (HaltException e) {
      logger.info("All done.");
      return "";
    } catch (Exception e) {
      logger.warning("Error: " + e.toString());
      return e.toString();
    }
  }

  public String getMemory() {
    return Memory.getInstance().toString();
  }

  public String getRegisters() {
    return cpu.gprString();
  }

  public String getStatistics() {
    // Ugly, but GWT does not support String.format.
    return cpu.getCycles() + " cycles executed\n" +
        cpu.getInstructions() + " instructions executed\n" +
        cpu.getRAWStalls() + " RAW Stalls\n" +
        cpu.getWAWStalls() + " WAW Stalls\n" +
        cpu.getStructuralStallsDivider() + " structural stalls (divider not available)\n" +
        cpu.getStructuralStallsMemory() + " structural stalls (Memory not available)\n" +
        "Code Size: " + (Memory.getInstance().getInstructionsNumber() * 4) + " Bytes";
  }

 @Override
 public void onModuleLoad() {
 }

  public void init() {
    // Simulator initialization.
    config = ConfigManager.getTmpConfig();
    ConfigManager.setConfig(config);
    memory = Memory.getInstance();
    symTab = new SymbolTable(memory);
    fu = new NullFileUtils();
    instructionBuilder = new InstructionBuilder(memory);
    parser = new Parser(fu, symTab, memory, instructionBuilder);
    cpu = CPU.getInstance();
  }
}
