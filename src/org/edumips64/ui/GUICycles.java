/* GUICycles.java
 *
 * This class draws the cycles component. It gives a representation of the timing
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

import org.edumips64.utils.Config;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.accessibility.*;
import javax.swing.*;

/** This class draws the cycles component. It gives a representation of the timing
* behaviour of the pipeline.
* @author Filippo Mondello, Massimo Trubia (FPU modifications)
*/
public class GUICycles extends GUIComponent {
  RightPanel rightPanel;
  LeftPanel leftPanel;

  JScrollPane jsp1, jsp2;
  private JSplitPane splitPane;

  Dimension dim, dim2;

  CycleBuilder builder;

  public GUICycles() {
    super();
    builder = new CycleBuilder();
    rightPanel = new RightPanel();

    jsp1 = new JScrollPane(rightPanel);
    dim = new Dimension(20, 30);
    rightPanel.setPreferredSize(dim);

    leftPanel = new LeftPanel();

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

  }

  public void setContainer(Container co) {
    super.setContainer(co);
    cont.add(splitPane);
    draw();
  }


  public synchronized void update() {
    synchronized (rightPanel) {
      synchronized (leftPanel) {
        builder.step();
      }
    }
  }

  public synchronized void draw() {
    int instrCount = builder.getInstructionsCount();
    int time = builder.getTime();

    dim.setSize(20 + time * 30, 30 + instrCount * 15);

    if (30 + instrCount * 15 > leftPanel.getHeight()) {
      dim2.setSize(splitPane.getDividerLocation(), 30 + instrCount * 15);
    } else {
      dim2.setSize(splitPane.getDividerLocation(), leftPanel.getHeight());
    }

    jsp1.getViewport().setViewSize(dim);
    jsp2.getViewport().setViewSize(dim2);
    jsp2.getViewport().setViewPosition(new Point(0, instrCount * 15));
    jsp1.getViewport().setViewPosition(new Point(time * 30, instrCount * 15));
    cont.repaint();
  }

  class RightPanel extends JPanel {

    public synchronized void paintComponent(Graphics g) {
      super.paintComponent(g);
      setBackground(Color.white);
      g.setColor(Color.black);
      Font f1 = new Font("Arial", Font.PLAIN, 11);
      FontMetrics fm1 = g.getFontMetrics(f1);
      g.setFont(f1);

      fill(g);
    }

    public synchronized void fill(Graphics g) {
      int i = 0;

      for (CycleElement el: builder.getElementsList()) {
        int j = 0;
        String pre = "IF";
        String ext_st = "";
        int elementTime = el.getTime();

        for (String st: el.getStates()) {
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

          g.fillRect(10 + (elementTime + j - 1) * 30, 9 + i * 15, 30, 13);
          g.setColor(Color.black);
          g.drawRect(10 + (elementTime + j - 1) * 30, 9 + i * 15, 30, 13);
          g.drawString(st, 15 + (elementTime + j - 1) * 30, 20 + i * 15);
          j++;

          if ((!st.equals(" ")) && (!st.equals("RAW"))) {
            pre = st;
          }
        }

        i++;
      }
    }
  }

  class LeftPanel extends JPanel {

    public synchronized void paintComponent(Graphics g) {
      super.paintComponent(g);
      setBackground(Color.white);
      g.setColor(Color.black);
      Font f1 = new Font("Arial", Font.PLAIN, 11);
      FontMetrics fm1 = g.getFontMetrics(f1);
      g.setFont(f1);
      int i = 0;

      for (CycleElement el: builder.getElementsList()) {
        g.drawString(el.getName(), 5, 20 + i * 15);
        i++;
      }
    }
  }
}
