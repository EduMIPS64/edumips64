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

import org.edumips64.core.is.Instruction;

/**
* This class represents the single element that is then drawn in the cycles
* component.
* @author Filippo Mondello
*/
public class CycleElement {

  private int startTime;
  private LinkedList<String> states;
  private Instruction instruction;

  // Boolean storing whether this CycleElement contains one or more invalid transactions.
  // Used for testing and debugging purposes.
  private boolean hasInvalidTransaction = false;

  private static final Logger logger = Logger.getLogger(CycleElement.class.getName());

  /**
  * A new element of this class is created.
  * @param instruction the instruction object
  * @param startTime the time in which the element entered in the pipeline
  */
  CycleElement(Instruction instruction, int startTime) {
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
      logger.severe("Instruction: " + instruction + ", startTime: " + startTime);
      logger.severe("State " + newState + " is not allowed after state " + lastState);
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
      if (allStates.size() == 2 && states.getFirst() == "IF") {
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
  static {
    allowedTransitions = new HashMap<>();
    allowedTransitions.put("IF", new HashSet<>(Arrays.asList("ID", " ")));
    allowedTransitions.put("ID", new HashSet<>(Arrays.asList("ID", "EX", "RAW", "WAW", "DIV", "StDiv", "StEx", "StFun", "A1", "M1")));
    allowedTransitions.put("RAW", new HashSet<>(Arrays.asList("RAW", "WAW", "EX", "M1", "A1")));
    allowedTransitions.put("WAW", new HashSet<>(Arrays.asList("WAW", "EX", "M1", "A1")));

    allowedTransitions.put("EX", new HashSet<>(Arrays.asList("MEM", "Str")));
    allowedTransitions.put("MEM", new HashSet<>(Arrays.asList("WB")));
    allowedTransitions.put("WB", new HashSet<>(Arrays.asList(" ")));
  }

  private static boolean validateStateTransition(String curState, String nextState) {
    // Don't check states that are not in the map.
    return !allowedTransitions.containsKey(curState) || allowedTransitions.get(curState).contains(nextState);

  }
}
