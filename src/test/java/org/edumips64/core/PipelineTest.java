package org.edumips64.core;

import org.edumips64.BaseWithInstructionBuilderTest;
import org.edumips64.core.is.BUBBLE;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class PipelineTest extends BaseWithInstructionBuilderTest {
  private Pipeline pipeline = new Pipeline();
  
  @Before
  public void testSetup() {
    super.testSetup();
  }

  @Test
  public void testEmptyPipeline() {
    assertEquals(0, pipeline.size());
    assertTrue(pipeline.isEmpty(Pipeline.Stage.IF));
    assertTrue(pipeline.isEmpty(Pipeline.Stage.ID));
    assertTrue(pipeline.isEmpty(Pipeline.Stage.EX));
    assertTrue(pipeline.isEmpty(Pipeline.Stage.MEM));
    assertTrue(pipeline.isEmpty(Pipeline.Stage.WB));
    
    // Test that even with an empty pipeline the getters work.
    assertEquals(null, pipeline.IF());
    assertEquals(null, pipeline.ID());
    assertEquals(null, pipeline.EX());
    assertEquals(null, pipeline.MEM());
    assertEquals(null, pipeline.WB());
  }
  
  @Test
  public void testNonEmptySize() {
    assertEquals(0, pipeline.size());
    pipeline.setIF(instructionBuilder.buildInstruction("ADD"));
    pipeline.setID(instructionBuilder.buildInstruction("ADD"));
    assertEquals(2, pipeline.size());
  }

  @Test
  public void testSizeIncreaseWithBubble() {
    assertEquals(0, pipeline.size());
    pipeline.setIF(new BUBBLE());
    assertEquals(1, pipeline.size());
  }
  
  @Test
  public void testSizeIncreaseWithOtherInstructions() {
    assertEquals(0, pipeline.size());
    pipeline.setIF(instructionBuilder.buildInstruction("ADD"));
    assertEquals(1, pipeline.size());
  }

  @Test
  public void testIsBubble() {
    pipeline.setIF(new BUBBLE());
    assertTrue(pipeline.isBubble(Pipeline.Stage.IF));
    assertTrue(pipeline.isEmptyOrBubble(Pipeline.Stage.IF));
    
    pipeline.setIF(instructionBuilder.buildInstruction("ADD"));
    assertFalse(pipeline.isBubble(Pipeline.Stage.IF));
    assertFalse(pipeline.isEmptyOrBubble(Pipeline.Stage.IF));
  }
  
  @Test
  public void testClear() {
    assertEquals(0, pipeline.size());
    pipeline.setIF(new BUBBLE());
    pipeline.setID(new BUBBLE());
    pipeline.setEX(new BUBBLE());
    pipeline.setMEM(new BUBBLE());
    pipeline.setWB(new BUBBLE());
    assertEquals(5, pipeline.size());
    
    pipeline.clear();
    assertEquals(0, pipeline.size());
    assertEquals(null, pipeline.IF());
    assertEquals(null, pipeline.ID());
    assertEquals(null, pipeline.EX());
    assertEquals(null, pipeline.MEM());
    assertEquals(null, pipeline.WB());
  }
}