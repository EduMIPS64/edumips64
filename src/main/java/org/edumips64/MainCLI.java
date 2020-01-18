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

import org.edumips64.core.*;
import org.edumips64.core.is.*;
import org.edumips64.core.parser.Parser;
import org.edumips64.core.parser.ParserMultiException;
import org.edumips64.utils.*;
import org.edumips64.utils.io.LocalFileUtils;

import java.io.*;

/** Interactive shell for EduMIPS64
 * @author Andrea Spadaccini
 * */
public class MainCLI {
  public static void main(String args[]) {
    try {
      ConfigStore cfg = new JavaPrefsConfigStore(ConfigStore.defaults);
      CurrentLocale.setConfig(cfg);

      // Parse the args as early as possible, since it will influence logging level as well.
      // TODO: extract parseArgsOrExit out of the Main class.
      String toOpen = Main.parseArgsOrExit(args);

      // Initialize the CPU and all its dependencies.
      Memory memory = new Memory();
      CPU c = new CPU(memory, cfg, new BUBBLE());
      LocalFileUtils localFileUtils = new LocalFileUtils();
      SymbolTable symTab = new SymbolTable(memory);
      IOManager iom = new IOManager(localFileUtils, memory);
      Dinero dinero = new Dinero();
      InstructionBuilder instructionBuilder = new InstructionBuilder(memory, iom, c, dinero, cfg);
      Parser p = new Parser(localFileUtils, symTab, memory, instructionBuilder);
      c.setStatus(CPU.CPUStatus.READY);

      // Initialization done. Print a welcome message and open the file if needed.
      System.out.println("Welcome to EduMIPS64 CLI shell!");
      if (toOpen != null) {
        String absoluteFilename = new File(toOpen).getAbsolutePath();
        try {
          p.parse(absoluteFilename);
        } catch (ParserMultiException e) {
          if (e.hasErrors()) {
            throw e;
          }
        }
        dinero.setDataOffset(memory.getInstructionsNumber()*4);
        c.setStatus(CPU.CPUStatus.RUNNING);
        System.out.println("(Loaded file " + absoluteFilename + ")");
      }

      // Start the (very primitive) Read/Eval/Print Loop.
      BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("> ");
      while (true) {

        String read = keyboard.readLine();

        if (read == null || read.equals("exit")) {
          break;
        }

        String[] tokens = read.split(" ");

        if (tokens[0].compareToIgnoreCase("help") == 0) {
          String help = "EduMIPS64 CLI SHELL - Available commands:\n";
          help += "-----------------------------------------\n";
          help += "help\t\t\tshow this help message\n";
          help += "step\t\t\tmake the CPU state machine advance of one step\n";
          help += "step n\t\t\tmake the CPU state machine advance of n steps\n";
          help += "run\t\t\texecute the program till its end\n";
          help += "show registers\t\tshow the content of registries\n";
          help += "show memory\t\tshow the content of memory\n";
          help += "show symbols\t\tshow the content of the symbol table\n";
          help += "show pipeline\t\tshow the content of the pipeline\n";
          help += "exit\t\t\texit EduMIPS64 CLI shell\n";
          System.out.println(help);
        } else if (tokens[0].compareToIgnoreCase("show") == 0) {
          if (tokens.length == 1) {
            System.out.println("The show command requires at least one parameter.");
          } else {
            if (tokens[1].compareToIgnoreCase("registers") == 0) {
              System.out.println(c.gprString());
            } else if (tokens[1].compareToIgnoreCase("register") == 0) {
              if (tokens.length < 3) {
                System.out.println("The show register command requires at least one parameter.");
              } else {
                System.out.println(c.getRegister(Integer.parseInt(tokens[2])));
              }
            } else if (tokens[1].compareToIgnoreCase("memory") == 0) {
              System.out.println(memory);
            } else if (tokens[1].compareToIgnoreCase("symbols") == 0) {
              System.out.println(symTab);
            } else if (tokens[1].compareToIgnoreCase("pipeline") == 0) {
              System.out.println(c.pipeLineString());
            } else {
              System.out.println("The show command requires at least one parameter.");
            }
          }
        } else if (tokens[0].compareTo("run") == 0) {
            int steps = 0;
            long startTimeMs = System.currentTimeMillis();
            try {
              while(true) {
                steps++;
                c.step();

              }
            } catch (HaltException e) {
              long endTimeMs = System.currentTimeMillis();
              long totalTimeMs = endTimeMs - startTimeMs;
              System.out.println("Execution ended. " + steps + " steps were executed in " + totalTimeMs + "ms");
            }
        } else if (tokens[0].compareTo("step") == 0) {
          try {
            int num = 1;

            if (tokens.length > 1) {
              num = Integer.parseInt(tokens[1]);
            }

            try {
              if (num > 0) {
                System.out.println("I execute " + num + " steps of simulation.");
              }

              for (int i = 0; i < num; ++i) {
                c.step();
                System.out.println(c.pipeLineString());
              }
            } catch (Exception e) {
              System.err.println(getErrorReportingMessage());
              e.printStackTrace();
            }
          } catch (NumberFormatException e) {
            System.out.println("The second parameter of the step command must be an integer.");
          }
        } else {
          System.out.println("Unknown command.\nType 'help' to get the list of available commands.");
        }

        System.out.print("> ");
      }

      System.out.println("Bye!");
    } catch (Exception e) {
      System.err.println(getErrorReportingMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  /**
   * Compiles and returns a generic error message with version 
   * and system diagnostic information to be used to report the error.
   * @return A string containing the error message.
   */
  private static String getErrorReportingMessage() {
    String msg = "EduMIPS64 fatal error!\n" +
  		  "Please report the following stacktrace and system information,\n" +
		  "along with the content of the assembly file you were executing\n" +
		  "to the EduMIPS64 GitHub account: https://github.com/lupino3/edumips64/issues/new\n";
    msg += String.format("Version: %s, %s, %s\n", Main.VERSION, Main.BUILD_DATE, Main.GIT_REVISION);
    msg += String.format("JRE version: %s\nOS: %s\n\n", System.getProperty("java.version"), System.getProperty("os.name"));
    return msg;
  }  
}
