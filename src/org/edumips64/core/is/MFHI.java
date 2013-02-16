/*
 * MFHI.java
 *
 * 1th june 2006
 * Instruction MFHI of the MIPS64 Instruction Set
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

//per diagnostica
import java.util.*;

/**
 * <pre>
 * Syntax:      MFHI rd
 * Description: rd = HI
 *              To copy the special purpose HI register to a GPR
 *              The contents of special register HI are loaded into GPR rd.
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 *
 */
class MFHI extends ALU_RType {
	final int RD_FIELD=0;
	final int HI_REG=1;
	final String OPCODE_VALUE="010000";
	
	public MFHI() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		syntax="%R";
		name="MFHI";
	}
	public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException {
		//if the HI register is valid passing his own value into temporary register
		Register hi_reg=cpu.getHI();
		if(hi_reg.getWriteSemaphore()>0)
			throw new RAWException();
		TR[HI_REG]=hi_reg;
		//locking the destination register
		Register rd=cpu.getRegister(params.get(RD_FIELD));
		rd.incrWriteSemaphore();
	}
	public void EX() throws IrregularStringOfBitsException,IntegerOverflowException,TwosComplementSumException {
		if(enableForwarding) {
			doWB();
		}
	}
	
	public void WB() throws IrregularStringOfBitsException {
		if(!enableForwarding)
			doWB();
	}
	
	public void doWB() throws IrregularStringOfBitsException {
		cpu.getRegister(params.get(RD_FIELD)).setBits(TR[HI_REG].getBinString(),0);
		cpu.getRegister(params.get(RD_FIELD)).decrWriteSemaphore();
	}
	public void pack() throws IrregularStringOfBitsException {
		//conversion of instruction parameters of "params" list to the "repr" form (32 binary value)
		repr.setBits(OPCODE_VALUE,OPCODE_VALUE_INIT);
		repr.setBits(Converter.intToBin(RD_FIELD_LENGTH,params.get(RD_FIELD)),RD_FIELD_INIT);
	}
	
}
