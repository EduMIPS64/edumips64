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

package edumips64.core;
import edumips64.core.fpu.*;
import java.util.*;
import java.util.logging.Logger;
import edumips64.core.is.*;
import edumips64.utils.*;

/** This class models a MIPS CPU with 32 64-bit General Purpose Registers.
*  @author Andrea Spadaccini, Simona Ullo, Antonella Scandura, Massimo Trubia (FPU modifications)
*/
public class CPU {
	private Memory mem;
	private Register[] gpr;
    private static final Logger logger = Logger.getLogger(CPU.class.getName());

    /** FPU Elements*/	
	private RegisterFP[] fpr;
	public static enum FPExceptions {INVALID_OPERATION,DIVIDE_BY_ZERO,UNDERFLOW,OVERFLOW};
	public static enum FPRoundingMode { TO_NEAREST, TOWARD_ZERO,TOWARDS_PLUS_INFINITY,TOWARDS_MINUS_INFINITY};
	private FCSRRegister FCSR;
	public static List<String> knownFPInstructions; // set of Floating point instructions that must pass through the FPU pipeline
	private FPPipeline fpPipe;
	private List<String> terminatingInstructionsOPCodes;
	private long serialNumberSeed; //is used for numerating instructions so that they are unambiguous into the pipelines
	
	/** Program Counter*/
	private Register pc,old_pc;
	private Register LO,HI;
	
	/** Pipeline status*/
	public enum PipeStatus {IF, ID, EX, MEM, WB};
	
	/** CPU status.
	 * 	READY - the CPU has been initialized but the symbol table hasn't been
	 *  already filled by the Parser. This means that you can't call the step()
	 *  method, or you'll get a StoppedCPUException.
	 *
	 * 	RUNNING - the CPU is executing a program, you can call the step()
	 * 	method, and the CPU will fetch additional instructions from the symbol
	 * 	table
	 *
	 * 	STOPPING - the HALT instruction has entered in the pipeline. This means
	 * 	that no additional instructions must be fetched but the instructions
	 * 	that are already in the pipeline must be executed. THe step() method can
	 * 	be called, but won't fetch any other instruction
	 *
	 * 	HALTED - the HALT instruction has passed the WB state, and the step()
	 * 	method can't be executed.
	 * */
	public enum CPUStatus {READY, RUNNING, STOPPING, HALTED};
	private CPUStatus status;
	
	/** CPU pipeline, each status contains an Instruction object*/
	private Map<PipeStatus, Instruction> pipe;
	private SymbolTable symTable;
	
	/** The current status of the pipeline.*/
	private PipeStatus currentPipeStatus;
	
	/** The code and data sections limits*/
	public static final int CODELIMIT = 1024;	// bus da 12 bit (2^12 / 4)
	public static final int DATALIMIT = 512;	// bus da 12 bit (2^12 / 8)
	
	
	private static CPU cpu;
	
	/** Statistics */
	private int cycles, instructions, RAWStalls, WAWStalls, dividerStalls,funcUnitStalls,memoryStalls,exStalls;
	
	/** Static initializer */
	static {
		cpu = null;
	}
	private CPU() {
		//instructions enumerating
		serialNumberSeed=0;
		// To avoid future singleton problems
		Instruction dummy = Instruction.buildInstruction("BUBBLE");

		logger.info("Creating the CPU...");
		cycles = 0;
		status = CPUStatus.READY;
		mem = Memory.getInstance();
		logger.info("Got Memory instance..");
		symTable = SymbolTable.getInstance();
		logger.info("Got SymbolTable instance..");

		// Registers initialization
		gpr = new Register[32];
		gpr[0] = new R0();
		for(int i=1;i<32;i++)
			gpr[i] = new Register();
		pc = new Register();
		old_pc = new Register();
		LO=new Register();
		HI=new Register();
		
		//Floating point registers initialization
		fpr=new RegisterFP[32];
		for(int i=0;i<32;i++)
			fpr[i]=new RegisterFP();
		FCSR = new FCSRRegister();
		fpPipe= new FPPipeline();
		fpPipe.reset();
		
		
		// Pipeline initialization
		pipe = new HashMap<PipeStatus, Instruction>();
		clearPipe();
		currentPipeStatus = PipeStatus.IF;
		

		//FPU initialization
		FPUConfigurator conf=new FPUConfigurator();
		knownFPInstructions=conf.getFPArithmeticInstructions();
		terminatingInstructionsOPCodes=conf.getTerminatingInstructions();

		logger.info("CPU Created.");
		
	}
	
	
// SETTING PROPERTIES ------------------------------------------------------------------
	/** Sets the CPU status.
	 *  @param status a CPUStatus value
	 */
	public  void setStatus(CPUStatus status) {
		this.status = status;
	}
	
	/** Sets the flag bits of the FCSR
	* @param tag a string value between  V  Z O U I
	* @param value a binary value
	 */
	public void setFCSRFlags(String tag,int value) throws IrregularStringOfBitsException{
		FCSR.setFCSRFlags(tag,value);
	}
	
	
	/** Sets the selected FCC bit of the FCSR 
	 * @param cc condition code is an int value in the range [0,7]
	 * @condition the binary value of the relative bit
	 */
	public void setFCSRConditionCode(int cc, int condition) throws IrregularStringOfBitsException
	{
		FCSR.setFCSRConditionCode(cc,condition);
	}
	
//GETTING PROPERTIES -----------------------------------------------------------------
	
	/** Gets the CPU status
	 *  @return status a CPUStatus value representing the current CPU status
	 */
	public CPUStatus getStatus() {
		return status;
	}

    private void clearPipe()
    {
        pipe.put(PipeStatus.IF, null);
        pipe.put(PipeStatus.ID, null);
        pipe.put(PipeStatus.EX, null);
        pipe.put(PipeStatus.MEM, null);
        pipe.put(PipeStatus.WB, null);
    } 

	public static CPU getInstance()
    {
		if(cpu == null) 
			cpu = new CPU();
		return cpu;
	}
	
	public Register[] getRegisters() {
		return gpr;
	}
	
	public RegisterFP[] getRegistersFP() {
		return fpr;
	}
	
	public Memory getMemory() {
		return mem;
	}
	
	public SymbolTable getSymbolTable() {
		return symTable;
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
	 *			ADDER [1,4], MULTIPLIER [1,7], DIVIDER [any] */
	public boolean isFuncUnitFilled(String funcUnit, int stage) {
		return fpPipe.isFuncUnitFilled(funcUnit, stage);
	}
	
	/** Returns true if the pipeline is empty. In this case, if CPU is in stopping state
	 *  we can halt the pipeline. The sufficient condition in order to return true is that fpPipe doesn't work
	 *	and it hadn't issued any instrution now in the MEM stage */
	public boolean isPipelinesEmpty() {
		boolean empty=true;
		empty=empty && (pipe.get(PipeStatus.MEM)==null || pipe.get(PipeStatus.MEM).getName()==" ");
		empty=empty && fpPipe.isEmpty();
		return empty;
	}
	
	/** Returns the instruction of the specified functional unit , null if it is empty.
	 *  No controls are carried out on the legality of parameters, for mistaken parameters null is returned
	 *  @param funcUnit The functional unit to check. Legal values are "ADDER", "MULTIPLIER", "DIVIDER"
	 *  @param stage The integer that refers to the stage of the functional unit.
	 *			ADDER [1,4], MULTIPLIER [1,7], DIVIDER [any] */
	
	public Instruction getInstructionByFuncUnit(String funcUnit, int stage) {
		return fpPipe.getInstructionByFuncUnit(funcUnit,stage);
	}
	
	/** Gets a binary string representing the Floating Point Control Status Register*/
	public String getFCSR(){
		return FCSR.getBinString();
	}

	/** Gets the selected FCC bit of the FCSR 
	 * @param cc condition code is an int value in the range [0,7]
	 */
	public int getFCSRConditionCode(int cc){
		return FCSR.getFCSRConditionCode(cc);
	}
	
	/** Gets the current rounding mode readeng the FCSR
	 * @return the rounding mode */
	public FPRoundingMode getFCSRRoundingMode(){
		return FCSR.getFCSRRoundingMode();
	}
	
	/** Gets the current computing step of the divider*/
	public int getDividerCounter() {
		return fpPipe.getDividerCounter();
	}
	
	/** Gets the integer pipeline
	 *  @return an HashMap 	  
	 */
	public Map<PipeStatus, Instruction> getPipeline() {
		return pipe;
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
	 * @ return an integer
	 */
	public int getStructuralStallsDivider(){
		return dividerStalls;
	}
	
	/** Returns the number of Structural Stalls (Memory not available) that happened inside the pipeline
	 * @ return an integer
	 */
	public int getStructuralStallsMemory(){
		return memoryStalls;
	}
	
	/** Returns the number of Structural Stalls (EX not available) that happened inside the pipeline
	 * @ return an integer
	 */
	public int getStructuralStallsEX(){
		return exStalls;
	}
	
	/** Returns the number of Structural Stalls (FP Adder and FP Multiplier not available) that happened inside the pipeline
	 * @ return an integer
	 */
	public int getStructuralStallsFuncUnit(){
		return funcUnitStalls;
	}
	
	/** Returns a legal serial number in order to numerate a new instruction
	 */
	public long getSerialNumber(){
		serialNumberSeed++;
		return serialNumberSeed-1;
	}
	
	
	/* Gets the stage's name of the instruction passed as serialNumber between this values
	 * A1,A2,A3,A4,M1,M2,M3,M4,M5,M6,M7,DIVXX in which XX means the FP Divider Counter.
	 * If the fpPipe doesn't contain that instruction null is returned
	public String getFPInstructionStage(long serialNumber) {
		return fpPipe.getInstructionStage(serialNumber);
	}
	*/
		
	/** Gets the floating point unit enabled exceptions
	 *  @return true if exceptionName is enabled, false in the other case
	 */
	public boolean getFPExceptions(FPExceptions exceptionName) {
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
	public Register getHI(){
		return HI;
	}
	
	/** Gets the structural stall counter
	 *@return the memory stall counter
	 */
	public int getMemoryStalls(){
		return memoryStalls;
	}
	
	/** Gets the list of terminating instructions*/
	public List<String> getTerminatingInstructions(){
		return terminatingInstructionsOPCodes;
	}

    /** This method performs a single pipeline step
    * @throws RAWHazardException when a RAW hazard is detected
    */
    public void step() throws IntegerOverflowException, AddressErrorException, HaltException, IrregularWriteOperationException, StoppedCPUException, MemoryElementNotFoundException, IrregularStringOfBitsException, TwosComplementSumException, SynchronousException, BreakException, NotAlignException, FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, WAWException, MemoryNotAvailableException, FPDividerNotAvailableException, FPFunctionalUnitNotAvailableException {
		/* The integer "breaking" is used to keep track of the BREAK
		 * instruction. When the BREAK instruction enters ID, the BreakException
		 * is thrown. We continue the normal cpu step flow, and at the end of
		 * this flow the BreakException is re-thrown.
		 */
		
		boolean SIMUL_MODE_ENABLED=true;
		boolean SIMUL_MODE_DISABLED=false;
		int breaking = 0;
		// Used for exception handling
		boolean masked = (Boolean)Config.get("syncexc-masked");
		boolean terminate = (Boolean)Config.get("syncexc-terminate");
		
		FCSR.setFPExceptions(CPU.FPExceptions.INVALID_OPERATION,(Boolean)Config.get("INVALID_OPERATION"));
		FCSR.setFPExceptions(CPU.FPExceptions.OVERFLOW,(Boolean)Config.get("OVERFLOW"));
		FCSR.setFPExceptions(CPU.FPExceptions.UNDERFLOW,(Boolean)Config.get("UNDERFLOW"));
		FCSR.setFPExceptions(CPU.FPExceptions.DIVIDE_BY_ZERO,(Boolean)Config.get("DIVIDE_BY_ZERO"));
		
		//setting the rounding mode
		if((Boolean)Config.get("NEAREST"))
			FCSR.setFCSRRoundingMode(FPRoundingMode.TO_NEAREST);
		else if((Boolean)Config.get("TOWARDZERO"))
			FCSR.setFCSRRoundingMode(FPRoundingMode.TOWARD_ZERO);
		else if((Boolean)Config.get("TOWARDS_PLUS_INFINITY"))
			FCSR.setFCSRRoundingMode(FPRoundingMode.TOWARDS_PLUS_INFINITY);
		else if((Boolean)Config.get("TOWARDS_MINUS_INFINITY"))
			FCSR.setFCSRRoundingMode(FPRoundingMode.TOWARDS_MINUS_INFINITY);
		
		String syncex = null;
		
		if(status != CPUStatus.RUNNING && status != CPUStatus.STOPPING)
			throw new StoppedCPUException();
		try {
			logger.info("Starting cycle " + ++cycles + "\n---------------------------------------------");
			currentPipeStatus = PipeStatus.WB; 

			// Let's execute the WB() method of the instruction located in the 
			// WB pipeline status
			if(pipe.get(PipeStatus.WB)!=null) {
				boolean terminatorInstrInWB= terminatingInstructionsOPCodes.contains(pipe.get(PipeStatus.WB).getRepr().getHexString());
				//we have to execute the WB method only if some conditions occur
				boolean notWBable=false;
				//the current instruction in WB is a terminating instruction and the fpPipe is working
				notWBable=notWBable || (terminatorInstrInWB && !fpPipe.isEmpty());
				//the current instruction in WB is a terminating instruction, the fpPipe doesn't work because it has just issued an instruction and it is in the MEM stage
				notWBable=notWBable || (terminatorInstrInWB && fpPipe.isEmpty() && pipe.get(PipeStatus.MEM).getName()!=" ");
				if(!notWBable)
					pipe.get(PipeStatus.WB).WB();
				
				if(!pipe.get(PipeStatus.WB).getName().equals(" "))
					instructions++;
				
				//if the pipeline is empty and it is into the stopping state (because a long latency instruction was executed) we can halt the cpu when computations finished
				if(isPipelinesEmpty() && getStatus()==CPUStatus.STOPPING) {
					setStatus(CPU.CPUStatus.HALTED);
					throw new HaltException();
				}
				
			}
			
			// We put null in WB, in order to avoid that an exception thrown in
			// the next instruction leaves the already completed instruction in
			// the WB pipeline state
			pipe.put(PipeStatus.WB, null);
			
			// MEM
			currentPipeStatus = PipeStatus.MEM;
			if(pipe.get(PipeStatus.MEM)!=null)
				pipe.get(PipeStatus.MEM).MEM();
			pipe.put(PipeStatus.WB, pipe.get(PipeStatus.MEM));
			pipe.put(PipeStatus.MEM,null);
			
			//if there will be a stall because a lot of instructions would fill the MEM stage, the EX() method cannot be called
			//because the integer instruction in EX cannot be moved
			// EX
		if(fpPipe.getInstruction(SIMUL_MODE_ENABLED)==null) {
			try {
				// Handling synchronous exceptions
				currentPipeStatus = PipeStatus.EX;
				if(pipe.get(PipeStatus.EX)!=null)
					pipe.get(PipeStatus.EX).EX();
			} catch (SynchronousException e) {
				if(masked)
					logger.info("[EXCEPTION] [MASKED] " + e.getCode());
				else {
					if(terminate) {
						logger.info("Terminating due to an unmasked exception");
						throw new SynchronousException(e.getCode());
                    } else
                        // We must complete this cycle, but we must notify the user.
                        // If the syncex string is not null, the CPU code will throw
                        // the exception at the end of the step
                        syncex = e.getCode();
				}
			}
			pipe.put(PipeStatus.MEM, pipe.get(PipeStatus.EX));
			pipe.put(PipeStatus.EX,null);
		} else {
			//a structural stall has to be raised if the EX stage contains an instruction different from a bubble or other fu's contain instructions (counter of structural stalls must be incremented)
			if((pipe.get(PipeStatus.EX)!=null && !(pipe.get(PipeStatus.EX).getName().compareTo(" ")==0)) || fpPipe.getNReadyToExitInstr()>1)
				memoryStalls++;	

			//the fpPipe is issuing an instruction and the EX method has to be called on it
			Instruction instr;
			//call EX
			instr=fpPipe.getInstruction(SIMUL_MODE_DISABLED);

			try {
				// Handling synchronous exceptions
				currentPipeStatus = PipeStatus.EX;
				instr.EX();
			} catch (SynchronousException e) {
				if(masked)
					logger.info("[MASKED] " + e.getCode());
				else {
					if(terminate) {
						logger.info("Terminating due to an unmasked exception");
						throw new SynchronousException(e.getCode());
                    } else
                        // We must complete this cycle, but we must notify the user.
                        // If the syncex string is not null, the CPU code will throw
                        // the exception at the end of the step
                        syncex = e.getCode();
					}
				}
				pipe.put(PipeStatus.MEM, instr);
			}
			
			//shifting instructions in the fpPipe
			fpPipe.step();
			
			// ID
			currentPipeStatus = PipeStatus.ID;
			if(pipe.get(PipeStatus.ID)!=null) {
				//if an FP instruction fills the ID stage a checking for InputStructuralStall must be performed before the ID() invocation.
				//This operation is carried out by checking if the fpPipe could accept the instruction we would insert in it (2nd condition)
				if(knownFPInstructions.contains(pipe.get(PipeStatus.ID).getName())) {
					//it is an FPArithmetic and it must be inserted in the fppipe
					//the fu is free
					if(fpPipe.putInstruction(pipe.get(PipeStatus.ID),SIMUL_MODE_ENABLED)==0) {
						if(fpPipe.isEmpty() || (!fpPipe.isEmpty()/* && !terminatingInstructionsOPCodes.contains(pipe.get(PipeStatus.ID).getRepr().getHexString())*/))
							pipe.get(PipeStatus.ID).ID();
						fpPipe.putInstruction(pipe.get(PipeStatus.ID),SIMUL_MODE_DISABLED);
						pipe.put(PipeStatus.ID,null);
					} else //the fu is filled by another instruction
					{
						if(pipe.get(PipeStatus.ID).getName().compareToIgnoreCase("DIV.D")==0)
							throw new FPDividerNotAvailableException();
						else
							throw new FPFunctionalUnitNotAvailableException();
					}
				}
				//if an integer instruction or an FP instruction that will not pass through the FP pipeline fills the ID stage a checking for
				//InputStructuralStall (second type) must be performed. We must control if the EX stage is filled by another instruction, in this case we have to raise a stall
				else {
					if(pipe.get(PipeStatus.EX)==null || /*testing*/ pipe.get(PipeStatus.EX).getName().compareTo(" ")==0) {
						if(fpPipe.isEmpty() || (!fpPipe.isEmpty()/* && !terminatingInstructionsOPCodes.contains(pipe.get(PipeStatus.ID).getRepr().getHexString())*/))
							pipe.get(PipeStatus.ID).ID();
						pipe.put(PipeStatus.EX,pipe.get(PipeStatus.ID));
						pipe.put(PipeStatus.ID,null);
					}
					//the EX stage is full
					else {
						throw new EXNotAvailableException();
					}
					
					
				}
				
			}
			
			// IF
			// We don't have to execute any methods, but we must get the new
			// instruction from the symbol table.
			currentPipeStatus = PipeStatus.IF;
			
			if(status == CPUStatus.RUNNING) {
				if(pipe.get(PipeStatus.IF) != null) { //rispetto a dinmips scambia le load con le IF
					try {
						pipe.get(PipeStatus.IF).IF();
					} catch (BreakException exc) {
						breaking = 1;
						logger.info("breaking = 1");
					}
				}
				pipe.put(PipeStatus.ID, pipe.get(PipeStatus.IF));
				pipe.put(PipeStatus.IF, mem.getInstruction(pc));
				old_pc.writeDoubleWord((pc.getValue()));
				pc.writeDoubleWord((pc.getValue())+4);
			} else {
				pipe.put(PipeStatus.ID, Instruction.buildInstruction("BUBBLE"));
			}
			if(breaking == 1) {
				breaking = 0;
				logger.info("Re-thrown the exception");
				throw new BreakException();
			}
			if(syncex != null)
				throw new SynchronousException(syncex);
			
			logger.info("\n"+cpu.getStatus());

		} catch(JumpException ex) {
            try {
                if(pipe.get(PipeStatus.IF) != null) //rispetto a dimips scambia le load con le IF
                        pipe.get(PipeStatus.IF).IF();
            }
            catch(BreakException bex) {
				logger.info("Caught a BREAK after a Jump: ignoring it.");
            }

			// A J-Type instruction has just modified the Program Counter. We need to
			// put in the IF state the instruction the PC points to
			pipe.put(PipeStatus.IF, mem.getInstruction(pc));
			pipe.put(PipeStatus.EX, pipe.get(PipeStatus.ID));
			pipe.put(PipeStatus.ID, Instruction.buildInstruction("BUBBLE"));
			old_pc.writeDoubleWord((pc.getValue()));
			pc.writeDoubleWord((pc.getValue())+4);
			if(syncex != null)
				throw new SynchronousException(syncex);
			
		} catch(RAWException ex) {
			if(currentPipeStatus == PipeStatus.ID)
				pipe.put(PipeStatus.EX, Instruction.buildInstruction("BUBBLE"));
			RAWStalls++;
			if(syncex != null)
				throw new SynchronousException(syncex);
			
		}
		catch(WAWException ex) {
			logger.info(fpPipe.toString());			
			if(currentPipeStatus == PipeStatus.ID)
				pipe.put(PipeStatus.EX, Instruction.buildInstruction("BUBBLE"));
			
			WAWStalls++;
			if(syncex != null)
				throw new SynchronousException(syncex);
		} catch(FPDividerNotAvailableException ex) {
			if(currentPipeStatus == PipeStatus.ID)
				pipe.put(PipeStatus.EX, Instruction.buildInstruction("BUBBLE"));
			
			dividerStalls++;
			if(syncex != null)
				throw new SynchronousException(syncex);
			
		} catch(FPFunctionalUnitNotAvailableException ex) {
			if(currentPipeStatus == PipeStatus.ID)
				pipe.put(PipeStatus.EX, Instruction.buildInstruction("BUBBLE"));
			
			funcUnitStalls++;
			if(syncex != null)
				throw new SynchronousException(syncex);
		} catch(EXNotAvailableException ex) {
			exStalls++;
			if(syncex != null)
				throw new SynchronousException(syncex);
		}
		
		catch(SynchronousException ex) {
			logger.info("Exception: " + ex.getCode());
			throw ex;
		}
		catch(HaltException ex) {
			pipe.put(PipeStatus.WB, null);
			throw ex;
		}
	}
	
	
	/** This method resets the CPU components (GPRs, memory,statistics,
	 *   PC, pipeline and Symbol table).
	 *   It resets also the Dinero Tracefile object associated to the current
	 *   CPU.
	 */
	public void reset() {
		// Reset stati della CPU
		status = CPUStatus.READY;
		cycles = 0;
		instructions = 0;
		RAWStalls = 0;
		WAWStalls = 0;
		dividerStalls = 0;
		funcUnitStalls = 0;
		exStalls=0;
		memoryStalls = 0;
		serialNumberSeed=0;
		
		// Reset dei registri
		for(int i = 0; i < 32; i++)
			gpr[i].reset();
		
		//reset FPRs
		for(int i=0;i<32;i++)
			fpr[i].reset();
		
		
		try {
			//reset the FCSR (only condition codes)
			for(int cc=0;cc<8;cc++)
				setFCSRConditionCode(cc,0);
			//reset the FCSR flags
			setFCSRFlags("V",0);
			setFCSRFlags("O",0);
			setFCSRFlags("U",0);
			setFCSRFlags("Z",0);
		} catch (IrregularStringOfBitsException ex) {
			ex.printStackTrace();
		}

		
		LO.reset();
		HI.reset();
		
		// Reset program counter
		pc.reset();
		old_pc.reset();
		
		// Reset memoria
		mem.reset();
		
		// Reset pipeline
		clearPipe();
		// Reset FP pipeline
		fpPipe.reset();
		
		// Reset Symbol table
		symTable.reset();
		
		// Reset tracefile
		Dinero.getInstance().reset();

		logger.info("CPU Resetted");
    }

	/** Test method that returns a string containing the status of the pipeline.
	 * @return string representation of the pipeline status
	 */
	public String pipeLineString() {
		String s = new String();
		s += "IF:\t" + pipe.get(PipeStatus.IF) + "\n";
		s += "ID:\t" + pipe.get(PipeStatus.ID) + "\n";
		s += "EX:\t" + pipe.get(PipeStatus.EX) + "\n";
		s += "MEM:\t" + pipe.get(PipeStatus.MEM) + "\n";
		s += "WB:\t" + pipe.get(PipeStatus.WB) + "\n";
		
		return s;
	}
	
	/** Test method that returns a string containing the values of every
	 * register.
	 * @return string representation of the register file contents
	 */
	public String gprString() {
		String s = new String();
		
		int i = 0;
		for(Register r : gpr) 
			s += "Register " + i++ + ":\t" + r.toString() + "\n";
		
		return s;
	}
	
	/** Test method that returns a string containing the values of every
	 * FPR.
	 * @returns a string
	 */
	public String fprString() {
		String s = new String();
		int i= 0;
		for(RegisterFP r: fpr)
			s+= "Registro " + i++ + ":\t" + r.toString() + "\n";
		return s;
	}
	
	public String toString() {
		String s = new String();
		s += mem.toString() + "\n";
		s += pipeLineString();
		s += gprString();
		s += fprString();
		return s;
	}
	
	/** Private class, representing the R0 register */
	// TODO: DEVE IMPOSTARE I SEMAFORI?????
	private class R0 extends Register {
		public long getValue() {
			return (long)0;
		}
		public String getBinString() {
			return "0000000000000000000000000000000000000000000000000000000000000000";
		}
		public String getHexString() {
			return "0000000000000000";
		}
		public void setBits(String bits, int start) {
		}
		public void writeByteUnsigned(int value){}
		public void writebyte(int value){}
		public void writeByte(int value,int offset){}
		public void writeHalfUnsigned(int value){}
		public void writeHalf(int value){}
		public void writeHalf(int value, int offset){}
		public void writeWordUnsigned(long value){}
		public void writeWord(int value){}
		public void writeWord(long value,int offset){}
		public void writeDoubleWord(long value){}
		
	}
}
