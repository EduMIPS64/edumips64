/*
 * SDC1.java
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

import org.edumips64.core.Converter;
import org.edumips64.core.IrregularStringOfBitsException;
import org.edumips64.core.Memory;
import org.edumips64.core.MemoryElement;
import org.edumips64.core.MemoryElementNotFoundException;

/** <pre>
 *       Syntax: SDC1 ft, offset(base)
 *  Description: memory[base+offset] = ft
 *               The double value in ft is stored in memory.
 * </pre>
 */
public class SDC1 extends FPStoring {

  protected String OPCODE_VALUE = "111101";
  SDC1(Memory memory) {
    super(memory);
    super.OPCODE_VALUE = OPCODE_VALUE;
    this.name = "SDC1";
  }

  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException, AddressErrorException {
    try {
      //restoring the address from the temporary register
      long address = TR[OFFSET_PLUS_BASE].getValue();
      //For the trace file
      cachesim.Store(Converter.binToHex(Converter.positiveIntToBin(64, address)), 8);
      MemoryElement memEl = memory.getCellByAddress(address);
      //writing on the memory element the RT register
      memEl.setBits(TR[RT_FIELD].getBinString(), 0);

      if (cpu.isEnableForwarding()) {
        WB();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

