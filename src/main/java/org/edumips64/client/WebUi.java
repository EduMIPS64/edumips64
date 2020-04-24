package org.edumips64.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.json.client.JSONArray;

import jsinterop.annotations.JsType;

import org.edumips64.core.*;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.parser.Parser;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.InMemoryConfigStore;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.NullFileUtils;

import java.util.logging.Logger;

@JsType(namespace = "jsedumips64")
public class WebUi implements EntryPoint {
  private CPU cpu;
  private Parser parser;
  private SymbolTable symTab;
  private Memory memory;
  private Dinero dinero;
   
  private Logger logger = Logger.getLogger("simulator");

  // Executes the program. Returns an empty string on success, or an error message.
  public String runProgram(String code) {
    logger.info("Running program: " + code);
    try {
      cpu.reset();
      dinero.reset();
      symTab.reset();
      logger.info("About to parse it.");
      parser.doParsing(code);
      dinero.setDataOffset(memory.getInstructionsNumber()*4);
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
    return memory.toString();
  }

  public String getRegisters() {
    JSONArray registers = new JSONArray();

    try {
      int i = 0;
      for(Register r : cpu.getRegisters()) {
        registers.set(i++,
          new FluentJsonObject()
            .put("name", r.getName())
            .put("value", r.getHexString())
            .toJsonObject());
      }
    } catch (Exception e) {
      logger.warning("Error fetching registers: " + e.toString());
    }
    return registers.toString();
  }

  public String getStatistics() {
    return new FluentJsonObject()
      // Execution
      .put("cycles", cpu.getCycles())
      .put("instructions", cpu.getInstructions())
      // Stalls
      .put("rawStalls", cpu.getRAWStalls())
      .put("wawStalls", cpu.getWAWStalls())
      .put("dividerStalls", cpu.getStructuralStallsDivider())
      .put("memoryStalls", cpu.getStructuralStallsMemory())
      // Code size
      .put("codeSizeBytes",memory.getInstructionsNumber() * 4)
      // FPU Control Status Register (FCSR)
      .put("fcsr", cpu.getFCSR().getBinString())
      .toString();
  }

  @Override
  public void onModuleLoad() {}

  public void init() {
    // Simulator initialization.
    ConfigStore config = new InMemoryConfigStore(ConfigStore.defaults);
    memory = new Memory();
    symTab = new SymbolTable(memory);
    FileUtils fu = new NullFileUtils();
    IOManager iom = new IOManager(fu, memory);
    cpu = new CPU(memory, config, new BUBBLE());
    dinero = new Dinero();
    InstructionBuilder instructionBuilder = new InstructionBuilder(memory, iom, cpu, dinero, config);
    parser = new Parser(fu, symTab, memory, instructionBuilder);
  }
}
