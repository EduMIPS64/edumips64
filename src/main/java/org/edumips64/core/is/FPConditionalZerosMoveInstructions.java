/*
 * FPConditionalZerosMoveInstructions.java
 *
 * 17th july 2007
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

public abstract class FPConditionalZerosMoveInstructions extends ComputationalInstructions {
  final static int FD_FIELD = 0;
  final static int FD_FIELD_INIT = 21;
  final static int FD_FIELD_LENGTH = 5;
  final static int FS_FIELD = 1;
  final static int FS_FIELD_INIT = 16;
  final static int FS_FIELD_LENGTH = 5;
  final static int RT_FIELD = 2;
  final static int RT_FIELD_INIT = 11;
  final static int RT_FIELD_LENGTH = 5;
  static String COP1_FIELD = "010001";
  static int COP1_FIELD_INIT = 0;
  static int OPCODE_VALUE_INIT = 26;
  static int FMT_FIELD_INIT = 6;

  String OPCODE_VALUE = "";
  String FMT_FIELD = "";
  FPConditionalZerosMoveInstructions() {
    this.syntax = "%F,%F,%R";
    this.paramCount = 3;
  }
  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    //if the source register is valid we pass its own value into a temporary register
    RegisterFP fd = cpu.getRegisterFP(params.get(FD_FIELD));
    RegisterFP fs = cpu.getRegisterFP(params.get(FS_FIELD));
    Register rt = cpu.getRegister(params.get(RT_FIELD));

    if (fs.getWriteSemaphore() > 0 || rt.getWriteSemaphore() > 0) {
      return true;
    }

    TRfp[FS_FIELD].setBits(fs.getBinString(), 0);
    TRfp[FD_FIELD].setBits(fd.getBinString(), 0);
    TR[RT_FIELD].setBits(rt.getBinString(), 0);

    //locking the destination register
    if (fd.getWAWSemaphore() > 0) {
      throw new WAWException();
    }

    fd.incrWriteSemaphore();
    fd.incrWAWSemaphore();
    return false;
  }
  public abstract void EX() throws IrregularStringOfBitsException;
  public void MEM() throws MemoryElementNotFoundException {
    cpu.getRegisterFP(params.get(FD_FIELD)).decrWAWSemaphore();
  };
  public void WB() throws IrregularStringOfBitsException {
    if (!cpu.isEnableForwarding()) {
      doWB();
    }
  }

  public void doWB() throws IrregularStringOfBitsException {
    //passing result from temporary register to destination register and unlocking it
    cpu.getRegisterFP(params.get(FD_FIELD)).setBits(TRfp[FD_FIELD].getBinString(), 0);
    cpu.getRegisterFP(params.get(FD_FIELD)).decrWriteSemaphore();
  }

  public void pack() throws IrregularStringOfBitsException {
    //conversion of instruction parameters of params list to the "repr" 32 binary value
    repr.setBits(COP1_FIELD, COP1_FIELD_INIT);
    repr.setBits(FMT_FIELD, FMT_FIELD_INIT);
    repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
    repr.setBits(Converter.intToBin(FS_FIELD_LENGTH, params.get(FS_FIELD)), FS_FIELD_INIT);
    repr.setBits(Converter.intToBin(FD_FIELD_LENGTH, params.get(FD_FIELD)), FD_FIELD_INIT);
    repr.setBits(OPCODE_VALUE, OPCODE_VALUE_INIT);
  }

}






