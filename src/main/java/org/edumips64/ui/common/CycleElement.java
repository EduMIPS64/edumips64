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
package org.edumips64.ui.common;

import java.util.*;
import java.util.logging.Logger;

import org.edumips64.core.is.Instruction;

/**
* This class represents the single element that is then drawn in the cycles
* component.
* @author Filippo Mondello
*/
public class CycleElement {

  int startTime;
  private LinkedList<String> states;
  private Instruction instruction;

  private static final Logger logger = Logger.getLogger(CycleElement.class.getName());

  /**
  * A new element of this class is created.
  * @param nom the name of the instruction
  * @param startTime the time in which the element is entered in pipeline
  */
  public CycleElement(Instruction instruction, int startTime) {
    this.startTime = startTime;
    this.instruction = instruction;
    states = new LinkedList<String>();
    states.add("IF");
  }

  /**
  * @return the name of the instruction
  */
  public String getName() {
    return instruction.getFullName();
  }

  public int getUpdateTime() {
    return startTime + states.size() - 1;
  }
  public boolean isFinalized() {
    // TODO(lupino): make this check if the instruction has been discarded
    // because of a jump.
    return states.getLast() == "WB";
  }

  /**
  * This method is called for every clock cycle.
  * @param stat the current stage in pipeline of the instruction.
  */
  public void addState(String newState) {
    String lastState = states.getLast();

    if (!validateStateTransition(lastState, newState)) {
      logger.severe("Instruction: " + instruction + ", startTime: " + startTime);
      logger.severe("State " + newState + " is not allowed after state " + lastState);
    }

    states.add(newState);
  }

  /**
  * @return the whole list of stages in pipeline
  */
  public LinkedList<String> getStates() {
    return states;
  }

  public String getLastState() {
    return states.getLast();
  }

  /**
  * @return the initial time in which the instruction occuped the IF stage in pipeline.
  */
  public int getTime() {
    return startTime;
  }

  /** Returns the serial number of the referred instruction*/
  public long getSerialNumber() {
    return instruction.getSerialNumber();
  }

  public boolean shouldRender() {
    return !instruction.isBubble();
  }

  // Map that associates to a given state the set of allowed successor states.
  // The states that are not added in the list are not checked.
  // TODO: complete the map (it does not contain all possible transitions).
  private static Map<String, Set<String>> allowedTransitions;
  static {
    allowedTransitions = new HashMap<String, Set<String>>();
    allowedTransitions.put("IF", new HashSet<String>(Arrays.asList("ID", " ")));
    allowedTransitions.put("ID", new HashSet<String>(Arrays.asList("ID", "EX", "RAW", "WAW", "DIV", "StDiv", "StEx", "StFun", "A1", "M1")));
    allowedTransitions.put("RAW", new HashSet<String>(Arrays.asList("RAW", "EX", "M1", "A1")));
    allowedTransitions.put("WAW", new HashSet<String>(Arrays.asList("WAW", "EX", "M1", "A1")));

    allowedTransitions.put("EX", new HashSet<String>(Arrays.asList("MEM", "Str")));
    allowedTransitions.put("MEM", new HashSet<String>(Arrays.asList("WB")));
    allowedTransitions.put("WB", new HashSet<String>(Arrays.asList(" ")));
  }

  private static boolean validateStateTransition(String curState, String nextState) {
    if (!allowedTransitions.containsKey(curState)) {
      // Don't check states that are not in the map.
      return true;
    }

    return allowedTransitions.get(curState).contains(nextState);
  }
}
