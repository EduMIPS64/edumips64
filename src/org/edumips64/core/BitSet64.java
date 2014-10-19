/* BitSet64.java
 *
 * This class models a 64-bit array, useful for registers and memory representation.
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

import org.edumips64.utils.*;
import java.util.BitSet;

/** This class models a 64-bit array, useful for registers and memory representation.
 * @author Salvatore Scellato
 * */

public class BitSet64 extends FixedBitSet {

//  public final long MAX_DOUBLE = 9223372036854775808L;
  /** Creates a default new instance of BitSet64. */
  public BitSet64() {
    super();
    size = 64;
  }

  /** Writes an unsigned byte value into this FixedBitSet: the value to be written must be in the range [0, 255],
   * otherwise an exception will be thrown.
   * @param value number to be written: must be <CODE>0 &lt;= value &lt;= 255</CODE>
   * @throws IrregularWriteOperationException if value is not correct or anything else goes wrong during the operation
   */
  public void writeByteUnsigned(int value) throws IrregularWriteOperationException {
    if (value < 0 || value > 255) {
      throw new IrregularWriteOperationException();
    } else {
      String bits = Converter.positiveIntToBin(value);

      try {

        this.reset(false);
        //dobbiamo scrivere solo negli ultimi 8 bit
        //ma considerando la dimensione della nuova stringa di bit!!
        this.setBits(bits, size - bits.length());
      } catch (IrregularStringOfBitsException e) {
        System.err.println("stringa errata: " + bits);
        e.printStackTrace();
        throw new IrregularWriteOperationException();
      }

    }
  }

  /** Writes a byte value into this FixedBitSet: the value to be written must be in the range [-128, 127],
   * otherwise an exception will be thrown.
   * @param value number to be written: must be <CODE>-128 &lt;= value &lt;= 127</CODE>
   * @throws IrregularWriteOperationException if value is not correct or anything else goes wrong during the operation
   */
  public void writeByte(int value) throws IrregularWriteOperationException {
    if (value < -128 || value > 127) {
      throw new IrregularWriteOperationException();
    } else {
      String bits = Converter.positiveIntToBin(value);

      if (value >= 0) {
        try {
          this.reset(false);
          this.setBits(bits, size - bits.length());
        } catch (IrregularStringOfBitsException e) {
          System.err.println("stringa errata: " + bits);
          e.printStackTrace();
          throw new IrregularWriteOperationException();
        }
      } else { //il numero Ãš negativo
        value = -value;

        try {
          bits = Converter.twoComplement(bits);
          //estensione del segno
          this.reset(true);  //il numero Ãš negativo, ci vogliono tutti '1'
          this.setBits(bits, size - bits.length());
        } catch (IrregularStringOfBitsException e) {
          System.err.println("stringa errata: " + bits);
          e.printStackTrace();
          throw new IrregularWriteOperationException();
        }
      }
    }
  }

  /** Writes a byte value into this FixedBitSet with an offset: the value to be written must be in the range [-128, 255],
   * otherwise an exception will be thrown.
   * @param value number to be written: must be <CODE>-128 &lt;= value &lt;= 255</CODE>
   * @param offset position to write the byte
   * @throws IrregularWriteOperationException if value is not correct or anything else goes wrong during the operation
   */
  public void writeByte(int value, int offset) throws IrregularWriteOperationException {
    offset *= 8;
    offset = 56 - offset;

    if (value < -128 || value > 255) {
      throw new IrregularWriteOperationException();
    } else {
      String bits = Converter.positiveIntToBin(8, value);

      if (value >= 0) {
        try {
          this.setBits(bits, offset);
        } catch (IrregularStringOfBitsException e) {
          System.err.println("stringa errata: " + bits);
          e.printStackTrace();
          throw new IrregularWriteOperationException();
        }
      } else { //il numero Ãš negativo
        value = -value;

        try {
          bits = Converter.twoComplement(bits);
          this.setBits(bits, offset);
        } catch (IrregularStringOfBitsException e) {
          System.err.println("stringa errata: " + bits);
          e.printStackTrace();
          throw new IrregularWriteOperationException();
        }
      }
    }
  }


  /** Writes an unsigned half-word (16 bit) value into this FixedBitSet: the value to be written must be in the range [0, 65535],
   * otherwise an exception will be thrown.
   * @param value number to be written: must be <CODE>0 &lt;= value &lt;= 65535</CODE>
   * @throws IrregularWriteOperationException if value is not correct or anything else goes wrong during the operation
   */
  public void writeHalfUnsigned(int value) throws IrregularWriteOperationException {
    if (value < 0 || value > 65535) {
      throw new IrregularWriteOperationException();
    } else {
      String bits = Converter.positiveIntToBin(value);

      try {
        this.reset(false);
        //dobbiamo scrivere solo negli ultimi 16 bit
        this.setBits(bits, size - bits.length());
      } catch (IrregularStringOfBitsException e) {
        System.err.println("stringa errata: " + bits);
        e.printStackTrace();
        throw new IrregularWriteOperationException();
      }

    }
  }


  /** Writes a half-word (16 bit) value into this FixedBitSet: the value to be written must be in the range [-32768, 32767],
   * otherwise an exception will be thrown.
   * @param value number to be written: must be <CODE>-32768 &lt;= value &lt;= 32767</CODE>
   * @throws IrregularWriteOperationException if value is not correct or anything else goes wrong during the operation
   */
  public void writeHalf(int value) throws IrregularWriteOperationException {
    if (value < -32768 || value > 32767) {
      throw new IrregularWriteOperationException();
    } else {
      String bits = Converter.positiveIntToBin(value);

      if (value >= 0) {
        try {
          this.reset(false);
          this.setBits(bits, size - bits.length());
        } catch (IrregularStringOfBitsException e) {
          System.err.println("stringa errata: " + bits);
          e.printStackTrace();
          throw new IrregularWriteOperationException();
        }
      } else { //il numero Ãš negativo
        value = -value;

        try {
          bits = Converter.twoComplement(bits);
          //estensione del segno
          this.reset(true);  //il numero Ãš negativo, ci vogliono tutti '1'
          this.setBits(bits, size - bits.length());
        } catch (IrregularStringOfBitsException e) {
          System.err.println("stringa errata: " + bits);
          e.printStackTrace();
          throw new IrregularWriteOperationException();
        }
      }
    }
  }
  /** Writes a half-word (16 bit) value into this FixedBitSet with a ofset: the value to be written must be in the
   * range [-32768, 65536], otherwise an exception will be thrown.
   * @param value number to be written: must be <CODE>-32768 &lt;= value &lt;= 65536</CODE>
   * @param offset position to write the HalfWord
   * @throws IrregularWriteOperationException if value is not correct or anything else goes wrong during the operation
   * @throws NotAlignException if offset is not aling to 16 bit
   */
  public void writeHalf(int value, int offset) throws IrregularWriteOperationException, NotAlignException {
    offset *= 8;
    offset = 48 - offset;

    if (value < -32768 || value > 65536) {
      throw new IrregularWriteOperationException();
    } else if (offset % 16 !=  0) {
      throw new NotAlignException();
    } else {
      String bits = Converter.positiveIntToBin(16, value);

      if (value >= 0) {
        try {
          this.setBits(bits, offset);
        } catch (IrregularStringOfBitsException e) {
          System.err.println("stringa errata: " + bits);
          e.printStackTrace();
          throw new IrregularWriteOperationException();
        }
      } else { //il numero Ãš negativo
        value = -value;

        try {
          bits = Converter.twoComplement(bits);
          this.setBits(bits, offset);
        } catch (IrregularStringOfBitsException e) {
          System.err.println("stringa errata: " + bits);
          e.printStackTrace();
          throw new IrregularWriteOperationException();
        }
      }
    }
  }

  /** Writes an unsigned word (32 bit) value into this FixedBitSet: the value to be written must be in the range [0,4294967295],
   * otherwise an exception will be thrown.
   * @param value number to be written: must be <CODE>0 &lt;= value &lt;= 4294967295</CODE>
   * @throws IrregularWriteOperationException if value is not correct or anything else goes wrong during the operation
   */
  public void writeWordUnsigned(long value) throws IrregularWriteOperationException, NotAlignException {
    if (value < 0 || value > 4294967295L) {
      throw new IrregularWriteOperationException();
    } else {
      String bits = Converter.positiveIntToBin(value);

      try {
        this.reset(false);
        //dobbiamo scrivere solo negli ultimi 32 bit
        this.setBits(bits, size - bits.length());
      } catch (IrregularStringOfBitsException e) {
        System.err.println("stringa errata: " + bits);
        e.printStackTrace();
        throw new IrregularWriteOperationException();
      }

    }
  }
  /** Writes a word value (32 bit) into this FixedBitSet: the value to be written must be in the range [-2147483648, 2147483647],
   * otherwise an exception will be thrown (please note that this range is the same of the java <CODE>int</CODE> type).
   * @param value number to be written: must be <CODE>-2147483648 &lt;= value &lt;= 2147483647</CODE>
   */
  public void writeWord(int value) throws IrregularWriteOperationException {
    if (value < -2147483648 || value > 2147483647) {
      throw new IrregularWriteOperationException();
    }

    String bits = Converter.positiveIntToBin(value);

    if (value >= 0) {
      try {
        this.reset(false);
        this.setBits(bits, size - bits.length());
      } catch (IrregularStringOfBitsException e) {
        System.err.println("stringa errata: " + bits);
        e.printStackTrace();
        throw new IrregularWriteOperationException();
      }
    } else { //il numero Ãš negativo
      value = -value;

      try {
        bits = Converter.twoComplement(bits);
        //estensione del segno
        this.reset(true);  //il numero Ãš negativo, ci vogliono tutti '1'
        this.setBits(bits, size - bits.length());
      } catch (IrregularStringOfBitsException e) {
        System.err.println("stringa errata: " + bits);
        e.printStackTrace();
        throw new IrregularWriteOperationException();
      }
    }
  }


  /** Writes a word value (32 bit) into this FixedBitSet with a offset: the value to be written must be in the range [-2147483648, 4294967296],
   * otherwise an exception will be thrown (please note that this range is the same of the java <CODE>int</CODE> type).
   * @param value number to be written: must be <CODE>-2147483648 &lt;= value &lt;= 4294967296</CODE>
   * @param offset position to write the Word
   * @throws IrregularWriteOperationException if value is not correct or anything else goes wrong during the operation
   * @throws NotAlignException if offset is not aling to 32 bit
   */
  public void writeWord(long value, int offset) throws IrregularWriteOperationException, NotAlignException {
    offset *= 8;
    offset = 32 - offset;

    if (value < -2147483648 || value > 4294967295L) {
      throw new IrregularWriteOperationException();
    } else if (offset % 32 !=  0) {
      throw new NotAlignException();
    }

    String bits = Converter.positiveIntToBin(32, value);

    if (value >= 0) {
      try {
        this.setBits(bits, offset);
      } catch (IrregularStringOfBitsException e) {
        System.err.println("stringa errata: " + bits);
        e.printStackTrace();
        throw new IrregularWriteOperationException();
      }
    } else { //il numero Ãš negativo
      value = -value;

      try {
        bits = Converter.twoComplement(bits);
        this.setBits(bits, offset);
      } catch (IrregularStringOfBitsException e) {
        System.err.println("stringa errata: " + bits);
        e.printStackTrace();
        throw new IrregularWriteOperationException();
      }
    }
  }



  /** Writes a double value (64 bit) into this FixedBitSet: the value to be written must be in the range [-2^63, (2^63)-1],
   * otherwise an exception will be thrown (please note that this range is the same of the java <CODE>long</CODE> type).
   * @param value number to be written: must be <CODE>2^63 &lt;= value &lt;= (2^63)-1</CODE>
   * @throws IrregularWriteOperationException if value is not correct or anything else goes wrong during the operation
   */
  public void writeDoubleWord(long value) throws IrregularWriteOperationException {
    if (-value > Math.pow(2.0, 63.0) || value >= Math.pow(2.0, 63.0)) {
      /*System.out.println("writeDoubleWord, hai tentato di scrivere un numero troppo grande: " + value);
      System.out.println( -value + " > " + Math.pow(2.0, 63.0));
      System.out.println(-value > Math.pow(2.0,63.0));
      System.out.println( (double)value + " >= " + Math.pow(2.0, 63.0));
      System.out.println((double)value >= Math.pow(2.0,63.0));
      */
      //throw new IrregularWriteOperationException();
    }

    String bits = Converter.positiveIntToBin(value);

    if (value >= 0) {
      try {
        this.reset(false);
        this.setBits(bits, size - bits.length());
      } catch (IrregularStringOfBitsException e) {
        System.err.println("stringa errata: " + bits);
        e.printStackTrace();
        throw new IrregularWriteOperationException();
      }
    } else { //il numero Ãš negativo
      if (value == -9223372036854775808L) {
        //questo numero non puÃ² essere convertito dalla positiveIntToBin
        //occorre fare a mano
        //e mettere il valore "1000000....0000" (tutti zero nel mezzo)
        this.reset(false);

        try {
          this.setBits("1", 0);
        } catch (IrregularStringOfBitsException e) {} //non puÃ² accadere :-)

        return;
      }

      value = -value;

      try {
        bits = Converter.positiveIntToBin(value);
        bits = Converter.twoComplement(bits);
        //estensione del segno
        this.reset(true);  //il numero Ãš negativo, ci vogliono tutti '1'
        this.setBits(bits, size - bits.length());
      } catch (IrregularStringOfBitsException e) {
        System.err.println("stringa errata: " + bits);
        e.printStackTrace();
        throw new IrregularWriteOperationException();
      }
    }
  }

  /** Get the value of the one Byte of bitset by position
   *  @param offset position to read the byte
   *  @return the value of the byte
   */
  public int readByte(int offset) {
    offset *= 8;
    offset = 56 - offset;
    String val = getBinString().substring(offset, offset + 8);

    try {
      return Converter.binToInt(val, false);
    } catch (IrregularStringOfBitsException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }

    return 0;
  }

  /** Get the value Unsigned of the one Byte of bitset by position
   *  @param offset position to read the byte
   *  @return the value Unsigned of the byte
   */
  public int readByteUnsigned(int offset) {
    offset *= 8;
    offset = 56 - offset;
    String val = getBinString().substring(offset, offset + 8);

    try {
      return Converter.binToInt(val, true);
    } catch (IrregularStringOfBitsException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }

    return 0;
  }
  /** Get the value of the one HalfWord of bitset by position
   *  @param offset position to read the byte
   *  @return the value of the byte
   * @throws NotAlignException if offset is not aling to 16 bit
   */
  public int readHalf(int offset) throws IrregularStringOfBitsException, NotAlignException {
    if (offset % 2 != 0) {
      throw new NotAlignException();
    }

    offset *= 8;
    offset = 48 - offset;
    String val = getBinString().substring(offset, offset + 16);

    try {
      return Converter.binToInt(val, false);
    } catch (IrregularStringOfBitsException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }

    return 0;
  }

  /** Get the value Unsigned of the one HalfWord of bitset by position
   *  @param offset position to read the byte
   *  @return the value Unsigned of the byte
   * @throws NotAlignException if offset is not aling to 16 bit
   */
  public int readHalfUnsigned(int offset) throws NotAlignException {
    if (offset % 2 != 0) {
      throw new NotAlignException();
    }

    offset *= 8;
    offset = 48 - offset;
    String val = getBinString().substring(offset, offset + 16);

    try {
      return Converter.binToInt(val, true);
    } catch (IrregularStringOfBitsException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }

    return 0;
  }
  /** Get the value of the one Word of bitset by position
   *  @param offset position to read the byte
   *  @return the value of the byte
   * @throws NotAlignException if offset is not aling to 32 bit
   */
  public int readWord(int offset) throws NotAlignException {
    if (offset % 4 != 0) {
      throw new NotAlignException();
    }

    offset *= 8;
    offset = 32 - offset;
    String val = getBinString().substring(offset, offset + 32);

    try {
      return Converter.binToInt(val, false);
    } catch (IrregularStringOfBitsException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }

    return 0;
  }

  /** Get the value Unsigned of the one Word of bitset by position
   *  @param offset position to read the byte
   *  @return the value Unsigned of the byte
   * @throws NotAlignException if offset is not aling to 32 bit
   */
  public long readWordUnsigned(int offset) throws NotAlignException {
    if (offset % 4 != 0) {
      throw new NotAlignException();
    }

    offset *= 8;
    offset = 32 - offset;
    String val = getBinString().substring(offset, offset + 32);

    try {
      return Converter.binToLong(val, true);
    } catch (IrregularStringOfBitsException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }

    return 0;
  }
  public static void main(String[] args) throws Exception {

    BitSet64 bs = new BitSet64();
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < 64; i++) {
      buf.append("1");
    }

    BitSet64 bs2 = new BitSet64();
    bs2.setBits(new String(buf), 0);
    bs.setBits("011100110101101100101011001101010", 0);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Binary string: " + bs2.getBinString());
    System.out.println("Hex string: " + bs2.getHexString());
    System.out.println("\nPROVA SCRITTURA");
    System.out.println("BYTE");
    bs.writeByteUnsigned(240);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Value: " + Converter.binToInt(bs.getBinString(), true));
    bs.writeByte(-12);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Value: " + Converter.binToInt(bs.getBinString(), false));
    System.out.println("HALF");
    bs.writeHalfUnsigned(64400);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Value: " + Converter.binToInt(bs.getBinString(), true));
    bs.writeHalf(-4);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Value: " + Converter.binToInt(bs.getBinString(), false));
    System.out.println("WORD");
    bs.writeWordUnsigned(4001198875L);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Value: " + Converter.binToLong(bs.getBinString(), false));
    bs.writeWord(-1987234555);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Value: " + Converter.binToInt(bs.getBinString(), false));
    System.out.println("DOUBLE");
    long l = (long)(Math.pow(2.0, 63.0) - 1);   // + Math.pow(2.0, 45) + Math.pow(3, 15));
    System.out.println("long = " + l);
    bs.writeDoubleWord(l);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Value: " + Converter.binToLong(bs.getBinString(), false));
    l = (long)(-Math.pow(2.0, 63.0));   // + Math.pow(2.0, 45) + Math.pow(3, 15));
    System.out.println("long = " + l);
    bs.writeDoubleWord(l);
    System.out.println("Binary string: " + bs.getBinString());
    System.out.println("Hex string: " + bs.getHexString());
    System.out.println("Value: " + Converter.binToLong(bs.getBinString(), false));
    System.out.println(bs.readHalf(6));
  }
}



