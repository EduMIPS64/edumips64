/*
 * CVT_W_D.java
 *
 * 29th july 2007
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
 *  Format: CVT.W.D fd, fs
 * Description: To convert an FP value to 32-bit fixed point
 *   Operation: fd = convert_and_round(fs.readdouble,FCSR[CURRENT_ROUND_MODE])
 *</pre>
 */
class CVT_W_D extends FPConversionFCSRInstructions {
  static String OPCODE_VALUE = "100100";
  static String FMT_FIELD = "10001"; //DOUBLE IS 17
  static String NAME = "CVT.W.D";

  CVT_W_D() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    super.FMT_FIELD = FMT_FIELD;
    super.name = NAME;
  }

  public void EX() throws IrregularStringOfBitsException, FPInvalidOperationException, IrregularWriteOperationException {
    //getting values from temporary registers
    String fs = TRfp[FS_FIELD].getBinString();
    BigInteger bi = FPInstructionUtils.doubleToBigInteger(fs, cpu.getFCSRRoundingMode());
    BigInteger biggest = new BigInteger("2147483647");  //2^31-1
    BigInteger smallest = new BigInteger("-2147483648");  //-2^31

    //if the value is larger than an int an exception may occur
    if (bi == null || bi.compareTo(biggest) == 1 || bi.compareTo(smallest) == -1) {
      //before raising the trap or return the special value we modify the cause bit
      cpu.setFCSRCause("V", 1);

      if (cpu.getFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION)) {
        throw new FPInvalidOperationException();
      } else {
        cpu.setFCSRFlags("V", 1);
        //if an exception occured without a trap the biggest value is returned
        bi = new BigInteger("2147483648");  //2^31-1
      }
    }

    //writing the int value into a temporary integer register in order to obtain the binary value
    Register tmp = new Register("tmp-CVT.W.D");
    tmp.writeWord(bi.intValue());
    TRfp[FD_FIELD].setBits(tmp.getBinString(), 0);

    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }
}
