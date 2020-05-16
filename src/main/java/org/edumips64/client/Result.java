/* Result.java
 *
 * Thin value object to represent the result of a generic operation, exported
 * as a type to JavaScript.
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

import jsinterop.annotations.JsType;

@JsType(namespace="jsedumips64")
public class Result {
  public boolean success = false;
  public String errorMessage = "";
  public Simulator.Status status = Simulator.Status.READY;
  public String memory = "";
  public String registers = "";
  public String statistics = "";
  public Pipeline pipeline;

  public Result(boolean success, String errorMessage) {
    this.success = success;
    this.errorMessage = errorMessage;
  }

  public String toString() {
    return "Result {success: " + success + ", errorMessage: " + errorMessage + ", Status: " + status.name() + "}";
  }
}
