/* MainCLI.java
 *
 * Interactive shell for EduMIPS64
 *
 * (c) 2006 Andrea Spadaccini
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

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.cli.Args;
import org.edumips64.utils.cli.Cli;
import picocli.CommandLine;

import java.io.*;

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

      if (args.isVerbose()) {
        System.out.println(CurrentLocale.getString("CLI.WELCOME"));
      }
      if (args.getFileName() != null) {
          commandLine.execute("load", args.getFileName());
      }

      runReplLoop(commandLine);

    } catch (Exception e) {
      org.edumips64.utils.cli.Cli.printErrorMessage(e);
      System.exit(1);
    }
  }

  private void runReplLoop(CommandLine commandLine) throws IOException {
      BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
      printArrow();
      while (true) {
          String read = keyboard.readLine();
          handleTokens(commandLine, read.split(" "));
          printArrow();
        }
    }

  private void printArrow() {
    System.out.print("> ");
  }

  private void handleTokens(CommandLine commandLine, String[] tokens) {
    if (tokens.length > 0) {
      commandLine.execute(tokens);
    } else {
      printHelp(commandLine);
    }
  }

  private void printHelp(CommandLine c) {
    c.usage(System.out);
  }
}
