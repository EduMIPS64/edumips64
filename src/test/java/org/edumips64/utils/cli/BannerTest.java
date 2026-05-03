package org.edumips64.utils.cli;

import org.edumips64.utils.MetaInfo;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class BannerTest {

  @Test
  public void markupContainsName() {
    String markup = Banner.asMarkup();
    // ANSI Shadow art uses Unicode block-drawing characters.
    assertTrue("markup should contain block-drawing art", markup.contains("█"));
    assertTrue("markup should mention EduMIPS64", markup.contains("EduMIPS64"));
    // VERSION is read from JAR manifest and may be null in unit tests; the
    // banner uses 'dev' as a fallback in that case.
    String expected = (MetaInfo.VERSION == null || MetaInfo.VERSION.isEmpty())
        ? "dev" : MetaInfo.VERSION;
    assertTrue("markup should mention current version (" + expected + ")",
        markup.contains(expected));
  }

  @Test
  public void leftAndRightArraysShareHeight() {
    String markup = Banner.asMarkup();
    int newlines = markup.split("\n", -1).length - 1;
    // 1 leading blank + 6 art rows + 1 blank + 3 tagline rows + 0/1 trailing
    assertTrue("expected at least 10 lines, got " + newlines, newlines >= 10);
  }

  @Test
  public void compactDoesNotEmitAnsiEscapes() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
    Banner.printCompact(ps);
    String out = baos.toString(StandardCharsets.UTF_8);
    assertFalse("compact form must not contain ANSI ESC", out.contains("\u001b["));
    assertFalse("compact form must not contain picocli markup", out.contains("@|"));
  }
}
