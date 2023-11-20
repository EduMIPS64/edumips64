/* CycleBuilder.java
 *
 * This class builds the temporal diagram of the pipeline that is then
 * represented by GUICycles.
 * (c) 2006-2013 Filippo Mondello, Trubia Massimo (FPU modifications), Andrea
 * Spadaccini
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

import org.edumips64.core.CPU;
import org.edumips64.core.Pipeline;
import org.edumips64.core.is.InstructionInterface;

import java.util.*;
import java.util.logging.Logger;

public class CycleBuilder {
  private CPU cpu;
  private int curTime, oldTime;
  private int instructionsCount;

  private static final Logger logger = Logger.getLogger(CycleBuilder.class.getName());

  // Data structure that contains the actual time diagram of the pipeline.
  private List<CycleElement> elementsList;

  // Counter of how many times a given instruction, represented by its serial number, has
  // been processed in a given step of the CycleBuilder. The map is reset at every step.
  //
  // This is needed to handle corner cases related to the same instruction being present
  // multiple times in the pipeline. By marking an instruction as processed, it's possible
  // to get the right CycleElement corresponding to the proper instruction instance being
  // processed at a given time.
  private Map<Integer, Integer> processedCountMap;

  // Stalls counters.
  // These are updated at the end of the step() cycle because they hold the value of the stalls for the *last* cycle,
  // so when step() is called again the new stall values can be compared with them to detect stalls.
  private int oldRAWStalls, oldWAWStalls, oldStructStallsEX, oldStructStallsDivider, oldStructStallsFuncUnit;
  // Used to understand if the EX instruction is in structural stall (memory).
  private int oldMemoryStalls;
  // Groups five stalls (EXNotAvailable, FuncUnitNotAvailable,
  // DividerNotAvailable, RAW, WAW), in order to understand if a new
  // instruction has to be added to "elementsList"
  private int oldInputStructuralStalls;
  private List<CycleElement> lastElements;

  public CycleBuilder(CPU cpu) {
    this.cpu = cpu;
    elementsList = new ArrayList<>();
    lastElements = new ArrayList<>();
    updateStalls();
  }
  public List<CycleElement> getElementsList() {
    return elementsList;
  }
  public int getInstructionsCount() {
    return instructionsCount;
  }

  public int getTime() {
    return curTime;
  }

  public void reset() {
    elementsList.clear();
    lastElements.clear();
    oldTime = 0;
    instructionsCount = 0;
  }

  // This method assumes that the CPU has just finished one cycle and that it is necessary to update the
  // CycleBuilder's internal representation to match the state of the CPU.
  public void step() {
    Map<Pipeline.Stage, InstructionInterface> pipeline = cpu.getPipeline();
    curTime = cpu.getCycles();
    int instrInPipelineCount = cpu.getInstructionCount();

    // View into the last N elements of the list. This list is used to search for CycleElements
    // corresponding to a given instruction in the getElementToUpdate() method.
    //
    // N is equal to the number of instructions in the pipeline plus the magic number 28, which represents
    // the maximum number of stages an instruction will traverse from IF to WB (FP Division).
    //
    // TODO: this is not a permanent fix, as there is still potential for bugs if an instruction gets stuck in
    // the pipeline for too long (which may happen in case of MEM stalls, since the FP pipelines and EX have a
    // deterministic precedence ordering. A permanent fix would probably look into the maximum age of the instructions
    // in the pipeline and do something to enforce that ordering in the list of CycleElements.
    //
    // The max() statement is needed to avoid negative indexing (and therefore errors), in the case when the
    // size of the list of elements is smaller than the number of stages in the pipeline, which happens in the
    // first cycles of a program.
    int lookback = Math.max(elementsList.size() - (instrInPipelineCount + 28), 0);
    lastElements = new ArrayList<>(elementsList.subList(lookback, elementsList.size()));

    // If there are multiple elements for a given instruction (e.g., a fetched instruction that will not run)
    // we need to extend the look-back of lastElements.
    Map<Integer, Integer> counts = new HashMap<>();
    for (CycleElement el : lastElements) {
      int counter = counts.getOrDefault(el.getSerialNumber(), 0) + 1;
      counts.put(el.getSerialNumber(), counter);
    }

    // Get the number of repetitions for each element, and extend the list.
    int additionalElementsCount = counts.values().stream().filter(x -> x > 1).mapToInt(x -> x - 1).sum();
    logger.info("There are " + additionalElementsCount + " repeated cycle elements, getting this many more elements.");
    for (int i = 0; i < additionalElementsCount; ++i) {
      int idx = elementsList.size() - lookback - i;
      if (idx == elementsList.size()) {
        continue;
      }
      if (idx < 0) {
        break;
      }
      CycleElement extra = elementsList.get(idx);
      lastElements.add(0, extra); 
    }

    // Reverse the list since the elements are updated from IF to WB, and the elementsList is sorted in
    // chronological order.
    Collections.reverse(lastElements);
    logger.info("Got " + lastElements.size() + " CycleElements. " + instrInPipelineCount + " instructions in the pipeline.");

    // The map is reset at every cycle on purpose.
    processedCountMap = new HashMap<>();

    if (oldTime != curTime) {
      CycleElement el;

      // Pre-compute EX stage stalls
      boolean RAWStallOccurred = (oldRAWStalls != cpu.getRAWStalls());
      boolean WAWStallOccurred = (oldWAWStalls != cpu.getWAWStalls());
      boolean structStallEXOccurred = (oldStructStallsEX != cpu.getStructuralStallsEX());
      boolean structStallDividerOccurred = (oldStructStallsDivider != cpu.getStructuralStallsDivider());
      boolean structStallsFuncUnitOccurred = (oldStructStallsFuncUnit != cpu.getStructuralStallsFuncUnit());
      boolean inputStallOccurred = (oldInputStructuralStalls != cpu.getStructuralStallsDivider() + cpu.getStructuralStallsEX() + cpu.getStructuralStallsFuncUnit() + cpu.getRAWStalls() + cpu.getWAWStalls());

      // Check if something fishy is going on.
      if (inputStallOccurred && !(RAWStallOccurred || WAWStallOccurred || structStallDividerOccurred || structStallEXOccurred || structStallsFuncUnitOccurred)) {
        logger.severe("Something fishy going on with the instruction that has to go into ID");
      }


      // IF
      // TODO: DOES NOT WORK ON RESET.
      InstructionInterface ifInstruction = pipeline.get(Pipeline.Stage.IF);
      if (ifInstruction != null) {
        // We must instantiate a new CycleElement only if no input stalls occurred or if it is the first cycle.
        if (!inputStallOccurred || curTime == 1) {
          synchronized(elementsList) {
            logger.info("Adding a new element to the list of elements");
            CycleElement newElement = new CycleElement(ifInstruction, curTime);
            elementsList.add(newElement);
          }
          instructionsCount++;
        } else {
          el = getElementToUpdate(pipeline.get(Pipeline.Stage.IF));

          if (el != null) {
            el.addState(" ");
          }
        }
      }

      // If there were stalls, such as RAW, WAW, EXNotAvailable,
      // DividerNotAvailable, FuncUnitNotAvailable, we cannot add a new
      // CycleElement in "elementsList" and we must add tags as RAW, WAW,
      // StEx, StDiv, StFun into the right instruction's state list

      // ID
      el = getElementToUpdate(pipeline.get(Pipeline.Stage.ID));
      if (el != null) {
        if (!inputStallOccurred) {
          el.addState("ID");
        }

        if (RAWStallOccurred) {
          el.addState("RAW");
        }

        if (WAWStallOccurred) {
          el.addState("WAW");
        }

        if (structStallDividerOccurred) {
          el.addState("StDiv");
        }

        if (structStallEXOccurred) {
          el.addState("StEx");
        }

        if (structStallsFuncUnitOccurred) {
          el.addState("StFun");
        }
      }

      // EX
      el = getElementToUpdate(pipeline.get(Pipeline.Stage.EX));
      if (el != null) {
        // If a structural stall(memory) occurs, the instruction in EX has to be tagged first with "EX" and then with "StEx"
        boolean exTagged = false;

        if (el.getLastState().equals("ID") ||
            el.getLastState().equals("RAW") ||
            el.getLastState().equals("WAW") ||
            el.getLastState().equals("StEx")) {
          el.addState("EX");
          exTagged = true;
        }

        //we check if a structural hazard  occurred if there's a difference between the previous value of memoryStall counter and the current one
        if (oldMemoryStalls != cpu.getMemoryStalls() && !exTagged) {
          el.addState("Str");
        }
      }

      // MEM
      el = getElementToUpdate(pipeline.get(Pipeline.Stage.MEM));
      if (el != null) {
        el.addState("MEM");
      }

      // WB
      el = getElementToUpdate(pipeline.get(Pipeline.Stage.WB));
      if (el != null) {
        el.addState("WB");
      }

      //we have to check instructions in the FP pipeline
      //ADDER -------------------------------------------------
      String stage;

      // Handle non-terminal adder stages (1-3) with a cycle.
      for (int i = 1; i <= 3; ++i) {
        el = getElementToUpdate(cpu.getFpuInstruction("ADDER", i));
        if (el != null) {
          el.addState("A" + i);
        }
      }

      el = getElementToUpdate(cpu.getFpuInstruction("ADDER", 4));
      if (el != null) {
        boolean A4tagged = false;
        if (el.getLastState().equals("A3")) {
          el.addState("A4");
          A4tagged = true;
        }

        //we have to check if a structural hazard  occurred and it involved the divider or the multiplier (it is sufficient to control if the "A4" o "StAdd" tag was added to the instruction
        if (!A4tagged && (el.getLastState().equals("A4") || el.getLastState().equals("StAdd"))) {
          el.addState("StAdd");
        }
      }

      //MULTIPLIER ----------------------------------------------------------------

      // Handle non-terminal multiplier stages (1-6) with a cycle.
      for (int i = 1; i <= 6; ++i) {
        el = getElementToUpdate(cpu.getFpuInstruction("MULTIPLIER", i));
        if (el != null) {
          el.addState("M" + i);
        }
      }

      el = getElementToUpdate(cpu.getFpuInstruction("MULTIPLIER", 7));
      if (el != null) {
        boolean M7tagged = false;
        if (el.getLastState().equals("M6")) {
          el.addState("M7");
          M7tagged = true;
        }

        //we check if a structural hazard  occurred and involved the divider
        if (!M7tagged && (el.getLastState().equals("M7") || el.getLastState().equals("StMul"))) {
          el.addState("StMul");
        }
      }

      //DIVIDER ------------------------------------------------------
      el = getElementToUpdate(cpu.getFpuInstruction("DIVIDER", 0));
      if (el != null) {
        boolean DIVtagged = false;
        stage = el.getLastState();
        if (!stage.equals("DIV") && !stage.matches("D[0-2][0-9]")) {
          el.addState("DIV");
          DIVtagged = true;
        }

        if (!DIVtagged) {
          int divCount = cpu.getDividerCounter();
          String divCountStr = String.valueOf(divCount);  //divCount in the format DXX (XX belongs to [00  24])
          el.addState((divCount < 10) ? "D0" + divCountStr : "D" + divCountStr);
        }
      }

      oldTime = curTime;
    }

    updateStalls();
  }

  // Returns the CycleElement corresponding to the given Instruction. Returns null if the Instruction is null,
  // is a BUBBLE or if it's not in the map.
  private CycleElement getElementToUpdate(InstructionInterface instruction) {
    // Early exit in case of bogus instructions.
    if (instruction == null || instruction.isBubble()) {
      return null;
    }

    int serial = instruction.getSerialNumber();
    if (!processedCountMap.containsKey(serial)) {
      processedCountMap.put(serial, 0);
    }
    int instructionProcessedCount = processedCountMap.get(serial);

    // The CycleElement to be returned.
    CycleElement element = null;

    // How many times the a CycleElement corresponding to the given instruction was seen in the list of CycleElements
    // being analyzed. Let N be the number of times the instruction was already processed by getElementToUpdate(). The
    // cycle terminates when the CycleElement N (corresponding to the given instruction) is found in the list of
    // CycleElements, or when the list is exhausted. (N == instructionProcessedCount).
    int instructionSeenCount = 0;
    for (CycleElement el : lastElements) {
      if (el.getSerialNumber() == serial) {
        if (instructionSeenCount == instructionProcessedCount) {
          element = el;
          break;
        }
        instructionSeenCount++;
      }
    }

    // Increment the counter to indicate that, in this step() cycle, the given instruction was processed one more time.
    processedCountMap.put(serial, instructionProcessedCount + 1);
    return element;
  }

  private void updateStalls() {
    oldMemoryStalls = cpu.getMemoryStalls();
    oldRAWStalls = cpu.getRAWStalls();
    oldWAWStalls = cpu.getWAWStalls();
    oldStructStallsEX = cpu.getStructuralStallsEX();
    oldStructStallsDivider = cpu.getStructuralStallsDivider();
    oldStructStallsFuncUnit = cpu.getStructuralStallsFuncUnit();
    oldInputStructuralStalls = oldStructStallsDivider + oldStructStallsEX + oldStructStallsFuncUnit + oldRAWStalls + oldWAWStalls;
  }
}
