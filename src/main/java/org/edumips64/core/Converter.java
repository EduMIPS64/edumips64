/* Converter.java
 *
 * This class provides a set of static method for numeric conversion.
 * (c) 2006 Salvatore Scellato
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
package org.edumips64.core;

/** This class provides a set of static method for numeric conversion.
 * NOTE: bit strings will be considered little-endian, that is to say that the right-most bit is the less significant one: this
 * assumption is implicitely made by each method of this class.
 * @author Salvatore Scellato
 * */
public class Converter {

  private static final int OVERFLOW_32 = -0x80000000;
  private static final long OVERFLOW_64 = -0x8000000000000000L;

  /** Converts a string of bits in the relative value.
   * @param bits string of bits, must be composed obly by '0' and '1' chars, otherwise
   * an exception will be thrown. If the bit string is coded into a signed representation a negative number will be produced
   * if the leftmost bit is '1' and a positive number if it is '0'.
   * @param unsignd if set to true the conversion will be unsigned, if set to false the binary representation will be considered
   * of a signed value.
   * @return int value of the bits representation (32 bit)
   * @throws IrregularStringOfBitsException if the string of bits is not well-formed or the string of bits contains
   * a value that cannot be stored into an <code>int</code> variable
   */

  public static int binToInt(String bits, boolean unsignd) throws IrregularStringOfBitsException {

    //check for string irregularities to begin with
    if ((bits.length() > 32) || (unsignd && bits.length() == 32 && bits.charAt(0) == '1') || !isBinaryString(bits)) {
      throw new IrregularStringOfBitsException();
    }

    //se la stringa di bits Ãš lunga 32 bit
    //ed Ãš composta da uno 1 e 31 0
    //allora la conversione si deve fare a mano
    //perchÃš il numero da ritornare Ãš -(2^32)
    //e non si puÃ² utilizzare il valore positivo (2^32)
    //con il tipo int :-(
    if (!unsignd && bits.length() == 32 && isOverflow(bits)) {
      return OVERFLOW_32;
    }

    if (unsignd) {
      return getUnsignedIntValue(bits);
    } else {
      if (bits.charAt(0) == '0') {
        return Converter.binToInt(bits.substring(1), true);
      } else{
        String s = Converter.twoComplement(bits);
        return -1 * Converter.binToInt(s, true);
      }
    }
  }

  /** Converts a string of bits in the relative value.
   * @param bits string of bits, must be composed obly by '0' and '1' chars, otherwise
   * an exception will be thrown. If the bit string is coded into a signed representation a negative number will be produced
   * if the leftmost bit is '1' and a positive number if it is '0'.
   * @param unsignd if set to true the conversion will be unsigned, if set to false the binary representation will be considered
   * of a signed value.
   * @return long value of the bits representation (64 bit)
   * @throws IrregularStringOfBitsException if the string of bits is not well-formed or the string of bits contains
   * a value that cannot be stored into a <code>long</code> variable
   */
  public static long binToLong(String bits, boolean unsignd) throws IrregularStringOfBitsException {

    //check for string irregularities to begin with
    if ((bits.length() > 64) || (unsignd && bits.length() == 64 && bits.charAt(0) == '1') || !isBinaryString(bits)) {
      throw new IrregularStringOfBitsException();
    }

    //se la stringa di bits Ãš lunga 64 bit
    //ed Ãš composta da uno 1 e 63 0
    //allora la conversione si deve fare a mano
    //perchÃš il numero da ritornare Ãš -(2^63)
    //e non si puÃ² utilizzare il valore positivo (2^63)
    //con il tipo long :-(
    if (!unsignd && bits.length() == 64 && isOverflow(bits)) {
        return OVERFLOW_64;
    }

    if (bits.length() == 0) {
      return 0;
    }

    if (unsignd) {
      return getUnsignedLongValue(bits);
    } else {
      if (bits.charAt(0) == '0') {
        return Converter.binToLong(bits.substring(1), true);
      } else {
        String s = Converter.twoComplement(bits);
        return -1 * Converter.binToLong(s, true);
      }
    }
  }

  /** Converts an integer positive value in the relative string of bits.
   * @param value positive integer to be converted into the relative string of bits: if it is negative,
   * the opposite integer will be converted.
   * @return string of bits representation of the number
   */
  public static String positiveIntToBin(long value) {
    StringBuffer buf = new StringBuffer();
    long divide = (value < 0 ? -value : value);
    int rem = 0;

    while (divide > 0) {
      rem = (int)(divide % 2);

      if (rem == 1) {
        buf.insert(0, '1');
      } else {
        buf.insert(0, '0');
      }

      divide = divide / 2;
    }

    return new String(buf);
  }

  /** Converts an integer positive value in the relative string of bits with length nbit, doing a zero-padding
   * if the value string is shorter than nbit..
   * @param nbit length of the resultant string of bits
   * @param value positive integer to be converted into the relative string of bits: if it is negative,
   * the opposite integer will be converted.
   * @return string of bits representation of the number with length nbit
   */
  public static String intToBin(int nbit, long value) {
    String s = Converter.positiveIntToBin(value);
    int num = nbit - s.length();
    StringBuffer buf = new StringBuffer(s);

    for (int i = 0; i < num; i++) {
      buf.insert(0, '0');
    }

    if (value < 0) {
      try {
        String tmp = twoComplement(new String(buf));
        return tmp;
      } catch (IrregularStringOfBitsException e) {
        return null;
      }
    } else {
      return new String(buf);
    }
  }
  /** Converts an integer positive value in the relative string of bits with length nbit, doing a zero-padding
   * if the value string is shorter than nbit..
   * @param nbit length of the resultant string of bits
   * @param value positive integer to be converted into the relative string of bits: if it is negative,
   * the opposite integer will be converted.
   * @return string of bits representation of the number with length nbit
   */
  public static String positiveIntToBin(int nbit, long value) {
    String s = Converter.positiveIntToBin(value);
    int num = nbit - s.length();
    StringBuffer buf = new StringBuffer(s);

    for (int i = 0; i < num; i++) {
      buf.insert(0, '0');
    }

    return new String(buf);
  }


  /** Performs a two-complement operation on the string of bits: that is to say every bit is complemented and then 1 is added to
   * the value. This method is used for coding and decoding negative integers.
   * @param bits string of bits, must be composed obly by '0' and '1' chars, otherwise
   * an exception will be thrown.
   * @return string of bits with the result.
   * @throws IrregularStringOfBitsException if the string of bits is not well-formed.
   * */
  public static String twoComplement(String bits) throws IrregularStringOfBitsException {
    boolean carry = true;
    StringBuffer sbf = new StringBuffer(bits);

    for (int i = bits.length() - 1; i >= 0; --i) {
      char cur = bits.charAt(i);
      if (cur != '0' && cur != '1') {
        throw new IrregularStringOfBitsException();
      }
      if (carry) {
        sbf.setCharAt(i, cur);
        if (cur == '1') {
          carry = false;
        }
      } else {
        if (cur == '0') {
          sbf.setCharAt(i, '1');
        } else {
          sbf.setCharAt(i, '0');
        }
      }
    }

    return sbf.toString();
  }

  /** Converts a string of bits in the relative hexadecimal representation of the same value.
   * @param bits string of bits, must be composed obly by '0' and '1' chars, otherwise
   * an exception will be thrown.
   * @return string of hexadecimal digit [0-9,A-F], obtained by mapping every set of 4 bits
   * into the correspondant hexadecimal digit.
   * @throws IrregularStringOfBitsException if the string of bits is not well-formed.
   */
  public static String binToHex(String bits) throws IrregularStringOfBitsException {
    //  if( Converter.binToLong(bits,false) == 0)
    //    return "0";

    StringBuffer buf = new StringBuffer(bits);
    StringBuffer ret = new StringBuffer();
    int rem = bits.length() % 4;

    if (rem != 0) { //padding
      for (int i = rem; i < 4; i++) {
        buf.insert(0, '0');
      }
    }

    int exas = buf.length() / 4;

    for (int i = 0; i < exas; i++) {
      String token = buf.substring(4 * i, 4 * (i + 1));
      int value = Converter.binToInt(token, true);
      char toAppend = 'x';

      switch (value) {
        case 0:
          toAppend = '0';
          break;
        case 1:
          toAppend = '1';
          break;
        case 2:
          toAppend = '2';
          break;
        case 3:
          toAppend = '3';
          break;
        case 4:
          toAppend = '4';
          break;
        case 5:
          toAppend = '5';
          break;
        case 6:
          toAppend = '6';
          break;
        case 7:
          toAppend = '7';
          break;
        case 8:
          toAppend = '8';
          break;
        case 9:
          toAppend = '9';
          break;
        case 10:
          toAppend = 'A';
          break;
        case 11:
          toAppend = 'B';
          break;
        case 12:
          toAppend = 'C';
          break;
        case 13:
          toAppend = 'D';
          break;
        case 14:
          toAppend = 'E';
          break;
        case 15:
          toAppend = 'F';
          break;
        default:
          throw new IrregularStringOfBitsException();
      }

      ret.append(toAppend);
    }

    return new String(ret);
  }

  /** Converts a string of hexadecimal in the relative long value.
   * @param hex string of hexadecimal, must start whith a 'x' or a 'X' and continue with only [0-9] or [A-F] (or [a-f]) chars, otherwise
   * an IrregularStringOfHexException exception will be thrown.
   * @return string of long digit [0-9].
   * @throws IrregularStringOfHexException if the string of hexadecimal is not well-formed.
   */
  public static String hexToLong(String hex) throws IrregularStringOfHexException {

    if (hex.charAt(0) != '0' || hex.toUpperCase().charAt(1) != 'X') {
      throw new IrregularStringOfHexException();
    }

    long ret = 0;
    int reversecont = hex.length() - 2;

    for (int i = 2; i < hex.length(); i++) {
      reversecont--;
      char value = hex.toUpperCase().charAt(i);

      switch (value) {
        case '0':
          ret += 0 * powLong(16, reversecont);
          break;
        case '1':
          ret += 1 * powLong(16, reversecont);
          break;
        case '2':
          ret += 2 * powLong(16, reversecont);
          break;
        case '3':
          ret += 3 * powLong(16, reversecont);
          break;
        case '4':
          ret += 4 * powLong(16, reversecont);
          break;
        case '5':
          ret += 5 * powLong(16, reversecont);
          break;
        case '6':
          ret += 6 * powLong(16, reversecont);
          break;
        case '7':
          ret += 7 * powLong(16, reversecont);
          break;
        case '8':
          ret += 8 * powLong(16, reversecont);
          break;
        case '9':
          ret += 9 * powLong(16, reversecont);
          break;
        case 'A':
          ret += 10 * powLong(16, reversecont);
          break;
        case 'B':
          ret += 11 * powLong(16, reversecont);
          break;
        case 'C':
          ret += 12 * powLong(16, reversecont);
          break;
        case 'D':
          ret += 13 * powLong(16, reversecont);
          break;
        case 'E':
          ret += 14 * powLong(16, reversecont);
          break;
        case 'F':
          ret += 15 * powLong(16, reversecont);
          break;
        default:
          throw new IrregularStringOfHexException();
      }

    }

    return new String("" + ret);
  }

  /** Converts a string of hexadecimal in the relative long value.
   * @param hex string of hexadecimal, must start whith a 'x' or a 'X' and continue with only [0-9] or [A-F] (or [a-f]) chars, otherwise
   * an IrregularStringOfHexException exception will be thrown.
   * @return string of long digit [0-9].
   * @throws IrregularStringOfHexException if the string of hexadecimal is not well-formed.
   */
  public static String hexToBin(String hex) throws IrregularStringOfHexException {

    String ret = "";

    for (int i = 0; i < hex.length(); i++) {

      char value = hex.toUpperCase().charAt(i);

      switch (value) {
        case '0':
          ret += "0000";
          break;
        case '1':
          ret += "0001";
          break;
        case '2':
          ret += "0010";
          break;
        case '3':
          ret += "0011";
          break;
        case '4':
          ret += "0100";
          break;
        case '5':
          ret += "0101";
          break;
        case '6':
          ret += "0110";
          break;
        case '7':
          ret += "0111";
          break;
        case '8':
          ret += "1000";
          break;
        case '9':
          ret += "1001";
          break;
        case 'A':
          ret += "1010";
          break;
        case 'B':
          ret += "1011";
          break;
        case 'C':
          ret += "1100";
          break;
        case 'D':
          ret += "1101";
          break;
        case 'E':
          ret += "1110";
          break;
        case 'F':
          ret += "1111";
          break;
        default:
          throw new IrregularStringOfHexException();
      }

    }

    return ret;
  }
  public static long powLong(int base, long exp) {
    long ret = 1;

    for (int i = 1; i <= exp; i++) {
      ret *= base;
    }

    return ret;
  }

  /**
   * Determines if a string contains only 0's and 1's
   * @param bits any string
   * @return false if bits contains character that is not '0' and not '1', true otherwise
   */
  private static boolean isBinaryString(String bits) throws IrregularStringOfBitsException{
    char bit;
    for (int i = 0; i < bits.length(); i++) {
      bit = bits.charAt(i);
      if (bit != '0' && bit != '1') {
        throw new IrregularStringOfBitsException();
      }
    }
    return true;
  }

  /**
   * Determines if msb is '1' and all the rest '0'
   * @param bits any string of only 0's and 1's
   * @return true if msb is '1' and all the rest '0', false otherwise
   */
  private static boolean isOverflow(String bits){
    if(bits.charAt(0) != '1'){
      return false;
    }
    for (int i = 1; i < bits.length(); i++) {
      if (bits.charAt(i) != '0') {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines the integer value of an unsigned string of bits
   * bits any string of only 0's and 1's
   * @return integer value of the bit string
   */
  private static int getUnsignedIntValue(String bits){
    int value = 0;
    int i = 0;
    for (int j = bits.length() - 1; j >= 0; j--, i++) {
      if (bits.charAt(j) == '1') {
        value += 1 << i;
      }
    }
    return value;
  }

  /**
   * Determines the long value of an unsigned string of bits
   * bits any string of only 0's and 1's
   * @return long value of the bit string
   */
  private static long getUnsignedLongValue(String bits){
    long value = 0;
    int i = 0;
    for (int j = bits.length() - 1; j >= 0; j--, i++) {
      if (bits.charAt(j) == '1') {
        value += 1L << i;
      }
    }
    return value;
  }

/** Check if a string is an integer, i.e. a sequence of digits that might have a
 *  sign as the first element.
   *  @param num the string to validate
   *  @return true if num is an integer in the specified format, otherwise false
   */
  public static boolean isInteger(String num) {
    int len = num.length();
    if (len == 0) {
      return false;
    }

    // Check the sign.
    int cur = 0;
    char first = num.charAt(cur);
    if (first == '+' || first == '-') {
      if (len == 1) {
        // Only a sign.
        return false;
      }
      cur++;
    }

    // Check the rest of the number.
    for (; cur < num.length(); cur++) {
      char c = num.charAt(cur);
      if (c < '0' || c > '9') {
        return false;
      }
    }

    return true;
  }

/** Check if a string is a Hex number
   *  @param num the string to validate
   *  @return true if num is a number, else false
   */
  public static boolean isHexNumber(String num) {
    // Need at least 3 characters: 0, x (or X) and a number.
    int len = num.length();
    if (len < 3) {
      return false;
    }

    // The first must be a 0, the second an x or X.
    int cur = 0;
    if (num.charAt(cur) != '0') {
      return false;
    }
    cur++;
    char x = num.charAt(cur);
    if (x != 'x' && x != 'X') {
      return false;
    }
    
    // Check the rest of the number.
    for (; cur < num.length(); cur++) {
      char c = num.charAt(cur);
      boolean isHexDigit = (c >= '0' || c <= '9') || (c >= 'a' || c <= 'f') || (c >= 'A' || c <= 'F');
      if (!isHexDigit) {
        return false;
      }
    }
    return true;
  }

  /**
   * Parses an immediate value without any overflow/underflow check.
   * 
   * The immediate value may be preceded by the # character (which is ignored).
   * The immediate value may be encoded in base 10 or in base 16. In the latter
   * case, it must be preceded by the '0x' or '0X' prefix.
   * 
   * If the # character is used in a base-16 immediate, it must precede the 0x prefix.
   * 
   * @param immediate a string representing an immediate value.
   * @throws NumberFormatException if the number is not well-formatted.
   * @return the parsed integer value.
   */
  public static long parseImmediate(String immediate) {
    if (immediate.length() == 0) {
      throw new NumberFormatException("Invalid immediate: empty string.");
    }
    
    // Skip the initial #, if present.
    if (immediate.charAt(0) == '#') {
      immediate = immediate.substring(1);
    }

    // Check if it's a hexadecimal.
    int base = 10;
    if (immediate.length() >= 3 && immediate.substring(0, 2).compareToIgnoreCase("0x") == 0) {
      immediate = immediate.substring(2);
      base = 16;
    }

    return Long.parseLong(immediate, base);
  }
}

