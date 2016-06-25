/*
 * SWC.java
 *
 * 25th july 2007
 * Instruction SW of the MIPS64 Instruction Set
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


/**
 *       Syntax: SWC1 ft, offset(base)
 *  Description: writewordmemory[base+offset] = ft
 *               To store a word from an FPR to memory
 */
class SWC1 extends FPStoring {
  final String OPCODE_VALUE = "111001";

  public SWC1(Memory memory) {
    super(memory);
    super.OPCODE_VALUE = OPCODE_VALUE;
    this.name = "SWC1";
  }


  public void MEM() throws IrregularStringOfBitsException, NotAlignException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {

    //restoring the address from the temporary register
    long address = TR[OFFSET_PLUS_BASE].getValue();
    //For the trace file
    Dinero din = Dinero.getInstance();
    din.Store(Converter.binToHex(Converter.positiveIntToBin(64, address)), 4);
    MemoryElement memEl = memory.getCellByAddress(address);
    //writing on the memory element the RT register
    memEl.writeWord(TR[RT_FIELD].readWord(0), (int)(address % 8));

  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException {

  }

  public void WB() throws IrregularStringOfBitsException {
  }





}

