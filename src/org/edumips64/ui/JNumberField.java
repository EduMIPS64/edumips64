/* GUIConfig.java
 *
 * This class provides a window for configuration options.
 * (c) 2006 EduMIPS64 project - Rizzo Vanni G.
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

import javax.swing.*;

public class JNumberField extends JTextField{
	public void setNumber(Integer number){
		super.setText(number.toString());
	}

	/**
	 * @return the number contained in the field, null if not a number
	 */
	public int getNumber(){
		return Integer.parseInt(super.getText());
	}
	public boolean isNumber(){
		try {
			Integer.parseInt(super.getText());
		}
		catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}
}
