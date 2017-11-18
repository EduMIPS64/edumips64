/*
 * ParserException.java
 *
 * Single Exception not throwable
 *
 * (c) 2006 mancausoft, Vanni
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

package org.edumips64.core.parser;
import org.edumips64.utils.*;

/**
 *
 * @author mancausoft, Vanni
 */

public class ParserException {

  private int row, column;
  private String line, description;
  private boolean isError;

  /** Create a new instance of ParserException
   *  @param description The Description of exception
   *  @param row The row where there are a error
   *  @param column The column where there are a error
   *  @param line A String with bad code
   */
  public ParserException(String description, int row, int column, String line) {
    this.row = row;
    this.column = column;
    this.line = line;
    this.description = CurrentLocale.getString(description);

  }

  public void setError(boolean iserror) {
    isError = iserror;
  }
  public boolean isError() {
    return isError;
  }
  /** Returns a string representation of the ParserException
   * @return a string representation of the ParserException
   */
  public String toString() {
    String tmp = new String(CurrentLocale.getString("ROW") + " " + row + ", " + CurrentLocale.getString("COLUMN") + " " + column + ": " + line + "\n" + description);
    return tmp;
  }
  public String[] getStringArray() {
    return new String[] {  "" + row,
                           "" + column,
                           line,
                           description
                        };
  }
}
