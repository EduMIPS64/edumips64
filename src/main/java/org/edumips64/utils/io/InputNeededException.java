package org.edumips64.utils.io;

import org.edumips64.utils.CurrentLocale;

/** Raised by the web stdin reader when the UI must collect input from the user. */
public class InputNeededException extends RuntimeException {
  private static final long serialVersionUID = 2375096654729363495L;

  private final int maxLength;
  private final String dialogTitle;
  private final String promptMessage;
  private final String tooLongMessage;

  public InputNeededException(int maxLength) {
    super("Input required");
    this.maxLength = maxLength;
    this.dialogTitle = "EduMIPS64 - Input";
    this.promptMessage = CurrentLocale.getString("ENTERINPUT");
    this.tooLongMessage =
        CurrentLocale.getString("INPUTNOTEXCEED")
            + " "
            + maxLength
            + " "
            + CurrentLocale.getString("CHARACTERS");
  }

  public int getMaxLength() {
    return maxLength;
  }

  public String getDialogTitle() {
    return dialogTitle;
  }

  public String getPromptMessage() {
    return promptMessage;
  }

  public String getTooLongMessage() {
    return tooLongMessage;
  }
}
