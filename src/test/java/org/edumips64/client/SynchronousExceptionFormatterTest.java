/* SynchronousExceptionFormatterTest.java
 *
 * Unit tests for SynchronousExceptionFormatter.
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
package org.edumips64.client;

import org.edumips64.BaseTest;
import org.edumips64.core.SynchronousException;
import org.edumips64.core.SynchronousExceptionCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SynchronousExceptionFormatter}, which is used by the
 * Web UI worker to turn a {@link SynchronousException} into a user-friendly
 * message.
 */
public class SynchronousExceptionFormatterTest extends BaseTest {

  @Test
  public void formatsIntegerOverflowWithInstructionAndStage() {
    SynchronousException e = new SynchronousException(SynchronousExceptionCode.INTOVERFLOW);
    e.setInstructionInfo("DADD R1,R2,R3", "EX");

    String message = SynchronousExceptionFormatter.format(e);

    assertEquals(
        "Integer overflow (INTOVERFLOW) caused by DADD R1,R2,R3 in stage EX",
        message);
  }

  @Test
  public void formatsDivisionByZeroWithInstructionAndStage() {
    SynchronousException e = new SynchronousException(SynchronousExceptionCode.DIVZERO);
    e.setInstructionInfo("DIV R1,R0", "EX");

    String message = SynchronousExceptionFormatter.format(e);

    assertEquals(
        "Division by zero (DIVZERO) caused by DIV R1,R0 in stage EX",
        message);
  }

  @Test
  public void formatsFpuExceptionsWithStructuredInfo() {
    assertTrue(SynchronousExceptionFormatter
        .format(new SynchronousException(SynchronousExceptionCode.FPOVERFLOW))
        .startsWith("FP overflow (FPOVERFLOW)"));
    assertTrue(SynchronousExceptionFormatter
        .format(new SynchronousException(SynchronousExceptionCode.FPUNDERFLOW))
        .startsWith("FP underflow (FPUNDERFLOW)"));
    assertTrue(SynchronousExceptionFormatter
        .format(new SynchronousException(SynchronousExceptionCode.FPINVALID))
        .startsWith("FP invalid operation (FPINVALID)"));
    assertTrue(SynchronousExceptionFormatter
        .format(new SynchronousException(SynchronousExceptionCode.FPDIVBYZERO))
        .startsWith("FP division by zero (FPDIVBYZERO)"));
  }

  @Test
  public void omitsInstructionSuffixWhenInfoMissing() {
    SynchronousException e = new SynchronousException(SynchronousExceptionCode.INTOVERFLOW);

    String message = SynchronousExceptionFormatter.format(e);

    assertEquals("Integer overflow (INTOVERFLOW)", message);
  }

  @Test
  public void handlesNullException() {
    assertEquals("", SynchronousExceptionFormatter.format(null));
  }
}
