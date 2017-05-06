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
import org.edumips64.ui.common.CycleBuilder;
import org.edumips64.utils.*;
import org.edumips64.utils.io.LocalFileUtils;

import java.io.*;

import static org.edumips64.Main.configStore;
import static org.edumips64.Main.showVersion;
import static org.edumips64.Main.usage;

/** Interactive shell for EduMIPS64
 * @author Andrea Spadaccini
 * */

public class MainCLI {
  public static void main(String args[]) {
    try {
      ConfigStore cfg = new JavaPrefsConfigStore(ConfigStore.defaults);
      CurrentLocale.setConfig(cfg);

      // Parse the args as early as possible, since it will influence logging level as well.
      String toOpen = Main.parseArgsOrExit(args);

      // Initialize the CPU and all its dependencies.
      Memory memory = new Memory();
      CPU c = new CPU(memory, cfg);
      LocalFileUtils localFileUtils = new LocalFileUtils();
      SymbolTable symTab = new SymbolTable(memory);
      IOManager iom = new IOManager(localFileUtils, memory);
      Dinero dinero = new Dinero(memory);
      InstructionBuilder instructionBuilder = new InstructionBuilder(memory, iom, c, dinero, cfg);
      Parser p = new Parser(localFileUtils, symTab, memory, instructionBuilder);
      CycleBuilder builder = new CycleBuilder(c);
      c.setStatus(CPU.CPUStatus.READY);

      // Initialization done. Print a welcome message and open the file if needed.
      System.out.println("Benvenuto nella shell di EduMIPS64!!");
      if (toOpen != null) {
        String absoluteFilename = new File(toOpen).getAbsolutePath();
        try {
          p.parse(absoluteFilename);
        } catch (ParserMultiException e) {
          if (e.hasErrors()) {
            throw e;
          }
        }
        c.setStatus(CPU.CPUStatus.RUNNING);
        System.out.println("(Caricato il file " + absoluteFilename + ")");
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
          String help = "EduMIPS64 CLI SHELL - Comandi disponibili:\n";
          help += "-----------------------------------------\n";
          help += "help\t\t\tmostra questo messaggio di aiuto\n";
          help += "step\t\t\tfa avanzare di uno step la macchina a stati della CPU:\n";
          help += "step n\t\t\tfa avanzare di n step la macchina a stati della CPU:\n";
          help += "run\t\t\tesegue il programma fino a terminazione\n";
          help += "show registers\t\tmostra il contenuto dei registri\n";
          help += "show memory\t\tmostra il contenuto della memoria\n";
          help += "show symbols\t\tmostra il contenuto della symbol table\n";
          help += "show pipeline\t\tmostra il contenuto della pipeline\n";
          System.out.println(help);
        } else if (tokens[0].compareToIgnoreCase("show") == 0) {
          if (tokens.length == 1) {
            System.out.println("Bisogna fornire almeno un parametro al comando show");
          } else {
            if (tokens[1].compareToIgnoreCase("registers") == 0) {
              System.out.println(c.gprString());
            } else if (tokens[1].compareToIgnoreCase("register") == 0) {
              if (tokens.length < 3) {
                System.out.println("Bisogna fornire almeno un parametro al comando show register");
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
              System.out.println("Bisogna fornire almeno un parametro al comando show");
            }
          }
        } else if (tokens[0].compareTo("run") == 0) {
            int steps = 0;
            long startTimeMs = System.currentTimeMillis();
            try {
              while(true) {
                steps++;
                c.step();
                builder.step();
              }
            } catch (HaltException e) {
              long endTimeMs = System.currentTimeMillis();
              long totalTimeMs = endTimeMs - startTimeMs;
              System.out.println("Esecuzione terminata. " + steps + " step eseguiti in " + totalTimeMs + "ms");
            }
        } else if (tokens[0].compareTo("step") == 0) {
          try {
            int num = 1;

            if (tokens.length > 1) {
              num = Integer.parseInt(tokens[1]);
            }

            try {
              if (num > 0) {
                System.out.println("Eseguo " + num + " step di simulazione");
              }

              for (int i = 0; i < num; ++i) {
                c.step();
                builder.step();
                System.out.println(c.pipeLineString());
              }
            } catch (Exception e) {
              System.out.println("Eccezione durante lo step!!");
              e.printStackTrace();
            }
          } catch (NumberFormatException e) {
            System.out.println("Il secondo parametro del comando step dev'essere un numero intero");
          }
        } else {
          System.out.println("Comando non riconosciuto.\nDigitare 'help' per avere un elenco di comandi");
        }

        System.out.print("> ");
      }

      System.out.println("Ciao ciao!");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
