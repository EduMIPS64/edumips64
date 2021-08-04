/*
 * Parser.java
 *
 * Parses a MIPS64 source code and fills the symbol table and the memory.
 *
 * (c) 2006 mancausoft, Vanni
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

/** Parses a MIPS64 source code and fills the symbol table and the memory.
 * @author mancausoft, Vanni
 */

package org.edumips64.core.parser;

import org.edumips64.core.Converter;
import org.edumips64.core.FCSRRegister;
import org.edumips64.core.IrregularStringOfBitsException;
import org.edumips64.core.IrregularStringOfHexException;
import org.edumips64.core.IrregularWriteOperationException;
import org.edumips64.core.Memory;
import org.edumips64.core.MemoryElement;
import org.edumips64.core.MemoryElementNotFoundException;
import org.edumips64.core.NotAlignException;
import org.edumips64.core.SameLabelsException;
import org.edumips64.core.StringFormatException;
import org.edumips64.core.SymbolTable;
import org.edumips64.core.SymbolTableOverflowException;
import org.edumips64.core.fpu.FPInstructionUtils;

import org.edumips64.core.fpu.FPOverflowException;
import org.edumips64.core.fpu.FPUnderflowException;
import org.edumips64.core.is.Instruction;
import org.edumips64.core.is.InstructionBuilder;
import org.edumips64.core.is.ParsedInstructionMetadata;
import org.edumips64.utils.io.FileUtils;
import org.edumips64.utils.io.ReadException;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

class VoidJump {
  Instruction instr;
  String label;
  int row;
  int column;
  int instrCount;
  String line;
  boolean isBranch = false;
}

public class Parser {
  /** Instance variables */
  private static final Logger logger = Logger.getLogger(Parser.class.getName());
  private final Memory mem;

  private enum AliasRegister
  {zero, at, v0, v1, a0, a1, a2, a3, t0, t1, t2, t3, t4, t5, t6, t7, s0, s1, s2, s3, s4, s5, s6, s7, t8, t9, k0, k1, gp, sp, fp, ra}
  private static final String deprecateInstruction[] = {"BNEZ", "BEQZ", "HALT", "DADDUI", "DMULU", "L.D", "S.D"};

  private ParserMultiException error;
  /** Base basePath to use for further #include directives. */
  private String basePath;
  private int numError;

  private enum FileSection {NONE, DATA, TEXT}

  private FileSection section;
  /** File to be parsed
  */
  private int memoryCount;
  private String filename;
  private SymbolTable symTab;
  private FileUtils fileUtils;
  private InstructionBuilder instructionBuilder;
  private FPInstructionUtils fpInstructionUtils;

  /** Accessible only because of unit tests.
   *  The Parser needs an FCSR register only because the core FP functions of the simulator
   *  are coupled too tightly with the actual implementation of the FPU, and therefore they assume
   *  that there is an FCSR register and use it to decide whether to throw an exception or not.
   *
   *  The Parser uses the FP functions to parse, and as a result it needs to have an FCSR. Not ideal. */
  private FCSRRegister fcsr;
  public FCSRRegister getFCSR() {
    return fcsr;
  }

  /** Public methods */
  public Parser(FileUtils utils, SymbolTable symTab, Memory memory, InstructionBuilder instructionBuilder) {
    this.symTab = symTab;
    this.fileUtils = utils;
    this.mem = memory;
    this.instructionBuilder = instructionBuilder;
    this.fcsr = new FCSRRegister();
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.INVALID_OPERATION, true);
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.OVERFLOW, true);
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.UNDERFLOW, true);
    fcsr.setFPExceptions(FCSRRegister.FPExceptions.DIVIDE_BY_ZERO, true);
    fpInstructionUtils = new FPInstructionUtils(this.fcsr);
  }

  /** Loading from File
   * @param filename A String with the system-dependent file name. It should be an absolute file name.
   * @throws SecurityException if a security manager exists and its checkRead method denies read access to the file.
   * @returns A string containing the code parsed.
   */
  public void parse(String filename) throws ParserMultiException, ReadException {
    logger.info("About to parse " + filename);
    this.filename = filename;
    basePath = fileUtils.GetBasePath(filename);
    String code = preprocessor(filename);
    doParsing(code);
    logger.info(filename + " correctly parsed.");
  }

  /** Replace all Tabulator with space
   * @param text the string to replace
   * @return a new String
   */
  public static String replaceTab(String text) {
    return text.replace("\t", " ");
  }

  /** Private methods */
  private String fileToString(String filename) throws ReadException {
    return fileUtils.ReadFile(filename);
  }

  private void checkLoop(String data, Stack<String> included) throws ParserMultiException, ReadException {
    int i = 0;

    do {
      i = data.indexOf("#include ", i);

      if (i != -1) {
        int end = data.indexOf("\n", i);

        if (end == -1) {
          end = data.length();
        }

        int a = included.search(data.substring(i + 9, end).trim());

        if (a != -1) {
          error = new ParserMultiException();
          error.add("INCLUDE_LOOP", 0, 0, "#include " + data.substring(i + 9, end).trim());
          throw error;
        }

        String filename = data.substring(i + 9, end).split(";") [0].trim();

        if (!fileUtils.isAbsolute(filename)) {
          filename = basePath + filename;
        }

        String filetmp = fileToString(filename);
        checkLoop(filetmp , included);
        i ++;
      }
    } while (i != -1);
  }

  /** Process the #include (Syntax #include file.ext )
   */
  private String preprocessor(String filename) throws ParserMultiException, ReadException {
    String filetmp;

    filetmp = fileToString(filename);

    int i = 0;

    //check loop
    Stack<String> included = new Stack<>();
    included.push(this.filename);

    checkLoop(filetmp, included);

    // include
    do {
      i = filetmp.indexOf("#include ", i);

      if (i != -1) {
        int end = filetmp.indexOf("\n", i);

        if (end == -1) {
          end = filetmp.length();
        }

        logger.info("Open by #include: " + filetmp.substring(i + 9, end).trim());
        String includedFilename = filetmp.substring(i + 9, end).split(";") [0].trim();

        if (!fileUtils.isAbsolute(includedFilename)) {
          includedFilename = basePath + includedFilename;
        }

        String fileContents = fileToString(includedFilename);
        filetmp = filetmp.substring(0, i) + fileContents + filetmp.substring(end);
      }

    } while (i != -1);

    return filetmp;
  }

  public void doParsing(String code) throws ParserMultiException {
    boolean isFirstOutOfInstructionMemory = true;
    boolean isFirstOutOfMemory = true;
    boolean halt = false;
    int row = 0;
    numError = 0;
    int numWarning = 0;
    int instrCount = -4;    // Hack fituso by Andrea
    error = new ParserMultiException();
    ParserMultiWarningException warning = new ParserMultiWarningException();

    LinkedList<VoidJump> voidJump = new LinkedList<>();

    memoryCount = 0;
    String lastLabel = "";

    code = code.replaceAll("\r\n", "\n");
    for (String line : code.split("\n")) {
      row++;
      logger.info("-- Processing line " + row);

      for (int column = 0; column < line.length(); column++) {
        if (line.charAt(column) == ';') {  //comments
          break;
        }

        if (line.charAt(column) == ' ' || line.charAt(column) == '\t') {
          continue;
        }

        int tab = line.indexOf('\t', column);
        int space = line.indexOf(' ', column);

        if (tab == -1) {
          tab = line.length();
        }

        if (space == -1) {
          space = line.length();
        }

        int end = Math.min(tab, space) - 1;

        String instr = line.substring(column, end + 1);
        String parameters;

        try {
          if (line.charAt(column) == '.') {
            logger.info("Processing " + instr);

            if (instr.compareToIgnoreCase(".DATA") == 0) {
              section = FileSection.DATA;
            } else if (instr.compareToIgnoreCase(".TEXT") == 0 || instr.compareToIgnoreCase(".CODE") == 0) {
              section = FileSection.TEXT;
            } else {
              String name = instr.substring(1);   // The name, without the dot.

              if (section != FileSection.DATA) {
                numError++;
                error.add(name.toUpperCase() + "INCODE", row, column + 1, line);
                column = line.length();
                continue;
              }

              try {
                if (!((instr.compareToIgnoreCase(".ASCII") == 0) || instr.compareToIgnoreCase(".ASCIIZ") == 0)) {
                  // We don't want strings to be uppercase, do we?
                  parameters = cleanFormat(line.substring(end + 2));
                  parameters = parameters.toUpperCase();
                  parameters = parameters.split(";") [0];
                  logger.info("parameters: " + parameters);
                } else {
                  parameters = line.substring(end + 2);
                }

                parameters = parameters.split(";") [0].trim();
                logger.info("parameters: " + parameters);
              } catch (StringIndexOutOfBoundsException e) {
                numWarning++;
                warning.add("VALUE_MISS", row, column + 1, line);
                error.addWarning("VALUE_MISS", row, column + 1, line);
                memoryCount++;
                column = line.length();
                continue;
              }

              MemoryElement tmpMem;
              tmpMem = mem.getCellByIndex(memoryCount);
              logger.info("line: " + line);
              String[] comment = (line.substring(column)).split(";", 2);

              if (comment.length == 2) {
                logger.info("found comments: " + comment[1]);
                tmpMem.setComment(comment[1]);
              }

              tmpMem.setCode(comment[0]);

              if (instr.compareToIgnoreCase(".ASCII") == 0 || instr.compareToIgnoreCase(".ASCIIZ") == 0) {
                logger.info(".ascii(z): parameters = " + parameters);
                boolean auto_terminate = false;

                if (instr.compareToIgnoreCase(".ASCIIZ") == 0) {
                  auto_terminate = true;
                }

                try {
                  List<String> pList = splitStringParameters(parameters, auto_terminate);

                  for (String current_string : pList) {
                    logger.info("Current string: [" + current_string + "]");
                    logger.info(".ascii(z): requested new memory cell (" + memoryCount + ")");
                    tmpMem = mem.getCellByIndex(memoryCount);
                    memoryCount++;
                    int posInWord = 0;
                    // TODO: Controllo sui parametri (es. virgolette?)
                    int num = current_string.length();
                    boolean escape = false;
                    boolean placeholder = false;
                    int escaped = 0;    // to avoid escape sequences to count as two bytes

                    for (int tmpi = 0; tmpi < num; tmpi++) {
                      if ((tmpi - escaped) % 8 == 0 && (tmpi - escaped) != 0 && !escape) {
                        logger.info(".ascii(z): requested new memory cell (" + memoryCount + ")");
                        tmpMem = mem.getCellByIndex(memoryCount);
                        memoryCount++;
                        posInWord = 0;
                      }

                      char c = current_string.charAt(tmpi);
                      int to_write = (int) c;
                      logger.info("Char: " + c + " (" + to_write + ") [" + Integer.toHexString(to_write) + "]");

                      if (escape) {
                        switch (c) {
                        case '0':
                          to_write = 0;
                          break;
                        case 'n':
                          to_write = 10;
                          break;
                        case 't':
                          to_write = 9;
                          break;
                        case '\\':
                          to_write = 92;
                          break;
                        case '"':
                          to_write = 34;
                          break;
                        default:
                          throw new StringFormatException();
                        }

                        logger.info("(escaped to [" + Integer.toHexString(to_write) + "])");
                        escape = false;
                        c = 0;  // to avoid re-entering the escape if branch.
                      }

                      if (placeholder) {
                        if (c != '%' && c != 's' && c != 'd' && c != 'i') {
                          logger.info("Invalid placeholder: %" + c);
                          // Invalid placeholder
                          throw new StringFormatException();
                        }

                        placeholder = false;
                      } else if (c == '%') {
                        logger.info("Expecting on next step a valid placeholder...");
                        placeholder = true;
                      }

                      if (c == '\\') {
                        escape = true;
                        escaped++;
                        continue;
                      }

                      tmpMem.writeByte(to_write, posInWord++);
                    }
                  }
                } catch (StringFormatException ex) {
                  logger.info("Badly formed string list");
                  numError++;
                  // TODO: more descriptive error message
                  error.add("INVALIDVALUE", row, 0, line);
                }

                end = line.length();
              } else if (instr.compareToIgnoreCase(".SPACE") == 0) {
                int posInWord = 0; //position of byte to write into a doubleword
                memoryCount++;

                try {
                  if (Converter.isHexNumber(parameters)) {
                    parameters = Converter.hexToLong(parameters);
                  }

                  if (Converter.isInteger(parameters)) {
                    int num = Integer.parseInt(parameters);

                    for (int tmpi = 0; tmpi < num; tmpi++) {
                      if (tmpi % 8 == 0 && tmpi != 0) {
                        tmpMem = mem.getCellByIndex(memoryCount);
                        memoryCount++;
                        posInWord = 0;
                      }

                      tmpMem.writeByte(0, posInWord++);
                    }
                  } else {
                    throw new NumberFormatException();
                  }
                } catch (NumberFormatException ex) {
                  numError++;
                  error.add("INVALIDVALUE", row, column + 1, line);
                  continue;
                } catch (IrregularStringOfHexException ex) {
                  numError++;
                  error.add("INVALIDVALUE", row, column + 1, line);
                  continue;
                }

                posInWord ++;
                end = line.length();
              } else if (instr.compareToIgnoreCase(".WORD") == 0 || instr.compareToIgnoreCase(".WORD64") == 0) {
                logger.info("pamword: " + parameters);
                writeIntegerInMemory(row, column, line, parameters, 64, "WORD");
                end = line.length();
              } else if (instr.compareToIgnoreCase(".WORD32") == 0) {
                writeIntegerInMemory(row, column, line, parameters, 32, "WORD32");
                end = line.length();
              } else if (instr.compareToIgnoreCase(".BYTE") == 0) {
                writeIntegerInMemory(row, column, line, parameters, 8, "BYTE");
                end = line.length();
              } else if (instr.compareToIgnoreCase(".WORD16") == 0) {
                writeIntegerInMemory(row, column, line, parameters, 16 , "WORD16");
                end = line.length();
              } else if (instr.compareToIgnoreCase(".DOUBLE") == 0) {
                writeDoubleInMemory(row, column, line, parameters);
                end = line.length();
              } else {
                numError++;
                error.add("INVALIDCODEFORDATA", row, column + 1, line);
                column = line.length();
                continue;
              }
            }
          } else if (line.charAt(end) == ':') {
            String label = line.substring(column, end);
            logger.info("Processing label " + label);

            if (section == FileSection.DATA) {
              logger.info("in .data section");

              try {
                symTab.setCellLabel(memoryCount * 8, label);
              } catch (SameLabelsException e) {
                // TODO: errore del parser
                logger.warning("Label " + label + " is already assigned");
              }
            } else if (section == FileSection.TEXT) {
              logger.info("in .text section");
              lastLabel = label;
            }

            logger.info("done");
          } else {
            if (section != FileSection.TEXT) {
              numError++;
              error.add("INVALIDCODEFORDATA", row, column + 1, line);
              column = line.length();
              continue;

            } else if (section == FileSection.TEXT) {
              boolean doPack = true;
              end++;
              Instruction tmpInst;

              // Check for halt-like instructions
              String temp = cleanFormat(line.substring(column)).toUpperCase();

              if (temp.contains("HALT") || temp.contains("SYSCALL 0") || temp.contains("TRAP 0")) {
                halt = true;
              }

              //timmy
              for (int timmy = 0; timmy < deprecateInstruction.length; timmy++) {
                if (deprecateInstruction[timmy].toUpperCase().equals(line.substring(column, end).toUpperCase())) {
                  warning.add("WINMIPS64_NOT_MIPS64", row, column + 1, line);
                  error.addWarning("WINMIPS64_NOT_MIPS64", row, column + 1, line);
                  numWarning++;
                }
              }

              // Parsing the instruction name to get the type of parameters to expect.
              ParsedInstructionMetadata meta = new ParsedInstructionMetadata(row, instrCount+4);
              tmpInst = instructionBuilder.buildInstruction(line.substring(column, end).toUpperCase(), meta);

              if (tmpInst == null) {
                numError++;
                error.add("INVALIDCODE", row, column + 1, line);
                column = line.length();
                continue;
              }


              // Syntax of the instruction. A string in a form like %R,%R,%I.
              // See @{link Instruction#getSyntax getSyntax} for a list of 
              // symbols to specify the types of parameters.
              String syntax = tmpInst.getSyntax();
              instrCount += 4;

              if (syntax.compareTo("") != 0 && (line.length() < end + 1)) {
                numError++;
                error.add("UNKNOWNSYNTAX", row, end, line);
                column = line.length();
                continue;
              }

              // Parse parameters if needed.
              if (syntax.compareTo("") != 0) {
                String param = cleanFormat(line.substring(end + 1));
                param = param.toUpperCase();
                param = param.split(";") [0].trim();
                logger.info("param: " + param);
                int paramStart = 0;

                for (int syntaxIterator = 0; syntaxIterator < syntax.length(); syntaxIterator++) {
                  if (syntax.charAt(syntaxIterator) == '%') {
                    syntaxIterator++; // Skip over the %.
                    var type = syntax.charAt(syntaxIterator);

                    // If there is another separator (e.g., a comma or a parenthesis)
                    // save it in nextToken so we can try to find it later.
                    Character nextToken = ' ';
                    if (syntaxIterator < syntax.length() - 1) {
                      nextToken = syntax.charAt(syntaxIterator+1);
                    }

                    // Find the end of the parameter value, and as part of that validate
                    // that the correct separator is present.
                    int paramEnd;
                    if (syntaxIterator != syntax.length() - 1) {
                      paramEnd = param.indexOf(nextToken, paramStart);
                    } else {
                      paramEnd = param.length();
                    }

                    if (paramEnd == -1) {
                      numError++;
                      error.add("SEPARATORMISS", row, paramStart, line);
                      column = line.length();
                      tmpInst.getParams().add(0);
                      continue;
                    }

                    var paramValue = param.substring(paramStart, paramEnd);

                    // %R: General Purpose Register.
                    if (type == 'R') {
                      int reg;

                      if ((reg = isRegister(paramValue.trim())) >= 0) {
                        tmpInst.getParams().add(reg);
                        paramStart = paramEnd + 1;
                      } else {
                        numError++;
                        error.add("INVALIDREGISTER", row, line.indexOf(paramValue) + 1, line);
                        tmpInst.getParams().add(0);
                        column = line.length();
                        continue;
                      }

                    // %F: Floating Point Register.
                    } else if (type == 'F') {
                      int reg;

                      if ((reg = isRegisterFP(paramValue.trim())) >= 0) {
                        tmpInst.getParams().add(reg);
                        paramStart = paramEnd + 1;
                      } else {
                        numError++;
                        error.add("INVALIDREGISTER", row, line.indexOf(paramValue) + 1, line);
                        tmpInst.getParams().add(0);
                        column = line.length();
                        continue;
                      }
                    
                    // %I: 16-bit immediate.
                    } else if (type == 'I') {
                      int imm;

                      if (Converter.isImmediate(paramValue)) {
                        if (param.charAt(paramStart) == '#') {
                          paramStart++;
                          paramValue = paramValue.substring(1);
                        }

                        if (Converter.isInteger(paramValue)) {
                          try {
                            imm = Integer.parseInt(paramValue);

                            if (imm < -32768 || imm > 32767) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);
                          }

                          tmpInst.getParams().add(imm);
                          paramStart = paramEnd + 1;
                        } else if (Converter.isHexNumber(paramValue)) {
                          try {
                            try {
                              imm = (int) Long.parseLong(Converter.hexToShort(paramValue));
                              logger.info("imm = " + imm);

                              if (imm < -32768 || imm > 32767) {
                                throw new NumberFormatException();
                              }
                            } catch (NumberFormatException ex) {
                              imm = 0;
                              numError++;
                              error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);
                            }

                            tmpInst.getParams().add(imm);
                            paramStart = paramEnd + 1;
                          } catch (IrregularStringOfHexException ex) {
                            //non ci dovrebbe mai arrivare
                          }
                        }

                      } else {
                        try {
                          int cc;
                          MemoryElement tmpMem;
                          cc = param.indexOf("+", paramStart);

                          if (cc != -1) {
                            tmpMem = symTab.getCell(param.substring(paramStart, cc).trim());

                            if (Converter.isInteger(param.substring(cc + 1, paramEnd))) {
                              try {
                                imm = Integer.parseInt(paramValue);

                                if (imm < -32768 || imm > 32767) {
                                  throw new NumberFormatException();
                                }
                              } catch (NumberFormatException ex) {
                                imm = 0;
                                numError++;
                                error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);
                              }

                              tmpInst.getParams().add(tmpMem.getAddress() + imm);
                              paramStart = paramEnd + 1;
                            } else if (Converter.isHexNumber(param.substring(cc + 1, paramEnd))) {
                              try {
                                try {
                                  imm = (int) Long.parseLong(Converter.hexToLong(paramValue));

                                  if (imm < -32768 || imm > 32767) {
                                    throw new NumberFormatException();
                                  }
                                } catch (NumberFormatException ex) {
                                  imm = 0;
                                  numError++;
                                  error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);
                                }

                                tmpInst.getParams().add(tmpMem.getAddress() + imm);
                                paramStart = paramEnd + 1;
                              } catch (IrregularStringOfHexException ex) {
                                logger.severe("Irregular string of bits: " + ex.getMessage());
                              }
                            } else {
                              MemoryElement tmpMem1 = symTab.getCell(param.substring(cc + 1, paramEnd).trim());
                              tmpInst.getParams().add(tmpMem.getAddress() + tmpMem1.getAddress());
                            }

                          } else {

                            cc = param.indexOf("-", paramStart);

                            if (cc != -1) {
                              tmpMem = symTab.getCell(param.substring(paramStart, cc).trim());

                              if (Converter.isInteger(param.substring(cc + 1, paramEnd))) {
                                try {
                                  imm = Integer.parseInt(paramValue);

                                  if (imm < -32768 || imm > 32767) {
                                    throw new NumberFormatException();
                                  }
                                } catch (NumberFormatException ex) {
                                  imm = 0;
                                  numError++;
                                  error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);
                                }

                                tmpInst.getParams().add(tmpMem.getAddress() - imm);
                                paramStart = paramEnd + 1;
                              } else if (Converter.isHexNumber(param.substring(cc + 1, paramEnd))) {
                                try {
                                  try {
                                    imm = (int) Long.parseLong(Converter.hexToLong(paramValue));

                                    if (imm < -32768 || imm > 32767) {
                                      throw new NumberFormatException();
                                    }
                                  } catch (NumberFormatException ex) {
                                    imm = 0;
                                    numError++;
                                    error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);
                                  }

                                  tmpInst.getParams().add(tmpMem.getAddress() - imm);
                                  paramStart = paramEnd + 1;
                                } catch (IrregularStringOfHexException ex) {
                                  //non ci dovrebbe mai arrivare
                                }
                              } else {
                                MemoryElement tmpMem1 = symTab.getCell(param.substring(cc + 1, paramEnd).trim());
                                tmpInst.getParams().add(tmpMem.getAddress() - tmpMem1.getAddress());
                              }
                            } else {
                              tmpMem = symTab.getCell(paramValue.trim());
                              tmpInst.getParams().add(tmpMem.getAddress());
                            }
                          }
                        } catch (MemoryElementNotFoundException ex) {
                          numError++;
                          error.add("INVALIDIMMEDIATE", row, line.indexOf(paramValue) + 1, line);
                          column = line.length();
                          tmpInst.getParams().add(0);
                          continue;
                        }
                      }
                    
                    // %U: Unsigned immediate.
                    } else if (type == 'U') {
                      int imm;

                      if (Converter.isImmediate(paramValue)) {
                        if (param.charAt(paramStart) == '#') {
                          paramStart++;
                          paramValue = paramValue.substring(1);
                        }

                        if (Converter.isInteger(paramValue)) {
                          try {
                            imm = Integer.parseInt(paramValue.trim());

                            if (imm < 0) {
                              numError++;
                              error.add("VALUEISNOTUNSIGNED", row, line.indexOf(paramValue) + 1, line);
                              column = line.length();
                              tmpInst.getParams().add(0);
                              continue;
                            }

                            if (imm < 0 || imm > 31) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("5BIT_IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);
                          }

                          tmpInst.getParams().add(imm);
                          paramStart = paramEnd + 1;
                        } else if (Converter.isHexNumber(paramValue.trim())) {
                          try {
                            imm = (int) Long.parseLong(Converter.hexToLong(paramValue));

                            if (imm < 0) {
                              numError++;
                              error.add("VALUEISNOTUNSIGNED", row, line.indexOf(paramValue) + 1, line);
                              column = line.length();
                              tmpInst.getParams().add(0);
                              continue;
                            }

                            tmpInst.getParams().add(imm);
                            paramStart = paramEnd + 1;

                            if (imm < 0 || imm > 31) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("5BIT_IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);

                            tmpInst.getParams().add(imm);
                            paramStart = paramEnd + 1;

                          } catch (IrregularStringOfHexException ex) {
                            //non ci dovrebbe mai arrivare
                          }
                        }

                      } else {
                        numError++;
                        error.add("INVALIDIMMEDIATE", row, line.indexOf(paramValue) + 1, line);
                        column = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }
                    
                    // %C: Unsigned Immediate (3 bit).
                    } else if (type == 'C') {
                      int imm;

                      if (Converter.isImmediate(paramValue)) {
                        if (param.charAt(paramStart) == '#') {
                          paramStart++;
                          paramValue = paramValue.substring(1);
                        }

                        if (Converter.isInteger(paramValue)) {
                          try {
                            imm = Integer.parseInt(paramValue.trim());

                            if (imm < 0) {
                              numError++;
                              error.add("VALUEISNOTUNSIGNED", row, line.indexOf(paramValue) + 1, line);
                              column = line.length();
                              tmpInst.getParams().add(0);
                              continue;
                            }

                            if (imm < 0 || imm > 7) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("3BIT_IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);
                          }

                          tmpInst.getParams().add(imm);
                          paramStart = paramEnd + 1;
                        } else if (Converter.isHexNumber(paramValue.trim())) {
                          try {
                            imm = (int) Long.parseLong(Converter.hexToLong(paramValue));

                            if (imm < 0) {
                              numError++;
                              error.add("VALUEISNOTUNSIGNED", row, line.indexOf(paramValue) + 1, line);
                              column = line.length();
                              tmpInst.getParams().add(0);
                              continue;
                            }

                            tmpInst.getParams().add(imm);
                            paramStart = paramEnd + 1;

                            if (imm < 0 || imm > 31) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("3BIT_IMMEDIATE_TOO_LARGE", row, line.indexOf(paramValue) + 1, line);

                            tmpInst.getParams().add(imm);
                            paramStart = paramEnd + 1;

                          } catch (IrregularStringOfHexException ex) {
                            //non ci dovrebbe mai arrivare
                          }
                        }

                      } else {
                        numError++;
                        error.add("INVALIDIMMEDIATE", row, line.indexOf(paramValue) + 1, line);
                        column = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }
                    
                    // %L: Memory Label.
                    } else if (type == 'L') {
                      try {
                        MemoryElement tmpMem;

                        if (paramValue.equals("")) {
                          tmpInst.getParams().add(0);
                        } else if (Converter.isInteger(paramValue.trim())) {
                          int tmp = Integer.parseInt(paramValue.trim());

                          if (tmp < Memory.MIN_OFFSET_BYTES || tmp > Memory.MAX_OFFSET_BYTES) {
                            numError++;
                            String er = "LABELADDRESSINVALID";

                            error.add(er, row, line.indexOf(paramValue) + 1, line);
                            column = line.length();
                            paramStart = paramEnd + 1;
                            tmpInst.getParams().add(0);
                            continue;
                          }

                          tmpInst.getParams().add(tmp);
                        } else {
                          tmpMem = symTab.getCell(paramValue.trim());
                          tmpInst.getParams().add(tmpMem.getAddress());

                        }

                        paramStart = paramEnd + 1;
                      } catch (MemoryElementNotFoundException e) {
                        numError++;
                        error.add("LABELNOTFOUND", row, line.indexOf(paramValue) + 1, line);
                        column = line.length();
                        paramStart = paramEnd + 1;
                        tmpInst.getParams().add(0);
                        continue;
                      }

                    // %E: Program label used for Jump instructions.
                    } else if (type == 'E') {
                      String label = paramValue.trim();
                      Integer labelAddr = symTab.getInstructionAddress(label);
                      logger.info("Label " + label + " at address " + labelAddr);

                      if (labelAddr != null) {
                        tmpInst.getParams().add(labelAddr);
                      } else {
                        VoidJump tmpVoid = new VoidJump();
                        tmpVoid.instr = tmpInst;
                        tmpVoid.row = row;
                        tmpVoid.line = line;
                        tmpVoid.column = paramStart;
                        tmpVoid.label = label;
                        voidJump.add(tmpVoid);
                        doPack = false;
                      }

                    // %B: Program label used for Branch instructions.
                    } else if (type == 'B') {
                      Integer labelAddr = symTab.getInstructionAddress(paramValue.trim());

                      if (labelAddr != null) {
                        labelAddr -= instrCount + 4;
                        tmpInst.getParams().add(labelAddr);
                      } else {
                        VoidJump tmpVoid = new VoidJump();
                        tmpVoid.instr = tmpInst;
                        tmpVoid.row = row;
                        tmpVoid.line = line;
                        tmpVoid.column = paramStart;
                        tmpVoid.label = paramValue;
                        tmpVoid.instrCount = instrCount;
                        tmpVoid.isBranch = true;
                        voidJump.add(tmpVoid);
                        doPack = false;
                      }
                    } else {
                      numError++;
                      error.add("UNKNOWNSYNTAX", row, 1, line);
                      column = line.length();
                      tmpInst.getParams().add(0);
                      continue;
                    }
                    
                    // Ugly hack. The code used to rely on "peeking" behavior inside the
                    // parsing of every parameter to actually bring forward the index variable
                    // within each iteration. Since we removed the peeking, we still need to move
                    // it forward, waiting for a better version of this code that doesn't need
                    // this index munging.
                    if (syntaxIterator != syntax.length() - 1) {
                      syntaxIterator++; 
                    }
                  } else {
                    if (syntax.charAt(syntaxIterator) != param.charAt(paramStart++)) {
                      numError++;
                      error.add("UNKNOWNSYNTAX", row, 1, line);
                      column = line.length();
                      tmpInst.getParams().add(0);
                      continue;
                    }
                  }
                }

                if (column == line.length()) {
                  continue;
                }
              }

              try {
                if (doPack) {
                  tmpInst.pack();
                }
              } catch (IrregularStringOfBitsException e) {
                logger.severe("Irregular string of bits: " + e.getMessage());
              }

              logger.info("row: " + line);
              String comment[] = line.split(";", 2);
              tmpInst.setFullName(replaceTab(comment[0].substring(column)));
              tmpInst.setFullName(replaceTab(comment[0].substring(column)));
              tmpInst.setFullName(replaceTab(comment[0].substring(column)));
              tmpInst.setFullName(replaceTab(comment[0].substring(column)));

              if (comment.length == 2) {
                tmpInst.setComment(comment[1]);
              }

              try {
                mem.addInstruction(tmpInst, instrCount);
                if (lastLabel != null && !lastLabel.equals("")) {
                  logger.info("About to add label: " + lastLabel);
                  symTab.setInstructionLabel(instrCount, lastLabel.toUpperCase());
                }
              } catch (SymbolTableOverflowException ex) {
                if (isFirstOutOfInstructionMemory) { //is first out of memory?
                  isFirstOutOfInstructionMemory = false;
                  numError++;
                  error.add("OUTOFINSTRUCTIONMEMORY", row, column + 1, line);
                  column = line.length();
                  continue;
                }
              } catch (SameLabelsException ex) {
                numError++;
                error.add("SAMELABEL", row, 1, line);
                column = line.length();
              }
              // Il finally e' totalmente inutile, ma Ãš bello utilizzarlo per la
              // prima volta in un programma ;)
              finally {
                lastLabel = "";
              }

              end = line.length();
            }
          }

          column = end;
        } catch (MemoryElementNotFoundException ex) {
          // Even if we don't report the error, advance i to prevent other errors from
          // being generated on this line.
          column = line.length();
          if (isFirstOutOfMemory) { //is first out of memory?
            isFirstOutOfMemory = false;
            numError++;
            error.add("OUTOFMEMORY_PARSER", row, column + 1, line);
            continue;
          }
        } catch (IrregularWriteOperationException ex) {
          numError++;
          error.add("INVALIDVALUE", row, column + 1, line);
          break;
        }
      }
    }

    for (int i = 0; i < voidJump.size(); i++) {
      Integer labelAddr = symTab.getInstructionAddress(voidJump.get(i).label.trim());

      if (labelAddr != null) {
        if (voidJump.get(i).isBranch) {
          labelAddr -= voidJump.get(i).instrCount + 4;
        }

        voidJump.get(i).instr.getParams().add(labelAddr);

        try {
          voidJump.get(i).instr.pack();
        } catch (IrregularStringOfBitsException ex) {
          logger.severe("Irregular string of bits: " + ex.getMessage());
        }
      } else {
        numError++;
        error.add("LABELNOTFOUND", voidJump.get(i).row, voidJump.get(i).column , voidJump.get(i).line);
        continue;
      }
    }

    if (!halt) { //if Halt is not present in code
      numWarning++;
      warning.add("HALT_NOT_PRESENT", row, 0, "");
      error.addWarning("HALT_NOT_PRESENT", row, 0, "");

      try {
        logger.warning("No terminating instruction detected, adding one.");
        ParsedInstructionMetadata meta = new ParsedInstructionMetadata(row, instrCount+4);
        Instruction tmpInst = instructionBuilder.buildInstruction("SYSCALL", meta);
        tmpInst.getParams().add(0);
        tmpInst.setFullName("SYSCALL 0");

        try {
          tmpInst.pack();
        } catch (IrregularStringOfBitsException ex) {
          logger.severe("Irregular string of bits: " + ex.getMessage());
        }

        mem.addInstruction(tmpInst, (instrCount + 4));
        symTab.setInstructionLabel((instrCount + 4), "");
      } catch (SymbolTableOverflowException ex) {
        if (isFirstOutOfInstructionMemory) { //is first out of memory?
          numError++;
          error.add("OUTOFINSTRUCTIONMEMORY", row, 0, "Halt");
        }
      } catch (SameLabelsException ex) {
        logger.severe("Same labels: " + ex);
      } // impossible
    }

    if (numError > 0) {
      throw error;
    } else if (numWarning > 0) {
      throw warning;
    }
  }

  /** Clean multiple tab or spaces in a badly formatted String. 
   *  @param s the badly formatted String
   *  @return the cleaned String
   */
  private String cleanFormat(String s) {
    if (s == null) {
      logger.warning("Null string passed to cleanFormat, returning an empty string.");
      return "";
    }

    if (s.length() > 0 && s.charAt(0) != ';' &&  s.charAt(0) != '\n') {
      s = s.trim();
      s = s.replace("\t", " ");

      while (s.contains("  ")) {
        s = s.replace("  ", " ");
      }

      s = s.replace(", ", ",");
      s = s.replace(" ,", ",");

      if (s.length() > 0) {
        return s;
      }
    }

    return "";
  }

  /** Check if is a valid string for a register
   *  @param reg the string to validate
   *  @return -1 if reg isn't a valid register, else a number of register
   */
  private int isRegister(String reg) {

    try {
      int num;

      if (reg.charAt(0) == 'r' || reg.charAt(0) == 'R' || reg.charAt(0) == '$')    //ci sono altri modi di scrivere un registro???
        if (Converter.isInteger(reg.substring(1))) {
          num = Integer.parseInt(reg.substring(1));

          if (num < 32 && num >= 0) {
            return num;
          }
        }

      if (reg.charAt(0) == '$' && (num = isAlias(reg.substring(1))) != -1) {
        return num;
      }
    } catch (Exception ignored) {}

    return -1;
  }

  /** Check if is a valid string for a floating point register
   *  @param reg the string to validate
   *  @return -1 if reg isn't a valid register, else a number of register
   */
  private int isRegisterFP(String reg) {

    try {
      int num;

      if (reg.charAt(0) == 'f' || reg.charAt(0) == 'F' || reg.charAt(0) == '$')
        if (Converter.isInteger(reg.substring(1))) {
          num = Integer.parseInt(reg.substring(1));

          if (num < 32 && num >= 0) {
            return num;
          }
        }

      // TODO: is it correct to use those aliases for FP registers?
      if (reg.charAt(0) == '$' && (num = isAlias(reg.substring(1))) != -1) {
        return num;
      }
    } catch (Exception ignored) {}

    return -1;
  }


  /** Check if the parameter is a valid string for an alias-register
   *  @param reg the string to validate
   *  @return -1 if reg isn't a valid alias-register, else a number of
  register
   */
  int isAlias(String reg) {
    for (AliasRegister x : AliasRegister.values()) {
      if (reg.equalsIgnoreCase(x.name())) {
        return x.ordinal();
      }
    }

    return -1;
  }

  /** Write a double in memory
   *  @param row number of row
   *  @param i
   *  @param line the line of code
   *  @param instr params
   */
  private void writeDoubleInMemory(int row, int i, String line, String instr) throws MemoryElementNotFoundException {
    String value[] = instr.split(",");
    MemoryElement tmpMem;

    for (String aValue : value) {
      tmpMem = mem.getCellByIndex(memoryCount);
      memoryCount++;

      // TODO(andrea): unit tests for those 3 cases.
      try {
        tmpMem.setBits(fpInstructionUtils.doubleToBin(aValue.trim()), 0);
      } catch (FPOverflowException ex) {
        numError++;
        //error.add("DOUBLE_TOO_LARGE",row,i+1,line);
        error.add("FP_OVERFLOW", row, i + 1, line);
      } catch (FPUnderflowException ex) {
        numError++;
        error.add("FP_UNDERFLOW", row, i + 1, line);
      } catch (IrregularStringOfBitsException e) {
        numError++;
        error.add("INVALIDVALUE", row, i + 1, line);
        i = line.length();
      }
    }
  }

  /** Write an integer in memory
   *  @param row number of row
   *  @param i
   *  @param line the line of code
   *  @param instr
   *  @param numBit
   *  @param name type of data
   */
  private void writeIntegerInMemory(int row, int i, String line, String instr, int numBit, String name) throws MemoryElementNotFoundException {
    int posInWord = 0; //position of byte to write into a doubleword
    String value[] = instr.split(",");
    MemoryElement tmpMem = null;

    for (int j = 0; j < value.length; j++) {
      if (j % (64 / numBit) == 0) {
        posInWord = 0;
        tmpMem = mem.getCellByIndex(memoryCount);
        memoryCount++;
      }
      String val = value[j].trim();
      if(val.isEmpty()) {
        numError++;
        error.add("INVALIDVALUE", row, i + 1, line);
        i = line.length();
        continue;
      }

      if (Converter.isHexNumber(val)) {
        try {
          val = Converter.hexToLong(val);
        } catch (IrregularStringOfHexException e) {
          numError++;
          error.add("INVALIDVALUE", row, i + 1, line);
          i = line.length();
          continue;
        }
      }

      try {
        // Convert the integer to a long, and then check for overflow.
        long num = Long.parseLong(val);

        if ((num < - (Converter.powLong(2, numBit - 1)) || num > (Converter.powLong(2, numBit - 1) - 1)) &&  numBit != 64) {
          throw new NumberFormatException();
        }

        if (numBit == 8) {
          tmpMem.writeByte((int) num, posInWord);
        } else if (numBit == 16) {
          tmpMem.writeHalf((int) num, posInWord);
        } else if (numBit == 32) {
          tmpMem.writeWord(num, posInWord);
        } else if (numBit == 64) {
          tmpMem.writeDoubleWord(num);
        }

      } catch (NumberFormatException ex) {
        numError++;
        error.add(name.toUpperCase() + "_TOO_LARGE", row, i + 1, line);
        continue;
      } catch (IrregularWriteOperationException | NotAlignException e) {
        e.printStackTrace();
        numError++;
        error.add("INVALIDVALUE", row, i + 1, line);
        i = line.length();
        continue;
      }

      posInWord += numBit / 8;
    }
  }

  private List<String> splitStringParameters(String params, boolean auto_terminate) throws StringFormatException {
    List<String> pList = new LinkedList<>();
    StringBuilder temp = new StringBuilder();
    logger.info("Params: " + params);
    params = params.trim();
    logger.info("After trimming: " + params);
    int length = params.length();
    boolean in_string = false;
    boolean escaping = false;
    boolean comma = false;

    for (int i = 0; i < length; ++i) {
      char c = params.charAt(i);

      if (!in_string) {
        switch (c) {
        case '"':

          if ((!comma && pList.size() != 0) || i == length - 1) {
            throw new StringFormatException();
          }

          in_string = true;
          comma = false;
          break;
        case ' ':
        case '\t':
          break;
        case ',':

          if (comma || i == 0 || i == length - 1) {
            throw new StringFormatException();
          }

          comma = true;
          break;
        default:
          throw new StringFormatException();
        }
      } else {
        if (!escaping && c == '\\') {
          escaping = true;
        } else if (!escaping && c == '"') {
          if (temp.length() > 0) {
            if (auto_terminate) {
              logger.info("Behaving like .asciiz.");
              temp.append((char) 0);
            }

            logger.info("Added to pList string " + temp.toString());
            pList.add(temp.toString());
            temp.setLength(0);
          }

          in_string = false;
        } else {
          if (escaping) {
            escaping = false;
            temp.append('\\');
          }

          temp.append(c);
        }
      }
    }

    if (pList.size() == 0 && in_string)
      // TODO: Unterminated string literal
    {
      throw new StringFormatException();
    }

    return pList;
  }
}
