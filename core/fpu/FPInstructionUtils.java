/*
 * FPInstructionUtils.java
 *
 * 6th may, 2007
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

package edumips64.core.fpu;
import java.math.*;

/** Group of functions used in the Floating point unit
 */
public class FPInstructionUtils 
{
	public static void main(String[] args) throws FPExponentTooLargeException,FPOverflowException,FPUnderflowException {
		//System.out.println(doubleToBin("1.9406564584124654417656879286821E324345",false));
	}
    
	/** Converts a double value passed as string to a 64 bit binary string according with IEEE754 standard for double precision floating point numbers
	*  @param value the double value in the format "123.213" or "1.23213E2"
	*		    value belongs to [-1.797693134862315708145274237317E308,-4.9406564584124654417656879286822E-324] U [4.9406564584124654417656879286822E-324, 1.797693134862315708145274237317E308]
	*  @param exceptionEnabled if this boolean is true  ExponentTooLargeException,FPOverflowException,FPUnderflowException may happen
	*  @throw ExponentTooLargeException,FPOverflowException,FPUnderflowException
	*  @return the binary string
	*/    
	public static String doubleToBin(String value,boolean exceptionEnabled) throws FPExponentTooLargeException,FPOverflowException,FPUnderflowException
	{
	    String PLUSINFINITY="0111111111110000000000000000000000000000000000000000000000000000";
	    String MINUSINFINITY="1111111111110000000000000000000000000000000000000000000000000000";
	    String PLUSZERO="0000000000000000000000000000000000000000000000000000000000000000";
	    String MINUSZERO="1000000000000000000000000000000000000000000000000000000000000000";

	    try //Check if the exponent is not in signed 32 bit, in this case the NumberFormatException occurs
	    {
		BigDecimal value_bd=new BigDecimal(value);
		BigDecimal theBiggest=new BigDecimal("1.797693134862315708145274237317E308");
		BigDecimal theSmallest=new BigDecimal("-1.797693134862315708145274237317E308");
		BigDecimal theZeroMinus= new BigDecimal("-4.9406564584124654417656879286822E-324");
		BigDecimal theZeroPlus=new BigDecimal("4.9406564584124654417656879286822E-324");
		BigDecimal zero=new BigDecimal(0);

		//Check for overflow
		if(value_bd.compareTo(theBiggest)==1 || value_bd.compareTo(theSmallest)==-1)
		{
			//exception
			if(exceptionEnabled)
				throw new FPOverflowException();
			if(value_bd.compareTo(theBiggest)==1)
				return PLUSINFINITY;
			if(value_bd.compareTo(theSmallest)==-1)
				return MINUSINFINITY;
		}
		//Check for underflow
		if(value_bd.compareTo(theZeroMinus)==1 && value_bd.compareTo(theZeroPlus)==-1)
		{
			if(exceptionEnabled)
				throw new FPUnderflowException();
			if(value_bd.compareTo(zero)==1)
				return PLUSZERO;
			if(value_bd.compareTo(zero)==-1)
				return MINUSZERO;
		}
	
		String output= Long.toBinaryString(Double.doubleToLongBits(value_bd.doubleValue()));
		

		return padding64(output);
	    }
	    catch(NumberFormatException e)
	    {
		    if(exceptionEnabled)
			throw new FPExponentTooLargeException();
		    return PLUSZERO;
	    }
	}

	/**In order to create a 64 bit binary string, the zero-padding on the left of the value is carried out
	*  @param value the string to pad
	*  @return Padded string	
	*/	
	public static String padding64(String value)
	{
		StringBuffer sb=new StringBuffer();
		sb.append(value);
		for(int i=0;i<64 - value.length();i++)
			sb.insert(0,"0");
		return sb.toString();
	}
	
	
	/*
	public static String doubleToHex(double value)
	{
		return Long.toHexString(Double.doubleToLongBits(value));
	}
	*/
   
}




	

