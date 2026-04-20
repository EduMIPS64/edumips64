/* CurrentLocale.java (GWT super-source)
 *
 * GWT-only version of CurrentLocale. The JVM version lives at
 * src/main/java/org/edumips64/utils/CurrentLocale.java; this file is selected by GWT because it
 * is referenced from the super-source path declared in webclient.gwt.xml.
 *
 * (c) 2006-2026 EduMIPS64
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 */
package org.edumips64.utils;

import java.util.logging.Logger;

/**
 * Static facade for localized message lookup in the GWT build. The behaviour mirrors the JVM
 * {@code CurrentLocale}, except that the {@link LocalizationProvider} used here reads the
 * {@code CurrentLocaleMessages*.properties} files through a GWT {@code ClientBundle} and a
 * minimal properties parser (GWT's JRE emulation doesn't include {@code ResourceBundle}).
 *
 * <p>The property files under {@code org/edumips64/utils/} are therefore the single source of
 * truth for translations in both builds.
 */
public class CurrentLocale {

  private static final Logger logger = Logger.getLogger(CurrentLocale.class.getName());

  private static final LocalizationProvider provider = new PropertiesLocalizationProvider();

  private static ConfigStore config;

  public static void setConfig(ConfigStore config) {
    CurrentLocale.config = config;
  }

  public static String getString(String key) {
    String lang = "en";
    if (config != null) {
      String configured = config.getString(ConfigKey.LANGUAGE);
      if ("en".equals(configured) || "it".equals(configured) || "zhcn".equals(configured)) {
        lang = configured;
      } else {
        logger.severe("Could not find language " + configured + ", defaulting to English");
      }
    }
    return provider.getString(lang, key);
  }
}
