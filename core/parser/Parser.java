/*
 * Parser.java
 *
 * Parses a MIPS64 source code and fills the symbol table and the memory.
 *
 * (c) 2008 Andrea Spadaccini
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edumips64.core.parser;
import java.util.HashMap;

public class Parser {
    // Association between directives and parsing algorithms
    protected HashMap<String, ParsingAlgorithm> algorithms;
    protected static Parser instance;
    protected Scanner scanner;
    protected ParsingAlgorithm default_alg;

    /* --------------------
     * Public methods
     * --------------------
     */
    public void parse(Scanner s) {
        System.out.println("Starting the parser subsystem");
        scanner = s;
        default_alg.parse(s);
    }

    // Singleton design pattern
    public static Parser getInstance() {
        if(instance == null)
            instance = new Parser();
        return instance;
    };
    /* ----------------------------
     * Package-wide visible methods
     * ----------------------------
     */
    boolean hasAlgorithm(String directive) {
        return algorithms.containsKey(directive);
    }

    void switchParsingAlgorithm(String directive) { 
        System.out.println("Switching parser due to directive " + directive);
        algorithms.get(directive).parse(scanner);
    }

    /* -----------------
     * Protected methods
     * -----------------
     */
    protected Parser() {
        algorithms = new HashMap<String, ParsingAlgorithm>();
        default_alg = new NullParsingAlgorithm(this);
        registerAlgorithm(".DATA", default_alg);
        registerAlgorithm(".CODE", default_alg);
    }

    protected void registerAlgorithm(String directive, ParsingAlgorithm p) {
        System.out.println("Registering a parser for directive " + directive + ", " + p.toString());
        algorithms.put(directive, p);
    }
}
