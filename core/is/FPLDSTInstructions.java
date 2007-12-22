/*
 * FPLDSTInstructions.java
 *
 * 27th may 2007
 * (c) 2006 EduMips64 project - Trubia Massimo
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

/**This is the base class of FP Load store instructions
 *
 * @author Trubia Massimo
 */

public abstract class FPLDSTInstructions extends LDSTInstructions {
	final static int FT_FIELD=0;
	final static int FT_FIELD_INIT=11;
	final static int FT_FIELD_LENGTH=5;
	public FPLDSTInstructions() {
		this.syntax="%F,%L(%R)";
		this.paramCount=3;
	}
	public abstract void ID() throws RAWException,IrregularWriteOperationException,IrregularStringOfBitsException,TwosComplementSumException,WAWException;
	public abstract void EX() throws IrregularStringOfBitsException,IntegerOverflowException;
	public abstract void MEM() throws MemoryExceptionStall,IrregularStringOfBitsException,MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException;
	public abstract void WB() throws IrregularStringOfBitsException;
	public void pack() throws IrregularStringOfBitsException {
		//conversion of instruction parameters of params list to the "repr" 32 binary value
		repr.setBits(OPCODE_VALUE,0);
		repr.setBits(Converter.intToBin(BASE_FIELD_LENGTH,params.get(BASE_FIELD)),BASE_FIELD_INIT);
		repr.setBits(Converter.intToBin(FT_FIELD_LENGTH,params.get(FT_FIELD)),FT_FIELD_INIT);
		repr.setBits(Converter.intToBin(OFFSET_FIELD_LENGTH,params.get(OFFSET_FIELD)),OFFSET_FIELD_INIT);
	}
	
}

 




