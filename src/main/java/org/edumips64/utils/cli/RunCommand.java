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
        try {
            while(true) {
                steps++;
                cli.getCPU().step();
            }
        } catch (HaltException e) {
            long endTimeMs = System.currentTimeMillis();
            long totalTimeMs = endTimeMs - startTimeMs;
            printEndOfExecMsg(steps, totalTimeMs);
        } catch (Exception e) {
            Cli.printErrorMessage(e);
        }
    }

    private void printEndOfExecMsg(int steps, long timeMs) {
        System.out.printf(CurrentLocale.getString("CLI.RUN.EXE_END"), steps, timeMs);
        System.out.println();
    }
}
