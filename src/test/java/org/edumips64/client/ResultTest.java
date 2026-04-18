/* ResultTest.java
 *
 * Unit tests for the Result class.
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
 * Unit tests for the Result class.
 * 
 * Note: The ResultFactory class uses GWT-specific classes (JSONArray, JsArray) that are not
 * available at test runtime, so we can only test the Result class directly without
 * triggering class loading of ResultFactory.
 */
public class ResultTest extends BaseTest {

  @Test
  public void testResultConstructorSuccess() {
    Result result = new Result(true, "", "stdout content");
    
    assertTrue("Success flag should be true", result.success);
    assertEquals("Error message should be empty", "", result.errorMessage);
    assertEquals("Stdout should be preserved", "stdout content", result.stdout);
    assertEquals("Default status should be READY", "READY", result.status);
  }

  @Test
  public void testResultConstructorFailure() {
    Result result = new Result(false, "Some error", "");
    
    assertFalse("Success flag should be false", result.success);
    assertEquals("Error message should match", "Some error", result.errorMessage);
    assertEquals("Stdout should be empty", "", result.stdout);
  }

  @Test
  public void testResultToString() {
    Result result = new Result(true, "", "");
    result.status = "RUNNING";
    
    String str = result.toString();
    
    assertTrue("toString should contain success", str.contains("success: true"));
    assertTrue("toString should contain status", str.contains("Status: RUNNING"));
  }

  @Test
  public void testResultToStringWithError() {
    Result result = new Result(false, "Test error message", "");
    result.status = "STOPPED";
    
    String str = result.toString();
    
    assertTrue("toString should contain success false", str.contains("success: false"));
    assertTrue("toString should contain error message", str.contains("errorMessage: Test error message"));
    assertTrue("toString should contain status", str.contains("Status: STOPPED"));
  }

  @Test
  public void testResultDefaultStringValues() {
    Result result = new Result(true, "", "");
    
    // Check default values for string fields not set in constructor
    assertEquals("Default memory should be empty", "", result.memory);
    assertEquals("Default registers should be empty", "", result.registers);
    assertEquals("Default statistics should be empty", "", result.statistics);
    assertEquals("Default cachestats should be empty", "", result.cachestats);
    assertFalse("Default encounteredBreak should be false", result.encounteredBreak);
    assertNull("Default pipeline should be null", result.pipeline);
    assertNull("Default validInstructions should be null", result.validInstructions);
  }

  @Test
  public void testResultStatusFieldModification() {
    Result result = new Result(true, "", "");
    
    assertEquals("Initial status should be READY", "READY", result.status);
    
    result.status = "RUNNING";
    assertEquals("Status should be modifiable", "RUNNING", result.status);
    
    result.status = "STOPPED";
    assertEquals("Status should be modifiable to STOPPED", "STOPPED", result.status);
  }

  @Test
  public void testResultEncounteredBreakModification() {
    Result result = new Result(true, "", "");
    
    assertFalse("Initial encounteredBreak should be false", result.encounteredBreak);
    
    result.encounteredBreak = true;
    assertTrue("encounteredBreak should be modifiable", result.encounteredBreak);
  }

  @Test
  public void testResultStdoutPreservation() {
    String multiLineOutput = "Line 1\nLine 2\nLine 3";
    Result result = new Result(true, "", multiLineOutput);
    
    assertEquals("Multiline stdout should be preserved", multiLineOutput, result.stdout);
  }

  @Test
  public void testResultErrorMessagePreservation() {
    String complexError = "Error: Invalid instruction at line 5\nExpected: ADD r1, r2, r3";
    Result result = new Result(false, complexError, "");
    
    assertEquals("Complex error message should be preserved", complexError, result.errorMessage);
  }
}
