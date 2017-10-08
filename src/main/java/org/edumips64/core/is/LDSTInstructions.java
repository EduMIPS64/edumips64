/*
 * LDSTInstructions.java
 *
 * 8th may 2006
 * Subgroup of the MIPS64 Instruction Set
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
import org.edumips64.core.IrregularWriteOperationException;
import org.edumips64.core.Memory;
import org.edumips64.core.MemoryElement;
import org.edumips64.core.MemoryElementNotFoundException;
import org.edumips64.core.NotAlignException;
import org.edumips64.core.fpu.FPInvalidOperationException;
import org.edumips64.utils.Converter;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.IrregularStringOfBitsException;

/**This is the base class of Load store instructions
 *
 * @author Trubia Massimo, Russo Daniele
 */

public abstract class LDSTInstructions extends Instruction {
  final static int RT_FIELD = 0;
  final static int OFFSET_FIELD = 1;
  final static int BASE_FIELD = 2;
  final static int LMD_REGISTER = 3;
  final static int OFFSET_PLUS_BASE = 4;
  final static int RT_FIELD_INIT = 11;
  final static int OFFSET_FIELD_INIT = 16;
  final static int BASE_FIELD_INIT = 6;
  final static int RT_FIELD_LENGTH = 5;
  final static int OFFSET_FIELD_LENGTH = 16;
  final static int BASE_FIELD_LENGTH = 5;

  // Size of the read/write operations. Must be set by derived classes
  protected byte memoryOpSize;

  // Memory address with which the instruction is operating
  protected long address;

  protected MemoryElement memEl;

  // Memory instance
  protected Memory memory;

  String OPCODE_VALUE = "";
  LDSTInstructions(Memory memory) {
    this.syntax = "%R,%L(%R)";
    this.paramCount = 3;
    this.memory = memory;
  }

  public void IF() {
    try {
      dinero.IF(Converter.binToHex(Converter.intToBin(64, cpu.getLastPC().getValue())));
    } catch (IrregularStringOfBitsException e) {
      e.printStackTrace();
    }
  }
  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    return false;
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, NotAlignException, AddressErrorException {
    // Compute the address
    address = TR[OFFSET_PLUS_BASE].getValue();

    // Address must be >= 0
    if (address < 0) {
      String message = CurrentLocale.getString("NEGADDRERR") + " " + fullname + ". " +
                       CurrentLocale.getString("ADDRESS") + ": " + address + ".";
      throw new AddressErrorException(message);
    }

    // Check alignment
    if (address % memoryOpSize != 0) {
      String message = CurrentLocale.getString("ALIGNERR") + " " + fullname + ": " +
                       CurrentLocale.getString("THEADDRESS") + " " + address + " " +
                       CurrentLocale.getString("ISNOTALIGNED") + " " + memoryOpSize + " bytes";
      throw new NotAlignException(message);
    }

  }

  // This is the method that actually stores/loads data to/from memory, and
  // that is different for each concrete sub-class
  public abstract void doMEM() throws IrregularStringOfBitsException, NotAlignException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException;

  // Does the necessary operations (e.g., forwarding) before and after the
  // execution of doMEM();
  public abstract void MEM() throws IrregularStringOfBitsException, NotAlignException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException;
  public void WB() throws IrregularStringOfBitsException {}

  public void pack() throws IrregularStringOfBitsException {
    //conversion of instruction parameters of params list to the "repr" 32 binary value
    repr.setBits(OPCODE_VALUE, 0);
    repr.setBits(Converter.intToBin(BASE_FIELD_LENGTH, params.get(BASE_FIELD)), BASE_FIELD_INIT);
    repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
    repr.setBits(Converter.intToBin(OFFSET_FIELD_LENGTH, params.get(OFFSET_FIELD)), OFFSET_FIELD_INIT);
  }
}
