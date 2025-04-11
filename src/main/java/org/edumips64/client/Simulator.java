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
import org.edumips64.core.parser.Parser;
import org.edumips64.core.parser.ParserMultiException;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.InMemoryConfigStore;
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

  // TODO: handle these errors more elegantly.
  private ParserMultiException lastParsingErrors = null;

  public ResultFactory resultFactory;
   
  private Logger logger = Logger.getLogger("simulator");

  public Simulator() {
    info("Initializing the simulator");
    // Simulator initialization.
    ConfigStore config = new InMemoryConfigStore(ConfigStore.defaults);
    memory = new Memory();
    symTab = new SymbolTable(memory);
    stdout = new StringWriter();
    FileUtils fu = new NullFileUtils();
    iom = new IOManager(fu, memory);
    iom.setStdOutput(stdout);
    cpu = new CPU(memory, config, new BUBBLE());
    cachesim = new CacheSimulator();

    InstructionBuilder instructionBuilder = new InstructionBuilder(memory, iom, cpu, cachesim, config);
    parser = new Parser(fu, symTab, memory, instructionBuilder);
    resultFactory = new ResultFactory(cpu, memory, cachesim, stdout);
    info("initialization complete!");
  }

  public Result setCacheConfig(int l1dSize, int l1dBlockSize, int l1dAssoc, int l1dPenalty,
                 int l1iSize, int l1iBlockSize, int l1iAssoc, int l1iPenalty)  {
    cpu.reset();
    cachesim.getL1InstructionCache().setConfig(l1iSize,l1iBlockSize,l1iAssoc,l1iPenalty, CacheSimulator.CacheMemory.CacheType.L1_INSTRUCTION);
    cachesim.getL1DataCache().setConfig(l1dSize,l1dBlockSize,l1dAssoc,l1dPenalty, CacheSimulator.CacheMemory.CacheType.L1_DATA);
    resultFactory = new ResultFactory(cpu, memory, cachesim,stdout);
    return resultFactory.Success();
  }

  public Result reset() {
      info("Resetting the CPU");
      cpu.reset();
      cachesim.reset();
      symTab.reset();
      stdout = new StringWriter();
      iom.setStdOutput(stdout);
      resultFactory = new ResultFactory(cpu, memory, cachesim,stdout);
      return resultFactory.Success();
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
      } while (--steps > 0);
    } catch (HaltException e) {
      info("Program terminated successfully.");
    } catch (BreakException e) {
      Result res = resultFactory.Success();
      res = ResultFactory.AddParserErrors(res, lastParsingErrors);
      res.encounteredBreak = true;
      return res;
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

  /* Private methods */
  private void info(String message) {
    logger.info("[GWT] "+ message);
  }
  private void warning(String message) {
    logger.warning("[GWT] " + message);
  }
}
