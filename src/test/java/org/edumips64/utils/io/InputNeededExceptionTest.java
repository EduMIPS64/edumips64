package org.edumips64.utils.io;

import org.edumips64.BaseTest;
import org.edumips64.utils.CurrentLocale;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InputNeededExceptionTest extends BaseTest {
  @Test
  public void storesMaxLengthAndLocalizedMessages() {
    InputNeededException e = new InputNeededException(5);

    assertEquals(5, e.getMaxLength());
    assertEquals("EduMIPS64 - Input", e.getDialogTitle());
    assertEquals(CurrentLocale.getString("ENTERINPUT"), e.getPromptMessage());

    String tooLong = e.getTooLongMessage();
    assertNotNull(tooLong);
    assertTrue("tooLongMessage should mention the max length, got: " + tooLong,
        tooLong.contains("5"));
    assertTrue("tooLongMessage should include INPUTNOTEXCEED, got: " + tooLong,
        tooLong.contains(CurrentLocale.getString("INPUTNOTEXCEED")));
    assertTrue("tooLongMessage should include CHARACTERS, got: " + tooLong,
        tooLong.contains(CurrentLocale.getString("CHARACTERS")));
  }

  @Test
  public void messageIsInputRequired() {
    assertEquals("Input required", new InputNeededException(1).getMessage());
  }
}
