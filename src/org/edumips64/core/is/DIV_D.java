/*
 * DIV_D.java
 *
 * 1th june 2007
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


package org.edumips64.core.is;
import org.edumips64.core.*;
import org.edumips64.core.fpu.*;
import org.edumips64.utils.*;
import java.math.*;

/**
 * <pre>
 *      Format: DIV.D fd, fs, ft
 * Description: To divide FP values
 *   Operation: fd = fs / ft
 *</pre>
 */
class DIV_D extends FPArithmeticInstructions {
	final String OPCODE_VALUE="000011";
	String FMT_FIELD="10001"; //DOUBLE IS 17
	String NAME = "DIV.D";
	
	
	public DIV_D() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		super.FMT_FIELD = FMT_FIELD;
		name=NAME;
	}
	
	public void EX() throws IrregularStringOfBitsException,FPInvalidOperationException,FPUnderflowException,FPOverflowException, FPDivideByZeroException {
		//getting values from temporary registers
		String operand1=TRfp[FS_FIELD].getBinString();
		String operand2=TRfp[FT_FIELD].getBinString();
		String outputstring=null;
		try{
			outputstring=FPInstructionUtils.doubleDivision(operand1,operand2);
		}
		catch(Exception ex){
			//if the enable forwarding is turned on we have to ensure that registers
			//should be unlocked also if a synchronous exception occurs. This is performed
			//by executing the WB method before raising the trap
			if(enableForwarding){
				doWB();
			}
			if(ex instanceof FPInvalidOperationException){
				throw new FPInvalidOperationException();
			}else if(ex instanceof FPUnderflowException){
				throw new FPUnderflowException();
			}else if(ex instanceof FPOverflowException){
				throw new FPOverflowException();
			}else if(ex instanceof FPDivideByZeroException){
				throw new FPDivideByZeroException();
			}
		}
		TRfp[FD_FIELD].setBits(outputstring,0);
		if(enableForwarding) {
			doWB();
		}
		
	}
}
