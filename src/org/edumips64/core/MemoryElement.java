/* MemoryElement.java
 *
 * This class models a 64-bit memory location.
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

import org.edumips64.utils.*;
/** This class models a 64-bit memory location with a given address.
 * @author Salvatore Scellato
 * */
public class MemoryElement extends BitSet64{
	private int address;
	private String comment;
	private String label;
	private String code;

	/** Creates a new MemoryElement with given address.
	 * @param address address of the MemoryElement
	 */
	public MemoryElement(int address){
		super();
		this.address = address;
		comment = "";
		label = "";
		code = "";
	}

	/** Returns the address of this MemoryElement
	 * @return address of the MemoryElement
	 */
	public int getAddress(){
		return address;
	}

	/** Returns the comment related to this MemoryElement
	 * @return comment of this MemoryElement
	 */
	public String getComment(){
		return comment;
	}
	
	/** Sets the comment related to this MemoryElement
	 * @param comment brief description of this MemoryElement
	 */
	public void setComment(String comment){
		this.comment = comment;
	}
	
	/** Returns the label of this MemoryElement
	 * @return label of the MemoryElement
	 */
	public String getLabel(){
		return label;
	}
	
	/** Sets the label related to this MemoryElement
	 * @param label label of this MemoryElement
	 */
	public void setLabel(String label){
		this.label = label;
	}


	/** Returns the code of this MemoryElement
	 * @return code of the MemoryElement
	 */
	public String getCode(){
		return code;
	}
		
	/** Sets the code related to this MemoryElement
	 * @param code code of this MemoryElement
	 */
	
	public void setCode(String code){
		this.code = code;
	}

	/** Returns the signed numeric decimal value stored in the 64 bits of this MemoryElement: basically
	 * this method performs a conversion from binary value to decimal value.
	 * @return signed numerical value stored in this MemoryElement.
	 */ 
	public long getValue(){
		try{
			return Converter.binToLong(this.getBinString(),false);
		}
		catch(IrregularStringOfBitsException e){
			System.err.println("Errore in un registro");
			this.reset(false); //azzeriamo il registro
			return 0;
		}
	}
	
	/** Returns a string represention of this MemoryElement, formatted with the address and
	 * the value (in hexadecimal digits).
	 * That is, if the address is 16 and the value stored within is 256 the string will be 
	 * <code>"ADDRESS 10, VALUE 0000000000000100"</code>.
	 *
	 * @return representation of this MemoryElement
	 */
	public String toString() {
		try{
			String s = "ADDRESS " + Converter.binToHex(Converter.positiveIntToBin(32,this.getAddress()));
			s += ", VALUE " + Converter.binToHex(this.getBinString());
			s += ", LABEL  " + this.getLabel();
			s += ", CODE " + this.getCode();
			s += ", COMMENT " + this.getComment();
			return s;
		}
		catch(IrregularStringOfBitsException e){
			e.printStackTrace();

		}
		return "ERRORE";
	}

	public static void main(String[] args) throws Exception{
		MemoryElement[] memory = new MemoryElement[64];
		java.util.Random rand = new java.util.Random();
		int index = 0;
		java.util.List<BitSet64> list = new java.util.ArrayList<BitSet64>();
		for(MemoryElement me : memory){
			me = new MemoryElement(index*8);
			int value = rand.nextInt(65536) - 32768;
			me.writeHalf(value);
			System.out.println("\nValue: " + value);
			System.out.println("MemoryElement " + index);
			System.out.println("Address " + me.getAddress());
			System.out.println("String: " + me.getBinString());
			//System.out.println("Unsigned value: " + r.getValueUnsigned());
			System.out.println("Signed value: " + me.getValue());
			index++;
			list.add(me);
		}
	}
}

 
	
