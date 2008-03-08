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
 
 package edumips64.core;
 import java.util.*;
 import edumips64.core.is.*;
 import edumips64.utils.*;
import edumips64.core.MemoryElementNotFoundException;
 
 /** This class acts as a proxy to retrieve memory cells and instruction from
 * labels
 *  @author Simona Ullo, Andrea Spadaccini
 */
 public class SymbolTable
 {
    private static SymbolTable symTable = null;
	private Map<String, Integer> mem_labels;
    private Map<String, Integer> instr_labels;

	private Memory mem = null;
        
    private SymbolTable()
    {
		mem_labels = new HashMap<String, Integer>();
        instr_labels = new HashMap<String, Integer>();
		mem = Memory.getInstance();
    }

	public void setCellLabel(int address, String label) throws SameLabelsException, MemoryElementNotFoundException {
		if(label != null && !label.equals("")) {
			label = label.toLowerCase();
			if(mem_labels.containsKey(label))
				throw new SameLabelsException();
			mem_labels.put(label, address);
			MemoryElement temp = mem.getCell(address);
			// TODO: attualmente la cella  si prende l'ultima etichetta
			temp.setLabel(label);
			//edumips64.Main.logger.debug("Added label " + label + " to address " + address);
		}
	}

	public Instruction getInstruction(String label) {
		////edumips64.Main.logger.debug("Request for instruction labelled " + label);
		int address = instr_labels.get(label);
		////edumips64.Main.logger.debug("Label found at address " + address);
		return mem.getInstruction(address);
	}

	public MemoryElement getCell(String label) throws MemoryElementNotFoundException {
		////edumips64.Main.logger.debug("Request for memory element labelled " + label);
		if(label == null)
			throw new MemoryElementNotFoundException();
		label = label.toLowerCase();
		if(!mem_labels.containsKey(label))
			throw new MemoryElementNotFoundException();

		int address = mem_labels.get(label);
		////edumips64.Main.logger.debug("Label found at address " + address);
		return mem.getCell(address);
	}
    
    /** Singleton pattern: This method returns the unique instance of SymbolTable.
	 *  @return the unique instance of SymbolTable
	 */
    public static SymbolTable getInstance()
    {
        if(symTable==null)
            symTable = new SymbolTable();
        return symTable;
    }

	/** Adds to the Symbol Table, at the specified address, the given 
	 * instruction with the given label.
	 */
	public void setInstructionLabel(int address, String label) throws SameLabelsException {
		if(label != null && !label.equals("")) {
			if(instr_labels.containsKey(label))
				throw new SameLabelsException();
            System.out.println("Settin label " + label + " at address " + address);
			instr_labels.put(label, address);
			//Instruction temp = mem.getInstruction(address);
			// TODO: attualmente l'istruzione si prende l'ultima etichetta
			//temp.setLabel(label);
		}
	}

    public Integer getInstructionAddress(String label) throws MemoryElementNotFoundException
    {
        edumips64.Main.logger.debug("Requested address for " + label + ": " + instr_labels.get(label));
        Integer addr = instr_labels.get(label);
        if(addr == null)
            throw new MemoryElementNotFoundException();
        return addr;
    }


    /** This method resets the symbol table */
    public void reset()
    {
	    instr_labels.clear();
		mem_labels.clear();
		// TODO: eliminare individualmente le label dalle celle e dalle
		// istruzioni?
    }

	public String toString() {
		String output = new String();
		/*int i = 0;
		try {
			for(Instruction instr : instructions)
				output += Converter.binToHex(Converter.positiveIntToBin(32, i++ * 4)) + ": " + instr.getFullName() + "\n";
		}
		catch(IrregularStringOfBitsException e) {
			e.printStackTrace();
		}*/
		return output;
	}
 }
