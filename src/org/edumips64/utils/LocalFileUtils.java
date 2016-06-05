package org.edumips64.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class LocalFileUtils extends FileUtils {
  @Override
  public Reader openReadOnly(String pathname) throws OpenException {
    try {
      return new FileReader(pathname);
    } catch (Exception e) {
      throw new OpenException(e);
    }
  }

  @Override
  public Writer openWriteOnly(String pathname, boolean append) throws OpenException {
    try {
      return new FileWriter(pathname, append);
    } catch (Exception e) {
      throw new OpenException(e);
    }
  }

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
      throw new ReadException(e);
    }
    return new String(contents);
  }

  @Override
  public String GetAbsoluteFilename(String filename) {
    return new File(filename).getAbsolutePath();
  }

  @Override
  public String GetBasePath(String filename) {
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
  public boolean isAbsolute(String filename) {
    return new File(filename).isAbsolute();
  }

  @Override
  public boolean Exists(String filename) {
    return new File(filename).exists();
  }
}
