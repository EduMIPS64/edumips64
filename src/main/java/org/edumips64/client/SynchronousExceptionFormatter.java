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
import org.edumips64.utils.CurrentLocale;

/**
 * Converts a {@link SynchronousException} into a user-friendly message for the
 * Web UI. The message text is localized through {@link CurrentLocale} (the GWT
 * super-source provider reads the same {@code CurrentLocaleMessages*.properties}
 * files as the Swing UI), reusing the existing {@code <CODE>.Message},
 * {@code SYNCEX.CAUSE} and {@code SYNCEX.STAGE} keys. The faulting instruction
 * and pipeline stage are appended when available.
 */
public final class SynchronousExceptionFormatter {
  private SynchronousExceptionFormatter() {}

  /**
   * Builds a user-friendly, localized message for the given exception, of the
   * form {@code "<description> (<code>) <cause> <instr>, <stage> <stage>"}.
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
      sb.append(localize("SYNCEX.GENERIC", "Synchronous exception"));
    } else {
      sb.append(localize(code.name() + ".Message", code.getDescription()))
        .append(" (").append(code.name()).append(")");
    }
    if (e.getInstructionName() != null && e.getStage() != null) {
      sb.append(" ")
        .append(localize("SYNCEX.CAUSE", "Caused by instruction:"))
        .append(" ")
        .append(e.getInstructionName())
        .append(", ")
        .append(localize("SYNCEX.STAGE", "stage:"))
        .append(" ")
        .append(e.getStage());
    }
    return sb.toString();
  }

  /**
   * Looks up {@code key} via {@link CurrentLocale}. Because that facade falls
   * back to returning the key itself when no translation exists, we treat a
   * result equal to the key as "missing" and use {@code fallback} instead, so
   * the UI never shows a raw message key.
   */
  private static String localize(String key, String fallback) {
    String value = CurrentLocale.getString(key);
    return (value == null || value.equals(key)) ? fallback : value;
  }
}
