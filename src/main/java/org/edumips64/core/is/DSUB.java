/*
 * DSUB.java
 *
 * 15th may 2006
 * Instruction DSUB of the MIPS64 Instruction Set
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
import org.edumips64.utils.*;
/**<pre>
 *      Syntax: DSUB rd, rs, rt
 * Description: To subtract 64-bit integers; trap on overflow
 *               The 64-bit doubleword value in GPR rt is subtracted from
 *              the 64-bit value in GPR rs to produce a 64-bit result.
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 */
public class DSUB extends ALU_RType {
  final String OPCODE_VALUE = "101110";
  /** Creates a new instance of DSUB */
  DSUB() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "DSUB";
  }
  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
    //getting strings from temporary registers
    String rs = TR[RS_FIELD].getBinString();
    String rt = TR[RT_FIELD].getBinString();
    //performing sign extension to detect IntegerOverflow
    rs = rs.charAt(0) + rs;
    rt = rt.charAt(0) + rt;
    String outputstring = InstructionsUtils.twosComplementSubstraction(rs, rt);

    //comparison between the two most significant bits of the outputstring and
    //raising integer overflow if the first bit is different from the second one
    if (outputstring.charAt(0) != outputstring.charAt(1)) {
      //if the enable forwarding is turned on we have to ensure that registers
      //should be unlocked also if a synchronous exception occurs. This is performed
      //by executing the WB method before raising the trap
      if (isEnableForwarding()) {
        doWB();
      }

      throw new IntegerOverflowException();
    } else {
      outputstring = outputstring.substring(1, 65);
    }

    TR[RD_FIELD].setBits(outputstring, 0);

    if (isEnableForwarding()) {
      doWB();
    }

  }


}
