/* Main.java
 *
 * Entry point of EduMIPS64
 * (c) 2006 Andrea Spadaccini, Antonella Scandura, Vanni Rizzo
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

import org.edumips64.core.*;
import org.edumips64.core.fpu.*;
import org.edumips64.core.is.*;
import org.edumips64.img.*;
import org.edumips64.ui.*;
import org.edumips64.utils.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.KeyStroke.*;
import javax.imageio.ImageIO;

//FPU diagnostics
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/** Entry point of EduMIPS64
 * @author Andrea Spadaccini, Antonella Scandura, Vanni Rizzo
 * */

public class Main extends JApplet {

  public static String VERSION;
  public static String CODENAME;
  public static String build_date;
  public static String git_revision;

  static CPU cpu;
  public static CPUGUIThread cgt;
  static Parser parser;
  static GUIFrontend front;
  static ConfigStore configStore = ConfigManager.getConfig();
  static JFileChooser jfc = new JFileChooser(new File(configStore.getString("lastdir")));

  static JFrame f = null;
  private static JMenuItem open, reset, exit, single_cycle, run_to, multi_cycle, aboutUs, dinero_tracefile, tile, dinFrontend, manual, settings, stop;
  private static StatusBar sb;
  private static JMenu file, lastfiles, exec, config, window, help, lang, tools;
  private static JCheckBoxMenuItem lang_en, lang_it, pipeFrameMI, codeFrameMI;
  private static JCheckBoxMenuItem pipelineJCB, registersJCB, memoryJCB, codeJCB, cyclesJCB, statsJCB, ioJCB;
  private static java.util.List<JCheckBoxMenuItem> frames_menu_items;

  // Stuff for choosing colors
  private static JColorChooser colorCh;
  private static JButton applyColor;
  private static JLabel label;
  private static JFrame frameColorCh;
  private static String[] pipe = {"IF", "ID", "EX", "MEM", "WB"};

  public static GUIIO ioFrame;
  public static IOManager iom;

  public final static Logger log = Logger.getLogger(Main.class.getName());

  private static JInternalFrame pipeFrame, registersFrame, memoryFrame, codeFrame, cyclesFrame, statsFrame;
  private static Map<String, JInternalFrame> mapped_frames;
  private static java.util.List<JInternalFrame> ordered_frames;

  private static String openedFile = null;
  public static boolean debug_mode = false;
  private static JDesktopPane desk;

  private static void usage() {
    showVersion();
    System.out.println(CurrentLocale.getString("HT.Options"));
    System.out.println(CurrentLocale.getString("HT.File"));
    System.out.println(CurrentLocale.getString("HT.Debug"));
    System.out.println(CurrentLocale.getString("HT.Help"));
    System.out.println(CurrentLocale.getString("HT.Reset"));
    System.out.println(CurrentLocale.getString("HT.Version"));
  }

  private static void showVersion() {
    System.out.println("EduMIPS64 version " + VERSION + " (codename: " + CODENAME + ", git revision " + git_revision + ", built on " + build_date + ") - Ciao 'mbare.");
  }

  public static void main(String args[]) {
    // Meta properties.
    VERSION = MetaInfo.get("Signature-Version");
    CODENAME = MetaInfo.get("Codename");
    build_date = MetaInfo.get("Build-Date");
    git_revision = MetaInfo.get("Git-Revision");

    // Checking CLI parameters.
    String toOpen = null;
    boolean printUsageAndExit = false, printVersionAndExit = false;

    if (args.length > 0) {
      for (int i = 0; i < args.length; ++i) {
        if (args[i].compareTo("-f") == 0 || args[i].compareTo("--file") == 0) {
          if (toOpen == null && ++i == args.length) {
            System.err.println(CurrentLocale.getString("HT.MissingFile") + "\n");
            printUsageAndExit = true;
          } else if (toOpen != null) {
            System.err.println(CurrentLocale.getString("HT.MultipleFile") + "\n");
            printUsageAndExit = true;
          } else {
            toOpen = args[i];
          }
        } else if (args[i].compareTo("-d") == 0 || args[i].compareTo("--debug") == 0) {
          debug_mode = true;
        } else if (args[i].compareTo("-h") == 0 || args[i].compareTo("--help") == 0) {
          printUsageAndExit = true;
        } else if (args[i].compareTo("-v") == 0 || args[i].compareTo("--version") == 0) {
          printVersionAndExit = true;
        } else if (args[i].compareTo("-r") == 0 || args[i].compareTo("--reset") == 0) {
          configStore.resetConfiguration();
        } else {
          System.err.println(CurrentLocale.getString("HT.UnrecognizedArgs") + ": " + args[i] + "\n");
          printUsageAndExit = true;
        }

        if (printUsageAndExit) {
          usage();
          return;
        }

        if (printVersionAndExit) {
          showVersion();
          return;
        }
      }
    }


    if (!debug_mode) {
      // Disable logging message whose level is less than WARNING.
      Logger rootLogger = log.getParent();

      for (Handler h : rootLogger.getHandlers()) {
        h.setLevel(java.util.logging.Level.WARNING);
      }
    }

    showVersion();

    // Creating the main JFrame
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
    f = new JFrame("EduMIPS64 v. " + VERSION + " - " + CurrentLocale.getString("PROSIM"));
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // Maximizing the application
    Insets screenInsets = f.getToolkit().getScreenInsets(f.getGraphicsConfiguration());
    Rectangle screenSize = f.getGraphicsConfiguration().getBounds();
    Rectangle maxBounds = new Rectangle(screenInsets.left + screenSize.x,
                                        screenInsets.top + screenSize.y,
                                        screenSize.x + screenSize.width - screenInsets.right - screenInsets.left,
                                        screenSize.y + screenSize.height - screenInsets.bottom - screenInsets.top);
    f.setMaximizedBounds(maxBounds);
    f.setBounds(maxBounds);

    f.setLocation(0, 0);
    Main mm = new Main();
    mm.init();
    f.setVisible(true);
    mm.start();
    log.info("Simulator started");

    if (toOpen != null) {
      resetSimulator(false);
      openFile(toOpen);
      addFileToRecentMenu(toOpen);
    }

    f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);

//debugging code
    //BigDecimal bd=new BigDecimal("1e-23");
    //BigInteger bi=FPInstructionUtils.doubleTo64FixedPoint(bd,CPU.FPRoundingMode.TOWARDS_PLUS_INFINITY);
    //System.out.println(bi.longValue());
  }

  private static void addFrame(String name, JInternalFrame f) {
    mapped_frames.put(name, f);
    ordered_frames.add(f);

    try {
      f.setFrameIcon(new ImageIcon(IMGLoader.getImage(name + ".png")));
    } catch (IOException e) {
      e.printStackTrace();
    }

    desk.add(f);
    f.setVisible(true);
  }

  public void init() {
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
    iom = IOManager.getInstance();
    
    desk = new JDesktopPane();
    Container cp = (f == null) ? getContentPane() : f.getContentPane();
    cp.setLayout(new BorderLayout());
    frames_menu_items = new ArrayList<JCheckBoxMenuItem>();
    cp.add(createMenuBar(), BorderLayout.NORTH);

    Toolkit tk = (f == null) ? getToolkit() : f.getToolkit();
    cpu = CPU.getInstance();
    cpu.setStatus(CPU.CPUStatus.READY);

    front = new GUIFrontend();

    cgt = new CPUGUIThread();
    cgt.start();

    parser = Parser.getInstance();

    // Internal Frames
    pipeFrame = new JInternalFrame("Pipeline", true, false, true, true);
    pipeFrame.addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameIconified(InternalFrameEvent e) {
        pipelineJCB.setState(false);
      }
      public void internalFrameDeiconified(InternalFrameEvent e) {
        pipelineJCB.setState(true);
      }
    });
    registersFrame = new JInternalFrame(CurrentLocale.getString("REGISTERS"), true, false, true, true);
    registersFrame.addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameIconified(InternalFrameEvent e) {
        registersJCB.setState(false);
      }
      public void internalFrameDeiconified(InternalFrameEvent e) {
        registersJCB.setState(true);
      }
    });
    memoryFrame = new JInternalFrame(CurrentLocale.getString("MEMORY"), true, false, true, true);
    memoryFrame.addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameIconified(InternalFrameEvent e) {
        memoryJCB.setState(false);
      }
      public void internalFrameDeiconified(InternalFrameEvent e) {
        memoryJCB.setState(true);
      }
    });
    codeFrame = new JInternalFrame(CurrentLocale.getString("CODE"), true, false, true, true);
    codeFrame.addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameIconified(InternalFrameEvent e) {
        codeJCB.setState(false);
      }
      public void internalFrameDeiconified(InternalFrameEvent e) {
        codeJCB.setState(true);
      }
    });
    cyclesFrame = new JInternalFrame(CurrentLocale.getString("CYCLES"), true, false, true, true);
    cyclesFrame.addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameIconified(InternalFrameEvent e) {
        cyclesJCB.setState(false);
      }
      public void internalFrameDeiconified(InternalFrameEvent e) {
        cyclesJCB.setState(true);
      }
    });
    statsFrame = new JInternalFrame(CurrentLocale.getString("STATS"), true, false, true, true);
    statsFrame.addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameIconified(InternalFrameEvent e) {
        statsJCB.setState(false);
      }
      public void internalFrameDeiconified(InternalFrameEvent e) {
        statsJCB.setState(true);
      }
    });

    ioFrame = new GUIIO(CurrentLocale.getString("IO"), true, false, true, true);
    ioFrame.addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameIconified(InternalFrameEvent e) {
        ioJCB.setState(false);
      }
      public void internalFrameDeiconified(InternalFrameEvent e) {
        ioJCB.setState(true);
      }
    });

    // Set the IOManager StdOutput with a Proxy writer from GUIIO
    iom.setStdOutput(ioFrame.getWriter());
    
    // Set the IOManager StdError with a Proxy writer from GUIIO
    iom.setStdError(ioFrame.getWriter());
    
    // Needed for internal frames handling
    // TODO: The actual approach is a workaround. To do things in the right
    // way, a new class that derives from JInternalFrame should be created,
    // with the "name" and "priority" attributes, and every internal frame
    // should derive from it, in order to remove the need for those two
    // structures. But time is running out, this is not my main task but
    // only something I need in order to achieve my objective. Sorry.
    // --andrea
    mapped_frames = new HashMap<String, JInternalFrame>();
    ordered_frames = new ArrayList<JInternalFrame>();

    addFrame("cycles", cyclesFrame);
    addFrame("registers", registersFrame);
    addFrame("stats", statsFrame);
    addFrame("pipeline", pipeFrame);
    addFrame("memory", memoryFrame);
    addFrame("code", codeFrame);
    addFrame("io", ioFrame);

    // Setting icons for the main frame and for the internal frames
    try {
      if (f != null) {
        f.setIconImage(IMGLoader.getImage("ico.png"));
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    front.setPipelineContainer(pipeFrame.getContentPane());
    front.setRegistersContainer(registersFrame.getContentPane());
    front.setDataContainer(memoryFrame.getContentPane());
    front.setCodeContainer(codeFrame.getContentPane());
    front.setCyclesContainer(cyclesFrame.getContentPane());
    front.setStatisticsContainer(statsFrame.getContentPane());

    sb = new StatusBar();
    cp.add(sb.getComponent(), BorderLayout.SOUTH);
    cp.add(desk, BorderLayout.CENTER);

    changeShownMenuItems(cpu.getStatus());
  }

  public void start() {
    // Auto-minimze the log window and the I/O window
    try {
      ioFrame.setIcon(true);
    } catch (java.beans.PropertyVetoException e) {}

    tileWindows();
  }

  /** Gets a reference to the status bar */
  public static StatusBar getSB() {
    return sb;
  }

  /** Updates the configuration parameters of CPUGUIThread */
  public static void updateCGT() {
    cgt.updateConfigValues();
  }

  /** Changes the status of running menu items.
   *  @param status a boolean
   */
  public static void setRunningMenuItemsStatus(boolean status) {
    single_cycle.setEnabled(status);
    multi_cycle.setEnabled(status);
    run_to.setEnabled(status);
  }

  /** Changes the status of cache-related menu items.
   *  @param status a boolean
   */
  private static void setCacheMenuItemsStatus(boolean status) {
    tools.setEnabled(status);
    dinero_tracefile.setEnabled(status);
    dinFrontend.setEnabled(status);
  }

  /** Enables or disables the Stop menu item and the Settings menu item.
   *  @param status boolean
   */
  public static void setStopStatus(boolean status) {
    stop.setEnabled(status);
    settings.setEnabled(!status);
    file.setEnabled(!status);
  }

  /** Changes the menu items to show, according to the CPU status.
   *    If the CPU status is READY, then we must make unavailable running menu
   *    items and cache-related menu items; if it's RUNNING, we must show
   *    running menu items but cache menu items should be hidden; in the HALTED
   *    state, only cache-related menu items must be shown, while running menu
   *    items must be hidden.
   *
   *    @param s the new CPU status
   * */
  public static void changeShownMenuItems(CPU.CPUStatus s) {
    if (s == CPU.CPUStatus.READY) {
      log.info("CPU Ready");
      setCacheMenuItemsStatus(false);
      setRunningMenuItemsStatus(false);
    } else if (s == CPU.CPUStatus.RUNNING) {
      log.info("CPU Running");
      setCacheMenuItemsStatus(false);
      setRunningMenuItemsStatus(true);
    } else if (s == CPU.CPUStatus.STOPPING) {
      log.info("CPU Stopping");
      setCacheMenuItemsStatus(false);
      setRunningMenuItemsStatus(true);
    } else if (s == CPU.CPUStatus.HALTED) {
      log.info("CPU Halted");
      setCacheMenuItemsStatus(true);
      setRunningMenuItemsStatus(false);
    }
  }

  /** Opens a file. */
  private static void openFile(String file) {
    log.info("Trying to open " + file);
    cpu.reset();

    try {
      // Aggiorniamo i componenti gai
      front.updateComponents();
      front.represent();
    } catch (Exception ex) {
      new ReportDialog(f, ex, CurrentLocale.getString("GUI_STEP_ERROR"));
    }

    try {
      log.info("Before parsing");

      try {
        parser.parse(file);
      } catch (ParserMultiWarningException pmwe) {
        new ErrorDialog(f, pmwe.getExceptionList(), CurrentLocale.getString("GUI_PARSER_ERROR"));
      } catch (NullPointerException e) {
        log.info("NullPointerException: " + e.toString());
        e.printStackTrace();
      }

      log.info("After parsing");

      // The file has correctly been parsed
      cpu.setStatus(CPU.CPUStatus.RUNNING);
      log.info("Set the status to RUNNING");

      // Let's fetch the first instruction
      synchronized (cgt) {
        cgt.setSteps(1);
        cgt.notify();
      }

      openedFile = file;
      log.info("File " + file + " successfully opened");
      StringTokenizer token = new StringTokenizer(file, File.separator, false);
      String nome_file = null;

      while (token.hasMoreElements()) {
        nome_file = token.nextToken();
      }

      f.setTitle("EduMIPS64 v. " + VERSION + " - " + CurrentLocale.getString("PROSIM") + " - " + nome_file);
    } catch (ParserMultiException ex) {
      log.info("Error opening " + file);
      new ErrorDialog(f, ex.getExceptionList(), CurrentLocale.getString("GUI_PARSER_ERROR"));
      openedFile = null;
      f.setTitle("EduMIPS64 v. " + VERSION + " - " + CurrentLocale.getString("PROSIM"));
      resetSimulator(false);
    } catch (java.io.FileNotFoundException ex) {
      String tmpfile;

      if (ex.getMessage().indexOf("(") != -1) {
        tmpfile = ex.getMessage().substring(0, ex.getMessage().indexOf("("));
      } else {
        tmpfile = ex.getMessage();
      }

      f.setTitle("EduMIPS64 v. " + VERSION + " - " + CurrentLocale.getString("PROSIM"));
      log.info("File not found: " + tmpfile);
      JOptionPane.showMessageDialog(f, CurrentLocale.getString("FILE_NOT_FOUND") + ": " + tmpfile, "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
      f.setTitle("EduMIPS64 v. " + VERSION + " - " + CurrentLocale.getString("PROSIM"));
      log.info("Error opening " + file);
      new ReportDialog(f, e, CurrentLocale.getString("ERROR"));
    }
  }

  /** Tiles windows. */
  private static void tileWindows() {
    // First of all, we don't have to consider iconified frames, because
    // the frames to be tiled are the ones that aren't iconified
    java.util.List<JInternalFrame> list = new ArrayList<JInternalFrame>();

    for (JInternalFrame f : ordered_frames) {
      if (!f.isIcon()) {
        list.add(f);
      }
    }

    int count = list.size();

    // Very special cases
    if (count == 0) {
      return;
    }

    Dimension size = desk.getSize();

    if (count == 1) {
      desk.getDesktopManager().resizeFrame(list.get(0), 0, 0, size.width, size.height);
    }

    // Ok, let's start with the code.
    int maxFramesInARow = 3;        // Maybe it'll be customizable...
    int rows = (new Double(Math.ceil(((double) count) / ((double) maxFramesInARow)))).intValue();
    int cols = maxFramesInARow;

    // Optimization of the space
    if (count % 2 == 0 && count % maxFramesInARow != 0) {
      cols--;
    }

    int w = size.width / cols;
    int h = size.height / rows;
    int x = 0;
    int y = 0;

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        int frame_number = (i * cols) + j;

        if (frame_number >= count) {
          break;
        }

        JInternalFrame f = list.get(frame_number);

        if (f.isIcon()) {
          continue;
        }

        desk.getDesktopManager().resizeFrame(f, x, y, w, h);
        x += w;
      }

      y += h; // start the next row
      x = 0;
    }
  }

  /** Sets the frame titles. Used when the locale is changed. */
  private static void setFrameTitles() {
    if (f != null) {
      if (openedFile != null) {
        f.setTitle("EduMIPS64 v. " + VERSION + " - " + CurrentLocale.getString("PROSIM") + " - " + openedFile);
      } else {
        f.setTitle("EduMIPS64 v. " + VERSION + " - " + CurrentLocale.getString("PROSIM"));
      }
    }

    for (Map.Entry<String, JInternalFrame>e : mapped_frames.entrySet()) {
      e.getValue().setTitle(CurrentLocale.getString(e.getKey().toUpperCase()));
    }
  }

  public static void resetSimulator(boolean reopenFile) {
    cpu.reset();

    try {
      iom.reset();
    } catch (IOException e1) {
      log.info("I/O error while resetting IOManager");
    }

    cpu.setStatus(CPU.CPUStatus.READY);
    front.updateComponents();
    front.represent();

    if (openedFile != null && reopenFile) {
      openFile(openedFile);
    }

    changeShownMenuItems(cpu.getStatus());
  }

  /** Sets the menu items captions, adding, if possible, the mnemonic. */
  private static void initMenuItems() {
    setMenuItem(file, "Menu.FILE");
    setMenuItem(exec, "Menu.EXECUTE");
    setMenuItem(config, "Menu.CONFIGURE");
    setMenuItem(window, "Menu.WINDOW");
    setMenuItem(help, "Menu.HELP");
    setMenuItem(lang, "Menu.CHANGE_LANGUAGE");
    setMenuItem(tools, "Menu.TOOLS");
    setMenuItem(open, "MenuItem.OPEN");
    setMenuItem(lastfiles, "MenuItem.OPENLAST");
    setMenuItem(reset, "MenuItem.RESET");
    setMenuItem(exit, "MenuItem.EXIT");
    setMenuItem(single_cycle, "MenuItem.SINGLE_CYCLE");
    setMenuItem(run_to, "MenuItem.RUN_TO");
    setMenuItem(multi_cycle, "MenuItem.MULTI_CYCLE");
    setMenuItem(lang_en, "MenuItem.ENGLISH");
    setMenuItem(lang_it, "MenuItem.ITALIAN");
    setMenuItem(dinero_tracefile, "MenuItem.DIN_TRACEFILE");
    setMenuItem(aboutUs, "MenuItem.ABOUT_US");
    setMenuItem(dinFrontend, "MenuItem.DIN_FRONTEND");
    setMenuItem(manual, "MenuItem.MANUAL");
    setMenuItem(settings, "Config.ITEM");
    setMenuItem(stop, "MenuItem.STOP");
    setMenuItem(pipelineJCB, "PIPELINE");
    setMenuItem(codeJCB, "CODE");
    setMenuItem(cyclesJCB, "CYCLES");
    setMenuItem(memoryJCB, "MEMORY");
    setMenuItem(statsJCB, "STATS");
    setMenuItem(registersJCB, "REGISTERS");
    setMenuItem(ioJCB, "IO");
  }

  public static boolean isWindows() {
    return System.getProperty("os.name").substring(0, 3).equalsIgnoreCase("win");
  }

  /** Creates a new menu bar.
   *  @return the menu bar
   */
  private static JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();

    // Creation of all menus and menu items
    file = new JMenu();
    lastfiles = new JMenu();
    exec = new JMenu();
    config = new JMenu();
    window = new JMenu();
    help = new JMenu();
    lang = new JMenu();
    tools = new JMenu();

    open = new JMenuItem();
    reset = new JMenuItem();
    exit = new JMenuItem();
    dinero_tracefile = new JMenuItem();
    single_cycle = new JMenuItem();
    run_to = new JMenuItem();
    multi_cycle = new JMenuItem();
    stop = new JMenuItem();
    tile = new JMenuItem();
    dinFrontend = new JMenuItem();
    manual = new JMenuItem();
    settings = new JMenuItem();
    pipelineJCB = new JCheckBoxMenuItem();
    codeJCB = new JCheckBoxMenuItem();
    memoryJCB = new JCheckBoxMenuItem();
    registersJCB = new JCheckBoxMenuItem();
    statsJCB = new JCheckBoxMenuItem();
    cyclesJCB = new JCheckBoxMenuItem();
    ioJCB = new JCheckBoxMenuItem();

    // Adding menus to the menu bar
    mb.add(file);
    mb.add(exec);
    mb.add(config);
    mb.add(tools);
    mb.add(window);
    mb.add(help);

    // ---------------- FILE MENU
    // Open file
    file.add(open);
    open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
    open.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int val = jfc.showOpenDialog(f);

        if (val == JFileChooser.APPROVE_OPTION) {
          String filename = jfc.getSelectedFile().getPath();
          configStore.putString("lastdir", jfc.getCurrentDirectory().getAbsolutePath());
          addFileToRecentMenu(filename);

          resetSimulator(false);
          openFile(filename);
          changeShownMenuItems(cpu.getStatus());
        }
      }
    });

    // Add recently opened files menu items to the recent files submenu.
    for (String filename : Arrays.asList(configStore.getString("files").split(File.pathSeparator))) {
      if (filename.length() > 0) {
        log.info("Adding '" + filename + "' to recently opened files.");
        addFileToRecentMenu(filename);
      }
    }

    file.add(lastfiles);


    // Reset the simulator and the CPU
    file.add(reset);
    reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
    reset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetSimulator(true);
      }
    });

    // Write Dinero Tracefile
    file.add(dinero_tracefile);
    dinero_tracefile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
    dinero_tracefile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jfc.setSelectedFile(new File(openedFile + ".xdin"));
        int val = jfc.showSaveDialog(f);

        if (val == JFileChooser.APPROVE_OPTION) {
          String filename = jfc.getSelectedFile().getPath();
          Dinero din = Dinero.getInstance();

          try {
            din.WriteXdinFile(filename);
            log.info("Wrote dinero tracefile");
          } catch (Exception ex) {
            ex.printStackTrace();
            log.info("Exception in DineroTracefile: " + ex);
          }
        }
      }
    });


    // Exit
    file.add(exit);
    exit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });


    // ---------------- EXECUTE MENU
    // Execute a single simulation step
    exec.add(single_cycle);
    single_cycle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
    single_cycle.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cgt.setSteps(1);

        synchronized (cgt) {
          cgt.notify();
        }
      }
    });

    // Execute the whole program
    exec.add(run_to);
    run_to.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
    run_to.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cgt.setSteps(-1);

        synchronized (cgt) {
          cgt.notify();
        }
      }
    });

    // Execute a fixed number of steps
    exec.add(multi_cycle);
    multi_cycle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
    multi_cycle.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cgt.setSteps(configStore.getInt("n_step"));

        synchronized (cgt) {
          cgt.notify();
        }
      }
    });

    // Stops the execution
    exec.add(stop);
    stop.setEnabled(false);
    stop.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
    stop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cgt.stopExecution();
      }
    });

    // ---------------- CONFIGURE MENU

    config.add(settings);
    settings.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new GUIConfig(f);
      }
    });
    // ---------------- LANGUAGE MENU
    Toolkit tk  = lang.getToolkit();
    config.add(lang);

    try {
      lang_en = new JCheckBoxMenuItem(
        CurrentLocale.getString("MenuItem.ENGLISH"),
        new ImageIcon(IMGLoader.getImage("en.png")),
        CurrentLocale.isSelected("en"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    lang.add(lang_en);

    lang_en.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        lang_en.setState(true);
        lang_it.setState(false);
        CurrentLocale.setLanguage("en");
        initMenuItems();
        setFrameTitles();
        front.updateLanguageStrings();
        // f.setVisible(true);
      }
    });

    try {
      lang_it = new JCheckBoxMenuItem(
        CurrentLocale.getString("MenuItem.ITALIAN"),
        new ImageIcon(IMGLoader.getImage("it.png")),
        CurrentLocale.isSelected("it"));

      lang.add(lang_it);
    } catch (IOException e) {
      e.printStackTrace();
    }

    lang_it.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        lang_it.setState(true);
        lang_en.setState(false);
        CurrentLocale.setLanguage("it");
        initMenuItems();
        setFrameTitles();
        front.updateLanguageStrings();
        // f.setVisible(true);
      }
    });


    // ---------------- HELP MENU

    manual = new JMenuItem();
    help.add(manual);
    manual.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // GUIManual man = new GUIManual(f, CurrentLocale.getString("INTRO_FILENAME"), CurrentLocale.getString("IS_FILENAME"), CurrentLocale.getString("GUI_FILENAME"),CurrentLocale.getString("SYSCALL_FILENAME"));
        // man.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);    // In order to avoid that closing the manual exits the program
        // man.setVisible(true);
        try {
          org.edumips64.ui.GUIHelp.showHelp(null, "id");
        } catch (Exception exx) {
          new org.edumips64.ui.ReportDialog(null, exx, "MIAO");
        }
      }
    });

    aboutUs = new JMenuItem(CurrentLocale.getString("MenuItem.ABOUT_US"));
    help.add(aboutUs);
    aboutUs.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        GUIAbout ab = new GUIAbout(null);
        //ab.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        ab.setVisible(true);
      }
    });
    // ---------------- TOOLS MENU
    dinFrontend.setEnabled(false);
    dinFrontend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JDialog dinFrame = new DineroFrontend(f);
        dinFrame.setModal(true);
        dinFrame.setVisible(true);
      }
    });
    tools.add(dinFrontend);

    // ---------------- WINDOW MENU
    //
    tile = new JMenuItem("Tile");
    tile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        tileWindows();
      }
    });
    window.add(tile);
    window.addSeparator();

    pipelineJCB.setText(CurrentLocale.getString("pipeline".toUpperCase()));
    pipelineJCB.setState(true);
    pipelineJCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean cur_state = mapped_frames.get("pipeline").isIcon();

        try {
          mapped_frames.get("pipeline").setIcon(!cur_state);
        } catch (java.beans.PropertyVetoException ex) {}
      }
    });
    frames_menu_items.add(pipelineJCB);
    window.add(pipelineJCB);

    cyclesJCB.setText(CurrentLocale.getString("cycles".toUpperCase()));
    cyclesJCB.setState(true);
    cyclesJCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean cur_state = mapped_frames.get("cycles").isIcon();

        try {
          mapped_frames.get("cycles").setIcon(!cur_state);
        } catch (java.beans.PropertyVetoException ex) {}
      }
    });
    frames_menu_items.add(cyclesJCB);
    window.add(cyclesJCB);

    registersJCB.setText(CurrentLocale.getString("registers".toUpperCase()));
    registersJCB.setState(true);
    registersJCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean cur_state = mapped_frames.get("registers").isIcon();

        try {
          mapped_frames.get("registers").setIcon(!cur_state);
        } catch (java.beans.PropertyVetoException ex) {}
      }
    });
    frames_menu_items.add(registersJCB);
    window.add(registersJCB);

    statsJCB.setText(CurrentLocale.getString("stats".toUpperCase()));
    statsJCB.setState(true);
    statsJCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean cur_state = mapped_frames.get("stats").isIcon();

        try {
          mapped_frames.get("stats").setIcon(!cur_state);
        } catch (java.beans.PropertyVetoException ex) {}
      }
    });
    frames_menu_items.add(statsJCB);
    window.add(statsJCB);

    memoryJCB.setText(CurrentLocale.getString("memory".toUpperCase()));
    memoryJCB.setState(true);
    memoryJCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean cur_state = mapped_frames.get("memory").isIcon();

        try {
          mapped_frames.get("memory").setIcon(!cur_state);
        } catch (java.beans.PropertyVetoException ex) {}
      }
    });
    frames_menu_items.add(memoryJCB);
    window.add(memoryJCB);

    codeJCB.setText(CurrentLocale.getString("code".toUpperCase()));
    codeJCB.setState(true);
    codeJCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean cur_state = mapped_frames.get("code").isIcon();

        try {
          mapped_frames.get("code").setIcon(!cur_state);
        } catch (java.beans.PropertyVetoException ex) {}
      }
    });
    frames_menu_items.add(codeJCB);
    window.add(codeJCB);

    ioJCB.setText(CurrentLocale.getString("log".toUpperCase()));
    ioJCB.setState(true);
    ioJCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean cur_state = mapped_frames.get("io").isIcon();

        try {
          mapped_frames.get("io").setIcon(!cur_state);
        } catch (java.beans.PropertyVetoException ex) {}
      }
    });
    frames_menu_items.add(ioJCB);
    window.add(ioJCB);
    initMenuItems();
    return mb;
  }

  /** add a new JMenuItem in recent file menu at the position pos for the file "namefile"*/
  private static void addFileToRecentMenu(final String filename) {
    JMenuItem item = new JMenuItem(filename);
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetSimulator(false);
        openFile(filename);
        addFileToRecentMenu(filename);
        changeShownMenuItems(cpu.getStatus());
      }
    });

    // Add at the top of the list.
    lastfiles.insert(item, 0);

    // Remove, if present, the old entry pointing to the file just opened.
    for (int i = 1; i < lastfiles.getItemCount(); ++i) {
      String cur = ((JMenuItem)lastfiles.getMenuComponent(i)).getText();

      if (cur.equals(item.getText())) {
        lastfiles.remove(i);
      }
    }

    // Trim length.
    if (lastfiles.getItemCount() > 6) {
      lastfiles.remove(6);
    }

    // Update configuration.
    String files = new String();

    for (Component c : lastfiles.getMenuComponents()) {
      if (c instanceof JMenuItem) {
        files += ((JMenuItem)c).getText() + File.pathSeparator;
      }
    }

    files = files.substring(0, files.length() - 1);
    configStore.putString("files", files);
  }

  /** Sets the caption of the menu item, adding, if possible, the mnemonic */
  private static void setMenuItem(JMenuItem item, String caption) {
    String localCaption = CurrentLocale.getString(caption);
    int pos = localCaption.indexOf("_");

    if (pos >= 0) {
      char mnemonic = localCaption.charAt(pos + 1);

      // The KeyEvent.VK_* constants refer to uppercase letters, so we
      // need to make the mnemonic uppercase.
      mnemonic = Character.toUpperCase(mnemonic);
      item.setMnemonic((int) mnemonic);

      // Deleting the _ character
      StringBuffer newLocalCaption = new StringBuffer();

      for (int i = 0; i < localCaption.length(); ++i)
        if (localCaption.charAt(i) != '_') {
          newLocalCaption.append(localCaption.charAt(i));
        }

      localCaption = newLocalCaption.toString();
    }

    item.setText(localCaption);
  }
  private static void openColorChooser(Color col) {
    frameColorCh = new JFrame();
    frameColorCh.setTitle(CurrentLocale.getString("CHOOSE_COLOR"));
    frameColorCh.setLocation(400, 200);
    Container c = frameColorCh.getContentPane();
    colorCh = new JColorChooser();
    colorCh.setColor(col);
    applyColor = new JButton("applica");
    c.add(applyColor, BorderLayout.SOUTH);
    c.add(colorCh, BorderLayout.CENTER);
    frameColorCh.pack();
    frameColorCh.setVisible(true);
  }

  public static GUIFrontend getGUIFrontend() {
    return front;
  }

  public static JFrame getMainFrame() {
    return f;
  }

  public static void startPB() {
    sb.startPB();
  }

  public static void stopPB() {
    sb.stopPB();
  }
}
