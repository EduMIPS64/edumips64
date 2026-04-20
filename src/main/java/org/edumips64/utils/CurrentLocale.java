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

import java.util.logging.Logger;

/**
 * Static facade for localized UI message lookup.
 *
 * <p>If the {@link ConfigStore} attribute is set, the current language is fetched from it;
 * otherwise English is used as the default. The actual lookup is delegated to a
 * {@link LocalizationProvider}; the JVM build uses {@link ResourceBundleLocalizationProvider},
 * which reads the {@code CurrentLocaleMessages*.properties} files under
 * {@code org/edumips64/utils/}. Those property files are the single source of truth for the
 * application's translations; the GWT web worker reads the very same files through a
 * super-source override of this class (see {@code webclient.gwt.xml}).
 */
public class CurrentLocale {

  private static final Logger logger = Logger.getLogger(CurrentLocale.class.getName());

  private static final LocalizationProvider provider = new ResourceBundleLocalizationProvider();

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

