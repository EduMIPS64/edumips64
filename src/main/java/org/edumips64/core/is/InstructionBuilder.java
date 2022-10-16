package org.edumips64.core.is;

import org.edumips64.core.CPU;
import org.edumips64.core.Dinero;
import org.edumips64.core.IOManager;
import org.edumips64.core.Memory;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;

/** InstructionBuilder should be used to build all the instructions to be run by the CPU. The only exception is
 * BUBBLE, which has a public constructor to allow the CPU to build it without depending on InstructionBuilder.
 *
 * BUBBLE is a special instruction that should not be used by programs (it's not rendered), therefore InstructionBuilder
 * will refuse to build it.
 */
public class InstructionBuilder {
  private Memory memory;
  private IOManager iom;
  private CPU cpu;
  private Dinero dinero;
  private ConfigStore config;

  public InstructionBuilder(Memory memory, IOManager iom, CPU cpu, Dinero dinero, ConfigStore config) {
    this.memory = memory;
    this.iom = iom;
    this.cpu = cpu;
    this.dinero = dinero;
    this.config = config;
  }
  
  /**
   * Creates a new instance of an Instruction's subclass, with null parsing-time metadata.
   * @param instructionName string value to pass in order to instantiate an instruction object
   * @return the instruction object, or null if the instruction is not implemented.
   *
   */
  public Instruction buildInstruction(String instructionName) {
    return buildInstruction(instructionName, null);
  }

  /**
   * Creates a new instance of an Instruction's subclass
   * @param instructionName string value to pass in order to instantiate an instruction object
   * @param instructionMetadata parsing-time metadata for the instruction. Can be null.
   * @return the instruction object, or null if the instruction is not implemented.
   *
   */
  public Instruction buildInstruction(String instructionName, ParsedInstructionMetadata instructionMetadata) {
    // If the name of the requested instruction has got a dot, the instruction is FP and an
    // underscore takes the place of the dot because classes names cannot contain dots
    String name = instructionName.replaceAll("\\.", "_");

    Instruction instruction;
    switch(name) {
      //ALU R-Type 32-bits
      case "ADD":
        instruction = new ADD();
        break;
      case "ADDU":
        instruction = new ADDU();
        break;
      case "SUB":
        instruction = new SUB();
        break;
      case "SUBU":
        instruction = new SUBU();
        break;
      case "DIV":
        instruction = new DIV();
        break;
      case "DIVU":
        instruction = new DIVU();
        break;
      case "MULT":
        instruction = new MULT();
        break;
      case "MULTU":
        instruction = new MULTU();
        break;

      //ALU I-Type 32-bits
      case "ADDI":
        instruction = new ADDI();
        break;
      case "ADDIU":
        instruction = new ADDIU();
        break;

      //ALU Shifting 32-bits
      case "SLL":
        instruction = new SLL();
        break;
      case "SLLV":
        instruction = new SLLV();
        break;
      case "SRA":
        instruction = new SRA();
        break;
      case "SRAV":
        instruction = new SRAV();
        break;
      case "SRL":
        instruction = new SRL();
        break;
      case "SRLV":
        instruction = new SRLV();
        break;

      //ALU R-Type
      case "AND":
        instruction = new AND();
        break;
      case "DADD":
        instruction = new DADD();
        break;
      case "DADDU":
        instruction = new DADDU();
        break;
      case "DSUB":
        instruction = new DSUB();
        break;
      case "DSUBU":
        instruction = new DSUBU();
        break;
      case "OR":
        instruction = new OR();
        break;
      case "SLT":
        instruction = new SLT();
        break;
      case "SLTU":
        instruction = new SLTU();
        break;
      case "XOR":
        instruction = new XOR();
        break;
      case "MOVN":
        instruction = new MOVN();
        break;
      case "MOVZ":
        instruction = new MOVZ();
        break;
      case "DDIV":
        instruction = new DDIV();
        break;
      case "DDIVU":
        instruction = new DDIVU();
        break;
      case "DMULT":
        instruction = new DMULT();
        break;
      case "DMULU":
        instruction = new DMULU();
        break;
      case "DMULTU":
        instruction = new DMULTU();
        break;
      case "MFLO":
        instruction = new MFLO();
        break;
      case "MFHI":
        instruction = new MFHI();
        break;

      //ALU I-Type
      case "ANDI":
        instruction = new ANDI();
        break;
      case "DADDI":
        instruction = new DADDI();
        break;
      case "DADDUI":
        instruction = new DADDUI();
        break;
      case "DADDIU":
        instruction = new DADDIU();
        break;
      case "LUI":
        instruction = new LUI();
        break;
      case "ORI":
        instruction = new ORI();
        break;
      case "SLTI":
        instruction = new SLTI();
        break;
      case "SLTIU":
        instruction = new SLTIU();
        break;
      case "SLTUI":
        instruction = new SLTUI();
        break;
      case "XORI":
        instruction = new XORI();
        break;

      //ALU Shifting
      case "DSLL":
        instruction = new DSLL();
        break;
      case "DSLLV":
        instruction = new DSLLV();
        break;
      case "DSRA":
        instruction = new DSRA();
        break;
      case "DSRAV":
        instruction = new DSRAV();
        break;
      case "DSRL":
        instruction = new DSRL();
        break;
      case "DSRLV":
        instruction = new DSRLV();
        break;

      //Load-Signed
      case "LB":
        instruction = new LB(memory);
        break;
      case "LH":
        instruction = new LH(memory);
        break;
      case "LW":
        instruction = new LW(memory);
        break;
      case "LD":
        instruction = new LD(memory);
        break;

      //Load-Unsigned
      case "LBU":
        instruction = new LBU(memory);
        break;
      case "LHU":
        instruction = new LHU(memory);
        break;
      case "LWU":
        instruction = new LWU(memory);
        break;

      //Store
      case "SB":
        instruction = new SB(memory);
        break;
      case "SH":
        instruction = new SH(memory);
        break;
      case "SW":
        instruction = new SW(memory);
        break;
      case "SD":
        instruction = new SD(memory);
        break;

      //Unconditional branches
      case "J":
        instruction = new J();
        break;
      case "JAL":
        instruction = new JAL();
        break;
      case "JALR":
        instruction = new JALR();
        break;
      case "JR":
        instruction = new JR();
        break;
      case "B":
        instruction = new B();
        break;

      //Conditional branches
      case "BEQ":
        instruction = new BEQ();
        break;
      case "BNE":
        instruction = new BNE();
        break;
      case "BNEZ":
        instruction = new BNEZ();
        break;
      case "BEQZ":
        instruction = new BEQZ();
        break;
      case "BGEZ":
        instruction = new BGEZ();
        break;

      //Special instructions
      case "NOP":
        instruction = new NOP();
        break;
      case "HALT":
        instruction = new HALT();
        break;
      case "TRAP":
        instruction = new TRAP(memory, iom);
        break;
      case "SYSCALL":
        instruction = new SYSCALL(memory, iom);
        break;
      case "BREAK":
        instruction = new BREAK();
        break;

      //Floating point instructions
      //Arithmetic
      case "ADD_D":
        instruction = new ADD_D(cpu.getFCSR());
        break;
      case "SUB_D":
        instruction = new SUB_D(cpu.getFCSR());
        break;
      case "MUL_D":
        instruction = new MUL_D(cpu.getFCSR());
        break;
      case "DIV_D":
        instruction = new DIV_D(cpu.getFCSR());
        break;

      //Load store
      case "LDC1":
        instruction = new LDC1(memory);
        break;
      case "L_D":
        instruction = new L_D(memory);
        break;
      case "SDC1":
        instruction = new SDC1(memory);
        break;
      case "S_D":
        instruction = new S_D(memory);
        break;
      case "LWC1":
        instruction = new LWC1(memory);
        break;
      case "SWC1":
        instruction = new SWC1(memory);
        break;

      //Move to and from
      case "DMTC1":
        instruction = new DMTC1();
        break;
      case "DMFC1":
        instruction = new DMFC1();
        break;
      case "MTC1":
        instruction = new MTC1();
        break;
      case "MFC1":
        instruction = new MFC1();
        break;

      //Formatted operand move
      case "MOV_D":
        instruction = new MOV_D();
        break;
      case "MOVZ_D":
        instruction = new MOVZ_D();
        break;
      case "MOVN_D":
        instruction = new MOVN_D();
        break;

      //Special arithmetic instructions
      case "C_LT_D":
        instruction = new C_LT_D();
        break;
      case "C_EQ_D":
        instruction = new C_EQ_D();
        break;

      //Conditional branches instructions
      case "BC1T":
        instruction = new BC1T();
        break;
      case "BC1F":
        instruction = new BC1F();
        break;

      //Conditional move on CC instructions
      case "MOVT_D":
        instruction = new MOVT_D();
        break;
      case "MOVF_D":
        instruction = new MOVF_D();
        break;

      //Conversion instructions
      case "CVT_L_D":
        instruction = new CVT_L_D();
        break;
      case "CVT_D_L":
        instruction = new CVT_D_L();
        break;
      case "CVT_W_D":
        instruction = new CVT_W_D();
        break;
      case "CVT_D_W":
        instruction = new CVT_D_W();
        break;

      default:
        return null;
    }

    // Serial number for the instruction being built.
    int serialNumber = config.getInt(ConfigKey.SERIAL_NUMBER);
    config.putInt(ConfigKey.SERIAL_NUMBER, serialNumber + 1);
    instruction.setSerialNumber(serialNumber);

    // Parsing-time metadata.
    instruction.setParsingMetadata(instructionMetadata);

    // Inject other dependencies.
    instruction.setCPU(cpu);
    instruction.setDinero(dinero);
    return instruction;
  }
}
