package org.edumips64.utils.cli;

import org.edumips64.utils.ConfigKey;
import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.stream.Stream;

@Command(name="config")
public class ConfigCommand implements Runnable {

    @ParentCommand
    private Cli cli;

    private static ConfigStore configStore;

    @Override
    public void run() {
        configStore = cli.getConfigStore();
        System.out.println(CurrentLocale.getString("CLI.CONFIG.HEADER"));
        Stream.of(ConfigKey.values()).forEach(ConfigCommand::printValue);
    }

    private static void printValue(ConfigKey k) {
        try {
            System.out.println("\tKey: " + k.toString() + ", Value: " + configStore.getString(k));
        } catch (ClassCastException e) {
            try {
                System.out.println("\tKey: " + k.toString() + ", Value: " + configStore.getBoolean(k));
            } catch (ClassCastException ex) {
                System.out.println("\tKey: " + k.toString() + ", Value: " + configStore.getInt(k));
            }
        }catch (Exception e) {
            Cli.printErrorMessage(e);
        }
    }
}
