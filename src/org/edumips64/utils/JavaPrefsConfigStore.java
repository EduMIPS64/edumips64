package org.edumips64.utils;

import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

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
