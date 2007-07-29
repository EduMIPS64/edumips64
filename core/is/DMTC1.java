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


package edumips64.core.is;
import edumips64.core.*;
import edumips64.core.fpu.*;
import edumips64.utils.*;
import java.math.*;

/**
 * <pre>
 *      Format: DMTC1 rt, fs
 * Description: To copy a doubleword from a GPR to an FPR
 *   Operation: fs = rt
 *</pre>
 */
class DMTC1 extends FPMoveToInstructions {
	String OPCODE_VALUE="00101";
	String NAME = "DMTC1";
	
	public DMTC1() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		super.name=NAME;
	}	
	
	public void EX() throws IrregularStringOfBitsException {
		//getting values from temporary registers
		String value=TR[RT_FIELD].getBinString();
		TRfp[FS_FIELD].setBits(value,0);
		if(enableForwarding) {
			doWB();
		}
	}
	
}
