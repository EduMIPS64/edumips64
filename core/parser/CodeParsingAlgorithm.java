/*
 * CodeParsingAlgorithm.java
 *
 * Parsing algorithm associated to the .code section.
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

import edumips64.core.is.*;
import edumips64.core.*;

class CodeParsingAlgorithm extends ParsingAlgorithm {

    public CodeParsingAlgorithm(Parser p) {
        super(p);
    }

    public void parse(Scanner s) {
        System.out.println("Starting CodeParsingAlgorithm");
        while(s.hasToken()) {
            Token token = s.nextToken();
            String data = token.getBuffer();

            // Error
            if(token.isErrorToken())
                System.out.println("Lexical error: " + token);


            // Directive
            else if(token.validate('D')) {
                if(parser.hasAlgorithm(data))
                    parser.switchParsingAlgorithm(data);
                else
                    System.out.println("Invalid directive in .data section");
            }

            // ID
            else if(token.validate('L')) {
                // Label?
                Instruction tmpInstr = Instruction.buildInstruction(data);
                if(tmpInstr == null) {
                    // Label! It would be useful if there was a "EndOfTokenStream" exception
                    Token colon = s.nextToken(); 
                    if(!colon.validate(':'))
                        System.out.println("Error: colon expected");
                    else {
                        // Good label
                        System.out.println("Label - " + data);
                        Token temp = s.nextToken();
                        tmpInstr = Instruction.buildInstruction(temp.getBuffer());
                        if(tmpInstr == null)
                            System.out.println("Error: missing instruction");
                    }
                }

                // Now we should have a valid instruction. Let's get its
                // syntax and validate the remaining tokens against it.
                String syntax = tmpInstr.getSyntax();

                for(int i = 0; i < syntax.length(); ++i) {
                    char c = syntax.charAt(i);
                    if(c == '%')
                        continue;
                    Token temp = s.nextToken();
                    if(temp.validate(c))
                        System.out.println("OK");
                    else
                        System.out.println("Unexpected token: " + temp);
                }
            }
        }
    }
}
