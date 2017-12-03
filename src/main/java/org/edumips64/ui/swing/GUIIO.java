/* GUIIO.java
 *
 * Input/output window
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

package org.edumips64.ui.swing;

import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.io.ReadException;
import org.edumips64.utils.io.Reader;
import org.edumips64.utils.io.WriteException;
import org.edumips64.utils.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Container;
import java.awt.Font;

/** Input/output window.
 *  @author Andrea Spadaccini
 */


public class GUIIO extends JInternalFrame {
  private JTextArea output_area;

  private class OutputTask implements Runnable {
    public String message;
    OutputTask(String message) {
      this.message = message;
    }

    public void run() {
      output_area.append(message);
      output_area.setCaretPosition(output_area.getText().length());

      if (GUIIO.this.isIcon()) {
        try {
          GUIIO.this.setIcon(false);
        } catch (java.beans.PropertyVetoException ignored) {}
      }
    }
  }


  /** Writes a message to the output area. */
  public void write(String message) {
    // Append text to the JTextArea using the Event Dispatch Thread. This
    // is the correct way of doing it, but since JTextArea.append() was
    // thread safe before Java 7, we simply invoked the append() method;
    // now that there are users that adopted Java 7, we need to use the
    // EDT to interact with the JTextArea.
    SwingUtilities.invokeLater(new OutputTask(message));
  }

  public void write(byte[] bytes_array) {
    String s = new String(bytes_array);
    write(s);
  }

  /*
   * This method forces the user to introduce  a number of characters that not exceed count.
  */
  public String read(int count) {
    String read_s;

    do {
      read_s = JOptionPane.showInputDialog(this, CurrentLocale.getString("ENTERINPUT"), "EduMIPS64 - Input", JOptionPane.PLAIN_MESSAGE);

      if (read_s == null) {
        read_s = "";
      }

      if (read_s.length() > count) {
        JOptionPane.showMessageDialog(this, CurrentLocale.getString("INPUTNOTEXCEED") + " " + count + " " + CurrentLocale.getString("CHARACTERS"), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.INFORMATION_MESSAGE);
      }

    } while (read_s.length() > count);

    return read_s;
  }

  public GUIIO(String title, ConfigStore config) {
    super(title, true, false, true, true);
    output_area = new JTextArea();
    output_area.setBorder(BorderFactory.createTitledBorder("Output"));
    output_area.setEditable(false);
    output_area.setFont(new Font("Monospaced", Font.PLAIN, config.getInt(ConfigKey.UI_FONT_SIZE)));

    JButton clear = new JButton(CurrentLocale.getString("CLEAR"));

    Container cp = this.getContentPane();
    Container lowerbox = Box.createHorizontalBox();
    lowerbox.add(clear);

    clear.addActionListener(e -> output_area.setText(""));


    cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

    cp.add(new JScrollPane(output_area));
    cp.add(lowerbox);
    setSize(650, 300);
  }

  public Writer getWriter() {
    return new WriterProxy(this);
  }

  public Reader getReader() {
    return new ReaderProxy(this);
  }

  /*
   * Proxy Object used by the Main class in order to decouple the GUI from the logic.
   */

  private class WriterProxy implements Writer {
    GUIIO guiio;

    WriterProxy(GUIIO guiio) {
      this.guiio = guiio;
    }

    @Override
    public void close() {}

    /*
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write(String s) throws WriteException {
      guiio.write(s);
    }

  }

  /*
   * Proxy Object used by the Main class in order to decouple the GUI from the logic.
   */

  private class ReaderProxy implements Reader {
    GUIIO guiio;

    ReaderProxy(GUIIO guiio) {
      this.guiio = guiio;
    }

    public void close() {}

    public int read(char[] cbuf, int len) throws ReadException {
      String input = guiio.read(len);

      for (int i = 0; i < input.length(); i++) {
        cbuf[i] = input.charAt(i);
      }

      return input.length();
    }
  }
}
