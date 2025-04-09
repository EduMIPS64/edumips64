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

package org.edumips64.ui.swing;

import org.edumips64.core.CPU;
import org.edumips64.core.CacheSimulator;
import org.edumips64.core.Memory;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.*;

/**
* This class shows the statistics
*/

public class GUIStatistics extends GUIComponent {

  StatPanel statPanel;
  JScrollPane jsp;
  private int nCycles, nInstructions, rawStalls, codeSize, WAWStalls, dividerStalls, memoryStalls;

  private int L1I_reads, L1I_reads_misses, L1D_reads, L1D_reads_misses, L1D_writes, L1D_writes_misses;

  private float cpi;
  GUITheme theme;

  GUIStatistics(CPU cpu, Memory memory, CacheSimulator cachesim, ConfigStore config, GUITheme theme) {
    super(cpu, memory, cachesim, config);
    this.theme = theme;
    statPanel = new StatPanel();
    jsp = new JScrollPane(statPanel);
    jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

  }

  class StatPanel extends JPanel {
    private static final long serialVersionUID = 81105844698513914L;
    JList<? extends String> statList;
    String [] statistics = {" Execution", " 0 Cycles", " 0 Instructions", " ", " Stalls", " 0 RAW Stalls", " 0 WAW Stalls",
                            " 0 Structural Stalls(Divider not available)", "0 Structural Stalls (Memory not available)",
                            " Code Size", " 0 Bytes", "FPU info", "FCSR", "FCSRGroups", "FCSRMnemonics", "FCSRValues",
                            " L1 Cache Stats", " 0 L1I Reads", " 0 L1I Read Misses",
                            " 0 L1D Reads", " 0 L1D Read Misses", " 0 L1D Writes", " 0 L1D Write Misses"
                           };
    StatPanel() {
      super();
      setLayout(new BorderLayout());
      setBackground(theme.getBackgroundColor());
      statList = new JList<String>(statistics);
      statList.setFixedCellWidth(scale(400)) ;
      statList.setCellRenderer(new MyListCellRenderer());
      add(statList, BorderLayout.WEST);
    }
  }

  public void setContainer(Container co) {
    super.setContainer(co);
    cont.add(jsp);
  }

  public void update() {
    nCycles = cpu.getCycles();
    nInstructions = cpu.getInstructions();

    if (nInstructions > 0) {
      cpi = (float) nCycles / (float) nInstructions;
    }

    rawStalls = cpu.getRAWStalls();
    codeSize = (memory.getInstructionsNumber()) * 4;
    WAWStalls = cpu.getWAWStalls();
    dividerStalls = cpu.getStructuralStallsDivider();
    memoryStalls = cpu.getStructuralStallsMemory();
    L1I_reads = (int) cachesim.getL1InstructionCache().getStats().getReadAccesses();
    L1I_reads_misses = (int) cachesim.getL1InstructionCache().getStats().getReadMisses();
    L1D_reads = (int) cachesim.getL1DataCache().getStats().getReadAccesses();
    L1D_reads_misses = (int) cachesim.getL1DataCache().getStats().getReadMisses();
    L1D_writes = (int) cachesim.getL1DataCache().getStats().getWriteAccesses();
    L1D_writes_misses = (int) cachesim.getL1DataCache().getStats().getWriteMisses();

  }

  public void draw() {
    cont.repaint();
  }

  class MyListCellRenderer implements ListCellRenderer<String> {

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
      statPanel.statList = list;
      JLabel label = new JLabel();
      label.setFont(font);

      switch (index) {
      case 0:
        label.setText(" " + CurrentLocale.getString("EXECUTION"));
        label.setForeground(Color.red);
        return label;
      case 1:

        if (nCycles != 1) {
          label.setText(" " + nCycles + " " + CurrentLocale.getString("CYCLES"));
        } else {
          label.setText(" " + nCycles + " " + CurrentLocale.getString("CYCLE"));
        }

        return label;
      case 2:

        if (nInstructions != 1) {
          label.setText(" " + nInstructions + " " + CurrentLocale.getString("INSTRUCTIONS"));
        } else {
          label.setText(" " + nInstructions + " " + CurrentLocale.getString("INSTRUCTION"));
        }

        return label;
      case 3:

        if (nInstructions > 0) {
          String floatNumber = Float.toString(cpi);

          if (floatNumber.length() > 5) {
            floatNumber = floatNumber.substring(0, 5);
          }

          label.setText(" " + floatNumber + " " + CurrentLocale.getString("CPI"));
          return label;
        } else {
          label.setText(" ");
        }
        return label;
      case 4:
        label.setText(" " + CurrentLocale.getString("STALLS"));
        label.setForeground(Color.red);
        return label;
      case 5:

        if (rawStalls != 1) {
          label.setText(" " + rawStalls + " " + CurrentLocale.getString("RAWS"));
        } else {
          label.setText(" " + rawStalls + " " + CurrentLocale.getString("RAW"));
        }
        return label;
      case 6:
        label.setText(" " + WAWStalls + " " + CurrentLocale.getString("WAWS"));
        return label;
      case 7:
        label.setText(" " + dividerStalls + " " + CurrentLocale.getString("STRUCTS_DIVNOTAVAILABLE"));
        return label;
      case 8:
        label.setText(" " + memoryStalls  + " " + CurrentLocale.getString("STRUCTS_MEMNOTAVAILABLE"));
        return label;
      case 9:
        label.setText(" " + CurrentLocale.getString("CSIZE"));
        label.setForeground(Color.red);
        return label;
      case 10:
        label.setText(" " + codeSize + " " + CurrentLocale.getString("BYTES"));
        return label;
      case 11:
        label.setText(" " + CurrentLocale.getString("FPUINFO"));
        label.setForeground(Color.red);
        return label;
      case 12:
        label.setText(" " + CurrentLocale.getString("FPUFCSR"));
        return label;
      case 13:
        label.setText(" " + "    FCC       Cause EnablFlag RM");
        return label;
      case 14:
        label.setText(" " + "7654321 0      VZOUIVZOUIVZOUI");
        return label;
      case 15:
        label.setText(" " + cpu.getFCSR().getBinString());
        return label;
      case 16:
        label.setText(" " + CurrentLocale.getString("L1CACHESTATS"));
        label.setForeground(Color.red);
        return label;
      case 17:
        label.setText(" " + L1I_reads + " " + CurrentLocale.getString("L1I READS"));
        return label;
      case 18:
        label.setText(" " + L1I_reads_misses + " " + CurrentLocale.getString("L1I READ MISSES"));
        return label;
      case 19:
        label.setText(" " + L1D_reads + " " + CurrentLocale.getString("L1D READS"));
        return label;
      case 20:
        label.setText(" " + L1D_reads_misses + " " + CurrentLocale.getString("L1D READ MISSES"));
        return label;
      case 21:
        label.setText(" " + L1D_writes + " " + CurrentLocale.getString("L1D WRITES"));
        return label;
      case 22:
        label.setText(" " + L1D_writes_misses + " " + CurrentLocale.getString("L1D WRITE MISSES"));
        return label;
      }

      return label;
    }
  }
}
