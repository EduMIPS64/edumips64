/* ConfigStore.java
 *
 * Common interface for classes implementing mechanisms to handle user
 * preferences.
 * (c) 2006-2013 EduMIPS64 project - Andrea Spadaccini
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
import java.util.Map;
import java.util.logging.Logger;

/** Interface representing a configuration store for the currently running
 * instance of EduMIPS64. Users of the interface should get the configuration
 * storage object by calling the getConfig method of ConfigManager.
 * */
public abstract class ConfigStore {
  public static Map<String, Object> defaults;
  static {
    ConfigStore.defaults = new HashMap<>();

    // Global parameters.
    ConfigStore.defaults.put("language", "en");
    ConfigStore.defaults.put("files", "");
    // TODO(andrea): this will create problems in the applet, and needs to be
    // encapsulated in some way.
    ConfigStore.defaults.put("lastdir", System.getProperty("user.dir", ""));
    ConfigStore.defaults.put("dineroIV", "dineroIV");
    ConfigStore.defaults.put("serialNumber", 0);

    // Colors.
    ConfigStore.defaults.put("IFColor", -256);                // Color.yellow.getRGB())
    ConfigStore.defaults.put("IDColor", -16746256);           // Color(0, 120, 240).getRGB());
    ConfigStore.defaults.put("EXColor", -65536);              // Color.red.getRGB());
    ConfigStore.defaults.put("MEMColor", -16711936);          // Color.green.getRGB());
    ConfigStore.defaults.put("FPAdderColor", -16744448);      // Color(0, 128, 0).getRGB());
    ConfigStore.defaults.put("FPMultiplierColor",-16744320);  // Color(0, 128, 128).getRGB());
    ConfigStore.defaults.put("FPDividerColor", -8355840);     // Color(128, 128, 0).getRGB());
    ConfigStore.defaults.put("WBColor", -5111630);            // Color.magenta.darker().getRGB());
    ConfigStore.defaults.put("RAWColor", -16776961);          // Color.blue.brighter().getRGB());
    ConfigStore.defaults.put("SAMEIFColor", -6908236);        // Color(150, 150, 180).getRGB());

    // Simulation parameters.
    ConfigStore.defaults.put("forwarding", false);
    ConfigStore.defaults.put("warnings", false);
    ConfigStore.defaults.put("verbose", true);
    ConfigStore.defaults.put("syncexc-masked", false);
    ConfigStore.defaults.put("syncexc-terminate", false);
    ConfigStore.defaults.put("n_step", 4);
    ConfigStore.defaults.put("sleep_interval", 10);
    ConfigStore.defaults.put("show_aliases", false);

    // FPU exceptions defaults.
    ConfigStore.defaults.put("INVALID_OPERATION", true);
    ConfigStore.defaults.put("OVERFLOW", true);
    ConfigStore.defaults.put("UNDERFLOW", true);
    ConfigStore.defaults.put("DIVIDE_BY_ZERO", true);

    // FPU Rounding mode defaults.
    ConfigStore.defaults.put("NEAREST", false);
    ConfigStore.defaults.put("TOWARDZERO", true);
    ConfigStore.defaults.put("TOWARDS_PLUS_INFINITY", false);
    ConfigStore.defaults.put("TOWARDS_MINUS_INFINITY", false);

    // How to show memory cells containing floating point values.
    ConfigStore.defaults.put("LONGDOUBLEVIEW", true);  // long=true  double=false
  }

  // The interface exposes getter and setter methods for all the supported
  // types (String, int, boolean, Color). Color is serialized as an int.
  public abstract void putString(String key, String value);
  public abstract String getString(String key);

  public abstract void putInt(String key, int value);
  public abstract int getInt(String key);

  public abstract void putBoolean(String key, boolean value);
  public abstract boolean getBoolean(String key);

  protected static final Logger logger = Logger.getLogger(ConfigStore.class.getName());

  // Generic utility function to populate a ConfigStore object from a set of
  // <String, Object> pairs.
  public void mergeFromGenericMap(Map<String, Object> values) throws ConfigStoreTypeException {
    for (Map.Entry<String, Object> item : values.entrySet()) {
      String key = item.getKey();
      Object value = item.getValue();

      if (value instanceof String) {
        putString(key, (String) value);
      } else if (value instanceof Integer) {
        putInt(key, (Integer) value);
      } else if (value instanceof Boolean) {
        putBoolean(key, (Boolean) value);
      } else {
        throw new ConfigStoreTypeException(); //"Unknown type for value " + value + " (" + key + ")");
      }
    }
  }

  // Reset configuration.
  public void resetConfiguration() {
    try {
      mergeFromGenericMap(defaults);
    } catch (ConfigStoreTypeException e) {
      // This should never happen, as the defaults are static and defined at
      // compile time.
      logger.severe("Type error while loading the defaults.");
    }
  }
}
