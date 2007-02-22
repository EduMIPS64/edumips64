/**
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
//utente: edumips.org58154
//password: edubugreport
//alias: bugs@edumips.org
package edumips64.ui;

import edumips64.core.*;
import edumips64.utils.*;

import java.util.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.font.*;
import java.text.*;

/**
 * This class provides a window for configuration options.
*/
public class ReportDialog extends JDialog{


	JButton okButton;
	int width = 450, height = 400;	
	public ReportDialog(final JFrame owner,Exception exception,String title){
	
		super(owner, title, true);
		
		JPanel buttonPanel = new JPanel();
		
			JButton okButton = new JButton(CurrentLocale.getString("ReportDialog.BUTTON"));
			buttonPanel.add(okButton);

			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
			buttonPanel.add(okButton);
		
		//Title's Icon and Text
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BorderLayout());
		String msg = CurrentLocale.getString("ReportDialog.MSG");
		JTextArea textArea = new JTextArea(msg);
		textArea.setFont(new Font("Verdana",0,20));
		textArea.setForeground(new Color(0,0,85));
		try{
			JLabel label = new JLabel(new ImageIcon(edumips64.img.IMGLoader.getImage("fatal.png")),SwingConstants.LEFT);	
			titlePanel.add("West",label);
		}catch(java.io.IOException e){}
		titlePanel.add("Center",textArea);
		//label style in TextArea
			textArea.setLineWrap (true);
			textArea.setWrapStyleWord (true);
			textArea.setEditable (false);
			textArea.setBackground ((Color)UIManager.get ("Label.background"));
			textArea.setForeground ((Color)UIManager.get ("Label.foreground"));
			textArea.setBorder (null);
		
		//fill the Text Area whit Exception informations
		String exmsg = new String();
		exception.fillInStackTrace();	
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			exmsg = "------\r\n" + sw.toString() + "------\r\n";
		}
		catch(Exception exc) {
			exmsg = "fatal error";
		} 
		
			
		JTextArea ta = new JTextArea(exmsg);

		JScrollPane scrollTable = new JScrollPane(ta);


		getRootPane().setDefaultButton(okButton);
		getContentPane().setLayout(new BorderLayout());

		getContentPane().add("North", titlePanel);
		getContentPane().add("Center", scrollTable);
		getContentPane().add("South", buttonPanel);
		
		setSize(width,height);
		setLocation((getScreenWidth() - getWidth()) / 2, (getScreenHeight() - getHeight()) / 2);
		setVisible(true);	

	}
	
	public static int getScreenWidth() {
		return (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	}

	public static int getScreenHeight() {
		return (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	}

}

