/*
 * SLTU.java
 *
 * 21th may 2006
 * Instruction SLTU of the MIPS64 Instruction Set
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
 *      Syntax: SLT rd, rs, rt
 * Description: Records the result of an unsigned less-than comparison
 *    (rs<rt) in the GPR rd. In case, an Integer Overflow
 *    exception will not occours.
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 *
 */
class SLTU extends ALU_RType {
  final String OPCODE_VALUE = "101011";


  SLTU() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "SLTU";
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException {
    //getting strings from temporary registers
    String rs = TR[RS_FIELD].getBinString();
    String rt = TR[RT_FIELD].getBinString();
    boolean rsbit, rtbit, diff, slt = false;

    //comparison between registers as unsigned integers
    for (int i = 0; i < 64; i++) {
      //XOR
      rsbit = rs.charAt(i) == '1' ? true : false;
      rtbit = rt.charAt(i) == '1' ? true : false;
      diff = rsbit ^ rtbit;

      if (diff) { //bits are different
        if (rtbit) { //rtbit is 1
          slt = true;
          break;
        }

        break;
      }

    }

    if (slt) {
      TR[RD_FIELD].writeDoubleWord(1);
    } else {
      TR[RD_FIELD].writeDoubleWord(0);
    }

    if (isEnableForwarding()) {
      doWB();
    }
  }


}
