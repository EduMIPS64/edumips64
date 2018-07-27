/* Memory.java
 *
 * This class models the main memory of a computer, with 64-bit elements (that is 8 byte).
 * (c) 2006 Salvatore Scellato, Andrea Spadaccini
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

package org.edumips64.core;

import org.edumips64.core.is.InstructionInterface;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

/**  This class models the main memory of a computer, with 64-bit elements (that is 8 byte).
 * The Memory is composed of MemoryElement and its size is not limited.
 */
public class Memory {
  /** The code and data sections limits. Note that these limits are expressed in number of memory cells,
   *  not number of bytes.*/
  public static final int CODELIMIT = 16384; // 16 bit bus (2^16 / 4)
  public static final int DATALIMIT = 8192;  // 16 bit bus (2^16 / 8)

  /** Instruction offset limits in bytes */
  public static final int MAX_OFFSET_BYTES = 32768;  // 2^15
  public static final int MIN_OFFSET_BYTES = -32767; // -2^15 - 1

  // Data structures for the data and code memory. In both maps, the key is represented by the index of the element.
  // The index is derived by taking the address of the given element and dividing it by its width (8 for the memory,
  // 4 for the code).
  // SortedMaps are used to have the map entries sorted by key (useful for string representation).
  private SortedMap<Integer, MemoryElement> cells;
  private SortedMap<Integer, InstructionInterface> instructions;

  private static final Logger logger = Logger.getLogger(Memory.class.getName());

  // Keep track of non-BUBBLE instructions for code size purposes.
  private int instructionCount = 0;

  public Memory() {
    logger.info("Building Memory: " + this.hashCode());
    cells = new TreeMap<>();
    instructions = new TreeMap<>();
    logger.info("Memory built: " + this.hashCode());
  }

  /** Gets the number of instructions of the Symbol Table.
   *  @return an integer
   */
  public int getInstructionsNumber() {
    return instructionCount;
  }

  /** Gets the index of the given instruction
   * @return the position of the instruction in the list, or -1 if the instruction doesn't exist.
   */
  public int getInstructionIndex(InstructionInterface to_find) {
    int pos = 0;
    for(InstructionInterface i : instructions.values()) {
      if (i.equals(to_find)) {
        return pos;
      }
      pos++;
    }
    return -1;
  }

  /** Returns the MemoryElement at given address.
   * Please note that an index is not an address, for addresses must be aligned to 8 byte and indexes do not.
   * @param address address of the requested element
   * @return MemoryElement with address equals to index*8
   * @throws MemoryElementNotFoundException if given index is too large for this memory.
   */
  public MemoryElement getCellByAddress(long address) throws MemoryElementNotFoundException {
    int index = (int)(address / 8);
    return getCellByIndex(index);
  }

  /** Returns the MemoryElement with the given index. If there is no MemoryElement at the given index, create one,
   * add it to the map and return it.
   * @param index index of the requested element
   * @return MemoryElement
   * @throws MemoryElementNotFoundException if the given index is out of
   * bounds
   */
  public MemoryElement getCellByIndex(int index) throws MemoryElementNotFoundException {
    if (index >= DATALIMIT || index < 0) {
      throw new MemoryElementNotFoundException();
    }

    if (!cells.containsKey(index)) {
      cells.put(index, new MemoryElement(index * 8));
    }

    return cells.get(index);
  }

  /** This method resets the memory*/
  public void reset() {
    cells.clear();
    instructions.clear();
    instructionCount = 0;
  }

  public String toString() {
    String tmp = "Data:\n";

    for(MemoryElement m : cells.values()) {
      tmp += m.toString() + "\n";
    }

    tmp += "\nCode:\n";
    for (InstructionInterface i : instructions.values()) {
      tmp += i.toString() + "\n";
    }

    return tmp;
  }

  public void addInstruction(InstructionInterface i, int address) throws SymbolTableOverflowException {
    // TODO(lupino3): remove the limit.
    if (address > CODELIMIT) {
      logger.warning("Address exceeding the CPU code limit: " + address + " > " + CODELIMIT);
      throw new SymbolTableOverflowException();
    }

    int listIndex = address / 4;
    if (!i.isBubble() && !instructions.containsKey(listIndex)) {
      instructionCount++;
    }
    instructions.put(listIndex, i);
  }

  // Returns null if there is no instruction at the given address.
  public InstructionInterface getInstruction(int address) {
    int index = address / 4;
    if (!instructions.containsKey(index)) {
      return null;
    }
    return instructions.get(address / 4);
  }

  /** This method returns the instruction at the specified position.
  *   @return an Instruction object
  *   @param address a BitSet64 object holding the address of the Instruction
    */
  InstructionInterface getInstruction(BitSet64 address) throws IrregularStringOfBitsException {
      return instructions.get((int)(Converter.binToLong(address.getBinString(), false) / 4));
  }
}
