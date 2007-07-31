/* FPUConfigurator.java
 *
 * This class gives the current Local settings.
 * (c)Massimo Trubia 2007
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
package edumips64.utils;

import java.util.*;
import java.io.*;
import java.net.URL;
public class FPUConfigurator{
	static LinkedList<String> fparithmetic,terminating;
	public FPUConfigurator(){
		fparithmetic = new LinkedList<String>();
		terminating = new LinkedList<String>();
		try{
			//the pattern to parse is   <tag>any character(at least one)</tag> any character (zero or more) 
			loadData("I","<I>.+</I>.*",fparithmetic);
			loadData("O","<O>.+</O>.*",terminating);
		}catch(Exception e){
			e.printStackTrace();
		}

		
	}
	
	public static void loadData(String tag,String regex, LinkedList<String> data)throws FileNotFoundException, IOException{
		String line;
		String splitted[];
		//BufferedReader in;
		
		URL url = FPUConfigurator.class.getResource("FPU.properties");
            	InputStreamReader isr = new InputStreamReader (url.openStream ());
            	BufferedReader br = new BufferedReader (isr);
				
		//we add into the data list tagged valued (es.)  <tag>valueToAddIntoData</tag>
		while ((line = br.readLine())!=null){
			if(line.matches(regex)){
				splitted=line.split("<"+tag+">");
				splitted=splitted[1].split("</" + tag + ">");
				splitted[0]=splitted[0].trim();
				data.add(splitted[0]);
			}
		}
		br.close();
		isr.close();
	}

	public LinkedList<String> getFPArithmeticInstructions(){
		return fparithmetic;
	}

	public LinkedList<String> getTerminatingInstructions(){
		return terminating;
	}
}
