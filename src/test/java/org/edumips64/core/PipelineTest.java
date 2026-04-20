package org.edumips64.core;

import org.edumips64.BaseWithInstructionBuilderTest;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.InstructionInterface;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

  // Overwriting a real instruction with another real instruction should fail,
  // because that was the root cause of bug #304 (an instruction was silently
  // overwritten while still in the pipeline).
  @Test
  public void testSetRefusesToOverwriteRealInstruction() {
    for (Pipeline.Stage stage : Pipeline.Stage.values()) {
      Pipeline localPipeline = new Pipeline();
      InstructionInterface firstInstr = instructionBuilder.buildInstruction("ADD");
      InstructionInterface secondInstr = instructionBuilder.buildInstruction("DADD");
      setStage(localPipeline, stage, firstInstr);
      try {
        setStage(localPipeline, stage, secondInstr);
        fail("Expected IllegalStateException when overwriting stage " + stage
            + " containing a real instruction with another real instruction.");
      } catch (IllegalStateException expected) {
        // The original instruction must still be in the stage.
        assertEquals("Stage " + stage + " must retain the original instruction after a "
            + "failed overwrite.", firstInstr, localPipeline.get(stage));
      }
    }
  }

  // Clearing a real instruction (setting the stage to null) is always allowed.
  @Test
  public void testSetAllowsClearingRealInstruction() {
    for (Pipeline.Stage stage : Pipeline.Stage.values()) {
      Pipeline localPipeline = new Pipeline();
      setStage(localPipeline, stage, instructionBuilder.buildInstruction("ADD"));
      assertNotNull(localPipeline.get(stage));
      setStage(localPipeline, stage, null);
      assertNull("Stage " + stage + " must be clearable via null.",
          localPipeline.get(stage));
    }
  }

  // Replacing a real instruction with a bubble is always allowed (used for
  // pipeline flushes, e.g., after a jump).
  @Test
  public void testSetAllowsReplacingRealInstructionWithBubble() {
    for (Pipeline.Stage stage : Pipeline.Stage.values()) {
      Pipeline localPipeline = new Pipeline();
      setStage(localPipeline, stage, instructionBuilder.buildInstruction("ADD"));
      InstructionInterface bubble = new BUBBLE();
      setStage(localPipeline, stage, bubble);
      assertTrue("Stage " + stage + " must contain a bubble after flush.",
          localPipeline.isBubble(stage));
    }
  }

  // Filling an empty stage or replacing a bubble with a real instruction is
  // always allowed (normal pipeline progression).
  @Test
  public void testSetAllowsFillingEmptyOrBubbleStage() {
    for (Pipeline.Stage stage : Pipeline.Stage.values()) {
      Pipeline localPipeline = new Pipeline();
      // Empty -> real.
      setStage(localPipeline, stage, instructionBuilder.buildInstruction("ADD"));
      assertFalse(localPipeline.isEmpty(stage));

      // Reset and test bubble -> real.
      localPipeline.clear();
      setStage(localPipeline, stage, new BUBBLE());
      setStage(localPipeline, stage, instructionBuilder.buildInstruction("ADD"));
      assertFalse(localPipeline.isEmpty(stage));
      assertFalse(localPipeline.isBubble(stage));
    }
  }

  // Setting null on an already empty stage is a no-op and must not throw.
  @Test
  public void testSetAllowsNullOnEmptyStage() {
    Pipeline localPipeline = new Pipeline();
    for (Pipeline.Stage stage : Pipeline.Stage.values()) {
      setStage(localPipeline, stage, null);
      assertTrue(localPipeline.isEmpty(stage));
    }
  }

  private void setStage(Pipeline p, Pipeline.Stage stage, InstructionInterface instr) {
    switch (stage) {
      case IF: p.setIF(instr); break;
      case ID: p.setID(instr); break;
      case EX: p.setEX(instr); break;
      case MEM: p.setMEM(instr); break;
      case WB: p.setWB(instr); break;
    }
  }
}