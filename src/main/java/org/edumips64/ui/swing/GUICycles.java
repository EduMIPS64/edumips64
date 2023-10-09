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

package org.edumips64.ui.swing;

import org.edumips64.core.CPU;
import org.edumips64.core.Memory;
import org.edumips64.utils.CycleBuilder;
import org.edumips64.utils.CycleElement;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;

import java.awt.*;
import javax.swing.*;

/** This class draws the cycles component. It gives a representation of the timing
* behaviour of the pipeline.
* @author Filippo Mondello, Massimo Trubia (FPU modifications)
*/
public class GUICycles extends GUIComponent {
  private static final int DY = 15;
  private static final int DX = 35;
  private CyclePanel cyclePanel;
  private JScrollPane cyclePanelSp;
  private Dimension instructionPanelDim;

  private InstructionPanel instructionPanel;
  private JScrollPane instructionPanelSp;
  private Dimension cyclePanelDim;

  private JSplitPane splitPane;
  private CycleBuilder builder;

  GUICycles(CPU cpu, Memory memory, ConfigStore config, CycleBuilder builder) {
    super(cpu, memory, config);
    this.builder = builder;

    // Initialize the left panel (list of instructions).
    instructionPanel = new InstructionPanel();
    instructionPanelSp = new JScrollPane(instructionPanel);
    instructionPanelDim = new Dimension(10, DX);
    instructionPanel.setPreferredSize(instructionPanelDim);
    instructionPanelSp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    // Initialize the right panel (list of instructions).
    cyclePanel = new CyclePanel();
    cyclePanelSp = new JScrollPane(cyclePanel);
    cyclePanelDim = new Dimension(20, DX);
    cyclePanel.setPreferredSize(cyclePanelDim);
    cyclePanelSp.setVerticalScrollBar(instructionPanelSp.getVerticalScrollBar());
    cyclePanelSp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    cyclePanelSp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    // SplitPane che contiene entrambi i pannelli.
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, instructionPanelSp, cyclePanelSp);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(150);

    // Reset the component's font. This window has a different font type and is slightly smaller than the rest of the UI.
    this.font = new Font("SansSerif", Font.PLAIN, font.getSize() - 1);

    // This component has a scaling factor of 11, since the original numbers were derived with a font size of 11.
    this.scalingDenominator = 11.0f;
  }

  @Override
  public void setContainer(Container co) {
    super.setContainer(co);
    cont.add(splitPane);
    draw();
  }

  @Override
  public void update() {
  }

  public synchronized void draw() {
    int instrCount = builder.getInstructionsCount();
    int time = builder.getTime();

    // Set CyclePanel size.
    int cyclePanelWidth = scale(20 + time * DX);
    int cyclePanelHeight = scale(30 + instrCount * DY);
    cyclePanelDim.setSize(cyclePanelWidth, cyclePanelHeight);
    cyclePanelSp.getViewport().setViewSize(cyclePanelDim);

    // Set InstrPanel size.
    int instrHeight = Math.max(cyclePanelHeight, instructionPanel.getHeight());
    instructionPanelDim.setSize(splitPane.getDividerLocation(), instrHeight);
    instructionPanelSp.getViewport().setViewSize(instructionPanelDim);

    // Set viewpoints.
    int yPoint = scale(instrCount * (DY + 20));
    int cyclePanelX = scale(time * DX);
    instructionPanelSp.getViewport().setViewPosition(new Point(0, yPoint));
    cyclePanelSp.getViewport().setViewPosition(new Point(cyclePanelX, yPoint));

    // App-triggered painting.
    cont.repaint();
  }

  class CyclePanel extends JPanel {
    private static final long serialVersionUID = 8910686437037524082L;

    @Override
    public synchronized void paint(Graphics g) {
      super.paint(g);
      setBackground(Color.darkGray);
      g.setColor(Color.white);
      g.setFont(font);

      fill(g);
    }

    private boolean shouldDraw(CycleElement el, int row, Rectangle clipBounds) {
      int elementTime = el.getTime();
      int x = scale(10 + (elementTime - 1) * DX);
      int y = scale(9 + row * DY);
      int width = el.getStates().size() * 9 * DX;
      int height = scale(DY - 2);
      Rectangle elRectangle = new Rectangle(x, y, width, height);
      return clipBounds.intersects(elRectangle);
    }

    synchronized void fill(Graphics g) {
      int row = 0;

      Rectangle clipBounds = g.getClipBounds();

      java.util.List<CycleElement> elements = builder.getElementsList();
      synchronized(elements) {
        for (CycleElement el : elements) {
          if (!el.shouldRender()) {
            continue;
          }

          // Do not draw the row if it's outside the clipping rectangle.
          if (!shouldDraw(el, row, clipBounds)) {
            row++;
            continue;
          }
          int column = 0;
          String pre = "IF";
          int elementTime = el.getTime();

          for (String st : el.getStates()) {
            Color color = getColorByState(st, pre);
            if (color != null) {
              g.setColor(color);
            }
            int x = scale(10 + (elementTime + column - 1) * DX);
            int y = scale(9 + row * DY);
            int width = scale(DX);
            int height = scale(DY - 2);

            // Draw the colored rectangle and a black outline.
            g.fillRect(x, y, width, height);
            g.setColor(Color.black);
            g.drawRect(x, y, width, height);

            // Write the stage name.
            int fontXOffset = scale(1);
            int fontYOffset = scale(11);
            g.drawString(st, x+fontXOffset, y+fontYOffset);
            column++;

            if ((!st.equals(" ")) && (!st.equals("RAW"))) {
              pre = st;
            }
          }
          row++;
        }
      }
    }

    private Color getColorByState(String st, String pre) {
      if (st.equals("IF")) {
        return new Color(config.getInt(ConfigKey.IF_COLOR));
      } else if (st.equals("ID")) {
        return new Color(config.getInt(ConfigKey.ID_COLOR));
      } else if (st.equals("EX")) {
        return new Color(config.getInt(ConfigKey.EX_COLOR));
      } else if (st.equals("MEM")) {
        return new Color(config.getInt(ConfigKey.MEM_COLOR));
      } else if (st.equals("WB")) {
        return new Color(config.getInt(ConfigKey.WB_COLOR));
      } else if (st.equals("Str")) {
        return new Color(config.getInt(ConfigKey.EX_COLOR));
      } else if (st.equals("A1") || st.equals("A2") || st.equals("A3") || st.equals("A4") || st.equals("StAdd")) {
        return new Color(config.getInt(ConfigKey.FP_ADDER_COLOR));
      } else if (st.equals("M1") || st.equals("M2") || st.equals("M3") || st.equals("M4") || st.equals("M5") || st.equals("M6") || st.equals("M7") || st.equals("StMul")) {
        return new Color(config.getInt(ConfigKey.FP_MULTIPLIER_COLOR));
      } else if (st.matches("D[0-2][0-9]") || st.matches("DIV")) {
        return new Color(config.getInt(ConfigKey.FP_DIVIDER_COLOR));
      } else if (st.equals("RAW")) {
        return new Color(config.getInt(ConfigKey.ID_COLOR));
      } else if (st.equals("WAW") || st.equals("StDiv") || st.equals("StEx") || st.equals("StFun")) {
        return new Color(config.getInt(ConfigKey.ID_COLOR));
      } else if (st.equals(" ")) {
        if (pre.equals("IF")) {
          return new Color(config.getInt(ConfigKey.IF_COLOR));
        }
      }
      return null;
    }
  }

  class InstructionPanel extends JPanel {
    private static final long serialVersionUID = 1390774892068028190L;

    @Override
    public synchronized void paint(Graphics g) {
      super.paint(g);
      setBackground(Color.darkGray);
      g.setColor(Color.white);
      g.setFont(font);

      Rectangle clip = g.getClipBounds();

      java.util.List<CycleElement> elements = builder.getElementsList();
      synchronized(elements) {
        int row = 0;
        for (CycleElement el : elements) {
          int x = scale(5);
          int y = scale(20 + row * DY);

          // Do not draw the row if it's outside the clipping rectangle.
          if (y < clip.y || y > clip.y + clip.height) {
            row++;
            continue;
          }

          g.drawString(el.getName(), x, y);
          row++;
        }
      }
    }
  }
}
