/* GUIPipeline.java
 *
 * This class draw the pipeline, composed of five stages of the MIPS64 processor
 * and the units for floating point operations.
 * (c) 2006 Filippo Mondello
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
import org.edumips64.core.Pipeline;
import org.edumips64.core.is.InstructionInterface;
import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;

import java.awt.*;
import javax.swing.*;
import java.util.*;

/**
* This class draw the pipeline, composed of five stages of the MIPS64 processor
* and the units for floating point operations.
* @author Filippo Mondello
*/
class GUIPipeline extends GUIComponent {
  private Pannello1 pannello;

  private int numMultiplier;
  private int numAdder;

  private Map <Pipeline.Stage, InstructionInterface> pipeline;

  GUIPipeline(CPU cpu, Memory memory, ConfigStore config) {
    super(cpu, memory, config);
    numMultiplier = 7;
    numAdder = 4;
    pannello = new Pannello1();
    pipeline = new HashMap<>();
  }

  public void setContainer(Container co) {
    super.setContainer(co);
    cont.add(pannello);
    draw();
  }

  @Override
  public void update() {
    pipeline = cpu.getPipeline();
  }

  public void draw() {
    cont.repaint();
  }

  private int dimCar;

  private class Pannello1 extends JPanel {
    private static final long serialVersionUID = -1873304516301831571L;
    int alt, largh;

    public void paintComponent(Graphics g) {
      super.paintComponent(g);  // va fatto sempre
      setBackground(Color.darkGray);  // fondo bianco

      largh = this.getWidth();
      alt = this.getHeight();
      riempiBlocchi(g);

      g.setColor(Color.white);
      //Blocco IF
      g.drawRect(largh / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);

      g.drawLine(largh * 3 / 20, alt / 2, largh * 4 / 20, alt / 2);
      //Blocco ID
      g.drawRect(largh * 4 / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);

      //Blocco MEM
      g.drawRect(largh * 14 / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);

      g.drawLine(largh * 16 / 20, alt / 2, largh * 17 / 20, alt / 2);
      //Blocco WB
      g.drawRect(largh * 17 / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);

      //Blocco EX
      g.drawRect(largh * 9 / 20, (alt / 2) - (alt * 5 / 12), largh / 10, alt / 6);

      //Blocco FP-DIV 0
      g.drawRect(largh * 8 / 20, (alt / 2) + (alt * 3 / 12), largh * 2 / 10, alt / 6);

      //IF---FPDIV0
      g.drawLine(largh * 6 / 20, alt / 2 + alt / 20, largh * 8 / 20, alt / 2 + alt / 3);
      //FPDIV0---MEM
      g.drawLine(largh * 12 / 20, alt / 2 + alt / 3, largh * 14 / 20, (alt / 2) + (alt / 20));

      //IF---EX
      g.drawLine(largh * 6 / 20, alt / 2 - alt / 20, largh * 9 / 20, alt / 2 - alt / 3);
      //EX---MEM
      g.drawLine(largh * 11 / 20, alt / 2 - alt / 3, largh * 14 / 20, (alt / 2) - (alt / 20));

//MULTIPLIER
      int spiazzMul = (largh * 20 / 60) / numMultiplier;

      for (int j = 0; j < numMultiplier; j++) {
        g.drawRect((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)), (alt / 2) - (alt / 10), spiazzMul * 5 / 8, alt / 10);
      }

      for (int j = 0; j < numMultiplier - 1; j++) {
        //FP MULTIPLIER(j)---FP MULTIPLIER(j+1)
        g.drawLine((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)) + spiazzMul * 5 / 8, alt / 2 - alt / 20, (largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)) + spiazzMul, alt / 2 - alt / 20);
      }

      //IF---FP MULTIPLIER1
      g.drawLine(largh * 6 / 20, alt / 2 - alt / 30, (largh * 20 / 60) + (largh / (10 * numMultiplier)), alt / 2 - alt / 20);
      //MEM---FP MULTIPLIER7
      g.drawLine((largh * 20 / 60) + ((numMultiplier - 1) * spiazzMul) + (largh / (10 * numMultiplier)) + spiazzMul * 5 / 8, alt / 2 - alt / 20, largh * 14 / 20, alt / 2 - alt / 30);


      int spiazzAdd = (largh * 20 / 60) / numAdder;

      for (int j = 0; j < numAdder; j++) {
        g.drawRect((largh * 20 / 60) + (j * spiazzAdd) + (largh / (10 * numAdder)), (alt / 2) + (alt / 40), spiazzAdd * 5 / 8, alt / 10);
      }

      for (int j = 0; j < numAdder - 1; j++) {
        //FP ADDER(j)---FP ADDER(j+1)
        g.drawLine((largh * 20 / 60) + (j * spiazzAdd) + (largh / (10 * numAdder)) + spiazzAdd * 5 / 8, alt / 2 + alt / 40 + alt / 20, (largh * 20 / 60) + (j * spiazzAdd) + (largh / (10 * numAdder)) + spiazzAdd, alt / 2 + alt / 40 + alt / 20);
      }

      //IF---FP ADDER1
      g.drawLine(largh * 6 / 20, alt / 2 + alt / 30, (largh * 20 / 60) + (largh / (10 * numAdder)), alt / 2 + alt / 40 + alt / 20);
      //MEM---FP ADDER4
      g.drawLine((largh * 20 / 60) + ((numAdder - 1) * spiazzAdd) + (largh / (10 * numAdder)) + spiazzAdd * 5 / 8, alt / 2 + alt / 40 + alt / 20, largh * 14 / 20, alt / 2 + alt / 30);


      if (largh / 30 < alt / 15) {
        dimCar = largh / 30;
      } else {
        dimCar = alt / 15;
      }

      Font f1 = new Font("SansSerif", Font.PLAIN, dimCar);
      g.getFontMetrics(f1);
      g.setFont(f1);
      g.setColor(Color.black);
      //Stringhe all'interno dei blocchi
      g.drawString("IF", largh * 17 / 200, (alt / 2));
      g.drawString("ID", largh * 47 / 200, (alt / 2));
      g.drawString("MEM", largh * 142 / 200, (alt / 2));
      g.drawString("WB", largh * 175 / 200, (alt / 2));
      g.drawString("EX", largh * 97 / 200, (alt / 2) - (alt * 40 / 120));
      g.drawString("FP-DIV " + cpu.getDividerCounter(), largh * 87 / 200, (alt / 2) + (alt * 40 / 120));

      g.setColor(Color.white);
      g.drawString("FP Multiplier", largh * 85 / 200, (alt / 2) - (alt * 15 / 120));
      g.drawString("FP Adder", largh * 85 / 200, (alt / 2) + (alt * 23 / 120));

      riempiStringhe(g);

    }

    /*Per ogni blocco della pipeline, all'interno di questa funzione dovrÃ² controllare se c'Ãš
    qualche istruzione, e in tal caso colorare il blocco e scrivere il nome dell'istruzione
    */
    void riempiBlocchi(Graphics g) {
      g.setColor(Color.white);
      g.fillRect(largh / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);
      g.fillRect(largh * 4 / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);
      g.fillRect(largh * 14 / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);
      g.fillRect(largh * 9 / 20, (alt / 2) - (alt * 5 / 12), largh / 10, alt / 6);
      g.fillRect(largh * 17 / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);

      //Filling FPU elements
      //divider
      g.fillRect(largh * 8 / 20, (alt / 2) + (alt * 3 / 12), largh * 2 / 10, alt / 6);
      //multiplier
      int spiazzMul = (largh * 20 / 60) / numMultiplier;

      for (int j = 0; j < numMultiplier; j++) {
        g.fillRect((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)), (alt / 2) - (alt / 10), spiazzMul * 5 / 8, alt / 10);
      }

      //adder
      int spiazzAdd = (largh * 20 / 60) / numAdder;

      for (int j = 0; j < numAdder; j++) {
        g.fillRect((largh * 20 / 60) + (j * spiazzAdd) + (largh / (10 * numAdder)), (alt / 2) + (alt / 40), spiazzAdd * 5 / 8, alt / 10);
      }



      InstructionInterface i = pipeline.get(Pipeline.Stage.IF);

      if ((i != null) && ((i.getName() != null)) && !i.isBubble()) {
        g.setColor(new Color(config.getInt(ConfigKey.IF_COLOR)));
        g.fillRect(largh / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);
      }

      i = pipeline.get(Pipeline.Stage.ID);

      if ((i != null) && ((i.getName() != null)) && !i.isBubble()) {
        g.setColor(new Color(config.getInt(ConfigKey.ID_COLOR)));
        g.fillRect(largh * 4 / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);
      }

      i = pipeline.get(Pipeline.Stage.EX);

      if ((i != null) && ((i.getName() != null)) && !i.isBubble()) {
        g.setColor(new Color(config.getInt(ConfigKey.EX_COLOR)));
        g.fillRect(largh * 9 / 20, (alt / 2) - (alt * 5 / 12), largh / 10, alt / 6);
      }

      i = pipeline.get(Pipeline.Stage.MEM);

      if ((i != null) && ((i.getName() != null)) && !i.isBubble()) {
        g.setColor(new Color(config.getInt(ConfigKey.MEM_COLOR)));
        g.fillRect(largh * 14 / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);
      }

      i = pipeline.get(Pipeline.Stage.WB);

      if ((i != null) && ((i.getName() != null)) && !i.isBubble()) {
        g.setColor(new Color(config.getInt(ConfigKey.WB_COLOR)));
        g.fillRect(largh * 17 / 20, (alt / 2) - (alt / 12), largh / 10, alt / 6);
      }


      //filling FPU elements
      //ADDER
      g.setColor(new Color(config.getInt(ConfigKey.FP_ADDER_COLOR)));
      spiazzAdd = (largh * 20 / 60) / numAdder;
      int j;

      if (cpu.isFuncUnitFilled("ADDER", 1)) {
        j = 0;
        g.fillRect((largh * 20 / 60) + (j * spiazzAdd) + (largh / (10 * numAdder)), (alt / 2) + (alt / 40), spiazzAdd * 5 / 8, alt / 10);
      }

      if (cpu.isFuncUnitFilled("ADDER", 2)) {
        j = 1;
        g.fillRect((largh * 20 / 60) + (j * spiazzAdd) + (largh / (10 * numAdder)), (alt / 2) + (alt / 40), spiazzAdd * 5 / 8, alt / 10);
      }

      if (cpu.isFuncUnitFilled("ADDER", 3)) {
        j = 2;
        g.fillRect((largh * 20 / 60) + (j * spiazzAdd) + (largh / (10 * numAdder)), (alt / 2) + (alt / 40), spiazzAdd * 5 / 8, alt / 10);
      }

      if (cpu.isFuncUnitFilled("ADDER", 4)) {
        j = 3;
        g.fillRect((largh * 20 / 60) + (j * spiazzAdd) + (largh / (10 * numAdder)), (alt / 2) + (alt / 40), spiazzAdd * 5 / 8, alt / 10);
      }

//MULTIPLIER
      g.setColor(new Color(config.getInt(ConfigKey.FP_MULTIPLIER_COLOR)));
      spiazzMul = (largh * 20 / 60) / numMultiplier;

      if (cpu.isFuncUnitFilled("MULTIPLIER", 1)) {
        j = 0;
        g.fillRect((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)), (alt / 2) - (alt / 10), spiazzMul * 5 / 8, alt / 10);
      }

      if (cpu.isFuncUnitFilled("MULTIPLIER", 2)) {
        j = 1;
        g.fillRect((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)), (alt / 2) - (alt / 10), spiazzMul * 5 / 8, alt / 10);
      }

      if (cpu.isFuncUnitFilled("MULTIPLIER", 3)) {
        j = 2;
        g.fillRect((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)), (alt / 2) - (alt / 10), spiazzMul * 5 / 8, alt / 10);
      }

      if (cpu.isFuncUnitFilled("MULTIPLIER", 4)) {
        j = 3;
        g.fillRect((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)), (alt / 2) - (alt / 10), spiazzMul * 5 / 8, alt / 10);
      }

      if (cpu.isFuncUnitFilled("MULTIPLIER", 5)) {
        j = 4;
        g.fillRect((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)), (alt / 2) - (alt / 10), spiazzMul * 5 / 8, alt / 10);
      }

      if (cpu.isFuncUnitFilled("MULTIPLIER", 6)) {
        j = 5;
        g.fillRect((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)), (alt / 2) - (alt / 10), spiazzMul * 5 / 8, alt / 10);
      }

      if (cpu.isFuncUnitFilled("MULTIPLIER", 7)) {
        j = 6;
        g.fillRect((largh * 20 / 60) + (j * spiazzMul) + (largh / (10 * numMultiplier)), (alt / 2) - (alt / 10), spiazzMul * 5 / 8, alt / 10);
      }

      //DIVIDER
      g.setColor(new Color(config.getInt(ConfigKey.FP_DIVIDER_COLOR)));

      if (cpu.isFuncUnitFilled("DIVIDER", 0)) {
        g.fillRect(largh * 8 / 20, (alt / 2) + (alt * 3 / 12), largh * 2 / 10, alt / 6);
      }
    }

    /**
    *Questo metodo stampa all'interno di ogni blocco della pipeline il nome dell'istruzione che viÃš all'interno.
    */
    void riempiStringhe(Graphics g) {
      Font f1 = new Font("SansSerif", Font.PLAIN, dimCar - 3);
      g.getFontMetrics(f1);
      g.setFont(f1);
      g.setColor(Color.blue);
      InstructionInterface i = pipeline.get(Pipeline.Stage.IF);

      if (i != null) {
        if (i.getName() != null && !i.getName().equals(" ")) {
          g.drawString(i.getName(), (largh / 20) + 5, (alt / 2) + (alt * 8 / 120));
        }
      }

      i = pipeline.get(Pipeline.Stage.ID);

      if (i != null) {
        if (i.getName() != null && !i.getName().equals(" ")) {
          g.drawString(i.getName(), (largh * 4 / 20) + 5, (alt / 2) + (alt * 8 / 120));
        }
      }

      i = pipeline.get(Pipeline.Stage.EX);

      if (i != null) {
        if (i.getName() != null && !i.getName().equals(" ")) {
          g.drawString(i.getName(), (largh * 9 / 20) + 5, (alt / 2) - (alt * 32 / 120));
        }
      }

      i = pipeline.get(Pipeline.Stage.MEM);

      if (i != null) {
        if (i.getName() != null && !i.getName().equals(" ")) {
          g.drawString(i.getName(), (largh * 14 / 20) + 5, (alt / 2) + (alt * 8 / 120));
        }
      }

      i = pipeline.get(Pipeline.Stage.WB);

      if (i != null) {
        if (i.getName() != null && !i.getName().equals(" ")) {
          g.drawString(i.getName(), (largh * 17 / 20) + 5, (alt / 2) + (alt * 8 / 120));
        }
      }
    }

  }
}
