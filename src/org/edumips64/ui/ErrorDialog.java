/* ErrorDialog.java
 *
 * This class provides a window for errors and warnings messages.
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

import org.edumips64.core.*;
import org.edumips64.utils.*;
import org.edumips64.Main;
import org.edumips64.core.is.Instruction;
import org.edumips64.core.CPU;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * This class provides a window for configuration options.
*/
public class ErrorDialog extends JDialog {

  private static boolean[] lineIsError;
  JButton okButton;
  int numError = 0, width = 710, height = 350;
  public ErrorDialog(final JFrame owner, LinkedList<ParserException> peList, String title) {

    super(owner, title, true);

    lineIsError = new boolean[peList.size() * 10];
    JPanel buttonPanel = new JPanel();

    JButton okButton = new JButton("OK");
    buttonPanel.add(okButton);

    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        setVisible(false);
        //dispose();
      }
    });
    buttonPanel.add(okButton);
    String[] columnnames = {"error1"};

    String[] columnNames = {
      CurrentLocale.getString("ErrorDialog.ROW"),
      CurrentLocale.getString("ErrorDialog.COLUMN"),
      CurrentLocale.getString("ErrorDialog.LINE"),
      CurrentLocale.getString("ErrorDialog.DESCRIPTION")
    };
    DefaultTableModel dft = new DefaultTableModel(columnNames, 0);  // peList.size());
    //DefaultTableModel(Object[][] data, Object[] columnNames)
    //DefaultTableModel(Object[] columnNames, int rowCount)

    MultiLineTable table = new MultiLineTable(dft);
    MultiLineCellRenderer renderer = new MultiLineCellRenderer(lineIsError);

    //DefaultTableCellRenderer dftcr =new DefaultTableCellRenderer();

    table.setCellSelectionEnabled(false);
    table.setFocusable(false);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setShowGrid(false);

    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    table.getTableHeader().setResizingAllowed(false);
    table.getTableHeader().setReorderingAllowed(false);
    table.getColumnModel().getColumn(0).setPreferredWidth(40);
    table.getColumnModel().getColumn(1).setPreferredWidth(50);
    table.getColumnModel().getColumn(2).setPreferredWidth(205);
    table.getColumnModel().getColumn(3).setPreferredWidth(400);

    table.getColumnModel().getColumn(0).setCellRenderer(renderer);
    table.getColumnModel().getColumn(1).setCellRenderer(renderer);
    table.getColumnModel().getColumn(2).setCellRenderer(renderer);
    table.getColumnModel().getColumn(3).setCellRenderer(renderer);

    int i = 0;

    for (ParserException e : peList) {
      lineIsError[i] = e.isError();

      if (lineIsError[i]) {
        numError++;
      }

      if (lineIsError[i] || (Boolean) Config.get("warnings")) {
        dft.addRow(e.getStringArray());
        i++;
      }
    }

    JScrollPane scrollTable = new JScrollPane(table);
    String msg = CurrentLocale.getString("ErrorDialog.MSG0") + " " + numError + " " +
                 CurrentLocale.getString("ErrorDialog.MSG1") + " " + (peList.size() - numError) + " " +
                 CurrentLocale.getString("ErrorDialog.MSG2");
    JLabel label = new JLabel(msg);

    try {
      label = new JLabel(msg, new ImageIcon(
                           org.edumips64.img.IMGLoader.getImage(
                             ((numError > 0) ? "error.png" : "warning.png")
                           )), SwingConstants.LEFT);
      label.setIconTextGap(50);
      label.setFont(new Font("Verdana", 0, 20));
      label.setForeground(new Color(0, 0, 85));
    } catch (java.io.IOException e) {}

    getRootPane().setDefaultButton(okButton);
    getContentPane().setLayout(new BorderLayout());

    getContentPane().add("North", label);
    getContentPane().add("Center", scrollTable);
    getContentPane().add("South", buttonPanel);

    setSize(width, height);
    setLocation((getScreenWidth() - getWidth()) / 2, (getScreenHeight() - getHeight()) / 2);

    if (!((Boolean) Config.get("warnings")) && numError == 0) {
      setVisible(false);
      dispose();
    } else {
      setVisible(true);
    }

  }
  public boolean fileWithError() {
    return (numError > 0);
  }

  public static int getScreenWidth() {
    return (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
  }

  public static int getScreenHeight() {
    return (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
  }
}
