/*
 * ORI.java
 *
 * 22th may 2006
 * Instruction ORI of the MIPS64 Instruction Set
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
 * Syntax:        ORI rt, rs, immediate
 * Description:   To do a bitwise logical OR with a constant
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */

class ORI extends ALU_IType {
	final String OPCODE_VALUE="001101";
	ORI() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		this.name="ORI";
	}
	//since this operation is carried out with zero padding of immediate, against sign_extend(immediate) methodology
	//of all others instructions in the same category, is necessary the overriding of ID method
	public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException {
		//if the source register is valid passing its own values into a temporary register
		Register rs=cpu.getRegister(params.get(RS_FIELD));
		if(rs.getWriteSemaphore()>0) {
			throw new RAWException();
		}
		TR[RS_FIELD]=rs;
		//locking the target register
		Register rt=cpu.getRegister(params.get(RT_FIELD));
		rt.incrWriteSemaphore();
		//writing the immediate value of "params" on a temporary register
		TR[IMM_FIELD].writeHalf(params.get(IMM_FIELD));
		//forcing zero-padding in the same temporary register
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<48;i++)
			sb.append('0');
		sb.append(TR[IMM_FIELD].getBinString().substring(48,64));
		TR[IMM_FIELD].setBits(sb.substring(0),0);
		
	}
	public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException,IrregularWriteOperationException {
		//getting values from temporary registers
		String imm=TR[IMM_FIELD].getBinString();
		String rs=TR[RS_FIELD].getBinString();
		StringBuffer sb=new StringBuffer();
		boolean immbit,rsbit,resbit;
		//performing bitwise OR between immediate and rs register
		for(int i=0;i<64;i++) {
			rsbit=rs.charAt(i)=='1'? true:false;
			immbit=imm.charAt(i)=='1'? true:false;
			resbit=rsbit||immbit;
			sb.append(resbit==true?'1':'0');
		}
		TR[RT_FIELD].setBits(sb.substring(0),0);
		if(enableForwarding) {
			doWB();
		}
		
	}
}
