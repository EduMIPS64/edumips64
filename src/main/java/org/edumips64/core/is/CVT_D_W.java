/*
 * CVT_D_W.java
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

import org.edumips64.core.FCSRRegister;
import org.edumips64.core.IrregularStringOfBitsException;
import org.edumips64.core.IrregularWriteOperationException;
import org.edumips64.core.fpu.FPInstructionUtils;
import org.edumips64.core.fpu.FPInvalidOperationException;
import org.edumips64.core.fpu.FPOverflowException;
import org.edumips64.core.fpu.FPUnderflowException;

import java.math.*;

/**
 *<pre>
 *  Format: CVT.D.W fd, fs
 * Description: To convert a word fixed point value to double FP
 *   Operation: fd = convert_and_round(fs.readword,FCSR[CURRENT_ROUND_MODE])
 *</pre>
 */
class CVT_D_W extends FPConversionFCSRInstructions {
  private final static String OPCODE_VALUE = "100101";
  private final static String FMT_FIELD = "10100"; //WORD IS 20
  private final static String NAME = "CVT.D.W";

  CVT_D_W() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    super.FMT_FIELD = FMT_FIELD;
    super.name = NAME;
  }

  public void EX() throws IrregularStringOfBitsException, FPInvalidOperationException, IrregularWriteOperationException, FPUnderflowException, FPOverflowException {
    //getting values from temporary registers
    BigDecimal bd;
    String fs = TRfp[FS_FIELD].getBinString();

    if ((bd = FPInstructionUtils.intToDouble(fs)) == null) {
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
