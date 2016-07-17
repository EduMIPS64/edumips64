package org.edumips64.core;

import org.edumips64.core.is.InstructionInterface;

import java.util.HashMap;
import java.util.Map;

/** A class representing the 5 pipeline stages, containing instructions.
 * Has some convenience methods for dealing with them and making the CPU code
 * a bit simpler.
 */
class Pipeline {
  private Map<CPU.PipeStage, InstructionInterface> stageInstructionMap;

  Pipeline() {
    stageInstructionMap = new HashMap<>();
    clear();
  }

  boolean isEmptyOrBubble(CPU.PipeStage stage) {
    return isEmpty(stage) || isBubble(stage);
  }

  boolean isEmpty(CPU.PipeStage stage) {
    return stageInstructionMap.get(stage) == null;
  }

  boolean isBubble(CPU.PipeStage stage) {
    return !isEmpty(stage) && stageInstructionMap.get(stage).getName().equals(" ");
  }

  int size() {
    return (int) stageInstructionMap.entrySet().stream()
        .filter(e -> e.getValue() != null)
        .count();
  }

  Map<CPU.PipeStage, InstructionInterface> getInternalRepresentation() {
    return stageInstructionMap;
  }

  InstructionInterface get(CPU.PipeStage stage) {
    return stageInstructionMap.get(stage);
  }

  /**
   * Shortcut setters/getters for the stages.
   * Like Map.put(), setters return the previous mapping if any, or null if
   * no mapping was in place.
   */
  InstructionInterface IF() {
    return get(CPU.PipeStage.IF);
  }

  InstructionInterface ID() {
    return get(CPU.PipeStage.ID);
  }

  InstructionInterface EX() {
    return get(CPU.PipeStage.EX);
  }

  InstructionInterface MEM() {
    return get(CPU.PipeStage.MEM);
  }

  InstructionInterface WB() {
    return get(CPU.PipeStage.WB);
  }

  InstructionInterface setIF(InstructionInterface instruction) {
    return stageInstructionMap.put(CPU.PipeStage.IF, instruction);
  }

  InstructionInterface setID(InstructionInterface instruction) {
    return stageInstructionMap.put(CPU.PipeStage.ID, instruction);
  }

  InstructionInterface setEX(InstructionInterface instruction) {
    return stageInstructionMap.put(CPU.PipeStage.EX, instruction);
  }

  InstructionInterface setMEM(InstructionInterface instruction) {
    return stageInstructionMap.put(CPU.PipeStage.MEM, instruction);
  }

  InstructionInterface setWB(InstructionInterface instruction) {
    return stageInstructionMap.put(CPU.PipeStage.WB, instruction);
  }

  void clear() {
    stageInstructionMap.put(CPU.PipeStage.IF, null);
    stageInstructionMap.put(CPU.PipeStage.ID, null);
    stageInstructionMap.put(CPU.PipeStage.EX, null);
    stageInstructionMap.put(CPU.PipeStage.MEM, null);
    stageInstructionMap.put(CPU.PipeStage.WB, null);
  }
}
