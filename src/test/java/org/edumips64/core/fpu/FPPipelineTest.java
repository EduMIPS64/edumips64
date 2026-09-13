package org.edumips64.core.fpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.edumips64.core.BitSet32;
import org.edumips64.core.is.InstructionInterface;
import org.edumips64.core.is.ParsedInstructionMetadata;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for FPPipeline, focused on the configurable divider latency
 * (see https://github.com/EduMIPS64/edumips64/issues/709).
 */
public class FPPipelineTest {
  private FPPipeline fp;

  @Before
  public void setUp() {
    fp = new FPPipeline();
    fp.reset();
  }

  /** Simulates the per-cycle sequence the CPU class uses: check for a completed
   *  instruction, then shift the pipeline. Returns the number of cycles it took
   *  for the given instruction to be reported as completed. */
  private int cyclesToComplete() {
    int cycles = 0;
    InstructionInterface completed;
    do {
      cycles++;
      completed = fp.getCompletedInstruction();
      fp.step();
    } while (completed == null);
    return cycles;
  }

  @Test
  public void defaultDividerLatencyIsUnchanged() {
    fp.putInstruction(new FakeFPInstruction("DIV.D"), false);
    assertEquals("Default DIV.D latency must stay at 24 cycles", 24, cyclesToComplete());
  }

  @Test
  public void dividerLatencyIsConfigurable() {
    fp.setDividerLatency(5);
    fp.putInstruction(new FakeFPInstruction("DIV.D"), false);
    assertEquals(5, cyclesToComplete());
  }

  @Test
  public void dividerLatencyChangeDoesNotAffectInFlightInstruction() {
    InstructionInterface inFlight = new FakeFPInstruction("DIV.D");
    fp.putInstruction(inFlight, false);

    // Changing the setting mid-flight must not affect the instruction already
    // in the divider: it keeps the latency that was in effect when it was issued.
    fp.setDividerLatency(5);

    assertEquals(24, cyclesToComplete());
  }

  @Test
  public void nonPositiveLatencyIsClampedToOne() {
    fp.setDividerLatency(0);
    fp.putInstruction(new FakeFPInstruction("DIV.D"), false);
    assertEquals(1, cyclesToComplete());
  }

  @Test
  public void dividerRejectsSecondInstructionWhileBusy() {
    fp.putInstruction(new FakeFPInstruction("DIV.D"), false);
    int result = fp.putInstruction(new FakeFPInstruction("DIV.D"), false);
    assertEquals("Structural hazard on the divider must be reported", 2, result);
  }

  @Test
  public void completedInstructionIsTheOneThatWasIssued() {
    InstructionInterface instr = new FakeFPInstruction("DIV.D");
    fp.setDividerLatency(3);
    fp.putInstruction(instr, false);

    InstructionInterface completed = null;
    for (int i = 0; i < 3 && completed == null; i++) {
      completed = fp.getCompletedInstruction();
      fp.step();
    }

    assertSame(instr, completed);
    assertNull("The divider must be empty again after completion", fp.getCompletedInstruction());
  }

  /** Minimal InstructionInterface stub: only getName() matters to FPPipeline. */
  private static class FakeFPInstruction implements InstructionInterface {
    private final String name;

    FakeFPInstruction(String name) {
      this.name = name;
    }

    @Override
    public boolean ID() {
      return false;
    }

    @Override
    public void EX() {}

    @Override
    public void MEM() {}

    @Override
    public void WB() {}

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getLabel() {
      return null;
    }

    @Override
    public String getComment() {
      return null;
    }

    @Override
    public String getFullName() {
      return name;
    }

    @Override
    public int getSerialNumber() {
      return 0;
    }

    @Override
    public BitSet32 getRepr() {
      return null;
    }

    @Override
    public boolean isBubble() {
      return false;
    }

    @Override
    public ParsedInstructionMetadata getParsingMetadata() {
      return null;
    }

    @Override
    public void setLabel(String label) {}
  }
}
