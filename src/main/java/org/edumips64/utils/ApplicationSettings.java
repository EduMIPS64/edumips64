package org.edumips64.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;

public class ApplicationSettings {
  public final static String VERSION;
  public final static String CODENAME;
  public final static String BUILD_DATE;
  public final static String GIT_REVISION;

  public static ConfigStore configStore;
  private static boolean debug_mode = false;

  static {
    VERSION = MetaInfo.get("Signature-Version");
    CODENAME = MetaInfo.get("Codename");
    BUILD_DATE = MetaInfo.get("Build-Date");
    GIT_REVISION = MetaInfo.get("Git-Revision");
    configStore = new JavaPrefsConfigStore(ConfigStore.defaults);
  }

  // Parses the command-line arguments.
  // Returns the filename to open (if any) and sets parameters such as logging. If necessary, exits.
  public static String parseArgsOrExit(String[] args) {
    // Checking CLI parameters.
    String toOpen = null;
    boolean printUsageAndExit = false;
    boolean printVersionAndExit = false;

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
          System.exit(0);
        }

        if (printVersionAndExit) {
          showVersion();
          System.exit(0);
        }
      }
    }

    if (!debug_mode) {
      Logger log = Logger.getLogger(ApplicationSettings.class.getName());
      // Disable logging message whose level is less than WARNING.
      Logger rootLogger = log.getParent();

      for (Handler h : rootLogger.getHandlers()) {
        h.setLevel(Level.WARNING);
      }
    }
    return toOpen;
  }

  private static void usage() {
    showVersion();
    System.out.println(CurrentLocale.getString("HT.Options"));
    System.out.println(CurrentLocale.getString("HT.File"));
    System.out.println(CurrentLocale.getString("HT.Debug"));
    System.out.println(CurrentLocale.getString("HT.Help"));
    System.out.println(CurrentLocale.getString("HT.Reset"));
    System.out.println(CurrentLocale.getString("HT.Version"));
  }

  public static void showVersion() {
    System.out.println("EduMIPS64 version " + VERSION + " (codename: " + CODENAME + ", git revision " + GIT_REVISION + ", built on " + BUILD_DATE + ") - Ciao 'mbare.");
  }
}
