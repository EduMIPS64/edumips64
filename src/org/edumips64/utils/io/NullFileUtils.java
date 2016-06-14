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
    return null;
  }

  @Override
  public Writer openWriteOnly(String pathname, boolean append) throws OpenException {
    return null;
  }
}
