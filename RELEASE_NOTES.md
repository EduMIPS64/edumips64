# EduMIPS64 version 1.2.9

*8th of November, 2020*

EduMIPS64 is a GPL MIPS64 Instruction Set Architecture (ISA) simulator and graphical debugger.

## Notes for this release

This is version 1.2.9 of EduMIPS64. Its codename is Baby Shark, because my
brain is overloaded with the song thanks to my son, and for some reason this
is the only name that comes to mind right now.

This version contains 2 major bug fixes, one for the LUI instruction, which in 1.2.8 was not
completely fixed, and another for the in-app manual, which was not displayed correctly
in some platforms.

This version contains contributions from the following people, listed in no particular order:

* leopoldwe - [**@leopoldwe**](http://github.com/leopoldwe)
* Paolo Viotti - [**@pviotti**](http://github.com/pviotti)
* Andrea Spadaccini - [**@lupino3**](http://github.com/lupino3)

Please keep in mind that this is still EXPERIMENTAL SOFTWARE. It may
BURN YOUR HARD DISK, DESTROY ALL YOUR DATA and even GO OUT WITH YOUR
PARTNER. :)

If you find a bug, please open an issue on GitHub.

EduMIPS64 is hosted on GitHub: www.github.com/EduMIPS64/edumips64.

Our web site is www.edumips.org, and our development blog is http://edumips64.blogspot.com.

## Main changes since 1.2.8
### Added
- Unit tests for multiple instructions (PR 488)

### Fixed
- LUI modifies static field of `ALU_IType` breaking the simulator every time it is run (Issue #501)
- Character encoding issues in manual (Issue #489)