/*
 * LBU.java
 *
 * 26th may 2006
 * Instruction LBU of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo, Russo Daniele
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


/** <pre>
 *         Syntax: LBU rt, offset(base)
 *    Description: To load a byte from memory as an unsigned value
 *                 rt = memory[base+offset]
  * </pre>
 * @author Trubia Massimo, Russo Daniele
 */
class LBU extends Loading {
  final String OPCODE_VALUE = "100100";

  LBU(Memory memory) {
    super(memory);
    super.OPCODE_VALUE = OPCODE_VALUE;
    this.name = "LBU";
    this.memoryOpSize = 1;
  }

  public void doMEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
    //reading first 8 low bits from the memory element and saving values on LMD register with zero padding
    TR[LMD_REGISTER].writeByteUnsigned(memEl.readByteUnsigned((int)(address % 8)));
  }
}
