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

import org.edumips64.utils.*;
import org.edumips64.utils.io.Writer;
import org.edumips64.utils.io.WriteException;
import java.util.*;
import java.io.IOException;

public class Dinero {

  /** Instance of the Dinero */
  private static Dinero dinero;

  int offset = 0;
  private LinkedList <String> dineroData;

  /**Singlethon constructor */
  private Dinero() {
    dineroData = new LinkedList <String>();
  }
  public static Dinero getInstance() {
    if (dinero == null) {
      dinero = new Dinero();
    }

    return dinero;
  }
  /**
   */
  public void reset() {
    offset = 0;
    dineroData = new LinkedList <String>();
  }
  /** Add a read Instruction
   * @param address address of the read Instruction
   */
  public void IF(String address) {
    dineroData.add("i " + address + " 4");
  }
  /**
   */
  public void Load(String address, int nByte) {
    if (offset == 0) {
      findOffset();
    }

    try {
      long addr = Long.parseLong(Converter.hexToLong("0x" + address));
      addr += offset;
      dineroData.add("r " + Converter.binToHex(Converter.intToBin(64, addr)) + " " + nByte);
    } catch (IrregularStringOfHexException ex) {
      ex.printStackTrace();
    } catch (IrregularStringOfBitsException ex) {
      ex.printStackTrace();
    }
  }
  /**
   */
  public void Store(String address, int nByte) {
    if (offset == 0) {
      findOffset();
    }

    try {
      long addr = Long.parseLong(Converter.hexToLong("0x" + address));
      addr += offset;
      dineroData.add("w " + Converter.binToHex(Converter.intToBin(64, addr)) + " " + nByte);
    } catch (IrregularStringOfHexException ex) {
      ex.printStackTrace();
    } catch (IrregularStringOfBitsException ex) {
      ex.printStackTrace();
    }

  }
  /** Calculate the offset */
  public void findOffset() {
    CPU cpu = CPU.getInstance();
    int i;

    for (i = 0; i < CPU.CODELIMIT; i++) {
      try {
        if (cpu.getMemory().getInstruction(i * 4).getName().equals(" ")) {
          break;
        }
      } catch (SymbolTableOverflowException ex) {
        // This should never happen, since the bounds checked in getInstruction are upper-limited by CPU.CODELIMIT.
        ex.printStackTrace();
      }
    }

    offset = i * 4;
    offset += offset % 8;
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
