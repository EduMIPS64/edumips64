/*
 * FPMoveInstructions.java
 *
 * 16th july 2007
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
import org.edumips64.core.fpu.FPInvalidOperationException;
import org.edumips64.core.fpu.RegisterFP;

/**This is the base class of the move to and from instructions
 *
 * @author Trubia Massimo
 */

public abstract class FPMoveToInstructions extends FPMoveToAndFromInstructions {

  FPMoveToInstructions() {
  }

  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    //if source registers are valid we pass their own values into temporary registers
    RegisterFP fs = cpu.getRegisterFP(params.get(FS_FIELD));
    Register rt = cpu.getRegister(params.get(RT_FIELD));

    if (rt.getWriteSemaphore() > 0) {
      return true;
    }

    TRfp[FS_FIELD].setBits(fs.getBinString(), 0);
    TR[RT_FIELD].setBits(rt.getBinString(), 0);

    //locking the destination register
    if (fs.getWAWSemaphore() > 0) {
      throw new WAWException();
    }

    fs.incrWriteSemaphore();
    fs.incrWAWSemaphore();
    return false;
  }
  public abstract void EX() throws IrregularStringOfBitsException;
  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
    cpu.getRegisterFP(params.get(FS_FIELD)).decrWAWSemaphore();
  }

  public void WB() throws IrregularStringOfBitsException {
    if (!cpu.isEnableForwarding()) {
      doWB();
    }
  }

  public void doWB() throws IrregularStringOfBitsException {
    //passing result from temporary register to destination register and unlocking it
    cpu.getRegisterFP(params.get(FS_FIELD)).setBits(TRfp[FS_FIELD].getBinString(), 0);
    cpu.getRegisterFP(params.get(FS_FIELD)).decrWriteSemaphore();

  }
}






