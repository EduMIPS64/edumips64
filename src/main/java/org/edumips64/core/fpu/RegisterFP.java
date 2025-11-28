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
 *  * Removed lock-related functions, in order to add read and write semaphores
 */
package org.edumips64.core.fpu;


import org.edumips64.core.Register;

/** This class models a 64-bit CPU's internal floating point register.
 * 
 * This class extends Register, which provides common register functionality
 * (writeSemaphore, WAWSemaphore, name, reset, toString). It adds the 
 * getFPDoubleValueAsString() method specific to floating point registers.
 * 
 * @author Massimo Trubia
 */
public class RegisterFP extends Register {

  /** Creates a default new instance of RegisterFP. */
  public RegisterFP(String name) {
    super(name);
  }

  /** Returns a string with a double value or the name of a special value.
   * It is recommended to use this method only for the visualisation of the double value 
   * because it may return an alphanumeric value.
   * @return the double value or the special values "Quiet NaN","Signaling NaN", 
   *         "Positive infinity", "Negative infinity","Positive zero","Negative zero"
   */
  public String getFPDoubleValueAsString() {
    return readDouble();
  }
}
