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
	private static CPU cpu;
	
	private Memory(){
		//edumips64.Main.logger.debug("Building Memory: " + this.hashCode());
		mem_comments = new HashMap<Integer,String>();
		cells = new ArrayList<MemoryElement>();
		instr_num = 0;
        instructions = new LinkedList<Instruction>();
		for(int i = 0; i < CPU.DATALIMIT; i++)
			cells.add(new MemoryElement(i*8));
		for(int i = 0; i < CPU.CODELIMIT; i++)
			instructions.add(Instruction.buildInstruction("BUBBLE"));
		//edumips64.Main.logger.debug("Memory built: " + this.hashCode());
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

	/** Writes to memory the given string, starting at the given address.
     * @param address memory address to write to
     * @param str the string to write
     * @param auto_terminate whether to add a '\0' character at the end of the string
	 * @return the number of bytes written to memory
	 * @throws IrregularWriteOperationException
	 * @throws MemoryElementNotFoundException
	 * @throws StringFormatException
	 */
    public int writeString(int address, String str, boolean auto_terminate) throws IrregularWriteOperationException, MemoryElementNotFoundException, StringFormatException {
        MemoryElement tmpMem = memory.getCell(address);
        int written = 0;
        int posInWord = 0;
        int escaped = 0;		// to avoid escape sequences to count as two bytes
        
        // If auto-terminate is set to true, we must add \0 to the end of the
        // string
        if(auto_terminate)
            str += "\\0";
        int len = str.length();
        boolean escape = false;
        boolean placeholder = false;
        

        
        for(int i = 0; i < len; i++) {
            if((i - escaped) % 8 == 0 && (i - escaped) != 0 && !escape) {
                address += 8;
                posInWord = 0;
                tmpMem = memory.getCell(address);
            }
            char c = str.charAt(i);
            int to_write = (int)c;
            System.out.println("Char: " + c + " (" + to_write + ") [" + Integer.toHexString(to_write) + "]");
            if(escape) {
                switch(c) {
                    case '0':
                        to_write = 0;
                        break;
                    case 'n':
                        to_write = 10;
                        break;
                    case 't':
                        to_write = 9;
                        break;
                    case '\\':
                        to_write = 92;
                        break;
                    case '"':
                        to_write = 34;
                        break;
                    default:
                        throw new StringFormatException();
                }
                System.out.println("(escaped to [" + Integer.toHexString(to_write) + "])");
                escape = false;
                c = 0;	// to avoid re-entering the escape if branch.
            }
            if(placeholder) {
                if(c != '%' && c != 's' && c != 'd' && c != 'i') {
                    System.out.println("Invalid placeholder: %" + c);
                    // Invalid placeholder
                    throw new StringFormatException();
                }
                placeholder = false;
            }
            if(c == '%' && !placeholder) {
                System.out.println("Expecting on next step a valid placeholder...");
                placeholder = true;
            }
            if(c == '\\') {
                escape = true;
                escaped++;
                continue;
            }
            tmpMem.writeByte(to_write, posInWord++);
            written++;
        }


        return written;
    }

	/** Writes to memory the given integer value at the given address.
	 * Data is written to memory according to the type passed, as a string, in
     * the type parameter.
     * @param address memory address to write to
     * @param value the value to write
     * @param type the data type of value
	 * @return the number of bytes written to memory
	 * @throws IrregularWriteOperationException
	 * @throws MemoryElementNotFoundException
	 * @throws NotAlingException
	 */
    public int writeInteger(int address, long value, String type) throws IrregularWriteOperationException, NotAlingException, MemoryElementNotFoundException {
        int offset = address % 8;
        MemoryElement tmpMem = getCell(address - offset);

        System.out.println("Address: " + address + ", offset: " + offset);

        if(type.equalsIgnoreCase("BYTE")) {
            tmpMem.writeByte((int)value, offset);
            return 1;
        }

        else if(type.equalsIgnoreCase("WORD16")) {
            tmpMem.writeHalf((int)value, offset);
            return 2;
        }

        else if(type.equalsIgnoreCase("WORD32")) {
            tmpMem.writeWord((int)value, offset);
            return 4;
        }

        else if(type.equalsIgnoreCase("WORD64") ||
            type.equalsIgnoreCase("WORD")) {
            tmpMem.writeDoubleWord((int)value);
            return 8;
        }

        // TODO: should we throw an exception if the type is not known?
        return 0;
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
    
    /** This method returns the list of instructions in memory in order to be showed in the GUICode
     */
    public List<Instruction> getInstructions()
    {
	    return instructions;
    }
}
