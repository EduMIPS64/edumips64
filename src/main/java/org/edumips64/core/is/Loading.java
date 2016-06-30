/*
 * Loading.java
 *
 * 26th may 2006
 * Subclass of the MIPS64 Instruction Set
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

import java.util.logging.Logger;

import org.edumips64.core.*;
import org.edumips64.utils.*;

/** This is the base class for loading instruction
 *
 * @author  Trubia Massimo, Russo Daniele
 */
public abstract class Loading extends LDSTInstructions {
  protected static final Logger logger = Logger.getLogger(Loading.class.getName());

  Loading(Memory memory) {
    super(memory);
  }

  public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException {
    //if the base register is valid ...
    Register base = cpu.getRegister(params.get(BASE_FIELD));

    if (base.getWriteSemaphore() > 0) {
      logger.info("RAW in " + fullname + ": base register still needs to be written to.");
      throw new RAWException();
    }

    //calculating  address (base+offset)
    long address = base.getValue() + params.get(OFFSET_FIELD);
    //saving address into a temporary register
    TR[OFFSET_PLUS_BASE].writeDoubleWord(address);
    //locking rt register
    Register rt = cpu.getRegister(params.get(RT_FIELD));
    rt.incrWriteSemaphore();
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, NotAlignException, AddressErrorException {
    // Will fill in the address variable.
    super.EX();

    // Save memory access for Dinero trace file
    dinero.Load(Converter.binToHex(Converter.positiveIntToBin(64, address)), memoryOpSize);
  }

  public void WB() throws IrregularStringOfBitsException {
    if (!isEnableForwarding()) {
      doWB();
    }
  }

  public void MEM() throws IrregularStringOfBitsException, NotAlignException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
    memEl = memory.getCellByAddress(address);
    doMEM();

    if (isEnableForwarding()) {
      doWB();
    }
  }

  public void doWB() throws IrregularStringOfBitsException {
    //passing memory value from temporary LMD register to the destination register and unlocking it
    cpu.getRegister(params.get(RT_FIELD)).setBits(TR[LMD_REGISTER].getBinString(), 0);
    cpu.getRegister(params.get(RT_FIELD)).decrWriteSemaphore();
  }
}

