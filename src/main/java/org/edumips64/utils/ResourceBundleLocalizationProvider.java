/* ResourceBundleLocalizationProvider.java
 *
 * JVM implementation of LocalizationProvider backed by java.util.ResourceBundle.
 * (c) 2026 EduMIPS64
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.edumips64.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * {@link LocalizationProvider} backed by {@link ResourceBundle}. Used in the JVM build (Swing UI
 * and CLI). The properties files live under {@code org/edumips64/utils/} so they are addressable
 * both through the Java resource bundle machinery and through GWT's classpath resource loading.
 *
 * <p>This class relies on {@link ResourceBundle}, which is not part of GWT's JRE emulation, and is
 * therefore excluded from GWT compilation in {@code webclient.gwt.xml}.
 */
public class ResourceBundleLocalizationProvider implements LocalizationProvider {

  static final String BUNDLE_BASE_NAME = "org.edumips64.utils.CurrentLocaleMessages";

  // Maps an internal language code (as used in ConfigKey.LANGUAGE) to the Locale used to look up
  // the matching ResourceBundle. English is served by the base bundle (no suffix), so "en" maps
  // to Locale.ROOT to avoid an extra lookup for CurrentLocaleMessages_en.properties.
  private static final Map<String, Locale> SUPPORTED_LANGUAGES;
  static {
    Map<String, Locale> m = new HashMap<>();
    m.put("en", Locale.ROOT);
    m.put("it", Locale.ITALIAN);
    // "zhcn" maps to the "_zh" bundle suffix already used by the existing CLI Messages bundle.
    m.put("zhcn", Locale.SIMPLIFIED_CHINESE);
    SUPPORTED_LANGUAGES = m;
  }

  private static final Logger logger =
      Logger.getLogger(ResourceBundleLocalizationProvider.class.getName());

  private static ResourceBundle getBundle(Locale locale) {
    // Use the no-fallback control so we never silently fall back to the JVM's default locale,
    // ensuring deterministic behaviour across environments.
    return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale,
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));
  }

  @Override
  public String getString(String lang, String key) {
    Locale locale = SUPPORTED_LANGUAGES.get(lang);
    if (locale == null) {
      // Unknown language code: behave as if English was selected.
      locale = Locale.ROOT;
    }

    try {
      return getBundle(locale).getString(key);
    } catch (MissingResourceException e) {
      // Fall back to English below.
    }

    if (!Locale.ROOT.equals(locale)) {
      logger.severe("Language " + lang + " does not contain message " + key
          + ", falling back to English.");
      try {
        return getBundle(Locale.ROOT).getString(key);
      } catch (MissingResourceException e) {
        // Fall through to the "return key" final fallback.
      }
    }

    logger.severe("Could not find message " + key + " neither in language " + lang
        + " nor in English.");
    // Return the key to avoid the UI showing a blank, and to make missing translations very
    // evident.
    return key;
  }
}
