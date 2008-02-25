/*
 * DataParsingAlgorithm.java
 *
 * Parsing algorithm associated to the .data section.
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

class DataParsingAlgorithm extends ParsingAlgorithm {

    public DataParsingAlgorithm(Parser p) {
        super(p);
    }

    public void parse(Scanner s) {
        System.out.println("Starting DataParsingAlgorithm");
        int address = 0;
        while(s.hasToken()) {
            System.out.println("Starting Data parsing cycle");
            String label = null;
            Token token = s.next();
            String data = token.getBuffer();
            
            // Error
            if(token.isErrorToken()) {
                parser.addError(token, "Lexical error");
                continue;
            }

            // Parser change directive
            else if(token.validate('D') && parser.hasAlgorithm(data)) {
                if(parser.hasAlgorithm(data)) {
                    parser.switchParsingAlgorithm(data);
                    break;
                }
                else {
                    parser.addError(token, "Invalid directive");
                    continue;
                } 
            }
            
            // ID - label verification
            else if(token.validate('L')) {
                if(!parser.isInstruction(token)) {
                    label = token.getBuffer();
                    token = s.next(); 
                    if(!token.validate(':')) {
                        parser.addError(token, "Colon expected");
                        continue;
                    } 

                    // Fetch the actual data directive for the next
                    // control
                    token = s.next();
                }
            }

            // Directive
            if(token.validate('D')) {
                System.out.println("** Data type: " + token.getBuffer());
                // Real .data parsing. We have the label
                // TODO: right now we toss away tokens
                while(!s.next().validate('\n')) ;
            }
        }
    }
}
