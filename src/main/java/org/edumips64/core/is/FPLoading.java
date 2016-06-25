/*
 * FPLoading.java
 *
 * 27th may 2007
 * Subclass of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo
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
import org.edumips64.utils.*;

/** This is the base class for loading instruction
 *
 * @author  Trubia Massimo
 */
public abstract class FPLoading extends FPLDSTInstructions {

  public FPLoading(Memory memory) {
    super(memory);
  }

  public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, WAWException {
    //if the base register is valid ...
    Register base = cpu.getRegister(params.get(BASE_FIELD));

    if (base.getWriteSemaphore() > 0) {
      throw new RAWException();
    }

    //calculating  address (base+offset)
    long address = base.getValue() + params.get(OFFSET_FIELD);
    //saving address into a temporary register
    TR[OFFSET_PLUS_BASE].writeDoubleWord(address);
    //locking ft register either in write mode or in read mode
    RegisterFP ft = cpu.getRegisterFP(params.get(FT_FIELD));

    if (ft.getWAWSemaphore() > 0) {
      throw new WAWException();
    }

    ft.incrWriteSemaphore();
    ft.incrWAWSemaphore();
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException {
  }

  public void MEM() throws IrregularStringOfBitsException, NotAlignException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
    //since the load instruction reaches the MEM() stage, the (read) lock can be removed because WB() is reached first by the load instruction
    cpu.getRegisterFP(params.get(FT_FIELD)).decrWAWSemaphore();
  }

  public void WB() throws IrregularStringOfBitsException {
    if (!isEnableForwarding()) {
      doWB();
    }
  }

  public void doWB() throws IrregularStringOfBitsException {
    //passing memory value from temporary LMD register to the destination register and unlocking it
    cpu.getRegisterFP(params.get(FT_FIELD)).setBits(TR[LMD_REGISTER].getBinString(), 0);
    cpu.getRegisterFP(params.get(FT_FIELD)).decrWriteSemaphore();
  }
}

