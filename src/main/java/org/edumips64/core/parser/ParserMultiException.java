package org.edumips64.core.parser;
import java.util.*;
/*
 * ParserMultiException.java
 *
 * The Exception throw by the Parser
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

/** The list of error in the MIPS64 asm code
 *
 * @author mancausoft, Vanni
 */

public class ParserMultiException extends Exception {
  private static final long serialVersionUID = -1182294876128642426L;
  protected LinkedList<ParserException> exception;

  /** Create the ParserMultiException
   */
  public ParserMultiException() {
    super(" ");
    exception = new LinkedList<ParserException>();
  }
  /*Create the ParserMultiException from List
   *
   * non dovrebbe essere usata la commento se compila la butto :D
   * @param
   *
  public ParserMultiException(String message, LinkedList<ParserException> exception )
  {
    this.exception = exception;
  } */

  /** Checks if the exception contains any errors.
   *
   * The exception might be empty or contain only warnings.
   */
  public boolean hasErrors() {
    for (ParserException e : exception) {
      if (e.isError()) {
        return true;
      }
    }

    return false;
  }

  /** Add an error in the list
   * @param description the String with description of the error
   * @param row the row number of the error
   * @param column the column number of the error
   * @param line the String with error code
   */
  public void add(String description, int row, int column, String line) {
    ParserError tmp = new ParserError(description, row, column, line);
    tmp.setError(true);
    exception.add(tmp);
  }
  /** Add a warning in the list
   * @param description the String with description of the error
   * @param row the row number of the error
   * @param column the column number of the error
   * @param line the String with error code
   */
  public void addWarning(String description, int row, int column, String line) {
    ParserWarning tmp = new ParserWarning(description, row, column, line);
    tmp.setError(false);
    exception.add(tmp);
  }
  /** Returns a String representation of the ParserMultiException
   * @return a String representation of the ParserMultiException
   */
  public String toString() {
    String ret = "";

    for (ParserException e : exception) {
      ret += e + "\n\n";
    }

    return ret;
  }

  /**
   */
  public ParserException getError(int num) {
    return exception.get(num);
  }
  public int getNumError() {
    return exception.size();
  }
  public LinkedList<ParserException> getExceptionList() {
    return exception;
  }
}
