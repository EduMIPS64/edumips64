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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CycleBuilder {
  private Instruction [] instr;
  private CPU cpu;
  private int curTime, oldTime;
  private int instructionsCount;

  // Data structure that contains the actual time diagram of the pipeline.
  List<CycleElement> elementsList;

  // Stalls counters.
  int RAWStalls, WAWStalls, structStallsEX, structStallsDivider, structStallsFuncUnit;
  // Used to understand if the EX instruction is in structural stall (memory).
  int memoryStalls;
  // Groups five stalls (EXNotAvailable, FuncUnitNotAvailable,
  // DividerNotAvailable, RAW, WAW), in order to understand if a new
  // instruction has to be added to "elementsList"
  int inputStructuralStalls;

  public CycleBuilder(CPU cpu) {
    this.cpu = cpu;
    instr = new Instruction[5];
    elementsList = Collections.synchronizedList(new LinkedList<CycleElement>());
    updateStalls();
  }
  public List<CycleElement> getElementsList() {
    return elementsList;
  }
  public int getInstructionsCount() {
    return instructionsCount;
  }

  // Does a chronological search in the list of CycleElements, finding the
  // next CycleElement belonging to the given instruction serial number that
  // is not finalized (i.e., past WB) and has not been updated yet.
  public int getInstructionToUpdate(long serialNumber) {
    for (int i = 0; i < elementsList.size(); ++i) {
      CycleElement tmp = elementsList.get(i);

      if (tmp.getSerialNumber() == serialNumber && tmp.getUpdateTime() == curTime - 1 && !tmp.isFinalized()) {
        return i;
      }
    }

    return -1;
  }


  public int getTime() {
    return curTime;
  }

  public void step() {
    Map<CPU.PipeStage, Instruction> pipeline = cpu.getPipeline();
    curTime = cpu.getCycles();

    if (oldTime != curTime) {
      if (curTime > 0) {
        int index; //used for searching instructions by serial number into "elementsList"
        instr[0] = pipeline.get(CPU.PipeStage.IF);
        instr[1] = pipeline.get(CPU.PipeStage.ID);
        instr[2] = pipeline.get(CPU.PipeStage.EX);
        instr[3] = pipeline.get(CPU.PipeStage.MEM);
        instr[4] = pipeline.get(CPU.PipeStage.WB);

        // WB
        if (instr[4] != null && instr[4].getName() != " ") {
          index = getInstructionToUpdate(instr[4].getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("WB");
          }
        }

        // MEM
        if (instr[3] != null && instr[3].getName() != " ") {
          index = getInstructionToUpdate(instr[3].getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("MEM");
          }
        }

        // EX
        if (instr[2] != null && instr[2].getName() != " ") {
          index = getInstructionToUpdate(instr[2].getSerialNumber());
          // If a structural stall(memory) occurs, the instruction in EX has to be tagged first with "EX" and then with "StEx"
          boolean exTagged = false;

          if (index != -1) {
            if (elementsList.get(index).getLastState() == "ID" ||
                elementsList.get(index).getLastState() == "RAW" ||
                elementsList.get(index).getLastState() == "WAW" ||
                elementsList.get(index).getLastState() == "StEx") {
              elementsList.get(index).addState("EX");
              exTagged = true;
            }

            //we check if a structural hazard  occurred if there's a difference between the previous value of memoryStall counter and the current one
            if (memoryStalls != cpu.getMemoryStalls() && !exTagged) {
              elementsList.get(index).addState("Str");
            }
          }

          exTagged = false;
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
        if (instr[1] != null && instr[1].getName() != " ") {
          index = getInstructionToUpdate(instr[1].getSerialNumber());

          if (!inputStallOccurred) {
            elementsList.get(index).addState("ID");
          }

          if (RAWStallOccurred) {
            elementsList.get(index).addState("RAW");
          }

          if (WAWStallOccurred) {
            elementsList.get(index).addState("WAW");
          }

          if (structStallDividerOccured) {
            elementsList.get(index).addState("StDiv");
          }

          if (structStallEXOccurred) {
            elementsList.get(index).addState("StEx");
          }

          if (structStallsFuncUnitOccurred) {
            elementsList.get(index).addState("StFun");
          }
        }

        // IF
        if (instr[0] != null) {
          if (!inputStallOccurred) {
            // We must instantiate a new CycleElement only if the CPU is running or there was a JumpException and the the IF instruction was changed.
            elementsList.add(new CycleElement(instr[0], curTime));
            instructionsCount++;
          } else {
            index = getInstructionToUpdate(instr[0].getSerialNumber());

            if (index != -1) {
              elementsList.get(index).addState(" ");
            }
          }
        }

        //we have to check instructions in the FP pipeline
        //ADDER -------------------------------------------------
        String stage;
        Instruction instrSearched;

        if (cpu.getInstructionByFuncUnit("ADDER", 1) != null) {
          index = getInstructionToUpdate(cpu.getInstructionByFuncUnit("ADDER", 1).getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("A1");
          }
        }

        if (cpu.getInstructionByFuncUnit("ADDER", 2) != null) {

          index = getInstructionToUpdate(cpu.getInstructionByFuncUnit("ADDER", 2).getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("A2");
          }
        }

        if (cpu.getInstructionByFuncUnit("ADDER", 3) != null) {

          index = getInstructionToUpdate(cpu.getInstructionByFuncUnit("ADDER", 3).getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("A3");
          }
        }

        if (cpu.getInstructionByFuncUnit("ADDER", 4) != null) {

          index = getInstructionToUpdate(cpu.getInstructionByFuncUnit("ADDER", 4).getSerialNumber());
          boolean A4tagged = false;

          if (index != -1) {
            if (elementsList.get(index).getLastState() == "A3") {
              elementsList.get(index).addState("A4");
              A4tagged = true;
            }

            //we have to check if a structural hazard  occurred and it involved the divider or the multiplier (it is sufficient to control if the "A4" o "StAdd" tag was added to the instruction
            if (!A4tagged && (elementsList.get(index).getLastState() == "A4" || elementsList.get(index).getLastState() == "StAdd")) {
              elementsList.get(index).addState("StAdd");
            }
          }

          A4tagged = false;
        }

        //MULTIPLIER ----------------------------------------------------------------
        if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 1)) != null) {
          index = getInstructionToUpdate(instrSearched.getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("M1");
          }
        }

        if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 2)) != null) {
          index = getInstructionToUpdate(instrSearched.getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("M2");
          }
        }

        if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 3)) != null) {
          index = getInstructionToUpdate(instrSearched.getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("M3");
          }
        }

        if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 4)) != null) {
          index = getInstructionToUpdate(instrSearched.getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("M4");
          }
        }

        if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 5)) != null) {
          index = getInstructionToUpdate(instrSearched.getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("M5");
          }
        }

        if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 6)) != null) {
          index = getInstructionToUpdate(instrSearched.getSerialNumber());

          if (index != -1) {
            elementsList.get(index).addState("M6");
          }
        }

        if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 7)) != null) {
          index = getInstructionToUpdate(instrSearched.getSerialNumber());
          boolean M7tagged = false;

          if (index != -1) {
            if (elementsList.get(index).getLastState() == "M6") {
              elementsList.get(index).addState("M7");
              M7tagged = true;
            }

            //we check if a structural hazard  occurred and involved the divider
            if (!M7tagged && (elementsList.get(index).getLastState() == "M7" || elementsList.get(index).getLastState() == "StMul")) {
              elementsList.get(index).addState("StMul");
            }
          }

          M7tagged = false;
        }

        //DIVIDER ------------------------------------------------------
        if ((instrSearched = cpu.getInstructionByFuncUnit("DIVIDER", 0)) != null) {
          boolean DIVtagged = false;
          index = getInstructionToUpdate(instrSearched.getSerialNumber());
          stage = elementsList.get(index).getLastState();

          if (index != -1) {
            if (stage != "DIV" && !stage.matches("D[0-2][0-9]")) {
              elementsList.get(index).addState("DIV");
              DIVtagged = true;
            }

            if (!DIVtagged) {
              int divCount = cpu.getDividerCounter();
              String divCountStr = String.valueOf(divCount);  //divCount in the format DXX (XX belongs to [00  24])
              elementsList.get(index).addState((divCount < 10) ? "D0" + divCountStr : "D" + divCountStr);
            }
          }

          DIVtagged = false;
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
