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

package edumips64.core.fpu;
import edumips64.core.*;
import edumips64.core.is.*;
import edumips64.utils.IrregularStringOfBitsException;
import java.util.*;

/** This class models a MIPS FPU  pipeline that supports multiple outstanding FP operations
*   it is used only by the cpu class
*  @author Massimo Trubia
*/
public class FPPipeline {
	//FPU functional units
	public enum FPAdderStatus{A1,A2,A3,A4};
	public enum FPMultiplierStatus{M1,M2,M3,M4,M5,M6,M7};
	public enum FPDividerStatus{DIVIDER};
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
	
	public int getNReadyToExitInstr()
	{
		return readyToExit;
	}
	
	/** Returns true if the specified functional unit is filled by an instruction, false when the contrary happens.
	 *  No controls are carried out on the legality of parameters, for mistaken parameters false is returned
	 *  @param funcUnit The functional unit to check. Legal values are "ADDER", "MULTIPLIER", "DIVIDER"
	 *  @param stage The integer that refers to the stage of the functional unit. 
	 *			ADDER [1,4], MULTIPLIER [1,7], DIVIDER [any] */
	public boolean isFuncUnitFilled(String funcUnit, int stage)
	{
		if(funcUnit.compareToIgnoreCase("ADDER")==0)
			switch(stage)
			{
				case 1:
					return (adder.getFuncUnit().get(FPAdderStatus.A1)!=null) ? true : false; 
				case 2:
					return (adder.getFuncUnit().get(FPAdderStatus.A2)!=null) ? true : false;
				case 3:
					return (adder.getFuncUnit().get(FPAdderStatus.A3)!=null) ? true : false;
				case 4:
					return (adder.getFuncUnit().get(FPAdderStatus.A4)!=null) ? true : false;
			}
		if(funcUnit.compareToIgnoreCase("MULTIPLIER")==0)
			switch(stage)
			{
				case 1:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M1)!= null) ? true : false;
				case 2:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M2)!= null) ? true : false;
				case 3:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M3)!= null) ? true : false;
				case 4:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M4)!= null) ? true : false;
				case 5:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M5)!= null) ? true : false;
				case 6:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M6)!= null) ? true : false;
				case 7:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M7)!= null) ? true : false;
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
	
	public Instruction getInstructionByFuncUnit(String funcUnit, int stage)
	{
		if(funcUnit.compareToIgnoreCase("ADDER")==0)
			switch(stage)
			{
				case 1:
					return (adder.getFuncUnit().get(FPAdderStatus.A1)); 
				case 2:
					return (adder.getFuncUnit().get(FPAdderStatus.A2));
				case 3:
					return (adder.getFuncUnit().get(FPAdderStatus.A3));
				case 4:
					return (adder.getFuncUnit().get(FPAdderStatus.A4));
			}
		if(funcUnit.compareToIgnoreCase("MULTIPLIER")==0)
			switch(stage)
			{
				case 1:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M1));
				case 2:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M2));
				case 3:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M3));
				case 4:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M4));
				case 5:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M5));
				case 6:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M6));
				case 7:
					return (multiplier.getFuncUnit().get(FPMultiplierStatus.M7));
			}
		if(funcUnit.compareToIgnoreCase("DIVIDER")==0)
			return (divider.getFuncUnit());
		
		return null;
	}
	/** Returns the stage's name of the instruction passed as serialNumber between this values
	 *  A1,A2,A3,A4,M1,M2,M3,M4,M5,M6,M7,DIVXX, in wich XX means the divider's counter value
	 *  If the FP pipe doesn't contain the instruction null is returned */
	public String getInstructionStage(long serialNumber)
	{
		String stage;
		if ((stage=adder.getInstructionStage(serialNumber))!=null)
			return stage;
		if((stage=multiplier.getInstructionStage(serialNumber))!=null)
			return stage;
		if((stage=divider.getInstructionStage(serialNumber))!=null)
			return stage;
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
		if(cpu.knownFPInstructions.contains(instr.getName()) && instr!=null)
		{
			//only for aging mechanism
			//if(!simulation)
			//	entryQueue.offer(instr);
			String instrName=instr.getName();
			if((instrName.compareToIgnoreCase("ADD.D")==0) || (instrName.compareToIgnoreCase("SUB.D")==0))
				if(adder.putInstruction(instr,simulation)==-1)
					return 1;
					//throw new InputStructuralHazardException();
			if(instrName.compareToIgnoreCase("MUL.D")==0)
				if(multiplier.putInstruction(instr,simulation)==-1)
					return 1;
					//throw new InputStructuralHazardException();
			if(instrName.compareToIgnoreCase("DIV.D")==0)
				if(divider.putInstruction(instr,simulation)==-1)
					return 2;
					//throw new InputStructuralHazardException();
			if(!simulation)
				nInstructions++;
			return 0;
		}
		return 3;
	}
	
	public Instruction getInstruction(boolean simulation)
	{
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
		if(readyToExit>0)
		{
			//structural stall status is setted
//			if(!simulation)
//				pipeStatus[STRUCT_HAZARD]=1;
			
		     
			//Retrieves, and remove from a temporary queue info about the oldest instruction entered in the pipeline
	/*--------- WITH AGING MECHANISM
			Instruction oldestInstr=null;
			if(simulation)
				oldestInstr=entryQueue.peek();
			else
				oldestInstr=entryQueue.remove();
			if(oldestInstr!=null)
			{
				//check which instruction withdrawn must to leave the FPPipeline
				if(instr_mult!=null && instr_mult.equals(oldestInstr))
				{	
					if(!simulation)
					{
						multiplier.removeLast();
						nInstructions--;
					}
					return instr_mult;
				}
				if(instr_adder!=null && instr_adder.equals(oldestInstr))
				{
					if(!simulation)
					{
						adder.removeLast();
						nInstructions--;
					}
					return instr_adder;
				}
				if(instr_div!=null && instr_div.equals(oldestInstr))
				{
					if(!simulation)
					{
						divider.removeLast();
						nInstructions--;
					}
					return instr_div;
				}
					
			}
	---------------------*/
			//WITHOUT AGING MECHANISM (order for exiting from the pipeline (Divider, Multiplier, Adder)
				if(instr_div!=null)
				{
					if(!simulation)
					{
						divider.removeLast();
						nInstructions--;
					}
					return instr_div;
				}
				if(instr_mult!=null)
				{	
					if(!simulation)
					{
						multiplier.removeLast();
						nInstructions--;
					}
					return instr_mult;
				}				
				if(instr_adder!=null)
				{
					if(!simulation)
					{
						adder.removeLast();
						nInstructions--;
					}
					return instr_adder;
				}

			
			
		}
		/*
		else if(readyToExit==1)
		{
			//structural stall status deactivated
			if(!simulation)
				pipeStatus[STRUCT_HAZARD]=0;
			if(instr_mult!=null)
			{
				if(!simulation)
					multiplier.removeLast();
				return instr_mult;
			}
			if(instr_adder!=null)
			{
				if(!simulation)
					adder.removeLast();
				return instr_adder;
			}
			if(instr_div!=null)
			{
				if(!simulation)
					divider.removeLast();
				return instr_div;
			}
		}
		
		else if(readyToExit==0)
		{
			//structural stall status deactivated
			if(!simulation)
				pipeStatus[STRUCT_HAZARD]=0;
			return null;
		}
		*/
		return null;
		
	}
	
	/* Calls EX methods for fu passed as strings (MULTIPLIER,ADDER,DIVIDER) returning obtained values*/
	public int callEX(String funcUnit) throws HaltException, IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException, DivisionByZeroException, FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, NotAlignException
	{
		if(funcUnit.compareToIgnoreCase("MULTIPLIER")==0)
			return multiplier.EX();
		else if(funcUnit.compareToIgnoreCase("ADDER")==0)
			return adder.EX();
		else if(funcUnit.compareToIgnoreCase("DIVIDER")==0)
			return divider.EX();
		return -1;
	}
	
	/** Determines if it is possible to execute the EX method for instructions so that they can be moved to the next stage*/
	public boolean isExecutable(String funcUnit)
	{
		if(funcUnit.compareToIgnoreCase("MULTIPLIER")==0)
			return multiplier.isExecutable();
		else if(funcUnit.compareToIgnoreCase("ADDER")==0)
			return adder.isExecutable();
		else if(funcUnit.compareToIgnoreCase("DIVIDER")==0)
			return divider.isExecutable();
		return false;
		
	}



	/* Shifts instructions into the functional units and calls the EX() method for instructions in the first step
	 * this method is called from getInstruction in order to prepare the pipeline for a new instruction entrance	*/
	public void step()
	{
		//try catch is necessary for handling structural stalls and  stalls coming from the EX() method  

		//adder
		adder.step();
		//multiplier
		multiplier.step();
		//divider
		divider.step();
	}
	
	/** This method is used in  order to understand if the fpPipe is not empty and the all CPU halt are disabled*/
	public boolean isEmpty()
	{
		return (nInstructions==0)?true:false;
	}
	
	/** Returns the FPPipeline status, index must be one between the status constants
	 * @returns the value of the status vector. 0 means the status is false, 1 the contrary . -1 if the status doesn't exist
	 *
	 **/
	public int getStatus(int index)
	{
		if(index>-1 && index<pipeStatus.length)
			return pipeStatus[index];
		return -1;
	}
	
	public int getDividerCounter()
	{
		return divider.getCounter();
	}
	
	/* Resets the fp pipeline */
	public void reset()
	{
		nInstructions=0;
		for(Iterator it=entryQueue.iterator();it.hasNext();)
		{
			entryQueue.poll();
		}
		multiplier.reset();
		adder.reset();
		divider.reset();
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
		
		private Map<FPMultiplierStatus, Instruction> getFuncUnit()
		{
			return multiplier;
		}
		
		public String toString()
		{
			String output="";
			Instruction instr;
			output+="MULTIPLIER\n";
			output+=((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M1))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M2))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M3))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M4))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M5))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M6))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M7))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			return output;
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
		private int putInstruction(Instruction instr,boolean simulation)
		{
			if(multiplier.get(FPPipeline.FPMultiplierStatus.M1)==null)
			{
				if(!simulation)
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

		/** Calls the EX() method of the instruction in M1, if no instruction fills M1 this method returns 0,if EX is correctly executed 1 is returned, if EX cannot be executed becaus M2 contains an instruction, -1 is returned */
		private int EX() throws HaltException, IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException, DivisionByZeroException, FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, NotAlignException
		{
			Instruction instr;
			if((instr=multiplier.get(FPMultiplierStatus.M1))!=null && multiplier.get(FPMultiplierStatus.M2)==null)
			{
				instr.EX();
				return 1;
			}
			if(instr==null)
				return 0;
			return -1;
		}
		
		/** Returns true if the in the secondary stage there isn't an instruction and EX on the 
		 * instruction at the first stage can be successfully invoked because it is possible to move the instruction after the execution
		 */
		private boolean isExecutable()
		{
			if(multiplier.get(FPMultiplierStatus.M2)==null)
				return true;
			return false;
		}		
		
		/* Shifts instructions into the functional unit and calls the EX() method for instructions in the secondary step
		 * this method is called from getInstruction in order to prepare the pipeline for a new instruction entrance	*/
		private void step()
		{
/* lo shift deve avvenire anche se l'ultimo stage è occupato			
			//only if the M7 stage is available the shift is carried out
			if(multiplier.get(FPPipeline.FPMultiplierStatus.M7)==null)
			{
				multiplier.put(FPPipeline.FPMultiplierStatus.M7,multiplier.get(FPPipeline.FPMultiplierStatus.M6));
				multiplier.put(FPPipeline.FPMultiplierStatus.M6,multiplier.get(FPPipeline.FPMultiplierStatus.M5));
				multiplier.put(FPPipeline.FPMultiplierStatus.M5,multiplier.get(FPPipeline.FPMultiplierStatus.M4));
				multiplier.put(FPPipeline.FPMultiplierStatus.M4,multiplier.get(FPPipeline.FPMultiplierStatus.M3));
				multiplier.put(FPPipeline.FPMultiplierStatus.M3,multiplier.get(FPPipeline.FPMultiplierStatus.M2));

				//here there is the EX() invocation of the M1 instruction
				
				multiplier.put(FPPipeline.FPMultiplierStatus.M2,multiplier.get(FPPipeline.FPMultiplierStatus.M1));
				multiplier.put(FPPipeline.FPMultiplierStatus.M1,null);
			}
			//a structural stall happens because the instruction in the M7 was not removed from the getInstruction()
			else
			{
				pipeStatus[FPPipeline.STRUCT_HAZARD]=1;
			}
		}	
*/				
			if(multiplier.get(FPPipeline.FPMultiplierStatus.M7)==null){			
				multiplier.put(FPPipeline.FPMultiplierStatus.M7,multiplier.get(FPPipeline.FPMultiplierStatus.M6));
				multiplier.put(FPPipeline.FPMultiplierStatus.M6,null);
			}
			if(multiplier.get(FPPipeline.FPMultiplierStatus.M6)==null){
				multiplier.put(FPPipeline.FPMultiplierStatus.M6,multiplier.get(FPPipeline.FPMultiplierStatus.M5));
				multiplier.put(FPPipeline.FPMultiplierStatus.M5,null);
			}
			if(multiplier.get(FPPipeline.FPMultiplierStatus.M5)==null){
				multiplier.put(FPPipeline.FPMultiplierStatus.M5,multiplier.get(FPPipeline.FPMultiplierStatus.M4));
				multiplier.put(FPPipeline.FPMultiplierStatus.M4,null);
			}
			if(multiplier.get(FPPipeline.FPMultiplierStatus.M4)==null){
				multiplier.put(FPPipeline.FPMultiplierStatus.M4,multiplier.get(FPPipeline.FPMultiplierStatus.M3));
				multiplier.put(FPPipeline.FPMultiplierStatus.M3,null);
			}
			if(multiplier.get(FPPipeline.FPMultiplierStatus.M3)==null){
				multiplier.put(FPPipeline.FPMultiplierStatus.M3,multiplier.get(FPPipeline.FPMultiplierStatus.M2));
				multiplier.put(FPPipeline.FPMultiplierStatus.M2,null);
			}
			if(multiplier.get(FPPipeline.FPMultiplierStatus.M2)==null){
				multiplier.put(FPPipeline.FPMultiplierStatus.M2,multiplier.get(FPPipeline.FPMultiplierStatus.M1));
				multiplier.put(FPPipeline.FPMultiplierStatus.M1,null);
			}
		}
		
		public String getInstructionStage(long serialNumber)
		{
			Instruction instr;
			if((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M1))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "M1";
			if((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M2))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "M2";
			if((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M3))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "M3";
			if((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M4))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "M4";
			if((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M5))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "M5";
			if((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M6))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "M6";
			if((instr=multiplier.get(FPPipeline.FPMultiplierStatus.M7))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "M7";
			
			return null;
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

		private Map<FPAdderStatus, Instruction> getFuncUnit()
		{
			return adder;
		}		
		
		public String toString()
		{
			String output="";
			Instruction instr;
			output+="ADDER\n";
			output+=((instr=adder.get(FPPipeline.FPAdderStatus.A1))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=adder.get(FPPipeline.FPAdderStatus.A2))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=adder.get(FPPipeline.FPAdderStatus.A3))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			output+=((instr=adder.get(FPPipeline.FPAdderStatus.A4))!=null) ? instr.getName()+"\n" : "EMPTY\n";
			return output;
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
		private int putInstruction(Instruction instr,boolean simulation)
		{
			if(adder.get(FPPipeline.FPAdderStatus.A1)==null)
			{
				if(!simulation)
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
		
		/** Calls the EX() method of the instruction in A1, if no instruction fills A1 this method returns 0,if EX is correctly executed 1 is returned, if EX cannot be executed becaus A2 contains an instruction, -1 is returned */
		private int EX() throws HaltException, IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException, DivisionByZeroException, FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, NotAlignException
		{
			Instruction instr;
			if((instr=adder.get(FPAdderStatus.A1))!=null && adder.get(FPAdderStatus.A2)==null)
			{
				instr.EX();
				return 1;
			}
			if(instr==null)
				return 0;
			return -1;
		}

		/** Returns true if the in the secondary stage there isn't an instruction and EX on the 
		 * instruction at the first stage can be successfully invoked because it is possible to move the instruction after the execution
		 */
		private boolean isExecutable()
		{
			if(adder.get(FPAdderStatus.A2)==null)
				return true;
			return false;
		}		
		
		/* Shifts instructions into the functional unit and calls the EX() method for the instruction in the secondary step
		 * this method is called from getInstruction in order to prepare the pipeline for a new instruction entrance	*/
		private void step()
		{
			if(adder.get(FPPipeline.FPAdderStatus.A4)==null){
				adder.put(FPPipeline.FPAdderStatus.A4,adder.get(FPPipeline.FPAdderStatus.A3));
				adder.put(FPPipeline.FPAdderStatus.A3,null);
			}
			if(adder.get(FPPipeline.FPAdderStatus.A3)==null){
				adder.put(FPPipeline.FPAdderStatus.A3,adder.get(FPPipeline.FPAdderStatus.A2));
				adder.put(FPPipeline.FPAdderStatus.A2,null);
			}
			if(adder.get(FPPipeline.FPAdderStatus.A2)==null){
				adder.put(FPPipeline.FPAdderStatus.A2,adder.get(FPPipeline.FPAdderStatus.A1));
				adder.put(FPPipeline.FPAdderStatus.A1,null);
			}
		}
		
		public String getInstructionStage(long serialNumber)
		{
			Instruction instr;
			if((instr=adder.get(FPPipeline.FPAdderStatus.A1))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "A1";
			if((instr=adder.get(FPPipeline.FPAdderStatus.A2))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "A2";
			if((instr=adder.get(FPPipeline.FPAdderStatus.A3))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "A3";
			if((instr=adder.get(FPPipeline.FPAdderStatus.A4))!=null)
				if(instr.getSerialNumber()==serialNumber)
					return "A4";
			return null;
		
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
		private Instruction getFuncUnit()
		{
			return instr;
		}		
	
		
		public String toString()
		{
			if(instr!=null)
				return "DIVIDER \n "+ instr.getName() +  " " +counter;
			else
				return "DIVIDER \n "+ "EMPTY " + counter;
		}
		
		/** Inserts the passed instruction in the first position of the functional unit
		    if another instruction holds that position a negative number is returned*/
		private int putInstruction(Instruction instr,boolean simulation)
		{
			if(this.instr==null)
			{
				if(!simulation)
				{
					this.instr=instr;
					this.counter=24;
				}
				return 0;
			}
			return -1;
		}

		/** Returns the instruction if counter has reached 1 else
		 *  null is returned*/
		private Instruction getInstruction()
		{
			if(counter==1)
				return this.instr;
			return null;
		}


		/** Call the EX() method of the instruction, if anyone instruction fills the divider this method return 0, if EX is correctly executed (alway in theory) 1 is returned else -1 */
		private int EX() throws HaltException, IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException, DivisionByZeroException, FPInvalidOperationException, FPUnderflowException, FPOverflowException, FPDivideByZeroException, NotAlignException
		{
			if(instr!=null && counter==24)
			{
				instr.EX();
				return 1;
			}
			if(instr==null)
				return 0;
			return -1;
		}
		
				/** Returns true if the in the secondary stage there isn't an instruction and EX on the 
		 * instruction at the first stage can be successfully invoked because it is possible to move the instruction after the execution
		 */
		private boolean isExecutable()
		{
			return true; //in theory is always possible
		}
		
		
		/* Shifts instructions into the functional unit and calls the EX() method for the instruction in the secondary step
		 * this method is called from getInstruction in order to prepare the pipeline for a new instruction entrance	*/
		private void step()
		{
/*			if(this.instr!=null && counter>0)
			{
				//EX() is called if the instruction is just get in in the divider and it is at the first step
				if(counter==24)
				{
					//invocazione EX()
				}
				//the instruction was withdrawn from the getInstruction() and now the divider is empty
				else if(counter==0)
					instr=null;
				counter--;
				
				//any control about the structural hazard is performed because the div.fmt 
				//has got the highest priority in order to exit from the pipeline. When getInstruction()
				//is called, if a div instruction has got 0 as counter then it must go out
			}
 */
			
			
			//if counter has reached 0 the instruction was removed by the previous getInstruction invocation wich called removeLast()
			//if counter is a number between 0 and 24 it must be decremented by 1
			if(this.instr!=null && counter>0 && counter <25)
			{
				counter--;
			}
			//if the divider does not contain instructions anyone operation is carried out
			
		}
		
		
		/** Resets the functional unit*/
		private void reset()
		{
			instr=null;
			counter=0;
		}
/*		
		** Increases the counter and return the new value*
		private int incrCounter()
		{
			counter++;
			return counter;
		}
		
		** Decreases the counter and return the new value*
		private int decrCounter()
		{
			counter--;
			return counter;
		}
*/		
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
		
		public String getInstructionStage(long serialNumber)
		{
			if(instr!=null)
				if(instr.getSerialNumber()==serialNumber)
					if(counter>9)
						return "DIV" + counter;
					else
						return "DIV0" + counter;
			return null;
		}
		
	}
	

	
	
}
