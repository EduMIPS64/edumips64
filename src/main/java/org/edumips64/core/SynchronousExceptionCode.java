/* SynchronousExceptionCode.java
 *
 * Enumeration of the synchronous exception types supported by the CPU.
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
package org.edumips64.core;

/**
 * Enumeration of the synchronous exception types that the CPU can raise.
 *
 * <p>Each value pairs a short symbolic code (the {@link #name() name}) with a
 * human-readable English description. The symbolic name is used both as a
 * stable wire-format identifier (e.g. serialized into {@code Result.errorCode}
 * for the Web UI) and as the prefix of the localized Swing message key
 * ({@code &lt;NAME&gt;.Message}) in {@code CurrentLocaleMessages*.properties}.
 * The description is used by the Web UI, which has no access to the Swing
 * localization infrastructure.
 */
public enum SynchronousExceptionCode {
  DIVZERO("Division by zero"),
  INTOVERFLOW("Integer overflow"),
  FPOVERFLOW("FP overflow"),
  FPUNDERFLOW("FP underflow"),
  FPINVALID("FP invalid operation"),
  FPDIVBYZERO("FP division by zero"),
  UNSUPPORTED_SYSCALL("Unsupported system call");

  private final String description;

  SynchronousExceptionCode(String description) {
    this.description = description;
  }

  /** Returns a short, human-readable English description of the exception. */
  public String getDescription() {
    return description;
  }
}
