# EduMIPS64 ChangeLog

## 1.2.2 (10/06/2016) - Contrada Fumata
### Fixed
- JR does not respect RAW stalls (Issue #68)

## 1.2.1 (21/05/2016) - IFSC
### Fixed
- Ghost instruction fetched at the end of program (Issue #50)
- Problem with syscall 0 after branch (Issue #51)
- Movn and movz problem after branches (Issue #63)
- Limited number of labels (Issue #64)
- Several issues related to code quality (Issues #41, #42, plus several other
  small problems not listed in the issue tracker)

## 1.2 (01/06/2013) - Ringsend
### Added
- Build JAR for every master push and make it available at a known location
  (Issue #30)
### Fixed
- Settings window's buttons are often hidden (Issue #38)
- StringIndexOutOfBoundsException raised at run-time (Issue #36)
- The Dinero frontend sometimes hangs while running Dinero under Windows
  (Issue #35)
- Consistency issues between the Settings window and the actual options' value
  (Issue #34)
- Missing Italian strings for --help (Issue #33)
- Splash screen not working for "slim" jar (Issue #31)
- Dinero Frontend does not support paths containing spaces for the DineroIV
  executable (Issue #29)
- Dinero tracefile tracks both Load and Store memory accesses as read (Issue
  #28)
- Refactor sphinx conf.py code internal-cleanup (Issue #18)
- Automatically populate version fields throughout the code/docs (Issue #17)

## 1.1 (16/03/2013) - Charleroi
### Added
- New configuration preference storage system: cross-platform and based on a
  standard Java feature (Issue #27) (andrea).
- New Ant targets to build JAR files with no embedded libraries, useful for
  distro packagers (andrea)
### Fixed:
- Issue #9 (Handle the JavaHelp dependency in a better way) (andrea)
- Issue #13 (Fix src-release ant target) (andrea)
- Issue #20 (Forwarding error with a H&P example) (andrea)
- Issue #21 (Add contact information in the first page of the help) (andrea)
- Issue #24 (Mention github issues link in the bugs report dialog) (andrea)
- Issue #25 (Resource mechanism doesn't work when the app is not inside a JAR
  file) (andrea)
- GUICycles window was not behaving correctly when the same instruction was
  present multiple times in the pipeline. (andrea)
- General code clean-up:
  - added several unit tests, including some for the forwarding feature;
  - source code moved to the org.edumips64 package;
  - better source code organization, with directories that match the package
    hierarchy;
  - fixed the style, by using an external linter;
  - enabled compilation warnings and fixed all of them;
  - refactored the GUICycle code;
  - converted everything that needed conversion to UTF-8.

## 1.0 (24/11/2012) - Philadelphia
### Added
- Floating Point Unit support (developed by max83t)
### Fixed
- Issue #14 (ant javadoc not working)
- Issue #15 (Make debug mode change verbosity)

## 0.5.4 (28/02/2012)
### Fixed
- Issue #2 (Misaligned memory operations are not handled correctly)
- Issue #10 (Random freeze when executing SYSCALL 5 under Java 7)
- added unit testing code; added some unit tests
- added regression tests for issues #2 and #7.

## 0.5.3 (19/09/2011)
### Added
- Embedded JavaHelp viewer (andrea)
- Converted the TeX docs to RST, so that the same sources can be used for
  the PDF manual and for the newly-embedded JavaHelp viewer (andrea)
- Added Splash Screen (visible with Java6+, not shown with Java5) (andrea,
  vanni)
- Added labels to the GUIData frame; fixed GUIData and GUICode default column
  widths so that they can be read (andrea)
### Fixed
- bugs #8 (Weird GUI under Mac OS X) and #9 (Incorrect behavior of
  MOVN and MOVZ)
- Added GUIData/GUICode column labels update on language change (andrea)
- Fixed a bug that disallowed access to odd memory locations from the code
  (odd labels were not correctly parsed).
- Changed logging mechanism to use java.util.logging (andrea)
- Removed GUI Log viewer, the debug messages now go to stdout (andrea)

## 0.5.2 (17/04/2008)
### Fixed
- stall for synchronous exceptions and forwarding (max83t)
- Java 6 Swing bug (andrea)
- ReportDialog bug (mancausoft)
- aligned read/write with labels (andrea)
- MIPS32 alias visualization (thegoodgiant)

## 0.5.1 (24/08/2007)
### Added
- Updated embedded manual with a SYSCALL page (jesky)
- Added the MIPS alias to each register's name in the Registers window (andrea)
- Added debug info to svnjar (mancausoft)
### Fixed
- Parser bug fixes (mancausoft)
- Memory bug fixes (mancausoft)
- Fixed MOVN/MOVZ behavior (andrea)
- Manual fixes (jesky)
- Fixed Jump-after-Break bug (andrea)
- Fixed OSX issues (mancausoft)

## 0.5 (05/06/2007)
### Added
- Implemented a subset of MIPS32 instructions:
  * ADD, ADDI, ADDIU, ADDU
  * DIV, DIVU
  * MULT, MULTU
  * SLL, SLLV, SRA, SRAV, SRL, SRLV
  * SUB, SUBU
- Added LO and HI registers visualization in GUI (max83t)
- More graceful error window for erroneous memory access (andrea)
- Bug fixes for SYSCALL 1 and 5 (andrea)
- Updated manual (MIPS32 group, andrea)
- Clear button for the I/O window (andrea)
- Changed default interval between cycles from 100ms to 10ms (andrea)
### Fixed
- Fixed compilation under Java 6 (andrea)
- Bug fix in the locale files (andrea)
- Bug fixes for the parser (max83t, mancausoft)
- Bug fixes for the Error Dialog (mancausoft)
- Bug fix for negative labels in load/store instructions (mancausoft)
- DSRAV fix (MIPS32 group)

## 0.4.2 (22/04/2007)
### Added
- Added DADDIU instruction, and marked DADDUI as deprecated (mancausoft)
- New instructions: B, BGEZ, DMULTU, DDIVU (mancausoft)
- Standard MIPS aliases for the first 32 registers (thegoodgiant, jesky,
  lorenzo)
- Updated all the manuals (andrea)
### Changed
- Changed the default Dinero Tracefile output filename (andrea)
### Fixed
- Fixed a parsing bug with big values (mancausoft)
- Fixed a bug regarding SynchronousException (mancausoft)
- Bug fixes for the manuals (andrea)
- Solved a bug that happened opening files (andrea)
- Fixed DMULT in order to use HI and LO registers (mancausoft)
- Solved a bug in the CPUGUIThread class that made the graphics not synced
  with the CPU status at the end of the execution (andrea)
- Fixed the availability of some menus during the execution (andrea)
- Fixed a bug that didn't allow the Dinero Tracefile to be written (andrea)

## 0.4.1 (31/03/2007)
### Fixed
- Corrected a bug in the LUI instruction (mancausoft)
- Corrected a bug in the manual regarding the LUI instruction (andrea)

## 0.4 (18/03/2007)
### Added
- New PDF manual (andrea)
- Italian translation of the PDF manual (simona)
- #include command (mancausoft)
- .word64 directive (andrea)
### Fixed
- Fixed a bug in SYSCALL 5. (andrea)

## 0.3.6 (10/10/2006)
### Added
- Added the ability to mask synchronous exceptions and to make those
  exceptions stop the CPU (andrea)
- Appended three dots to those menu items that open new dialogs, according
  to a widespread convention (andrea)
### Changed
- Removed an useless debug print in the BREAK instruction (andrea)
- Changed the "Run to" text to "Run", the "Completa" text to "Esegui". and
  the "Esegui" menu text to "Esecuzione (andrea)

## 0.3.5.2 (17/09/2006)
### Fixed
- Fixed applet behaviour. (andrea)
- Fixed Parser recognizing of TRAP 0 and SYSCALL 0 (andrea)

## 0.3.5.1 (17/09/2006)
### Added
- TRAP 0 is recognized as the end of the program, like SYSCALL 0 and HALT (andrea)
### Fixed
- Fixed a bug in labels handling: now only case insensitive labels are
  accepted (andrea)
- Fixed a bug in BitSet64 regarding bit alignment in unsigned bytes writing (andrea)

## 0.3.5 (17/09/2006)
### Fixed
- Fixed the Memory and SymbolTable interfaces. Now all memory accesses via
  labels are done through the SymbolTable, and memory accesses via address
  are done through Memory.
  This object contains instructions and memory elements.
  I've removed all the old get() and set() methods that accepted an index in
  SymbolTable and MemoryElement. Now every access must be done using the
  address as a parameter. (andrea)
- Fixed the "package" ant task, that builds automagically a .jar and a
  .tar.bz2 file from the edumips64 directory (andrea)

## 0.3.4 (17/09/2006)
### Added
- command line options (andrea):
  -h (--help)         shows a little help
  -f (--file) filename    opens a new file
  -d (--debug)         enters debug mode
- SYSCALL 1, 2, 3, 4 implemented!
  They are the EduMIPS64 equivalent of the open(), close(), read() and write()
  POSIX syscalls. More documentation will follow (andrea).
- Makefile updated with the I/O icon (mancausoft)
- The parser now recognizes SYSCALL 0 as HALT, and it doesn't complain if a
  SYSCALL 0 closes the program (mancausoft)
### Changed
- The HALT instruction is added to the list of deprecated instruction (mancausoft)

## 0.3.3 (09/09/2006)
### Added
- The SYSCALL instruction is born! Right now only SYSCALL 0 and 5 work:
  . SYSCALL 0: acts exactly like the HALT instruction. [exit()]
  . SYSCALL 5: roughly acts like printf(), supporting only the following
  placeholders: %s (string), %d (integer), %i (same as %d), %% (literal %)
  SYSCALL works like WinDLX's SYSCALL: it expects in R14 the address of its
  parameters and places the return value in R1. SYSCALL 0 doesn't return
  anything, but SYSCALL 5 returns the number of bytes written to STDOUT.
  SYSCALL 5 is Dinero-friendly: it records every memory access it does in
  the tracefile. More documentation will follow. (andrea)
- New TRAP instruction, alias of SYSCALL, added for the sake of
  compatibility with WinDLX. (andrea)
- .ascii and .asciiz parser directives, used to save strings in memory.
  .ascii saves it in memory "as is", while .asciiz automatically adds a null
  byte at the end of the string. The backslash character is used to escape
  special characters (\n, \t, \0, \"). Here's the syntax:
  .ascii "string1"[, "string2"[, "stringN"]]
- Added the BREAK instruction, that stops the execution as soon as it enters
  in the ID stage. (andrea)
- Basic I/O window, that automagically pops up when something is written to STDOUT (andrea)
- Made the Settings menu item gray when the CPU is running a program, to
  prevent race conditions on the configuration settings (andrea).
- Added the version number to the statusbar welcome message (andrea)
- Added the right locale entries for the log window's title. (andrea)
- The .space instruction now accepts an hexadecimal parameter too. (andrea)
- Parser code improvements and cleanups (mancausoft + andrea)
### Fixed
- Corrected a bug that made fatal errors send the CPU in an infinite loop.
  At least, now we can read the log when there's a fatal error. (andrea)
- Solved a little bug that caused the cycles number not to reset correctly (andrea)

## 0.3.2 (08/09/2006)
### Added
- New .space directive, that allows to reserve some space in memory. It
  takes as a parameter an integer indicating the number of bytes to reserve. (andrea)
- Added a log window, that tracks the execution of EduMIPS64. Messages can
  be printed via the ui.GUILog methods, called on the static Main.logger
  object (andrea)
- Ant buildfile added. On my PC, make achieved a 22 seconds build time,
  while Ant achieved a 4 seconds build time, due to its ability to consider
  dependencies. Some targets are not ready, but jarfile is built by simply
  executing "ant". (andrea)
- Advanced frames handling (improved tiling algorithm, ability to hide/unhide
  windows from menu) (andrea)
### Changed
- The "Execute" and the "Tools" menu now become gray if all the inner items
  are not available (andrea)
- Log window icon (vanni)
- CPUGUIThread now doesn't wait for the sleep interval to pass when a single
  step is issued (andrea)
### Fixed
- Integer overflow and division by zero exception handling.
  The execution is stopped if one of these exceptions is raised. This solved
  a previously unknown bug: after an integer overflow, EduMIPS64 entered in
  an infinite loop. Now it works (andrea)
- Removed a debug print in CPUGUIThread (andrea)
- Added Vanni in the Main class copyrights, because a significant portion of
  that class' code is written by him (andrea)

## 0.3.1 (03/09/2006)
### Added
- Added the ability to customize the delay between cycles in verbose mode (andrea)
- The jar file can be used as an applet (vanni)
### Fixed
- JDialog + XGl fix (mancausoft + andrea)
- JApplet + XGl fix (mancausoft)

## 0.3 (23/07/2006)
### Added
- Added a progress bar that warns the user that the CPU is executing
  something (andrea)
- Added an option that allows the user to choose if he wants to see the
  progress of the multi-step execution via GUI or not. (andrea)
  - Handled the FileNotFoundException exception in the Main class. (andrea)
- In DineroFrontend, dineroIV is invoked if you press Enter when you are in
  the parameters textfield. (andrea)
### Changed
- Updated the manual according to the last changes. (andrea)

## 0.2.9 (22/07/2006)
### Added
- Created a thread that controls the CPU and the GUI. (antonella e andrea)
- Stop menu item, used to stop the execution at any time. (antonella e
  andrea)
- Minor changes to the GUI code in order to support multi-threading. (andrea)
- Internal code fixes: implemented a real state machine for the CPU and the
  GUI menu items that are enabled/disabled. For info about the four states,
  see the CPU.java documentation and source code. (andrea)
- Minor documentation and code improvements (andrea)

## 0.2.2 (06/07/2006)
### Changed
- Improved error and crash window (vanni)

## 0.2.1 (29/06/2006)
### Added
- Added the Dinero Path to the Preferences (vanni)
- The parser reads the file as it's ISO-8859-1. (mancausoft) [Will be made customizable later].
- Added some instructions in the Italian manual (Vanni)
### Changed
- Better About Window (vanni)
### Fixed
- Fixed the "negative memory address" bug (mancausoft)
- Fixed a bug in the CPU reset: now LO and HI are correctly resetted (andrea)

## 0.2 (26/06/2006) [Public Release]
### Added
- Now in the titlebar there's the version number (andrea)
### Fixed
- Fixed Dinero Frontend font that in some PCs wasn't monospaced. (andrea)
- Fixed a DDIV bug: semaphores where wrongly decremented if there was a
  division by zero and forwarding was disabled (mancausoft)
- Fixed a micro-bug in the translations (andrea)
### Changed
- Changed DineroIV default options (andrea)

## 0.1.5 (25/06/2006)
### Added
- The status bar displays the decimal value of a register or of a memory
  cell if you click on it (vanni) [I told you that the status bar would be useful.. :P]
### Changed
- Updated the manual in order to include recent features. (andrea)
### Fixed
- Removed accented letters from the italian version of the manual (ale)
- Now the Manual is a modal dialog, so we solve the multiple-opening
  manual bug (andrea)
- Solved the "impossible focus" bug for each dialog. (andrea)
- Fixed a little bug that made possible to select Dinero Frontend or Write
  dinero tracefile if you opened a file after you completed the execution of
  another file (andrea)
- Fixed the "row selection" bug in the Registers frame: now you can interact
  only with GPR registers, as FP registers don't exist. (vanni)
    
## 0.1.4.1 (25/06/2006)
### Fixed
  - Fixed the missing translations bug (GUIManual && StatusBar) (andrea)
  - Fixed a bug that didn't make forwarding enabled from the GUI Config (andrea)

## 0.1.4 (25/06/2006)
### Added
- Added a status bar, that will be useful later. (andrea)
- Replaced all the Configure menu items with a configuration dialog (Vanni)
- Internationalization of the Manual window, plus adding version info from
  the Main class (andrea)
- The code window now focuses on the IF row even if it's not in the range
  of currently visible rows (andrea)
### Fixed
- Fixed the "Timmy bug": memory location that don't hold a value ara
  automatically set to zero and a warning is shown (mancausoft)
- Fixed a bug that crashed the parser if after a comma there
  wasn't a parameter for the instruction (mancausoft)
- Recent files list isn't allowed to hold multiple copies of a file (andrea)
- Improved the look&feel of the Manual window (filippo)
- Fixed a bug in the instruction set: the DMULT instruction was called DMUL (massimo)
- Fixed a bug in the parser that made possible to open malformed asm files (mancausoft)
- Little bugfix: if the opened file contains syntax errors, the window
  title is correctly cleaned (andrea)

## 0.1.3 (23/06/2006)
### Added
- Updated documentation for the new warnings menu option (andrea).
- Added an option to enable/disable warnings (vanni).
### Fixed
- Fixed the bug in the Gui Code frame that made the fetched instruction
  not to have its own colored background (andrea).
- Fixed the DineroFrontend CPU Burst bug (massimo + mancausoft).
- Debug printouts cleanup (andrea + vanni)

## 0.1.2 (20/06/2006)
### Added
- Added the split size chooser in the Cycles window (Filippo)
- Added a note in the changelog with the name of the author of the change
  entry(andrea)
### Fixed
- Fixed (maybe) the GREAT FORWARDING BUG (mancausoft)
- Dinero Frontend now works under Windows and Linux, with the standard
  Dinero distribution. (mancausoft)
- Fixed a bug in the Statistics window: CPI's now are back (Ale)

## 0.1.1 (19/06/2006)
### Fixed
- Improved the cycles windows (vanni)
- The statistics windows is now fully internationalized (andrea)

## 0.1 (17/06/2006)
### Added
  - Recent files list (vanni)
### Changed
  - Dinero frontend disabled under windows (andrea)
### Fixed
  - Fixed the "infinite open" bug (mancausoft)

## 0.0.8 (16/06/2006)
### Added
  - Added release_notes, readme, changelog, install, authors to the source
    distribution (andrea)
### Fixed
  - Fixed the config bug: saving config file into the .jar didn't work if
    there were spaces in the current directory name (andrea)
  - Closing the manual Window doesn't exit the program anymore. (vanni)

## 0.0.7 (15/06/2006)
### Added
  - First numbered release, contains almost all the planned edumips64
    features. (edumips64 team)