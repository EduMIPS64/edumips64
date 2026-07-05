# Web UI Roadmap
This is the list of user stories for the web UI, categorized in milestones, which
will serve as a guide for the development of the Web UI.

In the milestone list, stories will be addressed with the initial of their
section and then the number of the story (e.g., `BREAK support` is `E.6`).

## Introduction

The current Web UI is available at https://web.edumips.org/.

EduMIPS64 was born in 2006 as a desktop application, even if it initially ran
as a Java applet in addition to running as a desktop app.

As technology evolved, Java applets became obsolete, and EduMIPS64 doesn't run as
an applet anymore, but the idea of running the simulator on a browser is still
something useful, because it reduces installation friction and enables lots of
scenarios that are simply not possible in a desktop application.

In 2016, the core parts of the simulator were cleaned up and separated from the
rest of the application, and cross-compiled to JavaScript with Google Web Toolkit.

This document analyzes the work necessary to build a modern, clean web UI for
EduMIPS64 and, by extension, refine the Java -> JS API created with GWT.

## Milestones
### Alpha (EduMIPS64 2.0-alpha)
Status: **Completed**
Tracked on https://github.com/EduMIPS64/edumips64/issues/852 (closed)

This milestone is a basic working simulator, with a pretty rough UI and just enough
basic features to run most programs and verify its working.

User stories and features:

* ~~`E.1-6` (Full IS support, even with primitive UI and partial support for SYSCALL)~~
* ~~`U.1-5` (Basic functional UI, no settings)~~

### Beta (EduMIPS64 2.0-beta)
Status: **In Progress**

This milestone will improve the UI and enable settings support. The web UI
is intentionally kept in the same repository as the Java core (see `D.2`
below).

User stories and features:

* ~~`D.1, D.3` (Proper toolchain, Material UI)~~
* ~~`U.6` (full pipeline widget)~~
* `U.7` (full cycles widget, i.e. a temporal instruction-vs-cycle diagram like Swing's `GUICycles`)
* ~~`U.8` (Settings UI)~~
* ~~`S.1-2` (Forwarding setting, persisted via HTML5 storage)~~

### GA (EduMIPS64 2.0)
Status: **In Progress**

Fully working web simulator, suitable as a replacement for the current simulator.

* `E.7-8` (Dinero Tracefile Download, file-based SYSCALL)
* ~~`U.9` (Monaco-based editor with MIPS syntax highlighting and inline error markers)~~
* `U.10` (localization of the simulator UI)
* `S.3` (exceptions settings)
* `S.4` (FPU settings)
* `S.5` (warning/error settings)
* ~~`D.4` (Instrumentation)~~

### Future (EduMIPS64 3.0)
Status: **Not started**

The web allows us to do much more than we could do as a Java application. Examples
of future features might be:

* ~~live error checking~~
* shareable EduMIPS64 sessions based on simulator state (query string)
* "Run with EduMIPS64" button?
* support for opening from URL?
* maybe some sort of backend support to have workspaces, save files in external
  services such as GDrive, Gist, etc..

## User Stories / Features

Anything that is done (as of 05/07/2026) is marked with strike-through.

### Prerequisites
This section contains the preliminary work necessary to move forward with the
implementation of the remaining features.

1. ~~experiment with web frameworks and decide which one to use (#86)~~
2. ~~decide which layout to use (#86)~~

### Execution
1. ~~open a MIPS64 assembly program from a text area~~
2. ~~executing a MIPS64 assembly program~~
   1. ~~all at once~~
   1. ~~showing progress as time goes by~~
      1. ~~setting a customizable processor frequency (to show updates slowly)~~
   1. ~~step-by-step~~
      1. ~~with a customizable stride~~
  1. ~~reset execution state while paused~~
4. ~~graceful handling of all errors, including parser errors~~
5. SYSCALL support
   1. ~~console I/O (via Web UI)~~ (output via the StdOut accordion, input
      via a modal dialog)
6. ~~BREAK support~~
7. downloading a Dinero Tracefile at the end of the execution
8. SYSCALL File I/O (not sure it's possible, needs to be investigated)

### UI
1. ~~Basic code editor text area widget~~
2. ~~Execution controls (start/stop/etc.)~~
3. ~~Basic registers, memory and code widgets.~~
4. ~~Basic pipeline widget (no graphics, just text)~~
5. ~~I/O widgets (necessary for `E.5.1`)~~
6. ~~Full Pipeline widget (state of which instructions are in which stage)~~
   (mirrors the Swing `GUIPipeline`: IF/ID/EX/MEM/WB plus the FP
   adder/multiplier/divider functional units, customizable per-stage
   colors, and labeled RAW/WAW/structural stalls)
7. Full Cycles widget (temporal instruction diagram) — the Swing `Cycles`
   panel's history-across-cycles view; the web UI currently only shows the
   current cycle's pipeline snapshot
8. ~~Settings UI (necessary for `S.*`)~~
9. ~~Code editor with syntax highlighting (e.g., ACE or Monaco)~~
10. Localization (the Help dialog already supports en/it/zh; the rest of
    the UI is still English-only)
11. Show binary/decimal/hex values of all numbers in the UI. (partially:
    registers and memory cells show hex with the decimal value in a
    tooltip; binary is not yet shown)
12. ~~Show program counter~~ (rendered with the other special registers)

### Settings
1. ~~Enabling / disabling forwarding~~
2. ~~Storing preferences locally (probably with HTML5 storage)~~ (a
   schema-driven `useSetting` hook persists every preference to
   `localStorage`)
3. Setting options related to exceptions
4. Setting options related to the FPU
5. Setting parser warning/errors options

### Development features - DONE
1. ~~Move to a proper JS toolchain.~~
2. ~~Build and deploy the frontend JS from a separate GitHub repository to web.edumips.org. (#87)~~
   **Dropped.** The web UI will keep living in the same repository as the
   Java core: it shares the GWT-compiled worker with the core, and the
   single-repo setup has worked well in practice.
3. ~~Move to React Material UI~~
4. ~~Find a good (dashboard) Material UI template to use as a starting point (e.g. https://github.com/devias-io/react-material-dashboard)~~
5. ~~Instrument web UI (Google Analytics + some metrics)~~ (Azure
   Application Insights)
