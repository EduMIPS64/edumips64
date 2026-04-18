/* ParserErrorTest.java
 *
 * Unit tests for the ParserError JsType class.
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
package org.edumips64.client;

import org.edumips64.BaseTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the ParserError JsType class that represents parsing errors
 * for JavaScript consumption.
 */
public class ParserErrorTest extends BaseTest {

  @Test
  public void testParserErrorCreation() {
    ParserError error = new ParserError();
    
    // Default values
    assertEquals("Default row should be 0", 0, error.row);
    assertEquals("Default column should be 0", 0, error.column);
    assertFalse("Default isWarning should be false", error.isWarning);
    assertNull("Default description should be null", error.description);
  }

  @Test
  public void testParserErrorRowAssignment() {
    ParserError error = new ParserError();
    
    error.row = 10;
    assertEquals("Row should be assignable", 10, error.row);
    
    error.row = 0;
    assertEquals("Row should accept 0", 0, error.row);
    
    error.row = 1000;
    assertEquals("Row should accept large values", 1000, error.row);
  }

  @Test
  public void testParserErrorColumnAssignment() {
    ParserError error = new ParserError();
    
    error.column = 5;
    assertEquals("Column should be assignable", 5, error.column);
    
    error.column = 0;
    assertEquals("Column should accept 0", 0, error.column);
    
    error.column = 500;
    assertEquals("Column should accept large values", 500, error.column);
  }

  @Test
  public void testParserErrorIsWarningAssignment() {
    ParserError error = new ParserError();
    
    assertFalse("Default isWarning should be false", error.isWarning);
    
    error.isWarning = true;
    assertTrue("isWarning should be assignable to true", error.isWarning);
    
    error.isWarning = false;
    assertFalse("isWarning should be assignable to false", error.isWarning);
  }

  @Test
  public void testParserErrorDescriptionAssignment() {
    ParserError error = new ParserError();
    
    error.description = "Test error description";
    assertEquals("Description should be assignable", "Test error description", error.description);
    
    error.description = "";
    assertEquals("Description should accept empty string", "", error.description);
    
    error.description = null;
    assertNull("Description should accept null", error.description);
  }

  @Test
  public void testParserErrorFullyPopulated() {
    ParserError error = new ParserError();
    
    error.row = 15;
    error.column = 25;
    error.isWarning = true;
    error.description = "Undefined label referenced";
    
    assertEquals("Row should be preserved", 15, error.row);
    assertEquals("Column should be preserved", 25, error.column);
    assertTrue("isWarning should be preserved", error.isWarning);
    assertEquals("Description should be preserved", "Undefined label referenced", error.description);
  }

  @Test
  public void testParserErrorAsError() {
    ParserError error = new ParserError();
    
    error.row = 1;
    error.column = 1;
    error.isWarning = false;
    error.description = "Syntax error: unexpected token";
    
    assertFalse("Error should not be marked as warning", error.isWarning);
    assertNotNull("Error should have description", error.description);
  }

  @Test
  public void testParserErrorAsWarning() {
    ParserError error = new ParserError();
    
    error.row = 1;
    error.column = 1;
    error.isWarning = true;
    error.description = "Deprecated instruction usage";
    
    assertTrue("Warning should be marked as warning", error.isWarning);
    assertNotNull("Warning should have description", error.description);
  }

  @Test
  public void testParserErrorMultilineDescription() {
    ParserError error = new ParserError();
    
    String multilineDesc = "Error occurred\nLine 1: DADD R1, R2, R3\nExpected: valid register";
    error.description = multilineDesc;
    
    assertEquals("Multiline description should be preserved", multilineDesc, error.description);
  }

  @Test
  public void testParserErrorSpecialCharactersInDescription() {
    ParserError error = new ParserError();
    
    String specialDesc = "Error: unexpected '<' in instruction";
    error.description = specialDesc;
    
    assertEquals("Special characters should be preserved", specialDesc, error.description);
  }
}
