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
The window is split into a top toolbar and two main areas:

* On the left, the **code editor** (a Monaco-based MIPS64 editor).
* On the right, a stack of collapsible panels showing the runtime
  state of the simulation: **Issues**, **Statistics**, **Pipeline**,
  **Registers**, **Memory**, **Standard Output**, **Cache
  Configuration** and **General Settings**.

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

* **Load** — parses the contents of the editor and loads the resulting
  program into the simulator. The button is disabled while the
  simulator is running, and is hidden once a program has been loaded
  successfully.

* **Single Step** — executes one CPU cycle.

* **Multi Step** — executes a configurable number of CPU cycles in a
  single click. The number of steps is shown in the button's tooltip
  and can be changed in the *General Settings* panel ("Multi Step
  Size").

* **Run All** — executes the program until it terminates with a
  ``SYSCALL 0`` (or equivalent) or a ``BREAK`` instruction, or until
  it is paused or stopped manually. Between batches of cycles the
  simulator can wait a configurable delay (``Execution Delay``) so
  that long runs remain visually observable.

* **Pause** — interrupts a running execution at the current cycle.
  Single Step / Multi Step / Run All can then be used to continue.

* **Stop** — halts the running execution and resets the CPU to the
  ``READY`` state, clearing registers, memory and pipeline.

* **Clear** — empties the code editor, leaving only an empty assembly
  skeleton (``.data`` and ``.code`` directives plus a final
  ``SYSCALL 0``). The Clear button is disabled while the CPU is
  running.

* **Open Code** — opens a local file (typically a ``.s`` file) and
  loads its contents into the editor.

* **Save Code** — saves the current contents of the editor to a local
  file named ``code.s``.

* **Help (?)** — opens this manual inside the application, with a
  navigation drawer on the left and a language selector that lets you
  switch between English, Italian and Chinese. The Help dialog also
  includes an *About* tab that shows the version of the simulator and
  a description of the running build.

Buttons that would have no effect in the current state are
automatically disabled. For example, ``Single Step``, ``Multi Step``
and ``Run All`` are disabled until a program has been loaded with
``Load``, and ``Pause`` is only available while a long execution is
in progress.

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
  for errors. The chips are hidden when there is nothing to report.

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
Shows which instruction is currently in each stage of the CPU
pipeline. The five integer stages (IF, ID, EX, MEM, WB) are always
displayed; the FPU stages (Adder, Multiplier, Divider) appear when
they are populated. Stages use the same colour code as the editor's
in-line highlight.

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
* **Multi Step Size** — number of cycles executed by a single click of
  the *Multi Step* toolbar button.
* **Execution Delay (ms)** — delay inserted between successive
  internal batches of cycles during *Run All*. Increasing it slows
  down long runs so that the visual feedback (line highlighting,
  panel updates) can be followed in real time. The change is applied
  live, even mid-execution.

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
