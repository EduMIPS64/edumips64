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

import org.edumips64.core.CPU;
import org.edumips64.core.Dinero;
import org.edumips64.core.IOManager;
import org.edumips64.core.Memory;
import org.edumips64.core.SymbolTable;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.parser.Parser;
import org.edumips64.core.parser.ParserMultiException;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.JavaPrefsConfigStore;
import org.edumips64.utils.MetaInfo;
import org.edumips64.utils.cli.Args;
import org.edumips64.utils.cli.Cli;
import org.edumips64.utils.io.LocalFileUtils;
import picocli.CommandLine;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/** Interactive shell for EduMIPS64
 * @author Andrea Spadaccini
 * */
public class MainCLI {
  public static void main(String args[]) {
    try {
      ConfigStore cfg = new JavaPrefsConfigStore(ConfigStore.defaults);
      CurrentLocale.setConfig(cfg);

      // Parse the args as early as possible, since it will influence logging level as well.
      Args cliArgs = new Args();
      CommandLine commandLine = new CommandLine(cliArgs);
      if (commandLine.execute(args) != 0 || commandLine.isUsageHelpRequested() || commandLine.isVersionHelpRequested()) {
        System.exit(0);
      }
      if (cliArgs.isReset()) {
        cfg.resetConfiguration();
      }

      Cli cli = new Cli(cfg);
      commandLine = new CommandLine(cli);

      System.out.println(CurrentLocale.getString("CLI.WELCOME"));
      // Initialization done. Print a welcome message and open the file if needed.
      if (cliArgs.getFileName() != null) {
          commandLine.execute("file", cliArgs.getFileName());
      }

      runReplLoop(commandLine);

    } catch (Exception e) {
      Cli.printErrorMessage(e);
      System.exit(1);
    }
  }

    private static void runReplLoop(CommandLine commandLine) throws IOException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("> ");
        while (true) {
          String read = keyboard.readLine();
          String[] tokens = read.split(" ");
          commandLine.execute(tokens);
          System.out.print("> ");
        }
    }
}
