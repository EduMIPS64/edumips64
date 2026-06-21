package org.edumips64.ui.swing;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JFileChooserFixture;
import org.assertj.swing.finder.JFileChooserFinder;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.launcher.ApplicationLauncher;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.edumips64.Main;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.awt.GraphicsEnvironment;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import static org.junit.Assert.assertNotNull;

/** Basic smoke tests for the Swing frontend. */
public class MainSwingTest {
  private static final long UI_TIMEOUT_MILLIS = 10000;
  private static final Timeout UI_TIMEOUT = Timeout.timeout(UI_TIMEOUT_MILLIS);

  private Robot robot;
  private FrameFixture window;

  @Before
  public void setUp() {
    Assume.assumeFalse("Swing UI tests need a display", GraphicsEnvironment.isHeadless());
    robot = BasicRobot.robotWithNewAwtHierarchy();
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

  @Test
  @GUITest
  public void testMainWindowRendersWithEnabledMenuItems() {
    launchApplication();

    window.requireVisible();
    window.menuItemWithPath("File", "Open...").requireEnabled();
    window.menuItemWithPath("Help", "Manual...").requireEnabled();
  }

  @Test
  @GUITest
  public void testOpenFileAndRunSimulatorToHalt() throws Exception {
    launchApplication();

    Path program = testResourcePath("halt.s");
    window.menuItemWithPath("File", "Open...").click();
    JFileChooserFixture fileChooser = JFileChooserFinder.findFileChooser().withTimeout(UI_TIMEOUT_MILLIS).using(robot);
    fileChooser.selectFile(program.toFile());
    fileChooser.approve();

    waitForMainWindowTitle("halt.s");
    window.menuItemWithPath("Execute", "Run").click();
    waitForCpuStatus("HALTED");
    window.label("cpuStatusLabel").requireText("HALTED  ");
  }

  @Test
  @GUITest
  public void testOpenHelpManualWindow() {
    launchApplication();

    window.menuItemWithPath("Help", "Manual...").click();
    FrameFixture helpWindow = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
      @Override
      protected boolean isMatching(JFrame frame) {
        return "EduMIPS64 Help".equals(frame.getTitle()) && frame.isShowing();
      }
    }).withTimeout(UI_TIMEOUT_MILLIS).using(robot);

    helpWindow.requireVisible();
    helpWindow.close();
  }

  private void launchApplication() {
    ApplicationLauncher.application(Main.class).withArgs("--reset", "--no-banner").start();
    window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
      @Override
      protected boolean isMatching(JFrame frame) {
        return frame.getTitle().startsWith("EduMIPS64 v.") && frame.isShowing();
      }
    }).withTimeout(UI_TIMEOUT_MILLIS).using(robot);
    GuiActionRunner.execute(() -> ((JFrame) window.target()).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE));
  }

  private Path testResourcePath(String resourceName) throws URISyntaxException {
    URL resource = getClass().getClassLoader().getResource(resourceName);
    assertNotNull("Missing test resource " + resourceName, resource);
    return Path.of(resource.toURI());
  }

  private void waitForMainWindowTitle(String titlePart) {
    Pause.pause(new Condition("main window title contains " + titlePart) {
      @Override
      public boolean test() {
        return GuiActionRunner.execute(() -> window.target().getTitle().contains(titlePart));
      }
    }, UI_TIMEOUT);
  }

  private void waitForCpuStatus(String status) {
    Pause.pause(new Condition("CPU status is " + status) {
      @Override
      public boolean test() {
        return status.equals(GuiActionRunner.execute(() -> window.label("cpuStatusLabel").target().getText().trim()));
      }
    }, UI_TIMEOUT);
  }
}
