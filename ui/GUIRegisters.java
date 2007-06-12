/* GUIRegisters.java
 *
 * This class shows the values stored in the registers.
 * (c) 2006 Carmelo Mirko Musumeci
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

import edumips64.core.*;
import edumips64.utils.*;
import java.util.*;
import java.io.*;
import java.util.Arrays;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
* This class shows the values stored in the registers.
*/

public class GUIRegisters extends GUIComponent 
{ 	
    Register registers[];
	RegPanel regPanel;
	JTextArea text;
	String oldValue;
	String value[] = new String[34];
	int rowCurrent;
	String valueCurrent[];
	private enum AliasRegister 
		{zero,at,v0,v1,a0,a1,a2,a3,t0,t1,t2,t3,t4,t5,t6,t7,s0,s1,s2,s3,s4,s5,s6,s7,t8,t9,k0,k1,gp,sp,fp,ra};
	
	public GUIRegisters()
	{
		super();
		registers = cpu.getRegisters();
		regPanel = new RegPanel();
	}

	public void setContainer (Container co) 
	{
		super.setContainer(co);
		cont.add(regPanel);
	}
	
	public void update() 
	{
		registers = cpu.getRegisters();
		for (int i = 0; i < 32; i++) {
			value[i] = registers[i].toString();
		}
		value[32]=cpu.getLO().toString();
		value[33]=cpu.getHI().toString();
	}
	
	public void draw() 
	{
		cont.repaint();
	}

  public String registerToAlias(String reg)
		{
			int number = Integer.parseInt(reg.substring(1));
			if (number == 32) return ("   LO    =  ");
			if (number == 33) return ("   HI    =  ");
			
			for(AliasRegister x : AliasRegister.values())
			{
				if(number==(x.ordinal()))
					return ("$" + x.name());
			}
			return "";
		}


	class RegPanel extends JPanel 
	{
		JTable theTable;
		JScrollPane scrollTable;
		public FileTableModel tableModel;
		String numR[] = new String[34];
		String numRF[] = new String[34];

		int cont = 0; //contatore che conta le cifre significative dei valori di input
		
		public RegPanel () 
		{
			super();
			setLayout(new BorderLayout());
			tableModel = new FileTableModel(value);
			setBackground(Color.WHITE);
			theTable = new JTable(tableModel);
			theTable.getColumnModel().getColumn(0).setPreferredWidth(95);
			theTable.getColumnModel().getColumn(1).setPreferredWidth(122);
			theTable.getColumnModel().getColumn(2).setPreferredWidth(45);
			theTable.getColumnModel().getColumn(3).setPreferredWidth(122);
			theTable.setRowSelectionAllowed(false);
			theTable.setColumnSelectionAllowed(false);
			theTable.setCellSelectionEnabled(false);
			theTable.setTableHeader(null);
			theTable.setShowGrid(false);
			Font f = new Font("Monospaced", Font.PLAIN, 12);
			theTable.setFont(f);
			theTable.setAutoResizeMode(theTable.AUTO_RESIZE_OFF);

			//ascoltatore della tabella
			theTable.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					int row = theTable.getSelectedRow();
					int column = theTable.getSelectedColumn();
					if (row == -1 || column != 1)
				return; // can't determine selected cell
					else{
						try{
							edumips64.Main.getSB().setText(
								CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +
								CurrentLocale.getString("StatusBar.OFREGISTER") +
								theTable.getSelectedRow() +
              " ( "+ registerToAlias("R"+theTable.getSelectedRow()) + " ) : " +
              Converter.hexToLong("0X" + value[theTable.getSelectedRow()]));
						}catch(IrregularStringOfHexException hex) { hex.printStackTrace();}
					}
				}
			});


			theTable.addMouseListener(new MyMouseListener());

			scrollTable = new JScrollPane(theTable);
			add(scrollTable, BorderLayout.CENTER);
			//setBackground(Color.RED);
			
			//init dei vettori statici 1a e 3a colonna
			for (int i = 0; i < 32; i++) 
			{
				numR[i] = "R" + i + " (" + registerToAlias(" " + i) + ") =";
				numRF[i] = "F" + i + " =";
				value[i] = "0000000000000000";
			}
			numR[32] = "LO =";
			value[32] = "0000000000000000";
			numR[33] = "HI =";
			value[33] = "0000000000000000";
		}	

		//classe interna che gestisce l'evento doppio click
  		class MyMouseListener implements MouseListener 
		{
    		public void mouseClicked (MouseEvent e){
    			Object premuto = e.getSource();
      			if ((premuto == theTable) && (theTable.getSelectedColumn() == 1)){
				try{
					if(theTable.getSelectedRow()==32)
					{
					
					edumips64.Main.getSB().setText(
							CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +	"LO" +
              " : " +
							Converter.hexToLong("0X" + value[theTable.getSelectedRow()]));					
	
					}
					else if (theTable.getSelectedRow()==33)
					{
					edumips64.Main.getSB().setText(
							CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +
							"HI" + " : " +
							Converter.hexToLong("0X" + value[theTable.getSelectedRow()]));					
					}	
					else
					{
					edumips64.Main.getSB().setText(
							CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +
							CurrentLocale.getString("StatusBar.OFREGISTER") +
							theTable.getSelectedRow() +
              " ( "+ registerToAlias("R"+theTable.getSelectedRow()) + " ) : " +
							Converter.hexToLong("0X" + value[theTable.getSelectedRow()]));					
					}
				}catch(IrregularStringOfHexException hex) { hex.printStackTrace();}
        			if (theTable.getSelectedRow() != 0 && e.getClickCount() == 2) {
					int row = theTable.getSelectedRow();
					oldValue = value[row]; //estraggo il valore corrente dal registro
					JDialog IVD = new InsertValueDialog(row,oldValue,value); 
				}
      			}
    		}
  
 		public void mouseEntered(MouseEvent e)  { }
    		public void mouseExited(MouseEvent e)  { }
    		public void mousePressed(MouseEvent e) { }
    		public void mouseReleased(MouseEvent e) { }
  		}

		class FileTableModel extends AbstractTableModel 
		{
			private String[] columnNames = {"Colonna1", "Colonna2", "Colonna3", "Colonna4"};
			private Class[] columnClasses = {String.class, String.class, String.class, String.class};
			private String value[];
			
			public FileTableModel(String[] value) 
			{
				this.value = value;
			}

        	public int getColumnCount() 
			{
      			return columnNames.length;
    		}
    
   			 public int getRowCount() 
			 {
      			return value.length;
    		 }
    
   			 public String getColumnName(int col) 
			 {
      			return columnNames[col];
    		 }
    
    		 public Object getValueAt(int row, int col) 
			 {
      		 	switch(col) 
				{
             		case 0:
					{
                		return numR[row]; 
					}
            		case 1:
                  		return value[row];
            		case 2:
                  		return numRF[row];
///
			case 3:
				if(row!=32 && row!=33)
					return "0000000000000000";
				else
					return "";
            		default:
                  		return new Object();
      			}
			}
    
    		public Class getColumnClass(int c) 
			{
     	 		return columnClasses[c];
    		}	  
		}			   							
	}

	//classe per la JDialog
	class InsertValueDialog extends JDialog implements ActionListener 
	{
		JButton OK;
		public InsertValueDialog()
		{
			super();
		}
				
		public InsertValueDialog (int row, String oldValue, String value[]) 
		{
			super();
			rowCurrent = row;
			valueCurrent = value;
			JFrame frameDialog = new JFrame();
			setTitle("Register");
			GridBagLayout GBL = new GridBagLayout();
			GridBagConstraints GBC = new GridBagConstraints();
			Dimension d = new Dimension(150,20);
			Container c = getContentPane();
			c.setLayout(GBL);
			JLabel label = new JLabel("R"+rowCurrent+" ( "+registerToAlias("R"+rowCurrent)+" ) = ");
			c.add(label);
			GBC.gridx = 0;
			GBC.gridy = 0;
			GBC.insets = new Insets(0,5,0,0);  //spaziatura
			GBL.setConstraints(label,GBC);
			text = new JTextArea(oldValue);
			text.addKeyListener(new MyKeyListener());
			text.setPreferredSize(d);
			c.add(text);
			GBC.gridx = 1;
			GBC.gridy = 0;
			GBC.insets = new Insets(0,5,0,0);  //spaziatura
			GBL.setConstraints(text,GBC);
			OK = new JButton("OK");
			OK.addActionListener(this);
			OK.addKeyListener(new MyKeyListener());
			c.add(OK);
			GBC.gridx = 1;
			GBC.gridy = 1;
			GBC.insets = new Insets(15,-60,0,0);  //spaziatura
			GBL.setConstraints(OK,GBC);	
			setSize(210,100);
			setLocation(400,300);
			setVisible(true);
		}

		public int confirmAction ()
		{
			String renumeration = null;
			boolean check = false;
			int okValue = 0;

			for (int x = 0; x < text.getText().length(); x++) {
				char c = text.getText().charAt(x);
				if ((c == '0') || (c == '1') || (c == '2') || (c == '3') || (c == '4') || (c == '5') || (c == '6') || (c == '7') ||
						(c == '8') || (c == '9') || (c == 'A') || (c == 'B') || (c == 'C') || (c == 'D') || (c == 'E') || (c == 'F') ||
						(c == 'a') || (c == 'b') || (c == 'c') || (c == 'd') || (c == 'e') || (c == 'f'))
				{
					check = true;		
				}
				else
				{
					check = false;
					break;
				}
			}
			if (check == true) 
			{
				okValue = 1;
				String newValue = text.getText();
				int length = newValue.length();
				int difference = 16 - length;
				if (difference != 0) 
				{
					renumeration = new String();
					for (int i = 0; i < difference; i++) 
					{
						renumeration = renumeration + "0";
					}
					valueCurrent[rowCurrent] = renumeration + newValue;

					//register persistance from extern JDialog ^_^ !!!
					try
					{
						String bin = Converter.hexToBin(valueCurrent[rowCurrent]);
						registers[rowCurrent].setBits(bin,0);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}								
				}
				else 
				{
					valueCurrent[rowCurrent] = newValue;

					try
					{
						CPU cpu=CPU.getInstance();
						String bin = Converter.hexToBin(valueCurrent[rowCurrent]);
						//writing generic gpr
						if(rowCurrent<32)
							registers[rowCurrent].setBits(bin,0);
						//writing LO
						else if(rowCurrent==32)
							cpu.getLO().setBits(bin,0);
						//writing HI
						else if(rowCurrent==33)
							cpu.getHI().setBits(bin,0);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}		
			return okValue;
		}

		public void actionPerformed(ActionEvent ev) 
		{
			Object pressed = ev.getSource();
			if (pressed == OK) 
			{	
				InsertValueDialog obj = new InsertValueDialog();
				int closeDialog = obj.confirmAction();
				if (closeDialog == 1)
					setVisible(false);
			}
			
		}

		//classe interna per quando si PIGIA "INVIO"
		class MyKeyListener implements KeyListener 
		{
			String stringa = oldValue;
			public void keyPressed(KeyEvent e)  
			{
				text.setEditable(true);
      			int active = e.getKeyCode();
				if (text.getText().length() > 15)
					text.setEditable(false);

				if (active == KeyEvent.VK_BACK_SPACE)
					text.setEditable(true);

				if (active == KeyEvent.VK_ENTER)
				{
					InsertValueDialog obj = new InsertValueDialog();
					int closeDialog = obj.confirmAction();
					if (closeDialog == 1)
						setVisible(false);
					else
						text.setEditable(false);
				}
			}
		
    		public void keyReleased(KeyEvent e)  {}
    		public void keyTyped(KeyEvent e)  {}
  		}
	}

	public static void main (String argv[])
	{
		JFrame frame = new JFrame("Registers");
		Container c = frame.getContentPane();
		GUIRegisters GUIR = new GUIRegisters(); 
		GUIR.setContainer(c);
		//c.add(GUIR.regPanel);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}				
}
