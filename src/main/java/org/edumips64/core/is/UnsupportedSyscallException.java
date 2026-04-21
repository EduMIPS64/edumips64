/*
 * UnsupportedSyscallException.java
 *
 * Thrown when a SYSCALL instruction is issued that cannot be supported by the
 * current simulator environment (e.g. a file-I/O syscall issued from the Web
 * UI, which has no filesystem) or when the syscall number is invalid.
 *
 * The exception is unchecked so that it propagates out of the CPU pipeline
 * stages without requiring changes to their method signatures. The simulator
 * front-ends catch it, surface a rich message to the user built from the
 * carried SynchronousException-style metadata (code, instruction, stage), and
 * stop execution.
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 */
package org.edumips64.core.is;

import org.edumips64.core.SynchronousException;
import org.edumips64.core.SynchronousExceptionCode;

/**
 * Runtime exception raised when a SYSCALL cannot be executed in the current
 * environment. Alongside a user-facing message, the exception carries
 * metadata compatible with {@link SynchronousException} (an error code, the
 * offending instruction name, and the pipeline stage where it was detected)
 * so the Web UI can compose a rich, structured alert via
 * {@code ResultFactory.AddRuntimeErrors}.
 */
public class UnsupportedSyscallException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final SynchronousExceptionCode code;
  private final String instructionName;
  private final String stage;

  public UnsupportedSyscallException(String message, String instructionName, String stage) {
    super(message);
    this.code = SynchronousExceptionCode.UNSUPPORTED_SYSCALL;
    this.instructionName = instructionName;
    this.stage = stage;
  }

  public SynchronousExceptionCode getCode() {
    return code;
  }

  public String getInstructionName() {
    return instructionName;
  }

  public String getStage() {
    return stage;
  }

  /**
   * Returns a {@link SynchronousException} view of this exception, populated
   * with the same code, instruction name and stage. Useful for reusing the
   * existing runtime-error plumbing (e.g.
   * {@code ResultFactory.AddRuntimeErrors}) without having to change the
   * pipeline-stage method signatures to declare a checked exception.
   */
  public SynchronousException toSynchronousException() {
    SynchronousException sx = new SynchronousException(code);
    if (instructionName != null && stage != null) {
      sx.setInstructionInfo(instructionName, stage);
    }
    return sx;
  }
}
