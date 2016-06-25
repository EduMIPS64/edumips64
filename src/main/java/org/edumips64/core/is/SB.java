/*
 * SB.java
 *
 * 8th may 2006
 * Instruction SB of the MIPS64 Instruction Set
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
import org.edumips64.utils.*;

/** <pre>
 *       Syntax: SB rt, offset(base)
 *  Description: Stores the least-significant 8 bit of rt in memory
 *       i.e: memory[base+offset] = rt
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */
class SB extends Storing {
  final String OPCODE_VALUE = "101000";

  public SB(Memory memory) {
    super(memory);
    super.OPCODE_VALUE = OPCODE_VALUE;
    this.name = "SB";
    this.memoryOpSize = 1;
  }

  public void doMEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
    memEl.writeByte(TR[RT_FIELD].readByte(0), (int)(address % 8));
  }
}
