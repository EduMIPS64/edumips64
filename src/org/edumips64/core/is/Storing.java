/*
 * Storing.java
 *
 * 22th may 2006
 * Exception of the MIPS64 Instruction Set
 * (c) 2006 EduMips64 project - Trubia Massimo, Russo Daniele
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License. * *
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
import org.edumips64.utils.*;

import java.util.logging.Logger;


/** This is the base class for the storing instructions
 *
 * @author Massimo
 */
public abstract class Storing extends LDSTInstructions {
  protected static final Logger logger = Logger.getLogger(Storing.class.getName());
  protected Register rt;

  public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException, TwosComplementSumException {
    //if the base register and the rt register are valid passing value of rt register into a temporary register
    Register base = cpu.getRegister(params.get(BASE_FIELD));
    rt = cpu.getRegister(params.get(RT_FIELD));

    if (base.getWriteSemaphore() > 0) {
      logger.info("RAW in " + fullname + ": base register still needs to be written to.");
      throw new RAWException();
    }

    if (!enableForwarding) {
      if (rt.getWriteSemaphore() > 0) {
        logger.info("RAW in " + fullname + ": rt register still needs to be written to.");
        throw new RAWException();
      }

      TR[RT_FIELD].setBits(rt.getBinString(), 0);
    }

    //calculating  address (base+offset)
    long address = base.getValue() + params.get(OFFSET_FIELD);
    //saving address into a temporary register
    TR[OFFSET_PLUS_BASE].writeDoubleWord(address);
  }

  public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, NotAlignException {
    // Will fill in the address variable.
    super.EX();

    // Save memory access for Dinero trace file
    dinero.Store(Converter.binToHex(Converter.positiveIntToBin(64, address)), memoryOpSize);
  }

  public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException, NotAlignException, AddressErrorException, IrregularWriteOperationException {
    memEl = memory.getCellByAddress(address);

    if (enableForwarding) {
      TR[RT_FIELD].setBits(rt.getBinString(), 0);
    }

    doMEM();

    if (enableForwarding) {
      WB();
    }
  }

  public static void main(String args[]) {
    try {
      SD inst = new SD();
      //SH inst = new SH();
      //SW inst=new SW();
      //SB inst=new SB();
      CPU cpu = CPU.getInstance();
      inst.params.add(1);
      inst.params.add(8);
      inst.params.add(0);
      //R1=43524464456523452L
      cpu.getRegister(inst.params.get(RT_FIELD)).writeDoubleWord(9223372036854775807L);
      inst.pack();
      inst.ID();
      inst.MEM();
      System.out.println(memory);
    } catch (Exception e) {
      System.out.println("Eccezion: " + e);
      e.printStackTrace();
    }
  }
}
