/* WebUI.java
 *
 * GWT facade for the EduMIPS64 core.
 * (c) 2020 Andrea Spadaccini
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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;

import jsinterop.annotations.JsType;

import elemental2.dom.DomGlobal;

import org.edumips64.core.*;
import org.edumips64.core.CPU.CPUStatus;
import org.edumips64.core.Pipeline.Stage;
import org.edumips64.core.fpu.RegisterFP;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.is.InstructionInterface;
import org.edumips64.core.parser.Parser;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.InMemoryConfigStore;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.NullFileUtils;

import java.util.Map;
import java.util.logging.Logger;

@JsType(namespace = "jsedumips64")
public class WebUi implements EntryPoint {
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
  public void init() {
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
    resultFactory = new ResultFactory(cpu);
    info("initialization complete!");
  }

  public void reset() {
      info("Resetting the CPU");
      cpu.reset();
      dinero.reset();
      symTab.reset();
  }
  
  @Override
  public void onModuleLoad() {
    GWT.log("in onModuleLoad");
    info("Worker loaded, calling the global JS function onGwtReady()");
    init();
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

  /* Public methods to get Simulator state */
  public String getMemory() {
    return memory.toString();
  }

  public String getRegisters() {
    FluentJsonObject registers = new FluentJsonObject();

    try {
      // General Purpose Registers (GPR).
      int i = 0;
      JSONArray jsonGeneralRegisters = new JSONArray();
      for(Register r : cpu.getRegisters()) {
        jsonGeneralRegisters.set(i++,
          new FluentJsonObject()
            .put("name", r.getName())
            .put("value", r.getHexString())
            .toJsonObject());
      }
      registers.put("gpr", jsonGeneralRegisters);

      // FPU registers.
      i = 0;
      JSONArray jsonFpuRegisters = new JSONArray();
      for(RegisterFP r : cpu.getRegistersFP()) {
        jsonFpuRegisters.set(i++,
          new FluentJsonObject()
            .put("name", r.getName())
            .put("value", r.getHexString())
            .toJsonObject());
      }
      registers.put("fpu", jsonFpuRegisters);

      // Special registers (hi/lo/fcsr).
      i = 0;
      JSONArray specialRegisters = new JSONArray();
      specialRegisters.set(i++,
          new FluentJsonObject()
            .put("name", cpu.getLO().getName())
            .put("value", cpu.getLO().getHexString())
            .toJsonObject());
      specialRegisters.set(i++,
          new FluentJsonObject()
            .put("name", cpu.getHI().getName())
            .put("value", cpu.getHI().getHexString())
            .toJsonObject());
      specialRegisters.set(i++,
          new FluentJsonObject()
            .put("name", "FCSR")
            .put("value", cpu.getFCSR().getHexString())
            .toJsonObject());
      registers.put("special", specialRegisters);
    } catch (Exception e) {
      warning("Error fetching registers: " + e.toString());
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

  public Pipeline getPipeline() {
    // Convert the internal CPU representation to objects available to the JS code.
    Map<Stage, InstructionInterface> cpuPipeline = cpu.getPipeline();

    Pipeline p = new Pipeline();
    p.IF = Instruction.FromInstruction(cpuPipeline.get(Stage.IF));
    p.ID = Instruction.FromInstruction(cpuPipeline.get(Stage.ID));
    p.EX = Instruction.FromInstruction(cpuPipeline.get(Stage.EX));
    p.MEM = Instruction.FromInstruction(cpuPipeline.get(Stage.MEM));
    p.WB = Instruction.FromInstruction(cpuPipeline.get(Stage.WB));

    return p;
  }

  /* Private methods */
  private void info(String message) {
    DomGlobal.console.log("[GWT] " + message);
  }
  private void warning(String message) {
    DomGlobal.console.log("[GWT - warning] " + message);
  }
}