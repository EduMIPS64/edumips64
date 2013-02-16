/*
 * SRL.java
 *
 * 18th may 2007
 * Instruction SRL of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Urzì Erik - Sciuto Lorenzo - Giorgio Scibilia
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
 *      Syntax: SRL rd, rt, sa
 * Description: To execute a logical right-shift of a word by a fixed amount of 0 to 31 bits
 *              The word contents of GPR rt are shifted right, inserting zeros 
 *              into the emptied bits; the result is sign-extended and placed in GPR rd.
 *</pre>
 * @author Urzì Erik - Sciuto Lorenzo - Giorgio Scibilia
 */
public class SRL extends ALU_RType {
    final int RD_FIELD=0;
    final int RT_FIELD=1;
    final int SA_FIELD=2;
    final int RD_FIELD_INIT=16;
    final int RT_FIELD_INIT=11;
    final int SA_FIELD_INIT=21;
    final int RD_FIELD_LENGTH=5;
    final int RT_FIELD_LENGTH=5;
    final int SA_FIELD_LENGTH=5;
    final String OPCODE_VALUE="000010";

   SRL()
   {
   	super.OPCODE_VALUE = OPCODE_VALUE;
	name="SRL";
        syntax="%R,%R,%U";
   }    
   //since this operation is carried out writing sa value as unsigned value, it is necessary 
   //the overriding of ID method
   public void ID() throws RAWException,IrregularWriteOperationException
   {
        //if the source register is valid passing his own value into a temporary register
        Register rt=cpu.getRegister(params.get(RT_FIELD));
        if(rt.getWriteSemaphore()>0)
            throw new RAWException();
        TR[RT_FIELD]=rt;
        //writing on a temporary register the sa field as unsigned value
        TR[SA_FIELD].writeDoubleWord(params.get(SA_FIELD));
        //increment the semaphore of the destination register
        Register rd=cpu.getRegister(params.get(RD_FIELD));
        rd.incrWriteSemaphore();        
   }
    
    public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException 
    {
	//getting strings from temporary registers
	int sa=(int)TR[SA_FIELD].getValue();
	String rt=TR[RT_FIELD].getBinString();
	//cutting the high part of register
	rt=rt.substring(32,64);
	//composing new shifted value
	StringBuffer sb=new StringBuffer();
	sb.append("00000000000000000000000000000000");
	for(int i=0;i<sa;i++)
	       	sb.append('0');
	sb.append(rt.substring(0,32-sa));
	TR[RD_FIELD].setBits(sb.substring(0),0);
	if(enableForwarding)
	{
		doWB();
	}
    }
    public void pack() throws IrregularStringOfBitsException {
        //conversion of instruction parameters of "params" list to the "repr" form (32 binary value) 
        repr.setBits(OPCODE_VALUE,OPCODE_VALUE_INIT);
        repr.setBits(Converter.intToBin(SA_FIELD_LENGTH,params.get(SA_FIELD)),SA_FIELD_INIT);
        repr.setBits(Converter.intToBin(RT_FIELD_LENGTH,params.get(RT_FIELD)),RT_FIELD_INIT);
        repr.setBits(Converter.intToBin(RD_FIELD_LENGTH,params.get(RD_FIELD)),RD_FIELD_INIT);        
    }
}
