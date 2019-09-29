/*
 * SLLV.java
 *
 * 18th may 2007
 * Instruction SLLV of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Erik UrzÃ¬ - Giorgio Scibilia - Sciuto Lorenzo
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
 *      Syntax: SLLV rd, rt, rs word shift left logical
 * Description: To execute a left-shift of a word by a fixed amount of 0 to 31 bits
 *              The 32-bit word contents of GPR rt are shifted left,
 *              inserting zeros into the emptied bits; the result is sign-extended and placed in GPR rd.
 *              The bit-shift amount is specified by the low-order 5 bits of GPR rs.
 *</pre>
 * @author Erik UrzÃ¬ - Giorgio Scibilia - Sciuto Lorenzo
 */
public class SLLV extends ALU_RType {
  private static final int RD_FIELD = 0;
  private static final int RT_FIELD = 1;
  private static final int RS_FIELD = 2;
  private static final String OPCODE_VALUE = "000100";
  SLLV() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "SLLV";
    syntax = "%R,%R,%R";
  }

  @Override
  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
    //getting strings from temporary registers
    String rt = TR[RT_FIELD].getBinString();
    String rs = TR[RS_FIELD].getBinString();
    String shift = "";
    //getting the low order 5 bits from rs register
    shift = rs.substring(59);
    //cutting the high part of register
    rt = rt.substring(32, 64);
    int shiftValue = Converter.binToInt(shift, true);
    //composing new shifted value and performing sign extension
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < 32; i++) {
      buf.append(rt.charAt(0));
    }

    buf.append(rt.substring(shiftValue));

    //filling the remaining bits with 0
    for (int i = 0; i < shiftValue; i++) {
      buf.append('0');
    }

    String target = new String(buf);

    TR[RD_FIELD].setBits(target, 0);

    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }
}
