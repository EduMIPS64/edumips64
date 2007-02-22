/* GUIManual.java
 *
 * This class draw the EduMIPS64 Manual frame.
 * (c) 2006 Filippo Mondello
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

import edumips64.utils.CurrentLocale;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
* This class draw the EduMIPS64 Manual frame.
*/
public class GUIManual extends JDialog {
	
	JTextArea jta1,jta2,jta3;
	Bottone bott;
	
	/** Builds a new instance of the Manual window.
	 *  @param intro the absolute pathname of the introduction-related manual 
	 *  chapter
	 *  @param instr the absolute pathname of the instruction-related manual 
	 *  chapter
	 *  @param GUI the absolute pathname of the GUI-related manual chapter
	 */
	public GUIManual(Frame owner, String intro, String instr, String GUI){
		super(owner);
		setTitle("EduMIPS64 - " + CurrentLocale.getString("Manual.CAPTION"));
		setModal(true);
		setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(460, 50));
		tabbedPane.setBorder(BorderFactory.createLoweredBevelBorder());

		jta1=new JTextArea();
		jta1.setEnabled(true);
		jta1.setEditable(false);
		JScrollPane jsp1=new JScrollPane(jta1);
		tabbedPane.addTab(CurrentLocale.getString("Manual.INTRO"), jsp1);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		jta2=new JTextArea();
		jta2.setEnabled(true);
		jta2.setEditable(false);
		JScrollPane jsp2=new JScrollPane(jta2);
		tabbedPane.addTab(CurrentLocale.getString("Manual.GUI"), jsp2);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		jta3=new JTextArea();
		jta3.setEnabled(true);
		jta3.setEditable(false);
		JScrollPane jsp3=new JScrollPane(jta3);
		tabbedPane.addTab(CurrentLocale.getString("Manual.IS"), jsp3);
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
		Font f = new Font("Monospaced", Font.PLAIN, 12);

		jta1.setFont(f);
		jta2.setFont(f);
		jta3.setFont(f);
		
		Intestazione in=new Intestazione();
		in.setPreferredSize(new Dimension(400,100));
		setMinimumSize(new Dimension(400,350));
		bott=new Bottone();
		bott.setFrame(this);
		
		JPanel pannelloInt=new JPanel();
		getContentPane().add(pannelloInt,BorderLayout.NORTH);
		pannelloInt.setLayout(new BorderLayout());
		pannelloInt.add(in,BorderLayout.CENTER);
		
		getContentPane().add(bott,BorderLayout.SOUTH);
		
		JPanel pannnn=new JPanel();
		getContentPane().add(pannnn,BorderLayout.CENTER);
		
		pannnn.setLayout(new BorderLayout());
		getContentPane().add(new JPanel(),BorderLayout.EAST);
		pannnn.setBorder(BorderFactory.createRaisedBevelBorder());
		pannnn.add(tabbedPane,BorderLayout.CENTER);
		pannnn.add(new JPanel(),BorderLayout.NORTH);
		pannnn.add(new JPanel(),BorderLayout.SOUTH);
		pannnn.add(new JPanel(),BorderLayout.EAST);
		pannnn.add(new JPanel(),BorderLayout.WEST);
		getContentPane().add(new JPanel(),BorderLayout.WEST);
		setBounds(10,10,700,600);
		//setResizable(false);
		setIntroduzione(intro);
		setGUI(GUI);
		setIstruzioni(instr);
        //Uncomment the following line to use scrolling tabs.
        //tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	
	private void setTextAreaFile(String filename, JTextArea jta) {
		BufferedReader r = new BufferedReader(new InputStreamReader(edumips64.Main.class.getResourceAsStream(filename)));
		String temp;
		try{
			while((temp = r.readLine()) != null)
				jta.append("  " + temp + "\n");

		} catch(Exception e) {
			e.printStackTrace();
		}

		jta.moveCaretPosition(0);
	}
	
	/**
	* Sets the file to be shown in the tabbed pane dedicated to the introduction 
	* manual page.
	* @param nomeFile the file name where the introduction manual page is stored
	*/
	public void setIntroduzione(String nomeFile){
		setTextAreaFile(nomeFile, jta1);
	}
	
	/**
	* Sets the file to be shown in the tabbed pane dedicated to the GUI manual 
	* page.
	* @param nomeFile the file name where the introduction manual page is stored
	*/
	public void setGUI(String nomeFile){
		setTextAreaFile(nomeFile, jta2);
	}
	
	/**
	* Sets the file to be shown in the tabbed pane dedicated to the Instructions 
	* manual page.
	* @param nomeFile the file name where the introduction manual page is stored
	*/
	public void setIstruzioni(String nomeFile){
		setTextAreaFile(nomeFile, jta3);
	}
	
	
	class Intestazione extends JPanel{
		public void paintComponent(Graphics g){
			Font f1 = new Font("Times New Roman", Font.PLAIN, 50); 
			g.setFont(f1);
			g.setColor(Color.black);
			g.drawString("EduMIPS64",20,50);
			
			f1 = new Font("Times New Roman", Font.PLAIN, 20); 
			//JLabel jl2=new JLabel("v.0.1");
			g.setFont(f1);
			g.drawString("v." + edumips64.Main.VERSION ,290,50);
			
			f1 = new Font("Times New Roman", Font.ITALIC, 20); 
			//JLabel jl3=new JLabel("  UserGuide");
			g.setFont(f1);
			g.drawString(edumips64.utils.CurrentLocale.getString("Manual.CAPTION"),20,80);
		}
	}
	
	class Bottone extends JPanel  implements ActionListener{
		
		Component frame;
		
		public Bottone(){
			super(new BorderLayout());
			JPanel pa=new JPanel();
			JPanel pa2=new JPanel();
			add(pa,BorderLayout.NORTH);
			add(pa2,BorderLayout.SOUTH);
			JButton esci=new JButton("Esci");
			esci.addActionListener(this);
			add(esci,BorderLayout.EAST);
		}
		
		public void setFrame(Component fr){
			frame=fr;
		}
		
		
		public void actionPerformed(ActionEvent e){
			frame.setVisible(false);
		}
	}
}
