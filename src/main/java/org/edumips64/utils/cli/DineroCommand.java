package org.edumips64.utils.cli;

import org.edumips64.core.CPU;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.io.LocalWriterAdapter;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Writer;

@Command(name = "dinero", resourceBundle = "CliMessages")
public class DineroCommand implements Runnable {

    @ParentCommand
    private Cli cli;

    @Option(names = {"-h", "--help"}, usageHelp = true, descriptionKey = ".help.usage.description")
    private boolean help;

    @Parameters(arity = "1", descriptionKey = "dinero.param", paramLabel = "target_file")
    private File file;

    @Override
    public void run() {
        try (PrintWriter pw = new PrintWriter(file)){
            if (cli.getCPU().getStatus() == CPU.CPUStatus.HALTED) {
                cli.getDinero().writeTraceData(new LocalWriterAdapter(pw));
                pw.flush();
            } else {
                System.out.println(CurrentLocale.getString("DINERO.CANT.WRITE"));
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println(CurrentLocale.getString("FILE.NOT.FOUND"));
        } catch (Exception e) {
            Cli.printErrorMessage(e);
        }
    }
}
