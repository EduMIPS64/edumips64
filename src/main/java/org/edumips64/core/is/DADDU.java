/*
 * DADDU.java
 *
 * 15th may 2006
 * Instruction DADDU of the MIPS64 Instruction Set
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

import org.edumips64.utils.IrregularStringOfBitsException;

/**
 * <pre>
 *          Syntax: DADDU rd, rs, rt
 *     Description: rd = rs + rt
 *                  To add 64-bit integers
 *        The 64-bit doubleword value in GPR rt is added to the 64-bit value
 *        in GPR rs and the 64-bit arithmetic result is placed into GPR rd.
 *        No Integer Overflow exception occurs under any circumstances.
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */
public class DADDU extends ALU_RType {
  final String OPCODE_VALUE = "101101";
  DADDU() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "DADDU";
  }

  public void EX()
  throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
    //getting String from temporary register
    String rs = TR[RS_FIELD].getBinString();
    String rt = TR[RT_FIELD].getBinString();

    String outputstring = InstructionsUtils.twosComplementSum(rs, rt);
    //There isn't IntegerOverflow cases
    TR[RD_FIELD].setBits(outputstring, 0);

    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }
}
