/*
 * J.java
 *
 *  8th may 2006
 * Instruction J of the MIPS64 Instruction Set
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
package org.edumips64.core.is;
import org.edumips64.core.*;
import org.edumips64.utils.*;
/**
 * <pre>
 *      Syntax: J targetJ
 * Description: To branch within the current 256 MB-aligned region
 *               This is a PC-region branch (not PC-relative); the effective 
 *              target address is in the ÂcurrentÂ 256 MB-aligned region. 
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 *
 */

public class J extends FlowControl_JType {
	final String OPCODE_VALUE="000010";
	
	/** Creates a new instance of J */
	public J() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		this.name="J";
	}
	
	public void ID() throws RAWException,IrregularWriteOperationException,IrregularStringOfBitsException, JumpException {
		//converting INSTR_INDEX into a bynary value of 26 bits in length
		String instr_index=Converter.positiveIntToBin(28,params.get(INSTR_INDEX));
		//appending the 35 most significant bits of the program counter on the left of "instr_index"
		Register pc=cpu.getPC();
		String pc_all=pc.getBinString();
		String pc_significant=pc_all.substring(0,36);
		String pc_new=pc_significant+instr_index;
		pc.setBits(pc_new,0);
		throw new JumpException();
	}
	
	public void EX() throws IrregularStringOfBitsException,IntegerOverflowException {
	}
	
	public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
	}
	
	public void WB() throws IrregularStringOfBitsException {
	}
	
}
