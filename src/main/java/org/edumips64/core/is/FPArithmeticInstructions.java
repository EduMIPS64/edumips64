/*
 * FPArithmeticInstructions.java
 *
 * 30th may 2007
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
import org.edumips64.core.fpu.*;
//per diagnostica


/**This is the base class for the floatiing point arithmetic instructions
 *
 * @author Trubia Massimo
 */
public abstract class FPArithmeticInstructions extends ComputationalInstructions {
  private final static int FD_FIELD = 0;
  private final static int FS_FIELD = 1;
  private final static int FT_FIELD = 2;
  private static String COP1_FIELD = "010001";
  private final static int COP1_FIELD_INIT = 0;
  private final static int FD_FIELD_INIT = 21;
  private final static int FS_FIELD_INIT = 16;
  private final static int FT_FIELD_INIT = 11;
  private final static int FD_FIELD_LENGTH = 5;
  private final static int FS_FIELD_LENGTH = 5;
  private final static int FT_FIELD_LENGTH = 5;
  private final static int OPCODE_VALUE_INIT = 26;
  private final static int FMT_FIELD_INIT = 6;

  protected String OPCODE_VALUE = "";
  protected String FMT_FIELD = "";

  protected FPInstructionUtils fpInstructionUtils;

  FPArithmeticInstructions(FCSRRegister fcsr) {
    syntax = "%F,%F,%F";
    paramCount = 3;
    fpInstructionUtils = new FPInstructionUtils(fcsr);
  }

  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    //if source registers are valid passing their own values into temporary registers
    RegisterFP fs = cpu.getRegisterFP(params.get(FS_FIELD));
    RegisterFP ft = cpu.getRegisterFP(params.get(FT_FIELD));

    if (fs.getWriteSemaphore() > 0 || ft.getWriteSemaphore() > 0) {
      return true;
    }

    TRfp[FS_FIELD].setBits(fs.getBinString(), 0);
    TRfp[FT_FIELD].setBits(ft.getBinString(), 0);
    //locking the destination register
    RegisterFP fd = cpu.getRegisterFP(params.get(FD_FIELD));

    if (fd.getWAWSemaphore() > 0) {
      throw new WAWException();
    }

    fd.incrWriteSemaphore();
    fd.incrWAWSemaphore();
    return false;
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException, DivisionByZeroException, FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException {
    //getting values from temporary registers
    String operand1 = TRfp[FS_FIELD].getBinString();
    String operand2 = TRfp[FT_FIELD].getBinString();
    String outputstring = null;

    try {
      outputstring = doFPArith(operand1, operand2);
      TRfp[FD_FIELD].setBits(outputstring, 0);
    } catch (Exception ex) {
      //if the enable forwarding is turned on we have to ensure that registers
      //should be unlocked also if a synchronous exception occurs. This is performed
      //by executing the WB method before raising the trap
      if (cpu.isEnableForwarding()) {
        doWB();
      }

      if (ex instanceof FPInvalidOperationException) {
        throw new FPInvalidOperationException();
      } else if (ex instanceof FPUnderflowException) {
        throw new FPUnderflowException();
      } else if (ex instanceof FPOverflowException) {
        throw new FPOverflowException();
      } else if (ex instanceof FPDivideByZeroException) {
        throw new FPDivideByZeroException();
      } else if (ex instanceof IrregularStringOfBitsException) {
        throw new IrregularStringOfBitsException();
      }
    }

    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }

  protected abstract String doFPArith(String operand1, String operand2) throws FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, IrregularStringOfBitsException;

  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
    cpu.getRegisterFP(params.get(FD_FIELD)).decrWAWSemaphore();
  }

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
    //conversion of instruction parameters of "params" list to the "repr" form (32 binary value)
    repr.setBits(OPCODE_VALUE, OPCODE_VALUE_INIT);
    repr.setBits(Converter.intToBin(FS_FIELD_LENGTH, params.get(FS_FIELD)), FS_FIELD_INIT);
    repr.setBits(Converter.intToBin(FT_FIELD_LENGTH, params.get(FT_FIELD)), FT_FIELD_INIT);
    repr.setBits(Converter.intToBin(FD_FIELD_LENGTH, params.get(FD_FIELD)), FD_FIELD_INIT);
    repr.setBits(COP1_FIELD, COP1_FIELD_INIT);
    repr.setBits(FMT_FIELD, FMT_FIELD_INIT);
  }

}
