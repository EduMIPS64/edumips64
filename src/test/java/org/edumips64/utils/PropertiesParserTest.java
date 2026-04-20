package org.edumips64.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

/**
 * Tests for {@link PropertiesParser}. The most important assertion is that parsing the three
 * {@code CurrentLocaleMessages*.properties} files produces exactly the same key/value map as the
 * standard {@link Properties} class: this guarantees that the JVM and GWT builds see identical
 * translations, with the property files as the single source of truth.
 */
public class PropertiesParserTest {

  @Test
  public void parsesSimpleKeyValue() {
    Map<String, String> parsed = PropertiesParser.parse("a=1\nb=2\n");
    assertEquals("1", parsed.get("a"));
    assertEquals("2", parsed.get("b"));
    assertEquals(2, parsed.size());
  }

  @Test
  public void ignoresCommentsAndBlankLines() {
    String input = "# a comment\n"
        + "! another comment\n"
        + "\n"
        + "  \n"
        + "key=value\n";
    Map<String, String> parsed = PropertiesParser.parse(input);
    assertEquals(1, parsed.size());
    assertEquals("value", parsed.get("key"));
  }

  @Test
  public void supportsColonAndSpaceSeparators() {
    Map<String, String> parsed = PropertiesParser.parse("a:1\nb 2\nc = 3\nd\t:\t4\n");
    assertEquals("1", parsed.get("a"));
    assertEquals("2", parsed.get("b"));
    assertEquals("3", parsed.get("c"));
    assertEquals("4", parsed.get("d"));
  }

  @Test
  public void handlesEscapeSequences() {
    Map<String, String> parsed = PropertiesParser.parse(
        "nl=line1\\nline2\n"
        + "tab=a\\tb\n"
        + "unicode=\\u00e9\n"
        + "escaped=\\=value with \\:\n");
    assertEquals("line1\nline2", parsed.get("nl"));
    assertEquals("a\tb", parsed.get("tab"));
    assertEquals("\u00e9", parsed.get("unicode"));
    assertEquals("=value with :", parsed.get("escaped"));
  }

  @Test
  public void handlesLineContinuation() {
    Map<String, String> parsed = PropertiesParser.parse("k=one \\\n   two\n");
    assertEquals("one two", parsed.get("k"));
  }

  @Test
  public void preservesTrailingUnicodeLiterals() {
    // TextResource#getText() returns the raw UTF-8 content; no unicode escapes in our files.
    // Make sure
    // literal non-ASCII chars pass through unchanged.
    Map<String, String> parsed = PropertiesParser.parse("k=Valore non valido\n");
    assertEquals("Valore non valido", parsed.get("k"));
  }

  @Test
  public void matchesJavaPropertiesOnEnglishBundle() throws IOException {
    assertMatchesReferenceProperties("org/edumips64/utils/CurrentLocaleMessages.properties");
  }

  @Test
  public void matchesJavaPropertiesOnItalianBundle() throws IOException {
    assertMatchesReferenceProperties("org/edumips64/utils/CurrentLocaleMessages_it.properties");
  }

  @Test
  public void matchesJavaPropertiesOnChineseBundle() throws IOException {
    assertMatchesReferenceProperties("org/edumips64/utils/CurrentLocaleMessages_zh.properties");
  }

  private static void assertMatchesReferenceProperties(String resource) throws IOException {
    String raw = readResourceUtf8(resource);
    Map<String, String> parsed = PropertiesParser.parse(raw);

    Properties reference = new Properties();
    try (InputStream stream = PropertiesParserTest.class.getClassLoader()
        .getResourceAsStream(resource)) {
      assertTrue("Missing resource: " + resource, stream != null);
      // Java 9+ reads .properties files as UTF-8 with Properties#load via an InputStream that
      // passes through the default ISO-8859-1 decoding. Use a UTF-8 reader explicitly to mirror
      // ResourceBundle's behaviour.
      try (java.io.InputStreamReader reader =
          new java.io.InputStreamReader(stream, StandardCharsets.UTF_8)) {
        reference.load(reader);
      }
    }

    assertEquals("Key count mismatch for " + resource,
        reference.size(), parsed.size());
    for (String name : reference.stringPropertyNames()) {
      assertEquals("Value mismatch for key " + name + " in " + resource,
          reference.getProperty(name), parsed.get(name));
    }
  }

  private static String readResourceUtf8(String resource) throws IOException {
    try (InputStream stream = PropertiesParserTest.class.getClassLoader()
        .getResourceAsStream(resource)) {
      assertTrue("Missing resource: " + resource, stream != null);
      byte[] bytes = stream.readAllBytes();
      return new String(bytes, StandardCharsets.UTF_8);
    }
  }
}
