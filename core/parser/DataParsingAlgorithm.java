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

import edumips64.core.parser.tokens.*;
import edumips64.core.is.*;
import edumips64.core.*;
import edumips64.utils.Converter;

class DataParsingAlgorithm extends ParsingAlgorithm {
    protected Memory memory;
    public DataParsingAlgorithm(Parser p) {
        super(p);
        memory = Memory.getInstance();
    }

    public void parse(Scanner s) {
        System.out.println("Starting DataParsingAlgorithm");
        int address = 0;
        boolean memoryDirty = false;

        while(s.hasToken()) {
            System.out.println("Starting Data parsing cycle");

            // Alignment of memory address to 8 bytes
            if(address > 0 && (address % 8 != 0)) {
                System.out.println("Address was " + address + ", aligned to " + (address + 8 - (address % 8)));
                address += 8 - address % 8;
            }
            String label = null;
            Token token = s.next();
            String data = token.getBuffer();
            
            // Error
            if(token.isErrorToken()) {
                parser.addError(token, "Lexical error");
                continue;
            }
            
            // Parser change directive
            if(token.validate('D') && parser.hasAlgorithm(data)) {
                if(parser.hasAlgorithm(data)) {
                    parser.switchParsingAlgorithm(data);
                    break;
                }
                else {
                    parser.addError(token, "Invalid directive");
                    continue;
                } 
            }
            
            // ID - optional label verification
            if(token.validate('L')) {
                if(!parser.isInstruction(token)) {
                    label = token.getBuffer();
                    token = s.next(); 
                    if(!token.validate(':')) {
                        parser.addError(token, "Colon expected");
                        continue;
                    } 

                    //skip End-Of-Lines until fetching next data directive
                    do{
                        token = s.next();
                    }while(token.validate('\n'));
                }
            }



            // Directive, all the operations that throw exceptions start here.
            try {
                if(token.validate('D')) {
                    System.out.println("** Data type: " + token.getBuffer());
                    String directiveName = token.getBuffer();
                    //we have only 4 data types directive
                    // 1 - .space
                    if( directiveName.equalsIgnoreCase(".SPACE")){
                        System.out.println("Ho validato .SPACE");
                        token = s.next();
                        if( token.validate('I')){
                            long spaces = Converter.parseInteger(token.getBuffer());
                            System.out.println("Riservo "+spaces+" spazi in memoria");

                            for(int i = 0; i < spaces; ++i)
                                memory.writeInteger(address++, 0, "BYTE");

                            token = s.next(); //EOL for final control
                        }

                        else{
                            parser.addError(token, "Expected integer");
                            continue;
                        }
                    }
                    // 2- .double
                    else if(directiveName.equalsIgnoreCase(".DOUBLE")){
                        System.out.println("Ho validato .DOUBLE");
                        boolean error = false;
                        do{
                            token = s.next();
                            if(token.validate('F')){
                                //converti il numero e salvalo
                                System.out.println("Float value: " + token.getBuffer());
                            }
                            else{
                                parser.addError(token, "Expected float");
                                error = true;
                                break;
                            }
                            token = s.next();
                        }while(token.validate(','));
                        if(error)
                            continue;
                    }
                    // 3- integer types directive
                    else if(directiveName.equalsIgnoreCase(".BYTE") ||
                        (directiveName.equalsIgnoreCase(".WORD16")) ||
                        (directiveName.equalsIgnoreCase(".WORD32")) ||
                        (directiveName.equalsIgnoreCase(".WORD64")) ||
                        (directiveName.equalsIgnoreCase(".WORD"))) {

                        System.out.println("Ho validato " + directiveName);
                        boolean error = false;
                        do{
                            token = s.next();
                            if(token.validate('I')){
                                //converti il numero e salvalo
                                long value = Converter.parseInteger(token.getBuffer());
                                System.out.println("Integer value: " + value);
                                System.out.println("Writing to memory " + value);

                                // Rememmber that Memory.writeInteger returns
                                // the number of bytes written to memory, and
                                // that wants, as its fourth parameter, the data
                                // type name, so we have to strip the initial
                                // dot.
                                address += memory.writeInteger(address, value, directiveName.toUpperCase().substring(1));
                            }
                            else{
                                parser.addError(token, "Expected float");
                                error = true;
                                break;
                            }
                            token = s.next();
                        }while(token.validate(','));

                        if(error)
                            continue;
                    }
                    // 4 - string directive
                    else if(directiveName.equalsIgnoreCase(".ASCII") || 
                            directiveName.equalsIgnoreCase(".ASCIIZ")) {
                        System.out.println("Ho validato " + directiveName);
                        boolean error = false;
                        boolean auto_terminate = directiveName.equalsIgnoreCase(".ASCIIZ"); // inserimento automatico '\0'
                        do{
                            token = s.next();
                            if(token.validate('S')){
                                try {
                                    address += memory.writeString(address, token.getBuffer(), auto_terminate);
                                }
                                catch (StringFormatException e) {
                                    parser.addError(token, "Invalid string format");
                                }
                            }
                            else{
                                parser.addError(token, "String expected");
                                error = true;
                                break;
                            }
                            token = s.next();
                        } while(token.validate(','));

                        if(error)
                            continue;
                        
                    }
                    else {
                        parser.addError(token, "Invalid directive");
                        token = s.next();
                    }
                }
                //every chunk of code exits with a token, that MUST BE EOL
                if(!token.validate('\n'))
                    parser.addError(token, "Missing EOL");
                
            }  catch (IrregularWriteOperationException e) {
                // TODO: must be a warning
                parser.addError(token, "Value out of bounds");
            }  catch (NotAlingException e) {
                // TODO: must be a warning
                parser.addError(token, "Attempt to do a not-aligned write operation");
            }  catch (MemoryElementNotFoundException e) {
                // TODO: must be a warning
                parser.addError(token, "Attempt to write beyond the limits of EduMIPS64's memory");
            }
        }
    }
}
