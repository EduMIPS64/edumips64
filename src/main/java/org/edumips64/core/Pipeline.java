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
   * Shortcut getters for each pipeline stage.
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
   * Advances the instruction currently in {@code from} to {@code to}, leaving
   * {@code from} empty. This is the normal pipeline progression operation and
   * encodes the invariant that the destination stage must be empty or contain
   * a bubble -- otherwise a real instruction would be silently overwritten,
   * which is almost certainly a bug.
   *
   * @throws IllegalStateException if {@code to} already contains a real
   *         (non-bubble) instruction.
   */
  void advance(Stage from, Stage to) {
    InstructionInterface instruction = stageInstructionMap.get(from);
    put(to, instruction);
    stageInstructionMap.put(from, null);
  }

  /** Clears the given stage, setting it to {@code null}. Returns the
   *  instruction that was previously in the stage, if any. */
  InstructionInterface clear(Stage stage) {
    return stageInstructionMap.put(stage, null);
  }

  /**
   * Writes the given instruction into the given stage. Enforces the
   * invariant that a real (non-bubble) instruction already present in that
   * stage cannot be silently overwritten. Setting the stage to {@code null},
   * writing a bubble, or replacing a bubble/empty stage with a real
   * instruction are all allowed. To explicitly discard an in-flight
   * instruction (e.g., during a jump flush), use {@link #flushAndSet}.
   *
   * @return the instruction previously in the stage, if any.
   * @throws IllegalStateException if the stage currently holds a real
   *         instruction and the new value is also a real instruction.
   */
  InstructionInterface setStage(Stage stage, InstructionInterface instruction) {
    return put(stage, instruction);
  }

  /**
   * Clears the given stage and writes the new instruction into it, discarding
   * whatever was previously there (including real instructions). This is the
   * explicit "flush and replace" operation, intended for situations where the
   * CPU logic requires discarding an in-flight instruction -- for example,
   * when a jump invalidates the instruction fetched sequentially into IF.
   *
   * @return the instruction previously in the stage, if any.
   */
  InstructionInterface flushAndSet(Stage stage, InstructionInterface instruction) {
    InstructionInterface previous = stageInstructionMap.put(stage, null);
    put(stage, instruction);
    return previous;
  }

  /**
   * Stores the given instruction in the given pipeline stage, enforcing the
   * precondition that a real instruction already present in that stage cannot
   * be silently overwritten by another real instruction.
   */
  private InstructionInterface put(Stage stage, InstructionInterface instruction) {
    InstructionInterface current = stageInstructionMap.get(stage);
    boolean currentIsReal = current != null && !isBubble(current);
    boolean newIsReal = instruction != null && !isBubble(instruction);
    if (currentIsReal && newIsReal) {
      throw new IllegalStateException(
          "Refusing to overwrite instruction " + current + " in pipeline stage "
              + stage + " with " + instruction
              + ". The stage must be cleared or hold a bubble before writing a "
              + "new instruction; use flushAndSet() to discard explicitly.");
    }
    return stageInstructionMap.put(stage, instruction);
  }

  void clear() {
    stageInstructionMap.put(Stage.IF, null);
    stageInstructionMap.put(Stage.ID, null);
    stageInstructionMap.put(Stage.EX, null);
    stageInstructionMap.put(Stage.MEM, null);
    stageInstructionMap.put(Stage.WB, null);
  }
}
