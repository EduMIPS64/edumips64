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

package org.edumips64.core.is;

import org.edumips64.core.*;
import org.edumips64.core.fpu.*;
import org.edumips64.utils.*;
import java.util.*;
import java.util.logging.Logger;

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
  protected static Memory memory = Memory.getInstance();
  //protected static CPU cpu;
  protected Register[] TR; //is not static because each instruction has got its own registers
  protected RegisterFP[] TRfp;
  protected String fullname;
  protected static boolean enableForwarding = ConfigManager.getConfig().getBoolean("forwarding");
  protected String label;
  protected static final Logger logger = Logger.getLogger(Instruction.class.getName());
  protected Integer serialNumber;


  /** Creates a new instance of Instruction */
  public Instruction() {
    params = new LinkedList<Integer>();
    TR = new Register[5];
    TRfp = new RegisterFP[5];
    repr = new BitSet32();
    syntax = "";
    repr.reset(false);
    //generating a serial number for the current instruction
    serialNumber = ConfigManager.getConfig().getInt("serialNumber");
    ConfigManager.getConfig().putInt("serialNumber", serialNumber + 1);

    //initialization of temporary registers
    for (int i = 0; i < TR.length; i++) {
      TR[i] = new Register("TR " + i + "(Instruction " + serialNumber + ")");
      TRfp[i] = new RegisterFP();
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
  public static Instruction buildInstruction(String name) {
    Instruction returnedObject = null;
    //If the name of the requested instruction has got a dot, the instruction is FP and an
    //underscore takes the place of the dot because classes names cannot contain dots
    name = name.replaceAll("\\.", "_");

    for (InstructionEnumerator op : InstructionEnumerator.values()) {
      if (op.name().equals(name)) {
        returnedObject = op.getObject();
        break;
      }
    }

    return returnedObject;
  }

  public enum InstructionEnumerator {
    //ALU R-Type 32-bits
    ADD {
      Instruction getObject() {
        return new ADD();
      }
    },
    ADDU {
      Instruction getObject() {
        return new ADDU();
      }
    },
    SUB {
      Instruction getObject() {
        return new SUB();
      }
    },
    SUBU {
      Instruction getObject() {
        return new SUBU();
      }
    },
    DIV {
      Instruction getObject() {
        return new DIV();
      }
    },
    DIVU {
      Instruction getObject() {
        return new DIVU();
      }
    },
    MULT {
      Instruction getObject() {
        return new MULT();
      }
    },
    MULTU {
      Instruction getObject() {
        return new MULTU();
      }
    },

    //ALU I-Type 32-bits
    ADDI {
      Instruction getObject() {
        return new ADDI();
      }
    },
    ADDIU {
      Instruction getObject() {
        return new ADDIU();
      }
    },

    //ALU Shifting 32-bits
    SLL {
      Instruction getObject() {
        return new SLL();
      }
    },
    SLLV {
      Instruction getObject() {
        return new SLLV();
      }
    },
    SRA {
      Instruction getObject() {
        return new SRA();
      }
    },
    SRAV {
      Instruction getObject() {
        return new SRAV();
      }
    },
    SRL {
      Instruction getObject() {
        return new SRL();
      }
    },
    SRLV {
      Instruction getObject() {
        return new SRLV();
      }
    },

    //ALU R-Type
    AND {
      Instruction getObject() {
        return new AND();
      }
    },
    DADD {
      Instruction getObject() {
        return new DADD();
      }
    },
    DADDU {
      Instruction getObject() {
        return new DADDU();
      }
    },
    DSUB {
      Instruction getObject() {
        return new DSUB();
      }
    },
    DSUBU {
      Instruction getObject() {
        return new DSUBU();
      }
    },
    OR {
      Instruction getObject() {
        return new OR();
      }
    },
    SLT {
      Instruction getObject() {
        return new SLT();
      }
    },
    SLTU {
      Instruction getObject() {
        return new SLTU();
      }
    },
    XOR {
      Instruction getObject() {
        return new XOR();
      }
    },
    MOVN {
      Instruction getObject() {
        return new MOVN();
      }
    },
    MOVZ {
      Instruction getObject() {
        return new MOVZ();
      }
    },
    DDIV {
      Instruction getObject() {
        return new DDIV();
      }
    },
    DDIVU {
      Instruction getObject() {
        return new DDIVU();
      }
    },
    DMULT {
      Instruction getObject() {
        return new DMULT();
      }
    },
    DMULTU {
      Instruction getObject() {
        return new DMULTU();
      }
    },
    MFLO {
      Instruction getObject() {
        return new MFLO();
      }
    },
    MFHI {
      Instruction getObject() {
        return new MFHI();
      }
    },


    //ALU I-Type
    ANDI {
      Instruction getObject() {
        return new ANDI();
      }
    },
    DADDI {
      Instruction getObject() {
        return new DADDI();
      }
    },
    DADDUI {
      Instruction getObject() {
        return new DADDUI();
      }
    },
    DADDIU {
      Instruction getObject() {
        return new DADDIU();
      }
    },
    LUI {
      Instruction getObject() {
        return new LUI();
      }
    },
    ORI {
      Instruction getObject() {
        return new ORI();
      }
    },
    SLTI {
      Instruction getObject() {
        return new SLTI();
      }
    },
    SLTIU {
      Instruction getObject() {
        return new SLTIU();
      }
    },
    XORI {
      Instruction getObject() {
        return new XORI();
      }
    },
    //ALU Shifting
    DSLL {
      Instruction getObject() {
        return new DSLL();
      }
    },
    DSLLV {
      Instruction getObject() {
        return new DSLLV();
      }
    },
    DSRA {
      Instruction getObject() {
        return new DSRA();
      }
    },
    DSRAV {
      Instruction getObject() {
        return new DSRAV();
      }
    },
    DSRL {
      Instruction getObject() {
        return new DSRL();
      }
    },
    DSRLV {
      Instruction getObject() {
        return new DSRLV();
      }
    },
    //Load-Signed
    LB {
      Instruction getObject() {
        return new LB();
      }
    },
    LH {
      Instruction getObject() {
        return new LH();
      }
    },
    LW {
      Instruction getObject() {
        return new LW();
      }
    },
    LD {
      Instruction getObject() {
        return new LD();
      }
    },
    //Load-Unsigned
    LBU {
      Instruction getObject() {
        return new LBU();
      }
    },
    LHU {
      Instruction getObject() {
        return new LHU();
      }
    },
    LWU {
      Instruction getObject() {
        return new LWU();
      }
    },
    //Store
    SB {
      Instruction getObject() {
        return new SB();
      }
    },
    SH {
      Instruction getObject() {
        return new SH();
      }
    },
    SW {
      Instruction getObject() {
        return new SW();
      }
    },
    SD {
      Instruction getObject() {
        return new SD();
      }
    },
    //Unconditional branches
    J {
      Instruction getObject() {
        return new J();
      }
    },
    JAL {
      Instruction getObject() {
        return new JAL();
      }
    },
    JALR {
      Instruction getObject() {
        return new JALR();
      }
    },
    JR {
      Instruction getObject() {
        return new JR();
      }
    },
    BNE {
      Instruction getObject() {
        return new BNE();
      }
    },
    B {
      Instruction getObject() {
        return new B();
      }
    },
    //Conditional branches
    BEQ {
      Instruction getObject() {
        return new BEQ();
      }
    },
    BNEZ {
      Instruction getObject() {
        return new BNEZ();
      }
    },
    BEQZ {
      Instruction getObject() {
        return new BEQZ();
      }
    },
    BGEZ {
      Instruction getObject() {
        return new BGEZ();
      }
    },
    //Special instructions
    NOP {
      Instruction getObject() {
        return new NOP();
      }
    },
    BUBBLE {
      Instruction getObject() {
        return new BUBBLE();
      }
    },
    HALT {
      Instruction getObject() {
        return new HALT();
      }
    },
    TRAP {
      Instruction getObject() {
        return new TRAP();
      }
    },
    SYSCALL {
      Instruction getObject() {
        return new SYSCALL();
      }
    },
    BREAK {
      Instruction getObject() {
        return new BREAK();
      }
    },
    //Floating point instructions
    //Arithmetic
    ADD_D {
      Instruction getObject() {
        return new ADD_D();
      }
    },
    SUB_D {
      Instruction getObject() {
        return new SUB_D();
      }
    },
    MUL_D {
      Instruction getObject() {
        return new MUL_D();
      }
    },
    DIV_D {
      Instruction getObject() {
        return new DIV_D();
      }
    },
    //Load store
    LDC1 {
      Instruction getObject() {
        return new LDC1();
      }
    },
    L_D {
      Instruction getObject() {
        return new L_D();
      }
    },
    SDC1 {
      Instruction getObject() {
        return new SDC1();
      }
    },
    S_D {
      Instruction getObject() {
        return new S_D();
      }
    },
    LWC1 {
      Instruction getObject() {
        return new LWC1();
      }
    },
    SWC1 {
      Instruction getObject() {
        return new SWC1();
      }
    },
    //Move to and from
    DMTC1 {
      Instruction getObject() {
        return new DMTC1();
      }
    },
    DMFC1 {
      Instruction getObject() {
        return new DMFC1();
      }
    },
    MTC1 {
      Instruction getObject() {
        return new MTC1();
      }
    },
    MFC1 {
      Instruction getObject() {
        return new MFC1();
      }
    },
    //Formatted operand move
    MOV_D {
      Instruction getObject() {
        return new MOV_D();
      }
    },
    MOVZ_D {
      Instruction getObject() {
        return new MOVZ_D();
      }
    },
    MOVN_D {
      Instruction getObject() {
        return new MOVN_D();
      }
    },
    //Special arithmetic instructions
    C_LT_D {
      Instruction getObject() {
        return new C_LT_D();
      }
    },
    C_EQ_D {
      Instruction getObject() {
        return new C_EQ_D();
      }
    },
    //Conditional branches instructions
    BC1T {
      Instruction getObject() {
        return new BC1T();
      }
    },
    BC1F {
      Instruction getObject() {
        return new BC1F();
      }
    },
    //Conditional move on CC instructions
    MOVT_D {
      Instruction getObject() {
        return new MOVT_D();
      }
    },
    MOVF_D {
      Instruction getObject() {
        return new MOVF_D();
      }
    },
    //Conversion instructions
    CVT_L_D {
      Instruction getObject() {
        return new CVT_L_D();
      }
    },
    CVT_D_L {
      Instruction getObject() {
        return new CVT_D_L();
      }
    },
    CVT_W_D {
      Instruction getObject() {
        return new CVT_W_D();
      }
    },
    CVT_D_W {
      Instruction getObject() {
       return new CVT_D_W();
      }
    };

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
  public abstract void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException, JumpException, BreakException, WAWException, FPInvalidOperationException;

  /**
   * <pre>
   * Execute stage of the Pipeline
   * In this stage all Alu Instructions perform their computations and save results in temporary registers
   * </pre>
   **/

  public abstract void EX() throws HaltException, IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException, DivisionByZeroException, NotAlignException, FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, AddressErrorException;

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
  public String getSyntax() {
    return syntax;
  }

  /**
   * Returns the name of the instruction as string.
   * @return the instruction name(e.g. "DADD")
   **/
  public String getName() {
    return name;
  }


  /**
   *<pre>
   * Returns a list with the instruction parameters
   * e.g. DADD R1,R2,R3 --> params= { 1, 2, 3}
   *      LD R1, var(R0)--> params= { 1, address memory corresponding with var, 0}
   * </pre>
   *@return the list of parameters
   **/
  public List<Integer> getParams() {
    return params;
  }


  /**
   *<pre>
   * Sets the instruction with a list of parameters
   *          Passed list                                      | Instruction to set
   * e.g. list= { 1, 2, 3}                                     |   DADD R1,R2,R3
   *      list= { 1, address memory corresponding with var, 0} |   LD R1, var(R0)
   *@param params The list of parameters
   **/
  public void setParams(List<Integer> params) {
    this.params = params;
  }

  /**
   * Sets the full name of the instruction as string
   *@param value full name of the instruction (e.g. "DADD R1,R2,R3")
   */
  public void setFullName(String value) {
    fullname = value;
  }

  /** Sets the comment of the instruction as string. The comment is the text
   *  after every semicolon in the file .s
   * @param comment the comment associated with the instruction
   */
  public void setComment(String comment) {
    this.comment = comment;
  }


  /** Gets the comment of the instruction as string.The comment is the text
   *  after every semicolon in the file .s
   * @return the comment
   */
  public String getComment() {
    return comment;
  }

  /** Gets the full name of the instruction as string.
    * @return the full name of the instruction  (e.g. "DADD R1,R2,R3")
    */
  public String getFullName() {
    return fullname;
  }

  /** Gets the serial number of this instruction */
  public long getSerialNumber() {
    return serialNumber;
  }

  public String toString() {
    String repr = name + " (" + fullname + ") [# " + serialNumber + "]";
    if (label != null && label.length() > 0) {
      repr += " {label: " + label + "}";
    }
    return repr;
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
   * @param value label of the instruction
   */
  public void setLabel(String value) {
    label = value;
  }

  /**<pre>
   * The repr field of the passed instruction is compared with the repr field
   * of this instruction. If they are identical then true is returned else false is returned
   * </pre>
   * @param instr instruction to compare with this
   * @return the result of the comparison
   */
  @Override
  public boolean equals(Object instr) {
    if (instr == null) {
      return false;
    }

    if (instr == this) {
      return true;
    }

    if (!(instr instanceof Instruction)) {
      return false;
    }

    Instruction i = (Instruction) instr;
    return i.getSerialNumber() == serialNumber;
  }

  /**<pre>
   * Returns true if the instruction is a BUBBLE, false otherwise. BUBBLE is used to fill
   * the pipeline and is not a real instruction, so some parts of the UI code need to know
   * if the instruction is a BUBBLE or not. This method abstracts the details of how to check
   * if an instruction is a BUBBLE.
   * </pre>
   */
  public boolean isBubble() {
    return name == " ";
  }
}
