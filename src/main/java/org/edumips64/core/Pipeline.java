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
    return !isEmpty(stage) && isBubble(stageInstructionMap.get(stage));
  }

  private static boolean isBubble(InstructionInterface instruction) {
    return instruction != null && instruction.getName().equals(" ");
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

  /**
   * Stores the given instruction in the given pipeline stage, enforcing the
   * precondition that a real instruction already present in that stage cannot
   * be silently overwritten by another real instruction. Clearing a stage
   * (setting it to {@code null}) and inserting/replacing bubbles is always
   * allowed, as is replacing an empty stage or a bubble with a real
   * instruction. Attempting to overwrite a real instruction with another
   * real instruction is almost certainly a bug in the CPU logic and results
   * in an {@link IllegalStateException}.
   */
  private InstructionInterface put(Stage stage, InstructionInterface instruction) {
    InstructionInterface current = stageInstructionMap.get(stage);
    boolean currentIsReal = current != null && !isBubble(current);
    boolean newIsReal = instruction != null && !isBubble(instruction);
    if (currentIsReal && newIsReal) {
      throw new IllegalStateException(
          "Refusing to overwrite instruction " + current + " in pipeline stage "
              + stage + " with " + instruction
              + ". The stage must be cleared (set to null) or hold a bubble before "
              + "writing a new instruction.");
    }
    return stageInstructionMap.put(stage, instruction);
  }

  InstructionInterface setIF(InstructionInterface instruction) {
    return put(Stage.IF, instruction);
  }

  InstructionInterface setID(InstructionInterface instruction) {
    return put(Stage.ID, instruction);
  }

  InstructionInterface setEX(InstructionInterface instruction) {
    return put(Stage.EX, instruction);
  }

  InstructionInterface setMEM(InstructionInterface instruction) {
    return put(Stage.MEM, instruction);
  }

  InstructionInterface setWB(InstructionInterface instruction) {
    return put(Stage.WB, instruction);
  }

  void clear() {
    stageInstructionMap.put(Stage.IF, null);
    stageInstructionMap.put(Stage.ID, null);
    stageInstructionMap.put(Stage.EX, null);
    stageInstructionMap.put(Stage.MEM, null);
    stageInstructionMap.put(Stage.WB, null);
  }
}
