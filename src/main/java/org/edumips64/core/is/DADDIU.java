/*
 * DADDIU.java
 *
 * 21th may 2006
 * Instruction DADDIU of the MIPS64 Instruction Set
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
 *        Syntax: DADDIU rt, rs, immediate
 *   Description: To add a constant to a 64-bit integer
 *                The 16-bit signed immediate is added to the 64-bit value
 *                in GPR rs and the 64-bit arithmetic result is placed into
 *                GPR rt. No Integer Overflow exception occurs under any circumstances.
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */

class DADDIU extends ALU_IType {
  final String OPCODE_VALUE = "011001";
  DADDIU() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    this.name = "DADDIU";
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException {
    //getting values from temporary registers
    long imm = TR[IMM_FIELD].getValue();
    long rs = TR[RS_FIELD].getValue();
    //adding values without to control integer overflow
    long result = imm + rs;
    TR[RT_FIELD].writeDoubleWord(result);

    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }
}
