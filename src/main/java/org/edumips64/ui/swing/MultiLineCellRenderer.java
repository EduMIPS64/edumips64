/**
 * MultiLineCellRenderer.java
 *
 * This class provides a Renderer for JMultilineTable.
 * (c) 2006 EduMIPS64 project - Rizzo Vanni G.
 *
 * Special Thanks to Thomas Wernitz (thomas_wernitz@clear.net.nz)
 * for his source code.
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

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.*;
import java.awt.Component;
import java.awt.Color;

public class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
  private static final long serialVersionUID = -4281001238359343652L;

  protected static Border noFocusBorder;

  boolean[] lineIsError;
  private Color errorBackground ;
  private Color warningBackground;

  public MultiLineCellRenderer() {
    super();
    noFocusBorder = new EmptyBorder(1, 2, 1, 2);
    setLineWrap(true);
    setWrapStyleWord(true);
    setOpaque(true);
    setEditable(false);
    setBorder(noFocusBorder);
  }

  public MultiLineCellRenderer(boolean[] lineIsError, GUITheme theme) {
    super();
    noFocusBorder = new EmptyBorder(1, 2, 1, 2);
    setLineWrap(true);
    setWrapStyleWord(true);
    setOpaque(true);
    setBorder(noFocusBorder);
    
    this.errorBackground = theme.getErrorColor();
    this.warningBackground = theme.getWarningColor();
    this.lineIsError = lineIsError;
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus,
      int row, int column) {

    if (lineIsError[row]) {
      super.setBackground(errorBackground);
    } else {
      super.setBackground(warningBackground);
    }

    setFont(table.getFont());

    setBorder(noFocusBorder);

    setValue(value);

    return this;
  }

  protected void setValue(Object value) {
    setText((value == null) ? "" : (value.toString()).trim());
  }


  public static class UIResource extends MultiLineCellRenderer implements javax.swing.plaf.UIResource {
    private static final long serialVersionUID = -8775228397685982977L;
  }

}


