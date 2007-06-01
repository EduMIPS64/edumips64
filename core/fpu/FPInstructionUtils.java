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
import edumips64.core.CPU;
import edumips64.utils.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

/** Group of functions used in the Floating point unit
 */
public class FPInstructionUtils 
{
	static CPU cpu=CPU.getInstance();
	final static String PLUSINFINITY= "0111111111110000000000000000000000000000000000000000000000000000";
	final static String MINUSINFINITY="1111111111110000000000000000000000000000000000000000000000000000";
	final static String PLUSZERO="0000000000000000000000000000000000000000000000000000000000000000";
	final static String MINUSZERO="1000000000000000000000000000000000000000000000000000000000000000";
	final static String BIGGEST="1.797693134862315708145274237317E308";
	final static String SMALLEST="-1.797693134862315708145274237317E308";
	final static String MINUSZERO_DEC="-4.9406564584124654417656879286822E-324";
	final static String PLUSZERO_DEC="4.9406564584124654417656879286822E-324";
	
	/*
	snan
	0x7fffffffffffffff (value used in MIPS64 to generate a new snan)
	0 11111111111 1111111111111111111111111111111111111111111111111111 (bynary equivalent)
	X 11111111111 1XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX (pattern for snans values)*/
	final static String SNAN_NEW= "0111111111111111111111111111111111111111111111111111111111111111";
	final static String SNAN_PATTERN="X111111111111XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
	
	/*qnan
	0x7ff7ffffffffffff (value used in MIPS64 to generate a new qnan)
	0 11111111111 0111111111111111111111111111111111111111111111111111 (bynary equivalent)
	X 11111111111 0XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX (pattern for qnans values)	*/
	final static String QNAN_NEW="0111111111110111111111111111111111111111111111111111111111111111";
	final static String QNAN_PATTERN="X111111111110XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
						       
	
	/** Converts a double value passed as string to a 64 bit binary string according with IEEE754 standard for double precision floating point numbers
	*  @param value the double value in the format "123.213" or "1.23213E2"
	*		    value belongs to [-1.797693134862315708145274237317E308,-4.9406564584124654417656879286822E-324] U [4.9406564584124654417656879286822E-324, 1.797693134862315708145274237317E308]
	*  @param exceptionEnabled if this boolean is true  ExponentTooLargeException,FPOverflowException,FPUnderflowException may happen
	*  @throws ExponentTooLargeException,FPOverflowException,FPUnderflowException
	*  @return the binary string
	*/    
	public static String doubleToBin(String value) throws FPExponentTooLargeException,FPOverflowException,FPUnderflowException
	{
		//if a special values is passed then the proper binary string is returned
		String old_value=value;
		value=parseKeywords(value);
		if(old_value.compareToIgnoreCase(value)!=0)
			return value;
			
	    try //Check if the exponent is not in signed 32 bit, in this case the NumberFormatException occurs
	    {
		BigDecimal value_bd=new BigDecimal(value);
		BigDecimal theBiggest=new BigDecimal(BIGGEST);
		BigDecimal theSmallest=new BigDecimal(SMALLEST);
		BigDecimal theZeroMinus= new BigDecimal(MINUSZERO_DEC);
		BigDecimal theZeroPlus=new BigDecimal(PLUSZERO_DEC);
		BigDecimal zero=new BigDecimal(0.0);

		//Check for overflow
		if(value_bd.compareTo(theBiggest)==1 || value_bd.compareTo(theSmallest)==-1)
		{
			//exception
			if(cpu.getFPExceptions(CPU.FPExceptions.OVERFLOW))
				throw new FPOverflowException();
			if(value_bd.compareTo(theBiggest)==1)
				return PLUSINFINITY;
			if(value_bd.compareTo(theSmallest)==-1)
				return MINUSINFINITY;
		}
		//Check for underflow
		if(value_bd.compareTo(theZeroMinus)==1 && value_bd.compareTo(theZeroPlus)==-1)
		{			
			if(cpu.getFPExceptions(CPU.FPExceptions.UNDERFLOW))
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
		    if(cpu.getFPExceptions(CPU.FPExceptions.OVERFLOW))
			throw new FPExponentTooLargeException();
		    return PLUSZERO;
	    }
	}
	
	/** determines if the passed string contains the keywords for special values
	 * @param value a binary string or a string containing POSITIVEINFINITY,NEGATIVEINFINITY,POSITIVEZERO,NEGATIVEZERO,QNAN,SNAN
	 * @return the proper binary string if value contains special values, or value itself if the string is not special*/
	 public static String parseKeywords(String value)
	{
		if(value.compareToIgnoreCase("POSITIVEINFINITY")==0)
			return PLUSINFINITY;
		else if(value.compareToIgnoreCase("NEGATIVEINFINITY")==0)
			return MINUSINFINITY;
		else if(value.compareToIgnoreCase("POSITIVEZERO")==0)
			return PLUSZERO;
		else if(value.compareToIgnoreCase("NEGATIVEZERO")==0)
			return MINUSZERO;
		else if(value.compareToIgnoreCase("QNAN")==0)
			return QNAN_NEW;
		else if(value.compareToIgnoreCase("SNAN")==0)
			return SNAN_NEW;
		return value;
	}
	 
	/** Determines if value is a special values
	 * @param value a binary string or special values between POSITIVEINFINITY,NEGATIVEINFINITY,POSITIVEZERO,NEGATIVEZERO,QNAN,SNAN
	 * @return true if "value" is a special value */
	public static boolean isFPKeyword(String value)
	{
		if(value.compareToIgnoreCase("POSITIVEINFINITY")==0 ||
			value.compareToIgnoreCase("NEGATIVEINFINITY")==0 ||
			value.compareToIgnoreCase("POSITIVEZERO")==0 ||
			value.compareToIgnoreCase("NEGATIVEZERO")==0 ||
			value.compareToIgnoreCase("QNAN")==0 ||
			value.compareToIgnoreCase("SNAN")==0)
			return true;
		return false;
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
	
	/** This method performs the sum between two double values, if  the passed values are Snan or Qnan
	 *  and the invalid operation exception is not enabled  the result of the operation is a Qnan  else an InvalidOperation exception occurs,
	 *  if the passed values are infinities and their signs agree, an infinity (positive or negative is returned),
	 *  if signs don't agree then an invalid operation exception occurs if this trap is enabled.
	 *  After the addition, if the result is too large in absolute value a right signed infinity is returned, else
	 *  if the FP overflow or underflow are enabled an exception occurs.
	 *  @param value1 the binary string representing the double value
	 *  @param value2 the binary string representing the double value
	 *  @return the result value (if trap are disabled, special values are returned as binary string)
	 *  @throws FPInvalidOperationException,FPExponentTooLargeException,FPUnderflowException,FPOverflowException 
	 */
	public static String doubleSum(String value1, String value2) throws FPInvalidOperationException,FPExponentTooLargeException,FPUnderflowException,FPOverflowException
	{
		if(is64BinaryString(value1) && is64BinaryString(value2))
		{
			//if one or both of two operands are Not a value then the result is a nan
			//and if the trap is enabled an exception occurs, else a Qnan is returned
			if((isQNaN(value1) || isQNaN(value2)) || (isSNaN(value1)||isSNaN(value2) ))
			{
				if(cpu.getFPExceptions(CPU.FPExceptions.INVALID_OPERATION))
					throw new FPInvalidOperationException();
				return QNAN_NEW;
			}
			
		//(sign)Infinity - (sign)Infinity =NAN when signs agree ( but (sign)Infinity + (sign)Infinity = Infinity when signs agree ) (Status of IEEE754)

			//+infinity-infinity
			boolean cond1=isPositiveInfinity(value1) && isNegativeInfinity(value2);
			//-infinity+infinity
			boolean cond2=isNegativeInfinity(value1) && isPositiveInfinity(value2);
			if(cond1 || cond2)
			{
				if(cpu.getFPExceptions(CPU.FPExceptions.INVALID_OPERATION))
					throw new FPInvalidOperationException();
				return QNAN_NEW;
			}
			// infinity + infinity
			cond1 =isPositiveInfinity(value1) && isPositiveInfinity(value2);
			if(cond1)
				return PLUSINFINITY;
			//-infinity - infinity
			cond1 =isNegativeInfinity(value1) && isNegativeInfinity(value2);
			if(cond1)
				return MINUSINFINITY;
			
//RIDONDANTE
/*
		//any + (QNan/SNan)= QNan
			cond1=isQNaN(value1) || isQNaN(value2) || isSNaN(value1) || isSNaN(value2);
			if(cond1)
			{
				if(cpu.getFPExceptions(CPU.FPExceptions.INVALID_OPERATION))
					throw new FPInvalidOperationException();
				return QNAN_NEW;
			}
*/			
		//+/- Infinity + (any value, inclusive PLUSZERO and MINUSZERO, except QNan/Snan)=+/- Infinity	
			//in this point the (QNan/SNan) control is not necessary
			
			//+infinity + (any)
			cond1=isPositiveInfinity(value1) && !isInfinity(value2);
			if(cond1) return PLUSINFINITY;
			//-infinity + (any)
			cond1=isNegativeInfinity(value1) && !isInfinity(value2);
			if(cond1) return MINUSINFINITY;
			//(any) + +infinity
			cond1=!isInfinity(value1) && isPositiveInfinity(value2);
			if(cond1) return PLUSINFINITY;
			//(any) + -infinity
			cond1=!isInfinity(value1) && isNegativeInfinity(value2);
			if(cond1) return MINUSINFINITY;
			
			
			//at this point operands can be added and if an overflow or an underflow occurs
			//and if exceptions are activated then trap else results are returned
			MathContext mc=new MathContext(1000,RoundingMode.HALF_EVEN);
			BigDecimal operand1=null;
			BigDecimal operand2=null;
			try {
				operand1=new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value1,false)));
				operand2=new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value2,false)));
			}catch (IrregularStringOfBitsException ex) 
			{ex.printStackTrace();}
		
			BigDecimal result=operand1.add(operand2,mc);
		
			//checking for underflows or overflows are performed inside the doubleToBin method (if relative traps are disabled output is returned)
			String output=doubleToBin(result.toString());
			
			//if an underflow or overflow occur and they are activated (trap enabled) this point is never reached
			return output;
		}
		return null;
	}
	

	/** This method performs the subtraction between two double values, if  the passed values are Snan or Qnan
	 *  and the invalid operation exception is not enabled  the result of the operation is a Qnan else an InvalidOperation exception occurs,
	 *  if the passed values are infinities and their signs agree, an infinity (positive or negative is returned),
	 *  if signs don't agree then an invalid operation exception occurs if this trap is enabled.
	 *  After the addition, if the result is too large in absolute value a right signed infinity is returned, else
	 *  if the FP overflow or underflow are enabled an exception occurs.*/
	public static String doubleSubtraction(String value1, String value2) throws FPInvalidOperationException,FPExponentTooLargeException,FPUnderflowException,FPOverflowException
	{
		if(is64BinaryString(value1) && is64BinaryString(value2))
		{
			//if one or both of two operands are Not a Number then the result is a nan
			//and if the trap is enabled an exception occurs, else a Qnan is returned
			if((isQNaN(value1) || isQNaN(value2)) || (isSNaN(value1)||isSNaN(value2) ))
			{
				if(cpu.getFPExceptions(CPU.FPExceptions.INVALID_OPERATION))
					throw new FPInvalidOperationException();
				return QNAN_NEW;
			}
			
		//(sign)Infinity - (sign)Infinity = NAN   when signs agree (but (sign)Infinity - (sign)Infinity =Infinity   when signs don't agree ) (Status of IEEE754)

			// +infinity  -   +infinity
			boolean cond1 =isPositiveInfinity(value1) && isPositiveInfinity(value2);
			//-infinity   -   -infinity
			boolean cond2 =isNegativeInfinity(value1) && isNegativeInfinity(value2);
			
			
			if(cond1 || cond2)
			{
				if(cpu.getFPExceptions(CPU.FPExceptions.INVALID_OPERATION))
					throw new FPInvalidOperationException();
				return QNAN_NEW;
			}

			//+infinity   -   -infinity
			cond1=isPositiveInfinity(value1) && isNegativeInfinity(value2);
			if(cond1)
				return PLUSINFINITY;
			//-infinity   -   +infinity
			cond1=isNegativeInfinity(value1) && isPositiveInfinity(value2);
			if(cond1)
				return MINUSINFINITY;
			
			
			
//RIDONDANTE	 
/*			
		//any + (QNan/SNan)= QNan
			cond1=isQNaN(value1) || isQNaN(value2) || isSNaN(value1) || isSNaN(value2);
			if(cond1)
			{
				if(cpu.getFPExceptions(CPU.FPExceptions.INVALID_OPERATION))
					throw new FPInvalidOperationException();
				return QNAN_NEW;
			}
*/			
		//+/- Infinity + (any value, inclusive PLUSZERO and MINUSZERO, except QNan/Snan)=+/- Infinity	
			//in this point the (QNan/SNan) control is not necessary
			
			//+infinity - (any)
			cond1=isPositiveInfinity(value1) && !isInfinity(value2);
			if(cond1) return PLUSINFINITY;
			//-infinity - (any)
			cond1=isNegativeInfinity(value1) && !isInfinity(value2);
			if(cond1) return MINUSINFINITY;
			//(any) - +infinity
			cond1=!isInfinity(value1) && isPositiveInfinity(value2);
			if(cond1) return MINUSINFINITY;
			//(any) - -infinity
			cond1=!isInfinity(value1) && isNegativeInfinity(value2);
			if(cond1) return PLUSINFINITY;
			
			
			//at this point operands can be subtracted and if an overflow or an underflow occurs
			//and if exceptions are activated then a trap happens else results are returned
			MathContext mc=new MathContext(1000,RoundingMode.HALF_EVEN);
			BigDecimal operand1=null;
			BigDecimal operand2=null;
			try {
				operand1=new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value1,false)));
				operand2=new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value2,false)));
			}catch (IrregularStringOfBitsException ex) 
			{ex.printStackTrace();}
		
			BigDecimal result=operand1.subtract(operand2,mc);
		
			//checking for underflows or overflows are performed inside the doubleToBin method (if the relative traps are disabled the output is returned)
			String output=doubleToBin(result.toString());
			
			//if an underflow or overflow occur and they are activated (trap enabled) this point is never reached
			return output;
		}
		return null;
		
		
	}

	/** This method performs the multiplication between two double values, if  the passed values are Snan or Qnan
	 *  and the invalid operation exception is not enabled  the result of the operation is a Qnan else an InvalidOperation exception occurs,
	 *  if the passed values are infinities a positive or negative infinity is returned depending of the signs product,
	 *  Only if we attempt to perform (sign)0 X (sign)Infinity and the Invalid operation exception is not enabled NAN is returned,
	 *  else a trap occur. After the multiplication, if the result is too large in absolute value a right signed infinity is returned, else
	 *  if the FP overflow or underflow are enabled an exception occurs.*/
	public static String doubleMultiplication(String value1, String value2) throws FPInvalidOperationException,FPExponentTooLargeException,FPUnderflowException,FPOverflowException
	{
		if(is64BinaryString(value1) && is64BinaryString(value2))
		{
			//if one or both of two operands are Not a Number then the result is a nan
			//and if the exception is enabled a trap occurs, else a Qnan is returned
			if((isQNaN(value1) || isQNaN(value2)) || (isSNaN(value1)||isSNaN(value2) ))
			{
				if(cpu.getFPExceptions(CPU.FPExceptions.INVALID_OPERATION))
					throw new FPInvalidOperationException();
				return QNAN_NEW;
			}
			
			
			// (sign)Zero X (sign)Infinity
			boolean cond1 =isZero(value1) && isInfinity(value2);
			boolean cond2 =isInfinity(value1) && isZero(value2);
			if(cond1 || cond2)
			{
				if(cpu.getFPExceptions(CPU.FPExceptions.INVALID_OPERATION))
					throw new FPInvalidOperationException();
				return QNAN_NEW;
			}	
			
			//(sign)Infinity X (sign)Infinity
			if(isInfinity(value1) && isInfinity(value2))
			{
				int sign1=getInfinitySign(value1);
				int sign2=getInfinitySign(value2);
				int res_sign=sign1 * sign2;
				if(res_sign==-1)
					return MINUSINFINITY;
				else if(res_sign==1)
					return PLUSINFINITY;
			}
			//(sign)Infinity X any
			if(isInfinity(value1) && !isInfinity(value2))
			{
				if(isPositiveInfinity(value1))
					return PLUSINFINITY;
				else//isNegativeInfinity
					return MINUSINFINITY;
			}
			//any x (sign)Infinity
			if(!isInfinity(value1) && isInfinity(value2))
			{
				if(isPositiveInfinity(value2))
					return PLUSINFINITY;
				else//isNegativeInfinity
					return MINUSINFINITY;
			}
			
			//at this point operands can be multiplied and if an overflow or an underflow occurs
			//and if exceptions are activated then a trap happens else results are returned
			MathContext mc=new MathContext(1000,RoundingMode.HALF_EVEN);
			BigDecimal operand1=null;
			BigDecimal operand2=null;
			try {
				operand1=new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value1,false)));
				operand2=new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value2,false)));
			}catch (IrregularStringOfBitsException ex) 
			{ex.printStackTrace();}
		
			BigDecimal result=operand1.multiply(operand2,mc);
		
			//checking for underflows or overflows are performed inside the doubleToBin method (if the relative traps are disabled the output is returned)
			String output=doubleToBin(result.toString());
			
			//if an underflow or overflow occur and they are activated (trap enabled) this point is never reached
			return output;
		}
		return null;
		
		
		
	
	}
	
	
	/**Returns a string with a double value or the name of a special value
	  * it is recommended the use of this method only for the visualisation of the double value because it may return an alphanumeric value
	  * @param value the 64 bit binary string in the IEEE754 format to convert
	  * @return the double value or the special values "Quiet NaN","Signaling NaN", "Positive infinity", "Negative infinity","Positive zero","Negative zero"
	  */
	public static String binToDouble(String value)
	{
		if(is64BinaryString(value))
		{
			String new_value=getSpecialValues(value);
			//the value wasn't changed
			if(new_value.compareTo(value)==0)
			{
				Double new_value_d=null;
				try {
					new_value_d = Double.longBitsToDouble(Converter.binToLong(value, false));
				} catch (IrregularStringOfBitsException ex) {
					ex.printStackTrace();
				}
				return new_value_d.toString();
			}
			return new_value;
		}
		return null;
	}
	
	/** Returns the name of a special value (+-infinity, qnan, snan ) or "value" itself if it isn't a special value  */
	public static String getSpecialValues(String value)
	{
		if(isQNaN(value))
			return "Quiet NaN";
		else if(isSNaN(value))
			return "Signaling NaN";
		else if(isPositiveInfinity(value))
			return "Positive infinity";
		else if(isNegativeInfinity(value))
			return "Negative infinity";
		else if(isPositiveZero(value))
			return "Positive zero";
		else if(isNegativeZero(value))
			return "Negative zero";
		else return value;
	}
	
	/*Determines if the passed binary string is a Nan value, in other words if 
	 * it has got the QNAN_PATTERN
	 * @param value the binary string of 64 bits
	 * return true if the condition is true
	 */
	public static boolean isQNaN(String value)
	{
		//check if value is a binary string of 64 bits of type
		// X111111111110XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		final int CONSTANT_PART_INIT=1;
		final int CONSTANT_PART_END=13;
		final int VARIABLE_PART_INIT=13;
		final int VARIABLE_PART_END=64;
		final String PROHIBITE_FOR_VAR_PART="000000000000000000000000000000000000000000000000000";
		if(is64BinaryString(value))
		{
			
			boolean cond1=QNAN_PATTERN.substring(CONSTANT_PART_INIT,CONSTANT_PART_END).compareTo(value.substring(CONSTANT_PART_INIT,CONSTANT_PART_END))==0;
			boolean cond2=value.substring(VARIABLE_PART_INIT,VARIABLE_PART_END).compareTo(PROHIBITE_FOR_VAR_PART)==0; 
			if(cond1 && !cond2)
				return true;
			/*
			String stringa;
			if(value.matches("[01]111111111110[01]{51}") && value.substring(VARIABLE_PART_INIT,VARIABLE_PART_END).compareTo(PROHIBITE_FOR_VAR_PART)!=0)
				return true;
			*/
		}
		return false;
	}
	
	/*Determines if the passed binary string is an SNan value, in other words if 
	 * it has got the SNAN_PATTERN for MIPS64
	 * @param value the binary string of 64 bits
	 * return true if the condition is true
	 */
	public static boolean isSNaN(String value)
	{
		final int CONSTANT_PART_INIT=1;
		final int CONSTANT_PART_END=13;
		final int VARIABLE_PART_INIT=13;
		final int VARIABLE_PART_END=64;
		final String PROHIBITE_FOR_VAR_PART="000000000000000000000000000000000000000000000000000";
		if(is64BinaryString(value))
		{
			if(SNAN_PATTERN.substring(CONSTANT_PART_INIT,CONSTANT_PART_END).compareTo(value.substring(CONSTANT_PART_INIT,CONSTANT_PART_END))==0
			&& !(value.substring(VARIABLE_PART_INIT,VARIABLE_PART_END).compareTo(PROHIBITE_FOR_VAR_PART)==0))
			{
				return true;
			}	
		}
		return false;
		
	}
	
	/*Determines if the passed binary string is an infinity value according to the IEEE754 standard
	 * @param value the binary string of 64 bits
	 * @return true if the value is positive infinity
	 */
	public static boolean isPositiveInfinity(String value)
	{
		if(is64BinaryString(value))
		{
			if(value.compareTo(PLUSINFINITY)==0)
				return true;
		}
		return false;
	}

	/** Determines if value is a negative infinity according to the IEEE754 standard
	 * @param value the binary string of 64 bits
	 * @return true if the value is negative infinity
	 */
	public static boolean isNegativeInfinity(String value)
	{
		if(is64BinaryString(value))
		{
			if(value.compareTo(MINUSINFINITY)==0)
				return true;
		}
		return false;
	}
	
	/** Determines if value is an infinity according to the IEEE754 standard
	 * @param value the binary string of 64 bits
	 * @return true if the value is  infinity
	 */
	public static boolean isInfinity(String value)
	{
		if(is64BinaryString(value))
		{
			if(isPositiveInfinity(value) || isNegativeInfinity(value))
				return true;
		}
		return false;
	}
	
	/** Returns -1 if the infinity sign is negative, 1 if it is positive, 0 if "value" is not a well formed infinity according IEEE754 for FP64*/
	public static int getInfinitySign(String value)
	{
		if(is64BinaryString(value))
		{
			if(isPositiveInfinity(value))
				return 1;
			else if(isNegativeInfinity(value))
				return -1;
		}
		return 0;
	}
	
	/** Determines if value is a positive zero according to the IEEE754 standard
	 * @param value the binary string of 64 bits
	 * @return true if the value is  positive zero
	 */

	public static boolean isPositiveZero(String value)
	{
		if(is64BinaryString(value))
		{
			if(value.compareTo(PLUSZERO)==0)
				return true;
		}
		return false;
	}
	
	/*Determines if the passed binary string is a  negative zero
	 * @param value the binary string of 64 bits
	 * @return true if the value is a positive zero
	 */
	public static boolean isNegativeZero(String value)
	{
		if(is64BinaryString(value))
		{
			if(value.compareTo(MINUSZERO)==0)
				return true;
		}
		return false;
	}
	
	/** Determines if value is a zero according to the IEEE754 standard
	 * @param value the binary string of 64 bits
	 * @return true if the value is  infinity
	 */
	public static boolean isZero(String value)
	{
		if(is64BinaryString(value))
		{
			if(isPositiveZero(value) || isNegativeZero(value))
				return true;
		}
		return false;
		
	}

	
	/** Determines if the passed value is a binary string of 64 bits
	 *  @param value the binary string
	 *  @return a boolean value*/
	public static boolean is64BinaryString(String value)
	{
		if(value.length()==64 && value.matches("[01]{64}"))
			return true;
		return false;
	}
	
	
	
	
	 
	
	
	/*
	public static String doubleToHex(double value)
	{
		return Long.toHexString(Double.doubleToLongBits(value));
	}
	*/


   
}




	

