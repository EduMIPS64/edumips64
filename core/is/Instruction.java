/*
 * Instruction.java
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

package edumips64.core.is;


import edumips64.core.*;
import edumips64.core.fpu.*;
import edumips64.utils.*;
import java.util.*;
import java.util.logging.Logger;
import java.lang.Enum.*;
//debug
import edumips64.Main;
/**Abstract class: it provides all methods and attributes for each instruction type
 * 
 * @author Trubia Massimo, Russo Daniele
 */
public abstract class Instruction {
    
    protected BitSet32 repr;
    protected List<Integer> params;
    protected int paramCount;
    protected String syntax;
    protected String name;
    protected String comment;
    protected static Memory memory=Memory.getInstance();
    //protected static CPU cpu;
    protected Register[] TR; //is not static because each instruction has got its own registers
    protected RegisterFP[] TRfp;
    protected String fullname;
    protected static boolean enableForwarding=(Boolean)Config.get("forwarding");
    protected String label;
    protected static final Logger logger = Logger.getLogger(Instruction.class.getName());
    protected Long serialNumber;
    
    
    /** Creates a new instance of Instruction */
    public Instruction() {
	params=new LinkedList<Integer>();
        TR=new Register[5];
	TRfp=new RegisterFP[5];
        repr=new BitSet32();
        repr.reset(false);
        syntax=new String();
	//generating a serial number for the current instruction
	serialNumber=(Long)Config.get("serialNumber");;
	Config.set("serialNumber",serialNumber+1);
	//initialization of temporary registers
	for(int i=0;i<TR.length;i++)
	{
		TR[i]=new Register();
		TRfp[i]=new RegisterFP();
	}
    }
    
    
    /** <pre>
     *  Returns a BitSet32 holding the binary representation of the Instruction
     *  @return the Bitset32 representing the instruction 
     *  </pre>
     * */
    public BitSet32 getRepr() {
	    return repr;
    }
    
    
    /** 
     * Creates a new instance of an Instruction's subclass 
     * @param name string value to pass in order to instanciate an instruction object 
     * @return the instruction object 
     *
     */
     public static Instruction buildInstruction(String name){
        Instruction returnedObject=null;
	//If the name of the requested instruction has got a dot, the istruction is FP and an
	//underscore takes the place of the dot because classes names cannot contain dots
	name=name.replaceAll("\\.","_");
	for (InstructionEnumerator op : InstructionEnumerator.values()) {
	    if (op.name().equals(name)==true) {		    
		returnedObject=op.getObject();
                return returnedObject;
	    }
        }
	
        return returnedObject;
    }
    public enum InstructionEnumerator {
	
						//ALU R-Type 32-bits
						ADD  { Instruction getObject() { ADD newObject=new ADD();return newObject; } },
						ADDU { Instruction getObject() { ADDU newObject=new ADDU(); return newObject; } },
						SUB  { Instruction getObject() { SUB newObject=new SUB();return newObject;}},
					    SUBU { Instruction getObject() { SUBU newObject=new SUBU(); return newObject; } },
						DIV { Instruction getObject() { DIV newObject=new DIV(); return newObject; } },
						DIVU { Instruction getObject() { DIVU newObject=new DIVU(); return newObject; } },
						MULT { Instruction getObject() { MULT newObject=new MULT(); return newObject; } },
						MULTU { Instruction getObject() { MULTU newObject=new MULTU(); return newObject; } },
											
						//ALU I-Type 32-bits
						ADDI { Instruction getObject() { ADDI newObject=new ADDI(); return newObject; } },
						ADDIU{ Instruction getObject() { ADDIU newObject=new ADDIU(); return newObject; } },			

						//ALU Shifting 32-bits
						SLL { Instruction getObject() {SLL newObject=new SLL();return newObject;}},
						SLLV { Instruction getObject() {SLLV newObject=new SLLV();return newObject;}},
						SRA { Instruction getObject() {SRA newObject=new SRA();return newObject;}},
						SRAV { Instruction getObject() {SRAV newObject=new SRAV();return newObject;}},
						SRL { Instruction getObject() {SRL newObject=new SRL();return newObject;}},
						SRLV { Instruction getObject() {SRLV newObject=new SRLV();return newObject;}},
						
                        //ALU R-Type
                        AND   { Instruction getObject() { AND newObject=new AND(); return newObject; } },
						DADD  { Instruction getObject() { DADD newObject=new DADD();return newObject; } },
                        DADDU { Instruction getObject() { DADDU newObject=new DADDU(); return newObject; } },
                        DSUB  { Instruction getObject() { DSUB newObject=new DSUB();return newObject;}},
                        DSUBU { Instruction getObject() { DSUBU newObject=new DSUBU(); return newObject; } },
                        OR    { Instruction getObject() { OR newObject=new OR(); return newObject; } },
                        SLT  { Instruction getObject() { SLT newObject=new SLT();return newObject; } },
                        SLTU { Instruction getObject() { SLTU newObject=new SLTU(); return newObject; } },
                        XOR  { Instruction getObject() { XOR newObject=new XOR(); return newObject; } },
                        MOVN { Instruction getObject() { MOVN newObject=new MOVN(); return newObject; } },
                        MOVZ { Instruction getObject() { MOVZ newObject=new MOVZ(); return newObject; } },
                        DDIV { Instruction getObject() { DDIV newObject=new DDIV(); return newObject; } },
                        DDIVU { Instruction getObject() { DDIVU newObject=new DDIVU(); return newObject; } },
                        DMULT { Instruction getObject() { DMULT newObject=new DMULT(); return newObject; } },
                        DMULTU { Instruction getObject() { DMULTU newObject=new DMULTU(); return newObject; } },
                        MFLO { Instruction getObject() { MFLO newObject=new MFLO(); return newObject; } },
                        MFHI { Instruction getObject() { MFHI newObject=new MFHI(); return newObject; } },
                        
                        
                        //ALU I-Type
			ANDI  { Instruction getObject() { ANDI newObject=new ANDI();return newObject; } },
			DADDI { Instruction getObject() { DADDI newObject=new DADDI(); return newObject; } },
                        DADDUI{ Instruction getObject() { DADDUI newObject=new DADDUI(); return newObject; } },
                        DADDIU{ Instruction getObject() { DADDIU newObject=new DADDIU(); return newObject; } },
                        LUI   { Instruction getObject() { LUI newObject=new LUI(); return newObject; } },
                        ORI   { Instruction getObject() { ORI newObject=new ORI(); return newObject; } },
                        SLTI  { Instruction getObject() { SLTI newObject=new SLTI(); return newObject; } },
                        SLTIU { Instruction getObject() { SLTIU newObject=new SLTIU(); return newObject; } },
                        XORI  { Instruction getObject() { XORI newObject=new XORI(); return newObject; } },
                        //ALU Shifting 
                        DSLL { Instruction getObject() {DSLL newObject=new DSLL();return newObject;}},
                        DSLLV { Instruction getObject() {DSLLV newObject=new DSLLV();return newObject;}},
                        DSRA { Instruction getObject() {DSRA newObject=new DSRA();return newObject;}},
                        DSRAV { Instruction getObject() {DSRAV newObject=new DSRAV();return newObject;}},
                        DSRL { Instruction getObject() {DSRL newObject=new DSRL();return newObject;}},
                        DSRLV { Instruction getObject() {DSRLV newObject=new DSRLV();return newObject;}},
                          //Load-Signed
                        LB    { Instruction getObject() { LB newObject=new LB(); return newObject; } },
                        LH    { Instruction getObject() { LH newObject=new LH(); return newObject; } },
                        LW    { Instruction getObject() { LW newObject=new LW(); return newObject; } },
        		LD    { Instruction getObject() { LD newObject=new LD(); return newObject; } },
                        //Load-Unsigned
                        LBU { Instruction getObject() { LBU newObject=new LBU(); return newObject; } },
                        LHU { Instruction getObject() { LHU newObject=new LHU(); return newObject; } },
                        LWU { Instruction getObject() { LWU newObject=new LWU(); return newObject; } },                        
                        //Store
                        SB    { Instruction getObject() { SB newObject=new SB(); return newObject; } },
                        SH    { Instruction getObject() { SH newObject=new SH(); return newObject; } },
                        SW    { Instruction getObject() { SW newObject=new SW(); return newObject; } },
                        SD    { Instruction getObject() { SD newObject=new SD(); return newObject; } },
                        //Unconditional branches 
			J     { Instruction getObject() { J newObject=new J(); return newObject; } },
                        JAL   { Instruction getObject() { JAL newObject=new JAL(); return newObject; } },
                        JALR  { Instruction getObject() { JALR newObject=new JALR(); return newObject; } },
                        JR    { Instruction getObject() { JR newObject=new JR(); return newObject; } },
                        BNE   { Instruction getObject() { BNE newObject=new BNE(); return newObject; } },
			B     { Instruction getObject() { B newObject=new B(); return newObject; } },
                        //Conditional branches
                        BEQ   { Instruction getObject() { BEQ newObject=new BEQ(); return newObject; } },
                        BNEZ   { Instruction getObject() { BNEZ newObject=new BNEZ(); return newObject; } },
                        BEQZ   { Instruction getObject() { BEQZ newObject=new BEQZ(); return newObject; } },
                        BGEZ   { Instruction getObject() { BGEZ newObject=new BGEZ(); return newObject; } },
                        //Special instructions
                        NOP   { Instruction getObject() {NOP newObject=new NOP(); return newObject; } },
                        BUBBLE{ Instruction getObject() {BUBBLE newObject=new BUBBLE(); return newObject; } },
                        HALT  { Instruction getObject() {HALT newObject=new HALT(); return newObject; } },
                        TRAP  { Instruction getObject() {TRAP newObject=new TRAP(); return newObject; } },
                        SYSCALL  { Instruction getObject() {SYSCALL newObject=new SYSCALL(); return newObject; } },
                        BREAK  { Instruction getObject() {BREAK newObject=new BREAK(); return newObject; } },
			//Floating point instructions
			//Arithmetic
			ADD_D {Instruction getObject() {ADD_D newObject=new ADD_D(); return newObject; } },
			SUB_D {Instruction getObject() {SUB_D newObject=new SUB_D();return newObject; } },
			MUL_D { Instruction getObject() {MUL_D newObject=new MUL_D();return newObject;}},
			DIV_D { Instruction getObject() {DIV_D newObject=new DIV_D(); return newObject;}},
			//Load store
			LDC1 {Instruction getObject() {LDC1 newObject=new LDC1(); return newObject;}},
			SDC1 {Instruction getObject() {SDC1 newObject=new SDC1(); return newObject;}},
			//Move to and from
			DMTC1 {Instruction getObject() {DMTC1 newObject=new DMTC1(); return newObject;}},
			DMFC1 {Instruction getObject() {DMFC1 newObject=new DMFC1(); return newObject;}},
			//Formatted operand move
			MOV_D {Instruction getObject() {MOV_D newObject=new MOV_D(); return newObject;}},
		MOVZ_D {Instruction getObject() {MOVZ_D newObject=new MOVZ_D(); return newObject;}},
		MOVN_D {Instruction getObject() {MOVN_D newObject=new MOVN_D(); return newObject;}},
		//Special arithmetic instructions
		C_LT_D {Instruction getObject() {C_LT_D newObject = new C_LT_D(); return newObject; }},
		C_EQ_D {Instruction getObject() {C_EQ_D newObject = new C_EQ_D(); return newObject; }},
		//Conditional branches instructions
		BC1T {Instruction getObject() { BC1T newObject =new BC1T(); return newObject; }},
		BC1F {Instruction getObject() { BC1F newObject =new BC1F(); return newObject; }},
		//Conditional move on CC instructions
		MOVT_D {Instruction getObject() { MOVT_D newObject =new MOVT_D(); return newObject; }},
		MOVF_D {Instruction getObject() { MOVF_D newObject =new MOVF_D(); return newObject; }},
		//Conversion instructions
		CVT_L_D {Instruction getObject() { CVT_L_D newObject= new CVT_L_D(); return newObject; }};
		abstract Instruction getObject();
    }
    /**
     * <pre> 
     * Instruction fetch.
     * Now it is used in order to generate the Dinero trace-file
     *</pre>   
     */
    public void IF() throws BreakException {}
    
    /**
     * <pre>
     * Decode stage of the Pipeline
     * In this method all instructions that modify GPRs lock the involved register
     *</pre>
     **/
    public abstract void ID() throws RAWException,IrregularWriteOperationException,IrregularStringOfBitsException,TwosComplementSumException,HaltException,JumpException, BreakException,WAWException, FPInvalidOperationException;
    
    /**
     * <pre>
     * Execute stage of the Pipeline
     * In this stage all Alu Instructions perform their computations and save results in temporary registers
     * </pre>
     **/
    
    public abstract void EX() throws HaltException, IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException, DivisionByZeroException, NotAlignException, FPInvalidOperationException, FPExponentTooLargeException, FPUnderflowException, FPOverflowException, FPDivideByZeroException;
    
    /**
     * <pre>
     * Memory stage of the Pipeline
     * In this stage all Load and Store instructions access memory for getting or putting data
     * </pre>
     **/
    public abstract void MEM() throws HaltException, IrregularStringOfBitsException, NotAlignException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException;
    
    /**
     * <pre>
     * Write Back stage of the Pipeline
     * In this stage all instructions that modify registers write and unlock them 
     * </pre>
     **/ 
    public abstract void WB() throws HaltException, IrregularStringOfBitsException;
    
    /**
     * <pre>
     * Builds the binary encoding of instructions.
     * Every instruction is represented by a 32 bit field
     * </pre>
     **/
    public abstract void pack() throws IrregularStringOfBitsException;
    
    /**
     * Gets the number of params to pass at the instruction object
     * @return number of params
     **/ 
    public int getNParams(){return paramCount;};
    
    /**
     * <pre>
     * Gets the syntax of any instruction as string composed by the following simbols
     * %R   Register
     * %I   Immediate
     * %U   Unsigned Immediate
     * %L   Memory Label
     * %E   Program Label used for Jump Instructions
     * %B   Program Label used for Brench Instructions
     *
     * examples:
     *   Instruction -----> Syntax
     * DADD  R1,R2,R3   |   %R,%R,%R
     * DADDI R1,R2,-3   |   %R,%R,%I
     * DSLL  R1,R2,15   |   %R,%R,%U
     * LD    R1,vet(R0) |   %R,%L(%R)
     * J     loop       |   %E
     * BNE   R1,R2,loop |   %R,%R,%B
     * </pre>
     **/ 
    public String getSyntax(){return syntax;}
    
    /**
     * Returns the name of the instruction as string.
     * @return the instruction name(e.g. "DADD")
     **/
    public String getName(){return name;}
    
    
    /**
     *<pre> 
     * Returns a list with the instruction parameters 
     * e.g. DADD R1,R2,R3 --> params= { 1, 2, 3}
     *      LD R1, var(R0)--> params= { 1, address memory corresponding with var, 0}
     * </pre>
     *@return the list of parameters     
     **/
    public List<Integer> getParams(){return params;}
    
    
    /** 
     *<pre>
     * Sets the instruction with a list of parameters
     *          Passed list                                      | Instruction to set
     * e.g. list= { 1, 2, 3}                                     |   DADD R1,R2,R3
     *      list= { 1, address memory corresponding with var, 0} |   LD R1, var(R0)
     *@param params The list of parameters
     **/
    public void setParams(List<Integer> params){this.params=params; }
    
    /**
     * Sets the full name of the instruction as string
     *@param value full name of the instruction (e.g. "DADD R1,R2,R3")
     */
    public void setFullName(String value){fullname=value;}
    
    /** Sets the comment of the instruction as string. The comment is the text
     *  after every semicolon in the file .s
     * @param comment the comment associated with the instruction
     */
    public void setComment(String comment){this.comment=comment;}
    
    
    /** Gets the comment of the instruction as string.The comment is the text
     *  after every semicolon in the file .s
     * @return the comment
     */
    public String getComment(){return comment;}
    
   /** Gets the comment of the instruction as string. The comment is the text
     *  after every semicolon in the file .s
     * @return the full name of the instruction  (e.g. "DADD R1,R2,R3") 
     */    
    public String getFullName(){return fullname;}
    
   /** Gets the serial number of this instruction */
    public long getSerialNumber() { return serialNumber;}
    
    public String toString() {
		return fullname;
	}
	
	/**
	 * Enable forwarding mode
	 * @param value This variable enable the forwarding modality if it is true
	 * */
	public static void setEnableForwarding(boolean value) {
		enableForwarding = value;
	}
	
	/** Gets the state of EnableForwarding. This modality anticipates writing on registers
	 * at EX stage for Alu instructions or at MEM stage for Load-Store instructions
	 * @return The forwarding state
	 * */
	public static boolean getEnableForwarding() {
		return enableForwarding;
	}
	
	/**<pre>
	 * Gets the label of the instruction. Labels may be assigned to instructions
	 * when they are inserted in the symbol table
	 *</pre>
	 * @return label of the instruction
	 */
	public String getLabel() {
		return label;
	}
	
	/**<pre>
	 * Sets the label of the instruction. Labels may be assigned to instructions
	 * when they are inserted in the symbol table
	 *</pre>
	 * @value label of the instruction
	 */
	public void setLabel(String value) {
		label=value;
	}
	
	/**<pre>
	 * The repr field of the passed instruction is compared with the repr field
	 * of this instruction. If they are identical then true is returned else false is returned
	 * </pre>
	 * @instr instruction to compare with this
	 * return the result of the comparison
	 */
	public boolean equals(Instruction instr) {
		if(instr!=null) {
			if(instr.getRepr().getBinString().equalsIgnoreCase(this.repr.getBinString()))
				return true;
		}
		return false;
	}
	
}
