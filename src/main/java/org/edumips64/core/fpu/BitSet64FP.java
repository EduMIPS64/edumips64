/* BitSet64FP.java
 *
 * This class models a 64-bit array, useful for floating point registers and memory representation.
 * (c) 2006 Massimo Trubia
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
package org.edumips64.core.fpu;

import org.edumips64.core.FCSRRegister;
import org.edumips64.core.FixedBitSet;
import org.edumips64.core.IrregularStringOfBitsException;
import org.edumips64.core.IrregularWriteOperationException;

/** This class models a 64-bit array, useful for floating point registers
 * @author Massimo Trubia
 * */

public class BitSet64FP extends FixedBitSet {
  private FPInstructionUtils fpInstructionUtils;

  /** Creates a default new instance of BitSet64FP. */
  public BitSet64FP() {
    super(64);
    fpInstructionUtils = new FPInstructionUtils(new FCSRRegister());
  }

  /** Writes a floating point double precision number into this FixedBitSet: the value to be written must be in the range
   * [A=-1.797693134862315708145274237317E308,B=-4.9406564584124654417656879286822E-324] U [C=4.9406564584124654417656879286822E-324, D=1.797693134862315708145274237317E308].
   * For values that belong to ]-Infinity,A[ U ]D,+ Infinity[  an overflow exception will be thrown, on the contrary
   * values that belong to ]B,C[ an underflow exception will be thrown.
   * @param value double number to be written: must be on the format  "2.345" or "2345E-3"
   * @throws FPUnderflowException,FPOverflowException, IrregularWriteOperationException,FPInvalidOperationException
   */
  public void writeDouble(double value) throws FPUnderflowException, FPOverflowException, FPInvalidOperationException, IrregularWriteOperationException, IrregularStringOfBitsException {
    this.reset(false);
    String bits = fpInstructionUtils.doubleToBin(value + "");

    try {
      this.setBits(bits, 0);
    } catch (IrregularStringOfBitsException e) {
      e.printStackTrace();
      throw new IrregularWriteOperationException();
    }
  }

  /**Returns a string with a double value or the name of a special value
    * it is recommended the use of this method only for the visualisation of the double value because it may return an alphanumeric value
    * @return the double value or the special values "Quiet NaN","Signaling NaN", "Positive infinity", "Negative infinity","Positive zero","Negative zero"
    */
  public String readDouble() {
    return FPInstructionUtils.binToDouble(this.getBinString());
  }


}



