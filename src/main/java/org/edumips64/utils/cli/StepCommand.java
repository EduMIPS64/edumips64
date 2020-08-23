package org.edumips64.utils.cli;

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
            if (steps > 0) {
                printStepMsg();
            }

            for (int i = 0; i < steps; ++i) {
                cli.getCPU().step();
                System.out.println(cli.getCPU().pipeLineString());
            }
        } catch (Exception e) {
            Cli.printErrorMessage(e);
        }
    }

    private void printStepMsg() {
        System.out.printf(CurrentLocale.getString("STEP.NUM"), steps);
        System.out.println();
    }
}
