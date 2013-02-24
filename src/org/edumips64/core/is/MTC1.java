/*
 * MTC1.java
 *
 * 25th july 2007
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
import org.edumips64.core.*;
import org.edumips64.core.fpu.*;
import org.edumips64.utils.*;
import java.math.*;

/**
 *<pre>
 *  Format: MTC1 rt, fs
 * Description: To copy a word from a GPR to an FPR
 *   Operation: fs.writeword_nosignextend(readword(rt))
 *</pre>
 */
class MTC1 extends FPMoveToInstructions {
  String OPCODE_VALUE = "00100";
  String NAME = "MTC1";

  public MTC1() {
    super.OPCODE_VALUE = OPCODE_VALUE;
    super.name = NAME;
  }

  public void EX() throws IrregularStringOfBitsException {
    //getting values from temporary registers
    String value = TR[RT_FIELD].getBinString();
    TRfp[FS_FIELD].setBits(value.substring(32, 64), 32);

    if (enableForwarding) {
      doWB();
    }
  }

}
