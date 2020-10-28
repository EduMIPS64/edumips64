/*
 * LUI.java
 *
 * 21th may 2006
 * Instruction LUI of the MIPS64 Instruction Set
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

import org.edumips64.core.Converter;
import org.edumips64.core.IrregularStringOfBitsException;
import org.edumips64.core.IrregularWriteOperationException;
import org.edumips64.core.Register;
import org.edumips64.core.fpu.FPInvalidOperationException;


/** <pre>
 *  Format:        LUI rt, rs, immediate
 *  Description:   The 16-bit immediate is shifted left 16 bits and concatenated
 *                 with 16 bits of low-order zeros.
 *</pre>
  * @author Trubia Massimo, Russo Daniele
 */
class LUI extends ALU_IType {
  LUI() {
    syntax = "%R,%I";
    super.OPCODE_VALUE = "001111";
    IMM_FIELD = 1;
    this.name = "LUI";
  }

  @Override
  public boolean ID() throws IntegerOverflowException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    checkImmediateForOverflow();
    //if the source register is valid passing its own values into a temporary register
    //locking the target register
    Register rt = cpu.getRegister(params.get(RT_FIELD));
    rt.incrWriteSemaphore();
    //writing the immediate value of "params" on a temporary register
    TR[IMM_FIELD].writeHalf(params.get(IMM_FIELD));
    return false;
  }

  @Override
  public void EX() throws IrregularStringOfBitsException, IrregularWriteOperationException {
    //getting strings from temporary registers
    String imm = TR[IMM_FIELD].getBinString().substring(16, 64);
    String shift = imm + "0000000000000000";
    long shiftLong = Converter.binToLong(shift, false);
    TR[RT_FIELD].writeDoubleWord(shiftLong);

    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }

  @Override
  public void pack() throws IrregularStringOfBitsException {
    repr.setBits(OPCODE_VALUE, 0);
    repr.setBits(Converter.intToBin(RS_FIELD_LENGTH, 0), RS_FIELD_INIT);
    repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
    repr.setBits(Converter.intToBin(IMM_FIELD_LENGTH, params.get(IMM_FIELD)), IMM_FIELD_INIT);
  }

}
