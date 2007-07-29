/*
 * ALU_RType.java
 *
 * 5th may 2006
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

package edumips64.core.is;

import edumips64.core.*;
import edumips64.utils.*;
//per diagnostica
import java.util.*;
import java.util.logging.Logger;

/**This is the base class for the R-Type instructions
 *
 * @author Trubia Massimo, Russo Daniele
 */
public abstract class ALU_RType extends ALUInstructions {
    final static int RD_FIELD=0;
    final static int RS_FIELD=1;
    final static int RT_FIELD=2;
    final static int RD_FIELD_INIT=16;
    final static int RS_FIELD_INIT=6;
    final static int RT_FIELD_INIT=11;
    final static int RD_FIELD_LENGTH=5;
    final static int RS_FIELD_LENGTH=5;
    final static int RT_FIELD_LENGTH=5; 
    String OPCODE_VALUE="";
    final static int OPCODE_VALUE_INIT=26;
    private static final Logger logger = Logger.getLogger(ALU_RType.class.getName());
    public ALU_RType() {
        syntax="%R,%R,%R";
        paramCount=3;         
    }

    public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException {
        //if source registers are valid passing their own values into temporary registers
        logger.info("Executing step ID of " + fullname);
        logger.info("RD is R" + params.get(RD_FIELD) + "; RS is R" + params.get(RS_FIELD) + "; RT is R" + params.get(RT_FIELD) + ";");
        Register rs=cpu.getRegister(params.get(RS_FIELD));
        Register rt=cpu.getRegister(params.get(RT_FIELD));
        //if(rs.getWriteSemaphore()>0 || rt.getWriteSemaphore()>0)
        //    throw new RAWException();
        if(rs.getWriteSemaphore() > 0) {
            logger.info("RAW on RS");
            throw new RAWException();
        }
        if(rt.getWriteSemaphore() > 0) {
            logger.info("RAW on RT");
            throw new RAWException();
        }

        TR[RS_FIELD].setBits(rs.getBinString(),0); 
        TR[RT_FIELD].setBits(rt.getBinString(),0); 

        // Get the Destination Register value.
        // BE CAREFUL! If the instruction does not use RD (like MOVN and MOVZ
        // if the condition is false), and someone changes the value of RD
        // between the ID and the WB stage of the current instruction, the old
        // value of RD, read during ID, will be written to RD during WB.
        Register rd=cpu.getRegister(params.get(RD_FIELD));
        TR[RD_FIELD].setBits(rd.getBinString(),0); 

        // Lock RD
        rd.incrWriteSemaphore(); 
        logger.info("RD = " + TR[RD_FIELD].getValue() + "; RS = " + TR[RS_FIELD].getValue() + "; RT = " + TR[RT_FIELD].getValue() + ";");
    }

    public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException,IrregularWriteOperationException,DivisionByZeroException {
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
        cpu.getRegister(params.get(RD_FIELD)).setBits(TR[RD_FIELD].getBinString(),0);
        cpu.getRegister(params.get(RD_FIELD)).decrWriteSemaphore();    
        
    }

    public void pack() throws IrregularStringOfBitsException 
    {
        //conversion of instruction parameters of "params" list to the "repr" form (32 binary value) 
        repr.setBits(OPCODE_VALUE,OPCODE_VALUE_INIT);
        repr.setBits(Converter.intToBin(RS_FIELD_LENGTH,params.get(RS_FIELD)),RS_FIELD_INIT);
        repr.setBits(Converter.intToBin(RT_FIELD_LENGTH,params.get(RT_FIELD)),RT_FIELD_INIT);
        repr.setBits(Converter.intToBin(RD_FIELD_LENGTH,params.get(RD_FIELD)),RD_FIELD_INIT);
    }
    
    public static void main(String[] args)
    {
 //DEBUGGING DADD DADDU DSUB DSUBU
        //DSUB ins=new DSUB();
        DADD ins=new DADD();
        //DADDU ins=new DADDU();
        //SLT ins=new SLT();
        //SLTU ins=new SLTU();
        //OR ins=new OR();
        //DSLL ins=new DSLL();
        //AND ins=new AND();
        //DSRL ins=new DSRL();
        //DSLLV ins=new DSLLV();
        //DSRLV ins=new DSRLV();
        //DSRA ins=new DSRA();
        //DSRAV ins = new DSRAV();
        
        List<Integer>params=new Vector<Integer>();
        int rd=2;
        int rs=3;
        int rt=30;
        params.add(rd);  //destinazione R2
        params.add(rs);  //sorgente1 R3
        params.add(rt);  //sorgente2 R4
        try
        {
           cpu.getRegister(rs).writeDoubleWord(82337254775807L); //rs register
           cpu.getRegister(rt).writeDoubleWord(-76284523455L);     //rt register
           ins.setParams(params);
        }
        catch(IrregularWriteOperationException e)
        {
			e.printStackTrace();
		}
	}
	
}
