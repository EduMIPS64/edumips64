/*
 * FPStoring.java
 *
 * 27th may 2007
 * (c) 2006 EduMips64 project - Trubia Massimo
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License. * *
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
import org.edumips64.core.fpu.RegisterFP;


/** This is the base class for the storing instructions
 *
 * @author Massimo
 */
public abstract class FPStoring extends FPLDSTInstructions {

  FPStoring(Memory memory) {
    super(memory);
  }

  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    //if the base register and the ft register are valid passing value of ft register into a temporary floating point register
    Register base = cpu.getRegister(params.get(BASE_FIELD));
    RegisterFP ft = cpu.getRegisterFP(params.get(FT_FIELD));

    if (base.getWriteSemaphore() > 0 || ft.getWriteSemaphore() > 0) {
      return true;
    }

    TR[FT_FIELD].setBits(ft.getBinString(), 0);
    //calculating  address (base+offset)
    long address = base.getValue() + params.get(OFFSET_FIELD);
    //saving address into a temporary register
    TR[OFFSET_PLUS_BASE].writeDoubleWord(address);
    return false;
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException {}

  public void MEM() throws IrregularStringOfBitsException, NotAlignException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {}

  public void WB() throws IrregularStringOfBitsException {}

}
