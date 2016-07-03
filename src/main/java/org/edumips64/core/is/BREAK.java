/*
 * BREAK.java
 *
 * 15th may 2006
 * Instruction of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Andrea Spadaccini
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

/** *Syntax:    BREAK
 * Description: To cause a Breakpoint exception.
 * As soon as the BREAK instruction enters in the ID stage, the CPU stops the
 * execution, allowing the user to take control of the program.
 * Exceptions:   Breakpoint
 *</pre>
 * @author Andrea Spadaccini
 */
public class BREAK extends Instruction {
  final String OPCODE_VALUE = "000000"; // SPECIAL
  BREAK() {
    name = "BREAK";
  }
  public void IF() throws BreakException {
    try {
      dinero.IF(Converter.binToHex(Converter.intToBin(64, cpu.getLastPC().getValue())));
    } catch (IrregularStringOfBitsException e) {
      e.printStackTrace();
    }

    throw new BreakException();
  }
  public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException {
  }

  public void EX() throws HaltException, IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
  }

  public void MEM() throws HaltException, IrregularStringOfBitsException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
  }

  public void WB() throws HaltException, IrregularStringOfBitsException {
  }

  public void pack() throws IrregularStringOfBitsException {
    repr.setBits(OPCODE_VALUE, 0);
    /* The MIPS64 ISA specification requires the last 6 bits to be 01101 in
     * the BREAK instruction. */
    repr.setBits("001101", 25);
  }
}
