/*
 * JALR.java
 *
 *  22th may 2006
 * Instruction JALR of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo, Russo Daniele
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
package edumips64.core.is;
import edumips64.core.*;
import edumips64.utils.*;
/**
 * <pre>
 *      Syntax: JALR rs
 *     Purpose: To execute a procedure call to an instruction address in a register
 *              Place the return address link in GPR 31. 
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 *
 */

public class JALR extends FlowControl_RType {
	final int PC_VALUE=0;
	final String OPCODE_VALUE="001001";
	public JALR() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		this.name="JALR";
	}
	
	public void ID() throws RAWException,IrregularWriteOperationException,IrregularStringOfBitsException,JumpException {
		//saving PC value into a temporary register
		cpu.getRegister(31).incrWriteSemaphore();  //deadlock !!!
		TR[PC_VALUE].writeDoubleWord(cpu.getPC().getValue()-4);
		cpu.getPC().setBits(cpu.getRegister(params.get(RS_FIELD)).getBinString(),0);
		if(enableForwarding) {
			doWB();
		}
		throw new JumpException();
	}
	
	public void EX() throws IrregularStringOfBitsException,IntegerOverflowException, IrregularWriteOperationException {
	}
	
	public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
	}
	
	
	public void WB() throws IrregularStringOfBitsException {
		if(!enableForwarding) {
			doWB();
		}
	}
	public void doWB() throws IrregularStringOfBitsException {
		cpu.getRegister(31).setBits(TR[PC_VALUE].getBinString(),0);
		cpu.getRegister(31).decrWriteSemaphore();  //deadlock!!!
	}
	
}



