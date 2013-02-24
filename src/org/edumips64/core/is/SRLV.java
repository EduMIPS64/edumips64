/*
 * SRLV.java
 *
 * 18th may 2007
 * Instruction SRLV of the MIPS64 Instruction Set
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
import org.edumips64.core.*;
import org.edumips64.utils.*;

/**
 * <pre>
 *      Syntax: SRLV rd, rt, rs
 * Description: To execute a logical right-shift of a word by a fixed amount of 0 to 31 bits
 *              The word contents of GPR rt are shifted right, inserting zeros
 *              into the emptied bits; the result is sign extended and placed in GPR rd.
 *</pre>
 * @author UrzÃ¬ Erik - Sciuto Lorenzo - Giorgio Scibilia
 */
public class SRLV extends ALU_RType {
  final int RD_FIELD = 0;
  final int RT_FIELD = 1;
  final int RS_FIELD = 2;
  final int RD_FIELD_INIT = 11;
  final int RT_FIELD_INIT = 16;
  final int RS_FIELD_INIT = 21;
  final int RD_FIELD_LENGTH = 5;
  final int RT_FIELD_LENGTH = 5;
  final int RS_FIELD_LENGTH = 5;
  final String OPCODE_VALUE = "000110";

  SRLV() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "SRLV";
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
    //getting strings from temporary registers
    String rs = TR[RS_FIELD].getBinString();
    String rt = TR[RT_FIELD].getBinString();
    //cutting the high part of registers
    rs = rs.substring(32, 64);
    rt = rt.substring(32, 64);
    //getting the low 5 bits from rs
    String shift = rs.substring(27);
    int shift_value = Converter.binToInt(shift, true);
    //composing new shifted value
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < 32 + shift_value; i++) {
      sb.append('0');
    }

    sb.append(rt.substring(0, 32 - shift_value));
    TR[RD_FIELD].setBits(sb.substring(0), 0);

    if (enableForwarding) {
      doWB();
    }
  }
}
