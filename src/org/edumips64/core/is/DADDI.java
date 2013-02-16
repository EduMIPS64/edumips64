/*
 * DADDI.java
 *
 * 8th may 2006
 * Instruction DADDI of the MIPS64 Instruction Set
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
 *       Format: DADDI rt, rs, immediate
 *  Description:  To add a constant to a 64-bit integer. If overflow occurs, then trap.
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */

class DADDI extends ALU_IType
{
	final String OPCODE_VALUE="011000";
	DADDI()
	{
		super.OPCODE_VALUE = OPCODE_VALUE;
		this.name="DADDI";
	}

	public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException
	{
		//getting strings from temporary registers
		String imm=TR[IMM_FIELD].getBinString();
		String rs=TR[RS_FIELD].getBinString();
		//performing mips64 operations to detect IntegerOverflow
		rs = rs.charAt(0)+rs;
		imm = imm.charAt(0)+imm;
		String outputstring=InstructionsUtils.twosComplementSum(rs,imm);
		//comparison between the two most significant bits of the outputstring and 
		//raising integer overflow if the first bit is different from the second one
        if(outputstring.charAt(0)!=outputstring.charAt(1)){ 
            //if the enable forwarding is turned on we have to ensure that registers 
            //should be unlocked also if a synchronous exception occurs. This is performed 
            //by executing the WB method before raising the trap 
            if(enableForwarding) 
                doWB(); 
            throw new IntegerOverflowException(); 
        }  
		else
			outputstring=outputstring.substring(1,65);
		TR[RT_FIELD].setBits(outputstring,0);
		if(enableForwarding)
		{
			doWB();
		}
	}
}
