/** Implementation of Writer using java.io.FileWriter. */

package org.edumips64.utils.io;

import java.io.FileWriter;

public class LocalWriter implements Writer {
  private FileWriter writer;

  public LocalWriter(String filename, boolean append) throws OpenException {
    try {
      writer = new FileWriter(filename, append);
    } catch (Exception e) {
      throw new OpenException(e);
    }
  }

  public void write(String str) throws WriteException {
    try {
      writer.write(str);
    } catch(Exception e) {
      throw new WriteException(e);
    }
  }

  public void close() {
    try {
      writer.close();
    } catch (Exception e) {
      // TODO(andrea): change interface to report this error.
    }
  }
}
