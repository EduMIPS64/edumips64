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
import org.gwtproject.rpc.worker.client.worker.MessagePort;
import org.gwtproject.rpc.worker.client.worker.MessageEvent;

import jsinterop.annotations.JsType;

import java.util.logging.Logger;

@JsType(namespace = "jsedumips64")
public class Worker implements EntryPoint {
  private Simulator simulator;
  private Logger logger = Logger.getLogger("worker");
  
  @Override
  public void onModuleLoad() {
    simulator = new Simulator();
    self().addMessageHandler(new MessageEvent.MessageHandler() {
      public void onMessage(MessageEvent event) {
        route(event);
      }
    });
  }
  
  private final void route(MessageEvent event) {
    info("GOT A MESSAGE");
    info(event.getData());
    // TODO: route calls to Simulator and send back messages.
  }

  private native MessagePort self() /*-{
		return $wnd;
  }-*/;

  private void info(String message) {
    logger.info("[GWT] "+ message);
  }

  private void warning(String message) {
    logger.warning("[GWT] " + message);
  }
}