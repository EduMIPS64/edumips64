package org.edumips64.core;

import org.edumips64.BaseTest;
import org.edumips64.utils.io.NullFileUtils;
import org.edumips64.utils.io.ReadException;
import org.edumips64.utils.io.StringInputReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IOManagerTest extends BaseTest {
  @Test
  public void readWritesOnlyTheBytesActuallyRead() throws Exception {
    Memory memory = new Memory();
    IOManager ioManager = new IOManager(new NullFileUtils(), memory);
    ioManager.setStdInput(new StringInputReader() {
      @Override
      public int read(char[] buffer, int count) throws ReadException {
        buffer[0] = 'a';
        buffer[1] = 'b';
        buffer[2] = 'c';
        return 3;
      }

      @Override
      public String readString(int count) throws ReadException {
        return "abc";
      }

      @Override
      public void close() {}
    });

    int bytesRead = ioManager.read(0, 0, 5);
    MemoryElement element = memory.getCellByAddress(0);

    assertEquals(3, bytesRead);
    assertEquals('a', element.readByte(0));
    assertEquals('b', element.readByte(1));
    assertEquals('c', element.readByte(2));
    assertEquals(0, element.readByte(3));
    assertEquals(0, element.readByte(4));
  }

  @Test
  public void readUsesPlainReaderFallbackForNonStringInputReader() throws Exception {
    Memory memory = new Memory();
    IOManager ioManager = new IOManager(new NullFileUtils(), memory);
    ioManager.setStdInput(new org.edumips64.utils.io.Reader() {
      @Override
      public int read(char[] buffer, int count) throws ReadException {
        buffer[0] = 'X';
        buffer[1] = 'Y';
        return 2;
      }

      @Override
      public void close() {}
    });

    int bytesRead = ioManager.read(0, 0, 4);
    MemoryElement element = memory.getCellByAddress(0);

    assertEquals(2, bytesRead);
    assertEquals('X', element.readByte(0));
    assertEquals('Y', element.readByte(1));
    assertEquals(0, element.readByte(2));
    assertEquals(0, element.readByte(3));
  }
}
