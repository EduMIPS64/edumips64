/*
 * LDC1.java
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
import org.edumips64.utils.*;


/**
 * <pre>
 *      Format: LDC1 ft, offset(base)
 * Description: To load a doubleword from memory to an FPR
 *   Operation: ft = memory[base+offset]
 *</pre>

 */
class LDC1 extends FPLoading {
  String OPCODE_VALUE = "110111";
  public LDC1() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    this.name = "LDC1";
  }
  public void MEM() throws IrregularStringOfBitsException, NotAlignException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
    super.MEM(); //unlock the fp register in order to avoid WAW hazards
    //restoring the address from the temporary register
    long address = TR[OFFSET_PLUS_BASE].getValue();
    //For the trace file
    Dinero din = Dinero.getInstance();
    din.Load(Converter.binToHex(Converter.positiveIntToBin(64, address)), 8);

    MemoryElement memEl = memory.getCellByAddress(address);
    //reading from the memory element and saving values on LMD register
    TR[LMD_REGISTER].setBits(memEl.getBinString(), 0);

    if (enableForwarding) {
      doWB();
    }
  }
}
