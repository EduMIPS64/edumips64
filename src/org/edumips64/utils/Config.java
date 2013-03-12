/* Config.java
 *
 * This class manages the user settings.
 * (c) 2006-2013 EduMIPS64 project - Rizzo Vanni G., Andrea Spadaccini
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

import org.edumips64.Main;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/** This class manage the org.edumips64.config file
 * used for saving user preferences (like language, etc)
 */
public class Config {
  private static Map<String, Object> defaults;
  private static Preferences prefs;
  private static final Logger logger = Logger.getLogger(Config.class.getName());

  static {
    prefs = Preferences.userRoot().node("edumips64.config");
    defaults = new HashMap<String, Object>();

    // Global parameters.
    defaults.put("language", "en");
    defaults.put("files", "");
    defaults.put("lastdir", System.getProperty("user.dir"));
    defaults.put("dineroIV", "dineroIV");
    defaults.put("serialNumber", 0);

    // Colors.
    defaults.put("IFColor", Color.yellow.getRGB());
    defaults.put("IDColor", new Color(0, 120, 240).getRGB());
    defaults.put("EXColor", Color.red.getRGB());
    defaults.put("MEMColor", Color.green.getRGB());
    defaults.put("FPAdderColor", new Color(0, 128, 0).getRGB());
    defaults.put("FPMultiplierColor", new Color(0, 128, 128).getRGB());
    defaults.put("FPDividerColor", new Color(128, 128, 0).getRGB());
    defaults.put("WBColor", Color.magenta.darker().getRGB());
    defaults.put("RAWColor", Color.blue.brighter().getRGB());
    defaults.put("SAMEIFColor", new Color(150, 150, 180).getRGB());

    // Simulation parameters.
    defaults.put("forwarding", false);
    defaults.put("warnings", false);
    defaults.put("verbose", true);
    defaults.put("syncexc-masked", false);
    defaults.put("syncexc-terminate", false);
    defaults.put("n_step", 4);
    defaults.put("sleep_interval", 10);
    defaults.put("show_aliases", false);

    // FPU exceptions defaults.
    defaults.put("INVALID_OPERATION", true);
    defaults.put("OVERFLOW", true);
    defaults.put("UNDERFLOW", true);
    defaults.put("DIVIDE_BY_ZERO", true);

    // FPU Rounding mode defaults.
    defaults.put("NEAREST", false);
    defaults.put("TOWARDZERO", true);
    defaults.put("TOWARDS_PLUS_INFINITY", false);
    defaults.put("TOWARDS_MINUS_INFINITY", false);

    // How to show memory cells containing floating point values.
    defaults.put("LONGDOUBLEVIEW", true);  // long=true  double=false
  }

  // Getter/setter for each type.

  // ---- String
  public static void putString(String key, String value) {
    prefs.put(key, value);
  }

  public static String getString(String key) {
    String default_value = "";

    if (defaults.containsKey(key)) {
      default_value = (String) defaults.get(key);
    } else {
      logger.warning("No default value for string configuration key " + key + ", using empty string.");
    }

    return prefs.get(key, default_value);
  }

  // ---- Integer
  public static void putInt(String key, int value) {
    prefs.putInt(key, value);
  }

  public static int getInt(String key) {
    int default_value = 0;

    if (defaults.containsKey(key)) {
      default_value = (Integer) defaults.get(key);
    } else {
      logger.warning("No default value for integer configuration key " + key + ", using 0.");
    }

    return prefs.getInt(key, default_value);
  }

  // ---- Boolean
  public static void putBoolean(String key, boolean value) {
    prefs.putBoolean(key, value);
  }

  public static boolean getBoolean(String key) {
    boolean default_value = false;

    if (defaults.containsKey(key)) {
      default_value = (Boolean) defaults.get(key);
    } else {
      logger.warning("No default value for boolean configuration key " + key + ", using false.");
    }

    return prefs.getBoolean(key, default_value);
  }

  // ---- Color. Serialized as an int.
  public static void putColor(String key, Color value) {
    putInt(key, value.getRGB());
  }
  public static Color getColor(String key) {
    return new Color(getInt(key));
  }

  // Reset configuration.
  public static void resetConfiguration() {
    for (Map.Entry<String, Object> item : defaults.entrySet()) {
      String key = item.getKey();
      Object value = item.getValue();

      if (value instanceof String) {
        putString(key, (String) value);
      } else if (value instanceof Integer) {
        putInt(key, (Integer) value);
      } else if (value instanceof Boolean) {
        putBoolean(key, (Boolean) value);
      } else if (value instanceof Color) {
        putColor(key, (Color) value);
      } else {
        logger.severe("Unknown type for default value " + value + " (" + key + ")");
      }
    }
  }
}
