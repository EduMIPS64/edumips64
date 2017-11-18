/*
 * XORI.java
 *
 * 22th may 2006
 * Instruction SLTI of the MIPS64 Instruction Set
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
 * Foundation,  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.edumips64.core.is;

import org.edumips64.core.*;
import org.edumips64.core.fpu.FPInvalidOperationException;

/**
 * <pre>
 * Syntax:        XORI rt, rs, immediate
 * Description:   rt = rs XOR immediate
 *                To do a bitwise logical Exclusive OR with a constant
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */

class XORI extends ALU_IType {
  final String OPCODE_VALUE = "001110";
  XORI() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    this.name = "XORI";
  }

  //since this operation is carried out with zero padding of the immediate, //against sign_extend(immediate) methodology
  //of all others instructions in the same category, it is necessary the overriding of the ID method
  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    //if the source register is valid passing its own values into a temporary register
    Register rs = cpu.getRegister(params.get(RS_FIELD));

    if (rs.getWriteSemaphore() > 0) {
      return true;
    }

    TR[RS_FIELD] = rs;
    //locking the target register
    Register rt = cpu.getRegister(params.get(RT_FIELD));
    rt.incrWriteSemaphore();
    //writing the immediate value of "params" on a temporary register
    TR[IMM_FIELD].writeHalf(params.get(IMM_FIELD));
    //forcing zero-padding in the same temporary register
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < 48; i++) {
      sb.append('0');
    }

    sb.append(TR[IMM_FIELD].getBinString().substring(48, 64));
    TR[IMM_FIELD].setBits(sb.substring(0), 0);
    return false;
  }
  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException {
    //getting values from temporary registers
    String imm = TR[IMM_FIELD].getBinString();
    String rs = TR[RS_FIELD].getBinString();
    boolean rsbit, immbit, result;
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < 64; i++) {
      //XORI
      rsbit = rs.charAt(i) == '1';
      immbit = imm.charAt(i) == '1';
      result = rsbit ^ immbit;
      sb.append(result ? '1' : '0');
    }

    TR[RT_FIELD].setBits(sb.substring(0), 0);

    if (cpu.isEnableForwarding()) {
      doWB();
    }


  }

}
