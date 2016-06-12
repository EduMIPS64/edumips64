/** A Writer implementation that just wraps a java.io.Writer. */

package org.edumips64.utils.io;

import java.io.Writer;

public class WriterAdapter implements org.edumips64.utils.io.Writer {
  private java.io.Writer writer;

  public WriterAdapter(java.io.Writer writer) {
    this.writer = writer;
  }

  public void write(String s) throws WriteException {
    try {
      writer.write(s);
    } catch (Exception e) {
      throw new WriteException(e);
    }
  }

  public void close() {
    try {
      writer.close();
    } catch (Exception e) {
      // TODO: proper handling.
    }
  }
}
