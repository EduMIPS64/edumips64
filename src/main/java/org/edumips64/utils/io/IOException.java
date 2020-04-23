/** Base class for all org.edumips64.utils.io exceptions. */
package org.edumips64.utils.io;

public class IOException extends Exception {
  private static final long serialVersionUID = 4979137270248616293L;

  public IOException(Exception e) {
    super(e);
  }

  public IOException() {}
}
