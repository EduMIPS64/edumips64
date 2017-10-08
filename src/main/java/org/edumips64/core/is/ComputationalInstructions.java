/*
 * ComputationalInstructions.java
 *
 * 5th may 2006
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

/** This is the base class for all the ALU instructions
 *
 * @author Trubia Massimo, Russo Daniele
 */
package org.edumips64.core.is;
import org.edumips64.core.*;
import org.edumips64.core.fpu.*;
import org.edumips64.utils.*;

public abstract class ComputationalInstructions extends Instruction {
  public void IF() {
    try {
      dinero.IF(Converter.binToHex(Converter.intToBin(64, cpu.getLastPC().getValue())));
    } catch (IrregularStringOfBitsException e) {
      e.printStackTrace();
    }
  }
  public abstract boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException, JumpException, BreakException, WAWException, FPInvalidOperationException;
  public abstract void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException, DivisionByZeroException, FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, FPInvalidOperationException;
  public abstract void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException;
  public abstract void WB() throws IrregularStringOfBitsException;
  public abstract void pack() throws IrregularStringOfBitsException;


}








