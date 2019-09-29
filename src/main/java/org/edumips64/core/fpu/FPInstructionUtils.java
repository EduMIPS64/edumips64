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
package org.edumips64.core.fpu;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import org.edumips64.core.FCSRRegister;
import org.edumips64.core.Converter;
import org.edumips64.core.IrregularStringOfBitsException;

/** Group of functions used in the Floating point unit
 */
public class FPInstructionUtils {
  private FCSRRegister fcsr;

  /** The fcsr parameter is used to get/set flags relative to the state of FPU exceptions.
   *
   * Classes that don't need to work with the "real" state of the CPU (for example, Parser or unit tests), can just pass
   * any instance of FCSRRegister to this constructor.
   */
  public FPInstructionUtils(FCSRRegister fcsr) {
    this.fcsr = fcsr;
  }
  private final static String PLUSINFINITY = "0111111111110000000000000000000000000000000000000000000000000000";
  private final static String MINUSINFINITY = "1111111111110000000000000000000000000000000000000000000000000000";
  private final static String PLUSZERO = "0000000000000000000000000000000000000000000000000000000000000000";
  private final static String MINUSZERO = "1000000000000000000000000000000000000000000000000000000000000000";
  private final static String BIGGEST = "1.797693134862315708145274237317E308";
  private final static String SMALLEST = "-1.797693134862315708145274237317E308";
  private final static String MINUSZERO_DEC = "-4.9406564584124654417656879286822E-324";
  private final static String PLUSZERO_DEC = "4.9406564584124654417656879286822E-324";

  /*
  snan
  0x7fffffffffffffff (value used in MIPS64 to generate a new snan)
  0 11111111111 1111111111111111111111111111111111111111111111111111 (bynary equivalent)
  X 11111111111 1XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX (pattern for snans values)*/
  private final static String SNAN_NEW = "0111111111111111111111111111111111111111111111111111111111111111";

  /*qnan
  0x7ff7ffffffffffff (value used in MIPS64 to generate a new qnan)
  0 11111111111 0111111111111111111111111111111111111111111111111111 (bynary equivalent)
  X 11111111111 0XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX (pattern for qnans values) */
  private final static String QNAN_NEW = "0111111111110111111111111111111111111111111111111111111111111111";
  private final static String QNAN_PATTERN = "X111111111110XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"; //XX..XX cannot be equal to zero at the same time


  /**
   * Converts a double value passed as string to a 64 bit binary string according with IEEE754 standard for double precision floating point numbers
   *
   * @param value the double value in the format "123.213" or "1.23213E2"
   *              value belongs to [-1.797693134862315708145274237317E308,-4.9406564584124654417656879286822E-324] U [4.9406564584124654417656879286822E-324, 1.797693134862315708145274237317E308]
   * @return the binary string
   * @throws FPOverflowException,FPUnderflowException,IrregularStringOfBitsException
   */
  public String doubleToBin(String value) throws FPOverflowException, FPUnderflowException, IrregularStringOfBitsException {
    //if a special value is passed then the proper binary string is returned
    String old_value = value;
    value = parseKeywords(value);

    if (old_value.compareToIgnoreCase(value) != 0) {
      return value;
    }

    // Check if the value can be treated as a valid double.
    try {
      Double.parseDouble(value);
    } catch (NumberFormatException e) {
      throw new IrregularStringOfBitsException();
    }

    // Constants to be used for the comparisons.
    final BigDecimal theBiggest = new BigDecimal(BIGGEST);
    final BigDecimal theSmallest = new BigDecimal(SMALLEST);
    final BigDecimal theZeroMinus = new BigDecimal(MINUSZERO_DEC);
    final BigDecimal theZeroPlus = new BigDecimal(PLUSZERO_DEC);
    final BigDecimal zero = new BigDecimal(0.0);
    final BigDecimal minuszero = new BigDecimal(-0.0);

    try { //Check if the exponent is not in signed 32 bit, in this case the NumberFormatException occurs
      BigDecimal value_bd = new BigDecimal(value);

      // Check for overflow.
      if (value_bd.compareTo(theBiggest) == 1 || value_bd.compareTo(theSmallest) == -1) {
        fcsr.setFlagsOrRaiseException(FCSRRegister.FPExceptions.OVERFLOW);
        if (value_bd.compareTo(theBiggest) == 1) {
          return PLUSINFINITY;
        }

        if (value_bd.compareTo(theSmallest) == -1) {
          return MINUSINFINITY;
        }
      }

      // Check for underflow.
      if ((value_bd.compareTo(theZeroMinus) == 1 && value_bd.compareTo(theZeroPlus) == -1) && (value_bd.compareTo(zero) != 0 && value_bd.compareTo(minuszero) != 0)) {
        fcsr.setFlagsOrRaiseException(FCSRRegister.FPExceptions.UNDERFLOW);

        if (value_bd.compareTo(zero) == 1) {
          return PLUSZERO;
        }

        if (value_bd.compareTo(zero) == -1) {
          return MINUSZERO;
        }
      }

      String output = Long.toBinaryString(Double.doubleToLongBits(value_bd.doubleValue()));
      return padding64(output);

    } catch (NumberFormatException e) {
      if (fcsr.getFPExceptions(FCSRRegister.FPExceptions.OVERFLOW)) {
        fcsr.setFCSRCause("O", 1);
        throw new FPOverflowException();
      } else {
        fcsr.setFCSRFlags("V", 1);
      }
      return PLUSZERO;
    } catch (FPDivideByZeroException | FPInvalidOperationException e) {
      // Can't really happen.
      e.printStackTrace();
      return "";
    }
  }

  /**
   * determines if the passed string contains the keywords for special values
   *
   * @param value a binary string or a string containing POSITIVEINFINITY,NEGATIVEINFINITY,POSITIVEZERO,NEGATIVEZERO,QNAN,SNAN
   * @return the proper binary string if value contains special values, or value itself if the string is not special
   */
  private static String parseKeywords(String value) {
    if (value.compareToIgnoreCase("POSITIVEINFINITY") == 0) {
      return PLUSINFINITY;
    } else if (value.compareToIgnoreCase("NEGATIVEINFINITY") == 0) {
      return MINUSINFINITY;
    } else if (value.compareToIgnoreCase("POSITIVEZERO") == 0) {
      return PLUSZERO;
    } else if (value.compareToIgnoreCase("NEGATIVEZERO") == 0) {
      return MINUSZERO;
    } else if (value.compareToIgnoreCase("QNAN") == 0) {
      return QNAN_NEW;
    } else if (value.compareToIgnoreCase("SNAN") == 0) {
      return SNAN_NEW;
    }

    return value;
  }

  /**
   * In order to create a 64 bit binary string, the zero-padding on the left of the value is carried out
   *
   * @param value the string to pad
   * @return Padded string
   */
   private static String padding64(String value) {
    StringBuilder sb = new StringBuilder();
    sb.append(value);

    for (int i = 0; i < 64 - value.length(); i++) {
      sb.insert(0, "0");
    }

    return sb.toString();
  }

  /**
   * This method performs the sum between two double values, if  the passed values are Snan or Qnan
   * and the invalid operation exception is not enabled  the result of the operation is a Qnan  else an InvalidOperation exception occurs,
   * if the passed values are infinities and their signs agree, an infinity (positive or negative is returned),
   * if signs don't agree then an invalid operation exception occurs if this trap is enabled.
   * After the addition, if the result is too large in absolute value a right signed infinity is returned, else
   * if the FP overflow or underflow are enabled an exception occurs.
   *
   * @param value1 the binary string representing the double value
   * @param value2 the binary string representing the double value
   * @return the result value (if trap are disabled, special values are returned as binary string)
   * @throws FPInvalidOperationException,FPUnderflowException,FPOverflowException
   */
  public String doubleSum(String value1, String value2) throws FPInvalidOperationException, FPUnderflowException, FPOverflowException, IrregularStringOfBitsException {
    if (is64BinaryString(value1) && is64BinaryString(value2)) {
      // QNaN check:
      // 1. any NaN
      boolean isNan = isQNaN(value1) || isQNaN(value2) || isSNaN(value1) || isSNaN(value2);
      // 2. signs that make it impossible to determine the final result
      boolean wrongOperands = isPositiveInfinity(value1) && isNegativeInfinity(value2);
      wrongOperands = wrongOperands || (isNegativeInfinity(value1) && isPositiveInfinity(value2));
      if (isNan || wrongOperands) {
        try {
          fcsr.setFlagsOrRaiseException(FCSRRegister.FPExceptions.INVALID_OPERATION);
        } catch (FPDivideByZeroException e) {
          // Should never happen.
          e.printStackTrace();
        }
        return QNAN_NEW;
      }

      // Return +Inf if:
      // 1. +Inf + Inf
      boolean plusInf = isPositiveInfinity(value1) && isPositiveInfinity(value2);
      // 2. +Inf + (any)
      plusInf =  plusInf || (isPositiveInfinity(value1) && !isInfinity(value2));
      // 3. (any) + +Inf
      plusInf = plusInf || !isInfinity(value1) && isPositiveInfinity(value2);
      if (plusInf) {
        return PLUSINFINITY;
      }

      // Return -Inf if:
      // 1. -Inf + -Inf
      boolean minusInf = isNegativeInfinity(value1) && isNegativeInfinity(value2);
      // 2. -Inf + (any)
      minusInf =  minusInf || (isNegativeInfinity(value1) && !isInfinity(value2));
      // 3. (any) + -Inf
      minusInf = minusInf || !isInfinity(value1) && isNegativeInfinity(value2);
      if (minusInf) {
        return MINUSINFINITY;
      }

      // At this point operands can be added and if an overflow or an underflow occurs
      // and if exceptions are activated then trap else results are returned
      MathContext mc = new MathContext(1000, RoundingMode.HALF_EVEN);
      BigDecimal operand1 = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value1, false)));
      BigDecimal operand2 = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value2, false)));
      BigDecimal result = operand1.add(operand2, mc);

      //checking for underflows or overflows are performed inside the doubleToBin method (if relative traps are disabled output is returned)
      //if an underflow or overflow occur and they are activated (trap enabled) this point is never reached
      return doubleToBin(result.toString());
    }

    return null;
  }


  /**
   * This method performs the subtraction between two double values, if  the passed values are Snan or Qnan
   * and the invalid operation exception is not enabled  the result of the operation is a Qnan else an InvalidOperation exception occurs,
   * if the passed values are infinities and their signs agree, an infinity (positive or negative is returned),
   * if signs don't agree then an invalid operation exception occurs if this trap is enabled.
   * After the addition, if the result is too large in absolute value a right signed infinity is returned, else
   * if the FP overflow or underflow are enabled an exception occurs.
   */
  public String doubleSubtraction(String value1, String value2) throws FPInvalidOperationException, FPUnderflowException, FPOverflowException, IrregularStringOfBitsException {
    if (!(is64BinaryString(value1) && is64BinaryString(value2))) {
      return null;
    }
    // QNaN check:
    // 1. any NaN
    boolean isNan = isQNaN(value1) || isQNaN(value2) || isSNaN(value1) || isSNaN(value2);
    // 2. signs that make it impossible to determine the final result
    boolean wrongOperands = isPositiveInfinity(value1) && isPositiveInfinity(value2);
    wrongOperands = wrongOperands || (isNegativeInfinity(value1) && isNegativeInfinity(value2));
    if (isNan || wrongOperands) {
      try {
        fcsr.setFlagsOrRaiseException(FCSRRegister.FPExceptions.INVALID_OPERATION);
      } catch (FPDivideByZeroException e) {
        // Should never happen.
        e.printStackTrace();
      }
      return QNAN_NEW;
    }

    // Return +Inf if:
    // 1. +Inf - -Inf
    boolean plusInf = isPositiveInfinity(value1) && isNegativeInfinity(value2);
    // 2. +Inf - (any)
    plusInf =  plusInf || (isPositiveInfinity(value1) && !isInfinity(value2));
    // 3. (any) - -Inf
    plusInf = plusInf || !isInfinity(value1) && isNegativeInfinity(value2);
    if (plusInf) {
      return PLUSINFINITY;
    }

    // Return -Inf if:
    // 1. -Inf - +Inf
    boolean minusInf = isNegativeInfinity(value1) && isPositiveInfinity(value2);
    // 2. -Inf - (any)
    minusInf =  minusInf || (isNegativeInfinity(value1) && !isInfinity(value2));
    // 3. (any) - +Inf
    minusInf = minusInf || !isInfinity(value1) && isPositiveInfinity(value2);
    if (minusInf) {
      return MINUSINFINITY;
    }

    //at this point operands can be subtracted and if an overflow or an underflow occurs
    //and if exceptions are activated then a trap happens else results are returned
    MathContext mc = new MathContext(1000, RoundingMode.HALF_EVEN);
    BigDecimal operand1 = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value1, false)));
    BigDecimal operand2 = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value2, false)));

    BigDecimal result = operand1.subtract(operand2, mc);
    //checking for underflows or overflows are performed inside the doubleToBin method (if the relative traps are disabled the output is returned)
    //if an underflow or overflow occur and they are activated (trap enabled) this point is never reached
    return doubleToBin(result.toString());
  }

  /**
   * This method performs the multiplication between two double values, if  the passed values are Snan or Qnan
   * and the invalid operation exception is not enabled  the result of the operation is a Qnan else an InvalidOperation exception occurs,
   * if the passed values are infinities a positive or negative infinity is returned depending of the signs product,
   * Only if we attempt to perform (sign)0 X (sign)Infinity and the Invalid operation exception is not enabled NAN is returned,
   * else a trap occur. After the multiplication, if the result is too large in absolute value a right signed infinity is returned, else
   * if the FP overflow or underflow are enabled an exception occurs.
   */
  public String doubleMultiplication(String value1, String value2) throws FPInvalidOperationException, FPUnderflowException, FPOverflowException, IrregularStringOfBitsException {
    if (!(is64BinaryString(value1) && is64BinaryString(value2))) {
      return null;
    }
    // QNaN check:
    // 1. any NaN
    boolean isNan = isQNaN(value1) || isQNaN(value2) || isSNaN(value1) || isSNaN(value2);
    // 2. zero x Inf or Inf x zero
    boolean wrongOperands = isZero(value1) && isInfinity(value2);
    wrongOperands = wrongOperands || (isInfinity(value1) && isZero(value2));
    if (isNan || wrongOperands) {
      try {
        fcsr.setFlagsOrRaiseException(FCSRRegister.FPExceptions.INVALID_OPERATION);
      } catch (FPDivideByZeroException e) {
        // Should never happen.
        e.printStackTrace();
      }
      return QNAN_NEW;
    }

    // Get signs of value1 and value2, and the resulting sign.
    int sign1 = getDoubleSign(value1);
    int sign2 = getDoubleSign(value2);
    int res_sign = sign1 * sign2;

    // Return a signed infinity when infinity is involved. Zeros and NaNs won't be
    // operands because of the previous checks.
    boolean returnInf = isInfinity(value1) && isInfinity(value2);
    returnInf = returnInf || (isInfinity(value1) && !isInfinity(value2));
    // any X (sign)Infinity
    returnInf = returnInf || (!isInfinity(value1) && isInfinity(value2));
    //(sign)Infinity X (sign)Infinity
    if (returnInf) {
      return getSignedInfinity(res_sign);
    }

    //(sign)zero X (sign)zero
    if (isZero(value1) && isZero(value2)) {
      return getSignedZero(res_sign);
    }

    //at this point operands can be multiplied and if an overflow or an underflow occurs
    //and if exceptions are activated then a trap happens else results are returned
    MathContext mc = new MathContext(1000, RoundingMode.HALF_EVEN);
    BigDecimal operand1 = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value1, false)));
    BigDecimal operand2 = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value2, false)));

    BigDecimal result = operand1.multiply(operand2, mc);

    //checking for underflows or overflows are performed inside the doubleToBin method (if the relative traps are disabled the output is returned)
    //if an underflow or overflow occur and they are activated (trap enabled) this point is never reached
    return doubleToBin(result.toString());
  }

  private static String getSignedInfinity(int sign) {
    if (sign == -1) {
      return MINUSINFINITY;
    }
    return PLUSINFINITY;
  }

  private static String getSignedZero(int sign) {
    if (sign == -1) {
      return MINUSZERO;
    }
    return PLUSZERO;
  }

  /**
   * This method performs the division between two double values, if  the passed values are Snan or Qnan
   * and the invalid operation exception is not enabled  the result of the operation is a Qnan else an InvalidOperation exception occurs,
   * Only if the passed values are  both infinities or zeros a Qnan is returned if  the InvalidOperation exception is not enabled else a trap occurs,
   * If value2 (not also value1) is Zero a DivisionByZero Exception occurs if it is enabled else a right infinity is returned depending on the product's signs
   * After the operation, if the result is too small in absolute value a right signed infinity is returned, else
   * if the FP underflow is enabled an exception occurs.
   */
  public String doubleDivision(String value1, String value2) throws FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, IrregularStringOfBitsException {
    if (!(is64BinaryString(value1) && is64BinaryString(value2))) {
      return null;
    }
    // QNaN check:
    // 1. any NaN
    boolean isNan = isQNaN(value1) || isQNaN(value2) || isSNaN(value1) || isSNaN(value2);
    // 2. Inf / Inf or zero / zero
    boolean wrongOperands = isInfinity(value1) && isInfinity(value2);
    wrongOperands = wrongOperands || (isZero(value1) && isZero(value2));
    if (isNan || wrongOperands) {
      fcsr.setFlagsOrRaiseException(FCSRRegister.FPExceptions.INVALID_OPERATION);
      return QNAN_NEW;
    }

    // Get signs of value1 and value2, and the resulting sign.
    int sign1 = getDoubleSign(value1);
    int sign2 = getDoubleSign(value2);
    int res_sign = sign1 * sign2;

    // (sign)Zero / any
    if (isZero(value1) && !isZero(value2)) {
      return getSignedZero(res_sign);
    }

    if (!isZero(value1) && isZero(value2)) {
      fcsr.setFlagsOrRaiseException(FCSRRegister.FPExceptions.DIVIDE_BY_ZERO);
      return getSignedInfinity(res_sign);
    }

    // (sign)infinity / any(different from infinity and zero)
    if (isInfinity(value1)) {
      return getSignedInfinity(res_sign);
    }

    //at this point operands can be divided and if an  underflow occurs
    //and if exceptions are activated then a trap happens else results are returned
    MathContext mc = new MathContext(1000, RoundingMode.HALF_EVEN);
    BigDecimal operand1 = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value1, false)));
    BigDecimal operand2 = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value2, false)));

    BigDecimal result = operand1.divide(operand2, mc);

    //checking for underflows is performed inside the doubleToBin method (if the relative traps are disabled the output is returned)
    return doubleToBin(result.toString());
  }

  /**
   * Returns a string with a double value or the name of a special value
   * it is recommended the use of this method only for the visualisation of the double value because it may return an alphanumeric value
   *
   * @param value the 64 bit binary string in the IEEE754 format to convert
   * @return the double value or the special values "Quiet NaN","Signaling NaN", "Positive infinity", "Negative infinity","Positive zero","Negative zero"
   */
  public static String binToDouble(String value) {
    if (is64BinaryString(value)) {
      String new_value = getSpecialValues(value);

      //the value wasn't changed
      if (new_value.compareTo(value) == 0) {
        Double new_value_d = null;

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

  /**
   * Returns the name of a special value (+-infinity, qnan, snan ) or "value" itself if it isn't a special value
   */
  private static String getSpecialValues(String value) {
    if (isQNaN(value)) {
      return "Quiet NaN";
    } else if (isSNaN(value)) {
      return "Signaling NaN";
    } else if (isPositiveInfinity(value)) {
      return "Positive infinity";
    } else if (isNegativeInfinity(value)) {
      return "Negative infinity";
    } else if (isPositiveZero(value)) {
      return "Positive zero";
    } else if (isNegativeZero(value)) {
      return "Negative zero";
    } else {
      return value;
    }
  }

  /*Determines if the passed binary string is a Nan value, in other words if
   * it has got the QNAN_PATTERN
   * @param value the binary string of 64 bits
   * return true if the condition is true
   */
  public static boolean isQNaN(String value) {
    return value.matches("[01]111111111110[01]{51}") && !value.matches("[01]111111111110[0]{51}");
  }

  /*Determines if the passed binary string is an SNan value, in other words if
   * it has got the SNAN_PATTERN for MIPS64
   * @param value the binary string of 64 bits
   * return true if the condition is true
   */
  public static boolean isSNaN(String value) {
    return value.matches("[01]111111111111[01]{51}") && !value.matches("[01]111111111110[0]{51}");
  }

  /*Determines if the passed binary string is an infinity value according to the IEEE754 standard
   * @param value the binary string of 64 bits
   * @return true if the value is positive infinity
   */
  private static boolean isPositiveInfinity(String value) {
    if (is64BinaryString(value)) {
      if (value.compareTo(PLUSINFINITY) == 0) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines if value is a negative infinity according to the IEEE754 standard
   *
   * @param value the binary string of 64 bits
   * @return true if the value is negative infinity
   */
  private static boolean isNegativeInfinity(String value) {
    if (is64BinaryString(value)) {
      if (value.compareTo(MINUSINFINITY) == 0) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines if value is an infinity according to the IEEE754 standard
   *
   * @param value the binary string of 64 bits
   * @return true if the value is  infinity
   */
  private static boolean isInfinity(String value) {
    if (is64BinaryString(value)) {
      if (isPositiveInfinity(value) || isNegativeInfinity(value)) {
        return true;
      }
    }

    return false;
  }


  /**
   * Returns -1 if "value" is a negative double binary string,+1 if it is positive, 0 if "value" is not a well formed 64 binary string according to IEEE754 standard
   */
  private static int getDoubleSign(String value) {
    if (is64BinaryString(value)) {
      switch (value.charAt(0)) {
        case '0':
          return 1;
        case '1':
          return -1;
      }
    }

    return 0;
  }

  /**
   * Determines if value is a positive zero according to the IEEE754 standard
   *
   * @param value the binary string of 64 bits
   * @return true if the value is  positive zero
   */

  private static boolean isPositiveZero(String value) {
    if (is64BinaryString(value)) {
      if (value.compareTo(PLUSZERO) == 0) {
        return true;
      }
    }

    return false;
  }

  /*Determines if the passed binary string is a  negative zero
   * @param value the binary string of 64 bits
   * @return true if the value is a positive zero
   */
  private static boolean isNegativeZero(String value) {
    if (is64BinaryString(value)) {
      if (value.compareTo(MINUSZERO) == 0) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines if value is a zero according to the IEEE754 standard
   *
   * @param value the binary string of 64 bits
   * @return true if the value is  infinity
   */
  private static boolean isZero(String value) {
    if (is64BinaryString(value)) {
      if (isPositiveZero(value) || isNegativeZero(value)) {
        return true;
      }
    }

    return false;

  }


  /**
   * Determines if the passed value is a binary string of 64 bits
   *
   * @param value the binary string
   * @return a boolean value
   */
  private static boolean is64BinaryString(String value) {
    return value.length() == 64 && value.matches("[01]{64}");
  }

  /**
   * Returns the long fixed point format with the passed rounding mode, or null if an XNan or Infinity is passed to this function
   *
   * @param value a binary string representing a double value according to the IEEE754 standard
   * @param rm    the rounding mode to use for the conversion
   **/
  public static BigInteger doubleToBigInteger(String value, FCSRRegister.FPRoundingMode rm) throws IrregularStringOfBitsException {
    //we have to check if a XNan o Infinity was passed to this function
    if (isQNaN(value) || isSNaN(value) || isInfinity(value) || !is64BinaryString(value)) {
      return null;
    }

    final int INT_PART = 0;
    final int DEC_PART = 1;

    BigDecimal bd = new BigDecimal(Double.longBitsToDouble(Converter.binToLong(value, false)));
    String plainValue = bd.toPlainString();
    //removing the sign
    plainValue = plainValue.replaceFirst("-", "");

    //if the decimal part contains only zeros we must remove it
    if (plainValue.matches("[0123456789]+.[0]+")) {
      plainValue = plainValue.substring(0, plainValue.indexOf("."));
    }

    //we now split the integer part and the decimal one
    String[] splittedParts = plainValue.split("\\.");

    long int_part_value = Long.valueOf(splittedParts[INT_PART]);

    //if the decimal part of the plain value exists, we must round to the passed rounding mode
    if (splittedParts.length == 2)
      switch (rm) {
        case TO_NEAREST:

          //ex. 1.6-->2   1.8-->2
          if (splittedParts[DEC_PART].matches("[6789][0123456789]*")) {
            int_part_value++;
          }
          //1.5-->2   2.5-->2(we must round to the nearest even)
          else if (splittedParts[DEC_PART].matches("[5][0123456789]*"))
            if (splittedParts[INT_PART].matches("[0123456789]*[13579]")) {
              int_part_value++;
            }

          break;

        case TOWARD_ZERO:
          //it is a truncation +-4.X -->+-4
          break;
        case TOWARDS_PLUS_INFINITY:
          if (bd.doubleValue() > 0) {
            int_part_value++;
          }

          break;
        case TOWARDS_MINUS_INFINITY:
          if (bd.doubleValue() < 0) {
            int_part_value++;
          }
          break;
      }

    if (bd.doubleValue() < 0) {
      int_part_value *= (-1);
    }

    return new BigInteger(String.valueOf(int_part_value));
  }

  /**
   * Returns the double value of the 64 bit fixed point number , or null if an XNan or Infinity is passed to this function
   *
   * @param value a binary string representing a long value
   **/
  public static BigDecimal longToDouble(String value) throws IrregularStringOfBitsException {
    //we have to check if a XNan o Infinity was passed to this function
    if (isQNaN(value) || isSNaN(value) || isInfinity(value) || !is64BinaryString(value)) {
      return null;
    }

    long toConvertValue = Converter.binToLong(value, false);
    return new BigDecimal(toConvertValue);
  }

  /**
   * Returns the double value of the 64 bit fixed point number, or null if an  if an XNan or Infinity is passed to this function
   **/
  public static BigDecimal intToDouble(String value) throws IrregularStringOfBitsException {
    //we have to check if a XNan o Infinity was passed to this function
    if (isQNaN(value) || isSNaN(value) || isInfinity(value) || !is64BinaryString(value)) {
      return null;
    }

    long toConvertValue = Converter.binToInt(value.substring(32, value.length()), false);
    return new BigDecimal(toConvertValue);
  }
}
