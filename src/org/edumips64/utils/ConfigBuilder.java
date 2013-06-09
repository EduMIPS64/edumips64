
package org.edumips64.utils;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

// Configuration builder, to be used by client code to obtain an instance of
// ConfigStore.
public class ConfigBuilder {
  private static final Logger logger = Logger.getLogger(ConfigBuilder.class.getName());
  private static ConfigStore instance;

  static final Map<String, Object> defaults;

  static {
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

    instance = new JavaPrefsConfigStore(defaults);
    // TODO: Choose which implementation to use. If the one based on the Java
  }

  public static ConfigStore getConfig() {
    return instance;
  }

}

class JavaPrefsConfigStore extends ConfigStore {
  private static final Logger logger = Logger.getLogger(JavaPrefsConfigStore.class.getName());
  private Preferences prefs;
  private Map<String, Object> defaults;

  public JavaPrefsConfigStore(Map<String, Object> defaults) {
    prefs = Preferences.userRoot().node("edumips64.config");
    this.defaults = defaults;
  }

  public void putString(String key, String value) {
    prefs.put(key, value);
  }

  public String getString(String key) {
    String default_value = "";

    if (defaults.containsKey(key)) {
      default_value = (String) defaults.get(key);
    } else {
      logger.warning("No default value for string configuration key " + key + ", using empty string.");
    }

    return prefs.get(key, default_value);
  }

  public void putInt(String key, int value) {
    prefs.putInt(key, value);
  }

  public int getInt(String key) {
    int default_value = 0;

    if (defaults.containsKey(key)) {
      default_value = (Integer) defaults.get(key);
    } else {
      logger.warning("No default value for integer configuration key " + key + ", using 0.");
    }

    return prefs.getInt(key, default_value);
  }

  public void putBoolean(String key, boolean value) {
    prefs.putBoolean(key, value);
  }

  public boolean getBoolean(String key) {
    boolean default_value = false;

    if (defaults.containsKey(key)) {
      default_value = (Boolean) defaults.get(key);
    } else {
      logger.warning("No default value for boolean configuration key " + key + ", using false.");
    }

    return prefs.getBoolean(key, default_value);
  }
}
