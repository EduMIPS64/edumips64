/* RegisterFP.java
 *
 * This class models a 64-bit CPU's internal floating point register.
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
 *
 * 18/05/2006 - Andrea Spadaccini:
 * 	* Removed lock-related functions, in order to add read and write semaphores
 */
package edumips64.core;

import edumips64.utils.*;


/** This class models a 64-bit CPU's internal register.
 * @author Massimo Trubia
 */
public class RegisterFP extends BitSet64FP {
	private int writeSemaphore;
	private int readSemaphore;

	/** Creates a default new instance of Register. */
	public RegisterFP(){
		writeSemaphore = 0;
		readSemaphore = 0;
	}
	
	/** Returns the value of the semaphore
	 *  @return the numerical value of the semaphore
	 */
	public int getWriteSemaphore() {
		return writeSemaphore;
	}

	/** Returns the value of the semaphore
	 *  @return the numerical value of the semaphore
	 */
	public int getReadSemaphore() {
		return readSemaphore;
	}

	/** Increments the value of the semaphore
	 */
	public void incrReadSemaphore() {
		readSemaphore++;
	}
	
	/** Increments the value of the semaphore
	 */
	public void incrWriteSemaphore() {
		writeSemaphore++;
	}

	/** Decrements the value of the semaphore.
	 *  It throws a <code>RuntimeException</code> if the semaphore value gets below zero, because
	 *  the value becomes negative only in case of programming errors, and the EduMIPS64 team
	 *  doesn't make any programming error.
	 */
	public void decrWriteSemaphore() {
		if(--writeSemaphore < 0)
			throw new RuntimeException();
	}

	/** Decrements the value of the semaphore.
	 *  It throws a <code>RuntimeException</code> if the semaphore value gets below zero, because
	 *  the value becomes negative only in case of programming errors, and the EduMIPS64 team
	 *  doesn't make any programming error.
	 */
	public void decrReadSemaphore() {
		if(--readSemaphore < 0)
			throw new RuntimeException();
	}

	/** Returns the double value stored in the register
	 * @return double value stored in the register
	 */ 
	public double getValue(){
			return super.readDouble();
	}

	/** Reset the register and its associated semaphores
	 */
	public void reset() {
		super.reset(false);
		writeSemaphore = 0;
		readSemaphore = 0;
	}
		

	public String toString() {
		String s = new String();
		try {
			s = getHexString();
		} catch (IrregularStringOfBitsException e) {
			e.printStackTrace();
		} //Impossibile che si verifichi
		return s;
	}

}
