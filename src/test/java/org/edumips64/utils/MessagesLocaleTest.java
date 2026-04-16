package org.edumips64.utils;

import org.edumips64.utils.cli.Args;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Tests that the CLI help output correctly displays the localized verbose message
 * for all 3 supported locales (English, Italian, Chinese).
 * This ensures translations for CLI options like --verbose (added in PR #1483)
 * are properly displayed in the CLI help output.
 */
public class MessagesLocaleTest {

    private Locale originalLocale;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() {
        originalLocale = Locale.getDefault();
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void tearDown() {
        Locale.setDefault(originalLocale);
        System.setOut(originalOut);
    }

    @Test
    public void testEnglishLocaleShowsVerboseMessage() {
        Locale.setDefault(Locale.ENGLISH);
        new CommandLine(new Args()).execute("--help");
        String output = outputStream.toString();
        assertTrue("English CLI help should contain 'Verbose mode'",
                output.contains("Verbose mode"));
    }

    @Test
    public void testItalianLocaleShowsVerboseMessage() {
        Locale.setDefault(Locale.ITALIAN);
        new CommandLine(new Args()).execute("--help");
        String output = outputStream.toString();
        assertTrue("Italian CLI help should contain 'Modalità verbose'",
                output.contains("Modalità verbose"));
    }

    @Test
    public void testChineseLocaleShowsVerboseMessage() {
        Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
        new CommandLine(new Args()).execute("--help");
        String output = outputStream.toString();
        assertTrue("Chinese CLI help should contain '详细模式'",
                output.contains("详细模式"));
    }
}
