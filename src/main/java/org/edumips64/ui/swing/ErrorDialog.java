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
package org.edumips64.ui.swing;

import org.edumips64.core.parser.ParserException;
import org.edumips64.utils.CurrentLocale;

import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.awt.*;
import javax.swing.*;

/**
 * This class provides a window for configuration options.
*/
public class ErrorDialog extends JDialog {
  private static final long serialVersionUID = 6756487575875944232L;

  public ErrorDialog(final JFrame owner, LinkedList<ParserException> peList, String title, Boolean showWarning) {

    super(owner, title, true);

    boolean[] lineIsError = new boolean[peList.size() * 10];
    JPanel buttonPanel = new JPanel();

    JButton okButton = new JButton("OK");
    buttonPanel.add(okButton);

    okButton.addActionListener(e -> setVisible(false));
    buttonPanel.add(okButton);

    String[] columnNames = {
      CurrentLocale.getString("ErrorDialog.ROW"),
      CurrentLocale.getString("ErrorDialog.COLUMN"),
      CurrentLocale.getString("ErrorDialog.LINE"),
      CurrentLocale.getString("ErrorDialog.DESCRIPTION")
    };
    DefaultTableModel dft = new DefaultTableModel(columnNames, 0);

    MultiLineTable table = new MultiLineTable(dft);
    MultiLineCellRenderer renderer = new MultiLineCellRenderer(lineIsError);

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
    int numError = 0;
    for (ParserException e : peList) {
      lineIsError[i] = e.isError();

      if (lineIsError[i]) {
        numError++;
      }

      if (lineIsError[i] || showWarning) {
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
                           org.edumips64.ui.swing.img.IMGLoader.getImage(
                             ((numError > 0) ? "error.png" : "warning.png")
                           )), SwingConstants.LEFT);
      label.setIconTextGap(50);
      label.setFont(new Font("Verdana", Font.PLAIN, 20));
      label.setForeground(new Color(0, 0, 85));
    } catch (java.io.IOException ignored) {}

    getRootPane().setDefaultButton(okButton);
    getContentPane().setLayout(new BorderLayout());

    getContentPane().add("North", label);
    getContentPane().add("Center", scrollTable);
    getContentPane().add("South", buttonPanel);

    int width = 710;
    int height = 350;
    setSize(width, height);

    if (!showWarning && numError == 0) {
      setVisible(false);
      dispose();
    } else {
      setVisible(true);
    }
  }
}
