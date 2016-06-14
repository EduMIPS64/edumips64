package org.edumips64.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** ConfigStore implementation based on in-memory storage. */
public class InMemoryConfigStore extends ConfigStore {
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
