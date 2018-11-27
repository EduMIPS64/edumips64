/*
 * BGEZ.java
 *
 * Instruction BGEZ of the MIPS64 Instruction Set
 * (c) 2007 EduMips64 project - Andrea Milazzo (MancaUSoft)
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
import org.edumips64.core.fpu.FPInvalidOperationException;

/** <pre>
 *         Syntax: BGEZ rs, offset
 *    Description: if rs >= 0  then branch
 *                 To test a GPR then do a PC-relative conditional branch
 *</pre>
  * @author Andrea Milazzo
 */

public class BGEZ extends FlowControl_IType {
  final String OPCODE_VALUE = "000001";
  final static int OFFSET_FIELD = 1;
  final String RT_VALUE = "00001";

  /** Creates a new instance of BGEZ */
  BGEZ() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    syntax = "%R,%B";
    name = "BGEZ";
  }

  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, UntakenBranchException, TakenBranchException, BreakException, WAWException, FPInvalidOperationException {
    if (cpu.getRegister(params.get(RS_FIELD)).getWriteSemaphore() > 0) {
      return true;
    }

    //getting register rs
    String rs = cpu.getRegister(params.get(RS_FIELD)).getBinString();
    //converting offset into a signed binary value of 64 bits in length
    boolean condition = rs.charAt(0) == '0';

    UpdatePrediction(condition);

    if (prediction && !condition) {
      jumpBackToNormal();
    }
    else if (!prediction && condition){
      JumpBackToOffset(OFFSET_FIELD);
    }
    return false;
  }
  public void pack() throws IrregularStringOfBitsException {
    repr.setBits(OPCODE_VALUE, OPCODE_VALUE_INIT);
    repr.setBits(Converter.intToBin(RS_FIELD_LENGTH, params.get(RS_FIELD)), RS_FIELD_INIT);
    repr.setBits(RT_VALUE, RT_FIELD_INIT);
    repr.setBits(Converter.intToBin(OFFSET_FIELD_LENGTH, params.get(OFFSET_FIELD) / 4), OFFSET_FIELD_INIT);
  }

}
