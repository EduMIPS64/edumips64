/*
 *  DSLLV.java
 *
 * 8th may 2006
 * Instruction DSLLV of the MIPS64 Instruction Set
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
 *      Syntax: DSLLV rd, rt, rs
 * Description: To execute a left-shift of a doubleword by a variable number of bits.
 *              The 64-bit doubleword contents of GPR rt are shifted left, inserting 
 *              zeros into the emptied bits; the result is placed in GPR rd. 
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 **/
public class DSLLV extends ALU_RType
{
    final int RD_FIELD=0;
    final int RT_FIELD=1;
    final int RS_FIELD=2;
    final int RD_FIELD_INIT=11;
    final int RT_FIELD_INIT=16;
    final int RS_FIELD_INIT=21;
    final int RD_FIELD_LENGTH=5;
    final int RT_FIELD_LENGTH=5;
    final int RS_FIELD_LENGTH=5;
    final String OPCODE_VALUE="010100";
	/** Creates a new instance of DSLLV */
	public DSLLV()
	{
		super.OPCODE_VALUE = OPCODE_VALUE;
		name = "DSLLV";
	}
	
	public void EX() 
		throws IrregularStringOfBitsException,IntegerOverflowException,TwosComplementSumException 
	{
		String rt = TR[RT_FIELD].getBinString();
		String rs = TR[RS_FIELD].getBinString();
		String shift = "",rd;
		int nbits;
		shift=rs.substring(58);
		int shift_value = Converter.binToInt(shift,true);
                StringBuffer buf = new StringBuffer(rt);
                for(int i = 0; i  < shift_value; i++)
                    buf.append('0');
                String target = buf.substring(shift_value);

		TR[RD_FIELD].setBits(target,0);
		if(enableForwarding)
		{
			doWB();
		}
	}
	
}
