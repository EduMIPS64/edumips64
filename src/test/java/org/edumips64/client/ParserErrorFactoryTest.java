/* ParserErrorFactoryTest.java
 *
 * Unit tests for the ParserErrorFactory class.
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
import org.edumips64.core.parser.ParserMultiException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the ParserErrorFactory class that creates ParserError objects
 * for the web UI.
 */
public class ParserErrorFactoryTest extends BaseTest {

  @Test
  public void testFromParserExceptionWithError() {
    // Create a ParserMultiException with an error
    ParserMultiException multiEx = new ParserMultiException();
    multiEx.addError("PARSER_ERROR_TEST", 5, 10, "test line content");
    
    // Get the first exception
    org.edumips64.core.parser.ParserException parserEx = 
        multiEx.getExceptionList().get(0);
    
    // Convert using the factory
    ParserError result = ParserErrorFactory.FromParserException(parserEx);
    
    assertNotNull("Result should not be null", result);
    assertEquals("Row should match", 5, result.row);
    assertEquals("Column should match", 10, result.column);
    assertFalse("Error should not be marked as warning", result.isWarning);
    assertNotNull("Description should not be null", result.description);
  }

  @Test
  public void testFromParserExceptionWithWarning() {
    // Create a ParserMultiException with a warning
    ParserMultiException multiEx = new ParserMultiException();
    multiEx.addWarning("PARSER_WARNING_TEST", 3, 7, "warning line");
    
    // Get the first exception
    org.edumips64.core.parser.ParserException parserEx = 
        multiEx.getExceptionList().get(0);
    
    // Convert using the factory
    ParserError result = ParserErrorFactory.FromParserException(parserEx);
    
    assertNotNull("Result should not be null", result);
    assertEquals("Row should match", 3, result.row);
    assertEquals("Column should match", 7, result.column);
    assertTrue("Warning should be marked as warning", result.isWarning);
    assertNotNull("Description should not be null", result.description);
  }

  @Test
  public void testFromParserExceptionRowAndColumnParsing() {
    // Test that row and column are correctly parsed from string array
    ParserMultiException multiEx = new ParserMultiException();
    multiEx.addError("TEST_MESSAGE", 100, 50, "code here");
    
    org.edumips64.core.parser.ParserException parserEx = 
        multiEx.getExceptionList().get(0);
    
    ParserError result = ParserErrorFactory.FromParserException(parserEx);
    
    assertEquals("Large row number should be parsed correctly", 100, result.row);
    assertEquals("Large column number should be parsed correctly", 50, result.column);
  }
}
