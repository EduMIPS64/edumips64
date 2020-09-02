package org.edumips64.utils.cli;

import org.edumips64.utils.CurrentLocale;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

@Command(name = "show", resourceBundle = "CliMessages")
public class ShowCommand implements Runnable{

    @ParentCommand
    private Cli cli;

    @Option(names={"-h","--help"}, usageHelp = true, descriptionKey= ".help.usage.description")
    private boolean help;

    @Command(name="registers")
    private void showRegisters() {
        System.out.println(cli.getCPU().gprString());
    }

    @Command(name="register")
    private void showRegister(@Parameters(arity="1", paramLabel = "register_number", descriptionKey = "show.reg.param") Integer registerNum) {
        if (validReg(registerNum)) {
            System.out.println(cli.getCPU().getRegister(registerNum).toString());
        } else {
            printRegMsg();
        }
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

    @Command(name = "fps")
    private void showFps() {
        System.out.println(cli.getCPU().fprString());
    }

    @Command(name="fp")
    private void showFp(@Parameters(arity="1", paramLabel = "fp_register_number", descriptionKey = "show.fp.reg.param") Integer registerNum) {
        if (validReg(registerNum)) {
            System.out.println(cli.getCPU().getRegisterFP(registerNum).toString());
        } else {
            printRegMsg();
        }
    }

    @Command(name = "fcsr")
    private void showFcsr() {
        System.out.println(cli.getCPU().getFCSR().toString());
    }

    @Command(name = "hi")
    private void showHi() {
        System.out.println(cli.getCPU().getHI().toString());
    }

    @Command(name = "lo")
    private void showLo() {
        System.out.println(cli.getCPU().getLO().toString());
    }

    @Override
    public void run() {
        //print help with no subcommand
        new CommandLine(cli).execute("show", "-h");
    }

    private void printRegMsg() {
        System.out.println(CurrentLocale.getString("CLI.REG.WARN"));
    }

    private boolean validReg(int i) {
        return i >= 0 && i <= 31;
    }
}