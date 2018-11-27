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
  public static Map<ConfigKey, Object> defaults;
  static {
    ConfigStore.defaults = new HashMap<>();

    // Global parameters.
    ConfigStore.defaults.put(ConfigKey.LANGUAGE, "en");
    ConfigStore.defaults.put(ConfigKey.FILES, "");
    // TODO(andrea): this will create problems in the applet, and needs to be
    // encapsulated in some way.
    ConfigStore.defaults.put(ConfigKey.LAST_DIR, System.getProperty("user.dir", ""));
    ConfigStore.defaults.put(ConfigKey.DINERO, "dineroIV");
    ConfigStore.defaults.put(ConfigKey.SERIAL_NUMBER, 0);

    // Colors.
    ConfigStore.defaults.put(ConfigKey.IF_COLOR, -256);                // Color.yellow.getRGB())
    ConfigStore.defaults.put(ConfigKey.ID_COLOR, -16746256);           // Color(0, 120, 240).getRGB());
    ConfigStore.defaults.put(ConfigKey.EX_COLOR, -65536);              // Color.red.getRGB());
    ConfigStore.defaults.put(ConfigKey.MEM_COLOR, -16711936);          // Color.green.getRGB());
    ConfigStore.defaults.put(ConfigKey.FP_ADDER_COLOR, -16744448);      // Color(0, 128, 0).getRGB());
    ConfigStore.defaults.put(ConfigKey.FP_MULTIPLIER_COLOR,-16744320);  // Color(0, 128, 128).getRGB());
    ConfigStore.defaults.put(ConfigKey.FP_DIVIDER_COLOR, -8355840);     // Color(128, 128, 0).getRGB());
    ConfigStore.defaults.put(ConfigKey.WB_COLOR, -5111630);            // Color.magenta.darker().getRGB());
    ConfigStore.defaults.put(ConfigKey.RAW_COLOR, -16776961);          // Color.blue.brighter().getRGB());
    ConfigStore.defaults.put(ConfigKey.SAME_IF_COLOR, -6908236);        // Color(150, 150, 180).getRGB());

    // Simulation parameters.
    ConfigStore.defaults.put(ConfigKey.FORWARDING, false);
    ConfigStore.defaults.put(ConfigKey.WARNINGS, false);
    ConfigStore.defaults.put(ConfigKey.VERBOSE, true);
    ConfigStore.defaults.put(ConfigKey.SYNC_EXCEPTIONS_MASKED, false);
    ConfigStore.defaults.put(ConfigKey.SYNC_EXCEPTIONS_TERMINATE, false);
    ConfigStore.defaults.put(ConfigKey.N_STEPS, 4);
    ConfigStore.defaults.put(ConfigKey.SLEEP_INTERVAL, 10);

    // FPU exceptions defaults.
    ConfigStore.defaults.put(ConfigKey.FP_INVALID_OPERATION, true);
    ConfigStore.defaults.put(ConfigKey.FP_OVERFLOW, true);
    ConfigStore.defaults.put(ConfigKey.FP_UNDERFLOW, true);
    ConfigStore.defaults.put(ConfigKey.FP_DIVIDE_BY_ZERO, true);

    // Branch Prediction mode defaults
    ConfigStore.defaults.put(ConfigKey.PRED_UNTAKEN, true);
    ConfigStore.defaults.put(ConfigKey.PRED_TAKEN, false);
    ConfigStore.defaults.put(ConfigKey.PRED_2BIT, false);
    ConfigStore.defaults.put(ConfigKey.PRED_DYNAMIC21, false);

    // FPU Rounding mode defaults.
    ConfigStore.defaults.put(ConfigKey.FP_NEAREST, false);
    ConfigStore.defaults.put(ConfigKey.FP_TOWARDS_ZERO, true);
    ConfigStore.defaults.put(ConfigKey.FP_TOWARDS_PLUS_INFINITY, false);
    ConfigStore.defaults.put(ConfigKey.FP_TOWARDS_MINUS_INFINITY, false);

    // How to show memory cells containing floating point values.
    ConfigStore.defaults.put(ConfigKey.FP_LONG_DOUBLE_VIEW, true);  // long=true  double=false

    // UI font options.
    ConfigStore.defaults.put(ConfigKey.UI_FONT_SIZE, 18);
  }

  // The interface exposes getter and setter methods for all the supported
  // types (String, int, boolean, Color). Color is serialized as an int.
  public abstract void putString(ConfigKey key, String value);
  public abstract String getString(ConfigKey key);

  public abstract void putInt(ConfigKey key, int value);
  public abstract int getInt(ConfigKey key);

  public abstract void putBoolean(ConfigKey key, boolean value);
  public abstract boolean getBoolean(ConfigKey key);

  protected static final Logger logger = Logger.getLogger(ConfigStore.class.getName());

  // Generic utility function to populate a ConfigStore object from a set of
  // <String, Object> pairs.
  public void mergeFromGenericMap(Map<ConfigKey, Object> values) throws ConfigStoreTypeException {
    for (Map.Entry<ConfigKey, Object> item : values.entrySet()) {
      ConfigKey key = item.getKey();
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

  public static boolean isValid(Map<ConfigKey, Object> values) throws ConfigStoreTypeException {
    // Invalid configuration: mask synchronous exception and terminate on synchronous exceptions.

    // Can return true if the map does not contain both keys.
    if (!(values.containsKey(ConfigKey.SYNC_EXCEPTIONS_MASKED) && values.containsKey(ConfigKey.SYNC_EXCEPTIONS_TERMINATE))) {
      return true;
    }

    Object masked_value = values.get(ConfigKey.SYNC_EXCEPTIONS_MASKED);
    Object terminate_value = values.get(ConfigKey.SYNC_EXCEPTIONS_TERMINATE);

    if (!((masked_value instanceof Boolean) && (terminate_value instanceof Boolean))) {
      throw new ConfigStoreTypeException();
    }

    boolean masked = (Boolean) masked_value;
    boolean terminate = (Boolean) terminate_value;

    return !(masked && terminate);
  }
}
