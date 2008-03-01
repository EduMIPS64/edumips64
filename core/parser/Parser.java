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

import edumips64.utils.IrregularStringOfBitsException;
import edumips64.core.*;
import edumips64.core.is.Instruction;
import edumips64.core.parser.tokens.*;
import java.util.*;
import java.io.*;

public class Parser {
    // Association between directives and parsing algorithms (Strategy design
    // pattern)
    protected HashMap<String, ParsingAlgorithm> algorithms;

    // List of parsing errors
    protected List<ParserException> exceptions;
    protected boolean hasOnlyWarnings = true;   // Set to false if the parser encounters errors

    // Stupid inner class, because we are using a stupid programming language
    // And no, I'm not going to write getters and setters.
    protected class InstructionData {
        public InstructionData(Instruction instr, List<Token> params, int address, Token token) {
            this.instr = instr;
            this.params = params;
            this.address = address;
            this.token = token;
        }
        public Instruction instr;
        public Token token;
        public int address;
        public List<Token> params;
    }

    // List of instructions with parameters
    protected List<InstructionData> instructions;

    protected static Parser instance;
    protected Scanner scanner;
    protected ParsingAlgorithm default_alg;
    protected SymbolTable symbols;
    protected Memory memory;
    protected String path;
    /* --------------------
     * Public methods
     * --------------------
     */

    public void reset() {
        edumips64.Main.logger.debug("Resetting parser");
        instructions = new LinkedList<InstructionData>();
        exceptions = new LinkedList<ParserException>();
        hasOnlyWarnings = true;
    }


	public void parse(String filename) throws FileNotFoundException, SecurityException, IOException,ParserErrorsException {
        edumips64.Main.logger.debug("Parse begins!");

        String absolutePath = new File(filename).getAbsolutePath();
        int separator = new File(filename).getAbsolutePath().lastIndexOf(File.separator);

        edumips64.Main.logger.debug("Absolute path " + absolutePath);
        edumips64.Main.logger.debug("Separator: " + separator);

        if(separator != -1)
            this.path = absolutePath.substring(0,separator);
        else
            this.path = "";

        edumips64.Main.logger.debug("Filename: " + filename);
        edumips64.Main.logger.debug("Path: " + path);

        BufferedReader reader = new BufferedReader(preProcess(absolutePath));
        scanner = new Scanner(reader);
        default_alg.parse(scanner);

        // Packing instruction
        edumips64.Main.logger.debug("Will now pack the instructions");
        for(InstructionData i : instructions) {
            edumips64.Main.logger.debug("Processing " + i.instr.getFullName());
            for(Token t : i.params) {
                edumips64.Main.logger.debug("Adding " + t + " to parameters list");
                try {
                    t.addToParametersList(i.instr);
                }
                catch(ParameterException e) {
                    // TODO: correzione addError
                    addError(t, e.getMessage());
                }
            }

            for(Integer __ : i.instr.getParams())
                edumips64.Main.logger.debug("Param: " + __);

            try {
                i.instr.pack();
                edumips64.Main.logger.debug("Instruction packed, adding to memory");
                memory.addInstruction(i.instr, i.address);
            }
            catch (SymbolTableOverflowException e) {
                addError(i.token, "PARSER_OUT_OF_BOUNDS");
            }
            // TODO: verificare il precedente try/catch
            catch (Exception e) {
                addError(i.token, "PARSER_UNKNOWN");
            }
        }

        // Throw exception if needed
        if(exceptions.size() > 0)
            throw new ParserErrorsException(exceptions, false);
    }

    // Singleton design pattern
    public static Parser getInstance() {
        if(instance == null)
            instance = new Parser();
        return instance;
    };
    /* ----------------------------
     * Package-wide visible methods
     * used by parsing algorithms
     * ----------------------------
     */
    boolean hasAlgorithm(String directive) {
        return algorithms.containsKey(directive);
    }

    void switchParsingAlgorithm(String directive) { 
        edumips64.Main.logger.debug("Switching parser due to directive " + directive);
        algorithms.get(directive).parse(scanner);
    }

    // TODO: right now the addError method prints the error.
    // It will use ParserMultiException to report errors to user.
    void addError(Token t, String error) {
        edumips64.Main.logger.debug("************* " + error + ": " + t);
        System.out.println("************* " + error + ": " + t);
        hasOnlyWarnings = false;
        exceptions.add(new ParserException(error, t.getLine(), 0, t.toString(), true));
    }

    void addInstruction(Instruction instr, int address, List<Token> params, String label, Token instrToken) {
        //edumips64.Main.logger.debug("Adding " + instr + " to SymbolTable, label " + label + ", address " + address);
        if(label != null) {
            try {
                symbols.setInstructionLabel(address, label);
                instr.setLabel(label);
                edumips64.Main.logger.debug("ADDED LABEL " + label);
            }
            catch (SameLabelsException e) {
                addError(instrToken, "PARSER_DUPLICATE_LABEL");
            }
        }
        // For later parameters processing
        instructions.add(new InstructionData(instr, params, address, instrToken));
    }

    void addMemoryAddressToSymbolTable(int address, Token label) throws MemoryElementNotFoundException{
        edumips64.Main.logger.debug("Adding " + label.getBuffer() + " to SymbolTable, address " + address);
        try {
            symbols.setCellLabel(address, label.getBuffer());
        }
        catch (SameLabelsException e) {
            addError(label, "PARSER_DUPLICATE_LABEL");
        }
    }


    static boolean isInstruction(Token t) {
        // TODO: should we improve it?
        return Instruction.buildInstruction(t.getBuffer()) != null;
    }


    /* -----------------
     * Protected methods
     * -----------------
     */
	protected String readLines(Reader stream) throws IOException
	{
		String ret = "";
		String line;
        BufferedReader in = new BufferedReader(stream);

		while ((line = in.readLine()) != null)
		    ret += line + "\n";

		return ret;
	}

    protected String processInclude(String filename, Set<String> alreadyIncluded){
        edumips64.Main.logger.debug("processInclude file " + filename);
        edumips64.Main.logger.debug("Files already included: ");
        for(String s : alreadyIncluded)
            edumips64.Main.logger.debug(s);

        BufferedReader reader;
        String code;
        try{
            if(!(new File(filename)).isAbsolute())
                filename = this.path + File.separator + filename;

            if(alreadyIncluded.contains(filename)){
                edumips64.Main.logger.debug("file " + filename + " already included");
                return new String("");
            }

            alreadyIncluded.add(filename);
            
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"ISO-8859-1"));

            code = readLines(reader);
            int i = 0;
            while(true){
                i = code.indexOf("#include", i);

                if(i == -1)
                    break;

                int end = code.indexOf("\n", i);
                if(end == -1)
                    end = code.length();

                String includedFileName = code.substring(i+9, end).split(";")[0].trim();
                code = code.substring(0,i) + processInclude(includedFileName,alreadyIncluded) + code.substring(end);
            }
        }
        catch(IOException e){
            addError(new ErrorToken("#include"), "PARSER_WRONG_INCLUDE");
            return new String("");
        }
        
        return code;
    }

    protected Reader preProcess(String fileName){
        Set<String> included = new HashSet<String>();
        String code = processInclude(fileName,included);
        return new StringReader(code);
    }

    protected Parser() {
        algorithms = new HashMap<String, ParsingAlgorithm>();
        default_alg = new NullParsingAlgorithm(this);
        CodeParsingAlgorithm code_pa = new CodeParsingAlgorithm(this);
        DataParsingAlgorithm data_pa = new DataParsingAlgorithm(this);
        symbols = SymbolTable.getInstance();
        memory = Memory.getInstance();
        instructions = new LinkedList<InstructionData>();
        exceptions = new LinkedList<ParserException>();

        // Association of parsing algorithms with directives
        registerAlgorithm(".DATA", data_pa);
        registerAlgorithm(".CODE", code_pa);
        registerAlgorithm(".TEXT", code_pa);
    }

    protected void registerAlgorithm(String directive, ParsingAlgorithm p) {
        edumips64.Main.logger.debug("Registering a parser for directive " + directive + ", " + p.toString());
        algorithms.put(directive, p);
    }

}
