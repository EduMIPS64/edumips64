package org.edumips64.utils.io;

public class ReadException extends IOException {
  private static final long serialVersionUID = -6747880453691618237L;

  public ReadException(Exception e) {
    super(e);
  }

  public ReadException() {}
}
