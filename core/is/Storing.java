/*
 * Storing.java
 *
 * 22th may 2006
 * Exception of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo, Russo Daniele
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License. * *
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

package edumips64.core.is;
import edumips64.core.*;
import edumips64.utils.*;


/** This is the base class for the storing instructions
 *
 * @author Massimo
 */
public class Storing extends LDSTInstructions{
	
	/** Creates a new instance of Storing */
	public Storing() {
	}
	
	public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException {
		//if the base register and the rt register are valid passing value of rt register into a temporary register
		Register base=cpu.getRegister(params.get(BASE_FIELD));
		Register rt=cpu.getRegister(params.get(RT_FIELD));
		if(base.getWriteSemaphore()>0 || rt.getWriteSemaphore()>0)
			throw new RAWException();
		TR[RT_FIELD].setBits(rt.getBinString(),0);
		//calculating  address (base+offset)
		long address = base.getValue() + params.get(OFFSET_FIELD);
		//saving address into a temporary register
		TR[OFFSET_PLUS_BASE].writeDoubleWord(address);
	}
	
	public void EX() throws IrregularStringOfBitsException, IntegerOverflowException {
	}
	
	public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
	}
	
	public void WB() throws IrregularStringOfBitsException {
	}
	
	
	
	public static void main(String args[]) {
		
		try {
			SD inst=new SD();
			//SH inst = new SH();
			//SW inst=new SW();
			//SB inst=new SB();
			CPU cpu=CPU.getInstance();
			inst.params.add(1);
			inst.params.add(8);
			inst.params.add(0);
			//R1=43524464456523452L
			cpu.getRegister(inst.params.get(RT_FIELD)).writeDoubleWord(9223372036854775807L);
			inst.pack();
			inst.ID();
			inst.MEM();
			System.out.println(memory);
		} catch(Exception e) {
			System.out.println("Eccezion: "+e);
			e.printStackTrace();
		}
	}
}
