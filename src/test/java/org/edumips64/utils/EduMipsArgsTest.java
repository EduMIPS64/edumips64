package org.edumips64.utils;

import org.edumips64.Main;
import org.edumips64.utils.args.EduMipsArgs;
import org.edumips64.utils.args.EduMipsVersion;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class EduMipsArgsTest {

    private final String expectedItalianUsage = "Usage: <main class> [-dhrV] [-f=<fileName>]\n" +
                                           "  -d, --debug             attiva la modalità di debug\n" +
                                           "  -f, --file=<fileName>   apre il file specificato\n" +
                                           "  -h, --help              stampa questo messaggio\n" +
                                           "  -r, --reset             elimina le preferenze memorizzate\n" +
                                           "  -V, --version           stampa la versione\n";

    private final String expectedEnglishUsage = "Usage: <main class> [-dhrV] [-f=<fileName>]\n" +
                                           "  -d, --debug             activates debug mode\n" +
                                           "  -f, --file=<fileName>   filename - opens the specified file\n" +
                                           "  -h, --help              prints this help message\n" +
                                           "  -r, --reset             resets the stored preferences\n" +
                                           "  -V, --version           prints the version\n";

    private EduMipsArgs eduMipsArgs;
    
    @Before
    public void setup() {
        eduMipsArgs = new EduMipsArgs();
    }
    
    @Test
    public void edu_args_should_display_properly_for_italian() {
        Locale.setDefault(Locale.ITALIAN);
        String message = new CommandLine(eduMipsArgs).getUsageMessage();
        assertEquals(expectedItalianUsage, message);
    }

    @Test
    public void edu_args_should_display_properly_for_english() {
        Locale.setDefault(Locale.ENGLISH);
        String message = new CommandLine(eduMipsArgs).getUsageMessage();
        assertEquals(expectedEnglishUsage, message);
    }

    @Test
    public void properly_displays_version() {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        System.setOut(new PrintStream(boas));
        CommandLine.ParseResult result = new CommandLine(eduMipsArgs).parseArgs("-V");
        String expected = new EduMipsVersion().getVersion()[0];

        assertTrue(CommandLine.printHelpIfRequested(result));
        assertTrue(boas.toString().contains(expected));
    }

    @Test
    public void should_have_a_filename_debug_and_reset_default_to_false() {
        CommandLine.populateCommand(eduMipsArgs,"-f", "test.s");
        assertEquals("test.s", eduMipsArgs.getFileName());
        assertFalse(eduMipsArgs.isDebug());
        assertFalse(eduMipsArgs.isReset());
    }

    @Test
    public void should_have_a_filename_debug_and_reset_are_true() {
        CommandLine.populateCommand(eduMipsArgs, "-dr","-f=test.s");
        assertEquals("test.s",eduMipsArgs.getFileName());
        assertTrue(eduMipsArgs.isDebug());
        assertTrue(eduMipsArgs.isReset());
    }

    @Test
    public void should_display_warning_for_missing_file() {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        new CommandLine(eduMipsArgs)
                .setErr(new PrintWriter(boas))
                .execute("-f");
        assertTrue(boas.toString().contains("Missing required parameter for option '--file' (<fileName>)"));
    }

    @Test
    public void handlers_should_be_warning_level_without_debug() {
        new CommandLine(new EduMipsArgs()).execute();
        Logger log = Logger.getLogger(Main.class.getName()).getParent();
        for (Handler h : log.getHandlers()) {
            assertEquals(Level.WARNING, h.getLevel());
        }
    }
}