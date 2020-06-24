/*
 * DMULTU.java
 *
 * Instruction DMULT of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Andrea Milazzo (Mancausoft)
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

import org.edumips64.core.Converter;
import org.edumips64.core.IrregularStringOfBitsException;
import org.edumips64.core.IrregularWriteOperationException;
import org.edumips64.core.Register;
import org.edumips64.core.fpu.FPInvalidOperationException;

import java.math.BigInteger;

/**
 * <pre>
 *      Syntax: DMULTU rs, rt
 * Description: (HI)(LO) = rs * rt
 *              To multiply 64-bit unsigned integers
 *              The 64-bit doubleword value in GPR rt is multiplied by the 64-bit
 *              value in GPR rs.
 * </pre>
 *
 * @author Andrea Milazzo (Mancausoft)
 */
class DMULTU extends ALU_RType {
  private final int RS_FIELD = 0;
  private final int RT_FIELD = 1;
  private final String OPCODE_VALUE = "011100";

  private String lo;
  private String hi;

  DMULTU() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    syntax = "%R,%R";
    name = "DMULTU";
  }
  public boolean ID() throws IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, JumpException, BreakException, WAWException, FPInvalidOperationException {
    //if source registers are valid passing their own values into temporary registers
    Register rs = cpu.getRegister(params.get(RS_FIELD));
    Register rt = cpu.getRegister(params.get(RT_FIELD));

    if (rs.getWriteSemaphore() > 0 || rt.getWriteSemaphore() > 0) {
      return true;
    }

    TR[RS_FIELD] = rs;
    TR[RT_FIELD] = rt;
    //locking the destination register

    cpu.getLO().incrWriteSemaphore();
    cpu.getHI().incrWriteSemaphore();
    return false;
  }
  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {

    //getting values from temporary registers
    BigInteger rs = new BigInteger(TR[RS_FIELD].getHexString(), 16);
    BigInteger rt = new BigInteger(TR[RT_FIELD].getHexString(), 16);
    BigInteger result = rs.multiply(rt);

    // Convert result to a String of 128-bit
    String tmp = result.toString(2);

    while (tmp.length() < 128) {
      tmp = "0" + tmp;
    }

    hi = tmp.substring(0, 64);
    lo = tmp.substring(64);

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
    //passing results from temporary registers to destination registers and unlocking them
    Register lo = cpu.getLO();
    Register hi = cpu.getHI();
    lo.setBits(this.lo, 0);
    hi.setBits(this.hi, 0);

    lo.decrWriteSemaphore();
    hi.decrWriteSemaphore();
  }
  public void pack() throws IrregularStringOfBitsException {
    //conversion of instruction parameters of "params" list to the "repr" form (32 binary value)
    repr.setBits(OPCODE_VALUE, OPCODE_VALUE_INIT);
    repr.setBits(Converter.intToBin(RS_FIELD_LENGTH, params.get(RS_FIELD)), RS_FIELD_INIT);
    repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
  }
}
