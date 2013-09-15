package org.edumips64.utils;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/** Configuration builder, to be used to obtain an instance of ConfigStore. */
public class ConfigBuilder {
  private static final Logger logger = Logger.getLogger(ConfigBuilder.class.getName());
  private static ConfigStore instance;

  static final Map<String, Object> defaults;

  static {
    defaults = new HashMap<String, Object>();

    // Global parameters.
    defaults.put("language", "en");
    defaults.put("files", "");
    // TODO(andrea): this will create problems in the applet, and needs to be
    // encapsulated in some way.
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

    try {
      instance = new JavaPrefsConfigStore(defaults);
    } catch (Exception e) {
      logger.warning("Could not access the Java Preferences API. Using in-memory configuration storage. Error: " + e);
      instance = new InMemoryConfigStore(defaults);
    }
  }

  /** Factory method for ConfigStore instances.
   *
   * The class tries to build and return a ConfigStore object backed by
   * persistent storage. If it fails (e.g., because we are in a context were
   * our backends don't work) it will return an ephemeral ConfigStore
   * instance, stored in memory.
   *
   * @return an instance of a class derived by ConfigStore.
   */
  public static ConfigStore getConfig() {
    return instance;
  }

  /** Factory method for temporary ConfigStore instances.
   *
   * @return a memory-backed ConfigStore instance.
   */
  public static ConfigStore getTmpConfig() {
    return new InMemoryConfigStore(defaults);
  }

  /** Sets the global configuration object.
   */
  public static void setConfig(ConfigStore config) {
    instance = config;
  }
}

/** ConfigStore implementation based on the Java Preferences API */
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

/** ConfigStore implementation based on in-memory storage. */
class InMemoryConfigStore extends ConfigStore {
  private static final Logger logger = Logger.getLogger(InMemoryConfigStore.class.getName());
  private Map<String, Object> data;
  private Map<String, Object> defaults;

  public InMemoryConfigStore(Map<String, Object> defaults) {
    this.defaults = defaults;
    data = new HashMap<String, Object>();
  }

  public void putString(String key, String value) {
    data.put(key, value);
  }

  public String getString(String key) {
    if (data.containsKey(key)) {
      return (String) data.get(key);
    }

    String default_value = "";

    if (defaults.containsKey(key)) {
      default_value = (String) defaults.get(key);
    } else {
      logger.warning("No default value for string configuration key " + key + ", using empty string.");
    }

    return default_value;
  }

  public void putInt(String key, int value) {
    data.put(key, value);
  }

  public int getInt(String key) {
    if (data.containsKey(key)) {
      return (Integer) data.get(key);
    }

    int default_value = 0;

    if (defaults.containsKey(key)) {
      default_value = (Integer) defaults.get(key);
    } else {
      logger.warning("No default value for integer configuration key " + key + ", using 0.");
    }

    return default_value;
  }

  public void putBoolean(String key, boolean value) {
    data.put(key, value);
  }

  public boolean getBoolean(String key) {
    if (data.containsKey(key)) {
      return (Boolean) data.get(key);
    }

    boolean default_value = false;

    if (defaults.containsKey(key)) {
      default_value = (Boolean) defaults.get(key);
    } else {
      logger.warning("No default value for boolean configuration key " + key + ", using false.");
    }

    return default_value;
  }
}
