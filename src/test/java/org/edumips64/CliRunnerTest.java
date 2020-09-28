package org.edumips64;

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.JavaPrefsConfigStore;
import org.edumips64.utils.cli.Cli;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import static org.junit.Assert.*;

public class CliRunnerTest {
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
        commandLine.execute("load", "src/test/resources/add.s");
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
        CommandLine commandLine = new CommandLine(cli);
        runTestProg(commandLine);
        File f = new File("src/test/resources/temp_file");
        commandLine.execute("dinero", f.getAbsolutePath());
        assertTrue(f.exists());
        assertTrue(f.getTotalSpace() > 0);
        assertTrue(f.isFile());
        assertTrue(f.delete());
    }

    @Test
    public void can_load_file_using_file_command() {
        OutputStream os = getSystemOut();
        CommandLine cl = new CommandLine(cli);
        cl.execute("load", "src/test/resources/add.s");
        assertTrue(os.toString().contains("Loaded file "));
    }

    @Test
    public void can_handle_file_not_found() {
        OutputStream os = getSystemOut();
        CommandLine cl = new CommandLine(cli);
        cl.execute("load", "test.s");
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
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void prints_usage_message_it() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void prints_usage_message_show_en() {
        OutputStream os = getSystemOut();
        new CommandLine(cli).execute("show", "--help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void prints_usage_message_show_it() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("show", "--help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void prints_usage_message_step_en() {
        OutputStream os = getSystemOut();
        new CommandLine(cli).execute("step", "--help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void prints_usage_message_step_it() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("step", "--help");
        assertFalse(os.toString().trim().isEmpty());
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
}