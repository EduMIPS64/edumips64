/*
 * CVT_D_L.java
 *
 * 25th july 2007
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

/**
 *<pre>
 *  Format: CVT.D.L fd, fs
 * Description: To convert an FP or fixed point value to double FP
 *   Operation: fd = convert_and_round(fs,FCSR[CURRENT_ROUND_MODE])
 *</pre>
 */
class CVT_D_L extends FPConversionFCSRInstructions {
  private static String OPCODE_VALUE = "100101";
  private static String FMT_FIELD = "10101"; //LONG IS 21
  private static String NAME = "CVT.D.L";

  CVT_D_L() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    super.FMT_FIELD = FMT_FIELD;
    super.name = NAME;
  }

  public void EX() throws IrregularStringOfBitsException, FPInvalidOperationException, IrregularWriteOperationException, FPUnderflowException, FPOverflowException {
    //getting values from temporary registers
    BigDecimal bd;
    String fs = TRfp[FS_FIELD].getBinString();

    if ((bd = FPInstructionUtils.longToDouble(fs)) == null) {
      //before raising the trap or return the special value we modify the cause bit
      cpu.setFCSRCause("V", 1);

      if (cpu.getFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION)) {
        throw new FPInvalidOperationException();
      } else {
        cpu.setFCSRFlags("V", 1);
        TRfp[FD_FIELD].setBits("0000000000000000000000000000000000000000000000000000000000000000", 0);
      }
    } else {
      TRfp[FD_FIELD].writeDouble(bd.doubleValue());
    }

    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }
}
