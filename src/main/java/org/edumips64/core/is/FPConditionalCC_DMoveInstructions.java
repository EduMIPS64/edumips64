/*
 * FPConditionalCC_DMoveInstructions.java
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
import org.edumips64.utils.*;

/**This is the base class of the conditional move to and from instructions
 *
 * @author Trubia Massimo
 */

public abstract class FPConditionalCC_DMoveInstructions extends ComputationalInstructions {
  final static int FD_FIELD = 0;
  final static int FD_FIELD_INIT = 21;
  final static int FD_FIELD_LENGTH = 5;
  final static int FS_FIELD = 1;
  final static int FS_FIELD_INIT = 16;
  final static int FS_FIELD_LENGTH = 5;
  final static int CC_FIELD = 2;
  final static int CC_FIELD_INIT = 11;
  final static int CC_FIELD_LENGTH = 3;
  static String COP1_FIELD = "010001";
  static int COP1_FIELD_INIT = 0;
  static int MOVCF_FIELD_INIT = 26;
  static String MOVCF_FIELD_VALUE = "010001";
  static String ZERO_FIELD = "0";
  final static int ZERO_FIELD_INIT = 14;
  static String FMT_FIELD = "10001"; //17 for double
  static int FMT_FIELD_INIT = 6;
  final static int TF_FIELD_INIT = 15;
  static CPU cpu = CPU.getInstance();

  int TF_FIELD_VALUE;

  public FPConditionalCC_DMoveInstructions() {
    this.syntax = "%F,%F,%C";
    this.paramCount = 3;
  }
  public void ID() throws RAWException, WAWException, IrregularStringOfBitsException {
    //if the source register is valid we pass its own value into a temporary register
    RegisterFP fd = cpu.getRegisterFP(params.get(FD_FIELD));
    RegisterFP fs = cpu.getRegisterFP(params.get(FS_FIELD));

    if (fs.getWriteSemaphore() > 0) {
      throw new RAWException();
    }

    TRfp[FS_FIELD].setBits(fs.getBinString(), 0);
    TRfp[FD_FIELD].setBits(fd.getBinString(), 0);

    //locking the destination register
    if (fd.getWAWSemaphore() > 0) {
      throw new WAWException();
    }

    fd.incrWriteSemaphore();
    fd.incrWAWSemaphore();
  }
  public void EX() throws IrregularStringOfBitsException {
    String fs = TRfp[FS_FIELD].getBinString();

    if (cpu.getFCSRConditionCode(params.get(CC_FIELD).intValue()) == TF_FIELD_VALUE) {
      TRfp[FD_FIELD].setBits(fs, 0);
    }
  }
  public void MEM() throws MemoryElementNotFoundException {
    cpu.getRegisterFP(params.get(FD_FIELD)).decrWAWSemaphore();
  }
  public void WB() throws IrregularStringOfBitsException {
    if (!isEnableForwarding()) {
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
    repr.setBits(Converter.intToBin(CC_FIELD_LENGTH, params.get(CC_FIELD)), CC_FIELD_INIT);
    repr.setBits(ZERO_FIELD, ZERO_FIELD_INIT);
    repr.setBits(String.valueOf(TF_FIELD_VALUE), TF_FIELD_INIT);
    repr.setBits(Converter.intToBin(FS_FIELD_LENGTH, params.get(FS_FIELD)), FS_FIELD_INIT);
    repr.setBits(Converter.intToBin(FD_FIELD_LENGTH, params.get(FD_FIELD)), FD_FIELD_INIT);
    repr.setBits(MOVCF_FIELD_VALUE, MOVCF_FIELD_INIT);
  }

}






