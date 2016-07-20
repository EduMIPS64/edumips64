/* SymbolTable.java
 *
 * This class acts as a proxy to retrieve memory cells and instruction from
 * labels
 * (c) 2006 Simona Ullo, Andrea Spadaccini
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.edumips64.core.is.Instruction;

/** This class acts as a proxy to retrieve memory cells and instruction from
* labels
*  @author Simona Ullo, Andrea Spadaccini
*/
public class SymbolTable {
  private static final Logger logger = Logger.getLogger(SymbolTable.class.getName());
  private Map<String, Integer> mem_labels;
  private Map<String, Integer> instr_labels;

  private Memory mem;

  public SymbolTable(Memory memory) {
    mem_labels = new HashMap<>();
    instr_labels = new HashMap<>();
    this.mem = memory;
  }

  public void setCellLabel(int address, String label) throws SameLabelsException, MemoryElementNotFoundException {
    if (label != null && !label.equals("")) {
      label = label.toLowerCase();

      if (mem_labels.containsKey(label)) {
        throw new SameLabelsException();
      }

      mem_labels.put(label, address);
      MemoryElement temp = mem.getCellByAddress(address);
      // TODO: attualmente la cella  si prende l'ultima etichetta
      temp.setLabel(label);
      logger.info("Added memory label " + label + " to address " + address);
    }
  }

  public MemoryElement getCell(String label) throws MemoryElementNotFoundException {
    logger.info("Request for memory element labelled " + label);

    if (label == null) {
      throw new MemoryElementNotFoundException();
    }

    label = label.toLowerCase();

    if (!mem_labels.containsKey(label)) {
      throw new MemoryElementNotFoundException();
    }

    int address = mem_labels.get(label);
    logger.info("Label found at address " + address);
    return mem.getCellByAddress(address);
  }

  /** Adds to the Symbol Table, at the specified address, the given
   * instruction with the given label.
   */
  public void setInstructionLabel(int address, String label) throws SameLabelsException {
    if (label != null && !label.equals("")) {
      if (instr_labels.containsKey(label)) {
        throw new SameLabelsException();
      }

      instr_labels.put(label, address);
      Instruction temp = mem.getInstruction(address);
      if (temp == null) {
        // TODO: throw exception?
        logger.severe("No instruction at address " + address);
      }
      // TODO: attualmente l'istruzione si prende l'ultima etichetta
      temp.setLabel(label);
      logger.info("Added instruction label " + label + " to address " + address);
    }
  }

  public Integer getInstructionAddress(String label) {
    try {
      return instr_labels.get(label);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }


  /** This method resets the symbol table */
  public void reset() {
    instr_labels.clear();
    mem_labels.clear();
    // TODO: eliminare individualmente le label dalle celle e dalle
    // istruzioni?
  }

  public String toString() {
    String output = new String();
    output += "\nInstructions:\n";
    for(Map.Entry<String, Integer> entry : instr_labels.entrySet()) {
      output += entry.getKey() + ": " + entry.getValue() + "\n";
    }
    output += "\nMemory:\n";
    for(Map.Entry<String, Integer> entry : mem_labels.entrySet()) {
      output += entry.getKey() + ": " + entry.getValue() + "\n";
    }
    return output;
  }
}
