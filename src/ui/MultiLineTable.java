/**
 * MultiLineTable.java
 *
 * This class provides a JTable that supports multiline cells.
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

package edumips64.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.plaf.*;
import javax.swing.text.*;
import java.util.*;


public class MultiLineTable extends JTable {

	public MultiLineTable(TableModel dm) {
		this(dm, null, null);
	}
	public MultiLineTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm,cm,sm);
		setUI( new MultiLineBasicTableUI() );
		 
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {    
				revalidate();
			}
		});
	}

	public int rowAtPoint(Point point) {
		int y = point.y;
		int rowSpacing = getIntercellSpacing().height;
		int rowCount = getRowCount();
		int rowHeight = 0;
		for (int row=0; row<rowCount; row++) {
			rowHeight += getRowHeight(row) + rowSpacing;
			if (y < rowHeight) {
				return row;
			}
		}
		return -1;
	}

	public int getHeight(String text, int width) {
        if(text == null)
            text = "";
		FontMetrics fm = getFontMetrics(getFont());
		int numLines = 1;
		Segment s = new Segment(text.toCharArray(), 0, 0);
		s.count = s.array.length;
		TabExpander te = new MyTabExpander(fm);
		int breaks = getBreakLocation(s, fm, 0, width, te, 0);
		while((breaks+s.offset) < s.array.length) {
			s.offset += breaks;
			s.count = s.array.length - s.offset;
			numLines++;
			breaks = getBreakLocation(s, fm, 0, width, te, 0);
		}
		return numLines * fm.getHeight();
	}

	public boolean isCellEditable(int r,int c)
	{
		return false;
	}

	public int getTabbedTextOffset(Segment s, 
			FontMetrics metrics,
			int x0, int x, TabExpander e,
			int startOffset, 
			boolean round) {
		int currX = x0;
		int nextX = currX;
		char[] txt = s.array;
		int n = s.offset + s.count;
		for (int i = s.offset; i < n; i++) {
			if (txt[i] == '\t') {
				if (e != null) {
					nextX = (int) e.nextTabStop((float) nextX,
							startOffset + i - s.offset);
				} else {
					nextX += metrics.charWidth(' ');
				}
			} else if (txt[i] == '\n') {
				return i - s.offset;
			} else if (txt[i] == '\r') {
				return i + 1 - s.offset; // kill the newline as well
			} else {
				nextX += metrics.charWidth(txt[i]);
			}
			if ((x >= currX) && (x < nextX)) {
				// found the hit position... return the appropriate side
				if ((round == false) || ((x - currX) < (nextX - x))) {
					return i - s.offset;
				} else {
					return i + 1 - s.offset;
				}
			}
			currX = nextX;
		}

		return s.count;
	}

	public int getBreakLocation(Segment s, FontMetrics metrics,
			int x0, int x, TabExpander e,
			int startOffset) {

		int index = getTabbedTextOffset(s, metrics, x0, x, 
				e, startOffset, false);

		if ((s.offset+index) < s.array.length) {
			for (int i = s.offset + Math.min(index, s.count - 1); 
					i >= s.offset; i--) {

				char ch = s.array[i];
				if (Character.isWhitespace(ch)) {
					// found whitespace, break here
					index = i - s.offset + 1;
					break;
				}
					}
		}
		return index;
	}

	class MyTabExpander implements TabExpander {
		int tabSize;
		public MyTabExpander(FontMetrics metrics) {
			tabSize = 5 * metrics.charWidth('m');
		}
		public float nextTabStop(float x, int offset) {
			int ntabs = (int) x / tabSize;
			return (ntabs + 1) * tabSize;
		}
	}


	public int getRowHeight() {
		//getRowHeight() not valid in MultiLineTable
		//Thread.dumpStack();
		return -1;
	}
	public int getRowHeight(int row) {
		int numCols = getColumnCount();
		TableModel tm = getModel();
		int fontHeight = getFontMetrics(getFont()).getHeight();
		int height = fontHeight;
		Enumeration cols = getColumnModel().getColumns();
		int i = 0;
		while(cols.hasMoreElements()) {
			TableColumn col = (TableColumn) cols.nextElement();
			TableCellRenderer tcr = col.getCellRenderer();
			// without the revalidate hack above, the call th getWidth does not give the
			// right value at the right time. Take out the revalidate and uncomment the
			// next line to see for your self. If you find a way to do it right, drop me
			// a mail please! :)
			// System.out.println(col.getWidth());
			int colWidth = col.getWidth();
			if (tcr instanceof MultiLineCellRenderer) {
				height = Math.max(height, getHeight((String)tm.getValueAt(row,i), colWidth));
			}
			i++;
		}
		return height;
	}

	public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
		Rectangle cellFrame;
		TableColumn aColumn;

		cellFrame = new Rectangle();
		//        cellFrame.height = getRowHeight() + rowMargin;
		//        cellFrame.y = row * cellFrame.height;
		cellFrame.height = getRowHeight(row) + rowMargin;
		cellFrame.y = 0;
		for (int i=0; i<row; i++) {
			cellFrame.y += getRowHeight(i) + rowMargin;
		}

		int index = 0;
		int columnMargin = getColumnModel().getColumnMargin();
		Enumeration enumeration = getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			aColumn = (TableColumn)enumeration.nextElement();
			cellFrame.width = aColumn.getWidth() + columnMargin;

			if (index == column)
				break;

			cellFrame.x += cellFrame.width;
			index++;
		}

		if (!includeSpacing) {
			Dimension spacing = getIntercellSpacing();
			// This is not the same as grow(), it rounds differently.
			cellFrame.setBounds(cellFrame.x +      spacing.width/2,
					cellFrame.y +      spacing.height/2,
					cellFrame.width -  spacing.width,
					cellFrame.height - spacing.height);
		}
		return cellFrame;
	}
} // MultiLineTable
