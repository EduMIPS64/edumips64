# EduMIPS64 version 1.2.8

*22nd of October, 2020*

EduMIPS64 is a GPL MIPS64 Instruction Set Architecture (ISA) simulator and graphical debugger.

## Notes for this release

This is version 1.2.8 of EduMIPS64. Its codename is NLMS, to remember my late father
Nicola Luigi Maria Spadaccini, who left us last month.

This version contains a few bug fixes (including a fix for the LUI
instruction which was completely broken) and a new experimental CLI interface
(which you can enter by using the `--headless` command-line flag).

This is also the first version to be distributed as a Windows MSI installer, which contains
a JRE and everything you need to run the simulator on Windows without needing external
dependencies. As usual, the simulator is also distributed as a JAR and via the Snapcraft store.

This version contains contributions from the following people, listed in no particular order:

* iwodder - [**@iwodder**](http://github.com/iwodder)
* Miguel Pinto - [**@rocas777**](http://github.com/rocas777)
* Oscar Elhanafey - [**@Ooelhana**](http://github.com/Ooelhana)
* Paolo Viotti - [**@pviotti**](http://github.com/pviotti)
* Pimts - [**@pimts**](http://github.com/pimts)
* Andrea Spadaccini - [**@lupino3**](http://github.com/lupino3)

Please keep in mind that this is still EXPERIMENTAL SOFTWARE. It may
BURN YOUR HARD DISK, DESTROY ALL YOUR DATA and even GO OUT WITH YOUR
PARTNER. :)

If you find a bug, please open an issue on GitHub.

EduMIPS64 is hosted on GitHub: www.github.com/EduMIPS64/edumips64.

Our web site is www.edumips.org, and our development blog is http://edumips64.blogspot.com.

## Main changes since 1.2.7.1
### Added
- New experimental command-line interface, started with the --headless command-line option
- Windows installer (MSI)

### Changed
- EduMIPS64 now uses Java 11, to benefit from modern Java features
- Adopted picocli for command-line options
- Removed the JAR with no bundled dependencies, since start-up now depends on picocli

### Fixed
- Factor out the argv parsing logic (Issue #199)
- LUI instruction throws IndexOutOfBoundsException (Issue #475)
- Missing help in JAR (Issue #476)
- Fix italian flag icon (Issue #420)
- Code quality improvements
