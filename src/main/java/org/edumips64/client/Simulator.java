package org.edumips64.client;

import java.util.logging.Logger;

import jsinterop.annotations.JsType;
import org.edumips64.core.*;
import org.edumips64.core.CPU.CPUStatus;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.parser.Parser;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.InMemoryConfigStore;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.NullFileUtils;

public class Simulator {
  private CPU cpu;
  private Parser parser;
  private SymbolTable symTab;
  private Memory memory;
  private Dinero dinero;

  private ResultFactory resultFactory;
   
  private Logger logger = Logger.getLogger("simulator");

  // Enum for the simulator status. This is a trimmed-down version of
  // CPU.CPUStatus, hiding details that the JS client should not care
  // about.
  @JsType(namespace = "jsedumips64")
  public enum Status {READY, RUNNING, STOPPED};
  static Status FromCpuStatus(CPUStatus s) {
    switch (s) {
      case READY:
        return Status.READY;
      case RUNNING:
      case STOPPING:
        return Status.RUNNING;
      default:
        return Status.STOPPED;
    }
  }

  /* Public methods - available from JS. */

  /* Initialization / reset */
  public Simulator() {
    info("Initializing the simulator");
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
    resultFactory = new ResultFactory(cpu, memory);
    info("initialization complete!");
  }

  public void reset() {
      info("Resetting the CPU");
      cpu.reset();
      dinero.reset();
      symTab.reset();
  }

  /* Program execution control methods */
  public Result runProgram(String code) {
    Result parseResult = loadProgram(code);
    if (!parseResult.success) {
      return parseResult;
    }

    return runAll();
  }

  public Result runAll() {
    Result result;
    do {
      info("running one step");
      result = step();
      info("step results: " + result.toString());
    } while (result.success && (result.status != Status.STOPPED));

    return result;
  }

  public Result step() {
    CPUStatus status = cpu.getStatus();
    if (status != CPU.CPUStatus.RUNNING && status != CPU.CPUStatus.STOPPING) {
      String message = "Cannot run in state " + cpu.getStatus();
      return resultFactory.Failure(message);
    }

    try {
      cpu.step();
    } catch (HaltException e) {
      info("Program terminated successfully.");
    } catch (Exception e) {
      warning("Error: " + e.toString());
      return resultFactory.Failure(e.toString());
    }
    return resultFactory.Success();
  }

  public Result loadProgram(String code) {
    if (cpu.getStatus() != CPU.CPUStatus.READY) {
      info("Resetting CPU before loading a new program.");
      reset();
    }

    info("Loading program: " + code);
    try {
      parser.doParsing(code);
      dinero.setDataOffset(memory.getInstructionsNumber()*4);
    } catch (Exception e) {
      warning("Parsing error: " + e.toString());
      return resultFactory.Failure(e.toString());
    }
    cpu.setStatus(CPU.CPUStatus.RUNNING);
    info("Program parsed.");
    return resultFactory.Success();
  }
  /* Private methods */
  private void info(String message) {
    logger.info("[GWT] "+ message);
  }
  private void warning(String message) {
    logger.warning("[GWT] " + message);
  }
}