package org.edumips64.core.is;

import java.util.Map;
import java.util.function.Supplier;

import java.util.HashMap;

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
  private CPU cpu;
  private Dinero dinero;
  private ConfigStore config;

  private Map<String, Supplier<Instruction>> instructionDictionary;

  public InstructionBuilder(Memory memory, IOManager iom, CPU cpu, Dinero dinero, ConfigStore config) {
    this.cpu = cpu;
    this.dinero = dinero;
    this.config = config;

    // Note: the map isn't built using Map.ofEntries() due to GWT not supporting Map.entry() yet.
    // Also, this is not static because some instructions need to access the CPU and the memory at construction time.
    instructionDictionary = new HashMap<>();
     
    //ALU R-Type 32-bits
    instructionDictionary.put("ADD", ADD::new);
    instructionDictionary.put("ADDU", ADDU::new);
    instructionDictionary.put("SUB", SUB::new);
    instructionDictionary.put("SUBU", SUBU::new);
    instructionDictionary.put("DIV", DIV::new);
    instructionDictionary.put("DIVU", DIVU::new);
    instructionDictionary.put("MULT", MULT::new);
    instructionDictionary.put("MULTU", MULTU::new);

    //ALU I-Type 32-bits
    instructionDictionary.put("ADDI", ADDI::new);
    instructionDictionary.put("ADDIU", ADDIU::new);

    //ALU Shifting 32-bits
    instructionDictionary.put("SLL", SLL::new);
    instructionDictionary.put("SLLV", SLLV::new);
    instructionDictionary.put("SRA", SRA::new);
    instructionDictionary.put("SRAV", SRAV::new);
    instructionDictionary.put("SRL", SRL::new);
    instructionDictionary.put("SRLV", SRLV::new);

    //ALU R-Type
    instructionDictionary.put("AND", AND::new);
    instructionDictionary.put("DADD", DADD::new);
    instructionDictionary.put("DADDU", DADDU::new);
    instructionDictionary.put("DSUB", DSUB::new);
    instructionDictionary.put("DSUBU", DSUBU::new);
    instructionDictionary.put("OR", OR::new);
    instructionDictionary.put("SLT", SLT::new);
    instructionDictionary.put("SLTU", SLTU::new);
    instructionDictionary.put("XOR", XOR::new);
    instructionDictionary.put("MOVN", MOVN::new);
    instructionDictionary.put("MOVZ", MOVZ::new);
    instructionDictionary.put("DDIV", DDIV::new);
    instructionDictionary.put("DDIVU", DDIVU::new);
    instructionDictionary.put("DMUHU", DMUHU::new);
    instructionDictionary.put("DMULT", DMULT::new);
    instructionDictionary.put("DMULU", DMULU::new);
    instructionDictionary.put("DMULTU", DMULTU::new);
    instructionDictionary.put("MFLO", MFLO::new);
    instructionDictionary.put("MFHI", MFHI::new);

    //ALU I-Type
    instructionDictionary.put("ANDI", ANDI::new);
    instructionDictionary.put("DADDI", DADDI::new);
    instructionDictionary.put("DADDUI", DADDUI::new);
    instructionDictionary.put("DADDIU", DADDIU::new);
    instructionDictionary.put("LUI", LUI::new);
    instructionDictionary.put("ORI", ORI::new);
    instructionDictionary.put("SLTI", SLTI::new);
    instructionDictionary.put("SLTIU", SLTIU::new);
    instructionDictionary.put("XORI", XORI::new);

    //ALU Shifting
    instructionDictionary.put("DSLL", DSLL::new);
    instructionDictionary.put("DSLLV", DSLLV::new);
    instructionDictionary.put("DSRA", DSRA::new);
    instructionDictionary.put("DSRAV", DSRAV::new);
    instructionDictionary.put("DSRL", DSRL::new);
    instructionDictionary.put("DSRLV", DSRLV::new);

    //Load-Signed
    instructionDictionary.put("LB", () -> new LB(memory));
    instructionDictionary.put("LH", () -> new LH(memory));
    instructionDictionary.put("LW", () -> new LW(memory));
    instructionDictionary.put("LD", () -> new LD(memory));

    //Load-Unsigned
    instructionDictionary.put("LBU", () -> new LBU(memory));
    instructionDictionary.put("LHU", () -> new LHU(memory));
    instructionDictionary.put("LWU", () -> new LWU(memory));

    //Store
    instructionDictionary.put("SB", () -> new SB(memory));
    instructionDictionary.put("SH", () -> new SH(memory));
    instructionDictionary.put("SW", () -> new SW(memory));
    instructionDictionary.put("SD", () -> new SD(memory));

    //Unconditional branches
    instructionDictionary.put("J", J::new);
    instructionDictionary.put("JAL", JAL::new);
    instructionDictionary.put("JALR", JALR::new);
    instructionDictionary.put("JR", JR::new);
    instructionDictionary.put("B", B::new);

    //Conditional branches
    instructionDictionary.put("BEQ", BEQ::new);
    instructionDictionary.put("BNE", BNE::new);
    instructionDictionary.put("BNEZ", BNEZ::new);
    instructionDictionary.put("BEQZ", BEQZ::new);
    instructionDictionary.put("BGEZ", BGEZ::new);

    //Special instructions
    instructionDictionary.put("NOP", NOP::new);
    instructionDictionary.put("HALT", HALT::new);
    instructionDictionary.put("TRAP", () -> new TRAP(memory, iom));
    instructionDictionary.put("SYSCALL", () -> new SYSCALL(memory, iom));
    instructionDictionary.put("BREAK", BREAK::new);

    //Floating point instructions
    //Arithmetic
    instructionDictionary.put("ADD_D", () -> new ADD_D(cpu.getFCSR()));
    instructionDictionary.put("SUB_D", () -> new SUB_D(cpu.getFCSR()));
    instructionDictionary.put("MUL_D", () -> new MUL_D(cpu.getFCSR()));
    instructionDictionary.put("DIV_D", () -> new DIV_D(cpu.getFCSR()));

    //Load store
    instructionDictionary.put("LDC1", () -> new LDC1(memory));
    instructionDictionary.put("L_D", () -> new L_D(memory));
    instructionDictionary.put("SDC1", () -> new SDC1(memory));
    instructionDictionary.put("S_D", () -> new S_D(memory));
    instructionDictionary.put("LWC1", () -> new LWC1(memory));
    instructionDictionary.put("SWC1", () -> new SWC1(memory));

    //Move to and from
    instructionDictionary.put("DMTC1", DMTC1::new);
    instructionDictionary.put("DMFC1", DMFC1::new);
    instructionDictionary.put("MTC1", MTC1::new);
    instructionDictionary.put("MFC1", MFC1::new);

    //Formatted operand move
    instructionDictionary.put("MOV_D", MOV_D::new);
    instructionDictionary.put("MOVZ_D", MOVZ_D::new);
    instructionDictionary.put("MOVN_D", MOVN_D::new);

    //Special arithmetic instructions
    instructionDictionary.put("C_LT_D", C_LT_D::new);
    instructionDictionary.put("C_EQ_D", C_EQ_D::new);

    //Conditional branches instructions
    instructionDictionary.put("BC1T", BC1T::new);
    instructionDictionary.put("BC1F", BC1F::new);

    //Conditional move on CC instructions
    instructionDictionary.put("MOVT_D", MOVT_D::new);
    instructionDictionary.put("MOVF_D", MOVF_D::new);

    //Conversion instructions
    instructionDictionary.put("CVT_L_D", CVT_L_D::new);
    instructionDictionary.put("CVT_D_L", CVT_D_L::new);
    instructionDictionary.put("CVT_W_D", CVT_W_D::new);
    instructionDictionary.put("CVT_D_W", CVT_D_W::new);
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
    instruction.setDinero(dinero);
    return instruction;
  }
}
