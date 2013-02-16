/*
 * FlowControl_IType.java
 *
 * 15th may 2006
 * Subgroup of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo, Russo Daniele
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
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

/** This is the base class for immediate flow control instructions
 *
 * @author Trubia Massimo, Russo Daniele
 */
public abstract class FlowControl_IType extends FlowControlInstructions {
	final static int RS_FIELD=0;
	final static int RT_FIELD=1;
	final static int OFFSET_FIELD=2;
	final static int RT_FIELD_INIT=11;
	final static int RS_FIELD_INIT=6;
	final static int OFFSET_FIELD_INIT=16;
	final static int RT_FIELD_LENGTH=5;
	final static int RS_FIELD_LENGTH=5;
	final static int OFFSET_FIELD_LENGTH=16;
	String OPCODE_VALUE="";
	final static int OPCODE_VALUE_INIT=0;
	public FlowControl_IType() {
		this.syntax="%R,%R,%E";
		this.paramCount=3;
	}
	
	public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException,JumpException,TwosComplementSumException {
	}
	
	public void EX() throws IrregularStringOfBitsException, IntegerOverflowException,IrregularWriteOperationException {
	}
	
	public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
	}
	
	public void WB() throws IrregularStringOfBitsException {
	}
	
	public void pack() throws IrregularStringOfBitsException {
		repr.setBits(OPCODE_VALUE, OPCODE_VALUE_INIT);
		repr.setBits(Converter.intToBin(RS_FIELD_LENGTH, params.get(RS_FIELD)), RS_FIELD_INIT);
		repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
		repr.setBits(Converter.intToBin(OFFSET_FIELD_LENGTH, params.get(OFFSET_FIELD)/4), OFFSET_FIELD_INIT);
	}
	
}
