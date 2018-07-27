/* CurrentLocale.java
 *
 * This class gives the current locale settings.
 * (c) 2006
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
package org.edumips64.utils;

import java.util.*;
import java.util.logging.Logger;

/** This class has mostly static methods. If the 'config' attribute is set, then the current language is
   fetched from it. Otherwise, "en" is considered the default and used.
 */
public class CurrentLocale {

  private static Map<String, Map<String, String>> languages;
  private static ConfigStore config;

  public static void setConfig(ConfigStore config) {
    CurrentLocale.config = config;
  }

  private static final Logger logger = Logger.getLogger(CurrentLocale.class.getName());

  static {
    languages = new HashMap<>();

    // English messages.
    HashMap<String, String> en = new HashMap<>();
    en.put("DOUBLE_EXT_TOO_LARGE", "Exponent over 32 bits large");
    en.put("LABELADDRESSINVALID", "Invalid label");
    en.put("LABELTOOLARGE", "Label value too large ");
    en.put("FP_INVALID_OPERATION", "An invalid operation is performed");
    en.put("FP_DIV_BY_ZERO", "A division by zero is carried out");
    en.put("FP_OVERFLOW", "Floating point overflow, the number in absolute value is too large");
    en.put("FP_UNDERFLOW", "Floating point underflow, the number in absolute value is too small");
    en.put("BYTEINCODE", ".byte directive found in code section");
    en.put("WORD16INCODE", ".word16 directive found in code section");
    en.put("WORDINCODE", ".word directive found in code section");
    en.put("WORD32INCODE", ".word32 directive found in code section");
    en.put("SPACEINCODE", ".space directive found in code section");
    en.put("INVALIDVALUE", "Invalid value");
    en.put("INVALIDCODE", "Invalid code");
    en.put("INVALIDCODEFORDATA", "Invalid code for data");
    en.put("INVALIDREGISTER", "Invalid register");
    en.put("INVALIDIMMEDIATE", "Invalid Immediate value");
    en.put("UNKNOWNSYNTAX", "Unknown Syntax");
    en.put("SEPARATORMISS", "Separator is missing");
    en.put("LABELNOTFOUND", "Label not found");
    en.put("VALUEISNOTUNSIGNED", "Signed value: expected an unsigned value");
    en.put("GUI_STEP_ERROR", "CPU Step Error");
    en.put("GUI_PARSER_ERROR", "Parsing error");
    en.put("INCLUDE_LOOP", "loop of include");
    en.put("INT_FORMAT_EXCEPTION", "You must input an integer value");
    en.put("ERROR_LABEL", "Error accessing a memory element. Maybe you've reached the limit of EduMIPS64 memory.");
    en.put("ERROR", "Error");
    en.put("FILE_NOT_FOUND", "File not found");
    en.put("SYSCALL5_ERROR", "Error writing to standard output");
    en.put("Menu.FILE", "_File");
    en.put("Menu.EXECUTE", "E_xecute");
    en.put("Menu.CONFIGURE", "_Configure");
    en.put("Menu.WINDOW", "_Window");
    en.put("Menu.HELP", "_Help");
    en.put("Menu.CHANGE_LANGUAGE", "Change Language");
    en.put("Menu.TOOLS", "_Tools");
    en.put("Menu.CHANGE_PIPELINE_COLORS", "Change pipeline colors");
    en.put("MenuItem.OPEN", "_Open...");
    en.put("MenuItem.OPENLAST", "Open recent");
    en.put("MenuItem.RESET", "_Reset");
    en.put("MenuItem.EXIT", "E_xit");
    en.put("MenuItem.SINGLE_CYCLE", "Single Cycle");
    en.put("MenuItem.MULTI_CYCLE", "Multi-Cycle");
    en.put("MenuItem.MULTI_STEP", "Multi-Step");
    en.put("MenuItem.RUN_TO", "Run");
    en.put("MenuItem.ITALIAN", "Italian");
    en.put("MenuItem.ENGLISH", "English");
    en.put("MenuItem.DIN_TRACEFILE", "Write _Dinero tracefile...");
    en.put("MenuItem.FORWARDING", "Enable _forwarding");
    en.put("MenuItem.WARNINGS", "Enable _warnings");
    en.put("MenuItem.ABOUT_US", "_About us...");
    en.put("MenuItem.DIN_FRONTEND", "_Dinero frontend...");
    en.put("MenuItem.MANUAL", "_Manual...");
    en.put("MenuItem.STOP", "Stop");
    en.put("ABOUT", "About EduMIPS64");
    en.put("REGISTERS", "Registers");
    en.put("MEMORY", "Data");
    en.put("CODE", "Code");
    en.put("PIPELINE", "Pipeline");
    en.put("LOGGER", "Log");
    en.put("IO", "Input/Output");
    en.put("EXCEPTION", "Exception");
    en.put("FPUINFO", "Floating point unit");
    en.put("FPUFCSR", "FCSR register");
    en.put("ADDRESS", "Address");
    en.put("HEXREPR", "Representation");
    en.put("COMMENT", "Comment");
    en.put("LABEL", "Label");
    en.put("INSTRUCTION", "Instruction");
    en.put("STATS", "Statistics");
    en.put("PROSIM", "MIPS64 Processor Simulator");
    en.put("GUI_WARNING", "Warning");
    en.put("WINMIPS64_NOT_MIPS64", "The instruction belongs to WinMIPS64 instruction set, but it is not a legal MIPS64 instruction");
    en.put("HALT_NOT_PRESENT", "The HALT instruction is missing: it will automatically be inserted at the end of the file");
    en.put("ROW", "Row");
    en.put("COLUMN", "Column");
    en.put("IMMEDIATE_TOO_LARGE", "Immediate value too large");
    en.put("WORD_TOO_LARGE", "Word value too large");
    en.put("WORD32_TOO_LARGE", "Word32 value too large");
    en.put("WORD16_TOO_LARGE", "Word16 value too large");
    en.put("BYTE_TOO_LARGE", "Byte value too large");
    en.put("5BIT_IMMEDIATE_TOO_LARGE", "5 bit immediate value too large");
    en.put("3BIT_IMMEDIATE_TOO_LARGE", "3 bit immediate value too large");
    en.put("VALUE_MISS", "Missing value for memory cell");
    en.put("SAMELABEL", "Label already exists");
    en.put("CHOOSE_COLOR", "Select a color ");
    en.put("EXECUTION", "Execution");
    en.put("CYCLES", "Cycles");
    en.put("CYCLE", "Cycle");
    en.put("INSTRUCTIONS", "Instructions");
    en.put("CPI", "CPI (Cycles per instruction)");
    en.put("RAWS", "RAW Stalls");
    en.put("RAW", "RAW Stall");
    en.put("BYTES", "Bytes");
    en.put("STALLS", "Stalls");
    en.put("WAWS", "WAW Stalls");
    en.put("WARS", "WAR Stalls");
    en.put("STRUCTS_DIVNOTAVAILABLE", "Structural Stalls (Divider not available)");
    en.put("STRUCTS_MEMNOTAVAILABLE", "Structural Stalls (Memory not available)");
    en.put("BTS", "Branch Taken Stalls");
    en.put("BMS", "Branch Misprediction Stalls");
    en.put("CSIZE", "Code size");
    en.put("Config.APPEARANCE", "Appearance");
    en.put("Config.BEHAVIOR", "Behavior");
    en.put("Config.MAIN", "Main settings");
    en.put("Config.ITEM", "Settings...");
    en.put("Config.WARNINGS", "Enable Warnings");
    en.put("Config.WARNINGS.tip", "Enable Warnings in compile time");
    en.put("Config.FORWARDING", "Enable forwarding");
    en.put("Config.FORWARDING.tip", "Enables forwarding in the pipeline");
    en.put("Config.LONGDOUBLEVIEW", "Long/double mem.cells view");
    en.put("Config.LONGDOUBLEVIEW.tip", "Switchs between long and double visualisation of memory cells in the status bar");
    en.put("Config.VERBOSE", "Sync graphics with CPU in multi-step execution");
    en.put("Config.VERBOSE.tip", "Enables verbose multi-step execution");
    en.put("Config.N_STEP", "Number of step");
    en.put("Config.N_STEP.tip", "Number of step per istruction");
    en.put("Config.FPUEXCEPTIONS", "FPU Exceptions");
    en.put("Config.INVALID_OPERATION", "Invalid operation exception");
    en.put("Config.INVALID_OPERATION.tip", "Enables the floating point invalid operation exception");
    en.put("Config.OVERFLOW", "Overflow exception");
    en.put("Config.OVERFLOW.tip", "Enables the floating point overflow ");
    en.put("Config.UNDERFLOW", "Underflow exception");
    en.put("Config.UNDERFLOW.tip", "Enables the floating point underflow");
    en.put("Config.DIVIDE_BY_ZERO", "Divide by zero exception");
    en.put("Config.DIVIDE_BY_ZERO.tip", "Enables the floating point divide by zero exception");
    en.put("Config.FPUROUNDING", "FPU Rounding");
    en.put("Config.NEAREST", "To nearest");
    en.put("Config.NEAREST.tip", "Rounds the result to the nearest representable value, rounding to the even one when values are equally near");
    en.put("Config.TOWARDZERO", "Toward zero");
    en.put("Config.TOWARDZERO.tip", "Never increments the digit prior to a discarded fraction (i.e, truncates)");
    en.put("Config.TOWARDS_PLUS_INFINITY", "Towards plus infinity");
    en.put("Config.TOWARDS_PLUS_INFINITY.tip", "Round towards plus infinity");
    en.put("Config.TOWARDS_MINUS_INFINITY", "Towards minus infinity");
    en.put("Config.TOWARDS_MINUS_INFINITY.tip", "Round towards minus infinity");
    en.put("Config.IFCOLOR", "Fetch color");
    en.put("Config.IFCOLOR.tip", "Sets the IF color");
    en.put("Config.IDCOLOR", "ID color");
    en.put("Config.IDCOLOR.tip", "Sets Decode color");
    en.put("Config.EXCOLOR", "EX Color");
    en.put("Config.EXCOLOR.tip", "Sets the Execute color");
    en.put("Config.MEMCOLOR", "MEM color");
    en.put("Config.MEMCOLOR.tip", "Sets the Memory color");
    en.put("Config.WBCOLOR", "WB Color");
    en.put("Config.WBCOLOR.tip", "Sets the Write Back color");
    en.put("Config.FPADDERCOLOR", "FP adder color");
    en.put("Config.FPADDERCOLOR.tip", "Sets the FP adder color");
    en.put("Config.FPMULTIPLIERCOLOR", "FP multiplier color");
    en.put("Config.FPMULTIPLIERCOLOR.tip", "Sets the FP multiplier color");
    en.put("Config.FPDIVIDERCOLOR", "FP divider color");
    en.put("Config.FPDIVIDERCOLOR.tip", "Sets the FP divider color");
    en.put("Config.SLEEP_INTERVAL", "Interval between cycles (ms)");
    en.put("Config.SLEEP_INTERVAL.tip", "Amount of milliseconds that has to pass before each cycle is executed in verbose mode");
    en.put("Config.SYNCEXC-MASKED", "Mask synchronous exception");
    en.put("Config.SYNCEXC-MASKED.tip", "Makes the simulator ignore the Division by zero and Integer Overflow exceptions");
    en.put("Config.SYNCEXC-TERMINATE", "Terminate on synchronous exceptions");
    en.put("Config.SYNCEXC-TERMINATE.tip", "Halt the simulation on Division by zero and Integer Overflow exceptions");
    en.put("Config.FONTSIZE", "Font size");
    en.put("Config.FONTSIZE.tip", "Size of the font");
    en.put("StatusBar.WELCOME", "Welcome to EduMIPS64");
    en.put("StatusBar.DECIMALVALUE", "Decimal value");
    en.put("StatusBar.OFREGISTER", "of R");
    en.put("StatusBar.OFREGISTERFP", "of F");
    en.put("StatusBar.MEMORYCELL", "of the memory cell at address");
    en.put("Manual.CAPTION", "User Guide");
    en.put("Manual.INTRO", "Introduction");
    en.put("Manual.GUI", "GUI");
    en.put("Manual.IS", "Instructions");
    en.put("Manual.SYSCALL", "Syscall");
    en.put("ErrorDialog.ROW", "Row");
    en.put("ErrorDialog.COLUMN", "Column");
    en.put("ErrorDialog.LINE", "Line");
    en.put("ErrorDialog.DESCRIPTION", "Description");
    en.put("ErrorDialog.MSG0", "Code contains");
    en.put("ErrorDialog.MSG1", "errors and");
    en.put("ErrorDialog.MSG2", String.valueOf(ConfigKey.WARNINGS));
    en.put("ReportDialog.MSG", "EduMIPS64 Fatal error! Please help the developers, by opening a new issue on GitHub (https://github.com/lupino3/edumips64/issues/new) with the following text, or by sending it via email to bugs@edumips.org");
    en.put("ReportDialog.BUTTON", "Close");
    en.put("DIVZERO.Message", "Division by zero");
    en.put("INTOVERFLOW.Message", "Integer overflow");
    en.put("FPOVERFLOW.Message", "FP overflow");
    en.put("FPUNDERFLOW.Message", "FP underflow");
    en.put("FPINVALID.Message", "FP invalid operation");
    en.put("FPDIVBYZERO.Message", "FP division by zero");
    en.put("NOOPENMODE", "No mode has been specified to open the file (read/write)");
    en.put("WRITETOSTDIN", "Attempt to write to standard input");
    en.put("FILENOTOPENED", "The file descriptor isn't valid. Probably the file hasn't been correctly opened");
    en.put("OUTOFMEMORY", "The write (or read) attempt led to an invalid memory access");
    en.put("OUTOFINSTRUCTIONMEMORY", "Instruction memory exhausted");
    en.put("READFROMSTDOUT", "Attempt to read from standard output (or standard error)");
    en.put("OPENREADANDCREATE", "Attempt to open in read mode a file that doesn't exist (O_CREAT doesn't work in read or read/write mode)");
    en.put("INPUTNOTEXCEED", "Input must not exceed");
    en.put("CHARACTERS", "characters");
    en.put("ENTERINPUT", "Please input your text");
    en.put("INPUT", "input");
    en.put("IOEXCEPTION", "I/O error");
    en.put("HT.Options", "Options:");
    en.put("HT.File", "--file (-f) filename		opens the specified file");
    en.put("HT.Debug", "--debug (-d) 			activates debug mode");
    en.put("HT.Help", "--help (-h)			prints this help message");
    en.put("HT.Reset", "--reset (-r)			resets the stored preferences");
    en.put("HT.Version", "--version (-v)			prints the version");
    en.put("HT.MissingFile", "Error: File name is missing.");
    en.put("HT.UnrecognizedArgs", "Error: Unrecognized argument");
    en.put("HT.MultipleFile", "Error: -f may be used only once.");
    en.put("CLEAR", "Clear");
    en.put("DATA", "Data");
    en.put("HELPDIR", "/docs/user/en");
    en.put("NEGADDRERR", "Negative memory address error in instruction");
    en.put("ALIGNERR", "Alignment error in instruction");
    en.put("THEADDRESS", "the address");
    en.put("ISNOTALIGNED", "is not aligned to");
    en.put("RESTART_FONT", "Please restart the simulator to use the new font.");
    en.put("NO_MASK_AND_TERMINATE", "Please choose only one option between masking synchronous exceptions and program termination on synchronous exceptions.");
    languages.put("en", en);

    // Italian messages.
    HashMap<String, String> it = new HashMap<>();
    it.put("DOUBLE_EXT_TOO_LARGE", "Esponente oltre i 32 bit");
    it.put("LABELTOOLARGE", "Numero troppo grande per una label");
    it.put("MEMORYADDRESSINVALID", "Etichetta invalida, deve essere allineata a 64 bit");
    it.put("FP_INVALID_OPERATION", "È stata eseguita un'operazione non valida");
    it.put("FP_DIV_BY_ZERO", "È stata effettuata una divisione per zero");
    it.put("FP_OVERFLOW", "Floating point overflow, il numero in valore assoluto è troppo grande");
    it.put("FP_UNDERFLOW", "Floating point underflow, il numero in valore assoluto è troppo piccolo");
    it.put("BYTEINCODE", "Direttiva .byte trovata nella sezione codice");
    it.put("WORD16INCODE", "Direttiva .word16 trovata nella sezione codice");
    it.put("WORDINCODE", "Direttiva .word trovata nella sezione codice");
    it.put("WORD32INCODE", "Direttiva .word32 trovata nella sezione codice");
    it.put("SPACEINCODE", "Direttiva .space trovata nella sezione codice");
    it.put("INVALIDVALUE", "Valore non valido ");
    it.put("INVALIDCODE", "Codice non valido");
    it.put("INVALIDCODEFORDATA", "Codice non valido per il data");
    it.put("INVALIDREGISTER", "Registro non valido");
    it.put("INVALIDIMMEDIATE", "Valore Immediato invalido");
    it.put("UNKNOWNSYNTAX", "Sintassi sconosciuta");
    it.put("SEPARATORMISS", "Mancano i separatori");
    it.put("LABELNOTFOUND", "Etichetta non trovata");
    it.put("VALUEISNOTUNSIGNED", "Valore con segno: atteso valore senza segno");
    it.put("GUI_STEP_ERROR", "Errore durante l'esecuzione di uno step di simulazione");
    it.put("GUI_PARSER_ERROR", "Errore in fase di parsing");
    it.put("INT_FORMAT_EXCEPTION", "Inserire un valore intero");
    it.put("INCLUDE_LOOP", "Ciclo di include");
    it.put("ERROR_LABEL", "Errore durante l'accesso alla memoria. Probabilmente è stato raggiunto il limite della memoria di EduMIPS64");
    it.put("ERROR", "Errore");
    it.put("FILE_NOT_FOUND", "File non trovato");
    it.put("SYSCALL5_ERROR", "Errore nella scrittura su standard output");
    it.put("Menu.FILE", "_File");
    it.put("Menu.EXECUTE", "E_secuzione");
    it.put("Menu.CONFIGURE", "_Configura");
    it.put("Menu.WINDOW", "Fines_tra");
    it.put("Menu.HELP", "_Aiuto");
    it.put("Menu.CHANGE_LANGUAGE", "Selezione Lingua");
    it.put("Menu.TOOLS", "_Strumenti");
    it.put("Menu.CHANGE_PIPELINE_COLORS", "Cambia colori pipeline");
    it.put("MenuItem.OPEN", "_Apri...");
    it.put("MenuItem.OPENLAST", "Apri recente ");
    it.put("MenuItem.RESET", "_Resetta");
    it.put("MenuItem.EXIT", "E_sci");
    it.put("MenuItem.SINGLE_CYCLE", "Ciclo Singolo");
    it.put("MenuItem.MULTI_CYCLE", "Cicli Multipli");
    it.put("MenuItem.MULTI_STEP", "Passi Multipli");
    it.put("MenuItem.RUN_TO", "Esegui");
    it.put("MenuItem.ITALIAN", "Italiano");
    it.put("MenuItem.ENGLISH", "Inglese");
    it.put("MenuItem.DIN_TRACEFILE", "Scrivi tracefile _Dinero...");
    it.put("MenuItem.FORWARDING", "Abilita _forwarding");
    it.put("MenuItem.WARNINGS", "Abilita _warning");
    it.put("MenuItem.ABOUT_US", "_Informazioni su...");
    it.put("MenuItem.DIN_FRONTEND", "_Dinero frontend...");
    it.put("MenuItem.MANUAL", "_Manuale...");
    it.put("MenuItem.STOP", "Ferma");
    it.put("ABOUT", "Informazioni su EduMIPS64");
    it.put("REGISTERS", "Registri");
    it.put("MEMORY", "Memoria");
    it.put("CODE", "Codice");
    it.put("CYCLES", "Cicli");
    it.put("PIPELINE", "Pipeline");
    it.put("LOGGER", "Log");
    it.put("IO", "Input/Output");
    it.put("EXCEPTION", "Eccezione");
    it.put("FPUINFO", "Unità floating point");
    it.put("FPUFCSR", "Registro FCSR");
    it.put("ADDRESS", "Indirizzo");
    it.put("HEXREPR", "Rappresentazione");
    it.put("COMMENT", "Commento");
    it.put("LABEL", "Etichetta");
    it.put("STATS", "Statistiche");
    it.put("PROSIM", "Simulatore di processore MIPS64");
    it.put("GUI_WARNING", "Attenzione");
    it.put("WINMIPS64_NOT_MIPS64", "L'istruzione appartiene all'instruction set di WinMIPS64, ma non è un'istruzione MIPS64 legale.");
    it.put("HALT_NOT_PRESENT", "Manca l'istruzione HALT: sarà aggiunta automaticamente alla fine del file.");
    it.put("ROW", "Riga");
    it.put("COLUMN", "Colonna");
    it.put("IMMEDIATE_TOO_LARGE", "Numero troppo grande per un campo immediato");
    it.put("WORD_TOO_LARGE", "Numero troppo grande per un campo Word");
    it.put("WORD32_TOO_LARGE", "Numero troppo grande per un campo Word32");
    it.put("WORD16_TOO_LARGE", "Numero troppo grande per un campo Word16");
    it.put("BYTE_TOO_LARGE", "Numero troppo grande per un campo Byte");
    it.put("5BIT_IMMEDIATE_TOO_LARGE", "Numero troppo grande per un campo immediato a 5 bit");
    it.put("3BIT_IMMEDIATE_TOO_LARGE", "Numero troppo grande per un campo immediato a 3 bit");
    it.put("VALUE_MISS", "Manca il valore da assegnare alla cella di memoria");
    it.put("SAMELABEL", "Etichetta già esistente");
    it.put("CHOOSE_COLOR", "Seleziona un colore ");
    it.put("EXECUTION", "Esecuzione");
    it.put("CYCLE", "Ciclo");
    it.put("INSTRUCTIONS", "Istruzioni");
    it.put("INSTRUCTION", "Istruzione");
    it.put("CPI", "CPI (Cicli per istruzione)");
    it.put("RAWS", "Stalli RAW");
    it.put("RAW", "Stallo RAW");
    it.put("BYTES", "Bytes");
    it.put("STALLS", "Stalli");
    it.put("WAWS", "Stalli WAW");
    it.put("WARS", "Stalli WAR");
    it.put("STRUCTS_DIVNOTAVAILABLE", "Stalli strutturali (Divisore non disponibile)");
    it.put("STRUCTS_MEMNOTAVAILABLE", "Stalli strutturali (Memoria non disponibile)");
    it.put("BTS", "Stalli 'Branch Taken'");
    it.put("BMS", "Stalli 'Branch Misprediction'");
    it.put("CSIZE", "Dimensione del codice");
    it.put("Config.APPEARANCE", "Aspetto");
    it.put("Config.BEHAVIOR", "Comportamento");
    it.put("Config.MAIN", "Impost. generali");
    it.put("Config.ITEM", "Impostazioni...");
    it.put("Config.WARNINGS", "Abilita Avvisi");
    it.put("Config.WARNINGS.tip", "Abilita avvisi in fase di compilazione");
    it.put("Config.FORWARDING", "Abilita forwarding");
    it.put("Config.FORWARDING.tip", "Abilita l'opzione forwarding");
    it.put("Config.LONGDOUBLEVIEW", "Visualizza mem. Long/double");
    it.put("Config.LONGDOUBLEVIEW.tip", "Visualizza le celle di memoria come valori long o double nella barra di stato");
    it.put("Config.VERBOSE", "Sincronizza la GUI con la CPU nell'esecuzione multi-step");
    it.put("Config.VERBOSE.tip", "Abilita la sincronizzazione tra la grafica e la CPU nel multi-step");
    it.put("Config.N_STEP", "Numero di Step");
    it.put("Config.N_STEP.tip", "Il numero di step per istruzione");
    it.put("Config.FPUEXCEPTIONS", "Eccezioni FPU");
    it.put("Config.INVALID_OPERATION", "Eccezione Invalid operation ");
    it.put("Config.INVALID_OPERATION.tip", "Abilita l'eccezione Invalid operation della FPU");
    it.put("Config.OVERFLOW", "Eccezione Overflow ");
    it.put("Config.OVERFLOW.tip", "Abilita l'eccezione Overflow della FPU");
    it.put("Config.UNDERFLOW", "Eccezione Underflow");
    it.put("Config.UNDERFLOW.tip", "Abilita l'eccezione Underflow della FPU");
    it.put("Config.DIVIDE_BY_ZERO", "Eccezione Divide by zero");
    it.put("Config.DIVIDE_BY_ZERO.tip", "Abilita l'eccezione Divide by zero della FPU");
    it.put("Config.FPUROUNDING", "Arrot.FPU");
    it.put("Config.NEAREST", "Al più vicino");
    it.put("Config.NEAREST.tip", "Arrotonda al più vicino valore rappresentabile, arrotondando a quello pari se i valori sono ugualmente vicini");
    it.put("Config.TOWARDZERO", "Verso lo zero");
    it.put("Config.TOWARDZERO.tip", "Non incrementa mai la cifra intera prima della virgola (Troncamento)");
    it.put("Config.TOWARDS_PLUS_INFINITY", "Verso +Infinito");
    it.put("Config.TOWARDS_PLUS_INFINITY.tip", "Arrotonda verso l'infinito positivo");
    it.put("Config.TOWARDS_MINUS_INFINITY", "Verso -Infinito");
    it.put("Config.TOWARDS_MINUS_INFINITY.tip", "Arrotonda verso l'infinito negativo");
    it.put("Config.IFCOLOR", "Colore IF");
    it.put("Config.IFCOLOR.tip", "Imposta il colore della fase di Fetch dell'istruzione");
    it.put("Config.IDCOLOR", "Colore ID");
    it.put("Config.IDCOLOR.tip", "Imposta il colore della fase di Decode dell'istruzione");
    it.put("Config.EXCOLOR", "Colore EX");
    it.put("Config.EXCOLOR.tip", "Imposta il colore della fase di Esecuzione dell'istruzione");
    it.put("Config.MEMCOLOR", "Colore MEM");
    it.put("Config.MEMCOLOR.tip", "Imposta il colore della fase di Memoria dell'istruzione");
    it.put("Config.WBCOLOR", "Colore WB");
    it.put("Config.WBCOLOR.tip", "Imposta il colore della fase di Write Back dell'istruzione");
    it.put("Config.FPADDERCOLOR", "Colore addizionatore FP");
    it.put("Config.FPADDERCOLOR.tip", "Imposta il colore delle fasi dell'addizionatore FP ");
    it.put("Config.FPMULTIPLIERCOLOR", "Colore moltiplicatore FP");
    it.put("Config.FPMULTIPLIERCOLOR.tip", "Imposta il colore delle fasi del moltiplicatore FP");
    it.put("Config.FPDIVIDERCOLOR", "Colore divisore FP");
    it.put("Config.FPDIVIDERCOLOR.tip", "Imposta il colore delle fasi del divisore FP");
    it.put("Config.SLEEP_INTERVAL", "Intervallo tra i cicli (ms)");
    it.put("Config.SLEEP_INTERVAL.tip", "Numero di millisecondi che devono passare prima che ciascun ciclo sia eseguito in modalitÃ  verbose");
    it.put("Config.SYNCEXC-MASKED", "Maschera eccezioni sincrone");
    it.put("Config.SYNCEXC-MASKED.tip", "Fa sì che il simulatore ignori le eccezioni Divisione per zero ed Integer overflow");
    it.put("Config.SYNCEXC-TERMINATE", "Termina se si verifica un'eccezione sincrona");
    it.put("Config.SYNCEXC-TERMINATE.tip", "Ferma la simulazione al verificarsi di eccezioni di tipo Divisione per zero ed Integer overflow");
    it.put("Config.FONTSIZE", "Dimensione font");
    it.put("Config.FONTSIZE.tip", "Dimensione del font");
    it.put("StatusBar.WELCOME", "Benvenuti in EduMIPS64");
    it.put("StatusBar.DECIMALVALUE", "Valore decimale");
    it.put("StatusBar.OFREGISTER", "di R");
    it.put("StatusBar.OFREGISTERFP", "di F");
    it.put("StatusBar.MEMORYCELL", "della cella di memoria all'indirizzo");
    it.put("Manual.CAPTION", "Manuale utente");
    it.put("Manual.INTRO", "Introduzione");
    it.put("Manual.GUI", "GUI");
    it.put("Manual.IS", "Istruzioni");
    it.put("Manual.SYSCALL", "Syscall");
    it.put("ErrorDialog.ROW", "Riga");
    it.put("ErrorDialog.COLUMN", "Colonna");
    it.put("ErrorDialog.LINE", "Linea");
    it.put("ErrorDialog.DESCRIPTION", "Descrizione");
    it.put("ErrorDialog.MSG0", "Il codice contiene");
    it.put("ErrorDialog.MSG1", "errori e");
    it.put("ErrorDialog.MSG2", "avvisi");
    it.put("ReportDialog.MSG", "Errore fatale! Aiuta gli sviluppatori, aprendo una issue su GitHub(https://github.com/lupino3/edumips64/issues/new) con il seguente testo, o inviandolo via email a bugs@edumips.org");
    it.put("ReportDialog.BUTTON", "Chiudi");
    it.put("DIVZERO.Message", "Divisione per zero");
    it.put("INTOVERFLOW.Message", "Integer overflow");
    it.put("FPOVERFLOW.Message", "FP Overflow");
    it.put("FPUNDERFLOW.Message", "FP Underflow");
    it.put("FPINVALID.Message", "Operazione non valida FPU");
    it.put("FPDIVBYZERO.Message", "Divisione per zero FPU");
    it.put("NOOPENMODE", "Non Ã¨ stata specificata la modalitÃ  di apertura del file (lettura/scrittura)");
    it.put("WRITETOSTDIN", "Tentativo di scrittura sullo standard input");
    it.put("FILENOTOPENED", "Il descrittore di file non è valido. Probabilmente il file non è stato aperto correttamente");
    it.put("OUTOFMEMORY", "Il tentativo di lettura (o scrittura) ha condotto ad un accesso in memoria non valido");
    it.put("OUTOFINSTRUCTIONMEMORY", "Esaurita memoria istruzioni");
    it.put("READFROMSTDOUT", "Tentativo di lettura dallo standard output (o dallo standard error)");
    it.put("OPENREADANDCREATE", "Tentativo di apertura in modalità lettura di un file che non esiste (O_CREAT non funziona in modalità lettura o lettura/scrittura)");
    it.put("INPUTNOTEXCEED", "La dimensione dell'input non deve superare");
    it.put("CHARACTERS", "caratteri");
    it.put("ENTERINPUT", "Inserire il testo");
    it.put("INPUT", "input");
    it.put("IOEXCEPTION", "Errore di I/O");
    it.put("HT.Options", "Opzioni:");
    it.put("HT.File", "--file (-f) file		apre il file specificato");
    it.put("HT.Debug", "--debug (-d) 			attiva la modalità di debug");
    it.put("HT.Help", "--help (-h)			stampa questo messaggio");
    it.put("HT.Reset", "--reset (-r)			elimina le preferenze memorizzate");
    it.put("HT.Version", "--version (-v)			stampa la versione");
    it.put("HT.MissingFile", "Errore: file non specificato");
    it.put("HT.UnrecognizedArgs", "Errore: parametro non specificato");
    it.put("HT.MultipleFile", "Errore: -f può essere usato una sola volta");
    it.put("CLEAR", "Pulisci");
    it.put("DATA", "Dati");
    it.put("HELPDIR", "/docs/user/it");
    it.put("NEGADDRERR", "Tentativo di accesso ad indirizzo di memoria negativo nell'istruzione");
    it.put("ALIGNERR", "Errore di allineamento nell'istruzione");
    it.put("THEADDRESS", "l'indirizzo");
    it.put("ISNOTALIGNED", "non è allineato a");
    it.put("RESTART_FONT", "E' necessario riavviare il simulatore per utilizzare il nuovo font.");
    it.put("NO_MASK_AND_TERMINATE", "Selezionare solo una opzione tra mascheramento eccezioni sincrone e terminazione in seguito ad eccezioni sincrone.");
    languages.put("it", it);
  }

  public static String getString(String key) {
    String lang_name = "en";
    if (config != null) {
      lang_name = config.getString(ConfigKey.LANGUAGE);
    }

    try {
      Map<String, String> lang = languages.get(lang_name);
      return lang.get(key);
    } catch (Exception e) {
      logger.severe("Could not look up key " + key + " in language " + lang_name);
      return key;
    }
  }
}
