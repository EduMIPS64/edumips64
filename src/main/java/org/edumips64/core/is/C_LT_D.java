/*
 * C_LT_D.java
 *
 * 19th july 2007
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
 *  Format: C.LT.D cc, fs, ft
 * Description: To compare FP values (less than) and record the Boolean result in a condition code in the range [0,7] stored on the FCSR as binary value
 *   Operation: if fs<ft FCSR[cc]=1 else FCSR[cc]=0
 *</pre>

 */
class C_LT_D extends FPC_cond_DInstructions {
  String COND_VALUE = "0100"; // the first bit doesn't mean anything into this simulator
  // (100)bin=(4)dec is the condition code assigned to the predicate less than in a MIPS64 processor
  String NAME = "C.LT.D";

  C_LT_D() {
    super.COND_VALUE = COND_VALUE;
    super.name = NAME;
  }
}
