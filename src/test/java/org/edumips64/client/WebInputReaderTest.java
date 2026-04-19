package org.edumips64.client;

import org.edumips64.BaseTest;
import org.edumips64.utils.io.InputNeededException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WebInputReaderTest extends BaseTest {
  @Test
  public void readStringReturnsProvidedInput() throws Exception {
    WebInputReader reader = new WebInputReader();
    reader.setNextInput("hello");
    assertEquals("hello", reader.readString(5));
  }

  @Test
  public void readStringTruncatesToCount() throws Exception {
    WebInputReader reader = new WebInputReader();
    reader.setNextInput("helloworld");
    assertEquals("hello", reader.readString(5));
  }

  @Test
  public void readStringConsumesInputSoSecondCallNeedsMore() throws Exception {
    WebInputReader reader = new WebInputReader();
    reader.setNextInput("abc");
    reader.readString(3);
    try {
      reader.readString(3);
      fail("Expected InputNeededException on second readString call");
    } catch (InputNeededException expected) {
      assertEquals(3, expected.getMaxLength());
    }
  }

  @Test
  public void readStringThrowsWhenNoInputAvailable() throws Exception {
    WebInputReader reader = new WebInputReader();
    try {
      reader.readString(7);
      fail("Expected InputNeededException when no input was provided");
    } catch (InputNeededException expected) {
      assertEquals(7, expected.getMaxLength());
    }
  }

  @Test
  public void readFillsBufferWithProvidedInput() throws Exception {
    WebInputReader reader = new WebInputReader();
    reader.setNextInput("xyz");
    char[] buffer = new char[5];
    int bytesRead = reader.read(buffer, 5);
    assertEquals(3, bytesRead);
    assertEquals('x', buffer[0]);
    assertEquals('y', buffer[1]);
    assertEquals('z', buffer[2]);
  }

  @Test
  public void readThrowsInputNeededWhenNoInputAvailable() {
    WebInputReader reader = new WebInputReader();
    char[] buffer = new char[4];
    try {
      reader.read(buffer, 4);
      fail("Expected InputNeededException when no input was provided");
    } catch (InputNeededException expected) {
      assertEquals(4, expected.getMaxLength());
    } catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }

  @Test
  public void resetClearsPendingInput() throws Exception {
    WebInputReader reader = new WebInputReader();
    reader.setNextInput("pending");
    reader.reset();
    try {
      reader.readString(10);
      fail("Expected InputNeededException after reset");
    } catch (InputNeededException expected) {
      assertEquals(10, expected.getMaxLength());
    }
  }

  @Test
  public void closeIsSafeToCall() {
    WebInputReader reader = new WebInputReader();
    reader.close();
  }
}
