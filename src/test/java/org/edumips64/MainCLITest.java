package org.edumips64;

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.JavaPrefsConfigStore;
import org.edumips64.utils.cli.Cli;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.awt.desktop.OpenURIEvent;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import static org.junit.Assert.*;

public class MainCLITest {
    private Cli cli;
    private ConfigStore cfg;

    @Before
    public void setUp() {
        cfg = new JavaPrefsConfigStore(ConfigStore.defaults);
        CurrentLocale.setConfig(cfg);
        cli = new Cli(cfg);
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void can_run_a_file() {
        CommandLine commandLine = new CommandLine(cli);
        runTestProg(commandLine);
        assertEquals(5L, cli.getCPU().getRegister(1).getValue());
        assertEquals(2L, cli.getCPU().getRegister(2).getValue());
        assertEquals(7L, cli.getCPU().getRegister(3).getValue());
    }

    private void runTestProg(CommandLine commandLine) {
        commandLine.execute("file", "src/test/resources/add.s");
        commandLine.execute("run");
    }

    @Test
    public void cant_write_dinero_without_running_prog() {
        OutputStream os = getSystemOut();
        CommandLine commandLine = new CommandLine(cli);
        commandLine.execute("dinero", "temp_file");
        assertEquals("Cannot write this output until a program has been executed.", os.toString().trim());
    }

    @Test
    public void can_write_dinero_after_running_prog() {
        OutputStream os = getSystemOut();
        CommandLine commandLine = new CommandLine(cli);
        runTestProg(commandLine);
        commandLine.execute("dinero", "temp_file");
    }

    @Test
    public void can_load_file_using_file_command() {
        OutputStream os = getSystemOut();
        CommandLine cl = new CommandLine(cli);
        cl.execute("file", "src/test/resources/add.s");
        assertTrue(os.toString().contains("Loaded file "));
    }

    @Test
    public void can_handle_file_not_found() {
        OutputStream os = getSystemErr();
        CommandLine cl = new CommandLine(cli);
        cl.execute("file", "test.s");
        assertTrue(os.toString().contains("Unable to load file: "));
    }

    @Test
    public void can_show_registers() {
        OutputStream os = getSystemOut();
        CommandLine cl = new CommandLine(cli);
        cl.execute("show", "registers");
        for (int x = 0; x <= 31; x++) {
            assertTrue(os.toString().contains("Register "+x+":\t0000000000000000"));
        }
    }

    @Test
    public void can_show_a_register() {
        OutputStream os = getSystemOut();
        CommandLine cl = new CommandLine(cli);
        cl.execute("show", "register", "1");
        assertTrue(os.toString().contains("0000000000000000"));
    }

    @Test
    public void show_register_wont_work_with_alpha_chars() {
        OutputStream os = getSystemErr();
        CommandLine cl = new CommandLine(cli);
        cl.execute("show", "register", "a");
        assertTrue(os.toString().contains("Invalid value for positional parameter at index 0"));
    }

    @Test
    public void prints_usage_message_en() {
        OutputStream os = getSystemOut();
        new CommandLine(cli).execute("help");
        assertEquals(enMainUsageMsg(), os.toString().trim());
    }

    @Test
    public void prints_usage_message_it() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("help");
        assertEquals(itMainUsageMsg(), os.toString().trim());
    }

    @Test
    public void prints_usage_message_show_en() {
        OutputStream os = getSystemOut();
        new CommandLine(cli).execute("show", "--help");
        assertEquals(enShowUsageMsg(), os.toString().trim());
    }

    @Test
    public void prints_usage_message_show_it() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("show", "--help");
        assertEquals(itShowUsageMsg(), os.toString().trim());
    }

    @Test
    public void prints_usage_message_step_en() {
        OutputStream os = getSystemOut();
        new CommandLine(cli).execute("step", "--help");
        assertEquals(enStepUsageMsg(), os.toString().trim());
    }

    @Test
    public void prints_usage_message_step_it() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("step", "--help");
        assertEquals(itStepUsageMsg(), os.toString().trim());
    }

    @Test
    public void can_print_configuration() {
        new CommandLine(cli).execute("config");
    }

    private OutputStream getSystemOut() {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        System.setOut(new PrintStream(boas));
        return boas;
    }

    private OutputStream getSystemErr() {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        System.setErr(new PrintStream(boas));
        return boas;
    }

    private String enMainUsageMsg() {
        return "Usage:  [COMMAND]\n" +
                "Available commands:\n" +
                "  step    Make the CPU state machine advance 'N' number of steps\n" +
                "  run     Execute the program without intervention\n" +
                "  file    Provide a new file to execute\n" +
                "  show    Show various content of the CPU state machine\n" +
                "  config  Print the current configuration values to the screen\n" +
                "  exit    Exit EduMIPS64 CLI shell\n" +
                "  help    Show this help message";
    }

    private String itMainUsageMsg() {
        return "Usage:  [COMMAND]\n" +
                "Comandi disponibili:\n" +
                "  step    Fare in modo che la macchina a stati della CPU avanzi un numero di\n" +
                "            passaggi \"N\"\n" +
                "  run     Eseguire il programma senza intervento\n" +
                "  file    Fornisci un nuovo file da eseguire\n" +
                "  show    Mostra vari contenuti della macchina a stati della CPU\n" +
                "  config  Stampa i valori di configurazione correnti sullo schermo\n" +
                "  exit    Esci dalla shell della CLI di EduMIPS64\n" +
                "  help    Mostra questo messaggio di aiuto";
    }

    private String enShowUsageMsg() {
        return "Usage:  show [-h] [COMMAND]\n" +
                "Show various content of the CPU state machine\n" +
                "  -h, --help\n" +
                "Commands:\n" +
                "  memory     Show the content of memory\n" +
                "  pipeline   Show the content of the pipeline\n" +
                "  register   Show the content of a specific register\n" +
                "  registers  Show the content of registries\n" +
                "  symbols    Show the content of the symbol table";
    }

    private String itShowUsageMsg() {
        return "Usage:  show [-h] [COMMAND]\n" +
                "Mostra vari contenuti della macchina a stati della CPU\n" +
                "  -h, --help\n" +
                "Commands:\n" +
                "  memory     Mostra il contenuto della memoria\n" +
                "  pipeline   Mostra il contenuto della pipeline\n" +
                "  register   Mostra il contenuto di un registro specifico\n" +
                "  registers  Mostra il contenuto dei registri\n" +
                "  symbols    Mostra il contenuto della tabella dei simboli";
    }

    private String enStepUsageMsg() {
        return "Usage:  step [-h] <steps>\n" +
                "Make the CPU state machine advance 'N' number of steps\n" +
                "      <steps>   Number of steps to advance, default is 1\n" +
                "  -h, --help    Show this help message";
    }

    private String itStepUsageMsg() {
        return "Usage:  step [-h] <steps>\n" +
                "Fare in modo che la macchina a stati della CPU avanzi un numero di passaggi \"N\"\n" +
                "      <steps>   Numero di passaggi per avanzare, l'impostazione predefinita è 1\n" +
                "  -h, --help    Mostra questo messaggio di aiuto";
    }
}