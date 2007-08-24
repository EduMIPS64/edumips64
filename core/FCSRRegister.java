/* FCSRRegister.java
 *
 * This class models the Floating Point Control and Status Register
 * (c) 2007 Massimo Trubia
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
import edumips64.utils.*;

/** This class models the Floating Point Control and Status Register
 * @author Massimo Trubia
 * */

public class FCSRRegister extends BitSet32 {
//SETTING PROPERTIES ----------------------------------------------------------	
	/** Sets the FCSR Enables bits (the numeration of the bitset bits goes in this way  0 1 .... 30 31 
	*		 31 30 29 28 27 26 25  | 24 | 23 | 22 21 | 20 19 18 |17 16 15 14 13 12 | 11 10 9 8 7 | 6 5 4 3 2 | 1 0
        *			 FCC           | FS | FCC|  Impl |    000   |        Cause     |    Enables  |   Flags   |  RM
	* 		  7  6  5  4  3  2  1  |       0				       |  V  Z O U I | V Z O U I
	* @param tag a string value between  V  Z O U I
	* @param value a binary value
	*/
	public void setFCSREnables(String tag, int value) throws IrregularStringOfBitsException{
		if(tag.compareToIgnoreCase("V")==0)
			setBits(String.valueOf(value),20);
		else if(tag.compareToIgnoreCase("Z")==0)
			setBits(String.valueOf(value),21);
		else if(tag.compareToIgnoreCase("O")==0)
			setBits(String.valueOf(value),22);
		else if	(tag.compareToIgnoreCase("U")==0)
			setBits(String.valueOf(value),23);
		else if	(tag.compareToIgnoreCase("I")==0) //not implemented
			setBits(String.valueOf(value),24);
		
	}

	/** Sets the flags bits of the FCSR
	* @param tag a string value between  V  Z O U I
	* @param value a binary value
	 */
	public void setFCSRFlags(String tag,int value) throws IrregularStringOfBitsException{
		if(tag.compareToIgnoreCase("V")==0)
			setBits(String.valueOf(value),25);
		else if(tag.compareToIgnoreCase("Z")==0)
			setBits(String.valueOf(value),26);
		else if(tag.compareToIgnoreCase("O")==0)
			setBits(String.valueOf(value),27);
		else if	(tag.compareToIgnoreCase("U")==0)
			setBits(String.valueOf(value),28);
		else if	(tag.compareToIgnoreCase("I")==0) // not implemented
			setBits(String.valueOf(value),29);
	}
	
	/** Sets the selected FCC bit of the FCSR 
	 * @param cc condition code is an int value in the range [0,7]
	 * @condition the binary value of the relative bit
	 */
	public void setFCSRConditionCode(int cc, int condition) throws IrregularStringOfBitsException
	{
		final int FCC0=8;
		final int DISCONTINUITY=1;
		final int OFFSET=FCC0-DISCONTINUITY;
		if(cc==0)
			setBits(String.valueOf(condition),FCC0);
		else
			setBits(String.valueOf(condition),OFFSET-cc);
	}

	/** Sets the current rouding mode
	 * @param rm a constant that belongs to the following values TO_NEAREST ,TOWARD_ZERO,TOWARDS_PLUS_INFINITY,TOWARDS_MINUS_INFINITY*/
	public void setFCSRRoundingMode(CPU.FPRoundingMode rm) throws IrregularStringOfBitsException{
		final int FCSR_RM_FIELD_INIT=30;
		switch(rm){
			case TO_NEAREST:
				setBits("00",FCSR_RM_FIELD_INIT);
				break;
			case TOWARD_ZERO:
				setBits("01",FCSR_RM_FIELD_INIT);
				break;
			case TOWARDS_PLUS_INFINITY:
				setBits("10",FCSR_RM_FIELD_INIT);
				break;
			case TOWARDS_MINUS_INFINITY:
				setBits("11",FCSR_RM_FIELD_INIT);
				break;
		}
	}
	
	/** Sets the floating point unit enabled exceptions
	 *  @param the exception name to set
	 *  @param boolean that is true in order to enable that exception or false for disabling it
	 */
	public  void setFPExceptions(CPU.FPExceptions exceptionName, boolean value) throws IrregularStringOfBitsException {
		switch(exceptionName){
			case DIVIDE_BY_ZERO:
				setFCSREnables("Z",(value==true)?1:0);
				break;
			case OVERFLOW:
				setFCSREnables("O",(value==true)?1:0);
				break;
			case UNDERFLOW:
				setFCSREnables("U",(value==true)?1:0);
				break;
			case INVALID_OPERATION:
				setFCSREnables("V",(value==true)?1:0);
				break;
		}
	}
	
	
// GETTING PROPERTIES ---------------------------------------------------------------------	
	/** Gets the selected flag bit of the FCSR
	* @param tag a string value between  V=Invalid  Z=Divide by zero O=Overflow U=Underflow I=Inexact (not implemented)
	 */
	public boolean getFCSREnables(String tag){
		if(tag.compareToIgnoreCase("V")==0)
			return (getBinString().charAt(20)=='1') ? true : false;
		if(tag.compareToIgnoreCase("Z")==0)
			return (getBinString().charAt(21)=='1') ? true : false;
		if(tag.compareToIgnoreCase("O")==0)
			return (getBinString().charAt(22)=='1') ? true : false;
		if(tag.compareToIgnoreCase("U")==0)
			return (getBinString().charAt(23)=='1') ? true : false;
		if(tag.compareToIgnoreCase("I")==0) //not implemented
			return (getBinString().charAt(24)=='1') ? true : false;
		return false;
	}
	
	/** Gets the flags bits of the FCSR
	* @param tag a string value between  V=Invalid  Z=Divide by zero O=Overflow U=Underflow I=Inexact (not implemented)
	 */
	public boolean getFCSRFlags(String tag){
		if(tag.compareToIgnoreCase("V")==0)
			return (getBinString().charAt(25)=='1') ? true : false;
		if(tag.compareToIgnoreCase("Z")==0)
			return (getBinString().charAt(26)=='1') ? true : false;
		if(tag.compareToIgnoreCase("O")==0)
			return (getBinString().charAt(27)=='1') ? true : false;
		if(tag.compareToIgnoreCase("U")==0)
			return (getBinString().charAt(28)=='1') ? true : false;
		if(tag.compareToIgnoreCase("I")==0) // not implemented
			return (getBinString().charAt(29)=='1') ? true : false;
		return false;
	}
	
	/** Gets the selected FCC bit of the FCSR 
	 * @param cc condition code is an int value in the range [0,7]
	 */
	public int getFCSRConditionCode(int cc)
	{
		final int FCC0=8;
		final int DISCONTINUITY=1;
		final int OFFSET=FCC0-DISCONTINUITY;
		if(cc==0)
			return (Integer.valueOf(getBinString().substring(FCC0,FCC0+1)));
		else
			return (Integer.valueOf(getBinString().substring(OFFSET-cc,OFFSET-cc+1)));
	}
	
	public CPU.FPRoundingMode getFCSRRoundingMode(){
		final int FCSR_RM_FIELD_INIT=30;
		if(getBinString().substring(FCSR_RM_FIELD_INIT,size).compareTo("00")==0)
			return CPU.FPRoundingMode.TO_NEAREST;
		if(getBinString().substring(FCSR_RM_FIELD_INIT,size).compareTo("01")==0)
			return CPU.FPRoundingMode.TOWARD_ZERO;
		if(getBinString().substring(FCSR_RM_FIELD_INIT,size).compareTo("10")==0)
			return CPU.FPRoundingMode.TOWARDS_PLUS_INFINITY;
		if(getBinString().substring(FCSR_RM_FIELD_INIT,size).compareTo("11")==0)
			return CPU.FPRoundingMode.TOWARDS_MINUS_INFINITY;
		return null;
	}
	
	/** Gets the floating point unit enabled exceptions
	 *  @return true if exceptionName is enabled, false in the other case
	 */
	public boolean getFPExceptions(CPU.FPExceptions exceptionName) {
		//return this.fpEnabledExceptions.get(exceptionName);
		switch(exceptionName){
			case DIVIDE_BY_ZERO:
				return getFCSREnables("Z");
			case OVERFLOW:
				return getFCSREnables("O");
			case UNDERFLOW:
				return getFCSREnables("U");
			case INVALID_OPERATION:
				return getFCSREnables("V");
		}
		return false;
	}
	
	
	
}
		
		

