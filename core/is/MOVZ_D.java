/*
 * MOVZ_D.java
 *
 * 27th may 2007
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
 *<pre>
 *	Format: MOVZ.D fd, fs, rt
 * Description: To test a GPR then conditionally move an FP value
 *   Operation: if rt = 0 then fd=fs
 *</pre>
 */
class MOVZ_D extends FPConditionalZerosMoveInstructions {
	String OPCODE_VALUE="010010";
	String FMT_FIELD="10001"; //DOUBLE IS 17
	String NAME = "MOVZ.D";
	
	public MOVZ_D() {
		super.OPCODE_VALUE = OPCODE_VALUE;
		super.FMT_FIELD = FMT_FIELD;
		super.name=NAME;
	}
	
	public void EX() throws IrregularStringOfBitsException {
		//getting values from temporary registers
		String rt=TR[RT_FIELD].getBinString();
		String fs=TRfp[FS_FIELD].getBinString();
		
		if(rt.matches("[0]{64}"))
			TRfp[FD_FIELD].setBits(fs,0);
		if(enableForwarding) {
			doWB();
		}
	}
}
