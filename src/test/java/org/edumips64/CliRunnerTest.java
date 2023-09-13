package org.edumips64;

import org.edumips64.utils.ConfigStore;
import org.edumips64.utils.CurrentLocale;
import org.edumips64.utils.InMemoryConfigStore;
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

    @Before
    public void setUp() {
        var cfg = new InMemoryConfigStore(ConfigStore.defaults);
        CurrentLocale.setConfig(cfg);
        cli = new Cli(cfg);
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
    public void noDineroWithoutRunningFirst() {
        OutputStream os = getSystemOut();
        CommandLine commandLine = new CommandLine(cli);
        File f = new File("src/test/resources/temp_file");
        commandLine.execute("dinero", f.getAbsolutePath());
        assertEquals("Cannot write this output until a program has been executed.", os.toString().trim());
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void dineroWorksAfterRunning() {
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
    public void canLoad() {
        OutputStream os = getSystemOut();
        CommandLine cl = new CommandLine(cli);
        cl.execute("load", "src/test/resources/add.s");
        assertTrue(os.toString().contains("Loaded file "));
    }

    @Test
    public void handlesNotFound() {
        OutputStream os = getSystemOut();
        CommandLine cl = new CommandLine(cli);
        File f = new File("src/test/resources/a_nonexistent_file");
        assertTrue(!f.exists());
        cl.execute("load", f.getAbsolutePath());
        assertTrue(os.toString().contains("Unable to load file: "));
    }

    @Test
    public void canShowRegisters() {
        OutputStream os = getSystemOut();
        CommandLine cl = new CommandLine(cli);
        cl.execute("show", "registers");
        for (int x = 0; x <= 31; x++) {
            assertTrue(os.toString().contains("Register "+x+":\t0000000000000000"));
        }
    }

    @Test
    public void canShowSingleRegister() {
        OutputStream os = getSystemOut();
        CommandLine cl = new CommandLine(cli);
        cl.execute("show", "register", "1");
        assertTrue(os.toString().contains("0000000000000000"));
    }

    @Test
    public void showRegisterBreaksWithAlphaCharacter() {
        OutputStream os = getSystemErr();
        CommandLine cl = new CommandLine(cli);
        cl.execute("show", "register", "a");
        assertTrue(os.toString().contains("Invalid value for positional parameter at index 0"));
    }

    @Test
    public void printUsageEn() {
        OutputStream os = getSystemOut();
        new CommandLine(cli).execute("help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void printUsageIt() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void printUsageZh() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
        new CommandLine(cli).execute("help");
        assertFalse(os.toString().trim().isEmpty());
    }
    
    @Test
    public void printUsageShowEn() {
        OutputStream os = getSystemOut();
        new CommandLine(cli).execute("show", "--help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void printUsageShowIt() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("show", "--help");
        assertFalse(os.toString().trim().isEmpty());
    }
    @Test
    public void printUsageMessageShowZh() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("show", "--help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void printUsageStepEn() {
        OutputStream os = getSystemOut();
        new CommandLine(cli).execute("step", "--help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test
    public void printUsageStepIt() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(cli).execute("step", "--help");
        assertFalse(os.toString().trim().isEmpty());
    }

    @Test

    public void printUsageStepZh() {
        OutputStream os = getSystemOut();
        Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
        new CommandLine(cli).execute("step", "--help");
        assertFalse(os.toString().trim().isEmpty());
    }

    public void printConfig() {
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