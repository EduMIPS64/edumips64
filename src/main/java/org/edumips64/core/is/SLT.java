/*
 * SLT.java
 *
 * on 15 maggio 2006, 21.35
 * Instruction SLT of the MIPS64 Instruction Set
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

/**
* <pre>
*      Syntax: SLT rd, rs, rt
* Description: Records the result of a less-than comparison
*          i.e: rd = (rs < rt)
*
* </pre>
* @author Trubia Massimo, Russo Daniele
*/
public class SLT extends ALU_RType {
  final String OPCODE_VALUE = "101010";
  SLT() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "SLT";
  }
  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException {
    String rs = TR[RS_FIELD].getBinString();
    String rt = TR[RT_FIELD].getBinString();

    long rs_value = Converter.binToLong(rs, false);
    long rt_value = Converter.binToLong(rt, false);

    String rd = "";

    if (rs_value < rt_value) {
      for (int i = 0; i < 63; i++) {
        rd += '0';
      }

      rd = rd + '1';
    } else {
      for (int i = 0; i < 64; i ++) {
        rd += '0';
      }
    }

    TR[RD_FIELD].setBits(rd, 0);

    if (cpu.isEnableForwarding()) {
      doWB();
    }

  }
}
