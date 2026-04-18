/*
 * DMUHU.java
 *
 * Instruction DMUHU of the MIPS64 Instruction Set
 * (c) 2023 EduMips64 project
 * 
 * DMUHU: Multiply Doublewords Unsigned, High Doubleword
 *
 * Performs an unsigned 64-bit integer multiplication,
 * and places the high 64 bits of the result in the destination
register.
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

import java.math.BigInteger;

import org.edumips64.core.Converter;
import org.edumips64.core.IrregularStringOfBitsException;

/**
 * <pre>
 *      Syntax: DMUHU rd,rs,rt
 * Description: rd <- hi_doubleword(rs * rt)
 *              Multiply 64-bit unsigned integers
 *              The 64-bit doubleword value in GPR rt is multiplied by the 64-bit
 *              value in GPR rs, and the upper 64 bits of the 128-bit result
 */
class DMUHU extends ALU_RType {
    // See explanation in DMULU.
    private final String OPCODE_VALUE = "00011" + "011101";

    DMUHU() {
        super.OPCODE_VALUE = OPCODE_VALUE;
        name = "DMUHU";
    }

    public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
        // Getting operands from temporary registers.
        BigInteger rs = new BigInteger(TR[RS_FIELD].getHexString(), 16);
        BigInteger rt = new BigInteger(TR[RT_FIELD].getHexString(), 16);

        // Execute the multiplication.
        BigInteger result = rs.multiply(rt);

        // Convert result to a String of 128-bit
        String tmp = result.toString(2);

        // 0-pad up to 128 bit.
        while (tmp.length() < 128) {
            tmp = "0" + tmp;
        }

        // Get only the upper 64 bit.
        String tmpHi = tmp.substring(0, 64);
        TR[RD_FIELD].setBits(tmpHi, 0);

        if (cpu.isEnableForwarding()) {
          doWB();
        }
    }

    public void pack() throws IrregularStringOfBitsException {
        // "SPECIAL" value of 000000.
        repr.setBits("000000", 0);
        repr.setBits(Converter.intToBin(RS_FIELD_LENGTH, params.get(RS_FIELD)), RS_FIELD_INIT);
        repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
        repr.setBits(Converter.intToBin(RD_FIELD_LENGTH, params.get(RD_FIELD)), RD_FIELD_INIT);
        // Opcode and special opcode, at the end.
        repr.setBits(OPCODE_VALUE, 6 + RT_FIELD_LENGTH + RS_FIELD_LENGTH + RD_FIELD_LENGTH);
    }
}