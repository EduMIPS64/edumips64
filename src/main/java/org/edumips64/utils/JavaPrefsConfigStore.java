package org.edumips64.utils;

import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/** ConfigStore implementation based on the Java Preferences API */
public class JavaPrefsConfigStore extends ConfigStore {
  private static final Logger logger = Logger.getLogger(JavaPrefsConfigStore.class.getName());
  private Preferences prefs;
  private Map<ConfigKey, Object> defaults;

  public JavaPrefsConfigStore(Map<ConfigKey, Object> defaults) {
    prefs = Preferences.userRoot().node("edumips64.config");
    this.defaults = defaults;
  }

  @Override
  public void putString(ConfigKey key, String value) {
    prefs.put(String.valueOf(key), value);
  }

  @Override
  public String getString(ConfigKey key) {
    String default_value = "";

    if (defaults.containsKey(key)) {
      default_value = (String) defaults.get(key);
    } else {
      logger.warning("No default value for string configuration key " + key + ", using empty string.");
    }

    return prefs.get(String.valueOf(key), default_value);
  }

  @Override
  public void putInt(ConfigKey key, int value) {
    prefs.putInt(String.valueOf(key), value);
  }

  @Override
  public int getInt(ConfigKey key) {
    int default_value = 0;

    if (defaults.containsKey(key)) {
      default_value = (Integer) defaults.get(key);
    } else {
      logger.warning("No default value for integer configuration key " + key + ", using 0.");
    }

    return prefs.getInt(String.valueOf(key), default_value);
  }

  @Override
  public void putBoolean(ConfigKey key, boolean value) {
    prefs.putBoolean(String.valueOf(key), value);
  }

  @Override
  public boolean getBoolean(ConfigKey key) {
    boolean default_value = false;

    if (defaults.containsKey(key)) {
      default_value = (Boolean) defaults.get(key);
    } else {
      logger.warning("No default value for boolean configuration key " + key + ", using false.");
    }

    return prefs.getBoolean(String.valueOf(key), default_value);
  }
}
