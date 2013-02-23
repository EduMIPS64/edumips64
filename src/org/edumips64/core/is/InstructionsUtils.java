/*
 * InstructionsUtils.java
 *
 * 15th may 2006
 * Utils for the MIPS64 Instruction Set
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

package org.edumips64.core.is;
import org.edumips64.utils.*;

public class InstructionsUtils {

  /** Performs 2's complement addition between two binary values passed as strings
    *  of the same length
    *  @param r1 first binary value
    *  @param r2 second binary value
    *  @throws TwosComplementSumException
    *  @return result of two's complement addition as string
    */
  public static String twosComplementSum(String r1, String r2) throws TwosComplementSumException {
    //R = !A !B C + !A B !C + A B C + A !B !C
    //C = A B !C + A !B C + B C
    //controlling correctess of parameters
    if (r1.length() != r2.length()) {
      throw new TwosComplementSumException();
    }

    //performing 2's complement addition
    boolean a, b, carry, result;
    StringBuffer outputstring = new StringBuffer();

    carry = false; //riporto iniziale

    for (int i = r1.length() - 1; i > -1; i--) {
      a = (r1.charAt(i) == '1') ? true : false;
      b = (r2.charAt(i) == '1') ? true : false;

      result = (!a && !b && carry) || (!a && b && !carry) || (a && b && carry) || (a && !b && !carry);
      carry = (b && carry) || (a && b && !carry) || (a && !b && carry);

      //appending outputstring to the left with resulting bit
      outputstring.insert(0, result ? '1' : '0');
    }

    return outputstring.substring(0);

  }


  /** Performs 2's complement substraction between two binary values passed as strings
    *  of 64 characters in length
    *  @param r1 first binary value from which r2 is substracted
    *  @param r2 second binary value to substract to r1
    *  @return result of two's complement substraction as string
    */
  public static String twosComplementSubstraction(String r1, String r2) throws IrregularStringOfBitsException, TwosComplementSumException {
    //calculating two-complement of r2
    String r2_compl = Converter.twoComplement(r2);
    //performing sum between r1 and r2_compl
    String outputstring = twosComplementSum(r1, r2_compl);
    return outputstring;
  }

  /** Performs a bitwise XOR between passed strings
   *  @param r1 First string
   *  @param r2 Second string
   */

  public static String xorOperation(String r1, String r2) {
    String result = "";

    for (int i = 0; i < 64; i++) {
      if ((r1.charAt(i) == '1') && (r2.charAt(i) == '1')) {
        result += '0';
      } else if ((r1.charAt(i) == '1') && (r2.charAt(i) == '0')) {
        result += '1';
      } else if ((r1.charAt(i) == '0') && (r2.charAt(i) == '1')) {
        result += '1';
      } else {
        result += '0';
      }
    }

    return result;
  }


}

