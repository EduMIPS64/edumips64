/* Dinero.java
*
* This Class create a File compatible with dinaroIV, a cache Simulator
* (c) 2006 Mancausoft
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

/** This Class create a File compatible with dinaroIV, a cache Simulator
 * @author Andrea Milazzo
 */

package org.edumips64.core;

import org.edumips64.utils.io.Writer;
import org.edumips64.utils.io.WriteException;
import java.util.*;

public class Dinero {

  private LinkedList <String> dineroData = new LinkedList<>();

  // Offset of the data segment. This class writes a trace file that assumes
  // that the data segment starts immediately after the code segment ends.
  private int offset;

  /** Sets the data offset.
   * @param dataOffset offset of the data section. Should be after the code
   *                   section. Typically this is the number of instructions
   *                   times 4 (each instruction takes 32 bits).
   */
  public void setDataOffset(int dataOffset) {
    // Align the dataOffset to 64 bit if needed.
    offset = dataOffset + dataOffset % 8;
  }

  public void reset() {
    offset = 0;
    dineroData = new LinkedList <>();
  }

  /** Add a read Instruction
   * @param address address of the read Instruction
   */
  public void IF(String address) {
    dineroData.add("i " + address + " 4");
  }

  public void Load(String address, int nByte) {
    try {
      long addr = Long.parseLong(Converter.hexToLong("0x" + address));
      addr += offset;
      dineroData.add("r " + Converter.binToHex(Converter.intToBin(64, addr)) + " " + nByte);
    } catch (IrregularStringOfHexException | IrregularStringOfBitsException ex) {
      ex.printStackTrace();
    }
  }

  public void Store(String address, int nByte) {
    try {
      long addr = Long.parseLong(Converter.hexToLong("0x" + address));
      addr += offset;
      dineroData.add("w " + Converter.binToHex(Converter.intToBin(64, addr)) + " " + nByte);
    } catch (IrregularStringOfHexException | IrregularStringOfBitsException ex) {
      ex.printStackTrace();
    }

  }

  /** Writes the trace data to a Writer
   *  @param buff the Writer to output the data to
   */
  public void writeTraceData(Writer buff) throws java.io.IOException, WriteException {
    for (int i = 0; i < dineroData.size(); i++) {
      String tmp = dineroData.get(i) + "\n";
      buff.write(tmp);
    }
  }
}
