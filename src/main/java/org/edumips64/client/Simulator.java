/* Simulator.java
 *
 * A facade for the EduMIPS64 core, to be used by Worker.java.
 * 
 * All public methods return an instance of Result, which can be sent back
 * to the JS code.
 * 
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

import java.util.logging.Logger;

import org.edumips64.core.*;
import org.edumips64.core.CPU.CPUStatus;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.BreakException;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.is.UnsupportedSyscallException;
import org.edumips64.core.parser.Parser;
import org.edumips64.core.parser.ParserMultiException;
import org.edumips64.core.cache.CacheConfig;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CycleBuilder;
import org.edumips64.utils.InMemoryConfigStore;
import org.edumips64.utils.io.InputNeededException;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.NullFileUtils;
import org.edumips64.utils.io.StringWriter;

public class Simulator {
  private CPU cpu;
  private Parser parser;
  private SymbolTable symTab;
  private Memory memory;
  private CacheSimulator cachesim;
  private StringWriter stdout;
  private IOManager iom;
  private WebInputReader stdin;
  // Config store used by the CPU and the instruction builder. Held as a field
  // so that runtime-tweakable settings (e.g. forwarding) can be updated from
  // the worker protocol without recreating the whole simulator.
  private ConfigStore config;

  // Per-cycle builder of the pipeline-state diagram. Reused from the Swing UI
  // (`org.edumips64.utils.CycleBuilder`); we step it alongside `cpu.step()`
  // and read the latest state of each in-flight instruction from it so the
  // Web UI can label active stalls (RAW / WAW / StDiv / StEx / StFun / Str /
  // StAdd / StMul) the same way the Swing cycle widget does.
  private CycleBuilder cycleBuilder;

  // TODO: handle these errors more elegantly.
  private ParserMultiException lastParsingErrors = null;

  public ResultFactory resultFactory;
   
  private Logger logger = Logger.getLogger("simulator");
  private String supportedInstructions;

  public Simulator() {
    info("Initializing the simulator");
    // Simulator initialization.
    config = new InMemoryConfigStore(ConfigStore.defaults);
    memory = new Memory();
    symTab = new SymbolTable(memory);
    stdout = new StringWriter();
    stdin = new WebInputReader();
    FileUtils fu = new NullFileUtils();
    iom = new IOManager(fu, memory);
    iom.setStdOutput(stdout);
    iom.setStdInput(stdin);
    cpu = new CPU(memory, config, new BUBBLE());
    cachesim = new CacheSimulator();
    cycleBuilder = new CycleBuilder(cpu);

    InstructionBuilder instructionBuilder = new InstructionBuilder(memory, iom, cpu, cachesim, config);
    parser = new Parser(fu, symTab, memory, instructionBuilder);
    resultFactory = new ResultFactory(cpu, memory, cachesim, stdout, cycleBuilder);
    supportedInstructions = instructionBuilder.getSupportedInstructionString();
    info("initialization complete!");
  }

  public Result setForwarding(boolean enabled) {
    // The CPU reads this flag dynamically on every step from the ConfigStore.
    // Update the config *before* resetting the CPU so any component that
    // re-reads the setting during reset observes the new value.
    config.putBoolean(ConfigKey.FORWARDING, enabled);
    cpu.reset();
    cycleBuilder = new CycleBuilder(cpu);
    return resultFactory.Success();
  }

  public Result setCacheConfig(CacheConfig l1d_config, CacheConfig l1i_config)  {
    cpu.reset();
    cachesim.getL1InstructionCache().setConfig(l1i_config);
    cachesim.getL1DataCache().setConfig(l1d_config);
    cycleBuilder = new CycleBuilder(cpu);
    resultFactory = new ResultFactory(cpu, memory, cachesim, stdout, cycleBuilder);
    return resultFactory.Success();
  }

  public Result reset() {
      info("Resetting the CPU");
      cpu.reset();
      cachesim.reset();
      symTab.reset();
      stdout = new StringWriter();
      stdin.reset();
      iom.setStdOutput(stdout);
      iom.setStdInput(stdin);

      cycleBuilder = new CycleBuilder(cpu);
      resultFactory = new ResultFactory(cpu, memory, cachesim, stdout, cycleBuilder);
      var result = resultFactory.Success();

      // reset() provides the JS simulator's initial state. Therefore, we pass the
      // list of supported instructions, so we can set up the syntax highlighting.
      result.validInstructions = supportedInstructions;
      return result;
  }

  public Result step(int steps) {
    CPUStatus status = cpu.getStatus();
    if (status != CPU.CPUStatus.RUNNING && status != CPU.CPUStatus.STOPPING) {
      String message = "Cannot run in state " + cpu.getStatus();
      return resultFactory.Failure(message);
    }

    if (steps <= 0) {
      return resultFactory.Failure("The number of steps must be positive");
    }

    try {
      do {
        cpu.step();
        // Step the cycle builder alongside the CPU so the per-cycle stall
        // classification surfaced via Pipeline.getStage() stays in sync. We
        // ignore HaltException-driven step updates: when HaltException is
        // thrown the CPU decrements its `cycles` counter and we end up in
        // the catch block below, where stepping CycleBuilder would be a
        // no-op anyway (oldTime == cycles).
        cycleBuilder.step();
      } while (--steps > 0);
    } catch (HaltException e) {
      info("Program terminated successfully.");
    } catch (BreakException e) {
      Result res = resultFactory.Success();
      res = ResultFactory.AddParserErrors(res, lastParsingErrors);
      res.encounteredBreak = true;
      return res;
    } catch (InputNeededException e) {
      Result r = resultFactory.Success();
      r = ResultFactory.AddInputNeeded(r, e, steps);
      r = ResultFactory.AddParserErrors(r, lastParsingErrors);
      return r;
    } catch (SynchronousException e) {
      // Synchronous exceptions (INTOVERFLOW, DIVZERO, FP traps, ...) are
      // runtime errors. Build a Failure carrying a user-friendly message,
      // then layer runtime-error info and any pending parser warnings on top.
      warning("Synchronous exception: " + e.getCode());
      Result r = resultFactory.Failure(SynchronousExceptionFormatter.format(e));
      r = ResultFactory.AddRuntimeErrors(r, e);
      r = ResultFactory.AddParserErrors(r, lastParsingErrors);
      return r;
    } catch (UnsupportedSyscallException e) {
      // Unsupported SYSCALLs are runtime errors too. Route them through the
      // same rich-error plumbing as SynchronousException so the Web UI can
      // render errorCode / errorInstruction / errorStage.
      warning("Unsupported syscall: " + e.getMessage());
      SynchronousException sx = e.toSynchronousException();
      Result r = resultFactory.Failure(e.getMessage());
      r = ResultFactory.AddRuntimeErrors(r, sx);
      r = ResultFactory.AddParserErrors(r, lastParsingErrors);
      return r;
    } catch (Exception e) {
      warning("Error: " + e.toString());
      return ResultFactory.AddParserErrors(resultFactory.Failure(e.toString()), lastParsingErrors);
    }
    return ResultFactory.AddParserErrors(resultFactory.Success(), lastParsingErrors);
  }

  public Result loadProgram(String code) {
    info("Resetting CPU before loading a new program.");
    reset();

    info("Loading program: " + code);
    boolean hadErrors = false;
    try {
      parser.doParsing(code);
      cachesim.setDataOffset(memory.getInstructionsNumber()*4);
    } catch (ParserMultiException e) {
      hadErrors = true;
      warning("Parsing error: " + e.toString());
      lastParsingErrors = e;
      if (e.hasErrors()) {
        Result result = resultFactory.Failure("Parsing errors.");
        result = ResultFactory.AddParserErrors(result, e);
        return result;
      }
    }
    if (!hadErrors) {
      lastParsingErrors = null;
    }
    cpu.setStatus(CPU.CPUStatus.RUNNING);
    info("Program parsed.");
    return ResultFactory.AddParserErrors(resultFactory.Success(), lastParsingErrors);
  }

  public void provideInput(String input) {
    stdin.setNextInput(input);
  }

  /* Private methods */
  private void info(String message) {
    logger.info("[GWT] "+ message);
  }
  private void warning(String message) {
    logger.warning("[GWT] " + message);
  }
}
