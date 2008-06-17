/*
 * LDSTInstructions.java
 *
 * 8th may 2006
 * Instruction LD of the MIPS64 Instruction Set
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
 *         Format: LD rt, offset(base)
 *    Description: rt = memory[base+offset]
 *                 To load a doubleword from memory 
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */
class LD extends Loading {
	final String OPCODE_VALUE="110111";
	public LD() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		this.name="LD";
	}
	public void MEM() throws MemoryExceptionStall,IrregularStringOfBitsException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
		//restoring the address from the temporary register
		long address=TR[OFFSET_PLUS_BASE].getValue();
		//For the trace file
		Dinero din=Dinero.getInstance();
		din.Load(Converter.binToHex(Converter.positiveIntToBin(64,address)),8);
		
		/*MemoryElement memEl = memory.getCell((int)address);
		//reading from the memory element and saving values on LMD register
		TR[LMD_REGISTER].setBits(memEl.getBinString(),0);
		*/
                 /*MODIFICA*/
                //String value=memory.readD((int)address);
                String value=cache.CreadD((int)address);
                TR[LMD_REGISTER].setBits(value,0);
                /*MODIFICA FINE*/
            
                if(enableForwarding) {
			doWB();
		}
	}
}
