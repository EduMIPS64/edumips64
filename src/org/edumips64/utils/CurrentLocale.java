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

import java.util.*;
import java.io.*;
import java.net.URL;
import java.util.logging.Logger;

public class CurrentLocale {

  static Map<String, Map<String, String>> languages;
  private static ConfigStore config = ConfigBuilder.getConfig();

  private static final Logger logger = Logger.getLogger(CurrentLocale.class.getName());

  static {
    languages = new HashMap<String, Map<String, String>>();
    languages.put("en", new HashMap<String, String>());
    languages.put("it", new HashMap<String, String>());

    try {
      loadMessages("en", languages.get("en"));
    } catch (Exception e) {
      logger.severe("Could not load the English localization: " + e);
    }

    try {
      loadMessages("it", languages.get("it"));
    } catch (Exception e) {
      logger.severe("Could not load the Italian localization: " + e);
    }
  }

  public static void loadMessages(String filename, Map<String, String> map) throws FileNotFoundException, IOException {
    String line;

    URL url = CurrentLocale.class.getResource("MessagesBundle_" + filename + ".properties");
    InputStreamReader isr = new InputStreamReader(url.openStream());
    BufferedReader br = new BufferedReader(isr);

    while ((line = br.readLine()) != null) {
      String[] l = line.split("=", 2);
      map.put(l[0].trim(), l[1].trim());
    }

    br.close();
    isr.close();
  }

  public static void setLanguage(String language) {
    config.putString("language", language);
  }

  public static String getString(String key) {
    String lang_name = config.getString("language");

    try {
      Map<String, String> lang = languages.get(lang_name);
      return lang.get(key);
    } catch (Exception e) {
      logger.severe("Could not look up find language " + lang_name + "; key: " + key);
      return key;
    }
  }

  public static boolean isSelected(String lan) {
    return config.getString("language").equals(lan);
  }
}
