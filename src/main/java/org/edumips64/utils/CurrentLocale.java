/* CurrentLocale.java
 *
 * This class gives the current locale settings.
 * (c) 2006
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

/** This class has mostly static methods. If the 'config' attribute is set, then the current language is
   fetched from it. Otherwise, "en" is considered the default and used.

   Localized messages are loaded from {@link ResourceBundle} properties files named
   {@code CurrentLocaleMessages.properties} (English, default), {@code CurrentLocaleMessages_it.properties}
   (Italian) and {@code CurrentLocaleMessages_zh.properties} (Simplified Chinese). This consolidates the
   internationalization mechanism to be consistent with the CLI, which already used ResourceBundles
   (see PRs #264 and #199).

   Note: {@code java.util.ResourceBundle} is not part of GWT's JRE emulation, so this file is excluded
   from GWT compilation in {@code webclient.gwt.xml} and replaced by the {@code HashMap}-based version
   located under {@code supersource/}. Keep both implementations in sync behaviourally.
 */
public class CurrentLocale {

  private static final String BUNDLE_BASE_NAME = "CurrentLocaleMessages";

  // Maps an internal language code (as used in ConfigKey.LANGUAGE) to the Locale used to look up
  // the matching ResourceBundle. The English bundle is the base bundle (no suffix).
  private static final Map<String, Locale> SUPPORTED_LANGUAGES;
  static {
    Map<String, Locale> m = new HashMap<>();
    // English is served by the base bundle (no suffix), so use Locale.ROOT to avoid an extra
    // lookup for CurrentLocaleMessages_en.properties.
    m.put("en", Locale.ROOT);
    m.put("it", Locale.ITALIAN);
    m.put("zhcn", Locale.SIMPLIFIED_CHINESE);
    SUPPORTED_LANGUAGES = m;
  }

  private static ConfigStore config;

  public static void setConfig(ConfigStore config) {
    CurrentLocale.config = config;
  }

  private static final Logger logger = Logger.getLogger(CurrentLocale.class.getName());

  private static ResourceBundle getBundle(Locale locale) {
    // Use the no-fallback control so we never silently fall back to the JVM's default locale,
    // ensuring deterministic behaviour across environments.
    return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale,
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));
  }

  public static String getString(String key) {
    String lang_name = "en";
    Locale locale = Locale.ROOT;

    if (config != null) {
      lang_name = config.getString(ConfigKey.LANGUAGE);
      Locale found = SUPPORTED_LANGUAGES.get(lang_name);
      if (found != null) {
        locale = found;
      } else {
        logger.severe("Could not find language " + lang_name + ", defaulting to English");
        lang_name = "en";
      }
    }

    try {
      return getBundle(locale).getString(key);
    } catch (MissingResourceException e) {
      // Not found in the selected language: fall back to English.
    }

    logger.severe("Language " + lang_name + " does not contain message " + key + ", falling back to English.");
    try {
      return getBundle(Locale.ROOT).getString(key);
    } catch (MissingResourceException e) {
      logger.severe("Could not find message " + key + " neither in language " + lang_name + " nor in English.");
      // Return the key to avoid the UI to show a blank, and also to make it very evident in the UI that something
      // is very wrong.
      return key;
    }
  }
}
