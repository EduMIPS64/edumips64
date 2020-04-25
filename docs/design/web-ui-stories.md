# Web UI Roadmap
This is the list of user stories for the web UI, categorized in milestones, which
will serve as a guide for the development of the Web UI.

User stories should have corresponding GitHub issues (even if currently they don't).

In the milestone list, stories will be addressed with the initial of their
section and then the number of the story (e.g., `BREAK support` is `E.6`).

## Milestones
### Alpha (Unnamed)
This milestone is a basic working simulator, with a pretty rough UI and just enough
basic features to run most programs and verify its working.

User stories and features:

* `E.1-6` (Full IS support, even with primitive UI and partial support for SYSCALL)
* `U.1-5` (Basic functional UI, no settings)

### Beta (EduMIPS64 2.0-beta)
This milestone will improve the UI and split it out in its own repository
and enable settings support.

User stories and features:

* `D.1-3` (Separate repo, proper toolchain, Material UI)
* `U.6-8` (Improved UI, including settings)
* `S.1-2` (Forwarding setting)

### GA (EduMIPS64 2.0)
Fully working web simulator, suitable as a replacement for the current simulator.

* `E.7-8` (Dinero Tracefile Download, file-based SYSCALL)
* `U.9-10` (Better code editor, localization)
* `S.3-5` (Most runtime settings)
* `D.4` (Instrumentation)

### Future (EduMIPS64 3.0)
The web allows us to do much more than we could do as a Java application. Examples
of future features might be:

* live error checking
* shareable EduMIPS64 sessions based on simulator state (query string)
* "Run with EduMIPS64" button?
* support for opening from URL?
* maybe some sort of backend support to have workspaces, save files in external
  services such as GDrive, Gist, etc..
* a no-Javascript version that uses SSR?

## User Stories / Features
### Prerequisites
This section contains the preliminary work necessary to move forward with the
implementation of the remaining features.

1. experiment with web frameworks and decide which one to use (#86)
2. decide which layout to use (#86)

### Execution
1. open a MIPS64 assembly program from a text area
2. executing a MIPS64 assembly program
   1. all at once
   1. showing progress as time goes by
      1. setting a customizable processor frequency (to show updates slowly)
   1. step-by-step
      1. with a customizable stride
  1. reset execution state while paused
4. graceful handling of all errors, including parser errors
5. SYSCALL support
   1. console I/O (via Web UI)
6. BREAK support
7. downloading a Dinero Tracefile at the end of the execution
8. SYSCALL File I/O (not sure it's possible, needs to be investigated)

### UI
1. Basic code editor text area widget
2. Execution controls (start/stop/etc.)
3. Basic registers, memory and code widgets.
4. Basic pipeline widget (no graphics, just text)
5. I/O widget (necessary for `E.5.1`)
6. Full Pipeline widget (state of which instructions are in which stage)
7. Full Cycles widget (temporal instruction diagram)
8. Settings UI (necessary for `S.*`)
9. Code editor with syntax highlighting (e.g., ACE or Monaco)
10. Localization

### Settings
1. Enabling / disabling forwarding
2. Storing preferences locally (probably with HTML5 storage)
3. Setting options related to exceptions
4. Setting options related to the FPU
5. Setting parser warning/errors options

### Development features
1. Move to a proper JS toolchain. Build and deploy the frontend JS from a separate GitHub repository to web.edumips.org.
2. Move to React Material UI
3. Find a good (dashboard) Material UI template to use as a starting point (e.g. https://github.com/devias-io/react-material-dashboard)
4. Instrument web UI (Google Analytics + some metrics)