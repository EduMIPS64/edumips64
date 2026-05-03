/* CycleStateTest.java
 *
 * Unit tests for the CycleState enum.
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
package org.edumips64.client;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link CycleState}, in particular the per-slot validity
 * matrix that gates whether a CycleBuilder tag is allowed to be attached
 * to an instruction wrapped for a given physical pipeline slot.
 *
 * <p>Background: the parser produces a single {@code InstructionInterface}
 * per source line, which is reused on every loop iteration. When the same
 * instruction appears in two pipeline slots simultaneously (e.g. the body
 * of a tight loop where {@code mul.d} progresses through the multiplier
 * pipeline while a re-fetched {@code mul.d} is WAW-stalled in ID), both
 * slots share a serial number and {@code CycleBuilder.getLastStateForSerial}
 * returns the most recent tag for both. The slot-validity check rejects the
 * tag when it cannot physically describe the slot, which keeps the FP
 * functional-unit slots from being painted as stalled.
 */
public class CycleStateTest {

  @Test
  public void slotMatchesItself() {
    // Every "stage" CycleState describes its own slot.
    for (CycleState slot : new CycleState[] {
        CycleState.IF, CycleState.ID, CycleState.EX, CycleState.MEM, CycleState.WB,
        CycleState.A1, CycleState.A2, CycleState.A3, CycleState.A4,
        CycleState.M1, CycleState.M2, CycleState.M3, CycleState.M4,
        CycleState.M5, CycleState.M6, CycleState.M7,
        CycleState.DIV,
    }) {
      assertTrue(slot.name() + " must be valid for its own slot",
          slot.isValidForSlot(slot));
    }
  }

  @Test
  public void idAcceptsInputHazardTags() {
    // RAW / WAW and the three structural-input stalls are all detected in
    // ID, so they may legitimately be attached to the ID slot.
    assertTrue(CycleState.RAW.isValidForSlot(CycleState.ID));
    assertTrue(CycleState.WAW.isValidForSlot(CycleState.ID));
    assertTrue(CycleState.StDiv.isValidForSlot(CycleState.ID));
    assertTrue(CycleState.StEx.isValidForSlot(CycleState.ID));
    assertTrue(CycleState.StFun.isValidForSlot(CycleState.ID));
  }

  @Test
  public void exAcceptsMemoryStructuralStall() {
    // CycleBuilder tags an EX-stalled-on-memory instruction with "Str"; no
    // other stall makes physical sense at EX.
    assertTrue(CycleState.Str.isValidForSlot(CycleState.EX));
    assertFalse(CycleState.RAW.isValidForSlot(CycleState.EX));
    assertFalse(CycleState.WAW.isValidForSlot(CycleState.EX));
  }

  @Test
  public void terminalFpStagesAcceptTheirStructuralStall() {
    // M7 is the only multiplier slot that can be tagged StMul (waiting at
    // the dispatch boundary into MEM); A4 is its adder counterpart.
    assertTrue(CycleState.StMul.isValidForSlot(CycleState.M7));
    assertTrue(CycleState.StAdd.isValidForSlot(CycleState.A4));
  }

  @Test
  public void dividerSlotAcceptsCounterTag() {
    // The DIV slot uses two distinct tags: DIV on entry and the per-cycle
    // DIV_COUNT counter while the divider is busy.
    assertTrue(CycleState.DIV_COUNT.isValidForSlot(CycleState.DIV));
    assertTrue(CycleState.DIV.isValidForSlot(CycleState.DIV));
  }

  /**
   * Direct guard for the bug reported by the user. With the same Java
   * {@code mul.d} object physically in {@code M5} (progressing) and in
   * {@code ID} (WAW-stalled in a later loop iteration), the cycle-builder's
   * "most recent tag" is {@code WAW} for both. The validity check must drop
   * that tag for the {@code M5} slot so the pipeline widget does not paint
   * M5 as stalled.
   */
  @Test
  public void wawTagIsRejectedForFpFunctionalUnitSlots() {
    for (CycleState slot : new CycleState[] {
        CycleState.IF, CycleState.EX, CycleState.MEM, CycleState.WB,
        CycleState.A1, CycleState.A2, CycleState.A3, CycleState.A4,
        CycleState.M1, CycleState.M2, CycleState.M3, CycleState.M4,
        CycleState.M5, CycleState.M6, CycleState.M7,
        CycleState.DIV,
    }) {
      assertFalse("WAW must not be valid for slot " + slot.name(),
          CycleState.WAW.isValidForSlot(slot));
      assertFalse("RAW must not be valid for slot " + slot.name(),
          CycleState.RAW.isValidForSlot(slot));
    }
  }

  @Test
  public void nonTerminalFpStagesRejectStructuralStallTags() {
    // M1..M6 are pipelined and never stall in place; only M7 can carry the
    // StMul tag. Same story for A1..A3 vs A4 / StAdd.
    for (CycleState slot : new CycleState[] {
        CycleState.M1, CycleState.M2, CycleState.M3,
        CycleState.M4, CycleState.M5, CycleState.M6,
    }) {
      assertFalse("StMul must not be valid for non-terminal multiplier slot " + slot.name(),
          CycleState.StMul.isValidForSlot(slot));
    }
    for (CycleState slot : new CycleState[] {
        CycleState.A1, CycleState.A2, CycleState.A3,
    }) {
      assertFalse("StAdd must not be valid for non-terminal adder slot " + slot.name(),
          CycleState.StAdd.isValidForSlot(slot));
    }
  }

  @Test
  public void differentStageSlotsAreDistinct() {
    // Sanity checks against a few off-diagonal cells that should never match.
    assertFalse(CycleState.M5.isValidForSlot(CycleState.M4));
    assertFalse(CycleState.M5.isValidForSlot(CycleState.M6));
    assertFalse(CycleState.A2.isValidForSlot(CycleState.A3));
    assertFalse(CycleState.IF.isValidForSlot(CycleState.ID));
    assertFalse(CycleState.MEM.isValidForSlot(CycleState.WB));
  }

  @Test
  public void nullSlotIsRejected() {
    assertFalse(CycleState.IF.isValidForSlot(null));
    assertFalse(CycleState.WAW.isValidForSlot(null));
  }
}
