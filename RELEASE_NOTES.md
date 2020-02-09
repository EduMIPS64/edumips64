# EduMIPS64 version 1.2.6

*26th of January, 2020*

EduMIPS64 is a GPL MIPS64 Instruction Set Architecture (ISA) simulator and
graphical debugger.

## Notes for this release:

This is version 1.2.6 of EduMIPS64, a release that follows up on version 
1.2.5, that was released in August 2018. This release contains a couple of
changes, a new instruction alias (DMULU for DMULTU), an increased memory 
size (640kB), a better way to report bugs via GitHub and several bug fixes. 

The codename for this release is Phlegmatic. First because it took a while
to release a new version after 1.2.5. Second because I am releasing it while
fighting with a bad and annoying cough, which I hope goes away as soon as
possible.

This release reverts the build system change in 1.2.5: we went back from
Bazel to Gradle. There was no real benefit in using Bazel, and Gradle is
more supported by IDEs. The main advantage we got from the Bazel migration
was better code modularity, and that benefit still remains.

We are now using Azure Pipelines instead of Travis as a CI system.

Please keep in mind that this is still EXPERIMENTAL SOFTWARE. It may
BURN YOUR HARD DISK, DESTROY ALL YOUR DATA and even GO OUT WITH YOUR
PARTNER. :)

If you find a bug, please open an issue on GitHub.

EduMIPS64 is hosted on GitHub: www.github.com/EduMIPS64/edumips64.
Our web site is www.edumips.org, and our development blog is
http://edumips64.blogspot.com.

## New in this release since version 1.2.5

### Added

- Alias DMULU for DMULTU (Issue #249)

### Fixed

- Trying to store a large memory location in an immediate field causes EduMIPS64 to crash (Issue #255)
- Fixed NullReference in the CLI application (#258)

### Changed

- Improved the way OOM errors are handled (Issues #257 and #266)
- Increased default data memory to 640 kB (Issue #268)
- Improved the bug report dialog (Issue #262)
- Code quality improvements (Issue #222, still open)
- Migrated back to Gradle from Bazel (Issue #226)
