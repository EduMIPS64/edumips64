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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.user.client.Command;

import jsinterop.annotations.JsType;

import org.edumips64.core.*;
import org.edumips64.core.CPU.CPUStatus;
import org.edumips64.core.fpu.RegisterFP;
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

  /* Public methods - available from JS. */

  /* Initialization / reset */
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

  public void reset() {
      info("Resetting the CPU");
      cpu.reset();
      dinero.reset();
      symTab.reset();
  }

  @Override
  /* Initialization method, executed once GWT is ready to be imported by JS */
  public void onModuleLoad() {
    // Invoke JS initialization logic that depends on this GWT module being loaded.
    info("Module loaded, calling the global JS function onGwtReady()");
    Scheduler.get().scheduleDeferred(new Command() {
      public void execute() {
        runOnGwtReady();
      }
    });
  }

  /* Program execution control methods */
  public Result runProgram(String code) {
    Result parseResult = loadProgram(code);
    if (!parseResult.success) {
      return parseResult;
    }

    CPUStepResult result = runAll();
    return result;
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

  /* Private methods */
  private Result loadProgram(String code) {
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
      return Result.Failure(e.toString());
    }
    cpu.setStatus(CPU.CPUStatus.RUNNING);
    info("Program parsed.");
    return Result.Success();
  }

  private CPUStepResult step() {
    CPUStatus status = cpu.getStatus();
    if (status != CPU.CPUStatus.RUNNING && status != CPU.CPUStatus.STOPPING) {
      String message = "Cannot run in state " + cpu.getStatus();
      return new CPUStepResult(Result.Failure(message), true);
    }

    try {
      cpu.step();
    } catch (HaltException e) {
      info("Program terminated successfully.");
      return new CPUStepResult(Result.Success(), true);
    } catch (Exception e) {
      warning("Error: " + e.toString());
      return new CPUStepResult(Result.Failure(e.toString()), true);
    }

    return new CPUStepResult(Result.Success(), false);
  }

  private CPUStepResult runAll() {
    CPUStepResult cpuStatus;
    do {
      info("running one step");
      cpuStatus = step();
      info("step results: " + cpuStatus.toString());
    } while (cpuStatus.success && !cpuStatus.terminated);

    return cpuStatus;
  }


  private native void runOnGwtReady() /*-{
    if (typeof $wnd.onGwtReady !== "undefined") {
      $wnd.onGwtReady();
    }
  }-*/;
  private void info(String message) {
    logger.info("[GWT] " + message);
  }
  private void warning(String message) {
    logger.warning("[GWT] " + message);
  }

}
