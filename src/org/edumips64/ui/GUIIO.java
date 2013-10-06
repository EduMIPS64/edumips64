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

package org.edumips64.ui;

import org.edumips64.utils.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;

/** Input/output window.
 *  @author Andrea Spadaccini
 */

public class GUIIO extends JInternalFrame {
  private JTextArea output_area;
  private JButton clear;

  private class OutputTask implements Runnable {
    public String message;
    public OutputTask(String message) {
      this.message = message;
    }

    public void run() {
      output_area.append(message);
      output_area.setCaretPosition(output_area.getText().length());

      if (GUIIO.this.isIcon()) {
        try {
          GUIIO.this.setIcon(false);
        } catch (java.beans.PropertyVetoException e) {}
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

  public String read(int count) {
    String read_s = null;

    do {
      read_s = JOptionPane.showInputDialog(this, CurrentLocale.getString("ENTERINPUT"), "EduMIPS64 - Input", JOptionPane.PLAIN_MESSAGE);

      if (read_s == null) {
        read_s = new String();
      }

      if (read_s.length() > count) {
        JOptionPane.showMessageDialog(this, CurrentLocale.getString("INPUTNOTEXCEED") + " " + count + " " + CurrentLocale.getString("CHARACTERS"), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.INFORMATION_MESSAGE);
      }

    } while (read_s.length() > count);

    return read_s;
  }

  public GUIIO(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
    super(title, resizable, closable, maximizable, iconifiable);
    output_area = new JTextArea();
    output_area.setBorder(BorderFactory.createTitledBorder("Output"));
    output_area.setEditable(false);
    output_area.setFont(new Font("Monospaced", Font.PLAIN, 12));

    clear = new JButton(CurrentLocale.getString("CLEAR"));

    Container cp = this.getContentPane();
    Container lowerbox = Box.createHorizontalBox();
    lowerbox.add(clear);

    clear.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        output_area.setText("");
      }
    });


    cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

    cp.add(new JScrollPane(output_area));
    cp.add(lowerbox);
    setSize(650, 300);
  }
  
  public Writer getWriter() {
    return new WriterProxy(this);
  }
  
  /*
   * Proxy Object used by the Main class in order to decouple the GUI from the logic.
  */
  private class WriterProxy extends Writer{

    public GUIIO guiio;

    public WriterProxy(GUIIO guiio){

      this.guiio = guiio;
    }

    @Override
    public void close() throws IOException {}

    @Override
    public void flush() throws IOException {}

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {}

    public void write(String textToBeWritten){
      guiio.write(textToBeWritten);
    }     

  }
  
    
}
