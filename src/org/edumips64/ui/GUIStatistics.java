/* GUIStatistics.java
 *
 * This class shows the statistics
 * (c) 2006 Alessandro Nicolosi
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
import org.edumips64.core.*;
import org.edumips64.utils.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

/**
* This class shows the statistics
*/

public class GUIStatistics extends GUIComponent {

	StatPanel statPanel;
	JScrollPane jsp;
	private int nCycles, nInstructions, rawStalls, codeSize, WAWStalls, dividerStalls,memoryStalls;
	private float cpi;
	
	public GUIStatistics () 
	{
		super();
		statPanel = new StatPanel();
		
		jsp=new JScrollPane(statPanel);
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
	}

	class StatPanel extends JPanel {
		JList statList;
		String [] statistics = {" Execution", " 0 Cycles", " 0 Instructions", " ", " Stalls", " 0 RAW Stalls", " 0 WAW Stalls",
		       		       " 0 WAR Stalls", " 0 Structural Stalls(Divider not available)", "0 Structural Stalls (Memory not available)", " 0 Branch Taken Stalls", " 0 Branch Misprediction Stalls",
				       " Code Size", " 0 Bytes", "FPU info", "FCSR","FCSRGroups", "FCSRMnemonics","FCSRValues"};
		public StatPanel () 
		{
			super();
			setLayout(new BorderLayout());
			setBackground(Color.WHITE);
			statList = new JList(statistics);
			statList.setFixedCellWidth(400) ;
			statList.setCellRenderer(new MyListCellRenderer());
			add(statList,BorderLayout.WEST);
		}
	}                         

	public void setContainer (Container co) 
	{
		super.setContainer(co);
		cont.add(jsp);
	}

	public void update ()
	{
		nCycles = cpu.getCycles();
		nInstructions = cpu.getInstructions();
		if (nInstructions >0) {
			cpi = (float)nCycles/(float)nInstructions;
		}
		rawStalls = cpu.getRAWStalls();
		codeSize = (cpu.getMemory().getInstructionsNumber())*4;
		WAWStalls = cpu.getWAWStalls();
		dividerStalls =cpu.getStructuralStallsDivider();
		memoryStalls = cpu.getStructuralStallsMemory();
	}

	public void draw ()
	{
		cont.repaint();
	}

	class MyListCellRenderer implements ListCellRenderer {
			private JLabel label;
      
			public MyListCellRenderer() {
			}
   
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
			{
				statPanel.statList = list;
				label = new JLabel();
				Font f = new Font("Monospaced", Font.PLAIN, 12);
				switch (index) {
					case 0: 
						label.setText(" " + CurrentLocale.getString("EXECUTION"));
						label.setForeground(Color.red);
						label.setFont(f);
						return label;
					case 1: 
						if(nCycles != 1)
							label.setText(" " + nCycles + " " + CurrentLocale.getString("CYCLES"));
						else
							label.setText(" " + nCycles + " " + CurrentLocale.getString("CYCLE"));
						label.setFont(f);
						return label;
					case 2:
						if(nInstructions != 1)
							label.setText(" " + nInstructions + " " + CurrentLocale.getString("INSTRUCTIONS"));
						else
							label.setText(" " + nInstructions + " " + CurrentLocale.getString("INSTRUCTION"));
						label.setFont(f);
						return label;
					case 3:
						if (nInstructions >0) {
							String floatNumber = new Float(cpi).toString();
							if (floatNumber.length()>5)
								floatNumber = floatNumber.substring(0, 5);
							label.setText(" " + floatNumber + " " + CurrentLocale.getString("CPI"));
							label.setFont(f);
							return label;
						}
						else
							label.setText(" ");
						label.setFont(f);
						return label;
					case 4:	
						label.setText(" " + CurrentLocale.getString("STALLS"));
						label.setForeground(Color.red);
						label.setFont(f);
						return label;
					case 5:
						if(rawStalls != 1)
							label.setText(" " + rawStalls + " " + CurrentLocale.getString("RAWS"));
						else
							label.setText(" " + rawStalls + " " + CurrentLocale.getString("RAW"));
						label.setFont(f);
						return label;
					case 6:
						label.setText(" " + WAWStalls + " " + CurrentLocale.getString("WAWS"));
						label.setFont(f);
						return label;
					case 7:
						label.setText(" 0 " + CurrentLocale.getString("WARS"));
						label.setFont(f);
						return label;
					case 8:
						label.setText(" " + dividerStalls + " " + CurrentLocale.getString("STRUCTS_DIVNOTAVAILABLE"));
						label.setFont(f);
						return label;
					case 9:
						label.setText(" " + memoryStalls  + " " + CurrentLocale.getString("STRUCTS_MEMNOTAVAILABLE"));
						label.setFont(f);
						return label;
					case 10:
						label.setText(" 0 " + CurrentLocale.getString("BTS"));
						label.setFont(f);
						return label;
					case 11:
						label.setText(" 0 " + CurrentLocale.getString("BMS"));
						label.setFont(f);
						return label;
					//case 12:
					//	label.setText(" ");
					//	return label;
					case 12:
						label.setText(" " + CurrentLocale.getString("CSIZE"));
						label.setForeground(Color.red);
						label.setFont(f);
						return label;
					case 13:
						label.setText(" " + codeSize + " " + CurrentLocale.getString("BYTES"));
						label.setFont(f);
						return label;
					case 14:	
						label.setText(" " + CurrentLocale.getString("FPUINFO"));
						label.setForeground(Color.red);
						label.setFont(f);
						return label;
					case 15:
						label.setText(" " + CurrentLocale.getString("FPUFCSR"));
						label.setFont(f);
						return label;
					case 16:                    
						label.setText(" "+ "    FCC       Cause EnablFlag RM" );
						label.setFont(f);
						return label;
					case 17:
						label.setText(" " + "7654321 0      VZOUIVZOUIVZOUI" );
						label.setFont(f);
						return label;
					case 18:
						label.setText(" " + cpu.getFCSR());
						label.setFont(f);
						return label;
						
						
				}
				return label;
			}
	}
}
