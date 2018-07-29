/*
 * FPConditionalBranchesInstructions.java
 *
 * 20th july 2007
 * Subgroup of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
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

/** This is the base class for the FP conditional branches instructions
 *
 * @author Trubia Massimo
 */
public abstract class FPConditionalBranchesInstructions extends FlowControlInstructions {
  final static int CC_FIELD = 0;
  final static int CC_FIELD_INIT = 11;
  final static int CC_FIELD_LENGTH = 3;
  final static int OFFSET_FIELD = 1;
  final static int OFFSET_FIELD_INIT = 16;
  final static int OFFSET_FIELD_LENGTH = 16;
  static String COP1_VALUE = "010001";
  final static int COP1_FIELD_INIT = 0;
  static String BC_VALUE = "01000";
  final static int BC_FIELD_INIT = 6;
  final static int ND_FIELD_INIT = 14;
  final static int TF_FIELD_INIT = 15;

  String ND_FIELD = "";
  String TF_FIELD = "";
  String CC_FIELD_VALUE = "";
  FPConditionalBranchesInstructions() {
    this.syntax = "%C,%B";
    this.paramCount = 2;
  }

  public abstract boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException, BreakException, WAWException, FPInvalidOperationException;

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, IrregularWriteOperationException {
  }

  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
  }

  public void WB() throws IrregularStringOfBitsException {
  }

  public void pack() throws IrregularStringOfBitsException {
    repr.setBits(COP1_VALUE, COP1_FIELD_INIT);
    repr.setBits(BC_VALUE, BC_FIELD_INIT);
    repr.setBits(Converter.intToBin(CC_FIELD_LENGTH, params.get(CC_FIELD)), CC_FIELD_INIT);
    repr.setBits(ND_FIELD, ND_FIELD_INIT);
    repr.setBits(TF_FIELD, TF_FIELD_INIT);
    repr.setBits(Converter.intToBin(OFFSET_FIELD_LENGTH, params.get(OFFSET_FIELD) / 4), OFFSET_FIELD_INIT);
  }

}
