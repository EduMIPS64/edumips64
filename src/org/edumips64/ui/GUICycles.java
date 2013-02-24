/* GUICycles.java
 *
 * This class draw the cycles component. It gives a representation of the timing
 * behaviour of the pipeline.
 * (c) 2006 Filippo Mondello, Trubia Massimo (FPU modifications)
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


import org.edumips64.Main;
import org.edumips64.utils.Config;
import org.edumips64.core.*;
import org.edumips64.core.is.*;
import java.awt.*;
import javax.swing.*;
import javax.accessibility.*;
import java.awt.event.*;
import java.util.*;

/** This class draw the cycles component. It gives a representation of the timing
* behaviour of the pipeline.
* @author Filippo Mondello, Massimo Trubia (FPU modifications)
*/
public class GUICycles extends GUIComponent {

  Panel1 rightPanel;
  Panel2 leftPanel;

  JScrollPane jsp1, jsp2;
  private JSplitPane splitPane;
  int conta, curTime, oldTime, n_instr;
  Instruction [] instr;
  int memoryStalls; // used for understanding if the EX instruction is in structural stall (memory)
  int inputStructuralStalls; // groups five stalls (EXNotAvailable, FuncUnitNotAvailable, DividerNotAvailable, RAW, WAW) in order to understand if a new instruction has to be added to "elementsList"
  int RAWStalls, WAWStalls, structStallsEX, structStallsDivider, structStallsFuncUnit;

  Map <CPU.PipeStatus, Instruction> pipeline;
  Dimension dim, dim2;

  java.util.List<ElementoCiclo> elementsList;

  public GUICycles() {
    super();
    elementsList = Collections.synchronizedList(new LinkedList<ElementoCiclo>());
    memoryStalls = cpu.getMemoryStalls();
    inputStructuralStalls = cpu.getStructuralStallsDivider() + cpu.getStructuralStallsEX() + cpu.getStructuralStallsFuncUnit();
    RAWStalls = cpu.getRAWStalls();
    WAWStalls = cpu.getWAWStalls();
    structStallsEX = cpu.getStructuralStallsEX();
    structStallsDivider = cpu.getStructuralStallsDivider();
    structStallsFuncUnit = cpu.getStructuralStallsFuncUnit();
    rightPanel = new Panel1();

    jsp1 = new JScrollPane(rightPanel);
    dim = new Dimension(20, 30);
    rightPanel.setPreferredSize(dim);

    leftPanel = new Panel2();

    jsp2 = new JScrollPane(leftPanel);
    dim2 = new Dimension(10, 30);
    leftPanel.setPreferredSize(dim2);

    jsp1.setVerticalScrollBar(jsp2.getVerticalScrollBar());

    jsp1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    jsp2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    jsp1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jsp2, jsp1);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(150);

    pipeline = new HashMap<CPU.PipeStatus, Instruction>();
    curTime = 0;
    n_instr = 0;
    oldTime = 0;
    instr = new Instruction[5];
  }

  public void setContainer(Container co) {
    super.setContainer(co);
    cont.add(splitPane);
    draw();
  }


  public synchronized void update() {
    synchronized (rightPanel) {
      synchronized (leftPanel) {
        pipeline = cpu.getPipeline();
        curTime = cpu.getCycles();

        if (oldTime != curTime) {
          if (curTime > 0) {
            int index; //used for searching instructions by serial number into "elementsList"
            instr[0] = pipeline.get(CPU.PipeStatus.IF);
            instr[1] = pipeline.get(CPU.PipeStatus.ID);
            instr[2] = pipeline.get(CPU.PipeStatus.EX);
            instr[3] = pipeline.get(CPU.PipeStatus.MEM);
            instr[4] = pipeline.get(CPU.PipeStatus.WB);

            if (instr[4] != null) {
              if (instr[4].getName() != " ") {
                index = getLastELBySerialNumber(instr[4].getSerialNumber());

                if (index != -1) {
                  elementsList.get(index).addStato("WB");
                }
              }
            }

            if (instr[3] != null) {
              if (instr[3].getName() != " ") {
                index = getLastELBySerialNumber(instr[3].getSerialNumber());

                if (index != -1) {
                  elementsList.get(index).addStato("MEM");
                }
              }
            }

            if (instr[2] != null) {
              if (instr[2].getName() != " ") {
                index = getLastELBySerialNumber(instr[2].getSerialNumber());
                //if a structural stall(memory) occurs the instruction in EX has to be tagged with "EX" and succefully with "StEx"
                boolean exTagged = false;

                if (index != -1) {
                  if (elementsList.get(index).getStato().getLast() == "ID" || elementsList.get(index).getStato().getLast() == "RAW" ||  elementsList.get(index).getStato().getLast() == "WAW" || elementsList.get(index).getStato().getLast() == "StEx") {
                    elementsList.get(index).addStato("EX");
                    exTagged = true;
                  }

                  //we check if a structural hazard  occurred if there's a difference between the previous value of memoryStall counter and the current one
                  if (memoryStalls != cpu.getMemoryStalls() && !exTagged) {
                    elementsList.get(index).addStato("Str");
                  }
                }

                exTagged = false;
              }
            }

            //if there was stalls as RAW,WAW,EXNotAvailable,DividerNotAvailable, FuncUnitNotAvailable
            //we cannot add  a new ElementoCiclo in "elementsList" and we must add tags as RAW, WAW, StEx,StDiv,StFun into the right instruction's state list

            //EX stage stalls
            boolean RAWStallOccurred = (RAWStalls != cpu.getRAWStalls());
            boolean WAWStallOccurred = (WAWStalls != cpu.getWAWStalls());
            boolean structStallEXOccurred = (structStallsEX != cpu.getStructuralStallsEX());
            boolean structStallDividerOccured = (structStallsDivider != cpu.getStructuralStallsDivider());
            boolean structStallsFuncUnitOccurred = (structStallsFuncUnit != cpu.getStructuralStallsFuncUnit());
            boolean inputStallOccurred = (inputStructuralStalls != cpu.getStructuralStallsDivider() + cpu.getStructuralStallsEX() + cpu.getStructuralStallsFuncUnit() + cpu.getRAWStalls() + cpu.getWAWStalls());

            if (instr[1] != null) {
              if (instr[1].getName() != " " && cpu.getStatus() == CPU.CPUStatus.RUNNING) {
                index = getLastELBySerialNumber(instr[1].getSerialNumber());

                if (!inputStallOccurred) {
                  elementsList.get(index).addStato("ID");
                }

                if (RAWStallOccurred) {
                  elementsList.get(index).addStato("RAW");
                }

                if (WAWStallOccurred) {
                  elementsList.get(index).addStato("WAW");
                }

                if (structStallDividerOccured) {
                  elementsList.get(index).addStato("StDiv");
                }

                if (structStallEXOccurred) {
                  elementsList.get(index).addStato("StEx");
                }

                if (structStallsFuncUnitOccurred) {
                  elementsList.get(index).addStato("StFun");
                }
              }

            }

            if (instr[0] != null) {
              if (!inputStallOccurred) {
                //we must instantiate a new ElementoCiclo only if the CPU is running or there was a JumpException and the the IF instruction was changed
                if (cpu.getStatus() == CPU.CPUStatus.RUNNING) {
                  elementsList.add(new ElementoCiclo(instr[0].getFullName(), curTime, instr[0].getSerialNumber()));
                  n_instr++;
                }
              } else {
                index = getLastELBySerialNumber(instr[0].getSerialNumber());

                if (index != -1) {
                  elementsList.get(index).addStato(" ");
                }
              }
            }

            //we have to check instructions in the FP pipeline
            //ADDER -------------------------------------------------
            String stage;
            Instruction instrSearched;

            if (cpu.getInstructionByFuncUnit("ADDER", 1) != null) {
              index = getLastELBySerialNumber(cpu.getInstructionByFuncUnit("ADDER", 1).getSerialNumber());

              if (index != -1) {
                elementsList.get(index).addStato("A1");
              }
            }

            if (cpu.getInstructionByFuncUnit("ADDER", 2) != null) {

              index = getLastELBySerialNumber(cpu.getInstructionByFuncUnit("ADDER", 2).getSerialNumber());

              if (index != -1) {
                elementsList.get(index).addStato("A2");
              }
            }

            if (cpu.getInstructionByFuncUnit("ADDER", 3) != null) {

              index = getLastELBySerialNumber(cpu.getInstructionByFuncUnit("ADDER", 3).getSerialNumber());

              if (index != -1) {
                elementsList.get(index).addStato("A3");
              }
            }

            if (cpu.getInstructionByFuncUnit("ADDER", 4) != null) {

              index = getLastELBySerialNumber(cpu.getInstructionByFuncUnit("ADDER", 4).getSerialNumber());
              boolean A4tagged = false;

              if (index != -1) {
                if (elementsList.get(index).getStato().getLast() == "A3") {
                  elementsList.get(index).addStato("A4");
                  A4tagged = true;
                }

                //we have to check if a structural hazard  occurred and it involved the divider or the multiplier (it is sufficient to control if the "A4" o "StAdd" tag was added to the instruction
                if (!A4tagged && (elementsList.get(index).getStato().getLast() == "A4" || elementsList.get(index).getStato().getLast() == "StAdd")) {
                  elementsList.get(index).addStato("StAdd");
                }
              }

              A4tagged = false;
            }

            //MULTIPLIER ----------------------------------------------------------------
            if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 1)) != null) {
              index = getLastELBySerialNumber(instrSearched.getSerialNumber());

              if (index != -1) {
                elementsList.get(index).addStato("M1");
              }
            }

            if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 2)) != null) {
              index = getLastELBySerialNumber(instrSearched.getSerialNumber());

              if (index != -1) {
                elementsList.get(index).addStato("M2");
              }
            }

            if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 3)) != null) {
              index = getLastELBySerialNumber(instrSearched.getSerialNumber());

              if (index != -1) {
                elementsList.get(index).addStato("M3");
              }
            }

            if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 4)) != null) {
              index = getLastELBySerialNumber(instrSearched.getSerialNumber());

              if (index != -1) {
                elementsList.get(index).addStato("M4");
              }
            }

            if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 5)) != null) {
              index = getLastELBySerialNumber(instrSearched.getSerialNumber());

              if (index != -1) {
                elementsList.get(index).addStato("M5");
              }
            }

            if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 6)) != null) {
              index = getLastELBySerialNumber(instrSearched.getSerialNumber());

              if (index != -1) {
                elementsList.get(index).addStato("M6");
              }
            }

            if ((instrSearched = cpu.getInstructionByFuncUnit("MULTIPLIER", 7)) != null) {

              index = getLastELBySerialNumber(instrSearched.getSerialNumber());
              boolean M7tagged = false;

              if (index != -1) {
                if (elementsList.get(index).getStato().getLast() == "M6") {
                  elementsList.get(index).addStato("M7");
                  M7tagged = true;
                }

                //we check if a structural hazard  occurred and involved the divider
                if (!M7tagged && (elementsList.get(index).getStato().getLast() == "M7" || elementsList.get(index).getStato().getLast() == "StMul")) {
                  elementsList.get(index).addStato("StMul");
                }
              }

              M7tagged = false;
            }

            //DIVIDER ------------------------------------------------------
            if ((instrSearched = cpu.getInstructionByFuncUnit("DIVIDER", 0)) != null) {
              boolean DIVtagged = false;
              index = getLastELBySerialNumber(instrSearched.getSerialNumber());
              stage = elementsList.get(index).getStato().getLast();

              if (index != -1) {
                if (stage != "DIV" && !stage.matches("D[0-2][0-9]")) {
                  elementsList.get(index).addStato("DIV");
                  DIVtagged = true;
                }

                if (!DIVtagged) {
                  int divCount = cpu.getDividerCounter();
                  String divCountStr = String.valueOf(divCount);  //divCount in the format DXX (XX belongs to [00  24])
                  elementsList.get(index).addStato((divCount < 10) ? "D0" + divCountStr : "D" + divCountStr);
                }
              }

              DIVtagged = false;
            }
          } else {
            elementsList.clear();
            oldTime = 0;
            n_instr = 0;
          }

          oldTime = curTime;
        }

        memoryStalls = cpu.getMemoryStalls();
        inputStructuralStalls = cpu.getStructuralStallsDivider() + cpu.getStructuralStallsEX() + cpu.getStructuralStallsFuncUnit() + cpu.getRAWStalls() + cpu.getWAWStalls();
        RAWStalls = cpu.getRAWStalls();
        WAWStalls = cpu.getWAWStalls();
        structStallsEX = cpu.getStructuralStallsEX();
        structStallsDivider = cpu.getStructuralStallsDivider();
        structStallsFuncUnit = cpu.getStructuralStallsFuncUnit();
      }
    }
  }

  public int getLastELBySerialNumber(long serialNumber) {
    for (ListIterator<ElementoCiclo> it = elementsList.listIterator(elementsList.size()); it.hasPrevious();) {
      if (it.previous().getSerialNumber() == serialNumber) {
        return it.previousIndex() + 1;
      }
    }

    return -1;
  }

  public synchronized void draw() {

    dim.setSize(20 + curTime * 30, 30 + n_instr * 15);

    if (30 + n_instr * 15 > leftPanel.getHeight()) {
      dim2.setSize(splitPane.getDividerLocation(), 30 + n_instr * 15);
    } else {
      dim2.setSize(splitPane.getDividerLocation(), leftPanel.getHeight());
    }

    /*
    jsp1.setViewportView(rightPanel);
    Main.logger.debug("altezza" + rightPanel.getBounds().height);
    Main.logger.debug("larghezza " +rightPanel.getBounds().width);
    jsp1.getViewport().setViewPosition(new Point(rightPanel.getBounds().width,rightPanel.getBounds().height));
    */
    jsp1.getViewport().setViewSize(dim);
    jsp2.getViewport().setViewSize(dim2);
    jsp2.getViewport().setViewPosition(new Point(0, n_instr * 15));
    jsp1.getViewport().setViewPosition(new Point(curTime * 30, n_instr * 15));

    cont.repaint();
  }


  class Panel1 extends JPanel {

    public synchronized void paintComponent(Graphics g) {
      super.paintComponent(g);  // va fatto sempre
      setBackground(Color.white);  // fondo bianco

      g.setColor(Color.black);

      Font f1 = new Font("Arial", Font.PLAIN, 11);
      FontMetrics fm1 = g.getFontMetrics(f1);
      g.setFont(f1);

      fill(g);
    }


    public synchronized void fill(Graphics g) {
      int i = 0;

      for (ElementoCiclo el: elementsList) {
        int j = 0;
        String pre = "IF";
        String ext_st = "";
        int curTime = el.getTime();

        for (String st: el.getStato()) {

          ext_st = "";

          if (st.equals("IF")) {
            g.setColor(Config.getColor("IFColor"));
          } else if (st.equals("ID")) {
            g.setColor(Config.getColor("IDColor"));
          } else if (st.equals("EX")) {
            g.setColor(Config.getColor("EXColor"));
          } else if (st.equals("MEM")) {
            g.setColor(Config.getColor("MEMColor"));
          } else if (st.equals("WB")) {
            g.setColor(Config.getColor("WBColor"));
          } else if (st.equals("Str")) {
            g.setColor(Config.getColor("EXColor"));
          } else if (st.equals("A1") || st.equals("A2") || st.equals("A3") || st.equals("A4") || st.equals("StAdd")) {
            g.setColor(Config.getColor("FPAdderColor"));
          } else if (st.equals("M1") || st.equals("M2") || st.equals("M3") || st.equals("M4") || st.equals("M5") || st.equals("M6") || st.equals("M7") || st.equals("StMul")) {
            g.setColor(Config.getColor("FPMultiplierColor"));
          } else if (st.matches("D[0-2][0-9]") || st.matches("DIV")) {
            g.setColor(Config.getColor("FPDividerColor"));
          } else if (st.equals("RAW")) {
            g.setColor(Config.getColor("IDColor"));
          } else if (st.equals("WAW") || st.equals("StDiv") || st.equals("StEx") || st.equals("StFun")) {
            g.setColor(Config.getColor("IDColor"));
          } else if (st.equals(" ")) {
            if (pre.equals("IF")) {
              ext_st = " ";
              g.setColor(Config.getColor("IFColor"));
            }
          }

          g.fillRect(10 + (curTime + j - 1) * 30, 9 + i * 15, 30, 13);
          g.setColor(Color.black);
          g.drawRect(10 + (curTime + j - 1) * 30, 9 + i * 15, 30, 13);
          g.drawString(st, 15 + (curTime + j - 1) * 30, 20 + i * 15);
          j++;

          if ((!st.equals(" ")) && (!st.equals("RAW"))) {
            pre = st;
          }
        }

        i++;
      }
    }
  }


  class Panel2 extends JPanel {

    public synchronized void paintComponent(Graphics g) {
      super.paintComponent(g);  // va fatto sempre
      setBackground(Color.white);  // fondo bianco

      g.setColor(Color.black);


      Font f1 = new Font("Arial", Font.PLAIN, 11);
      FontMetrics fm1 = g.getFontMetrics(f1);
      g.setFont(f1);

      int i = 0;

      for (ElementoCiclo el: elementsList) {
        g.drawString(el.getName(), 5, 20 + i * 15);
        i++;
      }
    }
  }

}
