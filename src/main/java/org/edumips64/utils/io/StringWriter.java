package org.edumips64.utils.io;

/** A Writer implementation that writes to a string. */
public class StringWriter implements Writer {
  private StringBuffer internalStr;
  public StringWriter() {
    internalStr = new StringBuffer();
  }

  @Override
  public void write(String s) throws WriteException {
    internalStr.append(s);
  }

  @Override
  public String toString() {
    return internalStr.toString();
  }
}
