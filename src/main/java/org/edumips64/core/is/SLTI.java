/*
 * SLTI.java
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.edumips64.core.is;

import org.edumips64.core.*;
import org.edumips64.utils.*;

/**
 * <pre>
 *      Syntax: SLTI rt, rs, immediate
 * Description: Records the result of a less-than comparison with a constant
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */

class SLTI extends ALU_IType {
  final String OPCODE_VALUE = "001010";
  SLTI() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    this.name = "SLTI";
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException {
    //getting values from temporary registers
    long imm = TR[IMM_FIELD].getValue();
    long rs = TR[RS_FIELD].getValue();

    //comparing values without to control integer overflow
    if (rs < imm) {
      TR[RT_FIELD].writeDoubleWord(1);
    } else {
      TR[RT_FIELD].writeDoubleWord(0);
    }

    if (cpu.isEnableForwarding()) {
      doWB();
    }

  }
}
