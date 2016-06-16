package org.edumips64.utils;

import java.util.*;

/** Configuration builder, to be used to obtain an instance of ConfigStore. */
public class ConfigManager {
  private static ConfigStore instance;

  public static final Map<String, Object> defaults;

  static {
    defaults = new HashMap<>();

    // Global parameters.
    defaults.put("language", "en");
    defaults.put("files", "");
    // TODO(andrea): this will create problems in the applet, and needs to be
    // encapsulated in some way.
    defaults.put("lastdir", System.getProperty("user.dir", ""));
    defaults.put("dineroIV", "dineroIV");
    defaults.put("serialNumber", 0);

    // Colors.
    defaults.put("IFColor", -256);                // Color.yellow.getRGB())
    defaults.put("IDColor", -16746256);           // Color(0, 120, 240).getRGB());
    defaults.put("EXColor", -65536);              // Color.red.getRGB());
    defaults.put("MEMColor", -16711936);          // Color.green.getRGB());
    defaults.put("FPAdderColor", -16744448);      // Color(0, 128, 0).getRGB());
    defaults.put("FPMultiplierColor",-16744320);  // Color(0, 128, 128).getRGB());
    defaults.put("FPDividerColor", -8355840);     // Color(128, 128, 0).getRGB());
    defaults.put("WBColor", -5111630);            // Color.magenta.darker().getRGB());
    defaults.put("RAWColor", -16776961);          // Color.blue.brighter().getRGB());
    defaults.put("SAMEIFColor", -6908236);        // Color(150, 150, 180).getRGB());

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
    if (instance == null) {
      // TODO: a custom exception time would be nice, but this will do.
      throw new RuntimeException("The default ConfigStore was not initialized.");
    }
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
