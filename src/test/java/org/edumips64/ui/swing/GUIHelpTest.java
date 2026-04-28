package org.edumips64.ui.swing;

import org.edumips64.BaseTest;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/** Tests for {@link GUIHelp} failure paths. */
public class GUIHelpTest extends BaseTest {

  /** Captures log records emitted by GUIHelp during a test. */
  private static class CapturingHandler extends Handler {
    final List<LogRecord> records = new ArrayList<>();
    @Override public void publish(LogRecord record) { records.add(record); }
    @Override public void flush() {}
    @Override public void close() {}
  }

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

  @Test
  public void showHelpHandlesPathsContainingSpaces() throws Exception {
    // Regression test: when the help URL contains raw spaces (e.g. the JAR lives in
    // "C:/Users/foo/JAR (1)/..."), showHelp must not throw URISyntaxException while
    // building the URI. It should still fail gracefully (no helpset present) and
    // return false, without logging a "Could not parse Help URL" SEVERE record.
    Path tempDir = Files.createTempDirectory("edumips64 with spaces");
    tempDir.toFile().deleteOnExit();
    // Build a jar: URL with raw (unencoded) spaces, matching what is observed when
    // the JAR file lives in a directory whose name contains spaces. URI's opaque-part
    // parser is what rejected the raw space in the original bug report.
    URL url = new URL("jar:file:" + tempDir.toAbsolutePath() + "/edumips64.jar!/docs/user/en/");

    Logger logger = Logger.getLogger(GUIHelp.class.getName());
    CapturingHandler handler = new CapturingHandler();
    logger.addHandler(handler);
    try {
      assertFalse("showHelp should return false (and not throw) for paths containing spaces",
          GUIHelp.showHelp(null, url, config));
    } finally {
      logger.removeHandler(handler);
    }

    for (LogRecord r : handler.records) {
      if (r.getLevel() == Level.SEVERE && r.getMessage() != null
          && r.getMessage().startsWith("Could not parse Help URL")) {
        fail("URI parsing should succeed for URLs with raw spaces, "
            + "but got SEVERE log: " + r.getMessage());
      }
    }
  }
}

