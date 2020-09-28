package org.edumips64.utils.cli;

import org.edumips64.Main;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


@Command(mixinStandardHelpOptions = true, resourceBundle = "Messages", versionProvider = Version.class)
public class Args implements Runnable {

    @Option(names = {"-f", "--file"}, descriptionKey = "file")
    private String fileName;

    @Option(names = {"-d", "--debug"}, descriptionKey = "debug")
    private boolean debug;

    @Option(names = {"-r", "--reset"}, descriptionKey = "reset")
    private boolean reset;

    @Option(names = {"-hl", "--headless"}, descriptionKey = "headless", defaultValue = "false")
    private boolean headless;

    public String getFileName() {
        return this.fileName;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isReset() {
        return reset;
    }

    public boolean isHeadless() { return headless; }

    @Override
    public void run() {

        //keep logging level change here to occur when using execute
        if (!isDebug()) {
            Logger rootLogger = Logger.getLogger(Main.class.getName()).getParent();

            for (Handler h : rootLogger.getHandlers()) {
                h.setLevel(Level.WARNING);
            }
        }
    }
}
