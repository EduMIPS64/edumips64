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

    int i = 0;
    int n = input.length();
    StringBuilder logicalLine = new StringBuilder();

    while (i < n) {
      // Read one logical line (following backslash-continuations).
      logicalLine.setLength(0);
      boolean continued;
      boolean firstSegment = true;

      do {
        // Skip leading whitespace (spaces and tabs) on each physical line.
        while (i < n && (input.charAt(i) == ' ' || input.charAt(i) == '\t'
            || input.charAt(i) == '\f')) {
          i++;
        }

        // Find end of this physical line and detect whether it ends in an unescaped backslash.
        int lineStart = i;
        while (i < n && input.charAt(i) != '\n' && input.charAt(i) != '\r') {
          i++;
        }
        int lineEnd = i;

        // Consume the line terminator (handle \r, \n, and \r\n).
        if (i < n && input.charAt(i) == '\r') {
          i++;
          if (i < n && input.charAt(i) == '\n') {
            i++;
          }
        } else if (i < n && input.charAt(i) == '\n') {
          i++;
        }

        String segment = input.substring(lineStart, lineEnd);

        if (firstSegment) {
          firstSegment = false;
          // On the first physical line only: skip comments and blanks entirely.
          if (segment.isEmpty()
              || segment.charAt(0) == '#' || segment.charAt(0) == '!') {
            continued = false;
            continue;
          }
        }

        // Determine whether the segment ends with an unescaped backslash (line continuation).
        int trailingBackslashes = 0;
        for (int k = segment.length() - 1; k >= 0 && segment.charAt(k) == '\\'; k--) {
          trailingBackslashes++;
        }
        continued = (trailingBackslashes % 2) == 1;
        if (continued) {
          // Strip the trailing continuation backslash before appending.
          logicalLine.append(segment, 0, segment.length() - 1);
        } else {
          logicalLine.append(segment);
        }
      } while (continued && i < n);

      if (logicalLine.length() == 0) {
        continue;
      }

      // Split the logical line into key and value.
      String line = logicalLine.toString();
      int keyEnd = findKeyEnd(line);
      String rawKey = line.substring(0, keyEnd);
      String rawValue;
      if (keyEnd >= line.length()) {
        rawValue = "";
      } else {
        int valueStart = keyEnd;
        // Skip the separator character (= or :) if present, and surrounding whitespace.
        while (valueStart < line.length() && (line.charAt(valueStart) == ' '
            || line.charAt(valueStart) == '\t' || line.charAt(valueStart) == '\f')) {
          valueStart++;
        }
        if (valueStart < line.length()
            && (line.charAt(valueStart) == '=' || line.charAt(valueStart) == ':')) {
          valueStart++;
          while (valueStart < line.length() && (line.charAt(valueStart) == ' '
              || line.charAt(valueStart) == '\t' || line.charAt(valueStart) == '\f')) {
            valueStart++;
          }
        }
        rawValue = line.substring(valueStart);
      }

      out.put(unescape(rawKey), unescape(rawValue));
    }

    return out;
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
