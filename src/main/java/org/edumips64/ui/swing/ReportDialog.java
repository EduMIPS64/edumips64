/*
 * ReportDialog.java
 *
 * This class provides a dialog box which helps the user report non-catched exceptions.
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
package org.edumips64.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.edumips64.ui.swing.img.IMGLoader;
import org.edumips64.utils.CurrentLocale;

/**
 * This class provides a dialog box which helps the user report non-catched exceptions.
*/
public class ReportDialog extends JDialog implements HyperlinkListener {

  public ReportDialog(final JFrame owner, Exception exception, String title, String version, 
		  String buildDate, String gitRevision, String code) {
    super(owner, title, true);

    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton(CurrentLocale.getString("ReportDialog.BUTTON"));
    buttonPanel.add(okButton);

    okButton.addActionListener(e -> {
      setVisible(false);
      dispose();
    });
    buttonPanel.add(okButton);

    //Title's Icon and Text
    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new BorderLayout());

    try {
      JLabel label = new JLabel(new ImageIcon(IMGLoader.getImage("fatal.png")), SwingConstants.LEFT);
      titlePanel.add("West", label);
    } catch (java.io.IOException ignored) {}

    String msg = CurrentLocale.getString("ReportDialog.MSG");
    JEditorPane jeditPane = new JEditorPane();
    jeditPane.setContentType("text/html");
    jeditPane.setText(msg);
    jeditPane.addHyperlinkListener(this);
    jeditPane.setEditable(false);
    jeditPane.setBackground((Color) UIManager.get("Label.background"));
    jeditPane.setForeground((Color) UIManager.get("Label.foreground"));

    titlePanel.add("Center", jeditPane);

    // Fill the Text Area with Exception information
    String exmsg;

    try {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      exmsg = "```java\n" + sw.toString() + "```\n";
    } catch (Exception exc) {
      exmsg = "fatal error";
    }

    exmsg += "## System information\n";
    exmsg += String.format(" * Version: %s\n", version);
    exmsg += String.format(" * Build date: %s\n" , buildDate);
    exmsg += String.format(" * Git revision: %s\n" , gitRevision);
    exmsg += String.format(" * JRE version: %s\n", System.getProperty("java.version"));
    exmsg += String.format(" * OS: %s\n\n", System.getProperty("os.name"));
    exmsg += "## Code of the assembly file in execution\n";
    exmsg += code == null ? "<none>" : 
    	String.format("<details><summary>Click to expand</summary>\n\n```assembly\n%s\n```\n</details>", code);

    JTextArea ta = new JTextArea(exmsg);

    JScrollPane scrollTable = new JScrollPane(ta);

    getRootPane().setDefaultButton(okButton);
    getContentPane().setLayout(new BorderLayout());

    getContentPane().add("North", titlePanel);
    getContentPane().add("Center", scrollTable);
    getContentPane().add("South", buttonPanel);

    int height = 400;
    int width = 450;
    setSize(width, height);
    setLocationRelativeTo(owner);
    setVisible(true);
  }

  public void hyperlinkUpdate(HyperlinkEvent hle) {
    if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
      Desktop desktop = Desktop.getDesktop();
      try {
        desktop.browse(hle.getURL().toURI());
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
