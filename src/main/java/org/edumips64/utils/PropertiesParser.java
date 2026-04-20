/* PropertiesParser.java
 *
 * Minimal parser for the subset of the Java .properties file format used by EduMIPS64's
 * translation files. Used by the GWT build (which doesn't have java.util.Properties in its JRE
 * emulation) to read the same CurrentLocaleMessages*.properties files as the JVM build.
 *
 * (c) 2026 EduMIPS64
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 */
package org.edumips64.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal implementation of the Java {@code .properties} file format, sufficient for parsing
 * EduMIPS64's translation files.
 *
 * <p>Supports:
 *
 * <ul>
 *   <li>{@code key=value}, {@code key:value} and {@code key value} key/value separators (with
 *       optional surrounding whitespace);</li>
 *   <li>line comments starting with {@code #} or {@code !};</li>
 *   <li>blank lines;</li>
 *   <li>line continuations (a trailing backslash causes the next line's content to be appended,
 *       with leading whitespace on that line trimmed);</li>
 *   <li>the escape sequences {@code \n}, {@code \t}, {@code \r}, {@code \f}, {@code \\},
 *       {@code \"}, {@code \'}, {@code \=}, {@code \:}, {@code \ } (escaped space) and
 *       {@code \\uXXXX}.</li>
 * </ul>
 *
 * <p>The input is expected to already be decoded into a Java {@link String} (i.e. UTF-8 decoding
 * has already been performed). This matches what GWT's {@code TextResource#getText()} returns and
 * what {@link java.util.ResourceBundle} does under Java 9+ for {@code .properties} files.
 */
final class PropertiesParser {

  private PropertiesParser() {}

  /** Parses a {@code .properties} document and returns the resulting key/value map. */
  static Map<String, String> parse(String input) {
    Map<String, String> out = new LinkedHashMap<>();
    if (input == null || input.isEmpty()) {
      return out;
    }

    int[] cursor = {0};
    int n = input.length();
    while (cursor[0] < n) {
      String line = readLogicalLine(input, cursor);
      if (line == null || line.isEmpty()) {
        continue;
      }
      addEntry(line, out);
    }

    return out;
  }

  /**
   * Reads one logical line (honouring comments, blank lines, and trailing-backslash
   * continuations) starting at {@code cursor[0]} and advances the cursor past the consumed
   * characters. Returns {@code null} when the line was a comment or blank, and the empty string
   * only when the logical line was itself empty.
   */
  private static String readLogicalLine(String input, int[] cursor) {
    int n = input.length();
    StringBuilder logicalLine = new StringBuilder();
    boolean continued;
    boolean firstSegment = true;
    boolean skipped = false;

    do {
      cursor[0] = skipLeadingWhitespace(input, cursor[0]);
      int lineStart = cursor[0];
      while (cursor[0] < n && input.charAt(cursor[0]) != '\n' && input.charAt(cursor[0]) != '\r') {
        cursor[0]++;
      }
      int lineEnd = cursor[0];
      cursor[0] = consumeLineTerminator(input, cursor[0]);

      String segment = input.substring(lineStart, lineEnd);

      if (firstSegment) {
        firstSegment = false;
        if (segment.isEmpty() || segment.charAt(0) == '#' || segment.charAt(0) == '!') {
          skipped = true;
          continued = false;
          continue;
        }
      }

      continued = endsWithUnescapedBackslash(segment);
      if (continued) {
        logicalLine.append(segment, 0, segment.length() - 1);
      } else {
        logicalLine.append(segment);
      }
    } while (continued && cursor[0] < n);

    if (skipped) {
      return null;
    }
    return logicalLine.toString();
  }

  private static int skipLeadingWhitespace(String input, int i) {
    int n = input.length();
    while (i < n) {
      char c = input.charAt(i);
      if (c != ' ' && c != '\t' && c != '\f') {
        break;
      }
      i++;
    }
    return i;
  }

  private static int consumeLineTerminator(String input, int i) {
    int n = input.length();
    if (i >= n) {
      return i;
    }
    char c = input.charAt(i);
    if (c == '\r') {
      i++;
      if (i < n && input.charAt(i) == '\n') {
        i++;
      }
    } else if (c == '\n') {
      i++;
    }
    return i;
  }

  private static boolean endsWithUnescapedBackslash(String segment) {
    int trailing = 0;
    for (int k = segment.length() - 1; k >= 0 && segment.charAt(k) == '\\'; k--) {
      trailing++;
    }
    return (trailing % 2) == 1;
  }

  /** Splits a logical line into a key/value pair and stores the unescaped entry in {@code out}. */
  private static void addEntry(String line, Map<String, String> out) {
    int keyEnd = findKeyEnd(line);
    String rawKey = line.substring(0, keyEnd);
    String rawValue = extractRawValue(line, keyEnd);
    out.put(unescape(rawKey), unescape(rawValue));
  }

  private static String extractRawValue(String line, int keyEnd) {
    if (keyEnd >= line.length()) {
      return "";
    }
    int valueStart = skipValueWhitespace(line, keyEnd);
    if (valueStart < line.length()
        && (line.charAt(valueStart) == '=' || line.charAt(valueStart) == ':')) {
      valueStart = skipValueWhitespace(line, valueStart + 1);
    }
    return line.substring(valueStart);
  }

  private static int skipValueWhitespace(String line, int i) {
    int n = line.length();
    while (i < n) {
      char c = line.charAt(i);
      if (c != ' ' && c != '\t' && c != '\f') {
        break;
      }
      i++;
    }
    return i;
  }

  /**
   * Returns the index at which the key ends: the first unescaped whitespace, {@code =} or
   * {@code :} character, or the end of the line.
   */
  private static int findKeyEnd(String line) {
    boolean escaped = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (escaped) {
        escaped = false;
        continue;
      }
      if (c == '\\') {
        escaped = true;
        continue;
      }
      if (c == ' ' || c == '\t' || c == '\f' || c == '=' || c == ':') {
        return i;
      }
    }
    return line.length();
  }

  /** Applies the standard Java .properties escape sequences to {@code s}. */
  private static String unescape(String s) {
    if (s.indexOf('\\') < 0) {
      return s;
    }
    StringBuilder out = new StringBuilder(s.length());
    int i = 0;
    int n = s.length();
    while (i < n) {
      char c = s.charAt(i++);
      if (c != '\\' || i >= n) {
        out.append(c);
        continue;
      }
      char esc = s.charAt(i++);
      switch (esc) {
        case 'n': out.append('\n'); break;
        case 't': out.append('\t'); break;
        case 'r': out.append('\r'); break;
        case 'f': out.append('\f'); break;
        case 'u':
          if (i + 4 <= n) {
            int cp = 0;
            boolean ok = true;
            for (int k = 0; k < 4; k++) {
              char h = s.charAt(i + k);
              int d;
              if (h >= '0' && h <= '9') d = h - '0';
              else if (h >= 'a' && h <= 'f') d = 10 + (h - 'a');
              else if (h >= 'A' && h <= 'F') d = 10 + (h - 'A');
              else { ok = false; break; }
              cp = (cp << 4) | d;
            }
            if (ok) {
              out.append((char) cp);
              i += 4;
            } else {
              out.append(esc);
            }
          } else {
            out.append(esc);
          }
          break;
        default:
          // For anything else (\\, \", \', \=, \:, \space, etc.) the escaped character itself
          // is the intended output.
          out.append(esc);
          break;
      }
    }
    return out.toString();
  }
}
