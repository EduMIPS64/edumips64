/* SynchronousExceptionFormatter.java
 *
 * Translates a SynchronousException into a user-friendly message for the Web UI.
 *
 * (c) 2026 Andrea Spadaccini and the EduMIPS64 project.
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

import org.edumips64.core.SynchronousException;
import org.edumips64.core.SynchronousExceptionCode;

/**
 * Converts a {@link SynchronousException} into a user-friendly message for the
 * Web UI. The Swing UI uses {@code CurrentLocale} to look up localised
 * messages; in the Web UI we don't have access to that infrastructure, so this
 * class renders a short English description from the exception code and
 * appends information about the faulting instruction and pipeline stage when
 * available.
 */
public final class SynchronousExceptionFormatter {
  private SynchronousExceptionFormatter() {}

  /**
   * Builds a user-friendly message for the given exception, of the form
   * {@code "<description> (<code>) caused by <instr> in stage <stage>"}.
   * The instruction / stage suffix is omitted when the exception does not
   * carry that information.
   */
  public static String format(SynchronousException e) {
    if (e == null) {
      return "";
    }
    SynchronousExceptionCode code = e.getCode();
    StringBuilder sb = new StringBuilder();
    if (code == null) {
      sb.append("Synchronous exception");
    } else {
      sb.append(code.getDescription()).append(" (").append(code.name()).append(")");
    }
    if (e.getInstructionName() != null && e.getStage() != null) {
      sb.append(" caused by ")
        .append(e.getInstructionName())
        .append(" in stage ")
        .append(e.getStage());
    }
    return sb.toString();
  }
}
