package org.edumips64.core;

import java.io.*;

/**
 * Reads contents of files in a local filesystem.
 */
public class LocalFileReader extends FileReader{
  @Override
  public String ReadFile(String filename) throws ReadException {
    StringBuffer contents = new StringBuffer();
    try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "ISO-8859-1"))) {
      String line;

      while ((line = r.readLine()) != null) {
        String tmp = cleanFormat(line);

        if (tmp != null) {
          contents.append(tmp);
          contents.append('\n');
        }
      }
    } catch (IOException e) {
      throw new FileReader.ReadException(e);
    }
    return new String(contents);
  }

  @Override
  String GetAbsoluteFilename(String filename) {
    return new File(filename).getAbsolutePath();
  }

  @Override
  String GetBasePath(String filename) {
    filename = new File(filename).getAbsolutePath() ;

    int index = 0;
    int oldindex = 0;
    while ((index = filename.indexOf(File.separator, index)) != -1) {
      oldindex = index;
      index ++;
    }

    return filename.substring(0, oldindex + 1);
  }

  @Override
  boolean isAbsolute(String filename) {
    return new File(filename).isAbsolute();
  }
}
