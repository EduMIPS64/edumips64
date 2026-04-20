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
    assertNull(pipeline.IF());
    assertNull(pipeline.ID());
    assertNull(pipeline.EX());
    assertNull(pipeline.MEM());
    assertNull(pipeline.WB());
  }

  @Test
  public void testNonEmptySize() {
    assertEquals(0, pipeline.size());
    pipeline.setStage(Pipeline.Stage.IF, instructionBuilder.buildInstruction("ADD"));
    pipeline.setStage(Pipeline.Stage.ID, instructionBuilder.buildInstruction("ADD"));
    assertEquals(2, pipeline.size());
  }

  @Test
  public void testSizeIncreaseWithBubble() {
    assertEquals(0, pipeline.size());
    pipeline.setStage(Pipeline.Stage.IF, new BUBBLE());
    assertEquals(1, pipeline.size());
  }

  @Test
  public void testSizeIncreaseWithOtherInstructions() {
    assertEquals(0, pipeline.size());
    pipeline.setStage(Pipeline.Stage.IF, instructionBuilder.buildInstruction("ADD"));
    assertEquals(1, pipeline.size());
  }

  @Test
  public void testIsBubble() {
    pipeline.setStage(Pipeline.Stage.IF, new BUBBLE());
    assertTrue(pipeline.isBubble(Pipeline.Stage.IF));
    assertTrue(pipeline.isEmptyOrBubble(Pipeline.Stage.IF));

    pipeline.setStage(Pipeline.Stage.IF, instructionBuilder.buildInstruction("ADD"));
    assertFalse(pipeline.isBubble(Pipeline.Stage.IF));
    assertFalse(pipeline.isEmptyOrBubble(Pipeline.Stage.IF));
  }

  @Test
  public void testClear() {
    assertEquals(0, pipeline.size());
    for (Pipeline.Stage stage : Pipeline.Stage.values()) {
      pipeline.setStage(stage, new BUBBLE());
    }
    assertEquals(5, pipeline.size());

    pipeline.clear();
    assertEquals(0, pipeline.size());
    for (Pipeline.Stage stage : Pipeline.Stage.values()) {
      assertNull(pipeline.get(stage));
    }
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
      localPipeline.setStage(stage, firstInstr);
      try {
        localPipeline.setStage(stage, secondInstr);
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
      localPipeline.setStage(stage, instructionBuilder.buildInstruction("ADD"));
      assertNotNull(localPipeline.get(stage));
      localPipeline.setStage(stage, null);
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
      localPipeline.setStage(stage, instructionBuilder.buildInstruction("ADD"));
      localPipeline.setStage(stage, new BUBBLE());
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
      localPipeline.setStage(stage, instructionBuilder.buildInstruction("ADD"));
      assertFalse(localPipeline.isEmpty(stage));

      // Reset and test bubble -> real.
      localPipeline.clear();
      localPipeline.setStage(stage, new BUBBLE());
      localPipeline.setStage(stage, instructionBuilder.buildInstruction("ADD"));
      assertFalse(localPipeline.isEmpty(stage));
      assertFalse(localPipeline.isBubble(stage));
    }
  }

  // Setting null on an already empty stage is a no-op and must not throw.
  @Test
  public void testSetAllowsNullOnEmptyStage() {
    Pipeline localPipeline = new Pipeline();
    for (Pipeline.Stage stage : Pipeline.Stage.values()) {
      localPipeline.setStage(stage, null);
      assertTrue(localPipeline.isEmpty(stage));
    }
  }

  // advance() moves the instruction from one stage to the next, clearing the
  // source stage. The destination must be empty or contain a bubble.
  @Test
  public void testAdvance() {
    InstructionInterface instr = instructionBuilder.buildInstruction("ADD");
    pipeline.setStage(Pipeline.Stage.IF, instr);
    pipeline.advance(Pipeline.Stage.IF, Pipeline.Stage.ID);
    assertNull("Source stage must be empty after advance.", pipeline.IF());
    assertEquals("Destination stage must contain the advanced instruction.",
        instr, pipeline.ID());
  }

  // advance() is allowed when the destination contains a bubble: the bubble
  // is replaced by the advancing instruction.
  @Test
  public void testAdvanceOverBubble() {
    InstructionInterface instr = instructionBuilder.buildInstruction("ADD");
    pipeline.setStage(Pipeline.Stage.IF, instr);
    pipeline.setStage(Pipeline.Stage.ID, new BUBBLE());
    pipeline.advance(Pipeline.Stage.IF, Pipeline.Stage.ID);
    assertNull(pipeline.IF());
    assertEquals(instr, pipeline.ID());
  }

  // advance() refuses to overwrite a real instruction in the destination
  // stage, preserving the overwrite invariant.
  @Test
  public void testAdvanceRefusesToOverwriteRealInstruction() {
    InstructionInterface src = instructionBuilder.buildInstruction("ADD");
    InstructionInterface dst = instructionBuilder.buildInstruction("DADD");
    pipeline.setStage(Pipeline.Stage.IF, src);
    pipeline.setStage(Pipeline.Stage.ID, dst);
    try {
      pipeline.advance(Pipeline.Stage.IF, Pipeline.Stage.ID);
      fail("Expected IllegalStateException when advancing into a stage that "
          + "already contains a real instruction.");
    } catch (IllegalStateException expected) {
      // Both stages must be unchanged.
      assertEquals(src, pipeline.IF());
      assertEquals(dst, pipeline.ID());
    }
  }

  // advance() from an empty stage is allowed and simply clears the
  // destination (moving null into it).
  @Test
  public void testAdvanceFromEmptyStage() {
    pipeline.advance(Pipeline.Stage.IF, Pipeline.Stage.ID);
    assertNull(pipeline.IF());
    assertNull(pipeline.ID());
  }

  // clear(Stage) empties a single stage regardless of its content.
  @Test
  public void testClearStage() {
    InstructionInterface instr = instructionBuilder.buildInstruction("ADD");
    pipeline.setStage(Pipeline.Stage.WB, instr);
    assertEquals(instr, pipeline.clear(Pipeline.Stage.WB));
    assertNull(pipeline.WB());
    assertTrue(pipeline.isEmpty(Pipeline.Stage.WB));
  }

  // flushAndSet() explicitly discards the previous content (even if it is a
  // real instruction) and writes the new one. This is the escape hatch for
  // legitimate flushes such as the jump handler.
  @Test
  public void testFlushAndSetReplacesRealInstruction() {
    InstructionInterface first = instructionBuilder.buildInstruction("ADD");
    InstructionInterface second = instructionBuilder.buildInstruction("DADD");
    pipeline.setStage(Pipeline.Stage.IF, first);
    assertEquals(first, pipeline.flushAndSet(Pipeline.Stage.IF, second));
    assertEquals(second, pipeline.IF());
  }

  @Test
  public void testFlushAndSetOnEmptyStage() {
    InstructionInterface instr = instructionBuilder.buildInstruction("ADD");
    assertNull(pipeline.flushAndSet(Pipeline.Stage.IF, instr));
    assertEquals(instr, pipeline.IF());
  }
}