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

package edumips64.core;

import edumips64.utils.*;
import java.util.*;
import edumips64.core.is.*;

/**  This class models the main memory of a computer, with 64-bit elements (that is 8 byte).
 * The Memory is composed of MemoryElement and its size is not limited.
 */
public class Memory{
	// cancellabile?
	private List<MemoryElement> cells;
    private List<Instruction> instructions;
	
	private Map<Integer, String> mem_comments;

	private int instr_num;
	private static Memory memory = null;
	
	private Memory(){
		edumips64.Main.logger.debug("Building Memory: " + this.hashCode());
		mem_comments = new HashMap<Integer,String>();
		cells = new ArrayList<MemoryElement>();
		instr_num = 0;
        instructions = new LinkedList<Instruction>();
		for(int i = 0; i < CPU.DATALIMIT; i++)
			cells.add(new MemoryElement(i*8));
		for(int i = 0; i < CPU.CODELIMIT; i++)
			instructions.add(Instruction.buildInstruction("BUBBLE"));
		edumips64.Main.logger.debug("Memory built: " + this.hashCode());
	}

	/** Returns the maximum number of MemoryElement stored in Memory
	 * @return size of the memory
	 */
	public int getMemorySize(){
		return CPU.DATALIMIT;
	}

	/** Gets the instr_num of the Symbol Table.
	 *  @return an integer
	 */
	public int getInstructionsNumber() {
		return instr_num;
	}

	/** Singleton pattern: since the unique constructor of this class is private, this static method
	 * returns the unique allowed instance of Memory, thus subsequent calls of this method will return exactly
	 * the same object.
	 * @return unique instance of Memory
	 */
	public static Memory getInstance(){
		if( memory == null)
			memory = new Memory();
		return memory;
	}
	
    /** Gets the index of the given instruction
     * @return the position of the instruction in the list, or -1 if the instruction doesn't exist.
     */
    public int getInstructionIndex(Instruction i) {
	    return instructions.indexOf(i);
    }

	/** Returns the MemoryElement at given address.
	 * Please note that an index is not an address, for addresses must be aligned to 8 byte and indexes do not.
	 * @param address address of the requested element
	 * @return MemoryElement with address equals to index*8
	 * @throws MemoryElementNotFoundException if given index is too large for this memory.
	 */
	public MemoryElement getCell(int address) throws MemoryElementNotFoundException{
		int index = address / 8;
		
		if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();

		return cells.get(index);
	}

	/** This method resets the memory*/
	public void reset()
	{
		for(int i = 0; i < CPU.DATALIMIT; i++)
		{
			cells.get(i).reset(false);
			cells.get(i).setComment("");
			cells.get(i).setCode("");
			cells.get(i).setLabel("");
		}
		instructions.clear();
		for(int i = 0; i < CPU.CODELIMIT; i++)
			instructions.add(Instruction.buildInstruction("BUBBLE"));

		mem_comments.clear();
		instr_num = 0;
		// TODO sistemare il reset
	}
       
    public String toString()
	{
		String tmp="";
		for(int i=0; i<CPU.DATALIMIT;i++)
		{
			tmp += cells.get(i).toString()+"\n";
		}
		return tmp;
	} 

	public void addInstruction(Instruction i, int address) throws SymbolTableOverflowException {
		instr_num++;
		if(address > CPU.CODELIMIT)
			throw new SymbolTableOverflowException();

		int listIndex = address / 4;
		instructions.set(listIndex, i);
	}

	public Instruction getInstruction(int address) {
		return instructions.get(address / 4);
	}

	/** This method returns the instruction at the specified position.
	*   @return an Instruction object
	*   @param address a BitSet64 object holding the address of the Instruction
    */
    public Instruction getInstruction(BitSet64 address) throws HaltException, IrregularStringOfBitsException {
		try {
        	return instructions.get((int)(Converter.binToLong(address.getBinString(), false)/4));
		}
		catch(IndexOutOfBoundsException e) {
			throw new HaltException();
		}
    }
}
