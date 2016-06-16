/**
 * Interface representing a file-like object that supports writing and
 * closing.
 */
package org.edumips64.utils.io;

public interface Writer {
  /* Writes s to the given underlying storage / device. */
  void write(String s) throws WriteException;

  /* Tries to close the file. In case of failure, does nothing. */
  void close();
}
