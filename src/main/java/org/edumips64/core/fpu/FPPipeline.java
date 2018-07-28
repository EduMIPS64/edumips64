/*
 * FPPipeline.java
 *
 * This class models a MIPS FPU  pipeline that supports multiple outstanding FP operations
 * it is used only by the cpu class
 * (c) 2006 Massimo Trubia
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

package org.edumips64.core.fpu;
import org.edumips64.core.is.*;

import java.util.*;

/** This class models a MIPS FPU  pipeline that supports multiple outstanding FP operations
 *   it is used only by the cpu class
 *  @author Massimo Trubia
 */
public class FPPipeline {
  //FPU functional units
  public static class Constants {
    public enum FPAdderStatus {A1, A2, A3, A4};
    public enum FPMultiplierStatus {M1, M2, M3, M4, M5, M6, M7};
    public enum FPDividerStatus {DIVIDER};
    public static List<String> fparithmetic = new ArrayList<>(Arrays.asList("ADD.D",
        "SUB.D", "DIV.D", "MUL.D"));
  }
  private Divider divider;
  private Multiplier multiplier;
  private Adder adder;
  private int nInstructions; //used for understanding if the fpPipe is empty or not
  //FPPipeline in the same order by which they has entered


  public FPPipeline() {
    // Instantiating functional units objects.
    nInstructions = 0;
    divider = new Divider();
    divider.reset();
    multiplier = new Multiplier();
    multiplier.reset();
    adder = new Adder();
    adder.reset();
  }

  public int size() {
    return nInstructions;
  }

  public String toString() {
    String output = "";
    output += adder.toString();
    output += multiplier.toString();
    output += divider.toString();
    return output;
  }

  /** Returns true if the specified functional unit is filled by an instruction, false when the contrary happens.
   *  No controls are carried out on the legality of parameters, for mistaken parameters false is returned
   *  @param funcUnit The functional unit to check. Legal values are "ADDER", "MULTIPLIER", "DIVIDER"
   *  @param stage The integer that refers to the stage of the functional unit.
   *      ADDER [1,4], MULTIPLIER [1,7], DIVIDER [any] */
  public boolean isFuncUnitFilled(String funcUnit, int stage) {
    if (funcUnit.compareToIgnoreCase("ADDER") == 0)
      switch (stage) {
        case 1:
          return (adder.getFuncUnit().get(Constants.FPAdderStatus.A1) != null);
        case 2:
          return (adder.getFuncUnit().get(Constants.FPAdderStatus.A2) != null);
        case 3:
          return (adder.getFuncUnit().get(Constants.FPAdderStatus.A3) != null);
        case 4:
          return (adder.getFuncUnit().get(Constants.FPAdderStatus.A4) != null);
      }

    if (funcUnit.compareToIgnoreCase("MULTIPLIER") == 0)
      switch (stage) {
        case 1:
          return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M1) != null);
        case 2:
          return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M2) != null);
        case 3:
          return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M3) != null);
        case 4:
          return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M4) != null);
        case 5:
          return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M5) != null);
        case 6:
          return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M6) != null);
        case 7:
          return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M7) != null);
      }

    return funcUnit.compareToIgnoreCase("DIVIDER") == 0 && (divider.getFuncUnit() != null);

  }
  /** Returns the instruction of the specified functional unit , null if it is empty.
   *  No controls are carried out on the legality of parameters, for mistaken parameters null is returned
   *  @param funcUnit The functional unit to check. Legal values are "ADDER", "MULTIPLIER", "DIVIDER"
   *  @param stage The integer that refers to the stage of the functional unit.
   *      ADDER [1,4], MULTIPLIER [1,7], DIVIDER [any] */

  public InstructionInterface getInstructionByFuncUnit(String funcUnit, int stage) {
    if (funcUnit.compareToIgnoreCase("ADDER") == 0)
      switch (stage) {
      case 1:
        return (adder.getFuncUnit().get(Constants.FPAdderStatus.A1));
      case 2:
        return (adder.getFuncUnit().get(Constants.FPAdderStatus.A2));
      case 3:
        return (adder.getFuncUnit().get(Constants.FPAdderStatus.A3));
      case 4:
        return (adder.getFuncUnit().get(Constants.FPAdderStatus.A4));
      }

    if (funcUnit.compareToIgnoreCase("MULTIPLIER") == 0)
      switch (stage) {
      case 1:
        return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M1));
      case 2:
        return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M2));
      case 3:
        return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M3));
      case 4:
        return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M4));
      case 5:
        return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M5));
      case 6:
        return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M6));
      case 7:
        return (multiplier.getFuncUnit().get(Constants.FPMultiplierStatus.M7));
      }

    if (funcUnit.compareToIgnoreCase("DIVIDER") == 0) {
      return (divider.getFuncUnit());
    }

    return null;
  }

  /** Inserts the passed instruction into the right functional unit. If no errors occur
   *  0 is returned, else, if we want to insert an ADD.fmt, MUL.fmt, SUB.fmt  and
   *  the first place of the adder or multiplier is filled by other
   *  instructions 1 is returned, else, if we want to insert a DIV.fmt and the
   *  divider is full 2 is returned and the CPU raises a StructuralException.
   *  If an integer instruction is passed at the method 3 is returned
   */
  public int putInstruction(InstructionInterface instr, boolean simulation) {  //throws InputStructuralHazardException
    if (instr != null && Constants.fparithmetic.contains(instr.getName())) {
      String instrName = instr.getName();

      if ((instrName.compareToIgnoreCase("ADD.D") == 0) || (instrName.compareToIgnoreCase("SUB.D") == 0))
        if (adder.putInstruction(instr, simulation) == -1) {
          return 1;
        }

      if (instrName.compareToIgnoreCase("MUL.D") == 0)
        if (multiplier.putInstruction(instr, simulation) == -1) {
          return 1;
        }

      if (instrName.compareToIgnoreCase("DIV.D") == 0)
        if (divider.putInstruction(instr, simulation) == -1) {
          return 2;
        }

      if (!simulation) {
        nInstructions++;
      }

      return 0;
    }

    return 3;
  }

  // Returns the completed FP instruction. If more than one instruction completed, it will return them in the following
  // order: 1. divider; 2. multiplier; 3. adder. If no instruction is complete, it'll return null.
  // The returned instruction will be removed from the corresponding FPU unit.
  public InstructionInterface getCompletedInstruction() {
    InstructionInterface dividerInstruction = divider.getInstruction();
    InstructionInterface multiplierInstruction = multiplier.getInstruction();
    InstructionInterface adderInstruction = adder.getInstruction();

    if (dividerInstruction != null) {
      divider.removeLast();
      nInstructions--;
      return dividerInstruction;
    }

    if (multiplierInstruction != null) {
      multiplier.removeLast();
      nInstructions--;
      return multiplierInstruction;
    }

    if (adderInstruction != null) {
      adder.removeLast();
      nInstructions--;
      return adderInstruction;
    }

    return null;
  }

  /* Shifts instructions into the functional units and calls the EX() method for instructions in the first step
   * this method is called from getCompletedInstruction in order to prepare the pipeline for a new instruction entrance  */
  public void step() {
    //try catch is necessary for handling structural stalls and  stalls coming from the EX() method

    //adder
    adder.step();
    //multiplier
    multiplier.step();
    //divider
    divider.step();
  }

  /** This method is used in  order to understand if the fpPipe is not empty and the all CPU halt are disabled*/
  public boolean isEmpty() {
    return nInstructions == 0;
  }

  public int getDividerCounter() {
    return divider.getCounter();
  }

  /* Resets the fp pipeline */
  public void reset() {
    nInstructions = 0;
    multiplier.reset();
    adder.reset();
    divider.reset();
  }


//---------------------- FUNCTIONAL UNITS ----------------------------------------
  interface FPFunctionalUnit {
    int putInstruction(InstructionInterface instr, boolean simulation);
    InstructionInterface getInstruction();
    void step();
  }

  /** This class models the 7 steps floating point multiplier*/
  private class Multiplier implements FPFunctionalUnit {
    private Map<Constants.FPMultiplierStatus, InstructionInterface> multiplier;
    Multiplier() {
      //Multiplier initialization
      multiplier = new HashMap<>();
      this.reset();
    }

    Map<Constants.FPMultiplierStatus, InstructionInterface> getFuncUnit() {
      return multiplier;
    }

    public String toString() {
      String output = "";
      InstructionInterface instr;
      output += "MULTIPLIER\n";
      output += ((instr = multiplier.get(Constants.FPMultiplierStatus.M1)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      output += ((instr = multiplier.get(Constants.FPMultiplierStatus.M2)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      output += ((instr = multiplier.get(Constants.FPMultiplierStatus.M3)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      output += ((instr = multiplier.get(Constants.FPMultiplierStatus.M4)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      output += ((instr = multiplier.get(Constants.FPMultiplierStatus.M5)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      output += ((instr = multiplier.get(Constants.FPMultiplierStatus.M6)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      output += ((instr = multiplier.get(Constants.FPMultiplierStatus.M7)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      return output;
    }

    /** Resets the functional unit*/
    public void reset() {
      multiplier.put(Constants.FPMultiplierStatus.M1, null);
      multiplier.put(Constants.FPMultiplierStatus.M2, null);
      multiplier.put(Constants.FPMultiplierStatus.M3, null);
      multiplier.put(Constants.FPMultiplierStatus.M4, null);
      multiplier.put(Constants.FPMultiplierStatus.M5, null);
      multiplier.put(Constants.FPMultiplierStatus.M6, null);
      multiplier.put(Constants.FPMultiplierStatus.M7, null);
    }

    /** Inserts the passed instruction in the first position of the functional unit
     * if another instruction holds that position a negative number is returned*/
    public int putInstruction(InstructionInterface instr, boolean simulation_enabled) {
      if (multiplier.get(Constants.FPMultiplierStatus.M1) == null) {
        if (!simulation_enabled) {
          multiplier.put(Constants.FPMultiplierStatus.M1, instr);
        }

        return 0;
      } else {
        return -1;
      }
    }

    /** Returns the last instruction in the functional unit, if any instruction was found
     *  null is returned, the instruction is not removed from the HashMap */
    public InstructionInterface getInstruction() {
      return multiplier.get(Constants.FPMultiplierStatus.M7);
    }

    /** Remove the last instruction in the functional unit*/
    void removeLast() {
      multiplier.put(Constants.FPMultiplierStatus.M7, null);
    }

    /* Shifts instructions into the functional unit and calls the EX() method for instructions in the secondary step
     * this method is called from getCompletedInstruction in order to prepare the pipeline for a new instruction entrance  */
    public void step() {
      if (multiplier.get(Constants.FPMultiplierStatus.M7) == null) {
        multiplier.put(Constants.FPMultiplierStatus.M7, multiplier.get(Constants.FPMultiplierStatus.M6));
        multiplier.put(Constants.FPMultiplierStatus.M6, null);
      }

      if (multiplier.get(Constants.FPMultiplierStatus.M6) == null) {
        multiplier.put(Constants.FPMultiplierStatus.M6, multiplier.get(Constants.FPMultiplierStatus.M5));
        multiplier.put(Constants.FPMultiplierStatus.M5, null);
      }

      if (multiplier.get(Constants.FPMultiplierStatus.M5) == null) {
        multiplier.put(Constants.FPMultiplierStatus.M5, multiplier.get(Constants.FPMultiplierStatus.M4));
        multiplier.put(Constants.FPMultiplierStatus.M4, null);
      }

      if (multiplier.get(Constants.FPMultiplierStatus.M4) == null) {
        multiplier.put(Constants.FPMultiplierStatus.M4, multiplier.get(Constants.FPMultiplierStatus.M3));
        multiplier.put(Constants.FPMultiplierStatus.M3, null);
      }

      if (multiplier.get(Constants.FPMultiplierStatus.M3) == null) {
        multiplier.put(Constants.FPMultiplierStatus.M3, multiplier.get(Constants.FPMultiplierStatus.M2));
        multiplier.put(Constants.FPMultiplierStatus.M2, null);
      }

      if (multiplier.get(Constants.FPMultiplierStatus.M2) == null) {
        multiplier.put(Constants.FPMultiplierStatus.M2, multiplier.get(Constants.FPMultiplierStatus.M1));
        multiplier.put(Constants.FPMultiplierStatus.M1, null);
      }
    }
  }

  /** This class models the 4 steps floating point adder*/
  private class Adder implements FPFunctionalUnit {
    Map<Constants.FPAdderStatus, InstructionInterface> adder;
    Adder() {
      adder = new HashMap<>();
      this.reset();
    }

    Map<Constants.FPAdderStatus, InstructionInterface> getFuncUnit() {
      return adder;
    }

    public String toString() {
      String output = "";
      InstructionInterface instr;
      output += "ADDER\n";
      output += ((instr = adder.get(Constants.FPAdderStatus.A1)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      output += ((instr = adder.get(Constants.FPAdderStatus.A2)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      output += ((instr = adder.get(Constants.FPAdderStatus.A3)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      output += ((instr = adder.get(Constants.FPAdderStatus.A4)) != null) ? instr.getName() + "\n" : "EMPTY\n";
      return output;
    }

    /** Resets the functional unit*/
    public void reset() {
      adder.put(Constants.FPAdderStatus.A1, null);
      adder.put(Constants.FPAdderStatus.A2, null);
      adder.put(Constants.FPAdderStatus.A3, null);
      adder.put(Constants.FPAdderStatus.A4, null);
    }

    /** Inserts the passed instruction in the first position of the functional unit
     * if another instruction holds that position a negative number is returned*/
    public int putInstruction(InstructionInterface instr, boolean simulation) {
      if (adder.get(Constants.FPAdderStatus.A1) == null) {
        if (!simulation) {
          adder.put(Constants.FPAdderStatus.A1, instr);
        }

        return 0;
      } else {
        return -1;
      }
    }

    /** Returns the last instruction in the functional unit, if any instruction was found
     *  null is returned, the instruction is not removed from the HashMap */
    public InstructionInterface getInstruction() {
      return adder.get(Constants.FPAdderStatus.A4);
    }

    /** Remove the last instruction in the functional unit*/
    void removeLast() {
      adder.put(Constants.FPAdderStatus.A4, null);
    }

    /* Shifts instructions into the functional unit and calls the EX() method for the instruction in the secondary step
     * this method is called from getCompletedInstruction in order to prepare the pipeline for a new instruction entrance  */
    public void step() {
      if (adder.get(Constants.FPAdderStatus.A4) == null) {
        adder.put(Constants.FPAdderStatus.A4, adder.get(Constants.FPAdderStatus.A3));
        adder.put(Constants.FPAdderStatus.A3, null);
      }

      if (adder.get(Constants.FPAdderStatus.A3) == null) {
        adder.put(Constants.FPAdderStatus.A3, adder.get(Constants.FPAdderStatus.A2));
        adder.put(Constants.FPAdderStatus.A2, null);
      }

      if (adder.get(Constants.FPAdderStatus.A2) == null) {
        adder.put(Constants.FPAdderStatus.A2, adder.get(Constants.FPAdderStatus.A1));
        adder.put(Constants.FPAdderStatus.A1, null);
      }
    }
  }
  /** This class models the 24 steps floating point divider, instructions are not pipelined
   *  and for this reason a structural hazard happens when a DIV.fmt would to enter the FU when
   *  another DIV.fmt is present */
  private class Divider implements FPFunctionalUnit {
    InstructionInterface instr;
    public int counter;
    Divider() {
      this.reset();
    }
    InstructionInterface getFuncUnit() {
      return instr;
    }


    public String toString() {
      if (instr != null) {
        return "DIVIDER \n " + instr.getName() +  " " + counter;
      } else {
        return "DIVIDER \n " + "EMPTY " + counter;
      }
    }

    /** Inserts the passed instruction in the first position of the functional unit
     * if another instruction holds that position a negative number is returned*/
    public int putInstruction(InstructionInterface instr, boolean simulation) {
      if (this.instr == null) {
        if (!simulation) {
          this.instr = instr;
          this.counter = 24;
        }

        return 0;
      }

      return -1;
    }

    /** Returns the instruction if counter has reached 1 else
     *  null is returned*/
    public InstructionInterface getInstruction() {
      if (counter == 1) {
        return this.instr;
      }

      return null;
    }

    /* Shifts instructions into the functional unit and calls the EX() method for the instruction in the secondary step
     * this method is called from getCompletedInstruction in order to prepare the pipeline for a new instruction entrance  */
    public void step() {
      //if counter has reached 0 the instruction was removed by the previous getCompletedInstruction invocation wich called removeLast()
      //if counter is a number between 0 and 24 it must be decremented by 1
      if (this.instr != null && counter > 0 && counter < 25) {
        counter--;
      }

      //if the divider does not contain instructions anyone operation is carried out
    }

    /** Resets the functional unit*/
    public void reset() {
      instr = null;
      counter = 0;
    }

    /** Return the counter of the divider*/
    public int getCounter() {
      return counter;
    }

    /** Removes the instruction in the functional unit (improper name for to conform to the others f.u.*/
    void removeLast() {
      this.instr = null;
      this.counter = 0;
    }

  }
}
