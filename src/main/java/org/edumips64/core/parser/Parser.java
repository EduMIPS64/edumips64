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

import org.edumips64.core.*;
import org.edumips64.core.fpu.FPInstructionUtils;

import org.edumips64.core.fpu.FPOverflowException;
import org.edumips64.core.fpu.FPUnderflowException;
import org.edumips64.core.is.Instruction;
import org.edumips64.core.is.InstructionBuilder;
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
  public String parse(String filename) throws ParserMultiException, ReadException {
    logger.info("About to parse " + filename);
    this.filename = filename;
    basePath = fileUtils.GetBasePath(filename);
    String code = preprocessor(filename);
    doParsing(code);
    logger.info(filename + " correctly parsed.");
    return code;
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

      for (int i = 0; i < line.length(); i++) {
        if (line.charAt(i) == ';') {  //comments
          break;
        }

        if (line.charAt(i) == ' ' || line.charAt(i) == '\t') {
          continue;
        }

        int tab = line.indexOf('\t', i);
        int space = line.indexOf(' ', i);

        if (tab == -1) {
          tab = line.length();
        }

        if (space == -1) {
          space = line.length();
        }

        int end = Math.min(tab, space) - 1;

        String instr = line.substring(i, end + 1);
        String parameters;

        try {
          if (line.charAt(i) == '.') {
            logger.info("Processing " + instr);

            if (instr.compareToIgnoreCase(".DATA") == 0) {
              section = FileSection.DATA;
            } else if (instr.compareToIgnoreCase(".TEXT") == 0 || instr.compareToIgnoreCase(".CODE") == 0) {
              section = FileSection.TEXT;
            } else {
              String name = instr.substring(1);   // The name, without the dot.

              if (section != FileSection.DATA) {
                numError++;
                error.add(name.toUpperCase() + "INCODE", row, i + 1, line);
                i = line.length();
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
                warning.add("VALUE_MISS", row, i + 1, line);
                error.addWarning("VALUE_MISS", row, i + 1, line);
                memoryCount++;
                i = line.length();
                continue;
              }

              MemoryElement tmpMem;
              tmpMem = mem.getCellByIndex(memoryCount);
              logger.info("line: " + line);
              String[] comment = (line.substring(i)).split(";", 2);

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
                  if (isHexNumber(parameters)) {
                    parameters = Converter.hexToLong(parameters);
                  }

                  if (isNumber(parameters)) {
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
                  error.add("INVALIDVALUE", row, i + 1, line);
                  continue;
                } catch (IrregularStringOfHexException ex) {
                  numError++;
                  error.add("INVALIDVALUE", row, i + 1, line);
                  continue;
                }

                posInWord ++;
                end = line.length();
              } else if (instr.compareToIgnoreCase(".WORD") == 0 || instr.compareToIgnoreCase(".WORD64") == 0) {
                logger.info("pamword: " + parameters);
                writeIntegerInMemory(row, i, line, parameters, 64, "WORD");
                end = line.length();
              } else if (instr.compareToIgnoreCase(".WORD32") == 0) {
                writeIntegerInMemory(row, i, line, parameters, 32, "WORD32");
                end = line.length();
              } else if (instr.compareToIgnoreCase(".BYTE") == 0) {
                writeIntegerInMemory(row, i, line, parameters, 8, "BYTE");
                end = line.length();
              } else if (instr.compareToIgnoreCase(".WORD16") == 0) {
                writeIntegerInMemory(row, i, line, parameters, 16 , "WORD16");
                end = line.length();
              } else if (instr.compareToIgnoreCase(".DOUBLE") == 0) {
                writeDoubleInMemory(row, i, line, parameters);
                end = line.length();
              } else {
                numError++;
                error.add("INVALIDCODEFORDATA", row, i + 1, line);
                i = line.length();
                continue;
              }
            }
          } else if (line.charAt(end) == ':') {
            String label = line.substring(i, end);
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
              error.add("INVALIDCODEFORDATA", row, i + 1, line);
              i = line.length();
              continue;

            } else if (section == FileSection.TEXT) {
              boolean doPack = true;
              end++;
              Instruction tmpInst;

              // Check for halt-like instructions
              String temp = cleanFormat(line.substring(i)).toUpperCase();

              if (temp.contains("HALT") || temp.contains("SYSCALL 0") || temp.contains("TRAP 0")) {
                halt = true;
              }

              //timmy
              for (int timmy = 0; timmy < deprecateInstruction.length; timmy++) {
                if (deprecateInstruction[timmy].toUpperCase().equals(line.substring(i, end).toUpperCase())) {
                  warning.add("WINMIPS64_NOT_MIPS64", row, i + 1, line);
                  error.addWarning("WINMIPS64_NOT_MIPS64", row, i + 1, line);
                  numWarning++;
                }
              }

              tmpInst = instructionBuilder.buildInstruction(line.substring(i, end).toUpperCase());

              if (tmpInst == null) {
                numError++;
                error.add("INVALIDCODE", row, i + 1, line);
                i = line.length();
                continue;
              }


              String syntax = tmpInst.getSyntax();
              instrCount += 4;

              if (syntax.compareTo("") != 0 && (line.length() < end + 1)) {
                numError++;
                error.add("UNKNOWNSYNTAX", row, end, line);
                i = line.length();
                continue;
              }

              if (syntax.compareTo("") != 0) {
                String param = cleanFormat(line.substring(end + 1));
                param = param.toUpperCase();
                param = param.split(";") [0].trim();
                logger.info("param: " + param);
                int indPar = 0;

                for (int z = 0; z < syntax.length(); z++) {
                  if (syntax.charAt(z) == '%') {
                    z++;

                    if (syntax.charAt(z) == 'R') {  //register
                      int endPar;

                      if (z != syntax.length() - 1) {
                        endPar = param.indexOf(syntax.charAt(++z), indPar);
                      } else {
                        endPar = param.length();
                      }

                      if (endPar == -1) {
                        numError++;
                        error.add("SEPARATORMISS", row, indPar, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }

                      int reg;

                      if ((reg = isRegister(param.substring(indPar, endPar).trim())) >= 0) {
                        tmpInst.getParams().add(reg);
                        indPar = endPar + 1;
                      } else {
                        numError++;
                        error.add("INVALIDREGISTER", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                        tmpInst.getParams().add(0);
                        i = line.length();
                        continue;
                      }
                    } else if (syntax.charAt(z) == 'F') {  //floating point register
                      int endPar;

                      if (z != syntax.length() - 1) {
                        endPar = param.indexOf(syntax.charAt(++z), indPar);
                      } else {
                        endPar = param.length();
                      }

                      if (endPar == -1) {
                        numError++;
                        error.add("SEPARATORMISS", row, indPar, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }

                      int reg;

                      if ((reg = isRegisterFP(param.substring(indPar, endPar).trim())) >= 0) {
                        tmpInst.getParams().add(reg);
                        indPar = endPar + 1;
                      } else {
                        numError++;
                        error.add("INVALIDREGISTER", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                        tmpInst.getParams().add(0);
                        i = line.length();
                        continue;
                      }
                    }

                    else if (syntax.charAt(z) == 'I') {  //immediate
                      int endPar;

                      if (z != syntax.length() - 1) {
                        endPar = param.indexOf(syntax.charAt(++z), indPar);
                      } else {
                        endPar = param.length();
                      }

                      if (endPar == -1) {
                        numError++;
                        error.add("SEPARATORMISS", row, indPar, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }

                      int imm;

                      if (isImmediate(param.substring(indPar, endPar))) {
                        if (param.charAt(indPar) == '#') {
                          indPar++;
                        }

                        if (isNumber(param.substring(indPar, endPar))) {
                          try {
                            imm = Integer.parseInt(param.substring(indPar, endPar));

                            if (imm < -32768 || imm > 32767) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                          }

                          tmpInst.getParams().add(imm);
                          indPar = endPar + 1;
                        } else if (isHexNumber(param.substring(indPar, endPar))) {
                          try {
                            try {
                              imm = (int) Long.parseLong(Converter.hexToShort(param.substring(indPar, endPar)));
                              logger.info("imm = " + imm);

                              if (imm < -32768 || imm > 32767) {
                                throw new NumberFormatException();
                              }
                            } catch (NumberFormatException ex) {
                              imm = 0;
                              numError++;
                              error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                            }

                            tmpInst.getParams().add(imm);
                            indPar = endPar + 1;
                          } catch (IrregularStringOfHexException ex) {
                            //non ci dovrebbe mai arrivare
                          }
                        }

                      } else {
                        try {
                          int cc;
                          MemoryElement tmpMem;
                          cc = param.indexOf("+", indPar);

                          if (cc != -1) {
                            tmpMem = symTab.getCell(param.substring(indPar, cc).trim());

                            if (isNumber(param.substring(cc + 1, endPar))) {
                              try {
                                imm = Integer.parseInt(param.substring(indPar, endPar));

                                if (imm < -32768 || imm > 32767) {
                                  throw new NumberFormatException();
                                }
                              } catch (NumberFormatException ex) {
                                imm = 0;
                                numError++;
                                error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                              }

                              tmpInst.getParams().add(tmpMem.getAddress() + imm);
                              indPar = endPar + 1;
                            } else if (isHexNumber(param.substring(cc + 1, endPar))) {
                              try {
                                try {
                                  imm = (int) Long.parseLong(Converter.hexToLong(param.substring(indPar, endPar)));

                                  if (imm < -32768 || imm > 32767) {
                                    throw new NumberFormatException();
                                  }
                                } catch (NumberFormatException ex) {
                                  imm = 0;
                                  numError++;
                                  error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                                }

                                tmpInst.getParams().add(tmpMem.getAddress() + imm);
                                indPar = endPar + 1;
                              } catch (IrregularStringOfHexException ex) {
                                logger.severe("Irregular string of bits: " + ex.getMessage());
                              }
                            } else {
                              MemoryElement tmpMem1 = symTab.getCell(param.substring(cc + 1, endPar).trim());
                              tmpInst.getParams().add(tmpMem.getAddress() + tmpMem1.getAddress());
                            }

                          } else {

                            cc = param.indexOf("-", indPar);

                            if (cc != -1) {
                              tmpMem = symTab.getCell(param.substring(indPar, cc).trim());

                              if (isNumber(param.substring(cc + 1, endPar))) {
                                try {
                                  imm = Integer.parseInt(param.substring(indPar, endPar));

                                  if (imm < -32768 || imm > 32767) {
                                    throw new NumberFormatException();
                                  }
                                } catch (NumberFormatException ex) {
                                  imm = 0;
                                  numError++;
                                  error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                                }

                                tmpInst.getParams().add(tmpMem.getAddress() - imm);
                                indPar = endPar + 1;
                              } else if (isHexNumber(param.substring(cc + 1, endPar))) {
                                try {
                                  try {
                                    imm = (int) Long.parseLong(Converter.hexToLong(param.substring(indPar, endPar)));

                                    if (imm < -32768 || imm > 32767) {
                                      throw new NumberFormatException();
                                    }
                                  } catch (NumberFormatException ex) {
                                    imm = 0;
                                    numError++;
                                    error.add("IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                                  }

                                  tmpInst.getParams().add(tmpMem.getAddress() - imm);
                                  indPar = endPar + 1;
                                } catch (IrregularStringOfHexException ex) {
                                  //non ci dovrebbe mai arrivare
                                }
                              } else {
                                MemoryElement tmpMem1 = symTab.getCell(param.substring(cc + 1, endPar).trim());
                                tmpInst.getParams().add(tmpMem.getAddress() - tmpMem1.getAddress());
                              }
                            } else {
                              tmpMem = symTab.getCell(param.substring(indPar, endPar).trim());
                              tmpInst.getParams().add(tmpMem.getAddress());
                            }
                          }
                        } catch (MemoryElementNotFoundException ex) {
                          numError++;
                          error.add("INVALIDIMMEDIATE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                          i = line.length();
                          tmpInst.getParams().add(0);
                          continue;
                        }
                      }
                    } else if (syntax.charAt(z) == 'U') {  //Unsigned Immediate (5 bit)
                      int endPar;

                      if (z != syntax.length() - 1) {
                        endPar = param.indexOf(syntax.charAt(++z), indPar);
                      } else {
                        endPar = param.length();
                      }

                      if (endPar == -1) {
                        numError++;
                        error.add("SEPARATORMISS", row, indPar, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }

                      int imm;

                      if (isImmediate(param.substring(indPar, endPar))) {
                        if (param.charAt(indPar) == '#') {
                          indPar++;
                        }

                        if (isNumber(param.substring(indPar, endPar))) {
                          try {
                            imm = Integer.parseInt(param.substring(indPar, endPar).trim());

                            if (imm < 0) {
                              numError++;
                              error.add("VALUEISNOTUNSIGNED", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                              i = line.length();
                              tmpInst.getParams().add(0);
                              continue;
                            }

                            if (imm < 0 || imm > 31) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("5BIT_IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                          }

                          tmpInst.getParams().add(imm);
                          indPar = endPar + 1;
                        } else if (isHexNumber(param.substring(indPar, endPar).trim())) {
                          try {
                            imm = (int) Long.parseLong(Converter.hexToLong(param.substring(indPar, endPar)));

                            if (imm < 0) {
                              numError++;
                              error.add("VALUEISNOTUNSIGNED", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                              i = line.length();
                              tmpInst.getParams().add(0);
                              continue;
                            }

                            tmpInst.getParams().add(imm);
                            indPar = endPar + 1;

                            if (imm < 0 || imm > 31) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("5BIT_IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);

                            tmpInst.getParams().add(imm);
                            indPar = endPar + 1;

                          } catch (IrregularStringOfHexException ex) {
                            //non ci dovrebbe mai arrivare
                          }
                        }

                      } else {
                        numError++;
                        error.add("INVALIDIMMEDIATE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }
                    } else if (syntax.charAt(z) == 'C') {  //Unsigned Immediate (3 bit)
                      int endPar;

                      if (z != syntax.length() - 1) {
                        endPar = param.indexOf(syntax.charAt(++z), indPar);
                      } else {
                        endPar = param.length();
                      }

                      if (endPar == -1) {
                        numError++;
                        error.add("SEPARATORMISS", row, indPar, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }

                      int imm;

                      if (isImmediate(param.substring(indPar, endPar))) {
                        if (param.charAt(indPar) == '#') {
                          indPar++;
                        }

                        if (isNumber(param.substring(indPar, endPar))) {
                          try {
                            imm = Integer.parseInt(param.substring(indPar, endPar).trim());

                            if (imm < 0) {
                              numError++;
                              error.add("VALUEISNOTUNSIGNED", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                              i = line.length();
                              tmpInst.getParams().add(0);
                              continue;
                            }

                            if (imm < 0 || imm > 7) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("3BIT_IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                          }

                          tmpInst.getParams().add(imm);
                          indPar = endPar + 1;
                        } else if (isHexNumber(param.substring(indPar, endPar).trim())) {
                          try {
                            imm = (int) Long.parseLong(Converter.hexToLong(param.substring(indPar, endPar)));

                            if (imm < 0) {
                              numError++;
                              error.add("VALUEISNOTUNSIGNED", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                              i = line.length();
                              tmpInst.getParams().add(0);
                              continue;
                            }

                            tmpInst.getParams().add(imm);
                            indPar = endPar + 1;

                            if (imm < 0 || imm > 31) {
                              throw new NumberFormatException();
                            }
                          } catch (NumberFormatException ex) {
                            imm = 0;
                            numError++;
                            error.add("3BIT_IMMEDIATE_TOO_LARGE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);

                            tmpInst.getParams().add(imm);
                            indPar = endPar + 1;

                          } catch (IrregularStringOfHexException ex) {
                            //non ci dovrebbe mai arrivare
                          }
                        }

                      } else {
                        numError++;
                        error.add("INVALIDIMMEDIATE", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }
                    }

                    else if (syntax.charAt(z) == 'L') {  //Memory Label
                      int endPar;

                      if (z != syntax.length() - 1) {
                        endPar = param.indexOf(syntax.charAt(++z), indPar);
                      } else {
                        endPar = param.length();
                      }

                      if (endPar == -1) {
                        numError++;
                        error.add("SEPARATORMISS", row, indPar, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }

                      try {
                        MemoryElement tmpMem;

                        if (param.substring(indPar, endPar).equals("")) {
                          tmpInst.getParams().add(0);
                        } else if (isNumber(param.substring(indPar, endPar).trim())) {
                          int tmp = Integer.parseInt(param.substring(indPar, endPar).trim());

                          if (tmp < Memory.MIN_OFFSET_BYTES || tmp > Memory.MAX_OFFSET_BYTES) {
                            numError++;
                            String er = "LABELADDRESSINVALID";

                            error.add(er, row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                            i = line.length();
                            indPar = endPar + 1;
                            tmpInst.getParams().add(0);
                            continue;
                          }

                          tmpInst.getParams().add(tmp);
                        } else {
                          tmpMem = symTab.getCell(param.substring(indPar, endPar).trim());
                          tmpInst.getParams().add(tmpMem.getAddress());

                        }

                        indPar = endPar + 1;
                      } catch (MemoryElementNotFoundException e) {
                        numError++;
                        error.add("LABELNOTFOUND", row, line.indexOf(param.substring(indPar, endPar)) + 1, line);
                        i = line.length();
                        indPar = endPar + 1;
                        tmpInst.getParams().add(0);
                        continue;
                      }
                    } else if (syntax.charAt(z) == 'E') {  //Instruction Label
                      int endPar;

                      if (z != syntax.length() - 1) {
                        endPar = param.indexOf(syntax.charAt(++z), indPar);
                      } else {
                        endPar = param.length();
                      }

                      if (endPar == -1) {
                        numError++;
                        error.add("SEPARATORMISS", row, indPar, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }

                      String label = param.substring(indPar, endPar).trim();
                      Integer labelAddr = symTab.getInstructionAddress(label);
                      logger.info("Label " + label + " at address " + labelAddr);

                      if (labelAddr != null) {
                        tmpInst.getParams().add(labelAddr);
                      } else {
                        VoidJump tmpVoid = new VoidJump();
                        tmpVoid.instr = tmpInst;
                        tmpVoid.row = row;
                        tmpVoid.line = line;
                        tmpVoid.column = indPar;
                        tmpVoid.label = label;
                        voidJump.add(tmpVoid);
                        doPack = false;
                      }
                    } else if (syntax.charAt(z) == 'B') {  //Instruction Label for branch
                      int endPar;

                      if (z != syntax.length() - 1) {
                        endPar = param.indexOf(syntax.charAt(++z), indPar);
                      } else {
                        endPar = param.length();
                      }

                      if (endPar == -1) {
                        numError++;
                        error.add("SEPARATORMISS", row, indPar, line);
                        i = line.length();
                        tmpInst.getParams().add(0);
                        continue;
                      }

                      Integer labelAddr = symTab.getInstructionAddress(param.substring(indPar, endPar).trim());

                      if (labelAddr != null) {
                        labelAddr -= instrCount + 4;
                        tmpInst.getParams().add(labelAddr);
                      } else {
                        VoidJump tmpVoid = new VoidJump();
                        tmpVoid.instr = tmpInst;
                        tmpVoid.row = row;
                        tmpVoid.line = line;
                        tmpVoid.column = indPar;
                        tmpVoid.label = param.substring(indPar, endPar);
                        tmpVoid.instrCount = instrCount;
                        tmpVoid.isBranch = true;
                        voidJump.add(tmpVoid);
                        doPack = false;
                      }
                    }


                    else {
                      numError++;
                      error.add("UNKNOWNSYNTAX", row, 1, line);
                      i = line.length();
                      tmpInst.getParams().add(0);
                      continue;
                    }
                  } else {
                    if (syntax.charAt(z) != param.charAt(indPar++)) {
                      numError++;
                      error.add("UNKNOWNSYNTAX", row, 1, line);
                      i = line.length();
                      tmpInst.getParams().add(0);
                      continue;
                    }
                  }
                }

                if (i == line.length()) {
                  continue;
                }

                try {
                  if (doPack) {
                    tmpInst.pack();
                  }
                } catch (IrregularStringOfBitsException ex) {
                  logger.severe("Irregular string of bits: " + ex.getMessage());
                }
              } else {
                try {
                  tmpInst.pack();
                } catch (IrregularStringOfBitsException e) {
                  logger.severe("Irregular string of bits: " + e.getMessage());
                }
              }

              logger.info("row: " + line);
              String comment[] = line.split(";", 2);
              tmpInst.setFullName(replaceTab(comment[0].substring(i)));
              tmpInst.setFullName(replaceTab(comment[0].substring(i)));
              tmpInst.setFullName(replaceTab(comment[0].substring(i)));
              tmpInst.setFullName(replaceTab(comment[0].substring(i)));

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
                  error.add("OUTOFINSTRUCTIONMEMORY", row, i + 1, line);
                  i = line.length();
                  continue;
                }
              } catch (SameLabelsException ex) {
                numError++;
                error.add("SAMELABEL", row, 1, line);
                i = line.length();
              }
              // Il finally e' totalmente inutile, ma Ãš bello utilizzarlo per la
              // prima volta in un programma ;)
              finally {
                lastLabel = "";
              }

              end = line.length();
            }
          }

          i = end;
        } catch (MemoryElementNotFoundException ex) {
          if (isFirstOutOfMemory) { //is first out of memory?
            isFirstOutOfMemory = false;
            numError++;
            error.add("OUTOFMEMORY", row, i + 1, line);
            i = line.length();
            continue;
          }
        } catch (IrregularWriteOperationException ex) {
          numError++;
          error.add("INVALIDVALUE", row, i + 1, line);
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
        Instruction tmpInst = instructionBuilder.buildInstruction("SYSCALL");
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

  /** Clean multiple tab or spaces in a bad format String //and converts  this String to upper case
   *  @param s the bad format String
   *  @return the cleaned String
   */
  private String cleanFormat(String s) {
    if (s.length() > 0 && s.charAt(0) != ';' &&  s.charAt(0) != '\n') {
      //String[] nocomment=s.split(";");
      //s=nocomment[0];//.toUpperCase();
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

    return null;
  }

  /** Check if is a valid string for a register
   *  @param reg the string to validate
   *  @return -1 if reg isn't a valid register, else a number of register
   */
  private int isRegister(String reg) {

    try {
      int num;

      if (reg.charAt(0) == 'r' || reg.charAt(0) == 'R' || reg.charAt(0) == '$')    //ci sono altri modi di scrivere un registro???
        if (isNumber(reg.substring(1))) {
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
        if (isNumber(reg.substring(1))) {
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


  /** Check if the parameter is a valid string for an alias-register
   *  @param reg the string to validate
   *  @return -1 if reg isn't a valid alias-register, else a number of
  register
   */
  private int isAlias(String reg) {
    for (AliasRegister x : AliasRegister.values()) {
      if (reg.equalsIgnoreCase(x.name())) {
        return x.ordinal();
      }
    }

    return -1;
  }

  /** Check if a string is a number
   *  @param num the string to validate
   *  @return true if num is a number, else false
   */
  private boolean isNumber(String num) {
    if (num.charAt(0) == '+' || num.charAt(0) == '-') {
      num = num.substring(1);
    }

    for (int i = 0; i < num.length(); i++)
      if (num.charAt(i) < '0' || num.charAt(i) > '9') {
        return false;
      }

    return true;
  }
  /** Check if a string is a Hex number
   *  @param num the string to validate
   *  @return true if num is a number, else false
   */
  private boolean isHexNumber(String num) {
    try {
      if (num.substring(0, 2).compareToIgnoreCase("0X") != 0) {
        return false;
      }

      for (int i = 2; i < num.length(); i++)
        if ((num.charAt(i) < '0' || num.charAt(i) > '9') && (num.charAt(i) < 'A' || num.charAt(i) > 'F')) {
          return false;
        }

      return true;
    } catch (Exception e) {
      return false;
    }

  }

  /** Check if is a valid string for a register
   *  @param imm the string to validate
   *  @return false if imm isn't a valid immediate, else true
   */
  private boolean isImmediate(String imm) {
    try {
      if (imm.charAt(0) == '#') {
        imm = imm.substring(1);
      }

      if (isNumber(imm)) {
        return true;
      } else if (isHexNumber(imm)) {
        if (imm.length() <= 6) {
          return true;
        }
      }

      return false;
    } catch (Exception e) {
      return false;
    }

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

      if (isHexNumber(val)) {
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
