/* Config.java
 *
 * This class manages the user settings.
 * (c) 2006 EduMIPS64 project - Rizzo Vanni G.
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
import java.io.*;
import java.util.*;
import java.awt.Color;
import java.util.jar.*;
/** This class manage the edumips64.config file
 * used for saving user preferences (like language, etc)
 */
public class Config{
	static Map<String,Object> config; 
	//Static constructor
	static{
		config = new HashMap<String, Object>();
		try{
	
			openConfigFile();
	
		}catch(ConfigFileNotFoundException e) {
			config.put("version",Main.VERSION);
			config.put("language","en");
			
			config.put("IFColor",Color.yellow);
			config.put("IDColor",new Color(0,120,240));
			config.put("EXColor",Color.red);
			config.put("MEMColor",Color.green);
			config.put("WBColor",Color.magenta.darker());
			config.put("RAWColor",Color.blue.brighter());
			config.put("SAMEIFColor",new Color(150,150,180));
			
			config.put("dineroIV","dineroIV" );
			config.put("files",new LinkedList<String>());
			config.put("lastdir", new File(System.getProperty("user.dir")));
			config.put("forwarding",false);
			config.put("warnings",false);
			config.put("verbose",true);
			config.put("syncexc-masked",false);
			config.put("syncexc-terminate",false);
			config.put("n_step",4);
			config.put("sleep_interval",10);
                        config.put("show_aliases",false); 
		}
	}
	public static void setMap(Map<String,Object> newConfig){
		config = newConfig;
	}
	public static Map<String,Object> getMap(){
		return config;
	}
	/**Get the Object corresponding to a key in the config HashMap
	 * @param key The name of the setting
	 * @return Object (like Color, String, Boolean...)
	 * example: calling Config.get("language") returns it or en
	 */
	public static Object get(String key){
		return config.get(key);
	}

	/**Set a value in the config HashMap
	 * @param key Key name (String)
	 * @param obj assign this Object to key
	 * @return boolean: true if key exists in HashMap, else false
	 */
	public static boolean set(String key, Object obj){
		return (config.put(key,obj) != null)? false : true;
	}


	/**Method used to open the config file
	 * and insert its serialized content in a HasMap.
	 */
	public static void openConfigFile() throws ConfigFileNotFoundException{
		InputStream configfile;
		//we are in windowz
		if((System.getProperty("os.name").substring(0,3).equalsIgnoreCase("win"))){
			try{
				String path =	System.getProperty("user.home") + 
						System.getProperty("file.separator") +
						"edumips64.config";
				configfile = new FileInputStream(new File(path));


			
			}catch(Exception e){	
				throw new ConfigFileNotFoundException();
			}
		}
		else{ //linux
			configfile = Main.class.getResourceAsStream("edumips64.config");
		}
		
		try{
			
			ObjectInputStream in = new ObjectInputStream(configfile);
			//HashMap<String,Object> 
			@SuppressWarnings("unchecked")	
			Map<String,Object> tmp = (Map)in.readObject();
			if(tmp instanceof HashMap){//<String,Object>){
				config = (HashMap<String,Object>)tmp;

				//Opening File:configfile, and loading the HashMap: config
				in.close();
				if(!(config.get("version").equals(Main.VERSION)))
					throw new ConfigFileNotFoundException();
			}else{
				//file does not contain a HashMap
				throw new ConfigFileNotFoundException();
			}

		}catch(Exception e){
			
			throw new ConfigFileNotFoundException();
		}	
	}
	/** This mehod is called when edumips exit (from menu)
	 * Creates a temp dir and a temp config file,
	 * inserts in the jar file
	 * and removes temp file and dir. 
	 */
	public static void saveConfigFile(){
		try{
			//in windows will be rewritten the file in the user dir
			if((System.getProperty("os.name").substring(0,3).equalsIgnoreCase("win"))){
				String path = System.getProperty("user.home") + System.getProperty("file.separator") + "edumips64.config";
				ObjectOutputStream out = new ObjectOutputStream (new FileOutputStream(path));

				out.writeObject(config);

				out.close();

			}
			//in linux create a tmp directory an put there the config file
			else{
				String path = System.getProperty("java.io.tmpdir") + 
						System.getProperty("file.separator") +
						"_tmp_edumips64";
				File tmpdir = new File(path);
				boolean res = tmpdir.mkdir();

				path +=	System.getProperty("file.separator") + "edumips64";
				File dir = new File(path);
				
				res = dir.mkdir();
				
				path +=	System.getProperty("file.separator") + "edumips64.config";
				
				File tmpfile = new File(path);
				
				
				try {
					ObjectOutputStream out = new ObjectOutputStream (new FileOutputStream(path));
					out.writeObject(config);
					out.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}


				//if(Main.class.getResource("edumips64.config").toString().split(":",2)[0].equals("jar"))


				String jarpath = Main.class.getResource("Main.class").toString().split("file:")[1].split("!")[0];
				String filepath =  "edumips64" + System.getProperty("file.separator") + "edumips64.config";
				String temppath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "_tmp_edumips64";

				// Replacing %20 with a space
				jarpath = jarpath.replaceAll("%20", " ");
				filepath = filepath.replaceAll("%20", " ");
				temppath = temppath.replaceAll("%20", " ");

				ProcessBuilder pb = new ProcessBuilder("jar","uf",jarpath,"-C",temppath,filepath);
				//Executing "jar uf jarname jarfile " command to update the config file
				Process process = pb.start();

				//Print errors on System.out
				BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				process.waitFor();
				String s;
				while (( s = stdErr.readLine()) != null) {
					System.out.println("Error: " + s);
				}


				tmpfile.delete();
				dir.delete();
				tmpdir.delete();
			}		
		}catch(Exception e){
			System.out.println(e);
		}
	}
}
