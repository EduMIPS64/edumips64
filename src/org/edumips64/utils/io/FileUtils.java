/**
 * FileUtils
 *
 * Abstract class that encapsulates interaction with the filesystem without
 * using the java.io package.
 */
package org.edumips64.utils.io;

public abstract class FileUtils {
  /** Reads the file specified in 'filename' and returns its contents as a list of strings, one for each line.
   *
   * @param filename the file to open and read
   * @return a list of strings, one for each line in the file.
   */
  public abstract String ReadFile(String filename) throws ReadException;
  public abstract String GetAbsoluteFilename(String filename);
  public abstract String GetBasePath(String filename);
  public abstract boolean isAbsolute(String filename);
  public abstract boolean Exists(String filename);

  public abstract Reader openReadOnly(String pathname) throws OpenException;
  public abstract Writer openWriteOnly(String pathname, boolean append) throws OpenException;

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
