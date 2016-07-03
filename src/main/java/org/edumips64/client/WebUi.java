package org.edumips64.client;

import com.google.gwt.core.client.EntryPoint;

import jsinterop.annotations.JsType;

import org.edumips64.core.CPU;
import org.edumips64.core.IrregularWriteOperationException;
import org.edumips64.core.Memory;
import org.edumips64.core.MemoryElementNotFoundException;
import org.edumips64.core.NotAlignException;
import org.edumips64.core.Parser;
import org.edumips64.core.ParserMultiException;
import org.edumips64.core.Register;
import org.edumips64.core.StoppedCPUException;
import org.edumips64.core.SynchronousException;
import org.edumips64.core.fpu.FPDividerNotAvailableException;
import org.edumips64.core.fpu.FPFunctionalUnitNotAvailableException;
import org.edumips64.core.fpu.MemoryNotAvailableException;
import org.edumips64.core.is.AddressErrorException;
import org.edumips64.core.is.BreakException;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.TwosComplementSumException;
import org.edumips64.core.is.WAWException;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.ConfigManager;
import org.edumips64.utils.IrregularStringOfBitsException;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.NullFileUtils;

import java.util.logging.Logger;

@JsType(namespace = "jsedumips64")
public class WebUi implements EntryPoint {
  private CPU cpu;
  private Parser parser;
  private ConfigStore config;
  private FileUtils fu;

  class WrongStateException extends Exception {
    WrongStateException(State expected, State actual) {
      super("Expected state " + expected.name() + ", got " + actual.name());
    }

    WrongStateException() {
    }
  }

  enum State {
    // Simulator just loaded, nothing parsed.
    IDLE,
    // Program is running.
    RUNNING,
    // The program finished running
    HALTED
  };
  private State state;

  private void checkState(State expected) throws WrongStateException {
    if (expected != state) {
      throw new WrongStateException(expected, state);
    }
  }

  /** Will transition from IDLE to READY */
  public void loadProgram(String code) throws WrongStateException, ParserMultiException {
    checkState(State.IDLE);
    parser.doParsing(code);
    cpu.setStatus(CPU.CPUStatus.RUNNING);
    state = State.RUNNING;
  }

  /** This can throw a ton of exceptions.. need to be handled differently in the simulator */
  public void step() throws WrongStateException, MemoryNotAvailableException, NotAlignException, WAWException, SynchronousException, IrregularStringOfBitsException, StoppedCPUException, BreakException, FPFunctionalUnitNotAvailableException, HaltException, FPDividerNotAvailableException, IrregularWriteOperationException, AddressErrorException, MemoryElementNotFoundException, TwosComplementSumException {
    checkState(State.RUNNING);
    try {
      cpu.step();
    } catch (HaltException e) {
      state = State.HALTED;
    }
  }

  // Executes the program. Returns an empty string on success, or an error message.
  public String runProgram(String code) {
    Logger logger = Logger.getLogger("simulator");
    logger.info("Running program: " + code);
    try {
      cpu.reset();
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

  public Memory getMemory() {
    return cpu.getMemory();
  }

  public Register[] getRegisters() {
    return cpu.getRegisters();
  }

  public void reset() {
    state = State.IDLE;
    cpu.reset();
  }

  public String getMemoryString() {
    return cpu.getMemory().toString();
  }

  public String getRegistersString() {
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
        "Code Size: " + (cpu.getMemory().getInstructionsNumber() * 4) + " Bytes";
  }

  @Override
  public void onModuleLoad() {}

  public void init() {
    // Simulator initialization.
    config = ConfigManager.getTmpConfig();
    ConfigManager.setConfig(config);
    fu = new NullFileUtils();
    Parser.createInstance(fu);
    parser = Parser.getInstance();
    cpu = CPU.getInstance();
    state = State.IDLE;
  }
}
