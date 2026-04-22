package org.edumips64.utils.io;

/**
 * A FileUtils implementation that does nothing.
 */
public class NullFileUtils extends FileUtils {
  @Override
  public String ReadFile(String filename) throws ReadException {
    return null;
  }

  @Override
  public String GetAbsoluteFilename(String filename) {
    return null;
  }

  @Override
  public String GetBasePath(String filename) {
    return null;
  }

  @Override
  public boolean isAbsolute(String filename) {
    return false;
  }

  @Override
  public boolean Exists(String filename) {
    return false;
  }

  @Override
  public Reader openReadOnly(String pathname) throws OpenException {
    // File I/O is not supported in environments that use NullFileUtils
    // (e.g. the web UI). Throw OpenException so that callers such as
    // SYSCALL handle the failure gracefully (return value -1) instead
    // of receiving a null Reader that causes unhandled exceptions on
    // later read/close operations.
    throw new OpenException();
  }

  @Override
  public Writer openWriteOnly(String pathname, boolean append) throws OpenException {
    // See openReadOnly(): file I/O is not supported when NullFileUtils
    // is in use, so we fail the open() call cleanly.
    throw new OpenException();
  }

  @Override
  public boolean supportsFileSystem() {
    // NullFileUtils is used in environments (e.g. the web UI) that do not
    // provide a filesystem. Signal that to callers so they can refuse
    // unsupported syscalls with a clear error.
    return false;
  }
}
