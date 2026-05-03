package org.edumips64.utils.cli;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Verifies that every topic listed in {@code topics.index} resolves to a
 * non-empty resource bundled in the JAR. This guards the hermetic-build
 * contract: the rst chapters served by the in-shell {@code help} browser
 * are exactly the ones Sphinx uses to build the website / PDF.
 */
public class HelpTopicsTest {

  @Test
  public void indexLoadsAtLeastOneTopic() {
    Map<String, HelpCommand.Topic> idx = HelpCommand.loadIndex();
    assertFalse("topics.index should not be empty", idx.isEmpty());
  }

  @Test
  public void everyIndexedTopicResolvesToNonEmptyResource() throws Exception {
    Map<String, HelpCommand.Topic> idx = HelpCommand.loadIndex();
    for (HelpCommand.Topic t : idx.values()) {
      String path = HelpCommand.TOPIC_RESOURCE_PREFIX + t.resource;
      try (InputStream in = HelpCommand.class.getResourceAsStream(path)) {
        assertNotNull("missing resource for topic '" + t.id + "': " + path, in);
        BufferedReader br = new BufferedReader(
            new InputStreamReader(in, StandardCharsets.UTF_8));
        String first = br.readLine();
        assertNotNull("empty resource for topic '" + t.id + "'", first);
      }
    }
  }
}
