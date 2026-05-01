/* MulDLoopWAWTest.java
 *
 * Reproduces the loop-WAW bug where the same mul.d InstructionInterface
 * object lives in two pipeline slots simultaneously and verifies that the
 * per-slot validity check on CycleBuilder tags drops the spurious WAW tag
 * from the FP multiplier slot.
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.edumips64;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import org.edumips64.client.CycleState;
import org.edumips64.core.CPU;
import org.edumips64.core.Pipeline;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.InstructionInterface;
import org.edumips64.core.parser.Parser;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.CycleBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * End-to-end regression test for the multiplier-stall mis-classification
 * bug.
 *
 * <p>The MIPS program {@code src/test/resources/mul.d-loop-waw.s} repeatedly
 * fetches the same {@code mul.d f1, f0, f0} instruction in a tight loop. The
 * parser builds a single {@code InstructionInterface} per source line, so
 * every loop iteration uses the same Java object. While iteration N's
 * {@code mul.d} progresses through the multiplier pipeline (M1..M7),
 * iteration N+1's {@code mul.d} sits {@code WAW}-stalled in {@code ID}
 * because the previous instance will eventually overwrite {@code f1}. Both
 * pipeline slots therefore share the same {@code serialNumber}, so
 * {@link CycleBuilder#getLastStateForSerial(int)} returns the most recently
 * written tag — {@code WAW} — for both slots.
 *
 * <p>This test runs the program one cycle at a time, finds a cycle where
 * {@code mul.d} is simultaneously in {@code ID} and in some {@code Mn}
 * stage, and asserts:
 *
 * <ul>
 *   <li>the cycle-builder really does report {@code WAW} for that serial
 *       (i.e. the bug is reproduced upstream of the slot-validity filter);</li>
 *   <li>the {@code WAW} tag is consistent with the {@code ID} slot, so
 *       {@code ID} would still render as stalled in the Web UI;</li>
 *   <li>the {@code WAW} tag is <em>not</em> consistent with the matching
 *       {@code Mn} slot, so the multiplier slot is correctly painted as a
 *       progressing functional unit.</li>
 * </ul>
 */
@RunWith(JUnit4.class)
public class MulDLoopWAWTest extends BaseWithInstructionBuilderTest {
  private static final Logger log = Logger.getLogger(MulDLoopWAWTest.class.getName());

  private Parser parser;
  private CycleBuilder builder;

  @Before
  @Override
  public void testSetup() {
    super.testSetup();
    parser = new Parser(lfu, symTab, memory, instructionBuilder);
    config.putBoolean(ConfigKey.FORWARDING, true);
    builder = new CycleBuilder(cpu);
  }

  /** Maps the {@code (functional-unit, sub-stage)} pair to the matching
   *  {@link CycleState} multiplier slot. Returns {@code null} for slots not
   *  exercised by this test. */
  private static CycleState multiplierSlot(int stage) {
    switch (stage) {
      case 1: return CycleState.M1;
      case 2: return CycleState.M2;
      case 3: return CycleState.M3;
      case 4: return CycleState.M4;
      case 5: return CycleState.M5;
      case 6: return CycleState.M6;
      case 7: return CycleState.M7;
      default: return null;
    }
  }

  @Test
  public void mulDInBothIDAndMultiplierIsStalledOnlyInID() throws Exception {
    cpu.reset();
    cachesim.reset();
    symTab.reset();
    builder.reset();

    String absolutePath = new File("src/test/resources/mul.d-loop-waw.s").getAbsolutePath();
    parser.parse(absolutePath);
    cachesim.setDataOffset(memory.getInstructionsNumber() * 4);
    cpu.setStatus(CPU.CPUStatus.RUNNING);

    boolean reproduced = false;
    int safetyCap = 200;

    try {
      while (safetyCap-- > 0) {
        cpu.step();
        builder.step();

        Map<Pipeline.Stage, InstructionInterface> p = cpu.getPipeline();
        InstructionInterface idInstr = p.get(Pipeline.Stage.ID);
        if (idInstr == null || idInstr.isBubble()) {
          continue;
        }
        if (!"MUL.D".equals(idInstr.getName())) {
          continue;
        }

        // Hunt for a multiplier sub-stage hosting an instruction with the
        // same serial number as the one in ID — that is the precondition
        // for the bug.
        int idSerial = idInstr.getSerialNumber();
        CycleState matchingSlot = null;
        for (int s = 1; s <= 7; s++) {
          InstructionInterface mInstr = cpu.getFpuInstruction("MULTIPLIER", s);
          if (mInstr != null && !mInstr.isBubble() && mInstr.getSerialNumber() == idSerial) {
            matchingSlot = multiplierSlot(s);
            assertNotNull("Unexpected multiplier sub-stage " + s, matchingSlot);
            break;
          }
        }
        if (matchingSlot == null) {
          continue;
        }

        // Bug precondition reproduced. Now verify the slot-validity filter
        // is the right gate for resolving the rendered tag.
        String lastTag = builder.getLastStateForSerial(idSerial);
        // The very first cycle where mul.d enters ID, the cycle-builder has
        // tagged it "ID" (no stall detected yet); the stall tag is added on
        // the next cycle once the WAW hazard is detected. Wait for that.
        if (!"WAW".equals(lastTag)) {
          continue;
        }
        log.info("Reproduced: serial=" + idSerial + " lastTag=" + lastTag
            + " matching multiplier slot=" + matchingSlot);

        CycleState parsed = CycleState.fromTag(lastTag);
        assertEquals(CycleState.WAW, parsed);

        assertTrue("ID slot must accept the WAW tag (the slot where the "
                + "stall physically happens)",
            parsed.isValidForSlot(CycleState.ID));
        assertFalse("Multiplier slot " + matchingSlot.name() + " must reject "
                + "the WAW tag carried by the same Java instruction object",
            parsed.isValidForSlot(matchingSlot));

        reproduced = true;
        break;
      }
    } catch (HaltException e) {
      // Program completed before we caught the dual-slot configuration.
      // That would mean the test program no longer exercises the bug;
      // surface it so the test fails loudly instead of passing silently.
      fail("Program halted before the dual-slot mul.d configuration was observed");
    }

    assertTrue("Did not observe a cycle with mul.d simultaneously in ID and "
        + "in a multiplier sub-stage; the test program may no longer "
        + "reproduce the bug", reproduced);
  }

  // org.junit.Assert.assertFalse forwarded for readability above.
}
