/*
 * MFLO.java
 *
 * 30th may 2006
 * Instruction MFLO of the MIPS64 Instruction Set
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
import org.edumips64.core.*;
import org.edumips64.core.fpu.FPInvalidOperationException;

//per diagnostica


/**
 * <pre>
 * Syntax:      MFLO rd
 * Description: rd = LO
 *              To copy the special purpose LO register to a GPR
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 *
 */
class MFLO extends ALU_RType {
  final int RD_FIELD = 0;
  final int LO_REG = 1;
  final String OPCODE_VALUE = "010010";

  MFLO() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    syntax = "%R";
    name = "MFLO";
  }
  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    //if the LO register is valid passing his own value into temporary register
    Register lo_reg = cpu.getLO();

    if (lo_reg.getWriteSemaphore() > 0) {
      return true;
    }

    TR[LO_REG] = lo_reg;
    //locking the destination register
    Register rd = cpu.getRegister(params.get(RD_FIELD));
    rd.incrWriteSemaphore();
    return false;
  }
  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }
  public void WB() throws IrregularStringOfBitsException {
    if (!cpu.isEnableForwarding()) {
      doWB();
    }
  }
  public void doWB() throws IrregularStringOfBitsException {
    cpu.getRegister(params.get(RD_FIELD)).setBits(TR[LO_REG].getBinString(), 0);
    cpu.getRegister(params.get(RD_FIELD)).decrWriteSemaphore();
  }
  public void pack() throws IrregularStringOfBitsException {
    //conversion of instruction parameters of "params" list to the "repr" form (32 binary value)
    repr.setBits(OPCODE_VALUE, OPCODE_VALUE_INIT);
    repr.setBits(Converter.intToBin(RD_FIELD_LENGTH, params.get(RD_FIELD)), RD_FIELD_INIT);
  }



}
