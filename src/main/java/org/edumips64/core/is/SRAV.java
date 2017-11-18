/*
 * SRAV.java
 *
 * 18th may 2007
 * Instruction SRL of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - UrzÃ¬ Erik - Sciuto Lorenzo - Giorgio Scibilia
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
import org.edumips64.core.IrregularStringOfBitsException;

/**
 * <pre>
 *      Syntax: SRAV rd, rt, rs
 * Description: To execute an arithmetic right-shift of a word by a variable number of bits
 *              The contents of the low-order 32-bit word of GPR rt are shifted right, duplicating the sign-bit (bit 31) in the emptied
 *    bits; the word result is sign-extended and placed in GPR rd. The bit-shift amount is specified by the low-order 5 bits
 *    of GPR rs.
 *</pre>
 * @author UrzÃ¬ Erik - Sciuto Lorenzo - Giorgio Scibilia
 */
public class SRAV extends ALU_RType {
  final int RD_FIELD = 0;
  final int RT_FIELD = 1;
  final int RS_FIELD = 2;
  final int RD_FIELD_INIT = 11;
  final int RT_FIELD_INIT = 16;
  final int RS_FIELD_INIT = 21;
  final int RD_FIELD_LENGTH = 5;
  final int RT_FIELD_LENGTH = 5;
  final int RS_FIELD_LENGTH = 5;
  final String OPCODE_VALUE = "000111";

  SRAV() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "SRAV";
    syntax = "%R,%R,%R";
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
    //getting strings from temporary registers
    int rs = (int) TR[RS_FIELD].getValue();
    String rt = TR[RT_FIELD].getBinString();
    //cutting the high part of register
    rt = rt.substring(32, 64);
    //composing new shifted value
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < 32 + rs; i++) {
      sb.append(rt.charAt(0));
    }

    sb.append(rt.substring(0, 32 - rs));
    TR[RD_FIELD].setBits(sb.substring(0), 0);

    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }

}
