/*
 * BC1T.java
 *
 * 20th july, 2007
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
import org.edumips64.utils.*;
/** 
   *<pre>
 *	Format: BC1T cc, offset
 * Description: To test an FP condition code  in the range [0,7] previously stored from c.cond.fmt instructions
 *		on the FCSR as binary value and do a PC-relative conditional branch
 *   Operation: if FCSR[cc] = 1 then branch_to_offset
 *</pre>

 */

public class BC1T extends FPConditionalBranchesInstructions {
	static String ND_FIELD="0";
	static String TF_FIELD="1";
	static String NAME="BC1T";
	
	public BC1T() {
		super.ND_FIELD = ND_FIELD;
		super.TF_FIELD = TF_FIELD;
		super.name=NAME;
	}
	
	public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException,TwosComplementSumException, JumpException {
		boolean condition = (cpu.getFCSRConditionCode(params.get(CC_FIELD))==1)?true:false;
		
		//converting offset into a signed binary value of 64 bits in length
		BitSet64 bs=new BitSet64();
		bs.writeHalf(params.get(OFFSET_FIELD));
		String offset=bs.getBinString();
		
		if(condition) {
			String pc_new="";
			Register pc=cpu.getPC();
			String pc_old=cpu.getPC().getBinString();
			
			//subtracting 4 to the pc_old temporary variable using bitset64 safe methods
			BitSet64 bs_temp=new BitSet64();
			bs_temp.writeDoubleWord(-4);
			pc_old=InstructionsUtils.twosComplementSum(pc_old,bs_temp.getBinString());
			
			//updating program counter
			pc_new=InstructionsUtils.twosComplementSum(pc_old,offset);
			pc.setBits(pc_new,0);
			
			throw new JumpException();
		}
	}
	
	
}
