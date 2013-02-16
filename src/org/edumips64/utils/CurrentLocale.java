/* Locale.java
 *
 * This class gives the current Local settings.
 * (c) 2006
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

import java.util.*;
import java.io.*;
import java.net.URL;
public class CurrentLocale{
	
	static HashMap<String,String> en,it;

	//static String currentLocale;
	
	/* Static initializer. Needed because we don't have a constructor */
	static {
		//Config.set("language","en");//"en";
		en = new HashMap<String,String>();
		it = new HashMap<String,String>();
		try{
			loadMessages("en",en);
			loadMessages("it",it);
		}catch(Exception e){
			e.printStackTrace();
		}

		
	}
	public static void loadMessages(String filename,Map<String,String> map)throws FileNotFoundException, IOException{
		String line;
		//BufferedReader in;
		
		URL url = CurrentLocale.class.getResource("MessagesBundle_" + 
				filename + ".properties");
            	InputStreamReader isr = new InputStreamReader (url.openStream ());
            	BufferedReader br = new BufferedReader (isr);
				
		while ((line = br.readLine())!=null){
			String[] l = line.split("=",2);	
			map.put(l[0].trim(),l[1].trim());
		}
		br.close();
		isr.close();
		//System.out.println(map);
		
	}
	
	public static void setLanguage(String language){
		
		Config.set("language",language);
	}
	

	public static String getString(String key){
		return Config.get("language").equals("it") ? 
			it.get(key) 
			: en.get(key);
	}
	public static boolean isSelected(String lan){
		return (Config.get("language").equals(lan));
	}		
}
