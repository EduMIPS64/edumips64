package org.edumips64.utils;

import org.edumips64.Main;
import org.edumips64.utils.cli.Args;
import org.edumips64.utils.cli.Version;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class ArgsTest {

    private Args args;
    
    @Before
    public void setup() {
        args = new Args();
    }

    @Test
    public void properly_displays_version() {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        System.setOut(new PrintStream(boas));
        CommandLine.ParseResult result = new CommandLine(args).parseArgs("-V");
        String expected = new Version().getVersion()[0];

        assertTrue(CommandLine.printHelpIfRequested(result));
        assertTrue(boas.toString().contains(expected));
    }

    @Test
    public void should_have_a_filename_debug_and_reset_default_to_false() {
        CommandLine.populateCommand(args,"-f", "test.s");
        assertEquals("test.s", args.getFileName());
        assertFalse(args.isDebug());
        assertFalse(args.isReset());
    }

    @Test
    public void should_have_a_filename_debug_and_reset_are_true() {
        CommandLine.populateCommand(args, "-dr","-f=test.s");
        assertEquals("test.s", args.getFileName());
        assertTrue(args.isDebug());
        assertTrue(args.isReset());
    }

    @Test
    public void should_display_warning_for_missing_file() {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        new CommandLine(args)
                .setErr(new PrintWriter(boas))
                .execute("-f");
        assertTrue(boas.toString().contains("Missing required parameter for option '--file' (<fileName>)"));
    }

    @Test
    public void should_have_a_filename_debug_false_and_reset_true() {
        CommandLine.populateCommand(args, "-r","-f=test.s");
        assertEquals("test.s",args.getFileName());
        assertFalse(args.isDebug());
        assertTrue(args.isReset());
    }

    @Test
    public void should_have_a_filename_debug_true_and_reset_false() {
        CommandLine.populateCommand(args, "-d","-f=test.s");
        assertEquals("test.s",args.getFileName());
        assertTrue(args.isDebug());
        assertFalse(args.isReset());
    }

    @Test
    public void handlers_should_be_warning_level_without_debug() {
        new CommandLine(new Args()).execute();
        Logger log = Logger.getLogger(Main.class.getName()).getParent();
        for (Handler h : log.getHandlers()) {
            assertEquals(Level.WARNING, h.getLevel());
        }
    }
}