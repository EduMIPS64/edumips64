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
import org.edumips64.core.*;
import org.edumips64.core.is.*;
import org.edumips64.utils.IrregularStringOfBitsException;
import java.util.*;

/** This class models a MIPS FPU  pipeline that supports multiple outstanding FP operations
 *   it is used only by the cpu class
 *  @author Massimo Trubia
 */
public class FPPipeline {
	//FPU functional units
	static class Costanti{
		public enum FPAdderStatus{A1,A2,A3,A4};
		public enum FPMultiplierStatus{M1,M2,M3,M4,M5,M6,M7};
		public enum FPDividerStatus{DIVIDER};
	}
	public static int STRUCT_HAZARD=0; //status constant of pipeStatus[]
	private int pipeStatus[];
	private Divider divider;
	private Multiplier multiplier;
	private Adder adder;
	private CPU cpu;
	private int nInstructions; //used for understanding if the fpPipe is empty or not
	private Queue<Instruction> entryQueue; //if an output structural hazard occurs instructions leave the
	//FPPipeline in the same order by which they has entered
	private int readyToExit; //number of instructions that hold the last position of the f.u.
	
	
	public FPPipeline() {
		//Instanciating functional units objects
		nInstructions=0;
		divider= new Divider();
		divider.reset();
		multiplier=new Multiplier();
		multiplier.reset();
		adder =new Adder();
		adder.reset();
		entryQueue=new LinkedList<Instruction>();
		pipeStatus=new int[1];
		pipeStatus[STRUCT_HAZARD]=0; // 0 means that any structural hazard at the last getInstruction() call
		// happened. 1 means the contrary.
		readyToExit=0;
	}
	
	public String toString(){
		String output="";
		output+=adder.toString();
		output+=multiplier.toString();
		output+=divider.toString();
		return output;
	}
	
	public int getNReadyToExitInstr() {
		return readyToExit;
	}
	
	/** Returns true if the specified functional unit is filled by an instruction, false when the contrary happens.
	 *  No controls are carried out on the legality of parameters, for mistaken parameters false is returned
	 *  @param funcUnit The functional unit to check. Legal values are "ADDER", "MULTIPLIER", "DIVIDER"
	 *  @param stage The integer that refers to the stage of the functional unit.
	 *			ADDER [1,4], MULTIPLIER [1,7], DIVIDER [any] */
	public boolean isFuncUnitFilled(String funcUnit, int stage) {
		if(funcUnit.compareToIgnoreCase("ADDER")==0)
			switch(stage) {
				case 1:
					return (adder.getFuncUnit().get(Costanti.FPAdderStatus.A1)!=null) ? true : false;
				case 2:
					return (adder.getFuncUnit().get(Costanti.FPAdderStatus.A2)!=null) ? true : false;
				case 3:
					return (adder.getFuncUnit().get(Costanti.FPAdderStatus.A3)!=null) ? true : false;
				case 4:
					return (adder.getFuncUnit().get(Costanti.FPAdderStatus.A4)!=null) ? true : false;
			}
			if(funcUnit.compareToIgnoreCase("MULTIPLIER")==0)
				switch(stage) {
					case 1:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M1)!= null) ? true : false;
					case 2:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M2)!= null) ? true : false;
					case 3:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M3)!= null) ? true : false;
					case 4:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M4)!= null) ? true : false;
					case 5:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M5)!= null) ? true : false;
					case 6:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M6)!= null) ? true : false;
					case 7:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M7)!= null) ? true : false;
				}
				if(funcUnit.compareToIgnoreCase("DIVIDER")==0)
					return (divider.getFuncUnit()!=null) ? true : false;
				
				return false;
	}
	/** Returns the instruction of the specified functional unit , null if it is empty.
	 *  No controls are carried out on the legality of parameters, for mistaken parameters null is returned
	 *  @param funcUnit The functional unit to check. Legal values are "ADDER", "MULTIPLIER", "DIVIDER"
	 *  @param stage The integer that refers to the stage of the functional unit.
	 *			ADDER [1,4], MULTIPLIER [1,7], DIVIDER [any] */
	
	public Instruction getInstructionByFuncUnit(String funcUnit, int stage) {
		if(funcUnit.compareToIgnoreCase("ADDER")==0)
			switch(stage) {
				case 1:
					return (adder.getFuncUnit().get(Costanti.FPAdderStatus.A1));
				case 2:
					return (adder.getFuncUnit().get(Costanti.FPAdderStatus.A2));
				case 3:
					return (adder.getFuncUnit().get(Costanti.FPAdderStatus.A3));
				case 4:
					return (adder.getFuncUnit().get(Costanti.FPAdderStatus.A4));
			}
			if(funcUnit.compareToIgnoreCase("MULTIPLIER")==0)
				switch(stage) {
					case 1:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M1));
					case 2:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M2));
					case 3:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M3));
					case 4:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M4));
					case 5:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M5));
					case 6:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M6));
					case 7:
						return (multiplier.getFuncUnit().get(Costanti.FPMultiplierStatus.M7));
				}
				if(funcUnit.compareToIgnoreCase("DIVIDER")==0)
					return (divider.getFuncUnit());
				
				return null;
	}
	
	/** Inserts the passed instruction into the right functional unit. If no errors occur
	 *  0 is returned, else, if we want to insert an ADD.fmt, MUL.fmt, SUB.fmt  and
	 *  the first place of the adder or multiplier is filled by other
	 *  instructions 1 is returned, else, if we want to insert a DIV.fmt and the
	 *  divider is full 2 is returned and the CPU raises a StructuralException.
	 *  If an integer instruction is passed at the method 3 is returned
	 */
	public int putInstruction(Instruction instr,boolean simulation) //throws InputStructuralHazardException
	{
		cpu=CPU.getInstance();
		if(CPU.knownFPInstructions.contains(instr.getName()) && instr!=null) {
			String instrName=instr.getName();
			if((instrName.compareToIgnoreCase("ADD.D")==0) || (instrName.compareToIgnoreCase("SUB.D")==0))
				if(adder.putInstruction(instr,simulation)==-1)
					return 1;
			if(instrName.compareToIgnoreCase("MUL.D")==0)
				if(multiplier.putInstruction(instr,simulation)==-1)
					return 1;
			if(instrName.compareToIgnoreCase("DIV.D")==0)
				if(divider.putInstruction(instr,simulation)==-1)
					return 2;
			if(!simulation)
				nInstructions++;
			return 0;
		}
		return 3;
	}
	
	public Instruction getInstruction(boolean simulation) {
		//checking if multiple FP instructions are leaving at the same time the FPPipeline
		readyToExit=0;
		Instruction instr_mult=multiplier.getInstruction();
		Instruction instr_adder=adder.getInstruction();
		Instruction instr_div=divider.getInstruction();
		if(instr_mult!=null)
			readyToExit++;
		if(instr_adder!=null)
			readyToExit++;
		if(instr_div!=null)
			readyToExit++;
		if(readyToExit>0) {
			if(instr_div!=null) {
				if(!simulation) {
					divider.removeLast();
					nInstructions--;
				}
				return instr_div;
			}
			if(instr_mult!=null) {
				if(!simulation) {
					multiplier.removeLast();
					nInstructions--;
				}
				return instr_mult;
			}
			if(instr_adder!=null) {
				if(!simulation) {
					adder.removeLast();
					nInstructions--;
				}
				return instr_adder;
			}
		}
		return null;
	}
	
	/* Shifts instructions into the functional units and calls the EX() method for instructions in the first step
	 * this method is called from getInstruction in order to prepare the pipeline for a new instruction entrance	*/
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
		return (nInstructions==0)?true:false;
	}
	
	/** Returns the FPPipeline status, index must be one between the status constants
	 * @return the value of the status vector. 0 means the status is false, 1 the contrary . -1 if the status doesn't exist
	 *
	 **/
	public int getStatus(int index) {
		if(index>-1 && index<pipeStatus.length)
			return pipeStatus[index];
		return -1;
	}
	
	public int getDividerCounter() {
		return divider.getCounter();
	}
	
	/* Resets the fp pipeline */
	public void reset() {
		nInstructions=0;
        entryQueue.clear();
		multiplier.reset();
		adder.reset();
		divider.reset();
	}
	

//---------------------- FUNCTIONAL UNITS ----------------------------------------
	interface FPFunctionalUnit{
        public Object getFuncUnit();
		public int putInstruction(Instruction instr,boolean simulation);
		public Instruction getInstruction();
		public void removeLast();
		public void step();
	}
	
	/** This class models the 7 steps floating point multiplier*/
	private class Multiplier implements FPFunctionalUnit{
		private Map<Costanti.FPMultiplierStatus, Instruction> multiplier;
		Multiplier() {
			//Multiplier initialization
			multiplier = new HashMap<Costanti.FPMultiplierStatus, Instruction>();
			this.reset();
		}
		
		public Map<Costanti.FPMultiplierStatus, Instruction> getFuncUnit() {
			return multiplier;
		}
		
		public String toString() {
			String output="";
			Instruction instr;
			output+="MULTIPLIER\n";
			output+=((instr=multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M1))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M2))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M3))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M4))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M5))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M6))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M7))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			return output;
		}
		
		/** Resets the functional unit*/
		public void reset() {
			multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M1,null);
			multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M2,null);
			multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M3,null);
			multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M4,null);
			multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M5,null);
			multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M6,null);
			multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M7,null);
		}
		
		/** Inserts the passed instruction in the first position of the functional unit
		 * if another instruction holds that position a negative number is returned*/
		public int putInstruction(Instruction instr,boolean simulation) {
			if(multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M1)==null) {
				if(!simulation)
					multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M1,instr);
				return 0;
			} else
				return -1;
		}
		
		/** Returns the last instruction in the functional unit, if any instruction was found
		 *  null is returned, the instruction is not removed from the HashMap */
		public Instruction getInstruction() {
			Instruction instr;
			if((instr=multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M7))==null)
				return null;
			return instr;
		}
		
		/** Remove the last instruction in the functional unit*/
		public void removeLast() {
			multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M7,null);
		}
		
		/* Shifts instructions into the functional unit and calls the EX() method for instructions in the secondary step
		 * this method is called from getInstruction in order to prepare the pipeline for a new instruction entrance	*/
		public void step() {
			if(multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M7)==null){
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M7,multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M6));
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M6,null);
			}
			if(multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M6)==null){
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M6,multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M5));
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M5,null);
			}
			if(multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M5)==null){
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M5,multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M4));
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M4,null);
			}
			if(multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M4)==null){
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M4,multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M3));
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M3,null);
			}
			if(multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M3)==null){
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M3,multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M2));
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M2,null);
			}
			if(multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M2)==null){
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M2,multiplier.get(FPPipeline.Costanti.FPMultiplierStatus.M1));
				multiplier.put(FPPipeline.Costanti.FPMultiplierStatus.M1,null);
			}
		}
	}
	
	/** This class models the 4 steps floating point adder*/
	private class Adder implements FPFunctionalUnit{
		public Map<Costanti.FPAdderStatus, Instruction> adder;
		Adder() {
			adder = new HashMap<Costanti.FPAdderStatus, Instruction>();
			this.reset();
		}
		
		public Map<Costanti.FPAdderStatus, Instruction> getFuncUnit() {
			return adder;
		}
		
		public String toString() {
			String output="";
			Instruction instr;
			output+="ADDER\n";
			output+=((instr=adder.get(FPPipeline.Costanti.FPAdderStatus.A1))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=adder.get(FPPipeline.Costanti.FPAdderStatus.A2))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=adder.get(FPPipeline.Costanti.FPAdderStatus.A3))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=adder.get(FPPipeline.Costanti.FPAdderStatus.A4))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			return output;
		}
		
		/** Resets the functional unit*/
		public void reset() {
			adder.put(FPPipeline.Costanti.FPAdderStatus.A1,null);
			adder.put(FPPipeline.Costanti.FPAdderStatus.A2,null);
			adder.put(FPPipeline.Costanti.FPAdderStatus.A3,null);
			adder.put(FPPipeline.Costanti.FPAdderStatus.A4,null);
		}
		
		/** Inserts the passed instruction in the first position of the functional unit
		 * if another instruction holds that position a negative number is returned*/
		public int putInstruction(Instruction instr,boolean simulation) {
			if(adder.get(FPPipeline.Costanti.FPAdderStatus.A1)==null) {
				if(!simulation)
					adder.put(FPPipeline.Costanti.FPAdderStatus.A1,instr);
				return 0;
			} else
				return -1;
		}
		
		/** Returns the last instruction in the functional unit, if any instruction was found
		 *  null is returned, the instruction is not removed from the HashMap */
		public Instruction getInstruction() {
			Instruction instr;
			if((instr=adder.get(FPPipeline.Costanti.FPAdderStatus.A4))==null)
				return null;
			return instr;
		}
		
		/** Remove the last instruction in the functional unit*/
		public void removeLast() {
			adder.put(FPPipeline.Costanti.FPAdderStatus.A4,null);
		}
		
		/* Shifts instructions into the functional unit and calls the EX() method for the instruction in the secondary step
		 * this method is called from getInstruction in order to prepare the pipeline for a new instruction entrance	*/
		public void step() {
			if(adder.get(FPPipeline.Costanti.FPAdderStatus.A4)==null){
				adder.put(FPPipeline.Costanti.FPAdderStatus.A4,adder.get(FPPipeline.Costanti.FPAdderStatus.A3));
				adder.put(FPPipeline.Costanti.FPAdderStatus.A3,null);
			}
			if(adder.get(FPPipeline.Costanti.FPAdderStatus.A3)==null){
				adder.put(FPPipeline.Costanti.FPAdderStatus.A3,adder.get(FPPipeline.Costanti.FPAdderStatus.A2));
				adder.put(FPPipeline.Costanti.FPAdderStatus.A2,null);
			}
			if(adder.get(FPPipeline.Costanti.FPAdderStatus.A2)==null){
				adder.put(FPPipeline.Costanti.FPAdderStatus.A2,adder.get(FPPipeline.Costanti.FPAdderStatus.A1));
				adder.put(FPPipeline.Costanti.FPAdderStatus.A1,null);
			}
		}
	}
	/** This class models the 24 steps floating point divider, instructions are not pipelined
	 *  and for this reason a structural hazard happens when a DIV.fmt would to enter the FU when
	 *  another DIV.fmt is present */
	private class Divider implements FPFunctionalUnit{
		public Instruction instr;
		public int counter;
		Divider() {
			this.reset();
		}
		public Instruction getFuncUnit() {
			return instr;
		}
		
		
		public String toString() {
			if(instr!=null)
				return "DIVIDER \n "+ instr.getName() +  " " +counter;
			else
				return "DIVIDER \n "+ "EMPTY " + counter;
		}
		
		/** Inserts the passed instruction in the first position of the functional unit
		 * if another instruction holds that position a negative number is returned*/
		public int putInstruction(Instruction instr,boolean simulation) {
			if(this.instr==null) {
				if(!simulation) {
					this.instr=instr;
					this.counter=24;
				}
				return 0;
			}
			return -1;
		}
		
		/** Returns the instruction if counter has reached 1 else
		 *  null is returned*/
		public Instruction getInstruction() {
			if(counter==1)
				return this.instr;
			return null;
		}


		
		/* Shifts instructions into the functional unit and calls the EX() method for the instruction in the secondary step
		 * this method is called from getInstruction in order to prepare the pipeline for a new instruction entrance	*/
		public void step() {
			//if counter has reached 0 the instruction was removed by the previous getInstruction invocation wich called removeLast()
			//if counter is a number between 0 and 24 it must be decremented by 1
			if(this.instr!=null && counter>0 && counter <25) {
				counter--;
			}
			//if the divider does not contain instructions anyone operation is carried out
		}
		
		
		/** Resets the functional unit*/
		public void reset() {
			instr=null;
			counter=0;
		}
		
		/** Return the counter of the divider*/
		public int getCounter() {
			return counter;
		}
		
		/** Removes the instruction in the functional unit (improper name for to conform to the others f.u.*/
		public void removeLast() {
			this.instr=null;
			this.counter=0;
		}
		
	}
}
