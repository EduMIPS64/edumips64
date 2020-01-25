/* GUIData.java
 *
 * This class draw the data memory representation.
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
package org.edumips64.ui.swing;

import org.edumips64.core.*;
import org.edumips64.core.fpu.BitSet64FP;
import org.edumips64.utils.*;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;

/**
* This class draw the data memory representation.
*/
public class GUIData extends GUIComponent {
  private DataPanel dataPanel;
  private String[] memoryAddress = new String[Memory.DATALIMIT];
  private JTextArea text;
  private int row;
  private StatusBar statusbar;

  private static final Logger LOGGER = Logger.getLogger(CPU.class.getName());

  public GUIData(CPU cpu, Memory memory, ConfigStore config, StatusBar sb) {
    super(cpu, memory, config);
    statusbar = sb;
    dataPanel = new DataPanel();
  }

  @Override
  public void setContainer(Container co) {
    super.setContainer(co);
    cont.add(dataPanel);
  }

  public void update() {
  }

  public void draw() {
    cont.repaint();
  }

  @Override
  public void updateLanguageStrings() {
    GUIFrontend.updateColumnHeaderNames(dataPanel.theTable);
  }

  private class DataPanel extends JPanel {
    JTable theTable;
    JScrollPane scrollTable;
    FileTableModel tableModel;


    DataPanel() {
      super();
      setBackground(Color.WHITE);
      setLayout(new BorderLayout());
      tableModel = new FileTableModel(memoryAddress);
      theTable = new JTable(tableModel);
      theTable.setCellSelectionEnabled(false);
      theTable.setFont(font);
      theTable.getColumnModel().getColumn(0).setPreferredWidth(scale(60));
      theTable.getColumnModel().getColumn(1).setPreferredWidth(scale(130));
      theTable.getColumnModel().getColumn(2).setPreferredWidth(scale(80));
      theTable.getColumnModel().getColumn(3).setPreferredWidth(scale(200));
      theTable.getColumnModel().getColumn(4).setPreferredWidth(scale(200));
      theTable.setRowHeight(scale(theTable.getRowHeight()));

      //theTable.setTableHeader(null); //cosÃ¬ visualizzo le intestazioni
      Font headerFont = theTable.getTableHeader().getFont();
      theTable.getTableHeader().setFont(new Font(headerFont.getName(), headerFont.getStyle(), font.getSize()));
      theTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      theTable.setShowGrid(false);
      theTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      theTable.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) {
          int row = theTable.getSelectedRow();
          int column = theTable.getSelectedColumn();

          if (row != -1 && column == 1) {
            try {
              statusbar.setText(
                CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +
                CurrentLocale.getString("StatusBar.MEMORYCELL") +
                " " + tableModel.getValueAt(theTable.getSelectedRow(), 0) +
                " : " +
                Converter.hexToLong("0X" + tableModel.getValueAt(theTable.getSelectedRow(), 1)));
            } catch (IrregularStringOfHexException hex) {
              hex.printStackTrace();
            }
          }
        }
      });
      //ascoltatore della tabella
      theTable.addMouseListener(new MyMouseListener());

      //aggiungo la tabella ad uno SCROLLER
      scrollTable = new JScrollPane(theTable);
      add(scrollTable, BorderLayout.CENTER);
    }

    class MyMouseListener implements MouseListener {
      public void mouseClicked(MouseEvent e) {
        Object premuto = e.getSource();

        if ((premuto == theTable) && (theTable.getSelectedColumn() == 1)) {
          try {
            String value = CurrentLocale.getString("StatusBar.DECIMALVALUE") + " " +
                           CurrentLocale.getString("StatusBar.MEMORYCELL") +
                           " " + tableModel.getValueAt(theTable.getSelectedRow(), 0) + " : ";

            //if the current memory cell visualization is long
            if (config.getBoolean(ConfigKey.FP_LONG_DOUBLE_VIEW)) {
              value += Converter.hexToLong("0X" + tableModel.getValueAt(theTable.getSelectedRow(), 1));
            } else { //the current memory cell visualization in the status bar is double, we build a temp. bitset in order to read the double value
              BitSet64FP bs = new BitSet64FP();

              try {
                bs.setBits(Converter.hexToBin("" + tableModel.getValueAt(theTable.getSelectedRow(), 1)), 0);
              } catch (IrregularStringOfHexException | IrregularStringOfBitsException ex) {
                ex.printStackTrace();
              }

              value += bs.readDouble();
            }

            statusbar.setText(value);
          } catch (IrregularStringOfHexException hex) {
            hex.printStackTrace();
          }

          if (e.getClickCount() == 2) {
            row = theTable.getSelectedRow();
            String memLabel = (String) tableModel.getValueAt(row, 0);
            String oldValue = (String) tableModel.getValueAt(row, 1);
            JDialog IVD = new InsertValueDialog(memLabel, oldValue);
            IVD.setVisible(true);
          }
        }
      }

      public void mouseEntered(MouseEvent e)  { }
      public void mouseExited(MouseEvent e)  { }
      public void mousePressed(MouseEvent e) { }
      public void mouseReleased(MouseEvent e) { }
    }

    class FileTableModel extends AbstractTableModel {
      private String[] columnLocaleStrings = {"ADDRESS", "HEXREPR", "LABEL", "DATA", "COMMENT"};
      private Class<?>[] columnClasses = {String.class, String.class, String.class, String.class, String.class};
      private String[] memoryAddress;

      FileTableModel(String[] memoryAddress) {
        this.memoryAddress = memoryAddress;
      }

      public int getColumnCount() {
        return columnLocaleStrings.length;
      }

      public int getRowCount() {
        return memoryAddress.length;
      }

      public String getColumnName(int col) {
        return CurrentLocale.getString(columnLocaleStrings[col]);
      }

      public Object getValueAt(int row, int col) {
        Object toReturn = new Object();

        try {
          switch (col) {
          case 0:
            long address = memory.getCellByIndex(row).getAddress();
            toReturn = Converter.binToHex(Converter.positiveIntToBin(16, address));
            break;
          case 1:
            toReturn = memory.getCellByIndex(row).getHexString();
            break;
          case 2:
            toReturn = memory.getCellByIndex(row).getLabel();
            break;
          case 3:
            toReturn = memory.getCellByIndex(row).getCode();
            break;
          case 4:
            toReturn = memory.getCellByIndex(row).getComment();
            break;
          }
        } catch (IrregularStringOfBitsException | MemoryElementNotFoundException ex) {
          LOGGER.warning(ex.toString());
        }

        return toReturn;
      }

      public Class<?> getColumnClass(int c) {
        return columnClasses[c];
      }
    }

    class InsertValueDialog extends JDialog implements ActionListener {
      JButton OK;

      InsertValueDialog() {
        super();
      }

      InsertValueDialog(String memLabel, String oldValue) {
        super();
        setTitle("Data");
        GridBagLayout GBL = new GridBagLayout();
        GridBagConstraints GBC = new GridBagConstraints();
        Dimension d = new Dimension(150, 20);
        Container c = getContentPane();
        c.setLayout(GBL);
        JLabel label = new JLabel(memLabel);
        label.setFont(font);
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
        GBC.insets = new Insets(15, 0, 0, 0);  //spaziatura
        GBL.setConstraints(OK, GBC);
        setSize(200, 100);
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

          MemoryElement memoryElement;
          if (difference != 0) {
            renumeration = "";

            for (int i = 0; i < difference; i++) {
              renumeration = renumeration + "0";
            }

            String banana = renumeration + newValue;

            //memory persistance from extern JDialog ^_^ !!!

            try {
              String hexAddress = (String) tableModel.getValueAt(row, 0);
              String binAddress = Converter.hexToBin(hexAddress);
              int address = Converter.binToInt(binAddress, true);
              memoryElement = memory.getCellByAddress(address);
              memoryElement.setBits(Converter.hexToBin(banana), 0);
            } catch (Exception e) {
              e.printStackTrace();
            }
          } else {
            String banana = newValue;

            try {
              String hexAddress = (String) tableModel.getValueAt(row, 0);
              String binAddress = Converter.hexToBin(hexAddress);
              int address = Converter.binToInt(binAddress, true);
              memoryElement = memory.getCellByAddress(address);
              memoryElement.setBits(Converter.hexToBin(banana), 0);
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

      //classe interna che ascolta gli eventu sulla tastiera quando appare la
      //Jdialog per la modifica manuale delle celle di memoria
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
}
