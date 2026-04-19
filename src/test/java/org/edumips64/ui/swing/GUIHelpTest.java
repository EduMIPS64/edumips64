package org.edumips64.ui.swing;

import org.edumips64.BaseTest;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;

/** Tests for {@link GUIHelp} failure paths. */
public class GUIHelpTest extends BaseTest {

  @Test
  public void showHelpReturnsFalseWhenHelpsetFileIsMissing() throws Exception {
    // A directory without EduMIPS64.hs: HelpSet.findHelpSet returns null and
    // showHelp should fail gracefully instead of throwing or hanging.
    Path tempDir = Files.createTempDirectory("edumips64-no-helpset");
    tempDir.toFile().deleteOnExit();
    URL url = tempDir.toUri().toURL();

    assertFalse("showHelp should return false when no helpset is available",
        GUIHelp.showHelp(null, url, config));
  }
}

