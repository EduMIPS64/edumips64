/* CycleElement.java
 *
 * This class represents the single element that is then drawn in the cycles
 * component.
 * (c) 2006 Filippo Mondello
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
package org.edumips64.utils;

import java.util.*;
import java.util.logging.Logger;

import org.edumips64.core.is.InstructionInterface;

/**
* This class represents the single element that is then drawn in the cycles
* component.
* @author Filippo Mondello
*/
public class CycleElement {

  private int startTime;
  private LinkedList<String> states;
  private InstructionInterface instruction;

  // Boolean storing whether this CycleElement contains one or more invalid transactions.
  // Used for testing and debugging purposes.
  private boolean hasInvalidTransaction = false;

  private static final Logger logger = Logger.getLogger(CycleElement.class.getName());

  /**
  * A new element of this class is created.
  * @param instruction the instruction object
  * @param startTime the time in which the element entered in the pipeline
  */
  CycleElement(InstructionInterface instruction, int startTime) {
    this.startTime = startTime;
    this.instruction = instruction;
    states = new LinkedList<>();
    states.add("IF");
  }

  /**
  * @return the name of the instruction
  */
  public String getName() {
    return instruction.getFullName();
  }

  public int getSerialNumber() {
    return instruction.getSerialNumber();
  }

  /**
  * This method is called for every clock cycle.
  * @param newState the current stage in pipeline of the instruction.
  */
  void addState(String newState) {
    String lastState = states.getLast();

    if (!validateStateTransition(lastState, newState)) {
      hasInvalidTransaction = true;
      logger.severe("Instruction " + instruction + ", startTime: " + startTime + ". State " + newState + " is not allowed after state " + lastState);
    }

    states.add(newState);
  }

  // Should only be called when the Cycle refers to a completed instruction. Used only in unit tests.
  boolean isFinalStateValid() {
    String lastState = states.getLast();
    // Valid termination states. IF is valid due to branches.
    if (lastState.equals("WB") || lastState.equals("IF")) {
      return true;
    }

    // " " is the only other valid end state, but it is acceptable only if it's added to IF.
    if (lastState.equals(" ")) {
      Set<String> allStates = new HashSet<>(states);
      if (allStates.size() == 2 && states.getFirst().equals("IF")) {
        return true;
      } else {
        logger.severe("The empty state is not valid as a final state if there are not only IF states. All states: " + states.toString());
        return false;
      }
    }

    logger.severe(lastState + " is not a valid final state.");
    return false;
  }

  // Should only be called when the Cycle refers to a completed instruction. Used only in unit tests.
  public boolean isValid() {
    return !hasInvalidTransaction && isFinalStateValid();
  }

  /**
  * @return the whole list of stages in pipeline
  */
  public LinkedList<String> getStates() {
    return states;
  }

  String getLastState() {
    return states.getLast();
  }

  /**
  * @return the initial time in which the instruction occupied the IF stage in pipeline.
  */
  public int getTime() {
    return startTime;
  }

  public boolean shouldRender() {
    return !instruction.isBubble();
  }

  // Map that associates to a given state the set of allowed successor states.
  // The states that are not added in the list are not checked.
  // TODO: complete the map (it does not contain all possible transitions).
  private static Map<String, Set<String>> allowedTransitions;
  // Map that associates each physical pipeline slot to the set of state tags
  // that can legitimately describe an instruction sitting in that slot.
  //
  // <p>This lives next to {@link #allowedTransitions} because both encode the
  // same pipeline-state vocabulary; keeping them co-located avoids drift.
  // Whereas {@code allowedTransitions} answers "from state X, which Y states
  // are reachable?", this map answers "for physical slot S, which state tags
  // can describe it?".
  //
  // <p>It is needed because the parser produces a single
  // {@code InstructionInterface} per source line: in a tight loop the same
  // Java object can sit in two pipeline slots simultaneously (e.g. iteration
  // <i>n</i>'s {@code mul.d} progressing through {@code M5} while iteration
  // <i>n+1</i>'s {@code mul.d} is {@code WAW}-stalled in {@code ID}), so
  // {@link CycleBuilder#getLastStateForSerial(int)} returns the most recent
  // tag ({@code WAW}) for both slots. Consumers (e.g. the Web UI's
  // {@code ResultFactory.wrap()}) use {@link #isStateValidForSlot(String,
  // String)} to drop the tag for the slot that does not own it.
  private static Map<String, Set<String>> slotMembership;
  static {
    allowedTransitions = new HashMap<>();
    allowedTransitions.put("IF", new HashSet<>(Arrays.asList("ID", " ")));
    allowedTransitions.put("ID", new HashSet<>(Arrays.asList("ID", "EX", "RAW", "WAW", "DIV", "StDiv", "StEx", "StFun", "A1", "M1")));
    allowedTransitions.put("RAW", new HashSet<>(Arrays.asList("RAW", "WAW", "EX", "M1", "A1", "DIV")));
    allowedTransitions.put("WAW", new HashSet<>(Arrays.asList("WAW", "EX", "M1", "A1")));

    allowedTransitions.put("EX", new HashSet<>(Arrays.asList("MEM", "Str")));
    allowedTransitions.put("MEM", new HashSet<>(Arrays.asList("WB")));
    allowedTransitions.put("WB", new HashSet<>(Arrays.asList(" ")));

    slotMembership = new HashMap<>();
    slotMembership.put("IF", new HashSet<>(Arrays.asList("IF")));
    // Data hazards (RAW, WAW) and input structural stalls (StDiv, StEx,
    // StFun) are all detected and tagged at the ID slot.
    slotMembership.put("ID", new HashSet<>(Arrays.asList("ID", "RAW", "WAW", "StDiv", "StEx", "StFun")));
    // EX can stall waiting for MEM (Str).
    slotMembership.put("EX", new HashSet<>(Arrays.asList("EX", "Str")));
    slotMembership.put("MEM", new HashSet<>(Arrays.asList("MEM")));
    slotMembership.put("WB", new HashSet<>(Arrays.asList("WB")));
    // FP Adder non-terminal stages only host their own tag; the terminal
    // stage A4 can additionally carry the StAdd structural-stall tag.
    slotMembership.put("A1", new HashSet<>(Arrays.asList("A1")));
    slotMembership.put("A2", new HashSet<>(Arrays.asList("A2")));
    slotMembership.put("A3", new HashSet<>(Arrays.asList("A3")));
    slotMembership.put("A4", new HashSet<>(Arrays.asList("A4", "StAdd")));
    // FP Multiplier — same story as the Adder, with seven stages.
    slotMembership.put("M1", new HashSet<>(Arrays.asList("M1")));
    slotMembership.put("M2", new HashSet<>(Arrays.asList("M2")));
    slotMembership.put("M3", new HashSet<>(Arrays.asList("M3")));
    slotMembership.put("M4", new HashSet<>(Arrays.asList("M4")));
    slotMembership.put("M5", new HashSet<>(Arrays.asList("M5")));
    slotMembership.put("M6", new HashSet<>(Arrays.asList("M6")));
    slotMembership.put("M7", new HashSet<>(Arrays.asList("M7", "StMul")));
    // The FP Divider's DIV slot owns both the DIV entry tag and the
    // per-cycle DIV_COUNT counter (rendered as D00..D24).
    slotMembership.put("DIV", new HashSet<>(Arrays.asList("DIV", "DIV_COUNT")));
  }

  private static boolean validateStateTransition(String curState, String nextState) {
    // Don't check states that are not in the map.
    return !allowedTransitions.containsKey(curState) || allowedTransitions.get(curState).contains(nextState);

  }

  /**
   * Returns whether {@code state} is a valid state tag for an instruction
   * physically located in pipeline slot {@code slot}. Used by consumers that
   * need to filter cycle-builder tags by the slot they are rendering — see
   * the {@link #slotMembership} field comment for the motivating bug.
   *
   * <p>Returns {@code false} for unknown slots, which is the conservative
   * choice (an unknown slot has no known valid tags).
   */
  public static boolean isStateValidForSlot(String state, String slot) {
    if (state == null || slot == null) {
      return false;
    }
    Set<String> valid = slotMembership.get(slot);
    return valid != null && valid.contains(state);
  }
}
