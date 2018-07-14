package org.edumips64.core;

import org.edumips64.core.is.InstructionInterface;

import java.util.HashMap;
import java.util.Map;

/** A class representing the 5 pipeline stages, containing instructions.
 * Has some convenience methods for dealing with them and making the CPU code
 * a bit simpler.
 */
public class Pipeline {

  /** Pipeline stage*/
  public enum Stage {IF, ID, EX, MEM, WB}

  private Map<Stage, InstructionInterface> stageInstructionMap;

  Pipeline() {
    stageInstructionMap = new HashMap<>();
    clear();
  }

  boolean isEmptyOrBubble(Stage stage) {
    return isEmpty(stage) || isBubble(stage);
  }

  boolean isEmpty(Stage stage) {
    return stageInstructionMap.get(stage) == null;
  }

  boolean isBubble(Stage stage) {
    return !isEmpty(stage) && stageInstructionMap.get(stage).getName().equals(" ");
  }

  int size() {
    return (int) stageInstructionMap.entrySet().stream()
        .filter(e -> e.getValue() != null)
        .count();
  }

  Map<Stage, InstructionInterface> getInternalRepresentation() {
    return stageInstructionMap;
  }

  InstructionInterface get(Stage stage) {
    return stageInstructionMap.get(stage);
  }

  /**
   * Shortcut setters/getters for the stages.
   * Like Map.put(), setters return the previous mapping if any, or null if
   * no mapping was in place.
   */
  InstructionInterface IF() {
    return get(Stage.IF);
  }

  InstructionInterface ID() {
    return get(Stage.ID);
  }

  InstructionInterface EX() {
    return get(Stage.EX);
  }

  InstructionInterface MEM() {
    return get(Stage.MEM);
  }

  InstructionInterface WB() {
    return get(Stage.WB);
  }

  InstructionInterface setIF(InstructionInterface instruction) {
    return stageInstructionMap.put(Stage.IF, instruction);
  }

  InstructionInterface setID(InstructionInterface instruction) {
    return stageInstructionMap.put(Stage.ID, instruction);
  }

  InstructionInterface setEX(InstructionInterface instruction) {
    return stageInstructionMap.put(Stage.EX, instruction);
  }

  InstructionInterface setMEM(InstructionInterface instruction) {
    return stageInstructionMap.put(Stage.MEM, instruction);
  }

  InstructionInterface setWB(InstructionInterface instruction) {
    return stageInstructionMap.put(Stage.WB, instruction);
  }

  void clear() {
    stageInstructionMap.put(Stage.IF, null);
    stageInstructionMap.put(Stage.ID, null);
    stageInstructionMap.put(Stage.EX, null);
    stageInstructionMap.put(Stage.MEM, null);
    stageInstructionMap.put(Stage.WB, null);
  }
}
