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

package org.edumips64.ui.common;

import org.edumips64.core.CPU;
import org.edumips64.core.is.Instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CycleBuilder {
  private Instruction [] instr;
  private CPU cpu;
  private int curTime, oldTime;
  private int instructionsCount;

  // Data structure that contains the actual time diagram of the pipeline.
  private List<CycleElement> elementsList;

  // Lookup map from serial number to the corresponding CycleElement.
  private Map<Integer, CycleElement> serialToElementIndexMap;

  // Stalls counters.
  private int RAWStalls, WAWStalls, structStallsEX, structStallsDivider, structStallsFuncUnit;
  // Used to understand if the EX instruction is in structural stall (memory).
  private int memoryStalls;
  // Groups five stalls (EXNotAvailable, FuncUnitNotAvailable,
  // DividerNotAvailable, RAW, WAW), in order to understand if a new
  // instruction has to be added to "elementsList"
  private int inputStructuralStalls;

  public CycleBuilder(CPU cpu) {
    this.cpu = cpu;
    instr = new Instruction[5];
    elementsList = Collections.synchronizedList(new ArrayList<CycleElement>());
    serialToElementIndexMap = Collections.synchronizedMap(new HashMap<>());
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

  public void step() {
    Map<CPU.PipeStage, Instruction> pipeline = cpu.getPipeline();
    curTime = cpu.getCycles();

    if (oldTime != curTime) {
      if (curTime > 0) {
        CycleElement el;
        instr[0] = pipeline.get(CPU.PipeStage.IF);
        instr[1] = pipeline.get(CPU.PipeStage.ID);
        instr[2] = pipeline.get(CPU.PipeStage.EX);
        instr[3] = pipeline.get(CPU.PipeStage.MEM);
        instr[4] = pipeline.get(CPU.PipeStage.WB);

        // WB
        el = getElementToUpdate(instr[4]);
        if (el != null) {
          el.addState("WB");
        }

        // MEM
        el = getElementToUpdate(instr[3]);
        if (el != null) {
          el.addState("MEM");
        }

        // EX
        el = getElementToUpdate(instr[2]);
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
          if (memoryStalls != cpu.getMemoryStalls() && !exTagged) {
            el.addState("Str");
          }
        }

        // If there were stalls, such as RAW, WAW, EXNotAvailable,
        // DividerNotAvailable, FuncUnitNotAvailable, we cannot add a new
        // CycleElement in "elementsList" and we must add tags as RAW, WAW,
        // StEx, StDiv, StFun into the right instruction's state list

        //EX stage stalls
        boolean RAWStallOccurred = (RAWStalls != cpu.getRAWStalls());
        boolean WAWStallOccurred = (WAWStalls != cpu.getWAWStalls());
        boolean structStallEXOccurred = (structStallsEX != cpu.getStructuralStallsEX());
        boolean structStallDividerOccured = (structStallsDivider != cpu.getStructuralStallsDivider());
        boolean structStallsFuncUnitOccurred = (structStallsFuncUnit != cpu.getStructuralStallsFuncUnit());
        boolean inputStallOccurred = (inputStructuralStalls != cpu.getStructuralStallsDivider() + cpu.getStructuralStallsEX() + cpu.getStructuralStallsFuncUnit() + cpu.getRAWStalls() + cpu.getWAWStalls());

        // ID
        el = getElementToUpdate(instr[1]);
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

          if (structStallDividerOccured) {
            el.addState("StDiv");
          }

          if (structStallEXOccurred) {
            el.addState("StEx");
          }

          if (structStallsFuncUnitOccurred) {
            el.addState("StFun");
          }
        }

        // IF
        if (instr[0] != null) {
          if (!inputStallOccurred) {
            // We must instantiate a new CycleElement only if the CPU is running or there was a JumpException and the the IF instruction was changed.
            synchronized(elementsList) {
              int newIndex = elementsList.size();
              CycleElement element = new CycleElement(instr[0], curTime);
              elementsList.add(newIndex, element);
              serialToElementIndexMap.put(instr[0].getSerialNumber(), element);
            }
            instructionsCount++;
          } else {
            el = getElementToUpdate(instr[0]);

            if (el != null) {
              el.addState(" ");
            }
          }
        }

        //we have to check instructions in the FP pipeline
        //ADDER -------------------------------------------------
        String stage;

        // Handle non-terminal adder stages (1-3) with a cycle.
        for (int i = 1; i <= 3; ++i) {
          el = getElementToUpdate(cpu.getInstructionByFuncUnit("ADDER", i));
          if (el != null) {
            el.addState("A" + i);
          }
        }

        el = getElementToUpdate(cpu.getInstructionByFuncUnit("ADDER", 4));
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
          el = getElementToUpdate(cpu.getInstructionByFuncUnit("MULTIPLIER", i));
          if (el != null) {
            el.addState("M" + i);
          }
        }

        el = getElementToUpdate(cpu.getInstructionByFuncUnit("MULTIPLIER", 7));
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
        el = getElementToUpdate(cpu.getInstructionByFuncUnit("DIVIDER", 0));
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
      } else {
        elementsList.clear();
        oldTime = 0;
        instructionsCount = 0;
      }

      oldTime = curTime;
    }

    updateStalls();
  }

  // Returns the CycleElement corresponding to the given Instruction. Returns null if the Instruction is null,
  // is a BUBBLE or if it's not in the map.
  private CycleElement getElementToUpdate(Instruction instruction) {
    if (instruction == null || instruction.isBubble()) {
      return null;
    }
    return serialToElementIndexMap.get(instruction.getSerialNumber());
  }

  private void updateStalls() {
    memoryStalls = cpu.getMemoryStalls();
    RAWStalls = cpu.getRAWStalls();
    WAWStalls = cpu.getWAWStalls();
    structStallsEX = cpu.getStructuralStallsEX();
    structStallsDivider = cpu.getStructuralStallsDivider();
    structStallsFuncUnit = cpu.getStructuralStallsFuncUnit();
    inputStructuralStalls = structStallsDivider + structStallsEX + structStallsFuncUnit + RAWStalls + WAWStalls;
  }
}
