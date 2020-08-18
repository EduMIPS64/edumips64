package org.edumips64.utils.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

@Command(name = "show", resourceBundle = "CliMessages")
public class ShowCommand implements Runnable{

    @ParentCommand
    private Cli cli;

    @Option(names={"-h","--help"}, usageHelp = true)
    private boolean help;

    @Command(name="registers")
    private void showRegisters() {
        System.out.println(cli.getCPU().gprString());
    }

    @Command(name="register")
    private void showRegister(@Parameters(arity="1", paramLabel = "register_number", descriptionKey = "show.reg.param") Integer registerNum) {
        System.out.println(cli.getCPU().getRegister(registerNum));
    }

    @Command(name="memory")
    private void showMemory() {
        System.out.println(cli.getMemory());
    }

    @Command(name = "symbols")
    private void showSymbols() {
        System.out.println(cli.getSymTab());
    }

    @Command(name = "pipeline")
    private void showPipeline() {
        System.out.println(cli.getCPU().pipeLineString());
    }

    @Override
    public void run() {
        //Intentionally blank, to easily print usage
    }
}
