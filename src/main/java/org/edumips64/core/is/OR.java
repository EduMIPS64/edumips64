/*
 * OR.java
 *
 *  on 15 maggio 2006, 19.35
 * Instruction OR of the MIPS64 Instruction Set
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

import org.edumips64.core.IrregularStringOfBitsException;

/**
 * <pre>
 * Syntax:   OR rd, rs, rt
 * Description:  rd = rs or rt
 *               To do a bitwise logical OR
 *               The contents of GPR rs are combined with the contents of
 *               GPR rt in a bitwise logical OR operation.
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */
public class OR extends ALU_RType {
  public String OPCODE_VALUE = "100101";
  /** Creates a new instance of OR */
  OR() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "OR";
  }


  public void EX()
  throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
    //getting strings from temporary registers
    String rs = TR[RS_FIELD].getBinString();
    String rt = TR[RT_FIELD].getBinString();
    //performing bitwise OR between string values
    boolean rsbit, rtbit, resbit;
    String outputstring = "";

    for (int i = 0; i < 64; i ++) {
      rsbit = rs.charAt(i) == '1' ? true : false;
      rtbit = rt.charAt(i) == '1' ? true : false;

      resbit = rsbit || rtbit;
      outputstring += (resbit == true ? 1 : 0);
    }

    //saving bitwise AND result into a temporary register
    TR[RD_FIELD].setBits(outputstring, 0);

    if (cpu.isEnableForwarding()) {
      doWB();
    }

  }
}
