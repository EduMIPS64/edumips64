/* GUICode.java
 *
 * This class draw the code memory representation in a window with three columns.
 * (c) 2006 Alessandro Nicolosi, Massimo Trubia (FPU modifications)
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
import org.edumips64.core.is.Instruction;
import org.edumips64.utils.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
/**
* This class draws the code memory representation in a window with five columns.
*/
public class GUICode extends GUIComponent {
  private CodePanel codePanel;
  private String memoryAddress[] = new String[CPU.CODELIMIT];
  private static int ifIndex, idIndex, exIndex, memIndex, wbIndex, A1Index, A2Index, A3Index, A4Index, M1Index, M2Index, M3Index, M4Index, M5Index, M6Index, M7Index, DIVIndex;

  public GUICode(CPU cpu, Memory memory, ConfigStore config) {
    super(cpu, memory, config);
    codePanel = new CodePanel();
  }

  public void setContainer(Container co) {
    super.setContainer(co);
    cont.add(codePanel);
  }

  public void updateLanguageStrings() {
    GUIFrontend.updateColumnHeaderNames(codePanel.theTable);
  }

  public void update() {
    //codePanel.scrollTable.getViewport().setViewPosition(new Point(0,position+15));

    TableColumn column0 = codePanel.theTable.getColumnModel().getColumn(0);
    column0.setCellRenderer(new MyTableCellRenderer());

    TableColumn column1 = codePanel.theTable.getColumnModel().getColumn(1);
    column1.setCellRenderer(new MyTableCellRenderer());

    TableColumn column2 = codePanel.theTable.getColumnModel().getColumn(2);
    column2.setCellRenderer(new MyTableCellRenderer());

    TableColumn column3 = codePanel.theTable.getColumnModel().getColumn(3);
    column3.setCellRenderer(new MyTableCellRenderer());

    TableColumn column4 = codePanel.theTable.getColumnModel().getColumn(4);
    column4.setCellRenderer(new MyTableCellRenderer());

    Instruction ifInstruction = cpu.getPipeline().get(CPU.PipeStage.IF);
    ifIndex = memory.getInstructionIndex(ifInstruction);
    if ((ifInstruction != null) && ifInstruction.isBubble()) {
      ifIndex = -1;
    }
    idIndex = memory.getInstructionIndex(cpu.getPipeline().get(CPU.PipeStage.ID));
    exIndex = memory.getInstructionIndex(cpu.getPipeline().get(CPU.PipeStage.EX));
    memIndex = memory.getInstructionIndex(cpu.getPipeline().get(CPU.PipeStage.MEM));
    wbIndex = memory.getInstructionIndex(cpu.getPipeline().get(CPU.PipeStage.WB));

    A1Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("ADDER", 1));
    A2Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("ADDER", 2));
    A3Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("ADDER", 3));
    A4Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("ADDER", 4));
    M1Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("MULTIPLIER", 1));
    M2Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("MULTIPLIER", 2));
    M3Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("MULTIPLIER", 3));
    M4Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("MULTIPLIER", 4));
    M5Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("MULTIPLIER", 5));
    M6Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("MULTIPLIER", 6));
    M7Index = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("MULTIPLIER", 7));
    DIVIndex = memory.getInstructionIndex(cpu.getInstructionByFuncUnit("DIVIDER", 0));

  }

  public void draw() {
    cont.repaint();

    // I can get the table because it has package visibility.
    // This row makes the IF row always visible.
    codePanel.theTable.scrollRectToVisible(codePanel.theTable.getCellRect(ifIndex, 0, true));
  }

  private class CodePanel extends JPanel {
    JTable theTable;
    JScrollPane scrollTable;
    MyTableModel tableModel;

    CodePanel() {
      super();

      setLayout(new BorderLayout());
      setBackground(Color.WHITE);
      tableModel = new MyTableModel(memoryAddress);
      theTable = new JTable(tableModel);
      theTable.setCellSelectionEnabled(false);
      theTable.setFocusable(false);
      theTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      theTable.setShowGrid(false);

      theTable.getColumnModel().getColumn(0).setPreferredWidth(60);
      theTable.getColumnModel().getColumn(1).setPreferredWidth(130);
      theTable.getColumnModel().getColumn(2).setPreferredWidth(80);
      theTable.getColumnModel().getColumn(3).setPreferredWidth(200);
      theTable.getColumnModel().getColumn(4).setPreferredWidth(200);

      scrollTable = new JScrollPane(theTable);
      add(scrollTable, BorderLayout.CENTER);
    }

    @SuppressWarnings("rawtypes")
    class MyTableModel extends AbstractTableModel {
      private String[] columnLocaleStrings = {"ADDRESS", "HEXREPR", "LABEL", "INSTRUCTION", "COMMENT"};
      private Class[] columnClasses = {String.class, String.class, String.class, String.class, String.class};
      private String memoryAddress[];

      MyTableModel(String[] memoryAddress) {
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
        // Column 0 is the instruction address.
        if (col == 0) {
          try {
            return Converter.binToHex(Converter.positiveIntToBin(16, row * 4));
          } catch (IrregularStringOfBitsException ex) {
            // Should never happen.
            ex.printStackTrace();
            return 0;
          }
        }

        Instruction instruction = memory.getInstruction(row * 4);
        if (instruction == null) {
            return "";
        }

        switch (col) {
          case 1:
            try {
              return instruction.getRepr().getHexString();
            } catch (IrregularStringOfBitsException ex) {
              ex.printStackTrace();
              return "";
            }

          case 2:
            return instruction.getLabel();

          case 3:
            return instruction.getFullName();

          case 4:
            if (instruction.getComment() != null) {
              return ";" + instruction.getComment();
            }
        }
        return "";
      }

      @SuppressWarnings("rawtypes")
      public Class getColumnClass(int c) {
        return columnClasses[c];
      }
    }
  }

  private class MyTableCellRenderer implements TableCellRenderer {
    private JLabel label;

    public Component getTableCellRendererComponent(JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column) {
      codePanel.tableModel = (CodePanel.MyTableModel) table.getModel();
      label = new JLabel();
      Font f = new Font("Monospaced", Font.PLAIN, 12);
      int rowTable = row;

      if (column == 0) {
        try {
          label.setText(Converter.binToHex(Converter.positiveIntToBin(16, row++ * 4)));
          label.setFont(f);
        } catch (IrregularStringOfBitsException e) {
          e.printStackTrace();
        }
      }

      if (column == 1) {
        String repr = (String) codePanel.tableModel.getValueAt(row, column);
        label.setText(repr);
        label.setFont(f);
      }

      if (column == 2) {
        label.setText((String) codePanel.tableModel.getValueAt(row, column));
        label.setFont(f);
      }

      if (column == 3) {
        String iName = (String) codePanel.tableModel.getValueAt(row, column);
        label.setText(iName);
        label.setFont(f);
      }

      if (column == 4) {
        String iComment = (String) codePanel.tableModel.getValueAt(row, column);
        label.setText(iComment);
        label.setFont(f);
      }

      if (rowTable == ifIndex) {
        label.setOpaque(true);
        label.setBackground(new Color(config.getInt("IFColor")));
      }

      if (rowTable == idIndex) {
        label.setOpaque(true);
        label.setBackground(new Color(config.getInt("IDColor")));
      }

      if (rowTable == exIndex) {
        label.setOpaque(true);
        label.setBackground(new Color(config.getInt("EXColor")));
      }

      if (rowTable == memIndex) {
        label.setOpaque(true);
        label.setBackground(new Color(config.getInt("MEMColor")));
      }

      if (rowTable == wbIndex) {
        label.setOpaque(true);
        label.setBackground(new Color(config.getInt("WBColor")));
      }

      if (rowTable == M1Index || rowTable == M2Index || rowTable == M3Index || rowTable == M4Index || rowTable == M5Index || rowTable == M6Index || rowTable == M7Index) {
        label.setOpaque(true);
        label.setBackground(new Color(config.getInt("FPMultiplierColor")));
      }

      if (rowTable == A1Index || rowTable == A2Index || rowTable == A3Index || rowTable == A4Index) {
        label.setOpaque(true);
        label.setBackground(new Color(config.getInt("FPAdderColor")));
      }

      if (rowTable == DIVIndex) {
        label.setOpaque(true);
        label.setBackground(new Color(config.getInt("FPDividerColor")));
      }

      return label;
    }
  }
}
