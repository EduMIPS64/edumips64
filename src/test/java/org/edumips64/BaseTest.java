package org.edumips64;

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.InMemoryConfigStore;
import org.junit.BeforeClass;

import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Base class for all tests.
 */
public class BaseTest {
  protected static ConfigStore config;
  private final static Logger log = Logger.getLogger(BaseTest.class.getName());

  @BeforeClass
  public static void setup() {
    config = new InMemoryConfigStore(ConfigStore.defaults);
    // Disable logs of level lesser than SEVERE.
    Logger rootLogger = log.getParent();

    for (Handler h : rootLogger.getHandlers()) {
      h.setLevel(java.util.logging.Level.SEVERE);
    }
  }

}
