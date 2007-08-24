/*
 * DADD.java
 *
 * 8th may 2006
 * Instruction DADD of the MIPS64 Instruction Set
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
 *      Format: DADD rd, rs, rt
 * Description: To add 64-bit integers. If overflow occurs, then trap.
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 *
 */
class DADD extends ALU_RType {
	final String OPCODE_VALUE="101100";
	
	
	public DADD() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		name="DADD";
	}
	
	public void EX() throws IrregularStringOfBitsException,IntegerOverflowException,TwosComplementSumException {
		//getting strings from temporary registers
		String rs=TR[RS_FIELD].getBinString();
		String rt=TR[RT_FIELD].getBinString();
		//performing mips64 operations to detect IntegerOverflow
		rs=rs.charAt(0)+rs;
		rt=rt.charAt(0)+rt;
		String outputstring=InstructionsUtils.twosComplementSum(rs,rt);
		//comparison between the two most significant bits of the outputstring and
		//raising integer overflow if the first bit is different from the second one
		if(outputstring.charAt(0)!=outputstring.charAt(1))
			throw new IntegerOverflowException();
		else
			outputstring=outputstring.substring(1,65);
		TR[RD_FIELD].setBits(outputstring,0);
		if(enableForwarding) {
			doWB();
		}
	}
	
}
