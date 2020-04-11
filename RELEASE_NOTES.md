# EduMIPS64 version 1.2.7

*11th of April, 2020*

EduMIPS64 is a GPL MIPS64 Instruction Set Architecture (ISA) simulator and graphical debugger.

## Notes for this release:

This is version 1.2.7 of EduMIPS64, a bug-fix release. Its codename is Hope, because that's what helps us during the COVID-19 lockdown, and also as a reference to the upcoming Easter.

This release most notably fixes issue #308, which exposed 2 bugs related to EduMIPS64's FPU.

We are now using GitHub Actions instead of Azure Pipelines as a CI system.

Please keep in mind that this is still EXPERIMENTAL SOFTWARE. It may
BURN YOUR HARD DISK, DESTROY ALL YOUR DATA and even GO OUT WITH YOUR
PARTNER. :)

If you find a bug, please open an issue on GitHub.

EduMIPS64 is hosted on GitHub: www.github.com/EduMIPS64/edumips64.

Our web site is www.edumips.org, and our development blog is http://edumips64.blogspot.com.

## New in this release since version 1.2.6

### Fixed
- RAW stall in combination with FPU caused instructions to disappear (Issue #304)
- Some instructions not showing correctly in Cycles UI (Issue #304)

### Changed
- Using GitHub Actions instead of Azure Pipelines as CI.
- Added timeouts to tests