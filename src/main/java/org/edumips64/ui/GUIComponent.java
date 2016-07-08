/* GUIComponent.java
 *
 * This abstract class is implemented by every graphics component class.
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

package org.edumips64.ui;
import org.edumips64.core.*;
import org.edumips64.utils.ConfigStore;

import java.awt.*;

/**
* This abstract class is implemented by every graphics component class.
* @author Filippo Mondello
*/
abstract class GUIComponent {
  protected Container cont;
  protected CPU cpu;
  protected Memory memory;
  protected ConfigStore config;

  public GUIComponent(CPU cpu, Memory memory, ConfigStore config) {
    cont = null;
    this.cpu = cpu;
    this.memory = memory;
    this.config = config;
  }

  /**Set the container of the component class.
  @param co the container sent by GUIFrontend class.
  */
  public void setContainer(Container co) {
    cont = co;
  }

  /**
  * Update the component. All variables are up-to-date to current value, calling the required CPU methods.
  * This method is called for every clock cycle.
  */
  abstract public void update();

  /**Draw the component.
  */
  abstract public void draw();

  /**
   * Update language-dependent parts of the intrface
   */

  public void updateLanguageStrings() {
    // By default, do nothing.
  }
}
