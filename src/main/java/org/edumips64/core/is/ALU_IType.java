/*
 * ALU_IType.java
 *
 * 5th may 2006
 * Subgroup of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo, Russo Daniele
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
 * Foundation, Inc., 59 Temple  Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.edumips64.core.is;

import org.edumips64.core.*;
import org.edumips64.core.fpu.FPInvalidOperationException;
//per diagnostica
import java.util.logging.Logger;

/** This is the base class for all the immediate ALU instructions
 *
 * @author Trubia Massimo, Russo Daniele
 */
public abstract class ALU_IType extends ComputationalInstructions {
  protected final static int RT_FIELD = 0;
  protected final static int RS_FIELD = 1;
  protected final static int IMM_FIELD = 2;
  private final static int RT_FIELD_INIT = 11;
  private final static int RS_FIELD_INIT = 6;
  private final static int IMM_FIELD_INIT = 16;
  private final static int RT_FIELD_LENGTH = 5;
  private final static int RS_FIELD_LENGTH = 5;
  private final static int IMM_FIELD_LENGTH = 16;
  private final static int IMM_FIELD_MAX = (int) Math.pow(2, IMM_FIELD_LENGTH - 1) - 1;
  protected String OPCODE_VALUE = "";

  private static final Logger logger = Logger.getLogger(ALU_IType.class.getName());

  ALU_IType() {
    this.syntax = "%R,%R,%I";
    this.paramCount = 3;
  }

  /**
   * Throws an IntegerOverflowException if the size of the value that should be loaded in the immediate field
   * exceeds the length of the field itself.
   * @throws IntegerOverflowException
   */
  protected void checkImmediateForOverflow() throws IntegerOverflowException {
    if (params.get(IMM_FIELD) > IMM_FIELD_MAX) {
      throw new IntegerOverflowException();
    }
  }

  public boolean ID() throws IntegerOverflowException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    checkImmediateForOverflow();
    //if the source register is valid passing its own values into a temporary register
    final Register rs = cpu.getRegister(params.get(RS_FIELD));

    if (rs.getWriteSemaphore() > 0) {
      return true;
    }

    TR[RS_FIELD].setBits(rs.getBinString(), 0);
    // locking the target register
    final Register rt = cpu.getRegister(params.get(RT_FIELD));
    rt.incrWriteSemaphore();
    //writing the immediate value of "params" on a temporary register
    TR[IMM_FIELD].writeHalf(params.get(IMM_FIELD));
    return false;
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException {
  }

  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
  }


  public void WB() throws IrregularStringOfBitsException {
    if (!cpu.isEnableForwarding()) {
      doWB();
    }
  }

  public void doWB() throws IrregularStringOfBitsException {
    //passing result from temporary register to destination register and unlocking it
    logger.info("WB of the ALU I-Type instruction. Writing " + TR[RT_FIELD].getValue() + " to R" + params.get(RT_FIELD));
    cpu.getRegister(params.get(RT_FIELD)).setBits(TR[RT_FIELD].getBinString(), 0);
    cpu.getRegister(params.get(RT_FIELD)).decrWriteSemaphore();
  }

  public void pack() throws IrregularStringOfBitsException {
    repr.setBits(OPCODE_VALUE, 0);
    repr.setBits(Converter.intToBin(RS_FIELD_LENGTH, params.get(RS_FIELD)), RS_FIELD_INIT);
    repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
    repr.setBits(Converter.intToBin(IMM_FIELD_LENGTH, params.get(IMM_FIELD)), IMM_FIELD_INIT);
  }
}
