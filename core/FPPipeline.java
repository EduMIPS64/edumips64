/*
 * FPPipeline.java
 *
 * This class models a MIPS CPU with 32 64-bit General Purpose Register.
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

package edumips64.core;
import edumips64.core.is.*;
import java.util.*;

/** This class models a MIPS FPU  pipeline that supports multiple outstanding FP operations
*   it is used only by the cpu class
*  @author Massimo Trubia
*/
public class FPPipeline {
	//FPU functional units
	public enum FPAdderStatus{A1,A2,A3,A4};
	public enum FPMultiplierStatus{M1,M2,M3,M4,M5,M6,M7};
	private Divider divider;
	private Multiplier multiplier;
	private Adder adder;
	private CPU cpu;
	private Queue<Instruction> entryQueue; //if a structural hazard occurs instructions leave the
					//FPPipeline in the same order by which they has entered
	
	public FPPipeline() {
		cpu=CPU.getInstance();
		//Instanciating functional units objects
		divider= new Divider();
		multiplier=new Multiplier();
		adder =new Adder();
		entryQueue=new LinkedList<Instruction>();
	}
	
	
	/** Inserts the passed instruction into the right functional unit. If no errors occur
	 *  0 is returned, else, if we want to insert an ADD.fmt, MUL.fmt, SUB.fmt  and 
	 *  the first place of the adder or multiplier is filled by other 
	 *  instructions 1 is returned, else, if we want to insert a DIV.fmt and the
	 *  divider is full 2 is returned and CPU raises a StructuralException.
	 *  If an integer instruction is passed at the method 3 is returned
	 */
	public int putInstruction(Instruction instr)
	{
		if(cpu.knownFPInstructions.contains(instr.getName()) && instr!=null)
		{
			entryQueue.offer(instr);
			if(instr.getName().compareToIgnoreCase("ADD.D")==0 || instr.getName().compareToIgnoreCase("SUB.D")==0)
				if(adder.putInstruction(instr)==-1)
					return 1;
			else if(instr.getName().compareToIgnoreCase("MUL.D")==0)
				if(multiplier.putInstruction(instr)==-1)
					return 1;
			else if(instr.getName().compareToIgnoreCase("DIV.D")==0)
				if(divider.putInstruction(instr)==-1)
					return 2;
			return 0;
		}
		return 3;
	}
	
	public Instruction getInstruction()
	{
		//checking if multiple FP instructions are leaving at the same time the FPPipeline
		
		int readyToExit=0; //number of instructions hold the last position of the f.u.
		Instruction instr_mult=multiplier.getInstruction();
		Instruction instr_adder=adder.getInstruction();
		Instruction instr_div=divider.getInstruction();
		if(instr_mult!=null)
			readyToExit++;
		if(instr_mult!=null)
			readyToExit++;
		if(instr_div!=null)
			readyToExit++;
		if(readyToExit>1)
		{
			//Retrieves, but not remove from a temporary queue info about the oldest instruction entered in the pipeline
			Instruction oldestInstr=entryQueue.peek();
			if(oldestInstr!=null)
			{
				//check which instruction withdrawn must to leave the FPPipeline
				if(instr_mult!=null && instr_mult.equals(oldestInstr))
				{	
					multiplier.removeLast();
					return instr_mult;
				}
				if(instr_adder!=null && instr_adder.equals(oldestInstr))
				{
					adder.removeLast();
					return instr_adder;
				}
				if(instr_div!=null && instr_adder.equals(oldestInstr))
				{
					divider.removeLast();
					return instr_div;
				}
					
			}
		}
		else if(readyToExit==1)
		{
			if(instr_mult!=null)
			{
				multiplier.removeLast();
				return instr_mult;
			}
			if(instr_adder!=null)
			{
				adder.removeLast();
				return instr_adder;
			}
			if(instr_div!=null)
			{
				divider.removeLast();
				return instr_div;
			}
		}
		else if(readyToExit==0)
			return null;
		
		
		return null;
		
	}
	
	/** This class models the 7 steps floating point multiplier*/
	private class Multiplier
	{
		private Map<FPMultiplierStatus, Instruction> multiplier;
		Multiplier()
		{
			//Multiplier initialization
			multiplier = new HashMap<FPMultiplierStatus, Instruction>();
			this.reset();
		}
		
		/** Resets the functional unit*/ 
		private void reset()
		{
			multiplier.put(FPPipeline.FPMultiplierStatus.M1,null);
			multiplier.put(FPPipeline.FPMultiplierStatus.M2,null);
			multiplier.put(FPPipeline.FPMultiplierStatus.M3,null);
			multiplier.put(FPPipeline.FPMultiplierStatus.M4,null);
			multiplier.put(FPPipeline.FPMultiplierStatus.M5,null);
			multiplier.put(FPPipeline.FPMultiplierStatus.M6,null);
			multiplier.put(FPPipeline.FPMultiplierStatus.M7,null);
		}
		
		/** Inserts the passed instruction in the first position of the functional unit
		    if another instruction holds that position a negative number is returned*/
		private int putInstruction(Instruction instr)
		{
			if(multiplier.get(FPPipeline.FPMultiplierStatus.M1)==null)
			{
				multiplier.put(FPPipeline.FPMultiplierStatus.M1,instr);
				return 0;
			}
			else
				return -1;
		}
		
		/** Returns the last instruction in the functional unit, if any instruction was found 
		 *  null is returned, the instruction is not removed from the HashMap */
		private Instruction getInstruction()
		{
			Instruction instr;
			if((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M7))==null)
				return null;
			return instr;
		}
		
		/** Remove the last instruction in the functional unit*/
		private void removeLast()
		{
			multiplier.put(FPPipeline.FPMultiplierStatus.M7,null);
		}
		
		
	}
	
	/** This class models the 4 steps floating point adder*/
	private class Adder
	{
		private Map<FPAdderStatus, Instruction> adder;
		Adder()
		{
			adder = new HashMap<FPAdderStatus, Instruction>();
			this.reset();
		}
		
		/** Resets the functional unit*/
		private void reset()
		{
			adder.put(FPPipeline.FPAdderStatus.A1,null);
			adder.put(FPPipeline.FPAdderStatus.A2,null);
			adder.put(FPPipeline.FPAdderStatus.A3,null);
			adder.put(FPPipeline.FPAdderStatus.A4,null);
		}
		
		/** Inserts the passed instruction in the first position of the functional unit
		    if another instruction holds that position a negative number is returned*/
		private int putInstruction(Instruction instr)
		{
			if(adder.get(FPPipeline.FPAdderStatus.A1)==null)
			{
				adder.put(FPPipeline.FPAdderStatus.A1,instr);
				return 0;
			}
			else
				return -1;
		}
		
		/** Returns the last instruction in the functional unit, if any instruction was found 
		 *  null is returned, the instruction is not removed from the HashMap */
		private Instruction getInstruction()
		{
			Instruction instr;
			if((instr=adder.get(FPPipeline.FPAdderStatus.A4))==null)
				return null;
			return instr;
		}
		
		/** Remove the last instruction in the functional unit*/
		private void removeLast()
		{
			adder.put(FPPipeline.FPAdderStatus.A4,null);
		}
		
	}
	
	/** This class models the 24 steps floating point divider, instructions are not pipelined
	 *  and for this reason a structural hazard happens when a DIV.fmt would to enter the FU when
	 *  another DIV.fmt is present */
	private class Divider
	{
		private Instruction instr;
		private int counter;
		Divider()
		{
			this.reset();
		}
		/** Inserts the passed instruction in the first position of the functional unit
		    if another instruction holds that position a negative number is returned*/
		private int putInstruction(Instruction instr)
		{
			if(instr==null)
			{
				this.instr=instr;
				this.counter=24;
				return 0;
			}
			return -1;
		}

		/** Returns the last instruction in the functional unit, if any instruction was found 
		 *  null is returned, the instruction is not removed from the HashMap */
		private Instruction getInstruction()
		{
			return this.instr;
		}
		
		/** Resets the functional unit*/
		private void reset()
		{
			instr=null;
			counter=0;
		}
		
		/** Increases the counter and return the new value*/
		private int incrCounter()
		{
			counter++;
			return counter;
		}
		
		/** Decreases the counter and return the new value*/
		private int decrCounter()
		{
			counter--;
			return counter;
		}
		
		/** Return the counter of the divider*/
		private int getCounter()
		{
			return counter;
		}

		/** Removes the instruction in the functional unit (improper name for to conform to the others f.u.*/
		private void removeLast()
		{
			this.instr=null;
			this.counter=0;
		}
		
	}
	
	
}