/* FixedBitSet.java
 *
 * Abstract class, used for the derivation of the basic data structure of
 * registers, memory, instructions.
 * (c) 2006 Salvatore Scellato
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
package org.edumips64.core;

import java.util.BitSet;
import org.edumips64.utils.*;
/** Abstract class: it contains a fixed-size BitSet instance. 
 * @author Salvatore Scellato
 * */
public abstract class FixedBitSet {
	private BitSet bitset;
	protected int size;

	/** Creates a default new instance of FixedBitSet with zero size. */
	public FixedBitSet(){
		bitset = new BitSet();
		size = 0;
	}

	/** Resets this FixedBitSet, setting all bits to one if value is true and setting all bits to zero
	 * if value is false
	 * @param value if true bits will be set to '1', if false bits will be set to '0'
	 * */
	public void reset(boolean value){
		if(!value)
			bitset.clear();
		else
			bitset.set(0,size); //imposta tutto a true
	}
	
	/** Using a string containg binary digits (bits) this method sets the bit
	 * of the FixedBitSet starting from the <code>start</code> position until reaching
	 * the end of the string or the end of the FixedBitSet.
	 * @param bits string made of "0" and "1" chars
	 * @param start index of the first bit to be set
	 * @throws IrregularStringOfBitsException if the String bits does not contain only "0" and "1" chars
	 */
	public void setBits(String bits, int start) throws IrregularStringOfBitsException{
		//System.err.println("setBits() " + bits + ", " + start);
		int index = 0;
		for(int i = 0; i < bits.length(); i++){
			index = i + start;
			if( index >= size )
				return;
			char c = bits.charAt(i);
			switch(c){
				case '1': bitset.set(index, true); break;
				case '0': bitset.set(index,false); break;
				default: throw new IrregularStringOfBitsException(); 
			}
		}
	}

	/** Returns the bit sequence of this FixedBitSet as a string containing "0"s and "1"s.
	 * @return string form of the bit sequence stored in this FixedBitSet
	 */
	public String getBinString(){
		StringBuffer buf = new StringBuffer(size);
		for(int i = 0; i < size; i++)
			if(bitset.get(i))
				buf.append("1");
			else
				buf.append("0");
		
		return new String(buf);
	}		

	/** Returns the bit sequence of this FixedBitSet as a string containing hexadecimal
	 * digits.
	 * @return string form of the bit sequence stored in this FixedBitSet as hexadecimal digits
	 * @throws IrregularStringOfBitsException if the bit sequence is not well-formed
	 */
	public String getHexString() throws IrregularStringOfBitsException{
		return Converter.binToHex(this.getBinString());
	}

	//public void setBits(String string) {
	//	throw new UnsupportedOperationException("Not yet implemented");
	//}
}

