package org.edumips64.core.is;

import java.util.Map;
import java.util.function.Supplier;
import static java.util.Map.entry;    

import org.edumips64.core.*;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;

/** InstructionBuilder should be used to build all the instructions to be run by the CPU. The only exception is
 * BUBBLE, which has a public constructor to allow the CPU to build it without depending on InstructionBuilder.
 *
 * BUBBLE is a special instruction that should not be used by programs (it's not rendered), therefore InstructionBuilder
 * will refuse to build it.
 */
public class InstructionBuilder {
  private CPU cpu;
  private CacheSimulator cachesim;
  private ConfigStore config;

  private Map<String, Supplier<Instruction>> instructionDictionary;

  public InstructionBuilder(Memory memory, IOManager iom, CPU cpu, CacheSimulator cachesim, ConfigStore config) {
    this.cpu = cpu;
    this.cachesim = cachesim;
    this.config = config;

    instructionDictionary = Map.ofEntries(
      //ALU R-Type 32-bits
      entry("ADD", ADD::new),
      entry("ADDU", ADDU::new),
      
      entry("SUB", SUB::new),
      entry("SUBU", SUBU::new),
      entry("DIV", DIV::new),
      entry("DIVU", DIVU::new),
      entry("MULT", MULT::new),
      entry("MULTU", MULTU::new),

      //ALU I-Type 32-bits
      entry("ADDI", ADDI::new),
      entry("ADDIU", ADDIU::new),

      //ALU Shifting 32-bits
      entry("SLL", SLL::new),
      entry("SLLV", SLLV::new),
      entry("SRA", SRA::new),
      entry("SRAV", SRAV::new),
      entry("SRL", SRL::new),
      entry("SRLV", SRLV::new),

      //ALU R-Type
      entry("AND", AND::new),
      entry("DADD", DADD::new),
      entry("DADDU", DADDU::new),
      entry("DSUB", DSUB::new),
      entry("DSUBU", DSUBU::new),
      entry("OR", OR::new),
      entry("SLT", SLT::new),
      entry("SLTU", SLTU::new),
      entry("XOR", XOR::new),
      entry("MOVN", MOVN::new),
      entry("MOVZ", MOVZ::new),
      entry("DDIV", DDIV::new),
      entry("DDIVU", DDIVU::new),
      entry("DMUL", DMUL::new),
      entry("DMUHU", DMUHU::new),
      entry("DMULT", DMULT::new),
      entry("DMULU", DMULU::new),
      entry("DMULTU", DMULTU::new),
      entry("MFLO", MFLO::new),
      entry("MFHI", MFHI::new),

      //ALU I-Type
      entry("ANDI", ANDI::new),
      entry("DADDI", DADDI::new),
      entry("DADDUI", DADDUI::new),
      entry("DADDIU", DADDIU::new),
      entry("LUI", LUI::new),
      entry("ORI", ORI::new),
      entry("SLTI", SLTI::new),
      entry("SLTIU", SLTIU::new),
      entry("XORI", XORI::new),

      //ALU Shifting
      entry("DSLL", DSLL::new),
      entry("DSLLV", DSLLV::new),
      entry("DSRA", DSRA::new),
      entry("DSRAV", DSRAV::new),
      entry("DSRL", DSRL::new),
      entry("DSRLV", DSRLV::new),

      //Load-Signed
      entry("LB", () -> new LB(memory)),
      entry("LH", () -> new LH(memory)),
      entry("LW", () -> new LW(memory)),
      entry("LD", () -> new LD(memory)),

      //Load-Unsigned
      entry("LBU", () -> new LBU(memory)),
      entry("LHU", () -> new LHU(memory)),
      entry("LWU", () -> new LWU(memory)),

      //Store
      entry("SB", () -> new SB(memory)),
      entry("SH", () -> new SH(memory)),
      entry("SW", () -> new SW(memory)),
      entry("SD", () -> new SD(memory)),

      //Unconditional branches
      entry("J", J::new),
      entry("JAL", JAL::new),
      entry("JALR", JALR::new),
      entry("JR", JR::new),
      entry("B", B::new),

      //Conditional branches
      entry("BEQ", BEQ::new),
      entry("BNE", BNE::new),
      entry("BNEZ", BNEZ::new),
      entry("BEQZ", BEQZ::new),
      entry("BGEZ", BGEZ::new),

      //Special instructions
      entry("NOP", NOP::new),
      entry("HALT", HALT::new),
      entry("TRAP", () -> new TRAP(memory, iom)),
      entry("SYSCALL", () -> new SYSCALL(memory, iom)),
      entry("BREAK", BREAK::new),

      //Floating point instructions
      //Arithmetic
      entry("ADD_D", () -> new ADD_D(cpu.getFCSR())),
      entry("SUB_D", () -> new SUB_D(cpu.getFCSR())),
      entry("MUL_D", () -> new MUL_D(cpu.getFCSR())),
      entry("DIV_D", () -> new DIV_D(cpu.getFCSR())),

      //Load store
      entry("LDC1", () -> new LDC1(memory)),
      entry("L_D", () -> new L_D(memory)),
      entry("SDC1", () -> new SDC1(memory)),
      entry("S_D", () -> new S_D(memory)),
      entry("LWC1", () -> new LWC1(memory)),
      entry("SWC1", () -> new SWC1(memory)),

      //Move to and from
      entry("DMTC1", DMTC1::new),
      entry("DMFC1", DMFC1::new),
      entry("MTC1", MTC1::new),
      entry("MFC1", MFC1::new),

      //Formatted operand move
      entry("MOV_D", MOV_D::new),
      entry("MOVZ_D", MOVZ_D::new),
      entry("MOVN_D", MOVN_D::new),

      //Special arithmetic instructions
      entry("C_LT_D", C_LT_D::new),
      entry("C_EQ_D", C_EQ_D::new),

      //Conditional branches instructions
      entry("BC1T", BC1T::new),
      entry("BC1F", BC1F::new),

      //Conditional move on CC instructions
      entry("MOVT_D", MOVT_D::new),
      entry("MOVF_D", MOVF_D::new),

      //Conversion instructions
      entry("CVT_L_D", CVT_L_D::new),
      entry("CVT_D_L", CVT_D_L::new),
      entry("CVT_W_D", CVT_W_D::new),
      entry("CVT_D_W", CVT_D_W::new)
    );
  }

/**
 * Gets a string containing all supported instructions in lowercase, separated by '|'.
 * The string includes all instructions from the instruction dictionary plus 'BUBBLE'.
 * 
 * @return A string containing all supported instructions in lowercase, with each instruction
 *         separated by the '|' character
 */
  public String getSupportedInstructionString() {
    String instructions = String.join("|", instructionDictionary.keySet()) + "|BUBBLE";
    return instructions.toLowerCase();
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
  
    // If the instruction is not implemented, return null
    if (!instructionDictionary.containsKey(name)) {
      return null;
    }

    Instruction instruction = instructionDictionary.get(name).get();

    // Serial number for the instruction being built.
    int serialNumber = config.getInt(ConfigKey.SERIAL_NUMBER);
    config.putInt(ConfigKey.SERIAL_NUMBER, serialNumber + 1);
    instruction.setSerialNumber(serialNumber);

    // Parsing-time metadata.
    instruction.setParsingMetadata(instructionMetadata);

    // Inject other dependencies.
    instruction.setCPU(cpu);
    instruction.setCachesim(cachesim);
    return instruction;
  }
}
