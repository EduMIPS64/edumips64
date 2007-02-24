/* GUIHelp.java
 *
 * EduMIPS64 Help
 * (c) 2006 Vanni Rizzo G.
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

package edumips64.ui;


import java.awt.Dimension;
import java.awt.Window;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import javax.help.BadIDException;
import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.JOptionPane;

import edumips64.Main;


/**
 * This class controls the Edumips64 user guide.  
 */
public class GUIHelp {


        public final static String HELP_DEFAULT = "JLogoHelp";
        
       
            
        /**
         * The help broker used to display Edumips's help.
         */
        private static HelpBroker helpBroker;
        private static URL url,HSurl;
        static {
        	//file:C:\Documents and Settings\vanni\workspace\logo\jlogo\help\
        	HSurl = Main.class.getResource("help/");
        	String s = HSurl.getProtocol()+ ":" + HSurl.getPath().replace("%20", " ");
        	//JOptionPane.showInputDialog("Inserisci l'URL: ");///helpSetURL.getText();
            String s1 = "edumips64.hs";//helpSetName.getText();
            //System.err.println("FINALE: " + s);
            try {
            	//Surl = JLogoApplication.class.getResource("help/jlogo.hs");
                //s = JOptionPane.showInputDialog("Va bene così il percorso?",s);
            	URL aurl[] = GUIHelp.parseURLs(s);
            	URLClassLoader urlclassloader = new URLClassLoader(aurl);
            	//System.out.println("URL = " + aurl[0]);
            	url = HelpSet.findHelpSet(urlclassloader, s1);
            
            	
            	HelpSet helpset = new HelpSet(urlclassloader, url);
                
                
                helpBroker = helpset.createHelpBroker();                
                helpBroker.initPresentation();
                helpBroker.setSize(new Dimension(800,600));
            }
            catch(HelpSetException helpsetexception)
            {
                System.err.println("Could not create HelpSet for " + url);
                //new ReportDialog(null,helpsetexception,"Cannot find User Guide");
            }
        }

        private static URL[] parseURLs(String s){
        	Vector<URL> vector = new Vector<URL>();
        	try{
        		URL url = new URL(s);
        		vector.addElement(url);
        	}
        	catch(Exception exception){
        		System.err.println("cannot create URL for " + s);
        	}
        	URL aurl[] = new URL[vector.size()];
        	vector.copyInto(aurl);
        	return aurl;
        }

 
        /**
         * Shows the Edumips64 help window. If the help system was not initialized properly, shows an error dialog
         * instead.
         * 
         * @param parent
         *            the parent that owns this help dialog
         * @param helpId
         *            the help ID to display (an invalid ID will result in the top level help topic being
         *            displayed)
         */
        public static void showHelp(Window parent, String helpId) {
            if (helpBroker != null) {

                try {
                    //helpBroker.setCurrentID(helpId); //da verificare

                    // This is a workaround--the help window freezes when launched from a
                    // modal dialog unless we first set the activation window. However,
                    // if we set the activation window to a modal dialog, we have to
                    // re-initialize the presentation as well! Definitely a result of
                    // various bugs in JavaHelp.
                    ((DefaultHelpBroker) helpBroker).setActivationWindow(parent);
                    helpBroker.initPresentation();
                }
                catch (BadIDException bie) {
                    helpBroker.setCurrentID(HELP_DEFAULT);
                }


                // To maintain the last size of the help window, it
                // is necessary to explicitly get the size and set
                // it again. Note that the CSH class that normally
                // should be used to display the help window doesn't
                // respect the last size of the help window (a bug
                // in JavaHelp?).
                //menuItem.addActionListener(new CSH.DisplayHelpFromSource(helpBroker));
                helpBroker.setSize(helpBroker.getSize());

                helpBroker.setDisplayed(true);
            }


            else {        
                JOptionPane.showMessageDialog(parent,
                                              "Could not locate the Edumips64 User Guide!", //da internazionalizzare
                                              "Help Error", 
                                              JOptionPane.WARNING_MESSAGE);
            }
        }

    }
