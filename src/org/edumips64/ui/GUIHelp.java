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

package org.edumips64.ui;


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

import org.edumips64.Main;
import org.edumips64.utils.CurrentLocale;


/**
 * This class controls the Edumips64 user guide.
 */
public class GUIHelp {
  public final static String HELP_DEFAULT = "EduMIPS64Help";

  /**
   * The help broker used to display Edumips's help.
   */
  private static HelpBroker helpBroker;
  private static URL url, HSurl;

  private static URL[] parseURLs(String s) {
    Vector<URL> vector = new Vector<URL>();

    try {
      URL url = new URL(s);
      vector.addElement(url);
    } catch (Exception exception) {
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
    HSurl = Main.class.getResource(CurrentLocale.getString("HELPDIR") + "/");
    String s = HSurl.getProtocol() + ":" + HSurl.getPath().replace("%20", " ");
    String s1 = CurrentLocale.getString("HELPSET");

    try {
      URL aurl[] = GUIHelp.parseURLs(s);
      URLClassLoader urlclassloader = new URLClassLoader(aurl);
      url = HelpSet.findHelpSet(urlclassloader, s1);

      HelpSet helpset = new HelpSet(urlclassloader, url);
      helpBroker = helpset.createHelpBroker();
      helpBroker.initPresentation();
      helpBroker.setSize(new Dimension(800, 600));
      ((DefaultHelpBroker) helpBroker).setActivationWindow(parent);
      helpBroker.initPresentation();
      helpBroker.setSize(helpBroker.getSize());
      helpBroker.setDisplayed(true);
    } catch (HelpSetException helpsetexception) {
      System.err.println("Could not create HelpSet for " + url);
      System.err.println(helpsetexception);
    } catch (BadIDException bie) {
      helpBroker.setCurrentID(HELP_DEFAULT);
    }
  }
}
