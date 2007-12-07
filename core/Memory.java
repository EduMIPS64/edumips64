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
import java.io.*;

/**  This class models the main memory of a computer, with 64-bit elements (that is 8 byte).
 * The Memory is composed of MemoryElement and its size is not limited.
 */
public class Memory implements MemoryAccessor {
        int numStore=0;
        int numLoad=0;
        private LinkedList <String> MemoryData;
        int offset = 0;
	private List<MemoryElement> cells;
        private List<Instruction> instructions;
	private Map<Integer, String> mem_comments;

	private int instr_num;
	private static Memory memory = null;
	private static CPU cpu;
	
	private Memory(){
		edumips64.Main.logger.debug("Building Memory: " + this.hashCode());
		MemoryData = new LinkedList <String>();
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

        public void writeB(int address, int data) throws MemoryElementNotFoundException,IrregularWriteOperationException{
		int index = address / 8;
                if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
               cells.get(index).writeByte(data,(int)(address%8));
               numStore++;
                if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("w "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                
            }
    
    public void writeD(int address, String data) throws MemoryElementNotFoundException,IrregularStringOfBitsException
                {
		int index = address / 8;
		if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
               cells.get(index).setBits(data,0);
               numStore++;
                 if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("w "+Converter.binToHex(Converter.intToBin(64,addr))+" "+8);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                
            }
   
    public void writeH(int address,int data) throws MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException{
		int index = address / 8;
		if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
               cells.get(index).writeHalf(data,(int)(address%8));
               numStore++;
                 if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("w "+Converter.binToHex(Converter.intToBin(64,addr))+" "+2);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                
            }
  
    public void writeW(int address,int data) throws MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException
                {
		int index = address / 8;
		if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
               cells.get(index).writeWord(data,(int)(address%8));
               numStore++;
                 if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("w "+Converter.binToHex(Converter.intToBin(64,addr))+" "+4);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                
            }
                
    public int readB(int address) throws MemoryElementNotFoundException
            {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            int value=cells.get(index).readByte((int)(address%8)); 
            numLoad++;
            if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                return value;
            }
            
   //(s)Utilizzata in Syscall analoga a meno della scrittura nel triceFile di memory
            
    public int readBNT(int address) throws MemoryElementNotFoundException
            {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            return cells.get(index).readByte((int)(address%8));
            }
            
    public int readBU(int address) throws MemoryElementNotFoundException
            {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            int value=cells.get(index).readByteUnsigned((int)(address%8));
            numLoad++;
            if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                return value;
            }
            
            
    public int readH(int address) throws MemoryElementNotFoundException,IrregularStringOfBitsException,NotAlingException
            {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            int value=cells.get(index).readHalf((int)(address%8));
            numLoad++;
            if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+2);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                return value;
            }
            
           
    public int readHU(int address) throws MemoryElementNotFoundException,NotAlingException
            {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            int value=cells.get(index).readHalfUnsigned((int)(address%8));
            numLoad++;
            if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+2);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                return value;
            }
            
            
    public int readW(int address) throws MemoryElementNotFoundException,NotAlingException
            {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            int value=cells.get(index).readWord((int)(address%8));
            numLoad++;
            if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+4);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                return value;
            }
            
            
    public long readWU(int address) throws MemoryElementNotFoundException,NotAlingException
            {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            long value=cells.get(index).readWordUnsigned((int)(address%8));
            numLoad++;
            if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+4);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                return value;
            }
            
           
    
            
    public String readD(int address) throws MemoryElementNotFoundException
            {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            String value=cells.get(index).getBinString(); 
            numLoad++;
            if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+8);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
                return value;
            }
            
            
        public long readVal(int address) throws MemoryElementNotFoundException{
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            return cells.get(index).getValue();
        }
            
            
        
          public void SyscallL(int address){
                if(offset == 0)
		{	
			findOffset();
		}
		
		try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    MemoryData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+8);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		catch(IrregularStringOfBitsException ex)
		{
			ex.printStackTrace();
		}
            }
            
        
    public void findOffset ()
	{
		CPU cpu = CPU.getInstance();
		int i;
		for (i=0; i<CPU.CODELIMIT; i++)
		{	
			if(cpu.getMemory().getInstruction(i * 4).getName().equals(" "))
				break;
		}

		offset = i*4;
		offset+=offset%8;		
	}
       
        /** Write a file comatible with DineroIV cache simulator 
        * @param filename A String with the system-dependent file name
        */
        public void WriteXdinFile (String filename) throws java.io.IOException
	{
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        //Scrivo il contenuto della lista dineroData nel file filename
        writeTraceData(out);
        out.close();
	}
        
	
        /** Writes the trace data to a Writer
	 *  @param buff the Writer to output the data to
	 */
	public void writeTraceData(Writer buff) throws java.io.IOException {
		for (int i=0; i < MemoryData.size();i++) {
			String tmp = MemoryData.get(i) + "\n";
			//scrivo la stringa in buff e dunque nel file filename.
                        buff.write(tmp,0,tmp.length());
		}
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
