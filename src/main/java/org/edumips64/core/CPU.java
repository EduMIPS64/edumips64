/* CPU.java
 *
 * This class models a MIPS CPU with 32 64-bit General Purpose Register.
 * (c) 2006 Andrea Spadaccini, Simona Ullo, Antonella Scandura, Massimo Trubia (FPU modifications)
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

package org.edumips64.core;

import org.edumips64.core.fpu.EXNotAvailableException;
import org.edumips64.core.fpu.FPDividerNotAvailableException;
import org.edumips64.core.fpu.FPFunctionalUnitNotAvailableException;
import org.edumips64.core.fpu.FPInvalidOperationException;
import org.edumips64.core.fpu.FPPipeline;
import org.edumips64.core.fpu.RegisterFP;
import org.edumips64.core.is.AddressErrorException;
import org.edumips64.core.is.BreakException;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.InstructionInterface;
import org.edumips64.core.is.IntegerOverflowException;
import org.edumips64.core.is.JumpException;
import org.edumips64.core.is.RAWException;
import org.edumips64.core.is.StoppingException;
import org.edumips64.core.is.TwosComplementSumException;
import org.edumips64.core.is.WAWException;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;

/** This class models a MIPS CPU with 32 64-bit General Purpose Registers.
*  @author Andrea Spadaccini, Simona Ullo, Antonella Scandura, Massimo Trubia (FPU modifications)
*/
public class CPU {
  private Memory mem;
  private Register[] gpr;
  private static final Logger logger = Logger.getLogger(CPU.class.getName());

  /** FPU Elements*/
  private RegisterFP[] fpr;
  private FCSRRegister FCSR;
  private FPPipeline fpPipe;

  /** Program Counter*/
  private Register pc, old_pc;
  private Register LO, HI;

  /** CPU status.
   *  READY - the CPU has been initialized but the symbol table hasn't been
   *  already filled by the Parser. This means that you can't call the step()
   *  method, or you'll get a StoppedCPUException.
   *
   *  RUNNING - the CPU is executing a program, you can call the step()
   *  method, and the CPU will fetch additional instructions from the symbol
   *  table
   *
   *  STOPPING - the HALT instruction has entered in the pipeline. This means
   *  that no additional instructions must be fetched but the instructions
   *  that are already in the pipeline must be executed. THe step() method can
   *  be called, but won't fetch any other instruction
   *
   *  HALTED - the HALT instruction has passed the WB stage, and the step()
   *  method can't be executed.
   * */
  public enum CPUStatus {READY, RUNNING, STOPPING, HALTED}
  private CPUStatus status;

  /** CPU pipeline, each stage contains an Instruction object. */
  private Pipeline pipe;

  /** The current stage of the pipeline.*/
  private Pipeline.Stage currentPipeStage;

  /** Simulator configuration */
  private ConfigStore config;

  /** Statistics */
  private int cycles, instructions, RAWStalls, WAWStalls, dividerStalls, funcUnitStalls, memoryStalls, exStalls;

  /** BUBBLE */
  private InstructionInterface bubble;

  /** Terminating instructions */
  private static ArrayList<String> terminating = new ArrayList<>(
      Arrays.asList("0000000C",     // SYSCALL 0
                    "04000000"));   // HALT

  private Consumer<String> cpuStatusChangeCallback;

  public void setCpuStatusChangeCallback(Consumer<String> callback) {
    cpuStatusChangeCallback = callback;
  }

  public CPU(Memory memory, ConfigStore config, InstructionInterface bubble) {
    this.config = config;
    this.bubble = bubble;

    logger.info("Creating the CPU...");
    cycles = 0;
    setStatus(CPUStatus.READY);
    mem = memory;
    logger.info("Got Memory instance..");

    // Registers initialization
    gpr = new Register[32];
    gpr[0] = new R0();

    for (int i = 1; i < 32; i++) {
      gpr[i] = new Register("R" + i);
    }

    pc = new Register("PC");
    old_pc = new Register("Old PC");
    LO = new Register("LO");
    HI = new Register("HI");

    //Floating point registers initialization
    fpr = new RegisterFP[32];

    for (int i = 0; i < 32; i++) {
      fpr[i] = new RegisterFP("F" + i);
    }

    FCSR = new FCSRRegister();
    configFPExceptionsAndRM();
    fpPipe = new FPPipeline();
    fpPipe.reset();


    // Pipeline initialization
    pipe = new Pipeline();
    currentPipeStage = Pipeline.Stage.IF;
    logger.info("CPU Created.");
  }


// SETTING PROPERTIES ------------------------------------------------------------------
  /** Sets the CPU status.
   *  @param status a CPUStatus value
   */
  public void setStatus(CPUStatus status) {
    logger.info("Changing CPU status to " + status.name());
    this.status = status;
    if (cpuStatusChangeCallback != null) {
      cpuStatusChangeCallback.accept(status.name());
    }
  }

  /** Sets the flag bits of the FCSR
  * @param tag a string value between  V  Z O U I
  * @param value a binary value
   */
  public void setFCSRFlags(String tag, int value) throws IrregularStringOfBitsException {
    FCSR.setFCSRFlags(tag, value);
  }

  /** Sets the cause bits of the FCSR
  * @param tag a string value between  V  Z O U I
  * @param value a binary value
   */
  public void setFCSRCause(String tag, int value) throws IrregularStringOfBitsException {
    FCSR.setFCSRCause(tag, value);
  }

  /** Sets the selected FCC bit of the FCSR
   * @param cc condition code is an int value in the range [0,7]
   * @param condition the binary value of the relative bit
   */
  public void setFCSRConditionCode(int cc, int condition) throws IrregularStringOfBitsException {
    FCSR.setFCSRConditionCode(cc, condition);
  }

//GETTING PROPERTIES -----------------------------------------------------------------

  /** Gets the CPU status
   *  @return status a CPUStatus value representing the current CPU status
   */
  public CPUStatus getStatus() {
    return status;
  }

  public Register[] getRegisters() {
    return gpr;
  }

  public RegisterFP[] getRegistersFP() {
    return fpr;
  }

  /** This method returns a specific GPR
   * @param index the register number (0-31)
   */
  public Register getRegister(int index) {
    return gpr[index];
  }

  public RegisterFP getRegisterFP(int index) {
    return fpr[index];
  }

  /** Returns true if the specified functional unit is filled by an instruction, false when the contrary happens.
   *  No controls are carried out on the legality of parameters, for mistaken parameters false is returned
   *  @param funcUnit The functional unit to check. Legal values are "ADDER", "MULTIPLIER", "DIVIDER"
   *  @param stage The integer that refers to the stage of the functional unit.
   *      ADDER [1,4], MULTIPLIER [1,7], DIVIDER [any] */
  public boolean isFuncUnitFilled(String funcUnit, int stage) {
    return fpPipe.isFuncUnitFilled(funcUnit, stage);
  }

  /** Returns true if the pipeline is empty. In this case, if CPU is in stopping state
   *  we can halt the pipeline. The sufficient condition in order to return true is that fpPipe doesn't work
   *  and it hadn't issued any instrution now in the MEM stage */
  private boolean isPipelinesEmpty() {
    // WB is not checked because currently this method is called before the
    // instruction in WB is removed from the pipeline.
    return pipe.isEmptyOrBubble(Pipeline.Stage.ID) &&
           pipe.isEmptyOrBubble(Pipeline.Stage.EX) &&
           pipe.isEmptyOrBubble(Pipeline.Stage.MEM) &&
           fpPipe.isEmpty();
  }

  /** Returns the instruction of the specified functional unit , null if it is empty.
   *  No controls are carried out on the legality of parameters, for mistaken parameters null is returned
   *  @param funcUnit The functional unit to check. Legal values are "ADDER", "MULTIPLIER", "DIVIDER"
   *  @param stage The integer that refers to the stage of the functional unit.
   *      ADDER [1,4], MULTIPLIER [1,7], DIVIDER [any] */

  public InstructionInterface getFpuInstruction(String funcUnit, int stage) {
    return fpPipe.getInstructionByFuncUnit(funcUnit, stage);
  }

  /** Gets the Floating Point Control Status Register*/
  public FCSRRegister getFCSR() {
    return FCSR;
  }

  /** Gets the selected FCC bit of the FCSR
   * @param cc condition code is an int value in the range [0,7]
   */
  public int getFCSRConditionCode(int cc) {
    return FCSR.getFCSRConditionCode(cc);
  }

  /** Gets the current rounding mode readeng the FCSR
   * @return the rounding mode */
  public FCSRRegister.FPRoundingMode getFCSRRoundingMode() {
    return FCSR.getFCSRRoundingMode();
  }

  /** Gets the current computing step of the divider*/
  public int getDividerCounter() {
    return fpPipe.getDividerCounter();
  }

  /** Gets the integer pipeline
   *  @return an HashMap
   */
  public Map<Pipeline.Stage, InstructionInterface> getPipeline() {
    // TODO: fix callers to use Pipeline.
    return pipe.getInternalRepresentation();
  }

  // Includes FP instructions and bubbles. Used by CycleBuilder.
  public int getInstructionCount() {
    return pipe.size() + fpPipe.size();
  }

  /** Returns the number of cycles performed by the CPU.
   *  @return an integer
   */
  public int getCycles() {
    return cycles;
  }

  /** Returns the number of instructions executed by the CPU
   *  @return an integer
   */
  public int getInstructions() {
    return instructions;
  }

  /** Returns the number of RAW Stalls that happened inside the pipeline
   * @return an integer
   */
  public int getRAWStalls() {
    return RAWStalls;
  }

  /** Returns the number of WAW stalls that happened inside the pipeline
   * @return an integer
   */
  public int getWAWStalls() {
    return WAWStalls;
  }

  /** Returns the number of Structural Stalls (Divider not available) that happened inside the pipeline
   * @return an integer
   */
  public int getStructuralStallsDivider() {
    return dividerStalls;
  }

  /** Returns the number of Structural Stalls (Memory not available) that happened inside the pipeline
   * @return an integer
   */
  public int getStructuralStallsMemory() {
    return memoryStalls;
  }

  /** Returns the number of Structural Stalls (EX not available) that happened inside the pipeline
   * @return an integer
   */
  public int getStructuralStallsEX() {
    return exStalls;
  }

  /** Returns the number of Structural Stalls (FP Adder and FP Multiplier not available) that happened inside the pipeline
   * @return an integer
   */
  public int getStructuralStallsFuncUnit() {
    return funcUnitStalls;
  }

  /** Gets the floating point unit enabled exceptions
   *  @return true if exceptionName is enabled, false in the other case
   */
  public boolean getFPExceptions(FCSRRegister.FPExceptions exceptionName) {
    return FCSR.getFPExceptions(exceptionName);
  }

  /** Gets the Program Counter register
   *  @return a Register object
   */
  public Register getPC() {
    return pc;
  }
  /** Gets the Last Program Counter register
   *  @return a Register object
   */
  public Register getLastPC() {
    return old_pc;
  }

  /** Gets the LO register. It contains integer results of doubleword division
   * @return a Register object
   */
  public Register getLO() {
    return LO;
  }

  /** Gets the HI register. It contains integer results of doubleword division
   * @return a Register object
   */
  public Register getHI() {
    return HI;
  }

  /** Gets the structural stall counter
   *@return the memory stall counter
   */
  public int getMemoryStalls() {
    return memoryStalls;
  }

  /** This method performs a single pipeline step
  */
  public void step() throws AddressErrorException, HaltException, IrregularWriteOperationException, StoppedCPUException, MemoryElementNotFoundException, IrregularStringOfBitsException, TwosComplementSumException, SynchronousException, BreakException, NotAlignException {
    configFPExceptionsAndRM();
    Optional<String> syncex;
    if (status != CPUStatus.RUNNING && status != CPUStatus.STOPPING) {
      throw new StoppedCPUException();
    }

    try {
      // Stages are executed from the last one (WB) to the first one (IF). After the
      // logic for the given stage is executed, the instruction is moved to the next
      // stage (except for WB, where the instruction is discarded.
      logger.info("\n\nStarting cycle " + ++cycles + "\n---------------------------------------------");

      // WB: Write-back stage.
      stepWB();

      // MEM: Memory access stage.
      stepMEM();

      // EX: Execution/effective address stage.
      // Returns the code of the synchronous exception that can happen at this
      // stage, so the rest of the step can continue and, at the end, the
      // exception can be thrown.
      syncex = stepEX();

      // ID: instruction decode / register fetch stage. The RAW exception is handled
      // via a return value instead of an exception because throwing exceptions proved
      // to be a bottleneck in large programs. (See docs for Instruction.ID()).
      boolean rawException = stepID();
      if (rawException) {
        throw new RAWException();
      }

      // IF: instruction fetch stage.
      stepIF();

      if (syncex.isPresent()) {
        throw new SynchronousException(syncex.get());
      }
    } catch (JumpException ex) {
      logger.info("Executing a Jump.");
      try {
        if (!pipe.isEmpty(Pipeline.Stage.IF)) {
          logger.info("Executing the IF() method of the instruction in IF.");
          pipe.IF().IF();
        }
      } catch (BreakException bex) {
        // This needs to be ignored here because BREAK throws BreakException when it enters
        // the IF stage, but if BREAK enters IF after a jump instruction is about to modify
        // the program counter, then it must be discarded (like every other instruction).
        logger.info("Caught a BREAK after a Jump: ignoring it.");
      }

      // A J-Type instruction has just modified the Program Counter. We need to
      // put in the IF stage the instruction the PC points to
      pipe.setIF(mem.getInstruction(pc));
      pipe.setEX(pipe.ID());
      pipe.setID(bubble);
      old_pc.writeDoubleWord((pc.getValue()));
      pc.writeDoubleWord((pc.getValue()) + 4);

    } catch (RAWException ex) {
      logger.info("RAW - Read-After-Write");
      if (currentPipeStage == Pipeline.Stage.ID && pipe.EX() == null) {
        logger.info("Adding a BUBBLE instruction into EX.");
        pipe.setEX(bubble);
      }
      RAWStalls++;

    } catch (WAWException ex) {
      logger.info("WAW - Write-After-Write");
      logger.info(fpPipe.toString());

      if (currentPipeStage == Pipeline.Stage.ID && pipe.EX() == null) {
        logger.info("Adding a BUBBLE instruction into EX.");
        pipe.setEX(bubble);
      }
      WAWStalls++;

    } catch (FPDividerNotAvailableException ex) {
      logger.info("Structural Stall - FP Divider Unavailable");
      if (currentPipeStage == Pipeline.Stage.ID) {
        logger.info("Adding a BUBBLE instruction into EX.");
        pipe.setEX(bubble);
      }
      dividerStalls++;

    } catch (FPFunctionalUnitNotAvailableException ex) {
      logger.info("Structural Stall - FP Unavailable");
      if (currentPipeStage == Pipeline.Stage.ID) {
        logger.info("Adding a BUBBLE instruction into EX.");
        pipe.setEX(bubble);
      }
      funcUnitStalls++;

    } catch (EXNotAvailableException ex) {
      logger.info("Structural Stall - EX Unavailable");
      exStalls++;

    } catch (SynchronousException ex) {
      logger.info("Exception: " + ex.getCode());
      throw ex;

    } catch (HaltException ex) {
      setStatus(CPU.CPUStatus.HALTED);
      pipe.setWB(null);
      // The last tick does not execute a full CPU cycle, it just removes the last instruction from the pipeline.
      // Decrementing the cycles counter by one.
      cycles--;
      throw ex;

    } finally {
      logger.info("End of cycle " + cycles + "\n---------------------------------------------\n" + pipeLineString() + "\n");
    }
  }

  private void changeStage(Pipeline.Stage newStatus) {
    logger.info(newStatus.toString() + " STAGE: " + pipe.get(newStatus) + "\n================================");
    currentPipeStage = newStatus;
  }

  // Individual stages, in execution order (WB, MEM, EX, ID, IF).
  private void stepWB() throws HaltException, IrregularStringOfBitsException {
    changeStage(Pipeline.Stage.WB);

    if (pipe.isEmpty(Pipeline.Stage.WB)) {
      return;
    }

    // Do not execute the WB() method if the current instruction is a terminating instruction
    // and there is either some instruction in the FP pipeline or a non-BUBBLE instruction in
    // the MEM stage, which should only ever come from the FP pipeline, given that once a
    // terminating instruction enters the pipeline the CPU stops fetching instructions.
    // This corner case is necessary to handle out-of-order termination for FP instructions.
    boolean shouldWB = true;
    if (terminating.contains(pipe.WB().getRepr().getHexString()) &&
            (!fpPipe.isEmpty() || !pipe.isBubble(Pipeline.Stage.MEM))) {
      shouldWB = false;
    }

    if (!pipe.isBubble(Pipeline.Stage.WB)) {
      instructions++;
    }

    if (shouldWB) {
      logger.info("Executing WB() for " + pipe.WB());
      pipe.WB().WB();
    }

    // Move the instruction in WB out of the pipeline.
    logger.info("Instruction " + pipe.WB() + " has been completed. Removing it.");
    pipe.setWB(null);

    //if the pipeline is empty and it is into the stopping state (because a long latency instruction was executed) we can halt the cpu when computations finished
    if (isPipelinesEmpty() && getStatus() == CPUStatus.STOPPING) {
      logger.info("Pipeline is empty and we are in STOPPING --> going to HALTED.");
      throw new HaltException();
    }
  }

  private void stepMEM() throws NotAlignException, IrregularWriteOperationException, MemoryElementNotFoundException, AddressErrorException, IrregularStringOfBitsException {
    changeStage(Pipeline.Stage.MEM);

    if (!pipe.isEmpty(Pipeline.Stage.MEM)) {
      logger.info("Executing MEM() for " + pipe.MEM());
      pipe.MEM().MEM();
    }

    logger.info("Moving " + pipe.MEM() + " to WB");
    pipe.setWB(pipe.MEM());
    pipe.setMEM(null);
  }

  private Optional<String> stepEX() throws SynchronousException, NotAlignException, TwosComplementSumException, IrregularWriteOperationException, AddressErrorException, IrregularStringOfBitsException {
    changeStage(Pipeline.Stage.EX);

    // Used for exception handling
    boolean masked = config.getBoolean(ConfigKey.SYNC_EXCEPTIONS_MASKED);
    boolean terminate = config.getBoolean(ConfigKey.SYNC_EXCEPTIONS_TERMINATE);

    // Code of the synchronous exception that happens in EX.
    Optional<String> syncex = Optional.empty();

    // If the FPU has one completed instruction, that one should be executed. Otherwise, the instruction in the EX
    // stage should be executed.
    InstructionInterface completedFpInstruction = fpPipe.getCompletedInstruction();

    // Actual instruction to execute this cycle.
    InstructionInterface toExecute = pipe.EX();
    if (completedFpInstruction != null) {
      // If there is a completed FP instruction, we give precedence to that one.
      toExecute = completedFpInstruction;

      // A structural stall has to be raised if the EX stage contains an instruction different from a bubble.
      if (!pipe.isEmptyOrBubble(Pipeline.Stage.EX)) {
        memoryStalls++;
      }
    }

    // Execute the instruction, and handle synchronous exceptions.
    if (toExecute != null) {
      try {
        logger.info("Executing EX() for " + toExecute);
        toExecute.EX();
      } catch (SynchronousException e) {
        if (masked) {
          logger.info("[EXCEPTION] [MASKED] " + e.getCode());
        } else {
          if (terminate) {
            logger.info("Terminating due to an unmasked exception");
            throw new SynchronousException(e.getCode());
          } else {
            // We must complete this cycle, but we must notify the user.
            // If the syncex contains a string, the CPU code will throw
            // the exception at the end of the step
            syncex = Optional.of(e.getCode());
          }
        }
      }
    }

    logger.info("Moving " + toExecute + " to MEM");
    pipe.setMEM(toExecute);
    if (completedFpInstruction == null) {
      pipe.setEX(null);
    }
    // Shift instructions in the fpPipe.
    fpPipe.step();

    // Return the code of the synchronous exception (if any).
    return syncex;
  }

  // Returns true if there is a RAW conflict, false otherwis3. See docs for Instruction.ID()
  // for an explanation of why it is the case.
  private boolean stepID() throws IntegerOverflowException, TwosComplementSumException, WAWException, IrregularStringOfBitsException, FPInvalidOperationException, BreakException, IrregularWriteOperationException, JumpException, FPDividerNotAvailableException, FPFunctionalUnitNotAvailableException, EXNotAvailableException {
    changeStage(Pipeline.Stage.ID);

    if (pipe.isEmpty(Pipeline.Stage.ID)) {
      return false;
    }

    boolean isFP = FPPipeline.Constants.fparithmetic.contains(pipe.get(Pipeline.Stage.ID).getName());

    // Check if the desired unit (FP or not) is available.
    if (isFP && (fpPipe.putInstruction(pipe.ID(), true) != 0)) {
      if (pipe.ID().getName().compareToIgnoreCase("DIV.D") == 0) {
        throw new FPDividerNotAvailableException();
      } else {
        throw new FPFunctionalUnitNotAvailableException();
      }
    } else if (!isFP && (!pipe.isEmpty(Pipeline.Stage.EX) && !pipe.isBubble(Pipeline.Stage.EX))) {
      throw new EXNotAvailableException();
    }

    logger.info("Executing ID() for " + pipe.ID());
    // Can change the CPU status from RUNNING to STOPPING.
    boolean rawException = false;
    try {
      rawException = pipe.ID().ID();
    } catch (StoppingException e) {
      logger.info("Stopping CPU due to SYSCALL (" + pipe.ID().hashCode() + ")");
      setStatus(CPU.CPUStatus.STOPPING);
    }
    if (rawException) {
      return true;
    }

    if (isFP) {
      logger.info("Moving " + pipe.ID() + " to the FP pipeline.");
      fpPipe.putInstruction(pipe.ID(), false);
    } else {
      logger.info("Moving " + pipe.ID() + " to EX");
      pipe.setEX(pipe.ID());
    }

    pipe.setID(null);
    return false;
  }

  private void stepIF() throws IrregularStringOfBitsException, IrregularWriteOperationException, BreakException {
    // We don't have to execute any methods, but we must get the new
    // instruction from the symbol table.
    changeStage(Pipeline.Stage.IF);

    logger.info("CPU Status: " + status.name());

    boolean breaking = false;
    if (status == CPUStatus.RUNNING) {
      if (!pipe.isEmpty(Pipeline.Stage.IF)) {  //rispetto a dinmips scambia le load con le IF
        try {
          logger.info("Executing IF() for " + pipe.IF());
          pipe.IF().IF();
        } catch (BreakException exc) {
          breaking = true;
        }
      }

      logger.info("Moving " + pipe.IF() + " to ID");
      pipe.setID(pipe.IF());

      InstructionInterface next_if = mem.getInstruction(pc);
      logger.info("Fetched new instruction " + next_if);

      old_pc.writeDoubleWord((pc.getValue()));
      pc.writeDoubleWord((pc.getValue()) + 4);
      logger.info("New Program Counter value: " + pc.toString());
      logger.info("Putting " + next_if + "in IF.");
      pipe.setIF(next_if);
    } else {
      pipe.setID(bubble);
    }

    if (breaking) {
      logger.info("Re-throwing the BREAK exception");
      throw new BreakException();
    }
  }

  /** This method resets the CPU components (GPRs, memory,statistics,
   *   PC, pipeline and Symbol table).
   *   It resets also the Dinero Tracefile object associated to the current
   *   CPU.
   */
  public void reset() {
    // Reset CPU state.
    setStatus(CPUStatus.READY);
    cycles = 0;
    instructions = 0;
    RAWStalls = 0;
    WAWStalls = 0;
    dividerStalls = 0;
    funcUnitStalls = 0;
    exStalls = 0;
    memoryStalls = 0;

    // Reset registers.
    for (int i = 0; i < 32; i++) {
      gpr[i].reset();
    }

    //reset FPRs
    for (int i = 0; i < 32; i++) {
      fpr[i].reset();
    }


    try {
      // Reset the FCSR condition codes.
      for (int cc = 0; cc < 8; cc++) {
        setFCSRConditionCode(cc, 0);
      }

      // Reset the FCSR flags.
      setFCSRFlags("V", 0);
      setFCSRFlags("O", 0);
      setFCSRFlags("U", 0);
      setFCSRFlags("Z", 0);

      // Reset the FCSR cause bits.
      setFCSRCause("V", 0);
      setFCSRCause("O", 0);
      setFCSRCause("U", 0);
      setFCSRCause("Z", 0);
    } catch (IrregularStringOfBitsException ex) {
      ex.printStackTrace();
    }

    LO.reset();
    HI.reset();

    // Reset program counter
    pc.reset();
    old_pc.reset();

    // Reset the memory.
    mem.reset();

    // Reset pipeline
    pipe.clear();
    // Reset FP pipeline
    fpPipe.reset();

    logger.info("CPU Resetted");
  }

  /** Test method that returns a string containing the status of the pipeline.
   * @return string representation of the pipeline status
   */
  public String pipeLineString() {
    String s = "";
    s += "IF:\t" + pipe.IF() + "\n";
    s += "ID:\t" + pipe.ID() + "\n";
    s += "EX:\t" + pipe.EX() + "\n";
    s += "MEM:\t" + pipe.MEM() + "\n";
    s += "WB:\t" + pipe.WB() + "\n";

    return s;
  }

  /** Test method that returns a string containing the values of every
   * register.
   * @return string representation of the register file contents
   */
  public String gprString() {
    StringBuilder s = new StringBuilder();

    int i = 0;

    for (Register r : gpr) {
      s.append("Register ").append(i++).append(":\t").append(r.toString()).append("\n");
    }

    return s.toString();
  }

  public boolean isEnableForwarding() {
    return config.getBoolean(ConfigKey.FORWARDING);
  }

  /** Test method that returns a string containing the values of every
   * FPR.
   * @return a string
   */
  public String fprString() {
    StringBuilder s = new StringBuilder();
    int i = 0;

    for (RegisterFP r: fpr) {
      s.append("FP Register ").append(i++).append(":\t").append(r.toString()).append("\n");
    }

    return s.toString();
  }

  private void configFPExceptionsAndRM() {
    try {
      FCSR.setFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION, config.getBoolean(ConfigKey.FP_INVALID_OPERATION));
      FCSR.setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, config.getBoolean(ConfigKey.FP_OVERFLOW));
      FCSR.setFPExceptions(FCSRRegister.FPExceptions.UNDERFLOW, config.getBoolean(ConfigKey.FP_UNDERFLOW));
      FCSR.setFPExceptions(FCSRRegister.FPExceptions.DIVIDE_BY_ZERO, config.getBoolean(ConfigKey.FP_DIVIDE_BY_ZERO));

      //setting the rounding mode
      if (config.getBoolean(ConfigKey.FP_NEAREST)) {
        FCSR.setFCSRRoundingMode(FCSRRegister.FPRoundingMode.TO_NEAREST);
      } else if (config.getBoolean(ConfigKey.FP_TOWARDS_ZERO)) {
        FCSR.setFCSRRoundingMode(FCSRRegister.FPRoundingMode.TOWARD_ZERO);
      } else if (config.getBoolean(ConfigKey.FP_TOWARDS_PLUS_INFINITY)) {
        FCSR.setFCSRRoundingMode(FCSRRegister.FPRoundingMode.TOWARDS_PLUS_INFINITY);
      } else if (config.getBoolean(ConfigKey.FP_TOWARDS_MINUS_INFINITY)) {
        FCSR.setFCSRRoundingMode(FCSRRegister.FPRoundingMode.TOWARDS_MINUS_INFINITY);
      }
    } catch (IrregularStringOfBitsException ex) {
      Logger.getLogger(CPU.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public String toString() {
    String s = "";
    s += mem.toString() + "\n";
    s += pipeLineString();
    s += gprString();
    s += fprString();
    return s;
  }

  /** Private class, representing the R0 register */
  // TODO: DEVE IMPOSTARE I SEMAFORI?????
  private class R0 extends Register {
    public R0() {
      super("R0");
    }
    public long getValue() {
      return (long) 0;
    }
    public String getBinString() {
      return "0000000000000000000000000000000000000000000000000000000000000000";
    }
    public String getHexString() {
      return "0000000000000000";
    }
    public void setBits(String bits, int start) {
    }
    public void writeByteUnsigned(int value) {}
    public void writeByte(int value, int offset) {}
    public void writeHalfUnsigned(int value) {}
    public void writeHalf(int value) {}
    public void writeHalf(int value, int offset) {}
    public void writeWordUnsigned(long value) {}
    public void writeWord(int value) {}
    public void writeWord(long value, int offset) {}
    public void writeDoubleWord(long value) {}

  }
}
