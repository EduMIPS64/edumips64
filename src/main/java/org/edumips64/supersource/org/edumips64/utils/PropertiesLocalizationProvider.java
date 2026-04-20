/* PropertiesLocalizationProvider.java (GWT super-source)
 *
 * GWT-only LocalizationProvider that reads the same CurrentLocaleMessages*.properties files used
 * by the JVM build and caches the parsed key/value pairs per language.
 *
 * (c) 2026 EduMIPS64
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 */
package org.edumips64.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * GWT implementation of {@link LocalizationProvider}.
 *
 * <p>The three {@code CurrentLocaleMessages*.properties} files are embedded into the compiled
 * JavaScript via a {@link ClientBundle} of {@link TextResource}s and parsed on first use by
 * {@link PropertiesParser}. This keeps those files as the single source of truth for the UI's
 * translations (the JVM build reads exactly the same files through {@link java.util.ResourceBundle}).
 */
public class PropertiesLocalizationProvider implements LocalizationProvider {

  private static final Logger logger =
      Logger.getLogger(PropertiesLocalizationProvider.class.getName());

  /** ClientBundle exposing the three property files as compile-time string resources. */
  public interface Messages extends ClientBundle {
    Messages INSTANCE = GWT.create(Messages.class);

    @Source("CurrentLocaleMessages.properties")
    TextResource english();

    @Source("CurrentLocaleMessages_it.properties")
    TextResource italian();

    @Source("CurrentLocaleMessages_zh.properties")
    TextResource simplifiedChinese();
  }

  // Lazy, per-language parsed maps. Keys are internal language codes ("en"/"it"/"zhcn").
  private final Map<String, Map<String, String>> cache = new HashMap<>();

  private Map<String, String> load(String lang) {
    Map<String, String> cached = cache.get(lang);
    if (cached != null) {
      return cached;
    }
    String raw;
    switch (lang) {
      case "it":
        raw = Messages.INSTANCE.italian().getText();
        break;
      case "zhcn":
        raw = Messages.INSTANCE.simplifiedChinese().getText();
        break;
      case "en":
      default:
        raw = Messages.INSTANCE.english().getText();
        break;
    }
    Map<String, String> parsed = PropertiesParser.parse(raw);
    cache.put(lang, parsed);
    return parsed;
  }

  @Override
  public String getString(String lang, String key) {
    String value = load(lang).get(key);
    if (value != null) {
      return value;
    }

    if (!"en".equals(lang)) {
      logger.severe("Language " + lang + " does not contain message " + key
          + ", falling back to English.");
      String english = load("en").get(key);
      if (english != null) {
        return english;
      }
    }

    logger.severe("Could not find message " + key + " neither in language " + lang
        + " nor in English.");
    return key;
  }
}
