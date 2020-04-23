package org.edumips64.utils.io;

public class WriteException extends IOException {
  private static final long serialVersionUID = 3387477308435272998L;

  public WriteException(Exception e) {
    super(e);
  }

  public WriteException() {}
}
