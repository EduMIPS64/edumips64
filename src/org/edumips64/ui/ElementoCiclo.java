/*ElementoCiclo.java
 *
 * This class represents the single element that is then drawn in the cycles 
 * component.
 * (c) 2006 Filippo Mondello
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
package org.edumips64.ui;

import java.util.*;

/**
* This class represents the single element that is then drawn in the cycles 
* component.
* @author Filippo Mondello
*/
public class ElementoCiclo{
	
	boolean flagCancella;
	int tempoInizio;
	private String nome;
	private LinkedList<String> stato;
	private Long serialNumber; //instruction serial number
	
	/**
	* A new element of this class is created.
	* @param nom the name of the instruction
	* @param tempo the time in which the element is entered in pipeline
	*/
	public ElementoCiclo(String nom,int tempo,long serialNumber){
		stato=new LinkedList<String>();
		nome=nom;
		tempoInizio=tempo;
		stato.add("IF");
		flagCancella=false;
		this.serialNumber=serialNumber;
		
	}
	/**
	* @return the name of the instruction
	*/
	public String getName(){
		return nome;
	}
	
	/**
	* This method is called for every clock cycle.
	* @param stat the current stage in pipeline of the instruction.
	*/
	public void addStato(String stat){
		stato.add(stat);
	}
	
	/**
	* @return the whole list of stages in pipeline 
	*/
	public LinkedList<String> getStato(){
		return stato;
	}
	
	/**
	* @return the initial time in which the instruction occuped the IF stage in pipeline.
	*/
	public int getTime(){
		return tempoInizio;
	}
	
	public void setFlagCancella(boolean f){
		flagCancella=f;
	}
	
	/**
	* @return the value of the flag that indicate if an instruction has been aborted.
	*/
	public boolean getFlagCancella(){
		return flagCancella;
	}
	
	/** Returns the serial number of the referred instruction*/
	public long getSerialNumber()
	{
		return serialNumber;
	}
	
}
