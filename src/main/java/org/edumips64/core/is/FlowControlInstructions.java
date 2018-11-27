/*
 * FlowControlInstructions.java
 *
 * 15th may 2006
 * Subgroup of the MIPS64 Instruction Set
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
import org.edumips64.core.fpu.FPInvalidOperationException;


/**This is the base class for FlowControl instructions
 *
 * @author Trubia Massimo, Russo Daniele
 */
public abstract class FlowControlInstructions extends Instruction {
  public abstract void IF() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, PredictedJumpException;
  public abstract boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException, UntakenBranchException, TakenBranchException, BreakException, WAWException, FPInvalidOperationException;
  public abstract void EX() throws IrregularStringOfBitsException, IntegerOverflowException, IrregularWriteOperationException;
  public abstract void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException;
  public abstract void WB() throws IrregularStringOfBitsException;
  public abstract void pack() throws IrregularStringOfBitsException;

}
