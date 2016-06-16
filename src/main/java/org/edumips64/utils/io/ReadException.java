package org.edumips64.utils.io;

public class ReadException extends IOException {
  public ReadException(Exception e) {
    super(e);
  }

  public ReadException() {}
}
