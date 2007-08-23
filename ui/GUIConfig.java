/* GUIConfig.java
 *
 * This class provides a window for configuration options.
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
package edumips64.ui;

import edumips64.utils.Config;
import edumips64.utils.CurrentLocale;
import edumips64.Main;
import edumips64.core.is.Instruction;
import edumips64.core.CPU;

import java.util.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * This class provides a window for configuration options.
*/
public class GUIConfig extends JDialog{

	String MAIN;
	String APPEARANCE;
	String BEHAVIOR;
	
	JTabbedPane tabPanel;
	JButton okButton;
	HashMap<String,Object> updatedMap;
	int width = 400, height = 250;	
	public GUIConfig(final JFrame owner){
		super(owner, CurrentLocale.getString("Config.ITEM"), true);
		MAIN = CurrentLocale.getString("Config.MAIN");
		APPEARANCE = CurrentLocale.getString("Config.APPEARANCE");
		BEHAVIOR = CurrentLocale.getString("Config.BEHAVIOR");
		updatedMap = new HashMap<String, Object>();
		updatedMap.putAll(Config.getMap());

		tabPanel = new JTabbedPane();
		tabPanel.addTab(MAIN, makeMainPanel());
		tabPanel.addTab(BEHAVIOR, makeBehaviorPanel());
		tabPanel.addTab(APPEARANCE, makeAppearancePanel());

		final JPanel buttonPanel = new JPanel();
		addButtons(buttonPanel);

		getRootPane().setDefaultButton(okButton);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add("North", tabPanel);
		getContentPane().add("South", buttonPanel);
		
		//pack();
		setSize(width,height);
		setLocation((getScreenWidth() - getWidth()) / 2, (getScreenHeight() - getHeight()) / 2);
		setVisible(true);

	}

	GridBagLayout gbl;
	GridBagConstraints gbc;

	//INIZIA ZTUDIO
	private JPanel makeMainPanel(){

		gbl = new GridBagLayout();
		gbc = new GridBagConstraints();
		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,10,0,10);

		JPanel panel = new JPanel();

		panel.setLayout(gbl);
		panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
		int row = 2;
		
		addRow(panel,row++, "forwarding",new JCheckBox());
		addRow(panel,row++, "n_step",new JNumberField());

		// fill remaining vertical space
		grid_add(panel,new JPanel(),gbl,gbc,0,1,0,row,GridBagConstraints.REMAINDER,1);
		//panel.setSize(width,height - buttonHeight);
		return panel;
	}
	private JPanel makeBehaviorPanel(){

		gbl = new GridBagLayout();
		gbc = new GridBagConstraints();
		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,10,0,10);

		JPanel panel = new JPanel();

		panel.setLayout(gbl);
		panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
		int row = 2;

		addRow(panel, row++, "warnings",new JCheckBox());
		addRow(panel, row++, "verbose",new JCheckBox());
		addRow(panel, row++, "sleep_interval", new JNumberField());
		addRow(panel, row++, "syncexc-masked", new JCheckBox());
		addRow(panel, row++, "syncexc-terminate", new JCheckBox());

		// fill remaining vertical space
		grid_add(panel,new JPanel(),gbl,gbc,0,1,0,row,GridBagConstraints.REMAINDER,1);

		return panel;
	}

	private JPanel makeAppearancePanel(){

		gbl = new GridBagLayout();
		gbc = new GridBagConstraints();
		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,10,0,10);

		JPanel panel = new JPanel();

		panel.setLayout(gbl);
		panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
		int row = 2;

		addRow(panel,row++, "IFColor",new JButton());
		addRow(panel,row++, "IDColor",new JButton());
		addRow(panel,row++, "EXColor",new JButton());
		addRow(panel,row++, "MEMColor",new JButton());	
		addRow(panel,row++, "WBColor",new JButton());
		addRow(panel,row++, "show_aliases",new JCheckBox());


		// fill remaining vertical space
		grid_add(panel,new JPanel(),gbl,gbc,0,1,0,row,GridBagConstraints.REMAINDER,1);
		//panel.setSize(width,height - buttonHeight);
		return panel;
	}
	public void addRow(JPanel panel,final int row,final String key, final JComponent comp){
		String title = CurrentLocale.getString("Config." + key.toUpperCase());
		String tip = CurrentLocale.getString("Config." + key.toUpperCase() + ".tip");
		//Setting title
		JLabel label = new JLabel(title);
			label.setHorizontalAlignment(JLabel.RIGHT);
			label.setToolTipText(tip);
		grid_add(panel,label,gbl,gbc,.1,0,0,row,1,1);
		
		
		
		if(comp instanceof JCheckBox){
			final JCheckBox cbox = (JCheckBox)comp;
			//Setting Component
			cbox.setHorizontalAlignment(SwingConstants.LEFT);
			cbox.setVerticalAlignment(SwingConstants.CENTER);
			cbox.setSelected((Boolean)Config.get(key));

			cbox.setAction(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					updatedMap.put(key,cbox.getModel().isSelected());
				}
			});
		}
		else if(comp instanceof JNumberField){
			final JNumberField number = (JNumberField)comp;
			number.setNumber((Integer)Config.get(key));

			number.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					if(number.isNumber()) updatedMap.put(key,number.getNumber());
					else JOptionPane.showMessageDialog(GUIConfig.this, CurrentLocale.getString("INT_FORMAT_EXCEPTION"), CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
				}
			});
			number.setAction(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					if(number.isNumber()) updatedMap.put(key,number.getNumber());
					else JOptionPane.showMessageDialog(GUIConfig.this, CurrentLocale.getString("INT_FORMAT_EXCEPTION"), CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		else if(comp instanceof JTextField){
			final JTextField text = (JTextField)comp;
			text.setText(Config.get(key).toString());

			text.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					System.out.println("focus");
					updatedMap.put(key,text.getText());
				}
			});
			text.setAction(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("abstract");
					updatedMap.put(key,text.getText());
				}
			});
		}
		else if(comp instanceof JButton){
			final JButton button = (JButton)comp;
			button.setBounds(0,0,50,10);
			button.setBackground((Color)Config.get(key));
			button.addActionListener(new ActionListener(){
				public void actionPerformed( ActionEvent e ) {
					Color color = JColorChooser.showDialog(
                     					GUIConfig.this,
                     					CurrentLocale.getString("Config." + key.toUpperCase()),
                     					button.getBackground());
					if(color != null){
						button.setBackground(color);
						updatedMap.put(key,button.getBackground());
					}
				}
			});
		}
					
					

		grid_add(panel, comp,gbl,gbc,.2,0,1,row,1,1);

		panel.setMinimumSize(new java.awt.Dimension(10,10));
	}

	private static void grid_add 	(
						JComponent jc_, //pannello contenitore
		       				Component c_, //Componente da inserire
						GridBagLayout gbl_, //Layout da usare
						GridBagConstraints gbc_, //Costanti
						double weightx_, double weighty_, 
						int x_, int y_, 
						int w_, int h_
					){
		gbc_.weightx = weightx_;
		gbc_.weighty = weighty_;
		gbc_.gridx = x_;
		gbc_.gridy = y_;
		gbc_.gridwidth = w_;
		gbc_.gridheight = h_;
		gbl_.setConstraints (c_, gbc_);
		jc_.add (c_);
	}


	public void addButtons(JPanel buttonPanel) {

		final JButton okButton = new JButton("OK");
		final JButton cancelButton = new JButton("Cancel");
	
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		//Setting Action for each buttons
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				
				if( (Boolean) Config.get("show_aliases")  !=  updatedMap.get("show_aliases"))
				{
					Config.setMap(updatedMap);
					((GUIFrontend) edumips64.Main.getGUIFrontend()).updateComponents();
				}
				else
					Config.setMap(updatedMap);
				setVisible(false);
				if(Instruction.getEnableForwarding() != (Boolean)Config.get("forwarding")) {
					CPU cpu = CPU.getInstance();
					Instruction.setEnableForwarding((Boolean)Config.get("forwarding"));
					
					// Let's verify that we have to reset the CPU
					if(cpu.getStatus() == CPU.CPUStatus.RUNNING) {
						System.out.println("Reset");
						edumips64.Main.resetSimulator();
					}
				}
				edumips64.Main.updateCGT();
				
			}
		});
	}
	public static int getScreenWidth() {
		return (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	}

	public static int getScreenHeight() {
		return (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	}

}
