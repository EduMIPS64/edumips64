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

  // A separate Simulator instance, used only for syntax checking.
  // This is not great, but given how tightly coupled the Parser class is
  // with the rest of EduMIPS64 core, it's best to just keep another instance
  // of Simulator just for the parsing.
  private Simulator parsingSimulator;
  private Logger logger = Logger.getLogger("worker");
  
  @Override
  public void onModuleLoad() {
    simulator = new Simulator();
    parsingSimulator = new Simulator();

    DomGlobal.window.addEventListener("message", (evt) -> {
      info("Got message from the UI");
      if (evt instanceof MessageEvent<?>) {
        MessageEvent<JsPropertyMap<Object>> message = Js.cast(evt);
        JsPropertyMap<Object> data = message.data;
        info(data.toString());

        String method = data.getAsAny("method").asString().toLowerCase();
        info("Running worker method " + method);
        switch(method) {
          case "reset":
            postMessage(simulator.reset());
            break;
          case "step":
            int steps = data.getAsAny("steps").asInt();
            info("steps: " + steps);
            postMessage(simulator.step(steps));
            break;
          case "setcacheconfig":
            JsPropertyMap<Object> config = Js.cast(data.get("config"));
            JsPropertyMap<Object> l1d = Js.cast(config.get("l1d"));
            JsPropertyMap<Object> l1i = Js.cast(config.get("l1i"));

            int l1dSize = ((Double) l1d.get("size")).intValue();
            int l1dBlockSize = ((Double) l1d.get("blockSize")).intValue();
            int l1dAssoc = ((Double) l1d.get("associativity")).intValue();
            int l1dPenalty = ((Double) l1d.get("penalty")).intValue();

            int l1iSize = ((Double) l1i.get("size")).intValue();
            int l1iBlockSize = ((Double) l1i.get("blockSize")).intValue();
            int l1iAssoc = ((Double) l1i.get("associativity")).intValue();
            int l1iPenalty = ((Double) l1i.get("penalty")).intValue();

            simulator.setCacheConfig(l1dSize, l1dBlockSize, l1dAssoc, l1dPenalty,
                    l1iSize, l1iBlockSize, l1iAssoc, l1iPenalty);

            postMessage(simulator.resultFactory.Success());
            break;
          case "load":
            String code = data.getAsAny("code").asString();
            Result parseResult = simulator.loadProgram(code);
            if (!parseResult.success) {
              DomGlobal.console.log(parseResult);
              postMessage(parseResult);
            } else {
              postMessage(simulator.step(1));
            }
            break;
          case "checksyntax":
            // Use parsingSimulator to do a syntax check, then inject the parsing errors
            // (if any) in the Status object from the other simulator.
            String codeString = data.getAsAny("code").asString();
            Result parsingSimulatorResult = parsingSimulator.loadProgram(codeString);
            if (parsingSimulatorResult.success) {
              Result finalResult = simulator.resultFactory.Success();
              finalResult.parsingErrors = parsingSimulatorResult.parsingErrors;
              postMessage(finalResult);
              break;
            }

            // Create and send back a Result object containing the state of the
            // running simulator, but with the error message coming from the
            // parsing.
            Result finalResult = simulator.resultFactory.Failure(parsingSimulatorResult.errorMessage);
            finalResult.parsingErrors = parsingSimulatorResult.parsingErrors;
            postMessage(finalResult);
            break;
          default:
            info("UNKNOWN METHOD: " + method);
        }
      }
    });
  }

  private void postMessage(Object message) {
    DomGlobal.postMessage(message);
  }

  private void info(String message) {
    logger.info("[GWT] "+ message);
  }
}