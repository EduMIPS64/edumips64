/* CycleState.java
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

import jsinterop.annotations.JsType;

/**
 * Tag attached by {@code CycleBuilder} to an in-flight instruction for the
 * most recently completed CPU cycle. Used by {@link Instruction#Stage} to
 * give the Web UI a typed view over the per-cycle state previously exposed
 * as a free-form {@code String}.
 *
 * <p><b>Why this enum lives in {@code client/} and is separate from
 * {@code Pipeline.Stage}.</b> {@code Pipeline.Stage} (in {@code core/}) only
 * models the 5 physical pipeline stages ({@code IF}, {@code ID}, {@code EX},
 * {@code MEM}, {@code WB}) and is the key type of the {@code Pipeline}'s
 * stage-to-instruction map. Mixing in tags like {@code RAW}, {@code StDiv} or
 * the FP-pipe sub-stages would muddy that abstraction. We intentionally keep
 * two enums: {@code Pipeline.Stage} stays narrow, and {@code CycleState}
 * covers the broader vocabulary that {@code CycleBuilder} produces.
 *
 * <p><b>Why this enum lives next to {@code client/Instruction} (and not next
 * to {@code CycleBuilder}/{@code CycleElement}).</b> The minimal goal of this
 * change is to remove the stringly-typed {@code Stage} field on the public JS
 * API, without refactoring {@code CycleElement}'s internal {@code String}
 * representation or {@code CycleBuilder}'s {@code addState(String)} call sites.
 * Conversion happens once, at the {@code ResultFactory} boundary. A future
 * change can promote this enum into the simulator core if/when
 * {@code CycleElement} is refactored.
 *
 * <p><b>Constant naming.</b> Names match the historical {@code CycleBuilder}
 * string tags exactly (including mixed case, e.g. {@code StDiv}) so that
 * {@link Enum#name()} preserves the existing wire vocabulary.
 *
 * <p><b>Divider counter.</b> {@code CycleBuilder} tags an instruction sitting
 * in the FP divider with a per-cycle counter rendered as {@code D00}..
 * {@code D24} in the Swing UI. Encoding 25 enum constants would be brittle
 * (the upper bound depends on the FP divider latency), so all those tags map
 * to the single {@link #DIV_COUNT} constant; the actual counter value travels
 * alongside in {@link Instruction#DivCount}.
 *
 * <p>The bubble placeholder (the empty string {@code " "}) that
 * {@code CycleBuilder} appends to a squashed {@code IF} is intentionally
 * <em>not</em> represented here: {@code ResultFactory.wrap()} short-circuits
 * on bubble instructions, so it never reaches the client {@code Stage} field.
 */
@JsType
public enum CycleState {
  // Integer pipeline stages. Mirror Pipeline.Stage but kept separate; see
  // the class-level Javadoc for why we don't reuse Pipeline.Stage here.
  IF,
  ID,
  EX,
  MEM,
  WB,

  // Data hazards detected in ID.
  RAW,
  WAW,

  // Structural stalls.
  StDiv,
  StEx,
  StFun,
  Str,
  StAdd,
  StMul,

  // FP Adder pipeline stages (4 cycles).
  A1,
  A2,
  A3,
  A4,

  // FP Multiplier pipeline stages (7 cycles).
  M1,
  M2,
  M3,
  M4,
  M5,
  M6,
  M7,

  // FP Divider entry (first cycle in the divider).
  DIV,

  // FP Divider per-cycle counter; the actual count is exposed separately on
  // Instruction.DivCount, in the range [0, 24] inclusive. See class Javadoc.
  DIV_COUNT;

  /**
   * Parses one of the {@code CycleBuilder} state strings into the matching
   * {@code CycleState}. Returns {@code null} for the bubble placeholder
   * {@code " "} (which never reaches client code) and for unknown inputs;
   * callers should treat {@code null} the same as "no stage info".
   *
   * <p>Recognised values:
   * <ul>
   *   <li>The exact name of any constant in this enum (e.g. {@code "RAW"},
   *       {@code "StDiv"}, {@code "A3"}, {@code "M7"}, {@code "DIV"}); and</li>
   *   <li>The two-character divider counters {@code "D00"}..{@code "D24"},
   *       which map to {@link #DIV_COUNT}. The numeric counter must be read
   *       separately via {@link #parseDivCount(String)}.</li>
   * </ul>
   */
  public static CycleState fromTag(String tag) {
    if (tag == null || tag.isEmpty() || " ".equals(tag)) {
      return null;
    }
    if (isDivCounterTag(tag)) {
      return DIV_COUNT;
    }
    try {
      return CycleState.valueOf(tag);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * If {@code tag} is a divider counter ({@code "D00"}..{@code "D24"}),
   * returns the integer count (0..24). Returns {@code -1} for any other
   * input, including the {@code "DIV"} entry tag.
   */
  public static int parseDivCount(String tag) {
    if (!isDivCounterTag(tag)) {
      return -1;
    }
    try {
      return Integer.parseInt(tag.substring(1));
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  // A divider-counter tag is exactly three characters: 'D' followed by two
  // decimal digits. The 'DIV' entry tag is intentionally excluded.
  private static boolean isDivCounterTag(String tag) {
    if (tag.length() != 3 || tag.charAt(0) != 'D') {
      return false;
    }
    char c1 = tag.charAt(1);
    char c2 = tag.charAt(2);
    return c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9';
  }
}
