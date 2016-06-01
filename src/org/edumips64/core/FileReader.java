package org.edumips64.core;

/**
 * An interface to read files.
 */
public abstract class FileReader {
  /** Reads the file specified in 'filename' and returns its contents as a list of strings, one for each line.
   *
   * @param filename the file to open and read
   * @return a list of strings, one for each line in the file.
   */
  abstract String ReadFile(String filename) throws ReadException;
  abstract String GetAbsoluteFilename(String filename);
  abstract String GetBasePath(String filename);
  abstract boolean isAbsolute(String filename);

  public class ReadException extends Exception {
    ReadException(Exception e) {
      super(e);
    }

    ReadException() {}
  }

  /** Clean multiple tab or spaces in a bad format String //and converts  this String to upper case
   *  @param s the bad format String
   *  @return the cleaned String
   *  TODO(andrea): remove code duplication with Parser.java
   */
  String cleanFormat(String s) {
    if (s.length() > 0 && s.charAt(0) != ';' &&  s.charAt(0) != '\n') {
      //String[] nocomment=s.split(";");
      //s=nocomment[0];//.toUpperCase();
      s = s.trim();
      s = s.replace("\t", " ");

      while (s.contains("  ")) {
        s = s.replace("  ", " ");
      }

      s = s.replace(", ", ",");
      s = s.replace(" ,", ",");

      if (s.length() > 0) {
        return s;
      }
    }

    return null;
  }
}