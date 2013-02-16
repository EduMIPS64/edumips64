/* DineroFrontend.java
 *
 * Graphical frontend for DineroIV
 * (c) 2006 Andrea Spadaccini
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

import org.edumips64.utils.Config;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

/** Graphical frontend for DineroIV
 *  @author Andrea Spadaccini
 */

public class DineroFrontend extends JDialog {
	// Attributes are static in order to make them accessible from
	// the nested anonymous classes. They can be static, because at most
	// one instance of DineroFrame will be created in EduMIPS64
	private static JLabel pathLabel, paramsLabel;
	private static JTextField path, params;
	private static JButton browse, execute;
	private static JTextArea result;
	private static Container cp;
	private class ReadStdOut extends Thread
	{
	    public boolean finish = false;
	    private BufferedReader stdOut; 
	    private BufferedReader stdErr;
	    public ReadStdOut (	BufferedReader stdOut, BufferedReader stdErr, JTextArea result) 
	    {
		this.stdOut = stdOut;
		this.stdErr = stdErr;
	    }
	    public void run()
		{
			boolean found = false;
			String s;

			try
			{
				while (!finish)
				{
					if(stdOut.ready())
						while ((s = stdOut.readLine()) != null) {
							if(s.equals("---Simulation complete."))
								found = true;
							if(found)
								result.append(s + "\n");
						} 
					if(stdErr.ready())
						while ((s = stdErr.readLine()) != null) 
						{
							result.append(">> Dinero error: " + s + "\n");
						}
				}
			}
			catch (java.io.IOException ioe) 
			{
				result.append(">> ERROR: " + ioe);
			}
    	}
	
	}

	public DineroFrontend(Frame owner) {
		super(owner);
		setTitle("Dinero frontend");
		cp = rootPane.getContentPane();
		cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

		Dimension hSpace = new Dimension(5, 0);
		Dimension vSpace = new Dimension(0, 5);

		pathLabel = new JLabel("DineroIV executable path:");
		paramsLabel = new JLabel("Command line parameters:");

		path = new JTextField((String)Config.get("dineroIV"));
		params = new JTextField("-l1-usize 512 -l1-ubsize 64");

		path.setPreferredSize(new Dimension(400, 26));
		path.setMaximumSize(new Dimension(1000, 26));
		path.setMinimumSize(new Dimension(50, 25));

		params.setPreferredSize(new Dimension(400, 26));
		params.setMaximumSize(new Dimension(1000, 26));
		params.setMinimumSize(new Dimension(50, 26));

		params.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					execute.doClick();
				}
			}
		});

		browse = new JButton("Browse...");
		browse.setAlignmentX(Component.RIGHT_ALIGNMENT);
		execute = new JButton("Execute");
		execute.setAlignmentX(Component.CENTER_ALIGNMENT);

		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
			JFileChooser jfc = new JFileChooser();
				int val = jfc.showOpenDialog(null);
				if(val == JFileChooser.APPROVE_OPTION){
					Config.set("dineroIV",jfc.getSelectedFile().getPath());
					path.setText(jfc.getSelectedFile().getPath());
				}
			}
		});
		
		execute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Process representing Dinero
					String dineroPath = path.getText();
					String paramString = params.getText();

					// Cleaning the JTextArea
					result.setText("");
					result.append(">> Dinero path: " + dineroPath + "\n");
					result.append(">> Dinero parameters: " + paramString + "\n");

					Process dinero = Runtime.getRuntime().exec(dineroPath + " " + paramString);
					result.append(">> Simulation results:\n");
					// Readers associated with Dinero output streams
					BufferedReader stdErr = new BufferedReader(new InputStreamReader(dinero.getErrorStream()));
					BufferedReader stdOut = new BufferedReader(new InputStreamReader(dinero.getInputStream()));
				        ReadStdOut th = null;
					if(org.edumips64.Main.isWindows())
					{
					    th = new ReadStdOut(stdOut,stdErr,result);
					    th.start();
					}

					// Writer associated with Dinero input streams
					PrintWriter dineroIn = new PrintWriter(dinero.getOutputStream());

					String s = new String();

					// Let's send the tracefile to Dinero
					org.edumips64.core.Dinero.getInstance().writeTraceData(dineroIn);
					dineroIn.flush();
					dineroIn.close();

					try {
						// Well, wait for Dinero to terminate
						dinero.waitFor();
					}
					catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					if(org.edumips64.Main.isWindows())
					    th.finish = true;	
					else
					{
					    boolean found = false;

					    // Let's get the results
					    if(stdOut.ready())
						while ((s = stdOut.readLine()) != null) {
						    if(s.equals("---Simulation complete."))
							found = true;
						    if(found)
							result.append(s + "\n");
						} 
					    if(stdErr.ready())
						while ((s = stdErr.readLine()) != null) {
						    result.append(">> Dinero error: " + s + "\n");
						}
					}

				}
				catch (java.io.IOException ioe) {
					result.append(">> ERROR: " + ioe);
				}
			}
		});

		Box dineroEx = Box.createHorizontalBox();
		dineroEx.add(Box.createHorizontalGlue());
		dineroEx.add(pathLabel);
		dineroEx.add(Box.createRigidArea(hSpace));
		dineroEx.add(path);
		dineroEx.add(Box.createRigidArea(hSpace));
		dineroEx.add(browse);
		cp.add(dineroEx);
		
		cp.add(Box.createRigidArea(vSpace));

		Box cmdLine = Box.createHorizontalBox();
		cmdLine.add(Box.createHorizontalGlue());
		cmdLine.add(paramsLabel);
		cmdLine.add(Box.createRigidArea(hSpace));
		cmdLine.add(params);
		cmdLine.add(Box.createRigidArea(hSpace));
		cp.add(cmdLine);
		cp.add(Box.createRigidArea(vSpace));

		result = new JTextArea();
		result.setBorder(BorderFactory.createTitledBorder("Messages"));
		result.setEditable(false);
		result.setFont(new Font("Monospaced", Font.PLAIN, 12));

		cp.add(execute);
		cp.add(Box.createRigidArea(vSpace));
		cp.add(new JScrollPane(result));

		setSize(850, 500);
	}
	public static void main(String[] args) {
		DineroCacheOptions dco = new DineroCacheOptions('u', 1);
		dco.size = "256k";
		dco.bsize = "256";

		System.out.println(dco);

		JDialog f = new DineroFrontend(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

}


/** Panel with all the necessary controls to modify the options of a Cache.
 */
class DineroSingleCachePanel extends JPanel {
	private DineroCacheOptions dco;
	private JComboBox size, sizeUnit, bsize, bsizeUnit;
	private JTextField assoc;
	private JCheckBox ccc;
	public DineroSingleCachePanel(char type, int level) {
		dco = new DineroCacheOptions(type, level);

		String[] sizes = {"1", "2", "4", "8", "16", "32", "64", "128", "256", "512"};
		String[] units = {" ", "k", "M", "G"};

		size = new JComboBox(sizes);
		bsize = new JComboBox(sizes);

		sizeUnit = new JComboBox(units);
		bsizeUnit = new JComboBox(units);

		assoc = new JTextField();
		ccc = new JCheckBox();
		ccc.setEnabled(false);

		//setBorder(BorderFactory.createTitledBorder("Level " + level + " cache 
		//(" + type + ")"));
		//setLayout(new GridLayout(1, 3));
	}
}

/** Class holding the config options for a Cache.
 *  Its attributes are public because this class has package visibility, and so 
 *  it's used only by the DineroFrontend and the DineroCachePanel classes.
 */
class DineroCacheOptions {
	public String size, bsize;
	public int assoc = 0;
	public boolean ccc = false;
	
	private char type;
	private int level;
	
	public DineroCacheOptions(char type, int level) {
		this.type = type;
		this.level = level;
	}
	
	public String toString() {
		String prefix = "-l" + level + "-" + type;
		String cmdline = prefix + "size" + " " + size + " ";
		cmdline += prefix + "bsize" + " " + bsize + " ";

		if(assoc > 0)
			cmdline += prefix + "assoc" + " " + assoc + " ";
		if(ccc)
			cmdline += prefix + "ccc" + " ";

		return cmdline;
	}
}

