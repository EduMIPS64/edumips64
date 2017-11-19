/* StatusBar.java
 *
 * Status Bar for EduMIPS64
 * (c) 2006 Andrea Spadaccini
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

import org.edumips64.utils.CurrentLocale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.Component;
import java.awt.Dimension;

/** StatusBar class
 *  @author Andrea Spadaccini
 */

public class StatusBar {
  private JLabel statusLabel;
  private JProgressBar pb;
  private Component sbComponent;

  public StatusBar(String version) {
    statusLabel = new JLabel();
    statusLabel.setText(CurrentLocale.getString("StatusBar.WELCOME") + " " + version);

    pb = new JProgressBar(0, 10);
    pb.setMaximumSize(new Dimension(60, 10));
    //pb.setIndeterminate(true);

    //statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createLoweredBevelBorder());
    box.add(statusLabel);
    box.add(Box.createHorizontalGlue());
    box.add(pb);

    //sbComponent = statusLabel;
    sbComponent = box;
  }

  public Component getComponent() {
    return sbComponent;
  }

  public void startPB() {
    pb.setIndeterminate(true);
  }

  public void stopPB() {
    pb.setIndeterminate(false);
  }

  public void setText(String text) {
    statusLabel.setText(text);
  }
}

