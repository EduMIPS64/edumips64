/*
 * DMTC1.java
 *
 * 17th july 2007
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


package org.edumips64.core.is;
import org.edumips64.utils.*;

/**
 *<pre>
 *  Format: DMFC1 rt,fs
 * Description: To move a doubleword from an FPR to a GPR.
 *   Operation: rt=fs
 *</pre>
 */
class DMFC1 extends FPMoveFromInstructions {
  String OPCODE_VALUE = "00001";
  String NAME = "DMFC1";

  DMFC1() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    super.name = NAME;
  }


  public void EX() throws IrregularStringOfBitsException {
    //getting values from temporary registers
    String value = TRfp[FS_FIELD].getBinString();
    TR[RT_FIELD].setBits(value, 0);

    if (isEnableForwarding()) {
      doWB();
    }
  }


}
