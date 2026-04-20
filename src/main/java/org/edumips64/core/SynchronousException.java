/* SynchronousException.java
 *
 * Models a synchronous exception.
 * (c) 2006 Andrea Spadaccini
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

/** Models a synchronous exception.
 *
 * In addition to a short error code (used for looking up localised messages),
 * a synchronous exception can optionally carry information about the
 * instruction that caused it and the pipeline stage in which it was raised.
 * This extra context is attached by the CPU when the exception is detected
 * and is used by the UI to produce more informative error messages.
 */
public class SynchronousException extends Exception {
  private static final long serialVersionUID = -5084570924398502491L;
  private final String errcode;
  private String instructionName;
  private String stage;

  /** Gets the error code */
  public String getCode() {
    return errcode;
  }

  /** Gets the full name of the instruction that caused the exception, or null if not set. */
  public String getInstructionName() {
    return instructionName;
  }

  /** Gets the pipeline stage in which the exception was raised, or null if not set. */
  public String getStage() {
    return stage;
  }

  /** Attaches information about the instruction and pipeline stage that caused the exception. */
  public void setInstructionInfo(String instructionName, String stage) {
    this.instructionName = instructionName;
    this.stage = stage;
  }

  @Override
  public String getMessage() {
    if (instructionName != null && stage != null) {
      return errcode + " (caused by " + instructionName + " in stage " + stage + ")";
    }
    return errcode;
  }

  public SynchronousException(String errcode) {
    super(errcode);
    this.errcode = errcode;
  }
}


