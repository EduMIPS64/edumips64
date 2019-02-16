/*
 * MOVN.java
 *
 * 26th may 2006
 * Instruction MOVN of the MIPS64 Instruction Set
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
import org.edumips64.core.IrregularStringOfBitsException;

import java.util.logging.Logger;

/**
 * <pre>
 * Format:      MOVN rd, rs, rt
 * Description: if rt != 0 then rd = rs
 *              If the value in GPR rt is not equal to zero, then the contents
 *              of GPR rs are placed into GPR rd.
  *</pre>
 * @author Trubia Massimo, Russo Daniele
 *
 */
class MOVN extends ALU_RType {
  private final String OPCODE_VALUE = "001011";
  private static final Logger logger = Logger.getLogger(MOVN.class.getName());

  // Set to true if the Write Back stage should write data.
  private boolean should_write = false;


  MOVN() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    name = "MOVN";
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
    if (TR[RT_FIELD].getValue() != 0) {
      TR[RD_FIELD].setBits(TR[RS_FIELD].getBinString(), 0);
      should_write = true;
    }

    if (cpu.isEnableForwarding()) {
      doWB();
    }
  }

  public void doWB() throws IrregularStringOfBitsException {
    // The doWB() method is overridden because it must check if the write
    // on the registers must be done, checking the should_write variable.
    if (should_write) {
      logger.info("Writing to the dest register, since the condition is true.");
      cpu.getRegister(params.get(RD_FIELD)).setBits(TR[RD_FIELD].getBinString(), 0);
    }

    // We must unlock the register in both cases.
    cpu.getRegister(params.get(RD_FIELD)).decrWriteSemaphore();
  }
}
