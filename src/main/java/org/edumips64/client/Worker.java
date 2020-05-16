/* WebUI.java
 *
 * GWT facade for the EduMIPS64 core.
 * (c) 2020 Andrea Spadaccini
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
package org.edumips64.client;

import com.google.gwt.core.client.EntryPoint;

import elemental2.dom.DomGlobal;
import elemental2.dom.MessageEvent;

import java.util.logging.Logger;

public class Worker implements EntryPoint {
  private Simulator simulator;
  private Logger logger = Logger.getLogger("worker");
  
  @Override
  public void onModuleLoad() {
    simulator = new Simulator();
    DomGlobal.window.addEventListener("message", (evt) -> {
      info("GOT A MESSAGE FROM JS");
      if (evt instanceof MessageEvent) {
        String data = String.valueOf(((MessageEvent) evt).data);
        info(data);
      }
    });
    DomGlobal.postMessage("GWT ready");
  }

  private void info(String message) {
    logger.info("[GWT] "+ message);
  }
}