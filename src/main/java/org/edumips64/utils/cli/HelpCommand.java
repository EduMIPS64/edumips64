/* HelpCommand.java
 *
 * Help system for the EduMIPS64 CLI. Replaces the inline `help` subcommand
 * defined in Cli.java with a richer one:
 *
 *   help              print the picocli usage with a footer pointing at
 *                     `help topics` for documentation chapters.
 *   help topics       list curated documentation chapters (loaded from
 *                     org/edumips64/help/topics/topics.index).
 *   help <topic>      print a chapter, paged through Pager.
 *
 * Topic content is bundled as plain-text resources in the JAR, so the
 * feature works from a stock `edumips64.jar` with no extra files.
 */
package org.edumips64.utils.cli;

import org.edumips64.utils.CurrentLocale;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Command(name = "help", resourceBundle = "CliMessages")
public class HelpCommand implements Runnable {

    static final String TOPIC_RESOURCE_PREFIX = "/org/edumips64/help/topics/";
    static final String INDEX_RESOURCE = TOPIC_RESOURCE_PREFIX + "topics.index";

    /** A row of the topics index. */
    static final class Topic {
        final String id;
        final String resource;
        final String title;
        Topic(String id, String resource, String title) {
            this.id = id; this.resource = resource; this.title = title;
        }
    }

    @ParentCommand
    private Cli cli;

    @Parameters(arity = "0..1", paramLabel = "topic",
            descriptionKey = "help.param")
    private String topic;

    @Override
    public void run() {
        if (topic == null || topic.isEmpty()) {
            printGeneralUsage();
        } else if ("topics".equalsIgnoreCase(topic)) {
            printTopicsList();
        } else {
            printTopic(topic);
        }
    }

    private void printGeneralUsage() {
        PrintWriter pw = utf8Stdout();
        new CommandLine(cli).usage(pw);
        pw.println();
        pw.println(Ansi.AUTO.string(
            "@|bold Documentation topics:|@ type @|fg(cyan) help topics|@ "
            + "to list bundled chapters."));
        pw.flush();
    }

    private void printTopicsList() {
        Map<String, Topic> topics = loadIndex();
        if (topics.isEmpty()) {
            System.out.println(CurrentLocale.getString("CLI.HELP.NO.TOPICS"));
            return;
        }
        PrintWriter pw = utf8Stdout();
        pw.println();
        pw.println(Ansi.AUTO.string(
            "@|bold Available documentation topics|@ "
            + "(use @|fg(cyan) help <topic>|@ to read one):"));
        pw.println();
        int width = 0;
        for (String id : topics.keySet()) {
            width = Math.max(width, id.length());
        }
        for (Topic t : topics.values()) {
            pw.println(Ansi.AUTO.string(String.format(
                "  @|bold,fg(cyan) %-" + width + "s|@   %s",
                t.id, t.title)));
        }
        pw.println();
        pw.flush();
    }

    private void printTopic(String id) {
        Topic t = loadIndex().get(id);
        if (t == null) {
            System.out.println(Ansi.AUTO.string(String.format(
                "@|fg(red) %s|@ '%s'.",
                CurrentLocale.getString("CLI.HELP.UNKNOWN.TOPIC"),
                id)));
            System.out.println(Ansi.AUTO.string(
                "Type @|fg(cyan) help topics|@ to see the list."));
            return;
        }
        InputStream in = HelpCommand.class.getResourceAsStream(
            TOPIC_RESOURCE_PREFIX + t.resource);
        if (in == null) {
            System.out.println(Ansi.AUTO.string(String.format(
                "@|fg(red) %s|@ '%s' (resource '%s' missing from JAR).",
                CurrentLocale.getString("CLI.HELP.UNKNOWN.TOPIC"),
                id, t.resource)));
            return;
        }
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(in, StandardCharsets.UTF_8));
        BufferedReader stdin = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Pager pager = new Pager(System.out, stdin);
        try {
            pager.page(reader);
        } catch (IOException e) {
            Cli.printErrorMessage(e);
        }
    }

    /**
     * Loads the topic index file. Returns an ordered map of
     * {@code id -> Topic}. Visible for testing.
     */
    static Map<String, Topic> loadIndex() {
        Map<String, Topic> result = new LinkedHashMap<>();
        InputStream in = HelpCommand.class.getResourceAsStream(INDEX_RESOURCE);
        if (in == null) {
            return result;
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split("\\t+");
                if (parts.length != 3) {
                    continue;
                }
                Topic t = new Topic(parts[0].trim(), parts[1].trim(), parts[2].trim());
                result.put(t.id, t);
            }
        } catch (IOException ignored) {
            // ignore - return whatever we managed to read
        }
        return result;
    }

    /**
     * Returns the list of topic IDs, in declaration order. Visible for
     * tests so they can verify every indexed topic actually loads.
     */
    static List<String> listTopicIds() {
        return new ArrayList<>(loadIndex().keySet());
    }

    private static PrintWriter utf8Stdout() {
        return new PrintWriter(
            new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
    }
}
