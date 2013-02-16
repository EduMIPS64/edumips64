/* FPUConfigurator.java
 *
 * This class gives the FPU current Local settings.
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

import edumips64.Main;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.util.logging.Logger;
public class FPUConfigurator{
	static LinkedList<String> fparithmetic,terminating;
    private static final Logger logger = Logger.getLogger(FPUConfigurator.class.getName());

	public FPUConfigurator() {
		fparithmetic = new LinkedList<String>();
		terminating = new LinkedList<String>();
		try{
			//the pattern of the line to parse is    <tag>any character(at least one)</tag> any character (zero or more) 
			loadData("I","<I>.+</I>.*",fparithmetic);
			loadData("O","<O>.+</O>.*",terminating);
		} catch(ConfigFileNotFoundException e) {
            logger.info("Configuration file not found, using defaults.");
            fparithmetic.add("ADD.D");
            fparithmetic.add("SUB.D");
            fparithmetic.add("DIV.D");
            fparithmetic.add("MUL.D");
            terminating.add("0000000C");
            terminating.add("04000000");
		}
	}
	
	public static void loadData(String tag, String regex, LinkedList<String> data) throws ConfigFileNotFoundException{
		String line;
		String splitted[];
		InputStream configfile=null;
		InputStreamReader isr=null;
		
		try {
			URL url = FPUConfigurator.class.getResource("fpu.properties");
			isr = new InputStreamReader (url.openStream ());
			BufferedReader br = new BufferedReader (isr);
					
			//we add into the data list tagged values (es.)  <tag>valueToAddIntoData</tag>
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
		} catch (IOException ex) {
			throw new ConfigFileNotFoundException();
		} catch (NullPointerException ex) {
			throw new ConfigFileNotFoundException();
		}
	}

	public LinkedList<String> getFPArithmeticInstructions(){
		return fparithmetic;
	}

	public LinkedList<String> getTerminatingInstructions(){
		return terminating;
	}
}
