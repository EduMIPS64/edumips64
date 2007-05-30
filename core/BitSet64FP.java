/* BitSet64FP.java
 *
 * This class models a 64-bit array, useful for floating point registers and memory representation.
 * (c) 2006 Massimo Trubia
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
package edumips64.core;

//import edumips64.utils.*;
import edumips64.utils.Converter;
import edumips64.utils.IrregularStringOfBitsException;
import java.util.BitSet;
import edumips64.core.fpu.*;

/** This class models a 64-bit array, useful for floating point registers
 * @author Massimo Trubia
 * */

public class BitSet64FP extends FixedBitSet {
	
	/** Creates a default new instance of BitSet64FP. */
	public BitSet64FP(){
		super();
		size = 64;
	}
	
	/** Writes a floating point double precision number into this FixedBitSet: the value to be written must be in the range
	 * [A=-1.797693134862315708145274237317E308,B=-4.9406564584124654417656879286822E-324] U [C=4.9406564584124654417656879286822E-324, D=1.797693134862315708145274237317E308].
	 * For values that belong to ]-Infinity,A[ U ]D,+ Infinity[  an overflow exception will be thrown, on the contrary
	 * values that belong to ]B,C[ an underflow exception will be thrown. 
	 * @param value double number to be written: must be on the format  "2.345" or "2345E-3"
	 * @throws FPExponentTooLargeException is catched into the method FPInstructionUtils.doubleTobin()
	 * @throws FPUnderflowException,FPOverflowException, IrregularWriteOperationException,FPInvalidOperationException
	 */
	public void writeDouble(double value) throws FPExponentTooLargeException,FPUnderflowException,FPOverflowException, FPInvalidOperationException, IrregularWriteOperationException
	{
		this.reset(false);
		String bits=FPInstructionUtils.doubleToBin(value +""); 
		try
		{
			this.setBits(bits,0);
		}
		catch(IrregularStringOfBitsException e)
		{
			e.printStackTrace();
			throw new IrregularWriteOperationException();
		}
	}

	/** Writes a floating point double precision number expressed as string into this FixedBitSet: the value to be written must be in the range
	 * [A=-1.797693134862315708145274237317E308,B=-4.9406564584124654417656879286822E-324] U [C=4.9406564584124654417656879286822E-324, D=1.797693134862315708145274237317E308].
	 * For values that belong to ]-Infinity,A[ U ]D,+ Infinity[  an overflow exception will be thrown, on the contrary
	 * values that belong to ]B,C[ an underflow exception will be thrown. 
	 * @param value double number a string to be written: must be on the format  "2.345" or "2345E-3"
	 * @throws FPExponentTooLargeException is catched into the method FPInstructionUtils.doubleTobin()
	 * @throws FPUnderflowException,FPOverflowException, IrregularWriteOperationException,FPInvalidOperationException
	 */
	public void writeDouble(String value) throws FPExponentTooLargeException, FPOverflowException, FPUnderflowException, FPInvalidOperationException, IrregularWriteOperationException
	{
		this.reset(false);
		String bits=FPInstructionUtils.doubleToBin(value); 
		try
		{
			this.setBits(bits,0);
		}
		catch(IrregularStringOfBitsException e)
		{
			e.printStackTrace();
			throw new IrregularWriteOperationException();
		}
		
	}
	
	/**Returns a string with a double value or the name of a special value
	  * it is recommended the use of this method only for the visualisation of the double value because it may return an alphanumeric value
	  * @return the double value or the special values "Quiet NaN","Signaling NaN", "Positive infinity", "Negative infinity","Positive zero","Negative zero"
	  */
	public String readDouble() 
	{
		return FPInstructionUtils.binToDouble(this.getBinString());
	}

	
}
		
		

