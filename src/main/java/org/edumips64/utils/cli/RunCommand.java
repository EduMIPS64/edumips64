package org.edumips64.utils.cli;

import org.edumips64.core.is.HaltException;
import org.edumips64.utils.CurrentLocale;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "run", resourceBundle = "CliMessages")
public class RunCommand implements Runnable {

    @ParentCommand
    private Cli cli;

    @Override
    public void run() {
        if (cli.isCpuRunnable()) {
            execute();
        } else {
            cli.printNotRunnable();
        }
    }

    private void execute() {
        int steps = 0;
        long startTimeMs = System.currentTimeMillis();
        printStartOfExecMsg();
        try {
            while(true) {
                cli.getCPU().step();
                if (cli.isVerbose() && steps+1 % 1000 == 0) {  // do not print the first dot, so short programs do not litter stdout.
                    System.out.print(".");
                }
                steps++;
            }
        } catch (HaltException e) {
            long endTimeMs = System.currentTimeMillis();
            long totalTimeMs = endTimeMs - startTimeMs;
            printEndOfExecMsg(steps, totalTimeMs);
        } catch (Exception e) {
            Cli.printErrorMessage(e);
        }
    }

    private void printStartOfExecMsg() {
        if (cli.isVerbose()) {
            System.out.println(CurrentLocale.getString("CLI.RUN.EXE_START"));
            System.out.println(CurrentLocale.getString("CLI.RUNNING"));
        }
    }

    private void printEndOfExecMsg(int steps, long timeMs) {
        if (cli.isVerbose()) {
            System.out.println();
            System.out.printf(CurrentLocale.getString("CLI.RUN.EXE_END"), steps, timeMs);
            System.out.println();
        }
    }
}
