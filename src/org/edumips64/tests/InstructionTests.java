/* InstructionTests.java
 *
 * Tests for the EduMIPS64 instructions.
 *
 * (c) 2012-2013 Andrea Spadaccini
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
package org.edumips64.tests;

import org.edumips64.core.is.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class InstructionTests {

  /* Test the Instruction.equals() method. */
  @Test
  public void testEquals() {
    SYSCALL halt = new SYSCALL();
    halt.getParams().add(0);

    assertTrue(halt.equals(halt));
    assertFalse(halt.equals(null));

    SYSCALL anotherHalt = new SYSCALL();
    anotherHalt.getParams().add(0);

    assertTrue(halt.equals(anotherHalt));
    assertTrue(anotherHalt.equals(halt));

    Instruction lastHalt = Instruction.buildInstruction("SYSCALL");
    lastHalt.getParams().add(0);
    assertTrue(halt.equals(lastHalt));
  }
}
