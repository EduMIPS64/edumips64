/*
 * DMOD.java
 *
 * Instruction DMOD of the MIPS64 Instruction Set
 * (c) 2025 EduMips64 project
 * 
 * DMOD: Modulo Doublewords Signed
 *
 * Performs a signed 64-bit integer division,
 * and places the remainder in the destination register.
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

/**
 * <pre>
 *      Syntax: DMOD rd,rs,rt
 * Description: rd <- rs % rt
 *              Modulo operation on 64-bit signed integers
 *              The 64-bit doubleword value in GPR rs is divided by the 64-bit
 *              value in GPR rt, treating both operands as signed values, and the 
 *              remainder is placed in GPR rd.
 *              
 *              This is the MIPS64 Release 6 version of the modulo operation.
 * </pre>
 */
class DMOD extends ALU_RType {
    // DMOD uses SPECIAL (000000) opcode with sa field and function field.
    // sa = 00010 (2), function = 011011 (27)
    // The complete encoding is: SPECIAL | rs | rt | rd | sa | function
    private final String OPCODE_VALUE = "00010" + "011011";

    DMOD() {
        super.OPCODE_VALUE = OPCODE_VALUE;
        name = "DMOD";
    }

    public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, DivisionByZeroException {
        // Getting operands from temporary registers as signed values.
        long rs = TR[RS_FIELD].getValue();
        long rt = TR[RT_FIELD].getValue();

        // Perform modulo operation.
        long remainder = 0;

        try {
            remainder = rs % rt;
        } catch (ArithmeticException e) {
            if (cpu.isEnableForwarding()) {
                cpu.getRegister(params.get(RD_FIELD)).decrWriteSemaphore();
            }
            throw new DivisionByZeroException();
        }

        // Write result to temporary register.
        try {
            TR[RD_FIELD].writeDoubleWord(remainder);
        } catch (IrregularWriteOperationException e) {
            e.printStackTrace();
        }

        if (cpu.isEnableForwarding()) {
            doWB();
        }
    }

    public void WB() throws IrregularStringOfBitsException {
        if (!cpu.isEnableForwarding()) {
            doWB();
        }
    }

    public void pack() throws IrregularStringOfBitsException {
        // "SPECIAL" value of 000000.
        repr.setBits("000000", 0);
        repr.setBits(Converter.intToBin(RS_FIELD_LENGTH, params.get(RS_FIELD)), RS_FIELD_INIT);
        repr.setBits(Converter.intToBin(RT_FIELD_LENGTH, params.get(RT_FIELD)), RT_FIELD_INIT);
        repr.setBits(Converter.intToBin(RD_FIELD_LENGTH, params.get(RD_FIELD)), RD_FIELD_INIT);
        // sa and function fields, at the end.
        repr.setBits(OPCODE_VALUE, 6 + RT_FIELD_LENGTH + RS_FIELD_LENGTH + RD_FIELD_LENGTH);
    }
}
