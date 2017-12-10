/*
 * ReportDialog.java
 *
 * This class provides a window for report no-catched exception in EduMips64 code.
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

import org.edumips64.ui.swing.img.IMGLoader;
import org.edumips64.utils.CurrentLocale;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class provides a window for configuration options.
*/
public class ReportDialog extends JDialog {

  public ReportDialog(final JFrame owner, Exception exception, String title, String version) {
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
    String msg = CurrentLocale.getString("ReportDialog.MSG");
    JTextArea textArea = new JTextArea(msg);
    textArea.setFont(new Font("Verdana", Font.PLAIN, 20));
    textArea.setForeground(new Color(0, 0, 85));

    try {
      JLabel label = new JLabel(new ImageIcon(IMGLoader.getImage("fatal.png")), SwingConstants.LEFT);
      titlePanel.add("West", label);
    } catch (java.io.IOException ignored) {}

    titlePanel.add("Center", textArea);
    //label style in TextArea
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);
    textArea.setBackground((Color) UIManager.get("Label.background"));
    textArea.setForeground((Color) UIManager.get("Label.foreground"));
    textArea.setBorder(null);

    // Fill the Text Area with Exception information
    String exmsg;

    try {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      exmsg = "------\r\n" + sw.toString() + "------\r\n";
    } catch (Exception exc) {
      exmsg = "fatal error";
    }

    exmsg += "Version " + version + "\r\n";

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
    setVisible(true);
  }
}
