# Web UI User Stories

This is the list of user stories for the web UI, categorized in milestones.

The user here is just the student or enthusiast interested in playing with a
MIPS64 simulator. The context is always a desktop web interface, unless
specified.

Stories will be addressed with the initial of their section and then the
number of the story (e.g., `BREAK support` is `E.9`).

In terms of priority, `P.*` need to be done before any other work is done.

A first alpha version could probably contain `E.1-6` (full Instruction Set
support, even if the web UI for `SYSCALL` can be very primitive) and a few
elements of the UI (maybe `U.1-3`).

The first publicly available version (let's call it EduMIPS64 2.0) should have
a mostly complete UI, ideally with `E.7`, `U.1-5` and at least `S.1-3`
implemented.

### Prerequisites

This section contains the preliminary work necessary to move forward with the
implementation of the remaining features.

1. experiment with web frameworks and decide which one to use (#86)
2. decide which layout to use (#86)

### Execution

1. open a MIPS64 assembly program from a text area
2. open a MIPS64 assembly program from disk
3. parsing a MIPS64 assembly program, having clear indications of errors
   1. option to make warnings errors (?)
4. executing a MIPS64 assembly program
  1. all at once
    1. showing progress as time goes by
      1. setting a customizable processor frequency (to show updates slowly)
  1. step-by-step
     1. with a customizable stride
  1. reset execution state while paused
5. SYSCALL support
   1. console I/O (via Web UI)
   1. file I/O (??)
6. BREAK support
7. downloading a Dinero Tracefile at the end of the execution

### UI

Here, the components of the UI are simply called "widgets" because, as of now,
it is still unclear how they will be represented.

1. Registers, memory and code widgets.
2. Pipeline widget (state of which instructions are in which stage)
3. Cycles widget (temporal instruction diagram)
4. I/O widget (necessary for `E.5.1`)
5. Settings UI (necessary for `S.*`)
6. Code editor text area widget
7. Code editor with syntax highlighting.

### Settings

1. setting the UI language
2. enabling / disabling forwarding
3. storing preferences (locally, probably with HTML5 storage)
4. Setting options related to exceptions
5. Setting options related to the FPU

### New features

1. online warnings/errors in the code window (real-time checks as the user
   types)
2. open from URL (?)
