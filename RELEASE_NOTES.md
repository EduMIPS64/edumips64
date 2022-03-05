# EduMIPS64 version 1.2.10

*5th of March, 2021*

EduMIPS64 is a GPL MIPS64 Instruction Set Architecture (ISA) simulator and graphical debugger.

## Notes for this release

This is version 1.2.10 of EduMIPS64. Its codename is **FP - Freedom and Peace**, because
of the ongoing conflict in Ukraine, which is being invaded by Russia. Freedom and Peace
is what I wish right now to the Ukrainians.

This is mostly a bug-fixing release: issues #450, #646 and #304 (for the second time) were
fixed. In terms of development changes, we moved to JDK (and JRE) 17 and we fixed the Snapcraft
packages for the `armhf` architecture (among others). The latter means that EduMIPS64 is
available on Raspberry PI via `snap`!

This version contains contributions from the following people, listed in no particular order:

* @hugmanrique and @jcarletta - reported 2 critical bugs and provided MIPS64 code to reproduce them,
  which I was allowed to incorporate as regression tests.
* Andrea Spadaccini - [**@lupino3**](http://github.com/lupino3)

Please keep in mind that this is still EXPERIMENTAL SOFTWARE. It may
BURN YOUR HARD DISK, DESTROY ALL YOUR DATA and even GO OUT WITH YOUR
PARTNER. :)

If you find a bug, please open an issue on GitHub.

EduMIPS64 is hosted on GitHub: www.github.com/EduMIPS64/edumips64.

Our web site is www.edumips.org, and our development blog is http://edumips64.blogspot.com.

## Main changes since 1.2.9

### Added

* New Snapcraft packages for armhf (e.g. Raspberry PI) and other architectures

### Fixed

* Parser incorrectly interprets hexadecimal immediates (Issue #450)
* Some floating-point division cycles missing in Cycles window (Issue #646)
* Infinite RAW stall in floating-point code (Issue #304) (yes, *again*)

### Changed

* Migrated to JDK (and JRE) 17
