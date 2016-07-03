/*
 * XOR.java
 *
 * on 21 maggio 2006, 23.53
 *
 * Instruction XOR of the MIPS64 Instruction Set
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
 * Syntax:      XOR rd, rs, rt
 * Description: rd = rs XOR rt
 *              Combine the contents of GPR rs and GPR rt in a bitwise
 *              logical Exclusive OR operation

 * @author Trubia Massimo, Russo Daniele
 * </pre>
 */
public class XOR extends ALU_RType {
  public String OPCODE_VALUE = "100110";

  /** Creates a new instance of XOR */
  XOR() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "XOR";
  }

  public void EX() throws IrregularStringOfBitsException {
    String rs = TR[RS_FIELD].getBinString();
    String rt = TR[RT_FIELD].getBinString();
    String rd = "";

    rd = InstructionsUtils.xorOperation(rs, rt);
    TR[RD_FIELD].setBits(rd, 0);

    if (cpu.isEnableForwarding()) {
      doWB();
    }

  }
}
