/*
 * FPArithmeticInstructions.java
 *
 * 30th may 2007
 * (c) 2006 EduMips64 project - Trubia Massimo
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
import edumips64.core.fpu.*;
//per diagnostica
import java.util.*;

/**This is the base class for the floatiing point arithmetic instructions
 *
 * @author Trubia Massimo
 */
public abstract class FPArithmeticInstructions extends ALUInstructions {
    final static int FD_FIELD=0;
    final static int FS_FIELD=1;
    final static int FT_FIELD=2;
    static String COP1_FIELD="010001";
    final static int COP1_FIELD_INIT=0;
    final static int FD_FIELD_INIT=21;
    final static int FS_FIELD_INIT=16;
    final static int FT_FIELD_INIT=11;
    final static int FD_FIELD_LENGTH=5;
    final static int FS_FIELD_LENGTH=5;
    final static int FT_FIELD_LENGTH=5;
    final static int OPCODE_VALUE_INIT=26;    
    final static int FMT_FIELD_INIT=6;

    String OPCODE_VALUE="";
    String FMT_FIELD="";

    public FPArithmeticInstructions() 
    {
        syntax="%F,%F,%F";
        paramCount=3;         
    }

    public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException, WAWException {
        //if source registers are valid passing their own values into temporary registers
        RegisterFP fs=cpu.getRegisterFP(params.get(FS_FIELD));
        RegisterFP ft=cpu.getRegisterFP(params.get(FT_FIELD));
        if(fs.getWriteSemaphore()>0 || ft.getWriteSemaphore()>0)
            throw new RAWException();
        TRfp[FS_FIELD].setBits(fs.getBinString(),0);
        TRfp[FT_FIELD].setBits(ft.getBinString(),0);
        //locking the destination register
        RegisterFP fd=cpu.getRegisterFP(params.get(FD_FIELD));
	if(fd.getWriteSemaphore()>0)
		throw new WAWException();
        fd.incrWriteSemaphore(); 
    }

    public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException,IrregularWriteOperationException,DivisionByZeroException,FPInvalidOperationException,FPExponentTooLargeException,FPUnderflowException,FPOverflowException, FPDivideByZeroException {
    }

    public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
    }

    public void WB() throws IrregularStringOfBitsException 
    {  
	 if(!enableForwarding)
	    doWB();
    }

    public void doWB() throws IrregularStringOfBitsException 
    {
       //passing result from temporary register to destination register and unlocking it
        cpu.getRegisterFP(params.get(FD_FIELD)).setBits(TRfp[FD_FIELD].getBinString(),0);
        cpu.getRegisterFP(params.get(FD_FIELD)).decrWriteSemaphore();    
        
    }

    public void pack() throws IrregularStringOfBitsException 
    {
        //conversion of instruction parameters of "params" list to the "repr" form (32 binary value) 
        repr.setBits(OPCODE_VALUE,OPCODE_VALUE_INIT);
        repr.setBits(Converter.intToBin(FS_FIELD_LENGTH,params.get(FS_FIELD)),FS_FIELD_INIT);
        repr.setBits(Converter.intToBin(FT_FIELD_LENGTH,params.get(FT_FIELD)),FT_FIELD_INIT);
        repr.setBits(Converter.intToBin(FD_FIELD_LENGTH,params.get(FD_FIELD)),FD_FIELD_INIT);
	repr.setBits(COP1_FIELD,COP1_FIELD_INIT);
	repr.setBits(FMT_FIELD,FMT_FIELD_INIT);
    }
    
}
