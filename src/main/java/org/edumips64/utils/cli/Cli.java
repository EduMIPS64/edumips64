package org.edumips64.utils.cli;

import org.edumips64.core.*;
import org.edumips64.core.is.BUBBLE;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.parser.Parser;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.MetaInfo;
import org.edumips64.utils.io.LocalFileUtils;
import org.edumips64.utils.io.StdOutWriter;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name="", resourceBundle = "CliMessages", subcommands = {
        StepCommand.class,
        RunCommand.class,
        LoadCommand.class,
        ShowCommand.class,
        DineroCommand.class,
        ConfigCommand.class
})
public class Cli implements Runnable {

    private Memory memory;
    private CPU c;
    private SymbolTable symTab;
    private IOManager iom;
    private CacheSimulator cachesim;
    private InstructionBuilder instructionBuilder;
    private Parser p;
    private final ConfigStore configStore;

    public Cli(ConfigStore cfg) {
        this.configStore = cfg;
        reset();
    }

    @Command(name = "help")
    void help() {
        CommandLine.usage(this, System.out);
    }

    @Command(name = "reset")
    void reset() {
        memory = new Memory();
        c = new CPU(memory, this.configStore, new BUBBLE());
        symTab = new SymbolTable(memory);
        iom = new IOManager(new LocalFileUtils(), memory);

        // Allow SYSCALL 5 to go to stdout.
        iom.setStdOutput(new StdOutWriter());

        cachesim = new CacheSimulator();
        instructionBuilder = new InstructionBuilder(memory, iom, c, cachesim, this.configStore);
        p = new Parser(new LocalFileUtils(), symTab, memory, instructionBuilder);
        c.setStatus(CPU.CPUStatus.READY);
    }

    @Command(name = "exit")
    void exit() {
        System.out.println(CurrentLocale.getString("CLI.EXIT"));
        System.exit(0);
    }

    @Override
    public void run() {
        //Empty on purpose, allows easier command execution
    }

    public Memory getMemory() {
        return memory;
    }

    public CPU getCPU() {
        return c;
    }

    public SymbolTable getSymTab() {
        return symTab;
    }

    public IOManager getIom() {
        return iom;
    }

    public CacheSimulator getCacheSimulator() {
        return cachesim;
    }

    public InstructionBuilder getInstructionBuilder() {
        return instructionBuilder;
    }

    public Parser getParser() {
        return p;
    }

    public ConfigStore getConfigStore() {
        return this.configStore;
    }

    public static void printErrorMessage(Throwable e) {
        System.err.println(getErrorReportingMessage());
        e.printStackTrace();
    }

    /**
     * Compiles and returns a generic error message with version
     * and system diagnostic information to be used to report the error.
     * @return A string containing the error message.
     */
    public static String getErrorReportingMessage() {
        String msg = "EduMIPS64 fatal error!\n" +
                "Please report the following stacktrace and system information,\n" +
                "along with the content of the assembly file you were executing\n" +
                "to the EduMIPS64 GitHub account: https://github.com/EduMIPS64/edumips64/issues/new\n";
        msg += String.format("Version: %s, %s, %s\n", MetaInfo.VERSION, MetaInfo.BUILD_DATE, MetaInfo.FULL_BUILDSTRING);
        msg += String.format("JRE version: %s\nOS: %s\n\n", System.getProperty("java.version"), System.getProperty("os.name"));
        return msg;
    }

    public boolean isCpuRunnable() {
        return getCPU().getStatus() == CPU.CPUStatus.RUNNING || getCPU().getStatus() == CPU.CPUStatus.STOPPING;
    }

    public String getCpuStatus() {
        return getCPU().getStatus().toString();
    }

    public void printNotRunnable() {
        System.out.println(CurrentLocale.getString("CLI.CPU.NOT.RUNNABLE") + getCpuStatus());
        System.out.println(CurrentLocale.getString("CLI.NOT.RUNNABLE.LOAD.FILE"));
    }

    public boolean canLoadFile() {
        return getCPU().getStatus() == CPU.CPUStatus.HALTED || getCPU().getStatus() == CPU.CPUStatus.READY;
    }
}
