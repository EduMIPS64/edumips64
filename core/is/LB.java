/*
 * LB.java
 *
 * 20th may 2006
 * Instruction LB of the MIPS64 Instruction Set
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
 *         Syntax: LB rt, offset(base)
 *    Description: rt = memory[base+offset]
 *                 To load a byte from memory as a signed value
 * </pre>
 * @author Trubia Massimo, Russo Daniele
 */
class LB extends Loading {
	final String OPCODE_VALUE="100000";
	public LB() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		this.name="LB";
	}
	
	public  void MEM() throws /*-----*/MemoryExceptionStall,/*-----------*/IrregularStringOfBitsException,MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
		
		/**restoring the address from the temporary register*/
		/*(s) assegno alla var long address il contenuto del registro(di Instruction) TR[4]*/
                long address=TR[OFFSET_PLUS_BASE].getValue();
		//For the trace file
		//(s) Aquisisco l'unica istanza din della classe DINERO
                Dinero din=Dinero.getInstance();
                din.Load(Converter.binToHex(Converter.positiveIntToBin(64,address)),1);
		
                
                /*MemoryElement memEl = memory.getCell((int)address);
		reading from the memory element and saving values on LMD register TR[3] 
                TR[LMD_REGISTER].writeByte(memEl.readByte((int)(address%8)));
		*/
            
                /*MODIFICA*/
                /*-----------------------------------------*/
                //int value=memory.readB((int)address);
                 int value=cache.readB((int)address);
                /*------------------------------------------*/
                TR[LMD_REGISTER].writeByte(value);
                /*MODIFICA FINE*/
            
                //invoco la funzione doWB
                if(enableForwarding) {
			doWB();
		}
	}
	
}
