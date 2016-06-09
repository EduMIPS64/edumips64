/** 
 * Interface representing an file-like object that supports reading and
 * closing.
 */
package org.edumips64.utils.io;

public interface Reader {
  /* Tries to read 'count' bytes and put them in 'buffer'. Returns the actual
   * number of bytes read. */
  int read(char[] buffer, int count) throws ReadException;

  /* Tries to close the file. In case of failure, does nothing. */
  void close();
}
