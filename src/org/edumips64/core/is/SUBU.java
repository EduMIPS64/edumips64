/*
 * SUBU.java
 *
 * 18th may 2007
 * Instruction SUBU of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Sciuto Lorenzo - Urzì Erik - Giorgio Scibilia
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

/**<pre>
 *      Syntax: SUBU rd, rs, rt
 * Description: To subtract 32-bit integers;
 *              The 32-bit word value in GPR rt is subtracted from 
 *              the 32-bit value in GPR rs and the 32-bit arithmetic result 
 *              is sign-extended and placed into GPR rd.
 *</pre>
 * @author Sciuto Lorenzo - Urzì Erik - Giorgio Scibilia
 */
public class SUBU extends ALU_RType{   
    final String OPCODE_VALUE="100011"; 
    public SUBU() 
    {
        super.OPCODE_VALUE = OPCODE_VALUE;
	name="SUBU";
    }    

    public void EX() throws IrregularStringOfBitsException, IntegerOverflowException,TwosComplementSumException
   {
        //getting strings from temporary registers
        String rs=TR[RS_FIELD].getBinString();
        String rt=TR[RT_FIELD].getBinString();
        //cutting the high part of registers
        rs=rs.substring(32,64);
        rt=rt.substring(32,64);
        String outputstring=InstructionsUtils.twosComplementSubstraction(rs,rt);
	//performing sign extension
	outputstring=outputstring.substring(0,32);
	String filledOutputstring=outputstring;
	for(int i=0; i<32; i++)
		filledOutputstring = outputstring.charAt(0)+filledOutputstring;
       	TR[RD_FIELD].setBits(filledOutputstring,0);
	if(enableForwarding)
	{
		doWB();
	}
	
   }    

    
}
