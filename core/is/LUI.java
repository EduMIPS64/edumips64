/*
 * LUI.java
 *
 * 21th may 2006
 * Instruction LUI of the MIPS64 Instruction Set
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


/** <pre>
 *  Format:        LUI rt, rs, immediate
 *  Description:   The 16-bit immediate is shifted left 16 bits and concatenated 
 *                 with 16 bits of low-order zeros. 
 *</pre>
  * @author Trubia Massimo, Russo Daniele
 */
class LUI extends ALU_IType
{
    final static int RT_FIELD=0; 
    final static int IMM_FIELD=1;
    final static int RT_FIELD_INIT=11;
    final static int RS_FIELD_INIT=6;
    final static int IMM_FIELD_INIT=16;
    final static int RT_FIELD_LENGTH=5;
    final static int RS_FIELD_LENGTH=5;
    final static int IMM_FIELD_LENGTH=16;
    final String OPCODE_VALUE="001111";

    public LUI()
    {
	    syntax = "%R,%I";
	    //super.OPCODE_VALUE = OPCODE_VALUE;
	    this.name="LUI";
    }
    
    public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException {
	//if the source register is valid passing its own values into a temporary register
        //locking the target register
        Register rt=cpu.getRegister(params.get(RT_FIELD));
        rt.incrWriteSemaphore();
        //writing the immediate value of "params" on a temporary register
        TR[IMM_FIELD].writeHalf(params.get(IMM_FIELD));  
    
    }
    public void EX() throws IrregularStringOfBitsException,IrregularWriteOperationException 
    {
        //getting strings from temporary registers                   
        String imm=TR[IMM_FIELD].getBinString().substring(16,64);
        String imm_shift=imm+"0000000000000000";
        long imm_shift_lng=Converter.binToLong(imm_shift,false);
        TR[RT_FIELD].writeDoubleWord(imm_shift_lng);
	if(enableForwarding)
	{
		doWB();
	}
    }
    public void pack() throws IrregularStringOfBitsException
    {
    	repr.setBits(OPCODE_VALUE, 0);
	repr.setBits(Converter.intToBin(RS_FIELD_LENGTH,0 ), RS_FIELD_INIT);
	repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
	repr.setBits(Converter.intToBin(IMM_FIELD_LENGTH, params.get(IMM_FIELD)), IMM_FIELD_INIT); 
    }
    
}
