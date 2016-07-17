package org.edumips64.core;

import org.edumips64.BaseTest;
import org.edumips64.core.is.BUBBLE;
import org.junit.Test;

import static org.junit.Assert.*;

public class PipelineTest extends BaseTest {
  private Pipeline pipeline = new Pipeline();

  @Test
  public void testEmptySize() {
    assertEquals(0, pipeline.size());
  }

  @Test
  public void testSizeIncreaseWithBubble() {
    pipeline.setIF(new BUBBLE());
    assertEquals(1, pipeline.size());
  }

  @Test
  public void testIsBubble() {
    pipeline.setIF(new BUBBLE());
    assertTrue(pipeline.isBubble(CPU.PipeStage.IF));
    assertTrue(pipeline.isEmptyOrBubble(CPU.PipeStage.IF));
  }
}