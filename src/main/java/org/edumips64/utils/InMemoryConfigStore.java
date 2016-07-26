package org.edumips64.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** ConfigStore implementation based on in-memory storage. */
public class InMemoryConfigStore extends ConfigStore {
  private static final Logger logger = Logger.getLogger(InMemoryConfigStore.class.getName());
  private Map<ConfigKey, Object> data;
  private Map<ConfigKey, Object> defaults;

  public InMemoryConfigStore(Map<ConfigKey, Object> defaults) {
    this.defaults = defaults;
    data = new HashMap<>();
  }

  @Override
  public void putString(ConfigKey key, String value) {
    data.put(key, value);
  }

  @Override
  public String getString(ConfigKey key) {
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

  @Override
  public void putInt(ConfigKey key, int value) {
    data.put(key, value);
  }

  @Override
  public int getInt(ConfigKey key) {
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

  @Override
  public void putBoolean(ConfigKey key, boolean value) {
    data.put(key, value);
  }

  @Override
  public boolean getBoolean(ConfigKey key) {
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
