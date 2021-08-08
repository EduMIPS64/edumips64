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
  protected List<ParserException> exceptions = new LinkedList<>();

  /** Checks if the exception contains any errors.
   *
   * The exception might be empty or contain only warnings.
   */
  public boolean hasErrors() {
    return exceptions.stream().anyMatch(e -> e.isError());
  }

  /** Add an error in the list
   * @param description the String with description of the error
   * @param row the row number of the error
   * @param column the column number of the error
   * @param line the String with error code
   */
  public void addError(String description, int row, int column, String line) {
    exceptions.add(new ParserError(description, row, column, line));
  }
  /** Add a warning in the list
   * @param description the String with description of the error
   * @param row the row number of the error
   * @param column the column number of the error
   * @param line the String with error code
   */
  public void addWarning(String description, int row, int column, String line) {
    exceptions.add(new ParserWarning(description, row, column, line));
  }
  /** Returns a String representation of the ParserMultiException
   * @return a String representation of the ParserMultiException
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (ParserException e : exceptions) {
      sb.append(e).append("\n\n");
    }

    return sb.toString();
  }

  public int size() {
    return exceptions.size();
  }

  public List<ParserException> getExceptionList() {
    return exceptions;
  }
}
