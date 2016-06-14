/* FPUConfigurator.java
 *
 * This class gives the FPU current Local settings.
 * (c)Massimo Trubia 2007
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
package org.edumips64.utils;

import java.util.*;
public class FPUConfigurator {
  static LinkedList<String> fparithmetic, terminating;

  public FPUConfigurator() {
    fparithmetic = new LinkedList<>();
    terminating = new LinkedList<>();
    fparithmetic.add("ADD.D");
    fparithmetic.add("SUB.D");
    fparithmetic.add("DIV.D");
    fparithmetic.add("MUL.D");
    terminating.add("0000000C");    // SYSCALL 0
    terminating.add("04000000");    // HALT
  }

  public LinkedList<String> getFPArithmeticInstructions() {
    return fparithmetic;
  }

  public LinkedList<String> getTerminatingInstructions() {
    return terminating;
  }
}
