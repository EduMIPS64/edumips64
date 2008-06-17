/* Cache.java
 *
 * This class models the main memory of a computer, with 64-bit elements (that is 8 byte).
 * (c) 2008 Samuele Di Cataldo
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
import java.lang.*;


public class Cache {
        private static Cache cache = null; 
        private Memory mem;
        //Mi permettere di conoscere il numero di stal in corrispondenza di un miss.
        int stalls=0;
        //Mi permette di tenere traccia se un dato e in cache o meno in relazione al suo indirizzo.
        private Map<Integer, Boolean> CacheControl;
            
        int numStore=0;
        int numLoad=0;
        int stall = 0;
        private LinkedList <String> CacheData;
        int offset = 0;
	    private List<CacheElement> cells;
        private List<Instruction> instructions;
	    private Map<Integer, String> cache_comments;
            
        private int instr_num;
        private static CPU cpu;
        private static Memory memory = null;
        
        private Cache(){
            edumips64.Main.logger.debug("Building Cache: " + this.hashCode());
		    CacheData = new LinkedList <String>();
            cache_comments = new HashMap<Integer,String>();
		    cells = new ArrayList<CacheElement>();
		    instr_num = 0;
            instructions = new LinkedList<Instruction>();
            mem = Memory.getInstance();
            CacheControl = new HashMap<Integer, Boolean>();
		for(int i = 0; i < CPU.DATALIMIT; i++)
			cells.add(new CacheElement(i*8));
		for(int i = 0; i < CPU.CODELIMIT; i++)
			instructions.add(Instruction.buildInstruction("BUBBLE"));
		    edumips64.Main.logger.debug("Cache built: " + this.hashCode());
        for(int i = 0; i < CPU.DATALIMIT; i++)
                CacheControl.put(i,false);
            }

        public void findOffset(){
            CPU cpu = CPU.getInstance();
		int i;
		for (i=0; i<CPU.CODELIMIT; i++)
		{	
			if(cpu.getCache().getInstruction(i * 4).getName().equals(" "))
				break;
		}

		offset = i*4;
		offset+=offset%8;
            
            };

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
            
public Instruction getInstruction(BitSet64 address) throws HaltException, IrregularStringOfBitsException {
		try {
        	return instructions.get((int)(Converter.binToLong(address.getBinString(), false)/4));
		}
		catch(IndexOutOfBoundsException e) {
			throw new HaltException();
		}
    }
            
        public List<Instruction> getInstructions()
    {
	    return instructions;
    }
            
        public void resetMap()
            {
                for(int i = 0; i < CPU.DATALIMIT; i++){
                CacheControl.put(i,false);
                cells.get(i).reset(false);
			    cells.get(i).setComment("");
			    cells.get(i).setCode("");
			    cells.get(i).setLabel("");
                }
                instructions.clear();
		    for(int i = 0; i < CPU.CODELIMIT; i++)
			instructions.add(Instruction.buildInstruction("BUBBLE"));
		    cache_comments.clear();
		    instr_num = 0;
            }
      
    public int getInstructionsNumber() {
		return instr_num;
	} 

	public static Cache getInstance(){
		if( cache == null)
			cache = new Cache ();
		return cache;
	}
    
    public int getInstructionIndex(Instruction i) {
	    return instructions.indexOf(i);
    }
        
    public CacheElement getCell(int address) throws CacheElementNotFoundException{
		int index = address / 8;
		
		if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new CacheElementNotFoundException();

		return cells.get(index);
	}
    
        
public int CreadB(int address) throws MemoryElementNotFoundException, MemoryExceptionStall
{
            
    int index= address/8;
    if(index >= CPU.DATALIMIT || index < 0 || address < 0)
        throw new MemoryElementNotFoundException();
    boolean control=CacheControl.get(index);
    if(control==true)
    {
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
                    CacheData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
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
        else 
        {
            mem.readB(address);
            index = address/8;
            CacheControl.put(index,true);
            if(offset == 0)
		    {	
			findOffset();
		    } 
             try
                {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    CacheData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
                }
                    catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
                    catch(IrregularStringOfBitsException ex)
                    {
                    ex.printStackTrace();
                    }
          stalls = stalls++;
          throw new MemoryExceptionStall(stalls);
          
        }
    }
    

     public int CreadBU(int address) throws MemoryElementNotFoundException,MemoryExceptionStall
            {
            
    int index= address/8;
    if(index >= CPU.DATALIMIT || index < 0 || address < 0)
        throw new MemoryElementNotFoundException();
    boolean control=CacheControl.get(index);
    if(control==true)
    {
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
                    CacheData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
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
        else 
        {
            mem.readBU(address); 
            index = address/8;
            CacheControl.put(index,true);
            
            numStore++;
            if(offset == 0)
		    {	
			findOffset();
		    }
             stall = stall++;
            throw new MemoryExceptionStall(stall);
        }
    }
            
    public String CreadD(int address) throws MemoryElementNotFoundException,MemoryExceptionStall
            {
            
    int index= address/8;
    if(index >= CPU.DATALIMIT || index < 0 || address < 0)
        throw new MemoryElementNotFoundException();
    boolean control=CacheControl.get(index);
    if(control==true)
    {
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
                    CacheData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+8);
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
        else 
        {
            mem.readD(address); 
            index = address/8;
            CacheControl.put(index,true);
            
            numStore++;
            if(offset == 0)
		    {	
			findOffset();
		    }
             stall = stall++;
            throw new MemoryExceptionStall(stall);
        }
        
    }
        
            
    public int CreadH(int address) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularStringOfBitsException,NotAlingException
                        {
            
    int index= address/8;
    if(index >= CPU.DATALIMIT || index < 0 || address < 0)
        throw new MemoryElementNotFoundException();
    boolean control=CacheControl.get(index);
    if(control==true)
    {
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
                    CacheData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+2);
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
        else 
        {
            mem.readH(address); 
            index = address/8;
            CacheControl.put(index,true);
            
            numStore++;
            if(offset == 0)
		    {	
			findOffset();
		    }
             stall = stall++;
            throw new MemoryExceptionStall(stall);
        }
        
    }
            
            
     public int CreadHU(int address) throws MemoryExceptionStall,MemoryElementNotFoundException,NotAlingException
{
            
    int index= address/8;
    if(index >= CPU.DATALIMIT || index < 0 || address < 0)
        throw new MemoryElementNotFoundException();
    boolean control=CacheControl.get(index);
    if(control==true)
    {
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
                    CacheData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+2);
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
        else 
        {
            mem.readHU(address); 
            index = address/8;
            CacheControl.put(index,true);
            
            numStore++;
            if(offset == 0)
		    {	
			findOffset();
		    }
             stall = stall++;
            throw new MemoryExceptionStall(stall);
        }
        
    }
            
    
    public int CreadW(int address) throws MemoryExceptionStall,MemoryElementNotFoundException,NotAlingException
            {
            
    int index= address/8;
    if(index >= CPU.DATALIMIT || index < 0 || address < 0)
        throw new MemoryElementNotFoundException();
    boolean control=CacheControl.get(index);
    if(control==true)
    {
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
                    CacheData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+4);
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
        else 
        {
            mem.readW(address); 
            index = address/8;
            CacheControl.put(index,true);
            
            numStore++;
            if(offset == 0)
		    {	
			findOffset();
		    }
             stall = stall++;
            throw new MemoryExceptionStall(stall);
           
        }
        
    }
            
            
    public long CreadWU(int address) throws MemoryExceptionStall,MemoryElementNotFoundException,NotAlingException
  {
            
    int index= address/8;
    if(index >= CPU.DATALIMIT || index < 0 || address < 0)
        throw new MemoryElementNotFoundException();
    boolean control=CacheControl.get(index);
    if(control==true)
    {
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
                    CacheData.add("r "+Converter.binToHex(Converter.intToBin(64,addr))+" "+4);
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
        else 
        {
            mem.readWU(address); 
            index = address/8;
            CacheControl.put(index,true);
            
            numStore++;
            if(offset == 0)
		    {	
			findOffset();
		    }
             stall = stall++;
            throw new MemoryExceptionStall(stall);
        }
        
    }
            
            
    public void CwriteB(int address, int data) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException
        {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                    
                mem.writeB(address,data);
                CacheControl.put(index,true);
                numStore++;
                stall = stall++;
                if(offset == 0)
		{	
			findOffset();
		}
        
        try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    CacheData.add("w "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
                    throw new MemoryExceptionStall(stall);
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
            else return ;
            }
    
        
    public void CwriteD(int address, String data) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularStringOfBitsException
        {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                mem.writeD(address,data);
                CacheControl.put(index,true);
                    numStore++;
                if(offset == 0)
		{	
			findOffset();
		}
        try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    CacheData.add("w "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		            catch(IrregularStringOfBitsException ex)
		            {
			        ex.printStackTrace();
		            }
        
                stall++;
                throw new MemoryExceptionStall(stall);
                
                }
            else  return;
            }
            
     public void CwriteH(int address,int data) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException
       {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                mem.writeH(address,data);
                CacheControl.put(index,true);
                    numStore++;
                if(offset == 0)
		{	
			findOffset();
		}
        try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    CacheData.add("w "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		            catch(IrregularStringOfBitsException ex)
		            {
			        ex.printStackTrace();
		            }
        
                stall++;
                throw new MemoryExceptionStall(stall);
                
                }
            else  return;
            }
            
            
            
    public void CwriteW(int address,int data) throws MemoryExceptionStall,MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException
       {
            int index= address/8;
            if(index >= CPU.DATALIMIT || index < 0 || address < 0)
			throw new MemoryElementNotFoundException();
            boolean control=CacheControl.get(index);
            if(control==false){
                mem.writeW(address,data);
                CacheControl.put(index,true);
                    numStore++;
                if(offset == 0)
		{	
			findOffset();
		}
        try
                    {	
                    long addr = Long.parseLong(Converter.hexToLong("0x"+Converter.binToHex(Converter.positiveIntToBin(64,address))));
                    addr+=offset;
                    CacheData.add("w "+Converter.binToHex(Converter.intToBin(64,addr))+" "+1);
                    }
                catch(IrregularStringOfHexException ex)
                    {
                    ex.printStackTrace();
                    }
		            catch(IrregularStringOfBitsException ex)
		            {
			        ex.printStackTrace();
		            }
        
                stall++;
                throw new MemoryExceptionStall(stall);
                
                }
            else  return;
            }
            
            
            
    //Funzione che genera un numero random[tra 1 e 2]di stalli.AL MOMENTO NON UTILIZZATA potenzialemente da sostituire ai 2 stalli posti di default in presenza di un miss dati.
    /* public int chekCache(int address){
             int stalls=(int)Math.random()+1;
             return stalls;
     }*/

 }
     
     