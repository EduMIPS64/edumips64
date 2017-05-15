/* GUIUpdateThread.java
 *
 * Thread that updates the UI according to a specified frequency. Used to
 * rate-limit UI update requests from the CPUSwingWorker.
 *
 * (c) 2017 Andrea Spadaccini
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

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class GUIUpdateThread extends Thread {
  private boolean shouldUpdate = false;
  private boolean shouldTerminate = false;
  private GUIFrontend frontend;
  private long periodMs;

  GUIUpdateThread(GUIFrontend frontend) {
    this.frontend = frontend;
    // Rate-limiting to 100 updates / second.
    periodMs = 10;
  }

  // Signals to this thread that an update is necessary. The logic that handles this boolean
  // is not synchronized because it is not important to get it perfectly correct.
  void triggerUpdate() {
    shouldUpdate = true;
  }

  // Signals to this thread that it should stop executing as quickly as possible.
  void terminate() {
    shouldTerminate = true;
  }

  @Override
  public void run() {
    while(true) {
      if (shouldTerminate) {
        break;
      }
      long start = System.currentTimeMillis();
      if (shouldUpdate) {
        try {
          SwingUtilities.invokeAndWait(() -> {
            frontend.updateComponents();
            frontend.represent();
          });
          shouldUpdate = false;
        } catch (InterruptedException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
      long end = System.currentTimeMillis();
      try {
        Thread.sleep(end - start);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
