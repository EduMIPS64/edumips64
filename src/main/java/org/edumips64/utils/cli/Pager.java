/* Pager.java
 *
 * Tiny, dependency-free pager for the EduMIPS64 CLI help browser.
 * Pages plain text by terminal height when stdin/stdout are a TTY, and
 * dumps the entire text in one go otherwise (so piping/scripting is
 * unaffected).
 */
package org.edumips64.utils.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

/**
 * Minimal "more"-style pager. Reads up to {@code pageSize} lines from
 * {@code source}, prints them to {@code out}, then prompts the user
 * for a key (Enter / space → next page; q → quit). When stdin is not
 * interactive (e.g. fed from a script), the whole text is dumped at once.
 */
public final class Pager {

    private final PrintStream out;
    private final BufferedReader stdin;
    private final boolean interactive;
    private final int pageSize;

    public Pager(PrintStream out, BufferedReader stdin) {
        this.out = out;
        this.stdin = stdin;
        this.interactive = (System.console() != null) && stdin != null;
        this.pageSize = detectPageSize();
    }

    private static int detectPageSize() {
        // The JVM exposes stty rows via the LINES environment variable on
        // most shells; default to 24 (classic terminal height) - 4 for the
        // status / prompt line.
        String env = System.getenv("LINES");
        if (env != null) {
            try {
                int n = Integer.parseInt(env.trim());
                if (n > 6) return n - 3;
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return 22;
    }

    /** Pages the given text. Closes neither {@code source} nor {@code stdin}. */
    public void page(Reader source) throws IOException {
        BufferedReader br = (source instanceof BufferedReader)
                ? (BufferedReader) source
                : new BufferedReader(source);
        if (!interactive) {
            // Non-interactive: just dump.
            String line;
            while ((line = br.readLine()) != null) {
                out.println(line);
            }
            return;
        }
        int linesPrinted = 0;
        String line;
        while ((line = br.readLine()) != null) {
            out.println(line);
            linesPrinted++;
            if (linesPrinted >= pageSize) {
                out.print("-- More -- (Enter/space: next page, q: quit) ");
                out.flush();
                String reply = stdin.readLine();
                // Erase the prompt line so the next page starts cleanly.
                out.print("\r" + " ".repeat(50) + "\r");
                if (reply == null || reply.equalsIgnoreCase("q") || reply.equalsIgnoreCase("quit")) {
                    return;
                }
                linesPrinted = 0;
            }
        }
    }
}
