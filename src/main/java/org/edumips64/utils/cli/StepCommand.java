package org.edumips64.utils.cli;

import org.edumips64.core.IrregularStringOfBitsException;
import org.edumips64.core.IrregularWriteOperationException;
import org.edumips64.core.MemoryElementNotFoundException;
import org.edumips64.core.NotAlignException;
import org.edumips64.core.StoppedCPUException;
import org.edumips64.core.SynchronousException;
import org.edumips64.core.is.AddressErrorException;
import org.edumips64.core.is.BreakException;
import org.edumips64.core.is.HaltException;
import org.edumips64.core.is.TwosComplementSumException;
import org.edumips64.utils.CurrentLocale;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

@Command(name = "step", resourceBundle = "CliMessages")
public class StepCommand implements Runnable {

    @ParentCommand
    private Cli cli;

    @Option(names ={"-h", "--help"}, usageHelp = true, descriptionKey = ".help.usage.description")
    private boolean help;

    @Parameters(defaultValue = "1", descriptionKey = "steps.parameter")
    private Integer steps;

    @Override
    public void run() {
        try {
            if (cli.isCpuRunnable()) {
                if (steps > 0) {
                    printStepMsg();
                    runSteps();
                } else {
                    printWarning();
                }
            } else {
                cli.printNotRunnable();
            }
        } catch (Exception e) {
            Cli.printErrorMessage(e);
        }
    }

    private void runSteps() throws AddressErrorException, HaltException, IrregularWriteOperationException, StoppedCPUException, MemoryElementNotFoundException, IrregularStringOfBitsException, TwosComplementSumException, SynchronousException, BreakException, NotAlignException {
        for (int i = 0; i < steps; ++i) {
            cli.getCPU().step();
            System.out.println(cli.getCPU().pipeLineString());
        }
    }

    private void printStepMsg() {
        if (cli.isVerbose()) {
            System.out.printf(CurrentLocale.getString("CLI.STEP.NUM"), steps);
            System.out.println();
        }
    }

    private void printWarning() {
        System.out.println(CurrentLocale.getString("CLI.STEP.POSITIVE"));
    }
}
