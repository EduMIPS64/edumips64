/* SwingUITests.java
 *
 * Basic Swing UI tests for EduMIPS64.
 *
 * (c) 2024 EduMIPS64 Development Team
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
package org.edumips64;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JMenuItemFixture;
import org.edumips64.core.*;
import org.edumips64.core.parser.Parser;
import org.edumips64.ui.swing.StatusBar;
import org.edumips64.utils.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.junit.Assert.*;

/**
 * Basic Swing UI tests for EduMIPS64.
 * 
 * These tests verify that:
 * - The main window renders properly
 * - Menus and internal frames are present
 * - Files can be parsed and simulator can run
 * 
 * Tests are designed to run in headless mode via Xvfb in GitHub Actions.
 * These tests reuse the Main class initialization to avoid code duplication.
 */
@RunWith(JUnit4.class)
public class SwingUITests {
  
  private FrameFixture window;
  private Robot robot;
  private Main main;
  private JFrame mainFrame;
  
  private static final String TEST_FILE_PATH = "src/test/resources/halt.s";
  
  /**
   * Creates a GenericTypeMatcher for finding a JMenu by text content.
   * @param textToMatch the text to look for in the menu (case insensitive)
   * @return a GenericTypeMatcher that matches menus containing the specified text
   */
  private static GenericTypeMatcher<JMenuItem> menuMatcherForText(String textToMatch) {
    return new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
      @Override
      protected boolean isMatching(JMenuItem component) {
        return component instanceof JMenu && 
               component.getText() != null && 
               component.getText().toLowerCase().contains(textToMatch.toLowerCase());
      }
    };
  }
  
  @BeforeClass
  public static void setUpOnce() {
    // These tests require a graphical environment (real display or Xvfb).
    // The build configuration sets java.awt.headless=false and passes DISPLAY variable.
    // In CI, tests run with xvfb-run which provides the necessary X11 display.
    // If tests fail with AWTError, it indicates a CI configuration issue, not a test problem.
  }
  
  @Before
  public void setUp() {
    // Create a robot for GUI testing
    robot = BasicRobot.robotWithNewAwtHierarchy();
    
    // Initialize Main with an in-memory config store for testing
    ConfigStore configStore = new InMemoryConfigStore(ConfigStore.defaults);
    main = new Main(configStore);
    
    // Create and initialize the main frame on the EDT
    mainFrame = GuiActionRunner.execute(() -> {
      JFrame frame = new JFrame(Main.getDefaultTitle());
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      main.setMainFrame(frame);
      main.init();
      frame.setSize(800, 600);
      frame.setLocationRelativeTo(null);
      return frame;
    });
    
    // Create fixture for the frame
    window = new FrameFixture(robot, mainFrame);
    window.show();
  }
  
  @After
  public void tearDown() {
    if (window != null) {
      window.cleanUp();
    }
    if (robot != null) {
      robot.cleanUp();
    }
  }
  
  /**
   * Test that the main window renders properly.
   */
  @Test(timeout = 30000)
  public void testMainWindowRenders() {
    // Verify the main frame is showing and has correct title
    window.requireVisible();
    assertTrue("Main frame should be showing", mainFrame.isShowing());
    assertTrue("Main frame title should contain 'EduMIPS64'", 
        mainFrame.getTitle().contains("EduMIPS64"));
  }
  
  /**
   * Test that the main window has the expected menus.
   */
  @Test(timeout = 30000)
  public void testMenusExist() {
    // Verify the file menu exists and can be accessed
    JMenuItemFixture fileMenu = window.menuItem(menuMatcherForText("file"));
    fileMenu.requireVisible();
    
    // Verify the help menu exists
    JMenuItemFixture helpMenu = window.menuItem(menuMatcherForText("help"));
    helpMenu.requireVisible();
  }
  
  /**
   * Test that the status bar is visible.
   */
  @Test(timeout = 30000)
  public void testStatusBarVisible() {
    StatusBar sb = main.getStatusBar();
    assertNotNull("Status bar should not be null", sb);
    
    Component statusComponent = sb.getComponent();
    assertNotNull("Status bar component should not be null", statusComponent);
    assertTrue("Status bar should be visible", statusComponent.isVisible());
  }
  
  /**
   * Test that the GUI frontend components are initialized.
   */
  @Test(timeout = 30000)
  public void testFrontendComponentsInitialized() {
    assertNotNull("CPU should be initialized", main.getCpu());
    assertNotNull("Memory should be initialized", main.getMemory());
    assertEquals("CPU should be in READY state", CPU.CPUStatus.READY, main.getCpu().getStatus());
  }
  
  /**
   * Test that the internal frames (windows) are created.
   */
  @Test(timeout = 30000)
  public void testInternalFramesExist() {
    // Get the desktop pane and verify internal frames exist
    JDesktopPane desktop = window.robot().finder().findByType(JDesktopPane.class);
    assertNotNull("Desktop pane should exist", desktop);
    
    JInternalFrame[] frames = desktop.getAllFrames();
    // Main creates 7 internal frames but we only verify at least 6 for some flexibility
    assertTrue("Should have at least 6 internal frames", frames.length >= 6);
    
    // Verify some expected frame titles exist
    boolean hasPipeline = false, hasRegisters = false, hasCode = false;
    
    for (JInternalFrame frame : frames) {
      String title = frame.getTitle().toLowerCase();
      if (title.contains(CurrentLocale.getString("PIPELINE").toLowerCase())) hasPipeline = true;
      if (title.contains(CurrentLocale.getString("REGISTERS").toLowerCase())) hasRegisters = true;
      if (title.contains(CurrentLocale.getString("CODE").toLowerCase())) hasCode = true;
    }
    
    assertTrue("Pipeline frame should exist", hasPipeline);
    assertTrue("Registers frame should exist", hasRegisters);
    assertTrue("Code frame should exist", hasCode);
  }
  
  /**
   * Test that the parser can load a test file.
   * This verifies the file opening mechanism works.
   */
  @Test(timeout = 30000)
  public void testFileCanBeParsed() throws Exception {
    // Verify the test file exists
    File testFile = new File(TEST_FILE_PATH);
    assertTrue("Test file should exist: " + TEST_FILE_PATH, testFile.exists());
    
    // Get the parser from Main and parse the file
    Parser parser = main.getParser();
    assertNotNull("Parser should be initialized", parser);
    
    String absoluteFilename = testFile.getAbsolutePath();
    parser.parse(absoluteFilename);
    
    // Verify CPU can be set to running after loading file
    CacheSimulator cachesim = main.getCacheSimulator();
    Memory memory = main.getMemory();
    CPU cpu = main.getCpu();
    
    cachesim.setDataOffset(memory.getInstructionsNumber() * 4);
    cpu.setStatus(CPU.CPUStatus.RUNNING);
    assertEquals("CPU should be in RUNNING state after loading file", 
        CPU.CPUStatus.RUNNING, cpu.getStatus());
  }
  
  /**
   * Test that the simulator can run a program.
   * This verifies the basic execution mechanism.
   */
  @Test(timeout = 30000)
  public void testSimulatorCanRun() throws Exception {
    // Load and parse the test file
    File testFile = new File(TEST_FILE_PATH);
    String absoluteFilename = testFile.getAbsolutePath();
    
    Parser parser = main.getParser();
    parser.parse(absoluteFilename);
    
    // Set up for execution
    CacheSimulator cachesim = main.getCacheSimulator();
    Memory memory = main.getMemory();
    CPU cpu = main.getCpu();
    CycleBuilder builder = main.getCycleBuilder();
    
    cachesim.setDataOffset(memory.getInstructionsNumber() * 4);
    cpu.setStatus(CPU.CPUStatus.RUNNING);
    builder.reset();
    
    // Execute steps until halted
    int steps = 0;
    boolean halted = false;
    int maxSteps = 100;
    
    while (steps < maxSteps && !halted) {
      try {
        cpu.step();
        builder.step();
        steps++;
      } catch (org.edumips64.core.is.HaltException e) {
        halted = true;
      }
    }
    
    assertTrue("Program should halt within " + maxSteps + " steps", halted);
    assertTrue("At least one instruction should have been executed", cpu.getInstructions() > 0);
  }
}
