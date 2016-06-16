/** Base class for all org.edumips64.utils.io exceptions. */
package org.edumips64.utils.io;

public class IOException extends Exception {
  public IOException(Exception e) {
    super(e);
  }

  public IOException() {}
}
