/*
 * MOVN.java
 *
 * 26th may 2006
 * Instruction MOVN of the MIPS64 Instruction Set
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
 * Format:      MOVN rd, rs, rt  
 * Description: if rt != 0 then rd = rs
 *              If the value in GPR rt is not equal to zero, then the contents 
 *              of GPR rs are placed into GPR rd.
  *</pre>
 * @author Trubia Massimo, Russo Daniele
 *
 */
class MOVN extends ALU_RType
{
    final String OPCODE_VALUE="001011";
    
    
    public MOVN()
    {
    	super.OPCODE_VALUE = OPCODE_VALUE;
        name="MOVN";
    }

    public void EX() throws IrregularStringOfBitsException,IntegerOverflowException,TwosComplementSumException 
    {
        //getting strings from temporary registers
        String rs=TR[RS_FIELD].getBinString();
        String rt=TR[RT_FIELD].getBinString();
        //saving rd value because, if the move test is false, the old value must be rewritten in rd
        TR[RD_FIELD].setBits(cpu.getRegister(params.get(RD_FIELD)).getBinString(),0);
        boolean rtbit,diff=false;
        for(int i=0;i<64;i++)
        {
            rtbit=rt.charAt(i)=='1'?true:false;
            if(diff=rtbit^false)
            {
                TR[RD_FIELD].setBits(rs,0);
                break;
            }
        }
	if(enableForwarding)
	{
		doWB();
	}
	
    }
    



   
}
