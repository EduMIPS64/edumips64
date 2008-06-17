/*
 * SH.java
 *
 * 8th may 2006
 * Instruction SH of the MIPS64 Instruction Set
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
 *       Syntax: SH rt, offset(base)
 *  Description: Stores the halfword in rt to memory
 *   		 i.e: memory[base+offset] = rt
 *               
 *  
 * </pre>
 * @author IS Group
 */
class SH extends Storing
{
	final String OPCODE_VALUE="101001";
	public SH()
	{
		super.OPCODE_VALUE = OPCODE_VALUE;
		this.name="SH";
	}

	public void MEM() throws MemoryExceptionStall,IrregularStringOfBitsException,MemoryElementNotFoundException, AddressErrorException
	{ 
		try
		{
			//restoring the address from the temporary register
			long address=TR[OFFSET_PLUS_BASE].getValue();
			//For the trace file
			Dinero din=Dinero.getInstance();
			din.Store(Converter.binToHex(Converter.positiveIntToBin(64,address)),2);
			/*MemoryElement memEl = memory.getCell((int)address);
			//writing on the memory element the RT register
			memEl.writeHalf(TR[RT_FIELD].readHalf(0), (int) (address%8));*/
                        
                         //MODIFICA
                       // memory.writeH((int)address,TR[RT_FIELD].readHalf(0));
                         cache.CwriteH((int)address,TR[RT_FIELD].readHalf(0));
                        //MODIFICA FINE
			if(enableForwarding)
			{
				WB();
			}
		}
		catch(NotAlingException er)
		{
			throw new AddressErrorException();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
