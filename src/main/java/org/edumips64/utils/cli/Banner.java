/* Banner.java
 *
 * Modern startup banner for EduMIPS64. Renders the project name in a
 * heavy block font ("ANSI Shadow") with three colours that mirror the
 * project logo: gold "EDU" (the gold-on-black "Edu" chip), bright/white
 * "MIPS" (the black serif "MIPS" against a dark terminal background)
 * and red "64" (the iconic red italic "64"). The mixed-case spelling
 * "EduMIPS64" appears in the tagline directly below the art.
 *
 * (c) 2026 EduMIPS64 contributors
 *
 * This file is part of the EduMIPS64 project, and is released under the
 * GNU General Public License.
 */
package org.edumips64.utils.cli;

import org.edumips64.utils.MetaInfo;
import picocli.CommandLine.Help.Ansi;

import java.io.PrintStream;

/**
 * Renders the EduMIPS64 startup banner.
 *
 * <p>The art is rendered with Unicode box-drawing block characters
 * ({@code █╗╚═╝}) from the figlet "ANSI Shadow" font. They render
 * correctly on every modern terminal (Linux/macOS native, Windows
 * Terminal, IDE consoles, GitHub Codespaces, etc.) and on every Java
 * {@code System.out} stream we configure with UTF-8 in
 * {@link org.edumips64.Main#main(String[])}. On TTY-less streams,
 * narrow terminals, or non-UTF-8 encodings the banner falls back
 * automatically to a plain one-line tagline ({@link #printCompact}),
 * so scripts and pipelines stay clean.
 *
 * <p>Color is applied through picocli's {@link Ansi#AUTO} markup, which
 * detects ANSI support and {@code NO_COLOR}, and downgrades to plain
 * text when needed.
 */
public final class Banner {

    private Banner() {}

    // -----------------------------------------------------------------
    // ASCII art
    // -----------------------------------------------------------------
    //
    // The word "EduMIPS64" is rendered in three slices so we can color
    // each segment as in the project logo:
    //   "Edu"  → gold (the gold "Edu" chip in the logo),
    //   "MIPS" → bright/white (the black serif "MIPS" reads best as
    //            bright default on a dark terminal),
    //   "64"   → red (the iconic red italic "64").
    //
    // "MIPS" and "64" use the figlet "ANSI Shadow" font as-is. "Edu"
    // mixes the font's uppercase E with hand-crafted lowercase d and
    // u glyphs in the same heavy block style — the proper mixed-case
    // spelling matches the logo and the in-app branding.
    //
    // A dim grey row of ░ characters is appended below the art to give
    // the heavy blocks a subtle drop shadow.
    //
    // All three arrays MUST have the same length (BANNER_HEIGHT).

    private static final int BANNER_HEIGHT = 6;

    /** Minimum terminal width required to render the full banner. */
    static final int MIN_WIDTH = 90;

    /** "Edu" — gold, ANSI Shadow uppercase E + hand-crafted lowercase
     *  d (bowl whose right wall IS the ascender — a single vertical
     *  stroke, not two parallel ones) and u, 27 cols × 6 rows. */
    private static final String[] EDU = {
        "███████╗      ██╗          ",
        "██╔════╝      ██║          ",
        "█████╗   ███████║ ██╗   ██╗",
        "██╔══╝   ██   ██║ ██║   ██║",
        "███████╗ ███████║ ╚██████╔╝",
        "╚══════╝ ╚══════╝  ╚═════╝ ",
    };

    /** "MIPS" — bright/white, ANSI Shadow font (30 cols × 6 rows). */
    private static final String[] MIPS = {
        "███╗   ███╗██╗██████╗ ███████╗",
        "████╗ ████║██║██╔══██╗██╔════╝",
        "██╔████╔██║██║██████╔╝███████╗",
        "██║╚██╔╝██║██║██╔═══╝ ╚════██║",
        "██║ ╚═╝ ██║██║██║     ███████║",
        "╚═╝     ╚═╝╚═╝╚═╝     ╚══════╝",
    };

    /** "64" — red, ANSI Shadow font (17 cols × 6 rows). */
    private static final String[] SIXTYFOUR = {
        " ██████╗ ██╗  ██╗",
        "██╔════╝ ██║  ██║",
        "███████╗ ███████║",
        "██╔═══██╗╚════██║",
        "╚██████╔╝     ██║",
        " ╚═════╝      ╚═╝",
    };

    // Per-row colors give each segment a soft top-to-bottom gradient
    // (bright bold at the top, deeper/dimmer at the bottom), which
    // produces a drop-shadow / 3-D effect on the heavy block letters
    // without needing a separate shadow row underneath. The palette
    // for each segment is anchored on the matching logo colour.

    /** Gold gradient for "Edu". */
    private static final String[] EDU_COLORS = {
        "bold,fg(220)",  // bright gold
        "bold,fg(220)",
        "fg(214)",       // amber
        "fg(214)",
        "fg(130)",       // bronze
        "fg(130)",
    };

    /** White/grey gradient for "MIPS". */
    private static final String[] MIPS_COLORS = {
        "bold,fg(255)",  // bright white
        "bold,fg(255)",
        "fg(251)",       // light grey
        "fg(251)",
        "fg(244)",       // mid grey
        "fg(244)",
    };

    /** Red gradient for "64". */
    private static final String[] SIXTYFOUR_COLORS = {
        "bold,fg(196)",  // bright red
        "bold,fg(196)",
        "fg(160)",       // mid red
        "fg(160)",
        "fg(88)",        // deep red
        "fg(88)",
    };


    /** Rainbow palette for the "Ciao 'mbare!" tagline. */
    private static final String[] RAINBOW = {
        "fg(196)", "fg(208)", "fg(220)", "fg(46)",
        "fg(51)",  "fg(33)",  "fg(201)",
    };

    private static final String TAGLINE = "Ciao 'mbare!";

    /** Total banner width (incl. the 2-space left margin) used to
     *  right-align the tagline below the "64" segment. */
    private static final int BANNER_RIGHT_COL = 2 + 27 + 30 + 17;  // 76

    private static String rainbowMarkup(String text) {
        StringBuilder sb = new StringBuilder();
        int colorIdx = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                sb.append(' ');
            } else {
                sb.append("@|bold,").append(RAINBOW[colorIdx % RAINBOW.length])
                  .append(' ').append(c).append("|@");
                colorIdx++;
            }
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------

    /**
     * Returns the banner as a single multi-line string with picocli
     * ANSI markup. Visible for testing.
     */
    static String asMarkup() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        for (int i = 0; i < BANNER_HEIGHT; i++) {
            sb.append("  ");
            sb.append("@|").append(EDU_COLORS[i]).append(' ')
              .append(EDU[i]).append("|@");
            sb.append("@|").append(MIPS_COLORS[i]).append(' ')
              .append(MIPS[i]).append("|@");
            sb.append("@|").append(SIXTYFOUR_COLORS[i]).append(' ')
              .append(SIXTYFOUR[i]).append("|@");
            sb.append(System.lineSeparator());
        }
        // "Ciao 'mbare!" — right-aligned under the "64" segment, in
        // rainbow colours. A small Sicilian wink to whoever's reading.
        int padding = BANNER_RIGHT_COL - TAGLINE.length();
        for (int i = 0; i < padding; i++) {
            sb.append(' ');
        }
        sb.append(rainbowMarkup(TAGLINE));
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("  @|bold EduMIPS64|@ ");
        String version = (MetaInfo.VERSION == null || MetaInfo.VERSION.isEmpty())
            ? "dev" : MetaInfo.VERSION;
        sb.append("@|fg(220) v").append(version).append("|@");
        if (MetaInfo.CODENAME != null && !MetaInfo.CODENAME.isEmpty()) {
            sb.append(" @|faint (").append(MetaInfo.CODENAME).append(")|@");
        }
        if (MetaInfo.BUILD_DATE != null && !MetaInfo.BUILD_DATE.isEmpty()) {
            sb.append("  @|faint built ").append(MetaInfo.BUILD_DATE).append("|@");
        }
        sb.append(System.lineSeparator());
        sb.append("  @|faint A free, cross-platform visual MIPS64 CPU simulator|@");
        sb.append(System.lineSeparator());
        sb.append("  @|faint https://www.edumips.org  -  https://github.com/EduMIPS64/edumips64|@");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * Prints the full banner to {@code out}. Falls back to the compact
     * single-line form when the terminal cannot render the art (no TTY,
     * narrow terminal, no UTF-8 stdout).
     */
    public static void print(PrintStream out) {
        if (shouldRenderFull(out)) {
            out.print(Ansi.AUTO.string(asMarkup()));
        } else {
            printCompact(out);
        }
    }

    /**
     * Prints only the one-line tagline (no art, no ANSI). Used when
     * {@code --no-banner} is set, in scripts, or in narrow terminals.
     */
    public static void printCompact(PrintStream out) {
        out.println(Version.versionInfo);
    }

    // -----------------------------------------------------------------
    // Capability detection
    // -----------------------------------------------------------------

    /**
     * Returns true when the current terminal can comfortably display
     * the full banner. Returns false when stdout is not a TTY, when
     * the terminal width (from {@code COLUMNS}) is below
     * {@link #MIN_WIDTH}, or when stdout's charset is not Unicode-capable.
     */
    static boolean shouldRenderFull(PrintStream out) {
        if (System.console() == null) {
            return false;
        }
        // Don't render the block art if stdout can't encode the box-drawing
        // characters used in the art.
        if (!canEncodeBlockArt()) {
            return false;
        }
        String cols = System.getenv("COLUMNS");
        if (cols != null) {
            try {
                if (Integer.parseInt(cols.trim()) < MIN_WIDTH) {
                    return false;
                }
            } catch (NumberFormatException ignored) {
                // assume wide enough
            }
        }
        return true;
    }

    private static boolean canEncodeBlockArt() {
        // System.out's charset isn't directly exposed; we infer it from
        // file.encoding / stdout.encoding (set to UTF-8 by Main on entry).
        String enc = System.getProperty("stdout.encoding");
        if (enc == null) {
            enc = System.getProperty("file.encoding");
        }
        if (enc == null) {
            return false;
        }
        String e = enc.toLowerCase(java.util.Locale.ROOT);
        return e.contains("utf") || e.contains("unicode");
    }
}
