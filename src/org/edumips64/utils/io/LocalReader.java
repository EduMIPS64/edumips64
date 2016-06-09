/** Implementation of Reader using java.io.FileReader. */

package org.edumips64.utils.io;

import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

public class LocalReader implements Reader {
  private FileReader reader;

  public LocalReader(String filename) throws OpenException {
    try {
      reader = new FileReader(filename);
    } catch (FileNotFoundException e) {
      throw new OpenException(e);
    }
  }

  public int read(char[] buffer, int count) throws ReadException {
    try {
      return reader.read(buffer, 0, count);
    } catch(Exception e) {
      throw new ReadException(e);
    }
  }

  public void close() {
    try {
      reader.close();
    } catch (Exception e) {
      // TODO(andrea): change interface to report this error.
    }
  }
}
