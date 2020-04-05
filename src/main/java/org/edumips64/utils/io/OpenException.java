package org.edumips64.utils.io;

public class OpenException extends IOException {
  private static final long serialVersionUID = 6218240038233936983L;

  public OpenException(Exception e) {
    super(e);
  }

  public OpenException() {}
}
