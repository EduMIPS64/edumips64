/* Worker.java
 *
 * Implements the Web Worker protocol and allows JS code to call Simulator
 * methods.
 * 
 * It expects messages in the form of {"method": NAME_OF_METHOD}, and in case
 * of methods with parameters the additional methods must be contained in the
 * dictionary, having key names equal to the parameter names.
 * 
 * Methods don't return anything, but instead the worker will always send back
 * a message containing a Result object, which contains the results of the method
 * and the current state of the simulator.
 * 
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

import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.logging.Logger;

public class Worker implements EntryPoint {
  private Simulator simulator;
  private Logger logger = Logger.getLogger("worker");
  
  @Override
  public void onModuleLoad() {
    simulator = new Simulator();
    DomGlobal.window.addEventListener("message", (evt) -> {
      info("Got message from the UI");
      if (evt instanceof MessageEvent<?>) {
        MessageEvent<JsPropertyMap<Object>> message = Js.cast(evt);
        JsPropertyMap<Object> data = message.data;
        info(data.toString());

        String method = data.getAsAny("method").asString().toLowerCase();
        switch(method) {
          case "reset":
            DomGlobal.postMessage(simulator.reset());
            break;
          case "step":
            int steps = data.getAsAny("steps").asInt();
            info("steps: " + steps);
            DomGlobal.postMessage(simulator.step(steps));
            break;
          case "runall":
            // TODO: implement runAll on the client side, and have the GWT side
            // send status every N steps executed.
            DomGlobal.postMessage(simulator.runAll());
            break;
          case "load":
            String code = data.getAsAny("code").asString();
            Result parseResult = simulator.loadProgram(code);
            if (!parseResult.success) {
              DomGlobal.postMessage(parseResult);
            } else {
              DomGlobal.postMessage(simulator.step(1));
            }
            break;
          default:
            info("UNKNOWN METHOD: " + method);
        }
      }
    });
  }

  private void info(String message) {
    logger.info("[GWT] "+ message);
  }
}