package org.edumips64.client;

import org.edumips64.utils.io.InputNeededException;
import org.edumips64.utils.io.ReadException;
import org.edumips64.utils.io.StringInputReader;

/** Reader implementation used by the web worker to collect stdin from the UI thread. */
public class WebInputReader implements StringInputReader {
  private String nextInput;

  @Override
  public int read(char[] buffer, int count) throws ReadException {
    String input = readString(count);
    for (int i = 0; i < input.length(); i++) {
      buffer[i] = input.charAt(i);
    }
    return input.length();
  }

  @Override
  public void close() {}

  @Override
  public String readString(int count) throws ReadException {
    if (nextInput == null) {
      throw new InputNeededException(count);
    }

    String input = nextInput;
    nextInput = null;
    if (input.length() > count) {
      return input.substring(0, count);
    }
    return input;
  }

  public void setNextInput(String input) {
    nextInput = input;
  }

  public void reset() {
    nextInput = null;
  }
}
