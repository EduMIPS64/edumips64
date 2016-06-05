package org.edumips64.utils.io;

public class OpenException extends IOException {
  public OpenException(Exception e) {
    super(e);
  }

  public OpenException() {}
}
