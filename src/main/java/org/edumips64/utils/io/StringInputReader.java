package org.edumips64.utils.io;

/** Optional extension for Reader implementations that can return a String directly. */
public interface StringInputReader extends Reader {
  String readString(int count) throws ReadException;
}
