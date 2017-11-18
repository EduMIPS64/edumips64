/*
 * JAL.java
 *
 *  20th may 2006
 * Instruction JAL of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo, Russo Daniele
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
import org.edumips64.core.fpu.FPInvalidOperationException;

/**
 * <pre>
 *      Syntax: JAL target
 * Description: To execute a procedure call within the current 256 MB-aligned region
 *              Place the return address link in GPR 31.  This is a
 *              PC-region branch (not PC-relative);
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 *
 */

public class JAL extends FlowControl_JType {
  final String OPCODE_VALUE = "000011";
  final int PC_VALUE = 0;

  /** Creates a new instance of J */
  JAL() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    this.name = "JAL";
  }

  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    //saving PC value into a temporary register
    cpu.getRegister(31).incrWriteSemaphore();  //deadlock !!!
    TR[PC_VALUE].writeDoubleWord(cpu.getPC().getValue() - 4);
    //converting INSTR_INDEX into a bynary value of 26 bits in length
    String instr_index = Converter.positiveIntToBin(28, params.get(INSTR_INDEX));
    //appending the 35 most significant bits of the program counter on the left of "instr_index"
    Register pc = cpu.getPC();
    String pc_all = pc.getBinString();
    String pc_significant = pc_all.substring(0, 36);
    String pc_new = pc_significant + instr_index;
    pc.setBits(pc_new, 0);

    if (cpu.isEnableForwarding()) {
      doWB();
    }

    throw new JumpException();
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, IrregularWriteOperationException {
  }

  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
  }


  public void WB() throws IrregularStringOfBitsException {
    if (!cpu.isEnableForwarding()) {
      doWB();
    }
  }
  public void doWB() throws IrregularStringOfBitsException {
    cpu.getRegister(31).setBits(TR[PC_VALUE].getBinString(), 0);
    cpu.getRegister(31).decrWriteSemaphore();
  }


}
