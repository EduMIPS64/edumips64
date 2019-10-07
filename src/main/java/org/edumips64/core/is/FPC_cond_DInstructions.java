/*
 * FPC_cond_fmtInstructions.java
 *
 * 19th july 2007
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
import java.math.*;
//per diagnostica


/**This is the base class for the instructions of the type C.cond.fmt
 *
 * @author Trubia Massimo
 */
public abstract class FPC_cond_DInstructions extends ComputationalInstructions {
  private final static int CC_FIELD = 0;
  private final static int FS_FIELD = 1;
  private final static int FT_FIELD = 2;
  private final static String COP1_FIELD = "010001";
  private final static String CONST_FIELD = "0011";
  private final static int COP1_FIELD_INIT = 0;
  private final static int CONST_FIELD_INIT = 24;
  private final static int CC_FIELD_INIT = 21;
  private final static int FS_FIELD_INIT = 16;
  private final static int FT_FIELD_INIT = 11;
  private final static int CC_FIELD_LENGTH = 3;
  private final static int FS_FIELD_LENGTH = 5;
  private final static int FT_FIELD_LENGTH = 5;
  private final static int COND_VALUE_INIT = 28;
  private final static int FMT_FIELD_INIT = 6;
  private final static String FMT_FIELD = "10001"; // 17 is for double

  protected String COND_VALUE = "";

  FPC_cond_DInstructions() {
    syntax = "%C,%F,%F";
    paramCount = 3;
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
    return false;
  }

  public void EX() throws IrregularStringOfBitsException, FPInvalidOperationException {
    RegisterFP fs = TRfp[FS_FIELD];
    RegisterFP ft = TRfp[FT_FIELD];
    boolean less;
    boolean equal;
    boolean unordered;
    boolean condition;
    int condition_int;


    //truth mask
    boolean cond0 = COND_VALUE.charAt(3) == '1';  //codes the unordered predicate
    boolean cond1 = COND_VALUE.charAt(2) == '1';  //codes the equal predicate
    boolean cond2 = COND_VALUE.charAt(1) == '1';  //codes the less predicate

    if (FPInstructionUtils.isSNaN(fs.getBinString()) || FPInstructionUtils.isSNaN(ft.getBinString())
        || FPInstructionUtils.isQNaN(fs.getBinString()) || FPInstructionUtils.isQNaN(ft.getBinString())) {
      less = false;
      equal = false;
      unordered = true;

      //checking for invalid operation exception (if it is raised the FCSR isn't modified)
      //this exception occurs
      if (FPInstructionUtils.isSNaN(fs.getBinString()) || FPInstructionUtils.isSNaN(ft.getBinString())
          || (cpu.getFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION) && (FPInstructionUtils.isQNaN(fs.getBinString()) || FPInstructionUtils.isQNaN(ft.getBinString())))) {
        //before raising the trap or return the special value we modify the cause bit
        cpu.setFCSRCause("V", 1);
        throw new FPInvalidOperationException();
      }
    } else {
      BigDecimal fsbd = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(fs.getBinString(), false)));
      BigDecimal ftbd = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(ft.getBinString(), false)));

      less = fsbd.doubleValue() < ftbd.doubleValue();
      equal = (fs.getBinString().compareTo(ft.getBinString()) == 0);
      unordered = false;
    }

    //now we make the and operation between the truth mask and the comparison of the registers
    condition = (cond2 && less) || (cond1 && equal) || (cond0 && unordered);
    condition_int = condition ? 1 : 0;
    cpu.setFCSRConditionCode(params.get(CC_FIELD).intValue(), condition_int);
  }
  public void MEM() {}
  public void WB() {};

  public void pack() throws IrregularStringOfBitsException {
    //conversion of instruction parameters of "params" list to the "repr" form (32 binary value)
    repr.setBits(COP1_FIELD, COP1_FIELD_INIT);
    repr.setBits(FMT_FIELD, FMT_FIELD_INIT);
    repr.setBits(Converter.intToBin(FT_FIELD_LENGTH, params.get(FT_FIELD)), FT_FIELD_INIT);
    repr.setBits(Converter.intToBin(FS_FIELD_LENGTH, params.get(FS_FIELD)), FS_FIELD_INIT);
    repr.setBits(Converter.intToBin(CC_FIELD_LENGTH, params.get(CC_FIELD)), CC_FIELD_INIT);
    repr.setBits(CONST_FIELD, CONST_FIELD_INIT);
    repr.setBits(COND_VALUE, COND_VALUE_INIT);
  }

}
