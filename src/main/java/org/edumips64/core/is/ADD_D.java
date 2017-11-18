/*
 * ADD_D.java
 *
 * 27th may 2007
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

/**
 * <pre>
 *      Format: ADD.D fd, fs, ft
 * Description: To add floating point values
 *   Operation: fd = fs + ft
 *
 * Invalid Operation, Overflow, Underflow
 *</pre>
 */
class ADD_D extends FPArithmeticInstructions {
  String OPCODE_VALUE = "000000";
  String FMT_FIELD = "10001"; //DOUBLE IS 17
  String NAME = "ADD.D";

  ADD_D(FCSRRegister fcsr) {
    super(fcsr);
    super.OPCODE_VALUE = OPCODE_VALUE;
    super.FMT_FIELD = FMT_FIELD;
    super.name = NAME;
  }

  @Override
  protected String doFPArith(String operand1, String operand2) throws FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, IrregularStringOfBitsException {
    return fpInstructionUtils.doubleSum(operand1, operand2);
  }
}
