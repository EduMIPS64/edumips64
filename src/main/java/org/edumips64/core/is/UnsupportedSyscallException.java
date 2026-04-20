/*
 * UnsupportedSyscallException.java
 *
 * Thrown when a SYSCALL instruction is issued that cannot be supported by the
 * current simulator environment (e.g. a file-I/O syscall issued from the Web
 * UI, which has no filesystem) or when the syscall number is invalid.
 *
 * The exception is unchecked so that it propagates out of the CPU pipeline
 * stages without requiring changes to their method signatures. The simulator
 * front-ends catch it as a generic error, surface the message to the user and
 * stop execution.
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 */
package org.edumips64.core.is;

/**
 * Runtime exception raised when a SYSCALL cannot be executed in the current
 * environment. The message is meant to be shown directly to the user.
 */
public class UnsupportedSyscallException extends RuntimeException {
  public UnsupportedSyscallException(String message) {
    super(message);
  }
}
