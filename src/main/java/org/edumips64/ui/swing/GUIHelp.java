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

package org.edumips64.ui.swing;

import java.awt.Dimension;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import javax.help.BadIDException;
import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

/**
 * This class controls the Edumips64 user guide.
 */
public class GUIHelp {
  private final static String HELPSET = "EduMIPS64.hs";

  private static URL[] parseURLs(String s) throws MalformedURLException {
    Vector<URL> vector = new Vector<>();
    URL url = new URL(s);
    vector.addElement(url);
    URL aurl[] = new URL[vector.size()];
    vector.copyInto(aurl);
    return aurl;
  }

  /**
   * Shows the EduMIPS64 help window.
   *
   * @param parent the window that owns this help dialog
   * @param helpSetUrl the URL to the directory of the help set.
   */
  public static void showHelp(Window parent, URL helpSetUrl) throws HelpSetException, BadIDException, MalformedURLException {
    // Clean up the URL from spaces.
    String cleanUrl = helpSetUrl.getProtocol() + ":" + helpSetUrl.getPath().replace("%20", " ");

    URL aurl[] = GUIHelp.parseURLs(cleanUrl);
    URLClassLoader urlclassloader = new URLClassLoader(aurl);
    URL url = HelpSet.findHelpSet(urlclassloader, HELPSET);

    HelpSet helpset = new HelpSet(urlclassloader, url);
    HelpBroker helpBroker = helpset.createHelpBroker();
    helpBroker.initPresentation();
    helpBroker.setSize(new Dimension(800, 600));
    ((DefaultHelpBroker) helpBroker).setActivationWindow(parent);
    helpBroker.initPresentation();
    helpBroker.setSize(helpBroker.getSize());
    helpBroker.setDisplayed(true);
  }
}
