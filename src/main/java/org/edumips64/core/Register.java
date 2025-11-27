/* Register.java
 *
 * This class models a 64-bit CPU's internal register.
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
 *
 * 18/05/2006 - Andrea Spadaccini:
 *  * Removed lock-related functions, in order to add read and write semaphores
 */
package org.edumips64.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/** This class models a 64-bit CPU's internal register.
 * Supports both integer and floating point registers.
 * @author Salvatore Scellato
 */
public class Register extends BitSet64 {
  private int writeSemaphore;
  private int WAWSemaphore;
  private String reg_name, reg_alias;

  public final static Logger logger = Logger.getLogger(Register.class.getName());

  /** Creates a new instance of Register.
     *  @param name name of the register (for debugging purposes).
     */
  public Register(String name) {
    writeSemaphore = 0;
    WAWSemaphore = 0;
    reg_name = name;
    
    reg_alias = "";
    if (registerAliases.containsKey(name.toUpperCase())) {
      reg_alias = registerAliases.get(name.toUpperCase());
    }
  }

  public String getName() {
    return reg_name;
  }

  public String getAlias() {
    return reg_alias;
  }

  /** Returns the value of the semaphore
   *  @return the numerical value of the semaphore
   */
  public int getWriteSemaphore() {
    return writeSemaphore;
  }

  /** Returns the value of the WAW semaphore
   *  @return the numerical value of the WAW semaphore
   */
  public int getWAWSemaphore() {
    return WAWSemaphore;
  }

  /** Increments the value of the semaphore
   */
  public void incrWriteSemaphore() {
    writeSemaphore++;
    logger.info("Incremented write semaphore for " + reg_name + ": " + writeSemaphore);
  }

  /** Increments the value of the WAW semaphore
   */
  public void incrWAWSemaphore() {
    WAWSemaphore++;
  }

  /** Decrements the value of the semaphore.
   *  It throws a <code>RuntimeException</code> if the semaphore value gets below zero, because
   *  the value becomes negative only in case of programming errors, and the EduMIPS64 team
   *  doesn't make any programming error.
   */
  public void decrWriteSemaphore() {
    if (--writeSemaphore < 0) {
      throw new RuntimeException();
    }

    logger.info("Decremented write semaphore for " + reg_name + ": " + writeSemaphore);
  }

  /** Decrements the value of the WAW semaphore.
   *  It throws a <code>RuntimeException</code> if the semaphore value gets below zero, because
   *  the value becomes negative only in case of programming errors, and the EduMIPS64 team
   *  doesn't make any programming error.
   */
  public void decrWAWSemaphore() {
    if (--WAWSemaphore < 0) {
      throw new RuntimeException("WAW semaphore for " + reg_name + " reached a negative value.");
    }
  }

  /** Returns the signed numeric decimal value stored in this register.
   * @return signed numerical value stored in this register
   */
  public long getValue() {
    try {
      return Converter.binToLong(this.getBinString(), false);
    } catch (IrregularStringOfBitsException e) {
      e.printStackTrace();
      this.reset(false);  //azzeriamo il registro
      return 0;
    }
  }

  /** Returns a string with a double value or the name of a special value.
   * It is recommended to use this method only for the visualisation of the double value 
   * because it may return an alphanumeric value.
   * @return the double value or the special values "Quiet NaN","Signaling NaN", 
   *         "Positive infinity", "Negative infinity","Positive zero","Negative zero"
   */
  public String getValueString() {
    return readDouble();
  }

  /** Reset the register and its associated semaphores
   */
  public void reset() {
    super.reset(false);
    writeSemaphore = 0;
    WAWSemaphore = 0;
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
  
  // Used to derive register aliases.
  private static Map<String, String> registerAliases;
  static {
    registerAliases = new HashMap<>();
    registerAliases.put("R0", "zero");
    registerAliases.put("R1", "at");
    registerAliases.put("R2", "v1");
    registerAliases.put("R3", "v1");
    registerAliases.put("R4", "a0");
    registerAliases.put("R5", "a1");
    registerAliases.put("R6", "a2");
    registerAliases.put("R7", "a3");
    registerAliases.put("R8", "t0");
    registerAliases.put("R9", "t1");
    registerAliases.put("R10", "t2");
    registerAliases.put("R11", "t3");
    registerAliases.put("R12", "t4");
    registerAliases.put("R13", "t5");
    registerAliases.put("R14", "t6");
    registerAliases.put("R15", "t7");
    registerAliases.put("R16", "s0");
    registerAliases.put("R17", "s1");
    registerAliases.put("R18", "s2");
    registerAliases.put("R19", "s3");
    registerAliases.put("R20", "s4");
    registerAliases.put("R21", "s5");
    registerAliases.put("R22", "s6");
    registerAliases.put("R23", "s7");
    registerAliases.put("R24", "t8");
    registerAliases.put("R25", "t9");
    registerAliases.put("R26", "k0");
    registerAliases.put("R27", "k1");
    registerAliases.put("R28", "gp");
    registerAliases.put("R29", "sp");
    registerAliases.put("R30", "fp");
    registerAliases.put("R31", "ra");
  }
}
