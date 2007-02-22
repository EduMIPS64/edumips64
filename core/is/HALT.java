/*
 * HALT.java
 *
 *
 * 15th may 2006
 * Instruction of the MIPS64 Instruction Set
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

package edumips64.core.is;
import edumips64.core.*;
import edumips64.utils.*;

/** *Syntax:    HALT
 * Description: To terminate the program execution
 *               When an HALT instruction performs ID step, it notifies to CPU
 *              that all instructions in pipeline after HALT instruction must be
 *              ignored
 *Exceptions:   HaltException
 *</pre>
 * @author Trubia Massimo, Russo Daniele
 */
public class HALT extends Instruction {
    final String OPCODE_VALUE="000001";
    public HALT() {
        name = "HALT";
    }
    public void IF()
    {
	Dinero din=Dinero.getInstance();
        try
        {
            CPU cpu=CPU.getInstance();
            din.IF(Converter.binToHex(Converter.intToBin(64,cpu.getLastPC().getValue())));
        }
        catch(IrregularStringOfBitsException e)
        {
            e.printStackTrace();
        }
    }
    public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException, HaltException {
	  CPU.getInstance().setStatus(CPU.CPUStatus.STOPPING);
    }

    public void EX() throws HaltException, IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException {
    }

    public void MEM() throws HaltException, IrregularStringOfBitsException, MemoryElementNotFoundException, AddressErrorException, IrregularWriteOperationException {
    }

    public void WB() throws HaltException, IrregularStringOfBitsException {
		CPU.getInstance().setStatus(CPU.CPUStatus.HALTED);
		throw new HaltException();
    }

    public void pack() throws IrregularStringOfBitsException {
        repr.setBits(OPCODE_VALUE,0);
    }
    
}
