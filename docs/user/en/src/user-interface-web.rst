The web user interface
======================
EduMIPS64 is also available as a web application that runs entirely in
the browser. The simulator core is cross-compiled from Java to
JavaScript and runs as a Web Worker, while the user interface is built
with React. The production deployment is hosted at
https://web.edumips.org.

This chapter describes the web frontend. For the source file format,
the supported instruction set, the FPU and example programs, refer to
the other chapters of this manual: those are independent of the user
interface.

Layout overview
---------------
The window is organized into a top toolbar and a resizable workspace with
three regions:

* On the left of the upper area, the **code editor** (a Monaco-based
  MIPS64 editor).
* On the right of the upper area, a column of collapsible panels showing
  the runtime state of the simulation: **Issues**, **Statistics**,
  **Pipeline**, **Registers**, **Memory** and **Standard Output**.
* Across the full width of the lower area, the **Cycles** diagram — a
  temporal, instruction-versus-cycle view of the pipeline that grows
  horizontally as the program runs (see *The Cycles diagram* below).

The boundary between the code editor and the widgets column, and the
boundary between the upper area and the Cycles region, can be dragged to
resize them; each region keeps a minimum size so the code editor always
stays usable. The widgets column and the Cycles region can each be
collapsed to a thin bar with the toggle button in their header, and
expanded again from the same button. The chosen sizes and collapsed
states are saved in the browser's local storage and restored on the next
visit.

On narrow windows (phones and small tablets) the three regions stack
vertically and the whole page scrolls, instead of being split.

The **Cache Configuration** and **General Settings** described later in
this chapter are reached from the gear button in the top toolbar rather
than from the right-hand column.

The top toolbar
---------------
The toolbar at the top of the window groups every action that controls
the simulator. Each button has a tooltip that describes its effect.

* **EduMIPS64 logo and "Web Version" label** — indicate the running
  build. A coloured chip is shown next to the label when the build is
  not the production one:

  * a yellow ``PR #N`` chip identifies a per-pull-request preview build
    and links back to the originating pull request on GitHub;
  * a blue ``dev`` chip identifies a local development build.

* **CPU status chip** — shows the current state of the simulated CPU
  with a colour code:

  * ``READY`` (green) — no program is loaded yet, or the CPU has just
    been reset;
  * ``RUNNING`` (yellow) — a program is loaded and the CPU is in the
    middle of an execution;
  * ``STOPPING`` / ``STOPPED`` (red) — a termination instruction has
    been fetched and the pipeline is draining, or the program has
    ended.

Execution controls and toolbar layout
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The execution controls appear as a **floating, draggable, icon-only
toolbar** that overlays the content, much like the debug toolbar in
VS Code. The toolbar is contextually shown based on the current
simulator state, reducing visual clutter and making available actions
immediately obvious:

* **EMPTY** (no program loaded) — the toolbar is hidden; only the
  ``Load`` button in the top header is available.
* **READY** (program loaded, ready to run) — the floating toolbar
  appears with ``Single Step``, ``Multi Step``, ``Run All``, and
  ``Stop`` as icon buttons. The toolbar can be dragged to reposition
  it anywhere on the screen.
* **EXECUTING** (program running) — the toolbar shows ``Pause`` and
  ``Stop`` (disabled with the tooltip "Pause before stopping"). The
  toolbar remains draggable.
* **ENDED** (program finished) — the toolbar is hidden.
* **Waiting for input** (input dialog open) — the toolbar is hidden;
  the input dialog must be resolved first.

The **Load** button, the **Program** menu, and the **Help (?)** button
remain always visible in the top header bar.

Individual button descriptions:

* **Single Step** — executes one CPU cycle. Shown in the floating
  toolbar when a program is loaded and ready to execute.

* **Multi Step** — executes a configurable number of CPU cycles in a
  single click. The number of steps is shown in the button's tooltip
  and can be changed in the *General Settings* panel ("Multi Step
  Size"). Shown in the floating toolbar when a program is loaded and
  ready to execute.

* **Run All** — executes the program until it terminates with a
  ``SYSCALL 0`` (or equivalent) or a ``BREAK`` instruction, or until
  it is paused or stopped manually. Between batches of cycles the
  simulator can wait a configurable delay (``Execution Delay``) so
  that long runs remain visually observable. Shown in the floating
  toolbar when a program is loaded and ready to execute.

* **Pause** — interrupts a running execution at the current cycle.
  Single Step / Multi Step / Run All can then be used to continue.
  Shown in the floating toolbar only while the program is actively
  executing.

* **Stop** — halts the running execution and resets the CPU to the
  ``READY`` state, clearing registers, memory and pipeline. Shown in
  the floating toolbar in READY and EXECUTING states (disabled in
  EXECUTING with tooltip "Pause before stopping").

Program menu
~~~~~~~~~~~~
The **Program** menu (folder icon with a dropdown caret) consolidates
program management into a single header button. It opens a dropdown menu
with the following items. **The menu is unavailable while a program is
loaded in the simulator** (i.e. while the simulator is running), preventing
accidental changes to the program during simulation. It becomes available
again once the loaded program has finished running or after the simulator
is reset (no program loaded).

* **New** — empties the code editor, leaving only an empty assembly
  skeleton (``.data`` and ``.code`` directives plus a final
  ``SYSCALL 0``).

* **Open…** — opens a local file (typically a ``.s`` file) and
  loads its contents into the editor.

* **Save…** — saves the current contents of the editor to a local
  file named ``code.s``.

* **Load Example** — replaces the editor contents with the
  bundled sample program shipped with EduMIPS64 (the same one shown
  the first time you open the web simulator). This is useful to
  recover a known-good
  starting point after experimenting, or to discard the persisted
  editor contents (see *Saving and loading* below).

Help button
~~~~~~~~~~~
* **Help (?)** — opens this manual inside the application, with a
  navigation drawer on the left and a language selector that lets you
  switch between English, Italian and Chinese. The Help dialog also
  includes an *About* tab that shows the version of the simulator and
  a description of the running build.

The code editor
---------------
The code editor is based on `Monaco
<https://microsoft.github.io/monaco-editor/>`_ — the editor that powers
Visual Studio Code — and is dedicated to writing MIPS64 assembly. It
supports all the usual code-editor conveniences (multi-cursor, find &
replace, line numbers, undo/redo, automatic layout, automatic
light/dark theme based on the OS preference) plus a number of
EduMIPS64-specific features described below.

Syntax highlighting
~~~~~~~~~~~~~~~~~~~
The editor provides syntax highlighting for MIPS64 sources:

* labels (lines starting with an identifier followed by ``:``);
* every instruction supported by EduMIPS64 (the list of valid
  instructions is computed at runtime from the simulator core);
* directives that start with ``.``, e.g. ``.data``, ``.code``,
  ``.word``;
* register names of the form ``rNN``;
* numeric literals;
* string literals;
* comments starting with ``;``.

Live validation
~~~~~~~~~~~~~~~
The simulator parses the code in the background while you type and
reports any **errors** and **warnings** directly in the editor:

* errors are underlined in red;
* warnings are underlined with a yellow squiggle;
* hovering over an underlined region shows the description of the
  problem in a tooltip;
* the affected line is also marked in the editor's "minimap"-style
  gutter.

The same problems are summarized in the **Issues** panel on the right
(see below). Warnings do not block execution; errors do — pressing
``Load`` while the program contains errors will surface a popup with
the parser's message.

Hover-based information
~~~~~~~~~~~~~~~~~~~~~~~
Once a program has been parsed (i.e. once it has been loaded with the
``Load`` button), hovering over an instruction in the editor opens a
tooltip with information about it:

* **Address** — the memory address at which the instruction has been
  placed.
* **OpCode** — the assembly opcode (e.g. ``DADD``, ``LD``).
* **Binary** — the 32-bit binary encoding.
* **Hex** — the same encoding in hexadecimal, zero-padded to 8 digits.
* **CPU Stage** — only shown if the instruction is currently inside
  the pipeline; identifies the stage in which it sits at the current
  cycle (e.g. ``Instruction Fetch (IF)``, ``Execute (EX)``, ``FPU
  Multiplier (3)``).

Real-time pipeline-stage visualization
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
While the program is running, the line of source corresponding to the
instruction currently in each pipeline stage is highlighted with a
colour that identifies the stage. The colour code is shared with the
**Pipeline** panel:

================================== ==================
Stage                              Highlight colour
================================== ==================
Instruction Fetch (IF)             Yellow
Instruction Decode (ID)            Blue
Execute (EX)                       Red
Memory Access (MEM)                Green
Write Back (WB)                    Magenta
FPU Adder (1..4)                   Dark green
FPU Multiplier (1..7)              Teal
FPU Divider                        Olive
================================== ==================

The highlight follows the instructions through the pipeline as the
simulation advances, providing an at-a-glance view of which lines of
source code are active in which stage at any given cycle. Combined
with the per-instruction hover tooltip described above, this makes it
easy to inspect the state of the pipeline at any point during the
execution.

The editor becomes read-only while a program is loaded into the
simulator. Use ``Stop`` to reset the CPU and edit the source again.

Saving and loading
~~~~~~~~~~~~~~~~~~
The editor's contents can be persisted using the **Save Code** and
**Open Code** toolbar buttons. ``Save Code`` triggers a download of
the current source as ``code.s``; ``Open Code`` lets you pick a local
file and replaces the editor's contents with it.

In addition, the editor automatically persists its contents in the
browser's local storage as you type, so an accidental page reload
does not wipe a non-trivial program back to the bundled sample. The
last edited source is restored the next time the page is opened in
the same browser. Use the **Restore default sample** toolbar button
to discard the persisted contents and bring back the original
example program.

Optional Vi mode
~~~~~~~~~~~~~~~~
A *Vi mode* for the editor can be toggled in the *General Settings*
panel. When enabled, the editor honours basic vi keybindings (modes,
motions, search), which is convenient if you are used to editing
sources from a terminal.

Font size
~~~~~~~~~
The editor's font size can be increased or decreased from the *General
Settings* panel; the chosen size is also used by other monospaced
elements of the UI.

The Issues panel
----------------
The **Issues** panel on the right of the window mirrors the
diagnostics that the editor surfaces inline:

* every entry shows the line and column of the problem and a short
  description from the parser;
* a warning icon (yellow triangle) marks warnings, an error icon (red
  circle) marks errors;
* the panel header shows two count chips, one for warnings and one
  for errors. The chips are hidden when there is nothing to report;
* every entry is clickable: selecting an issue scrolls the editor so
  the offending line is centred in the viewport, places the cursor at
  the reported column and focuses the editor so you can start fixing
  the problem right away.

The Issues panel is expanded by default so problems are visible at a
glance.

Runtime panels
--------------
The right-hand side of the window stacks several collapsible
"accordion" panels. Each panel can be expanded or collapsed
independently by clicking on its header; the expansion state is
persisted across page reloads.

When a panel is collapsed and its contents change because of a
simulation step, a small pulsating dot appears next to the panel's
title. This makes it easy to spot interesting changes (for example, a
register being written) without having to keep every panel expanded.
The pulsating indicator can be disabled in *General Settings*
("Accordion Change Alerts").

Statistics
~~~~~~~~~~
Counters about the program execution:

* number of executed cycles;
* number of executed instructions;
* CPI — cycles per instruction (``cycles / instructions``);
* RAW, WAW and structural stalls;
* L1 instruction-cache reads and misses;
* L1 data-cache reads, read misses, writes and write misses (only
  meaningful when the cache simulator is configured — see *Cache
  Configuration*).

Pipeline
~~~~~~~~
Shows a graphical representation of the CPU pipeline that resembles the
classic Swing UI's pipeline diagram. The five integer stages (IF, ID,
EX, MEM, WB) are drawn as connected blocks, with the FPU functional
units — the FP Adder (4 stages), FP Multiplier (7 stages) and FP
Divider — laid out around them. Each block:

* lights up with the stage's colour while it holds an instruction, and
  shows the instruction's mnemonic inside the block;
* stays as an empty outline when the corresponding stage is idle or
  holds a pipeline bubble (e.g. branch-flush slots, end-of-program
  drain bubbles): just like Swing's pipeline widget, bubbles are
  rendered as empty stages;
* renders with a hatched fill, the dedicated *Stall* colour and a
  short stall-type label whenever a stall actually occurred in the
  current cycle. The labels match the Swing cycle widget's
  classification:

  - **RAW** — Read-After-Write data hazard (typically on the ID
    stage when forwarding is disabled);
  - **WAW** — Write-After-Write hazard between two FP instructions
    competing for the same destination register;
  - **Struct: Div / EX / FU** — structural hazard at the FP Divider,
    the integer EX stage or another FP functional unit;
  - **Struct: Mem / Add / Mul** — structural hazard caused by an
    instruction stuck in MEM, in the FP Adder's last stage (A4) or
    the FP Multiplier's last stage (M7).

  WAR (Write-After-Read) hazards are *not* possible in this MIPS
  implementation: the in-order issue at ID combined with the late
  writeback at WB orders all reads before later writes, so the
  simulator never raises one.

Stalls are identified by the same logic that updates the CPU's
stall counters, so the Web pipeline widget always agrees with the
totals shown in the *Statistics* panel.

The per-stage colours (including the *Stall* colour) can be
customised from *General Settings → Pipeline Colors* (see below) and
are persisted in browser local storage.

The Pipeline panel shows only the *current* cycle. For the history of
every instruction across all cycles, see *The Cycles diagram* below.

Registers
~~~~~~~~~
The contents of the integer general-purpose registers, the
floating-point registers and the FCSR are shown in this panel. Values
are displayed in their hexadecimal representation; hovering on a
value shows the corresponding decimal interpretation as a tooltip.

Memory
~~~~~~
The current contents of the simulated main memory, organized in
addressable cells. Each row shows the address (in hexadecimal) and the
value stored at that address; tooltips reveal the decimal value and
the source-code labels and comments associated with the cell.

Standard Output
~~~~~~~~~~~~~~~
A read-only text area that collects everything the program prints via
``SYSCALL 4`` (write integer) and ``SYSCALL 5`` (write string).
``SYSCALL 3`` (read string) is supported through a popup dialog: when
the running program issues a read, an *Input* dialog appears asking
for the value to feed back to the program. The dialog enforces the
maximum length declared by the program and can be cancelled.

Cache Configuration
~~~~~~~~~~~~~~~~~~~
Lets you configure the parameters of the L1 cache simulator:

* **Size** — total capacity of the cache, in bytes;
* **Block Size** — size of a single cache line, in bytes;
* **Associativity** — number of ways per set (``1`` is direct mapped,
  ``>1`` is set-associative).

The L1 instruction cache and the L1 data cache are configured
independently. The fields are disabled while the simulator is
running; the new configuration takes effect on the next reset.

General Settings
~~~~~~~~~~~~~~~~
Persistent settings that influence the simulator and the UI. All
values are saved in the browser's local storage and survive page
reloads.

* **Editor Vi Mode** — toggles basic vi keybindings in the code
  editor.
* **Font Size** — font size for the code editor and other monospaced
  panels; can be adjusted with the ``-`` and ``+`` buttons.
* **Accordion Change Alerts** — enables or disables the pulsating
  indicator shown on collapsed panels when their contents change.
* **CPU Forwarding** — enables or disables operand forwarding in the
  pipeline. Disabled while the simulator is running because changing
  it requires a reset.
* **Branch Delay Slot** — enables or disables the classical MIPS
  branch delay slot, as described in Hennessy & Patterson. When
  enabled, the instruction immediately following any branch or jump
  is **always executed**, regardless of whether the branch is taken;
  when disabled (the default), that instruction is squashed and the
  pipeline shows a bubble. Disabled while the simulator is running
  because changing it requires a reset.
* **Multi Step Size** — number of cycles executed by a single click of
  the *Multi Step* toolbar button.
* **Execution Delay (ms)** — delay inserted between successive
  internal batches of cycles during *Run All*. Increasing it slows
  down long runs so that the visual feedback (line highlighting,
  panel updates) can be followed in real time. The change is applied
  live, even mid-execution.
* **Pipeline Colors** — per-stage colours used by the *Pipeline*
  diagram. Each entry (``IF``, ``ID``, ``EX``, ``MEM``, ``WB``,
  ``FP Adder``, ``FP Multiplier``, ``FP Divider``, ``Stall``) can be
  edited with a colour picker, and the *Reset to defaults* button
  restores the original palette (the same RGB values the Swing UI
  uses by default).

The Cycles diagram
------------------
The **Cycles** region across the bottom of the window shows the
*temporal* behaviour of the pipeline: a diagram of which stage every
instruction occupied at every clock cycle. It mirrors the "Cycles"
window of the classic Swing desktop UI.

* each **row** is one instruction, in the order it was fetched, labelled
  on the left with its assembly text;
* each **column** is one CPU cycle, numbered along the top;
* each **cell** shows, with the same colour code as the *Pipeline*
  panel, the stage the instruction was in during that cycle: ``IF``,
  ``ID``, ``EX``, ``MEM``, ``WB`` for the integer pipeline; ``A1``–``A4``
  and ``M1``–``M7`` for the FP Adder and Multiplier; and ``DIV`` (with
  the per-cycle divider counter ``D00``–``D24``) for the FP Divider.

Stall cycles are drawn in the dedicated *Stall* colour and labelled with
the hazard that caused them (``RAW``, ``WAW`` and the structural-stall
tags ``StDiv`` / ``StEx`` / ``StFun`` / ``Str`` / ``StAdd`` / ``StMul``),
using exactly the same classification as the *Pipeline* panel. Because
the diagram is built from the same data the Swing "Cycles" window draws,
the web and desktop views never disagree on the history of a run.

The diagram scrolls to follow the most recent cycle as the program
advances; both scrollbars can be used to review earlier cycles or
instructions. Before a program has run, the region shows an empty grid.
For very long executions only the most recent cycles and instructions are
kept on screen (a note above the grid says so) to keep the browser
responsive.

Keyboard shortcuts
------------------
The following keyboard shortcuts are available at all times, unless a
dialog (Help, Settings, Input) is open.  All listed keys call
``preventDefault()`` so the browser's own default action (e.g. F10
menu bar, Esc) does not also fire.

.. list-table::
   :header-rows: 1
   :widths: 15 35 50

   * - Key
     - Action
     - Active when
   * - **F2**
     - Load program
     - Program has no syntax errors
   * - **F8**
     - Run All / Pause (toggle)
     - Run All: program loaded (READY); Pause: currently executing
   * - **F9**
     - Single Step
     - Program loaded (READY)
   * - **F10**
     - Multi Step
     - Program loaded (READY)
   * - **Esc**
     - Stop & reset CPU
     - Program loaded (READY)

Running EduMIPS64 as a desktop or CLI application
-------------------------------------------------
The web frontend is convenient because it requires no installation,
but EduMIPS64 is primarily distributed as a Java desktop application
that can also be run from the command line. The desktop JAR exposes
additional features (a richer settings dialog, the Dinero frontend
for cache trace analysis, CLI options for batch / headless execution,
a tracefile writer) that are documented in the full manual available
on `Read the Docs <https://edumips64.readthedocs.io/>`_.

To install the desktop application or run EduMIPS64 from the command
line, see the project's GitHub repository:

* Project page: https://www.edumips.org
* Source code, releases and installation instructions:
  https://github.com/EduMIPS64/edumips64

If you find a bug or want to suggest an improvement to the web
frontend, please open an issue on GitHub.
