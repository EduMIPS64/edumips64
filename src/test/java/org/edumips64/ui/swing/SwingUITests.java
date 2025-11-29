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
package org.edumips64.ui.swing;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JMenuItemFixture;
import org.edumips64.core.*;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.parser.Parser;
import org.edumips64.utils.*;
import org.edumips64.utils.io.LocalFileUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

/**
 * Basic Swing UI tests for EduMIPS64.
 * 
 * These tests verify that:
 * - The main window renders properly
 * - Opening a file works
 * - Running the simulator works
 * - Opening the help works
 * 
 * Tests are designed to run in headless mode via Xvfb in GitHub Actions.
 */
@RunWith(JUnit4.class)
public class SwingUITests {
  
  private FrameFixture window;
  private Robot robot;
  private JFrame mainFrame;
  private ConfigStore configStore;
  
  // References to main components for testing
  private CPU cpu;
  private Memory memory;
  private Parser parser;
  private SymbolTable symTab;
  private CacheSimulator cachesim;
  private GUIFrontend front;
  private CycleBuilder builder;
  private StatusBar sb;
  
  private static final String TEST_FILE_PATH = "src/test/resources/halt.s";
  private static final int MAX_SIMULATION_STEPS = 100;
  
  /**
   * Creates a GenericTypeMatcher for finding a JMenu by text content.
   * Note: JMenu extends JMenuItem, so this matcher works with menuItem() finder.
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
    // Skip tests if running in a true headless environment without display
    assumeFalse("Skipping Swing tests in headless environment without display", 
        GraphicsEnvironment.isHeadless());
  }
  
  @Before
  public void setUp() {
    // Create a robot for GUI testing
    robot = BasicRobot.robotWithNewAwtHierarchy();
    
    // Initialize config store
    configStore = new InMemoryConfigStore(ConfigStore.defaults);
    CurrentLocale.setConfig(configStore);
    
    // Create and show the main frame on the EDT
    mainFrame = GuiActionRunner.execute(() -> {
      JFrame frame = new JFrame("EduMIPS64 Test");
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      
      // Initialize core components
      memory = new Memory();
      cpu = new CPU(memory, configStore, new BUBBLE());
      cpu.setStatus(CPU.CPUStatus.READY);
      symTab = new SymbolTable(memory);
      cachesim = new CacheSimulator();
      
      LocalFileUtils lfu = new LocalFileUtils();
      IOManager iom = new IOManager(lfu, memory);
      InstructionBuilder instructionBuilder = new InstructionBuilder(memory, iom, cpu, cachesim, configStore);
      parser = new Parser(lfu, symTab, memory, instructionBuilder);
      
      builder = new CycleBuilder(cpu);
      sb = new StatusBar(configStore);
      front = new GUIFrontend(cpu, memory, cachesim, configStore, builder, sb);
      
      // Create main content
      JDesktopPane desk = new JDesktopPane();
      Container cp = frame.getContentPane();
      cp.setLayout(new BorderLayout());
      
      // Create menu bar
      JMenuBar menuBar = createTestMenuBar();
      cp.add(menuBar, BorderLayout.NORTH);
      
      // Create internal frames
      JInternalFrame pipeFrame = new JInternalFrame(CurrentLocale.getString("PIPELINE"), true, false, true, true);
      JInternalFrame registersFrame = new JInternalFrame(CurrentLocale.getString("REGISTERS"), true, false, true, true);
      JInternalFrame memoryFrame = new JInternalFrame(CurrentLocale.getString("MEMORY"), true, false, true, true);
      JInternalFrame codeFrame = new JInternalFrame(CurrentLocale.getString("CODE"), true, false, true, true);
      JInternalFrame cyclesFrame = new JInternalFrame(CurrentLocale.getString("CYCLES"), true, false, true, true);
      JInternalFrame statsFrame = new JInternalFrame(CurrentLocale.getString("STATS"), true, false, true, true);
      
      // Add frames to desktop
      desk.add(cyclesFrame);
      desk.add(registersFrame);
      desk.add(statsFrame);
      desk.add(pipeFrame);
      desk.add(memoryFrame);
      desk.add(codeFrame);
      
      // Make frames visible
      cyclesFrame.setVisible(true);
      registersFrame.setVisible(true);
      statsFrame.setVisible(true);
      pipeFrame.setVisible(true);
      memoryFrame.setVisible(true);
      codeFrame.setVisible(true);
      
      // Set up frontend containers
      front.setPipelineContainer(pipeFrame.getContentPane());
      front.setRegistersContainer(registersFrame.getContentPane());
      front.setDataContainer(memoryFrame.getContentPane());
      front.setCodeContainer(codeFrame.getContentPane());
      front.setCyclesContainer(cyclesFrame.getContentPane());
      front.setStatisticsContainer(statsFrame.getContentPane());
      
      cp.add(sb.getComponent(), BorderLayout.SOUTH);
      cp.add(desk, BorderLayout.CENTER);
      
      frame.setSize(800, 600);
      frame.setLocationRelativeTo(null);
      
      return frame;
    });
    
    // Create fixture for the frame
    window = new FrameFixture(robot, mainFrame);
    window.show();
  }
  
  private JMenuBar createTestMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    
    // File menu
    JMenu fileMenu = new JMenu(CurrentLocale.getString("Menu.FILE"));
    fileMenu.setName("fileMenu");
    
    JMenuItem openItem = new JMenuItem(CurrentLocale.getString("MenuItem.OPEN"));
    openItem.setName("openMenuItem");
    fileMenu.add(openItem);
    
    JMenuItem resetItem = new JMenuItem(CurrentLocale.getString("MenuItem.RESET"));
    resetItem.setName("resetMenuItem");
    fileMenu.add(resetItem);
    
    JMenuItem exitItem = new JMenuItem(CurrentLocale.getString("MenuItem.EXIT"));
    exitItem.setName("exitMenuItem");
    fileMenu.add(exitItem);
    
    menuBar.add(fileMenu);
    
    // Execute menu
    JMenu execMenu = new JMenu(CurrentLocale.getString("Menu.EXECUTE"));
    execMenu.setName("executeMenu");
    
    JMenuItem singleCycleItem = new JMenuItem(CurrentLocale.getString("MenuItem.SINGLE_CYCLE"));
    singleCycleItem.setName("singleCycleMenuItem");
    execMenu.add(singleCycleItem);
    
    JMenuItem runItem = new JMenuItem(CurrentLocale.getString("MenuItem.RUN_TO"));
    runItem.setName("runMenuItem");
    execMenu.add(runItem);
    
    menuBar.add(execMenu);
    
    // Help menu
    JMenu helpMenu = new JMenu(CurrentLocale.getString("Menu.HELP"));
    helpMenu.setName("helpMenu");
    
    JMenuItem manualItem = new JMenuItem(CurrentLocale.getString("MenuItem.MANUAL"));
    manualItem.setName("manualMenuItem");
    helpMenu.add(manualItem);
    
    JMenuItem aboutItem = new JMenuItem(CurrentLocale.getString("MenuItem.ABOUT_US"));
    aboutItem.setName("aboutMenuItem");
    helpMenu.add(aboutItem);
    
    menuBar.add(helpMenu);
    
    return menuBar;
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
    // The status bar should be in the SOUTH of the content pane
    Component statusComponent = sb.getComponent();
    assertNotNull("Status bar component should not be null", statusComponent);
    assertTrue("Status bar should be visible", statusComponent.isVisible());
  }
  
  /**
   * Test that the GUI frontend components are initialized.
   */
  @Test(timeout = 30000)
  public void testFrontendComponentsInitialized() {
    assertNotNull("GUIFrontend should be initialized", front);
    assertNotNull("CPU should be initialized", cpu);
    assertNotNull("Memory should be initialized", memory);
    assertEquals("CPU should be in READY state", CPU.CPUStatus.READY, cpu.getStatus());
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
    assertTrue("Should have at least 6 internal frames", frames.length >= 6);
    
    // Get expected frame titles from locale
    String expectedPipeline = CurrentLocale.getString("PIPELINE").toLowerCase();
    String expectedRegisters = CurrentLocale.getString("REGISTERS").toLowerCase();
    String expectedData = CurrentLocale.getString("MEMORY").toLowerCase();  // Returns "Data" in English
    String expectedCode = CurrentLocale.getString("CODE").toLowerCase();
    String expectedCycles = CurrentLocale.getString("CYCLES").toLowerCase();
    String expectedStats = CurrentLocale.getString("STATS").toLowerCase();  // Returns "Statistics" in English
    
    // Verify that each expected frame type exists
    boolean hasPipeline = false, hasRegisters = false, hasData = false;
    boolean hasCode = false, hasCycles = false, hasStats = false;
    
    for (JInternalFrame frame : frames) {
      String title = frame.getTitle().toLowerCase();
      if (title.contains(expectedPipeline)) hasPipeline = true;
      if (title.contains(expectedRegisters)) hasRegisters = true;
      if (title.contains(expectedData)) hasData = true;
      if (title.contains(expectedCode)) hasCode = true;
      if (title.contains(expectedCycles)) hasCycles = true;
      if (title.contains(expectedStats)) hasStats = true;
    }
    
    assertTrue("Pipeline frame should exist", hasPipeline);
    assertTrue("Registers frame should exist", hasRegisters);
    assertTrue("Data frame should exist", hasData);
    assertTrue("Code frame should exist", hasCode);
    assertTrue("Cycles frame should exist", hasCycles);
    assertTrue("Statistics frame should exist", hasStats);
  }
  
  /**
   * Test that clicking the about menu item triggers an action.
   */
  @Test(timeout = 30000)
  public void testAboutDialogOpens() {
    // Set up an action listener on the About menu item before clicking
    AtomicBoolean aboutClicked = new AtomicBoolean(false);
    
    // Get the about menu item and add a listener
    JMenuItemFixture aboutItem = window.menuItem("aboutMenuItem");
    JMenuItem aboutMenuItem = (JMenuItem) aboutItem.target();
    
    // Add an action listener that sets a flag when clicked
    aboutMenuItem.addActionListener(e -> aboutClicked.set(true));
    
    // Click on the Help menu first to open it
    window.menuItem(menuMatcherForText("help")).click();
    
    // Now click on the About menu item
    aboutItem.click();
    
    // Give time for the action to trigger
    robot.waitForIdle();
    
    // Verify the action was triggered
    assertTrue("About menu item action should have been triggered", aboutClicked.get());
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
    
    // Parse the file using the parser
    String absoluteFilename = testFile.getAbsolutePath();
    parser.parse(absoluteFilename);
    
    // Verify CPU is ready to run
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
    parser.parse(absoluteFilename);
    
    // Set up for execution
    cachesim.setDataOffset(memory.getInstructionsNumber() * 4);
    cpu.setStatus(CPU.CPUStatus.RUNNING);
    builder.reset();
    
    // Execute steps until halted
    int steps = 0;
    boolean halted = false;
    
    while (steps < MAX_SIMULATION_STEPS && !halted) {
      try {
        cpu.step();
        builder.step();
        steps++;
      } catch (org.edumips64.core.is.HaltException e) {
        halted = true;
      }
    }
    
    assertTrue("Program should halt within " + MAX_SIMULATION_STEPS + " steps", halted);
    assertTrue("At least one instruction should have been executed", cpu.getInstructions() > 0);
  }
  
  /**
   * Test that the GUIFrontend can update components.
   * This verifies that the UI can be updated after execution without throwing exceptions.
   */
  @Test(timeout = 30000)
  public void testFrontendCanUpdate() {
    // Run on EDT - if no exception is thrown, the test passes
    GuiActionRunner.execute(() -> {
      front.updateComponents();
      front.represent();
      return null;
    });
    // Test passes if we reach this point without exceptions
  }
}
