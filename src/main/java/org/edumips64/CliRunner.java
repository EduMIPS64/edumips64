/* CliRunner.java
 *
 * Interactive shell for EduMIPS64
 *
 * (c) 2006 Andrea Spadaccini
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 */
package org.edumips64;

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.cli.Args;
import org.edumips64.utils.cli.Cli;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** Interactive shell for EduMIPS64
 * @author Andrea Spadaccini
 * */
public class CliRunner {
  private final ConfigStore configStore;
  private final Args args;

  public CliRunner(ConfigStore cfg, Args commandLineArgs) {
    this.configStore = cfg;
    this.args = commandLineArgs;
  }

  public void start() {
    try {
      Cli cli = new Cli(configStore, args.isVerbose());
      CommandLine commandLine = new CommandLine(cli);
      installFriendlyExceptionHandlers(commandLine);

      if (args.isVerbose()) {
        System.out.println(Ansi.AUTO.string(
            "@|faint " + CurrentLocale.getString("CLI.WELCOME") + "|@"));
      }
      if (args.getFileName() != null) {
          commandLine.execute("load", args.getFileName());
      }

      runReplLoop(cli, commandLine);

    } catch (Exception e) {
      Cli.printErrorMessage(e);
      System.exit(1);
    }
  }

  private void runReplLoop(Cli cli, CommandLine commandLine) throws IOException {
      BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
      printPrompt(cli);
      while (true) {
          String read = keyboard.readLine();
          if (read == null) {
              // EOF (Ctrl+D / piped script ended) -> exit cleanly.
              System.out.println();
              System.out.println(CurrentLocale.getString("CLI.EXIT"));
              return;
          }
          handleTokens(commandLine, read.trim().split("\\s+"));
          printPrompt(cli);
        }
    }

  private void printPrompt(Cli cli) {
    String status = cli.getCpuStatus();
    String coloredStatus = colorForStatus(status);
    String prompt = Ansi.AUTO.string(
        "@|bold,fg(220) edumips64|@ "
        + "@|faint [|@" + coloredStatus + "@|faint ]|@ "
        + "@|bold,fg(cyan) >|@ ");
    System.out.print(prompt);
    System.out.flush();
  }

  /** Picks an ANSI color hint for a CPU status string. */
  private String colorForStatus(String status) {
    String tag;
    switch (status) {
      case "RUNNING":
        tag = "bold,fg(green)"; break;
      case "HALTED":
        tag = "bold,fg(red)"; break;
      case "STOPPING":
        tag = "bold,fg(yellow)"; break;
      case "READY":
      default:
        tag = "bold,fg(cyan)"; break;
    }
    return "@|" + tag + " " + status + "|@";
  }

  private void handleTokens(CommandLine commandLine, String[] tokens) {
    // After a String.trim().split("\\s+") an empty input yields {""}.
    if (tokens.length == 0 || (tokens.length == 1 && tokens[0].isEmpty())) {
      printHelp(commandLine);
      return;
    }
    commandLine.execute(tokens);
  }

  private void printHelp(CommandLine c) {
    c.usage(System.out);
  }

  /**
   * Replace picocli's default exception output (a stack trace + full usage
   * dump) with a friendly one-liner styled like the rest of the shell.
   * Keeps the shell responsive: a typo doesn't drown the screen.
   */
  private void installFriendlyExceptionHandlers(CommandLine commandLine) {
    IParameterExceptionHandler paramHandler = new IParameterExceptionHandler() {
      @Override
      public int handleParseException(ParameterException ex, String[] cmdArgs) {
        System.err.println(Ansi.AUTO.string(
            "@|fg(red) " + CurrentLocale.getString("CLI.UNKNOWN.COMMAND")
            + "|@ " + ex.getMessage()));
        System.err.println(Ansi.AUTO.string(
            "@|faint " + CurrentLocale.getString("CLI.UNKNOWN.COMMAND.HINT")
            + "|@"));
        return commandLine.getCommandSpec().exitCodeOnInvalidInput();
      }
    };
    IExecutionExceptionHandler execHandler = new IExecutionExceptionHandler() {
      @Override
      public int handleExecutionException(Exception ex, CommandLine cmd, ParseResult parseResult) {
        Cli.printErrorMessage(ex);
        return cmd.getCommandSpec().exitCodeOnExecutionException();
      }
    };
    commandLine.setParameterExceptionHandler(paramHandler);
    commandLine.setExecutionExceptionHandler(execHandler);
  }
}
