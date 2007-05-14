/* BitSet64FP.java
 *
 * This class models a 64-bit array, useful for registers and memory representation.
 * (c) 2006 Massimo Trubia
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
package edumips64.core;

//import edumips64.utils.*;
import java.util.BitSet;
import edumips64.core.fpu.*;

/** This class models a 64-bit array, useful for floating point registers
 * @author Massimo Trubia
 * */

public class BitSet64FP extends FixedBitSet {
	
	/** Creates a default new instance of BitSet64FP. */
	public BitSet64FP(){
		super();
		size = 64;
	}
	
	public void writeDouble(double value) throws FPExponentTooLargeException,FPUnderflowException,FPOverflowException
	{

		
	}
	
	public void writeDouble(String value)
	{
		
	}
	
	public double readDouble()
	{
		return 0;
	}
	

	
}
		
		

