package org.edumips64.utils.io;

public class WriteException extends IOException {
  public WriteException(Exception e) {
    super(e);
  }

  public WriteException() {}
}
