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
import edumips64.utils.*;
import edumips64.core.is.*;
import edumips64.core.*;
import edumips64.utils.Converter;

class DataParsingAlgorithm extends ParsingAlgorithm {
    protected Memory memory;
    protected SymbolTable symTab;
    public DataParsingAlgorithm(Parser p) {
        super(p);
        memory = Memory.getInstance();
        symTab = SymbolTable.getInstance();
    }

    public void parse(Scanner s) {
        edumips64.Main.logger.debug("Starting DataParsingAlgorithm");
        int address = 0;
        boolean memoryDirty = false;

        while(s.hasToken()) {
            edumips64.Main.logger.debug("Starting Data parsing cycle");

            // Alignment of memory address to 8 bytes
            if(address > 0 && (address % 8 != 0)) {
                edumips64.Main.logger.debug("Address was " + address + ", aligned to " + (address + 8 - (address % 8)));
                address += 8 - address % 8;
            }
            String label = null;
            Token token = s.next();
            String data = token.getBuffer();
            
            // Error
            if(token.isErrorToken()) {
                parser.addError(token, "PARSER_LEXICAL_ERROR");
                continue;
            }
            
            // Parser change directive
            if(token.validate('D') && parser.hasAlgorithm(data)) {
                if(parser.hasAlgorithm(data)) {
                    parser.switchParsingAlgorithm(data);
                    break;
                }
                else {
                    parser.addError(token, "PARSER_INVALID_DIRECTIVE");
                    continue;
                } 
            }
            
            // ID - optional label verification
            if(token.validate('L')) {
                if(!parser.isInstruction(token)) {
                    label = token.getBuffer();
                    try {
                        symTab.setCellLabel(address, label);
                    }
                    catch (SameLabelsException e) {
                        parser.addError(token, "PARSER_DUPLICATE_LABEL");
                    }
                    catch (MemoryElementNotFoundException e) {
                        parser.addError(token, "PARSER_OUT_OF_BOUNDS");
                    }
                    token = s.next(); 
                    if(!token.validate(':')) {
                        parser.addError(token, "PARSER_COLON_EXPECTED");
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
                    edumips64.Main.logger.debug("** Data type: " + token.getBuffer());
                    String directiveName = token.getBuffer();
                    //we have only 4 data types directive
                    // 1 - .space
                    if( directiveName.equalsIgnoreCase(".SPACE")){
                        edumips64.Main.logger.debug("Ho validato .SPACE");
                        token = s.next();
                        if( token.validate('I')){
                            long spaces = Converter.parseInteger(token.getBuffer());
                            edumips64.Main.logger.debug("Riservo "+spaces+" spazi in memoria");

                            for(int i = 0; i < spaces; ++i)
                                memory.writeInteger(address++, 0, "BYTE");

                            token = s.next(); //EOL for final control
                        }

                        else{
                            parser.addError(token, "PARSER_INTEGER_EXPECTED");
                            continue;
                        }
                    }
                    // 2- .double
                    else if(directiveName.equalsIgnoreCase(".DOUBLE")){
                        edumips64.Main.logger.debug("Ho validato .DOUBLE");
                        boolean error = false;
                        do{
                            token = s.next();
                            if(token.validate('G')){
                                //converti il numero e salvalo
                                edumips64.Main.logger.debug("Float value: " + token.getBuffer());
                                address += memory.writeDouble(address, token.getBuffer());
                            }
                            else{
                                parser.addError(token, "PARSER_FLOAT_EXPECTED");
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

                        edumips64.Main.logger.debug("Ho validato " + directiveName);
                        boolean error = false;
                        do{
                            token = s.next();
                            if(token.validate('I')){
                                //converti il numero e salvalo
                                edumips64.Main.logger.debug("About to convert " + token.getBuffer());
                                long value = Converter.parseInteger(token.getBuffer());
                                edumips64.Main.logger.debug("Writing to memory " + value);

                                // Rememmber that Memory.writeInteger returns
                                // the number of bytes written to memory, and
                                // that wants, as its fourth parameter, the data
                                // type name, so we have to strip the initial
                                // dot.
                                address += memory.writeInteger(address, value, directiveName.toUpperCase().substring(1));
                            }
                            else{
                                parser.addError(token, "PARSER_INTEGER_EXPECTED");
                                error = true;
                                break;
                            }
                            token = s.next();
                        } while(token.validate(','));

                        if(error)
                            continue;
                    }
                    // 4 - string directive
                    else if(directiveName.equalsIgnoreCase(".ASCII") || 
                            directiveName.equalsIgnoreCase(".ASCIIZ")) {
                        edumips64.Main.logger.debug("Ho validato " + directiveName);
                        boolean error = false;
                        boolean auto_terminate = directiveName.equalsIgnoreCase(".ASCIIZ"); // auto-append of '\0'
                        do{
                            token = s.next();
                            if(token.validate('S')){
                                try {
                                    address += memory.writeString(address, token.getBuffer(), auto_terminate);
                                }
                                catch (StringFormatException e) {
                                    parser.addError(token, "PARSER_INVALID_STRING");
                                }
                            }
                            else{
                                parser.addError(token, "PARSER_STRING_EXPECTED");
                                error = true;
                                break;
                            }
                            token = s.next();
                        } while(token.validate(','));

                        if(error)
                            continue;
                        
                    }
                    else {
                        parser.addError(token, "PARSER_INVALID_DIRECTIVE");
                        token = s.next();
                    }
                }
                //every chunk of code exits with a token, that MUST BE EOL
                if(!token.validate('\n'))
                    parser.addError(token, "PARSER_EOL_EXPECTED");
                
            }
            catch (IrregularWriteOperationException e) {
                parser.addWarning(token, "PARSER_OUT_OF_BOUNDS");
            }
            catch (NotAlingException e) {
                parser.addWarning(token, "PARSER_NOT_ALIGN");
            }
            catch (MemoryElementNotFoundException e) {
                parser.addWarning(token, "PARSER_EXCEED_MEMORY");
            }
		    catch(edumips64.core.fpu.FPOverflowException ex)
		    {
			    parser.addError(token, "FP_OVERFLOW");
		    }
		    catch(edumips64.core.fpu.FPUnderflowException ex)
		    {
			    parser.addError(token, "FP_UNDERFLOW");
            }  
            catch (IrregularStringOfBitsException e) {
			    parser.addError(token, "PARSER_INVALID_VALUE");
            }
        }
    }
}
