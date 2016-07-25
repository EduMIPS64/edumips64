/* GUIRegisters.java
 *
 * This class shows the values stored in the registers.
 * (c) 2006 Carmelo Mirko Musumeci, Massimo Trubia (FPU modifications)
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

import org.edumips64.core.CPU;
import org.edumips64.core.Memory;
import org.edumips64.core.Register;
import org.edumips64.core.RegisterFP;
import org.edumips64.utils.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

/**
* This class shows the values stored in the registers.
*/

class GUIRegisters extends GUIComponent {
  private Register registers[];
  private RegisterFP registersFP[];
  private RegPanel regPanel;
  private JTextArea text;
  String value[] = new String[34];
  private String valueFP[] = new String[32];
  private int rowCurrent;
  private String valueCurrent[];
  private enum AliasRegister
  {zero, at, v0, v1, a0, a1, a2, a3, t0, t1, t2, t3, t4, t5, t6, t7, s0, s1, s2, s3, s4, s5, s6, s7, t8, t9, k0, k1, gp, sp, fp, ra}
  private int xprLastDoubleClick; //used for instanciating an InsertValueDialog if a double click on gpr or fpr is performed
  //(i want to avoid to modify the class constructor) the value is zero if a gpr is double clicked
  //1 if an fpr is double clicked

  GUIRegisters(CPU cpu, Memory memory, ConfigStore config) {
    super(cpu, memory, config);
    registers = cpu.getRegisters();
    registersFP = cpu.getRegistersFP();
    regPanel = new RegPanel();
    xprLastDoubleClick = 0;
  }

  public void setContainer(Container co) {
    super.setContainer(co);
    cont.add(regPanel);
  }

  public void update() {
    regPanel.updateRegistersNames();
    registers = cpu.getRegisters();
    registersFP = cpu.getRegistersFP();

    for (int i = 0; i < 32; i++) {
      value[i] = registers[i].toString();
    }

    for (int i = 0; i < 32; i++) {
      valueFP[i] = registersFP[i].toString();
    }

    value[32] = cpu.getLO().toString();
    value[33] = cpu.getHI().toString();
  }

  public void draw() {
    cont.repaint();
  }

  private String registerToAlias(String reg) {
    int number = Integer.parseInt(reg.substring(1));

    if (number == 32) {
      return ("   LO    =  ");
    }

    if (number == 33) {
      return ("   HI    =  ");
    }

    for (AliasRegister x : AliasRegister.values()) {
      if (number == (x.ordinal())) {
        return ("$" + x.name());
      }
    }

    return "";
  }


  private class RegPanel extends JPanel {
    JTable theTable;
    JScrollPane scrollTable;
    FileTableModel tableModel;
    String numR[] = new String[34];
    String numRF[] = new String[34];

    RegPanel() {
      super();
      setLayout(new BorderLayout());
      tableModel = new FileTableModel(value);
      setBackground(Color.WHITE);
      theTable = new JTable(tableModel);
      theTable.getColumnModel().getColumn(0).setPreferredWidth(50);
      theTable.getColumnModel().getColumn(1).setPreferredWidth(147);
      theTable.getColumnModel().getColumn(2).setPreferredWidth(50);
      theTable.getColumnModel().getColumn(3).setPreferredWidth(147);
      theTable.setRowSelectionAllowed(false);
      theTable.setColumnSelectionAllowed(false);
      theTable.setCellSelectionEnabled(false);
      theTable.setTableHeader(null);
      theTable.setShowGrid(false);
      Font f = new Font("Monospaced", Font.PLAIN, 12);
      theTable.setFont(f);
      theTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      //ascoltatore della tabella
      theTable.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) {
          int row = theTable.getSelectedRow();
          int column = theTable.getSelectedColumn();

          if (row != -1 && column == 1) {
            try {
              org.edumips64.Main.getSB().setText(
                CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +
                CurrentLocale.getString("StatusBar.OFREGISTER") +
                theTable.getSelectedRow() +
                " ( " + registerToAlias("R" + theTable.getSelectedRow()) + " ) : " +
                Converter.hexToLong("0X" + value[theTable.getSelectedRow()]));
            } catch (IrregularStringOfHexException hex) {
              hex.printStackTrace();
            }
          }
        }
      });


      theTable.addMouseListener(new MyMouseListener());

      scrollTable = new JScrollPane(theTable);
      add(scrollTable, BorderLayout.CENTER);
      //setBackground(Color.RED);

      //init dei vettori statici 1a e 3a colonna
      for (int i = 0; i < 32; i++) {
        numR[i] = fillFirstColumn(i);
        numRF[i] = "F" + i + " =";
        value[i] = "0000000000000000";
//FPU
        valueFP[i] = "0000000000000000";
      }

      numR[32] = "LO =";
      value[32] = "0000000000000000";
      numR[33] = "HI =";
      value[33] = "0000000000000000";
    }

    String fillFirstColumn(int i) {
      if (config.getBoolean(ConfigKey.SHOW_ALIASES)) {
        return registerToAlias(" " + i) + "=";
      } else {
        return "R" + i + " =";
      }
    }

    void updateRegistersNames() {
      for (int i = 0; i < 32; i++) {
        numR[i] = fillFirstColumn(i);
      }

      draw();
    }

    //classe interna che gestisce l'evento doppio click
    class MyMouseListener implements MouseListener {
      public void mouseClicked(MouseEvent e) {
        Object premuto = e.getSource();

        if ((premuto == theTable)) {
          try {
            //click on LO register
            if (theTable.getSelectedRow() == 32 && theTable.getSelectedColumn() == 1) {

              org.edumips64.Main.getSB().setText(
                CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " + "LO" + " : " +
                Converter.hexToLong("0X" + value[theTable.getSelectedRow()]));

            }
            //click on HI register
            else if (theTable.getSelectedRow() == 33  && theTable.getSelectedColumn() == 1) {
              org.edumips64.Main.getSB().setText(
                CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +
                "HI" + " : " +
                Converter.hexToLong("0X" + value[theTable.getSelectedRow()]));
            }
            //FPU   //click on generic fpr
            else if (theTable.getSelectedColumn() == 3 && theTable.getSelectedRow() < 32) {
              org.edumips64.Main.getSB().setText(
                CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +
                CurrentLocale.getString("StatusBar.OFREGISTERFP") +
                theTable.getSelectedRow() +
                " : " +
                cpu.getRegisterFP(theTable.getSelectedRow()).readDouble());

            }
            //click on generic gpr
            else if (theTable.getSelectedColumn() == 1) {
              org.edumips64.Main.getSB().setText(
                CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +
                CurrentLocale.getString("StatusBar.OFREGISTER") +
                theTable.getSelectedRow() +
                " ( " + registerToAlias("R" + theTable.getSelectedRow()) + " ) : " +
                Converter.hexToLong("0X" + value[theTable.getSelectedRow()]));
            }
          } catch (IrregularStringOfHexException hex) {
            hex.printStackTrace();
          }


          //double click on the generic gpr
          String oldValue;
          if (theTable.getSelectedColumn() == 1 &&  theTable.getSelectedRow() != 0 && e.getClickCount() == 2) {
            xprLastDoubleClick = 0;
            int row = theTable.getSelectedRow();
            oldValue = value[row]; //estraggo il valore corrente dal registro
            JDialog IVD = new InsertValueDialog(row, oldValue, value);
            IVD.setVisible(true);
          }
          //double click on the generic fpr
          else if (theTable.getSelectedColumn() == 3 && theTable.getSelectedRow() < 32 && e.getClickCount() == 2) {
            xprLastDoubleClick = 1;
            int row = theTable.getSelectedRow();
            oldValue = valueFP[row];
            JDialog IVD = new InsertValueDialog(row, oldValue, valueFP);
            IVD.setVisible(true);
          }
        }
      }

      public void mouseEntered(MouseEvent e)  { }
      public void mouseExited(MouseEvent e)  { }
      public void mousePressed(MouseEvent e) { }
      public void mouseReleased(MouseEvent e) { }
    }

    @SuppressWarnings("rawtypes")
    class FileTableModel extends AbstractTableModel {
      private String[] columnNames = {"Colonna1", "Colonna2", "Colonna3", "Colonna4"};
      private Class[] columnClasses = {String.class, String.class, String.class, String.class};
      private String value[];

      FileTableModel(String[] value) {
        this.value = value;
      }

      public int getColumnCount() {
        return columnNames.length;
      }

      public int getRowCount() {
        return value.length;
      }

      public String getColumnName(int col) {
        return columnNames[col];
      }

      public Object getValueAt(int row, int col) {
        switch (col) {
        case 0: {
          return numR[row];
        }
        case 1:
          return value[row];
        case 2:
          return numRF[row];
///
        case 3:

          if (row != 32 && row != 33)
//FPU         //return "0000000000000000";
          {
            return valueFP[row];
          } else {
            return "";
          }

        default:
          return new Object();
        }
      }

      @SuppressWarnings("rawtypes")
      public Class getColumnClass(int c) {
        return columnClasses[c];
      }
    }
  }

  //classe per la JDialog
  private class InsertValueDialog extends JDialog implements ActionListener {
    JButton OK;
    
    InsertValueDialog() {
      super();
    }

    InsertValueDialog(int row, String oldValue, String value[]) {
      super();
      rowCurrent = row;
      valueCurrent = value;
      setTitle("Register");
      GridBagLayout GBL = new GridBagLayout();
      GridBagConstraints GBC = new GridBagConstraints();
      Dimension d = new Dimension(150, 20);
      Container c = getContentPane();
      c.setLayout(GBL);
      JLabel label = null;

      //double click on generic gpr
      if (xprLastDoubleClick == 0) {
        label = new JLabel("R" + rowCurrent + " ( " + registerToAlias("R" + rowCurrent) + " ) = ");
      }
      //double click on generic fpr
      else if (xprLastDoubleClick == 1) {
        label = new JLabel("F" + rowCurrent + "=");
      }

      c.add(label);
      GBC.gridx = 0;
      GBC.gridy = 0;
      GBC.insets = new Insets(0, 5, 0, 0);  //spaziatura
      GBL.setConstraints(label, GBC);
      text = new JTextArea(oldValue);
      text.addKeyListener(new MyKeyListener());
      text.setPreferredSize(d);
      c.add(text);
      GBC.gridx = 1;
      GBC.gridy = 0;
      GBC.insets = new Insets(0, 5, 0, 0);  //spaziatura
      GBL.setConstraints(text, GBC);
      OK = new JButton("OK");
      OK.addActionListener(this);
      OK.addKeyListener(new MyKeyListener());
      c.add(OK);
      GBC.gridx = 1;
      GBC.gridy = 1;
      GBC.insets = new Insets(15, -60, 0, 0);  //spaziatura
      GBL.setConstraints(OK, GBC);
      setSize(210, 100);
      setLocation(400, 300);
    }
    
    int confirmAction() {
      String renumeration;
      boolean check = false;
      int okValue = 0;

      for (int x = 0; x < text.getText().length(); x++) {
        char c = text.getText().charAt(x);

        if ((c == '0') || (c == '1') || (c == '2') || (c == '3') || (c == '4') || (c == '5') || (c == '6') || (c == '7') ||
            (c == '8') || (c == '9') || (c == 'A') || (c == 'B') || (c == 'C') || (c == 'D') || (c == 'E') || (c == 'F') ||
            (c == 'a') || (c == 'b') || (c == 'c') || (c == 'd') || (c == 'e') || (c == 'f')) {
          check = true;
        } else {
          check = false;
          break;
        }
      }

      if (check) {
        okValue = 1;
        String newValue = text.getText();
        int length = newValue.length();
        int difference = 16 - length;

        if (difference != 0) {
          renumeration = "";

          for (int i = 0; i < difference; i++) {
            renumeration = renumeration + "0";
          }

          valueCurrent[rowCurrent] = renumeration + newValue;

          //register persistance from extern JDialog ^_^ !!!
          try {
            String bin = Converter.hexToBin(valueCurrent[rowCurrent]);

            if (xprLastDoubleClick == 0) {
              registers[rowCurrent].setBits(bin, 0);
            } else if (xprLastDoubleClick == 1) {
              registersFP[rowCurrent].setBits(bin, 0);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          valueCurrent[rowCurrent] = newValue;

          try {
            String bin = Converter.hexToBin(valueCurrent[rowCurrent]);

            //writing labels on registers
            //writing gprs
            if (xprLastDoubleClick == 0 && rowCurrent < 32) {
              registers[rowCurrent].setBits(bin, 0);
            }
            //writing fprs
            else if (xprLastDoubleClick == 1 && rowCurrent < 32) {
              registersFP[rowCurrent].setBits(bin, 0);
            }

            //writing LO
            if (xprLastDoubleClick == 0 && rowCurrent == 32) {
              cpu.getLO().setBits(bin, 0);
            } else if (xprLastDoubleClick == 0 && rowCurrent == 33) {
              cpu.getHI().setBits(bin, 0);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

      return okValue;
    }

    public void actionPerformed(ActionEvent ev) {
      Object pressed = ev.getSource();

      if (pressed == OK) {
        InsertValueDialog obj = new InsertValueDialog();
        obj.setVisible(true);
        int closeDialog = obj.confirmAction();

        if (closeDialog == 1) {
          setVisible(false);
        }
      }

    }

    //classe interna per quando si PIGIA "INVIO"
    class MyKeyListener implements KeyListener {
      public void keyPressed(KeyEvent e) {
        text.setEditable(true);
        int active = e.getKeyCode();

        if (text.getText().length() > 15) {
          text.setEditable(false);
        }

        if (active == KeyEvent.VK_BACK_SPACE) {
          text.setEditable(true);
        }

        if (active == KeyEvent.VK_ENTER) {
          InsertValueDialog obj = new InsertValueDialog();
          obj.setVisible(true);
          int closeDialog = obj.confirmAction();

          if (closeDialog == 1) {
            setVisible(false);
          } else {
            text.setEditable(false);
          }
        }
      }

      public void keyReleased(KeyEvent e)  {}
      public void keyTyped(KeyEvent e)  {}
    }
  }
}
