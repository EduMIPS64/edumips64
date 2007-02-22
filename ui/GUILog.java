/* GUILog.java
 *
 * Logging window
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

package edumips64.ui;

import edumips64.utils.Config;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;

/** Logging window.
 *  @author Andrea Spadaccini
 */

public class GUILog extends JInternalFrame {
	private JTextArea log_area;
	private String getTimeStamp() {
		String format = "yyyy-MM-dd HH:mm:ss";
		Date today = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(today);
	}
	/** Writes a debug message to the log, prepending the tag [DEBUG]. */
	public void debug(String message) {
		log("[DEBUG] " + message);
	}

	/** Writes a warning message to the log, prepending the tag [WARNING]. */
	public void warning(String message) {
		log("[WARNING] " + message);
	}

	/** Writes a message that indicates an exception to the log, prepending the tag [EXCEPTION]. */
	public void exception(String message) {
		log("[EXCEPTION] " + message);
	}
	/** Writes a message to the log. */
	public void log(String message) {
		if(edumips64.Main.debug_mode) {
			log_area.append(getTimeStamp() + ": " + message + "\n");
			log_area.setCaretPosition(log_area.getText().length());
		}
	}

	public GUILog(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
		log_area = new JTextArea();
		log_area.setBorder(BorderFactory.createTitledBorder("Log"));
		log_area.setEditable(false);
		log_area.setFont(new Font("Monospaced", Font.PLAIN, 12));

		this.getContentPane().add(new JScrollPane(log_area));
		setSize(500, 300);
	}
}
