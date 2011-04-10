/* GUILoggingHandler.java
 *
 * Handler (in the style of java.util.logging.Handler) that writes log
 * messages to the GUILog window.
 *
 * (c) 2011 Andrea Spadaccini
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

package edumips64.utils;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import edumips64.ui.GUILog;

/** GUILoggingHandler
 *  Java Logging Handler that writes log messages to the GUILog window.
 */
public class GUILoggingHandler extends Handler {
    private GUILog guilog;
    public GUILoggingHandler(GUILog guilog) {
        super();
        this.guilog = guilog;
        this.setFormatter(new SimpleFormatter());
    }

    /** Method that writes the LogRecord to the log window.*/
    public void publish(LogRecord record) {
        if(!isLoggable(record)) {
            return;
        }

        guilog.log(getFormatter().format(record));
    }

    /** Need to override this, but no action is required.*/
    public void close() {
    }

    /** Need to override this, but no action is required.*/
    public void flush() {
    }
}
