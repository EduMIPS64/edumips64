/*
 * FlowControl_IType.java
 *
 * 15th may 2006
 * Subgroup of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo, Russo Daniele
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
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
import org.edumips64.core.fpu.FPInvalidOperationException;

/** This is the base class for immediate flow control instructions
 *
 * @author Trubia Massimo, Russo Daniele
 */
public abstract class FlowControl_IType extends FlowControlInstructions {
  final static int RS_FIELD = 0;
  final static int RT_FIELD = 1;
  final static int OFFSET_FIELD = 2;
  final static int RT_FIELD_INIT = 11;
  final static int RS_FIELD_INIT = 6;
  final static int OFFSET_FIELD_INIT = 16;
  final static int RT_FIELD_LENGTH = 5;
  final static int RS_FIELD_LENGTH = 5;
  final static int OFFSET_FIELD_LENGTH = 16;
  String OPCODE_VALUE = "";
  final static int OPCODE_VALUE_INIT = 0;
  protected boolean prediction = false;

  FlowControl_IType() {
    this.syntax = "%R,%R,%E";
    this.paramCount = 3;
  }

  // Used in ID stage of conditional instructions
  // call by BGEZ,BEQZ,BNE,BEQ,BNEZ
  void UpdatePrediction(boolean actual_outcome){
    int branch_address = (int)cpu.getPCBranch().getValue();
    String str_outcome = actual_outcome? "true" : "false";
    logger.info("Update the prediction for instruction: "
        + String.format("%04x",branch_address) + str_outcome);
    cpu.getBranchPredictor().UpdatePrediction(branch_address, actual_outcome);
  }

  // ID stage: Jump function for unconditional branch instructions only
  void jumpToOffset(int offsetField) throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException {
    BitSet64 bs = new BitSet64();
    bs.writeHalf(params.get(offsetField));
    String offset = bs.getBinString();

    Register pc = cpu.getPC();
    String pc_old = cpu.getPC().getBinString();

    //subtracting 4 to the pc_old temporary variable using bitset64 safe methods
    BitSet64 bs_temp = new BitSet64();
    bs_temp.writeDoubleWord(-4);
    pc_old = InstructionsUtils.twosComplementSum(pc_old, bs_temp.getBinString());

    //updating program counter
    String pc_new = InstructionsUtils.twosComplementSum(pc_old, offset);
    pc.setBits(pc_new, 0);

    throw new JumpException();
  }

  // ID stage: if the inst was predicted untaken but ACTUALLY TAKEN, call this function
  void JumpBackToOffset(int offsetField) throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, TakenBranchException {
    BitSet64 bs = new BitSet64();
    bs.writeHalf(params.get(offsetField));
    String offset = bs.getBinString();

    Register pc = cpu.getPC();
    String pc_old = cpu.getPC().getBinString();

    //subtracting 4 to the pc_old temporary variable using bitset64 safe methods
    BitSet64 bs_temp = new BitSet64();
    bs_temp.writeDoubleWord(-4);
    pc_old = InstructionsUtils.twosComplementSum(pc_old, bs_temp.getBinString());

    //update program counter
    String pc_new = InstructionsUtils.twosComplementSum(pc_old, offset);
    pc.setBits(pc_new, 0);

    throw new TakenBranchException();
  }

  // ID stage: if the inst was predicted taken but ACTUALLY UNTAKEN, call this function
  void jumpBackToNormal() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, UntakenBranchException {
    logger.info("Misprediction: Flush and refetch");
    Register pc = cpu.getPC();
    String str_pc_branch = cpu.getPCBranch().getBinString();

    //Adding 4 to the pc_old temporary variable using bitset64 safe methods
    BitSet64 bs_temp = new BitSet64();
    bs_temp.writeDoubleWord(4);
    str_pc_branch = InstructionsUtils.twosComplementSum(str_pc_branch, bs_temp.getBinString());
    //updating program counter
    pc.setBits(str_pc_branch, 0);

    throw new UntakenBranchException();
  }

  // IF stage: if the inst was predicted taken, call this function
  void JumpToTarget() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException {
    logger.info("Predicting Taken!");
    BitSet64 bs = new BitSet64();
    bs.writeHalf(params.get(OFFSET_FIELD));
    String offset = bs.getBinString();

    Register pc = cpu.getPC();
    String pc_old = cpu.getPCBranch().getBinString();

    //Adding 4 to the pc_old temporary variable using bitset64 safe methods
    BitSet64 bs_temp = new BitSet64();
    bs_temp.writeDoubleWord(4);
    pc_old = InstructionsUtils.twosComplementSum(pc_old, bs_temp.getBinString());

    //updating program counter
    logger.info("Setting pc to target");
    String pc_new = InstructionsUtils.twosComplementSum(pc_old, offset);
    pc.setBits(pc_new, 0);

  }

  public void IF() throws PredictedJumpException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException{
    try {
        dinero.IF(Converter.binToHex(Converter.intToBin(64, cpu.getLastPC().getValue())));
    } catch (IrregularStringOfBitsException e) {
        e.printStackTrace();
    }

    String pc_old = cpu.getPC().getBinString();
    //subtracting 4 to the pc_old temporary variable using bitset64 safe methods
    BitSet64 bs_temp = new BitSet64();
    bs_temp.writeDoubleWord(-4);
    pc_old = InstructionsUtils.twosComplementSum(pc_old, bs_temp.getBinString());

    //store the pc_branch
    cpu.getPCBranch().setBits(pc_old, 0);
    logger.info("New Program Counter of Branch: " + cpu.getPCBranch().toString());

    int branch_address = (int)cpu.getPCBranch().getValue();
    prediction = cpu.getBranchPredictor().getPrediction(branch_address, fullname);
    logger.info("Getting prediction for instruction " + String.format("%04x",branch_address));
    if (prediction) {
      JumpToTarget();
      throw new PredictedJumpException();
    }

  }

  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException, UntakenBranchException, TakenBranchException, BreakException, WAWException, FPInvalidOperationException {
    return false;
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, IrregularWriteOperationException {
  }

  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
  }

  public void WB() throws IrregularStringOfBitsException {
  }

  public void pack() throws IrregularStringOfBitsException {
    repr.setBits(OPCODE_VALUE, OPCODE_VALUE_INIT);
    repr.setBits(Converter.intToBin(RS_FIELD_LENGTH, params.get(RS_FIELD)), RS_FIELD_INIT);
    repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
    repr.setBits(Converter.intToBin(OFFSET_FIELD_LENGTH, params.get(OFFSET_FIELD) / 4), OFFSET_FIELD_INIT);
  }

}
