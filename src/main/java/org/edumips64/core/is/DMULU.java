/*
 * DMULU.java
 *
 * Instruction DMULU of the MIPS64 Instruction Set
 * (c) 2023 EduMips64 project
 * 
 * DMULU: Multiply Doublewords Unsigned, Low Doubleword
 *
 * Performs an unsigned 64-bit integer multiplication,
 * and places the low 64 bits of the result in the destination
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
 *      Syntax: DMULU rd,rs,rt
 * Description: rd <- lo_doubleword(rs * rt)
 *              Multiply 64-bit unsigned integers
 *              The 64-bit doubleword value in GPR rt is multiplied by the 64-bit
 *              value in GPR rs, and the lower 64 bits of the 128-bit result
 */
class DMULU extends ALU_RType {
    // Note: there are 2 fields at the end of the DMULU representation:
    //              the opcode: 00010
    //   the special opcode 35: 011101
    // The combination of both fields is necessary for some multiplication
    // instructions, and is present in DMULU as well.
    // For simplicity, we bundle them together in a single field.
    private final String OPCODE_VALUE = "00010" + "011101";

    DMULU() {
        super.OPCODE_VALUE = OPCODE_VALUE;
        name = "DMULU";
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

        // Get only the lower 64 bit.
        String tmpLo = tmp.substring(64);
        TR[RD_FIELD].setBits(tmpLo, 0);

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