package org.edumips64.core.is;

import org.edumips64.core.IOManager;
import org.edumips64.core.Memory;

/** InstructionBuilder should be used to build all the instructions to be run by the CPU. The only exception is
 * BUBBLE, which has a public constructor to allow the CPU to build it without depending on InstructionBuilder.
 *
 * BUBBLE is a special instruction that should not be used by programs (it's not rendered), therefore InstructionBuilder
 * will refuse to build it.
 */
public class InstructionBuilder {
  private Memory memory;
  private IOManager iom;

  public InstructionBuilder(Memory memory, IOManager iom) {
    this.memory = memory;
    this.iom = iom;
  }
  /**
   * Creates a new instance of an Instruction's subclass
   * @param instructionName string value to pass in order to instantiate an instruction object
   * @return the instruction object, or null if the instruction is not implemented.
   *
   */
  public Instruction buildInstruction(String instructionName) {
    // If the name of the requested instruction has got a dot, the instruction is FP and an
    // underscore takes the place of the dot because classes names cannot contain dots
    String name = instructionName.replaceAll("\\.", "_");

    switch(name) {
      //ALU R-Type 32-bits
      case "ADD":
        return new ADD();
      case "ADDU":
        return new ADDU();
      case "SUB":
        return new SUB();
      case "SUBU":
        return new SUBU();
      case "DIV":
        return new DIV();
      case "DIVU":
        return new DIVU();
      case "MULT":
        return new MULT();
      case "MULTU":
        return new MULTU();

      //ALU I-Type 32-bits
      case "ADDI":
        return new ADDI();
      case "ADDIU":
        return new ADDIU();

      //ALU Shifting 32-bits
      case "SLL":
        return new SLL();
      case "SLLV":
        return new SLLV();
      case "SRA":
        return new SRA();
      case "SRAV":
        return new SRAV();
      case "SRL":
        return new SRL();
      case "SRLV":
        return new SRLV();

      //ALU R-Type
      case "AND":
        return new AND();
      case "DADD":
        return new DADD();
      case "DADDU":
        return new DADDU();
      case "DSUB":
        return new DSUB();
      case "DSUBU":
        return new DSUBU();
      case "OR":
        return new OR();
      case "SLT":
        return new SLT();
      case "SLTU":
        return new SLTU();
      case "XOR":
        return new XOR();
      case "MOVN":
        return new MOVN();
      case "MOVZ":
        return new MOVZ();
      case "DDIV":
        return new DDIV();
      case "DDIVU":
        return new DDIVU();
      case "DMULT":
        return new DMULT();
      case "DMULTU":
        return new DMULTU();
      case "MFLO":
        return new MFLO();
      case "MFHI":
        return new MFHI();

      //ALU I-Type
      case "ANDI":
        return new ANDI();
      case "DADDI":
        return new DADDI();
      case "DADDUI":
        return new DADDUI();
      case "DADDIU":
        return new DADDIU();
      case "LUI":
        return new LUI();
      case "ORI":
        return new ORI();
      case "SLTI":
        return new SLTI();
      case "SLTIU":
        return new SLTIU();
      case "XORI":
        return new XORI();

      //ALU Shifting
      case "DSLL":
        return new DSLL();
      case "DSLLV":
        return new DSLLV();
      case "DSRA":
        return new DSRA();
      case "DSRAV":
        return new DSRAV();
      case "DSRL":
        return new DSRL();
      case "DSRLV":
        return new DSRLV();

      //Load-Signed
      case "LB":
        return new LB(memory);
      case "LH":
        return new LH(memory);
      case "LW":
        return new LW(memory);
      case "LD":
        return new LD(memory);

      //Load-Unsigned
      case "LBU":
        return new LBU(memory);
      case "LHU":
        return new LHU(memory);
      case "LWU":
        return new LWU(memory);

      //Store
      case "SB":
        return new SB(memory);
      case "SH":
        return new SH(memory);
      case "SW":
        return new SW(memory);
      case "SD":
        return new SD(memory);

      //Unconditional branches
      case "J":
        return new J();
      case "JAL":
        return new JAL();
      case "JALR":
        return new JALR();
      case "JR":
        return new JR();
      case "B":
        return new B();

      //Conditional branches
      case "BEQ":
        return new BEQ();
      case "BNE":
        return new BNE();
      case "BNEZ":
        return new BNEZ();
      case "BEQZ":
        return new BEQZ();
      case "BGEZ":
        return new BGEZ();

      //Special instructions
      case "NOP":
        return new NOP();
      case "HALT":
        return new HALT();
      case "TRAP":
        return new TRAP(memory, iom);
      case "SYSCALL":
        return new SYSCALL(memory, iom);
      case "BREAK":
        return new BREAK();

      //Floating point instructions
      //Arithmetic
      case "ADD_D":
        return new ADD_D();
      case "SUB_D":
        return new SUB_D();
      case "MUL_D":
        return new MUL_D();
      case "DIV_D":
        return new DIV_D();

      //Load store
      case "LDC1":
        return new LDC1(memory);
      case "L_D":
        return new L_D(memory);
      case "SDC1":
        return new SDC1(memory);
      case "S_D":
        return new S_D(memory);
      case "LWC1":
        return new LWC1(memory);
      case "SWC1":
        return new SWC1(memory);

      //Move to and from
      case "DMTC1":
        return new DMTC1();
      case "DMFC1":
        return new DMFC1();
      case "MTC1":
        return new MTC1();
      case "MFC1":
        return new MFC1();

      //Formatted operand move
      case "MOV_D":
        return new MOV_D();
      case "MOVZ_D":
        return new MOVZ_D();
      case "MOVN_D":
        return new MOVN_D();

      //Special arithmetic instructions
      case "C_LT_D":
        return new C_LT_D();
      case "C_EQ_D":
        return new C_EQ_D();

      //Conditional branches instructions
      case "BC1T":
        return new BC1T();
      case "BC1F":
        return new BC1F();

      //Conditional move on CC instructions
      case "MOVT_D":
        return new MOVT_D();
      case "MOVF_D":
        return new MOVF_D();

      //Conversion instructions
      case "CVT_L_D":
        return new CVT_L_D();
      case "CVT_D_L":
        return new CVT_D_L();
      case "CVT_W_D":
        return new CVT_W_D();
      case "CVT_D_W":
        return new CVT_D_W();
      default:
        return null;
    }
  }
}
