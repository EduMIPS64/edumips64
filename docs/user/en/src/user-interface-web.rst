The web user interface
======================
EduMIPS64 is also available as a web application that runs entirely in the
browser. The simulator core is cross-compiled from Java to JavaScript and
runs as a Web Worker, while the user interface is built with React. The
production deployment is hosted at https://web.edumips.org.

This chapter describes the web frontend. For source files format, the
instruction set, the FPU and example programs, refer to the other
chapters of this manual: those are independent of the user interface.

Quick start
-----------
After loading or typing a program in the code editor, use the toolbar to
step through the simulation, run it, or reset it. Hover over any
instruction to see information about it: the address, its binary
representation, the opcode and (if it is currently in the pipeline) the
CPU stage in which the instruction is in the current step. CPU stages
are also encoded by colors.

Layout
------
The web frontend is organized in panels that show different aspects of
the running simulation:

* **Code** — the program loaded in memory, with the address and
  hexadecimal representation of each instruction.
* **Registers** — the contents of the integer and floating point
  registers.
* **Memory** — the contents of memory cells.
* **Pipeline** — the instructions currently in each stage of the CPU
  pipeline. Stages are color coded.
* **Statistics** — counters about the program execution (cycles
  executed, instructions executed, stalls, etc.).

Loading and running programs
----------------------------
Programs can be typed directly into the code editor, or loaded from one
of the bundled samples. The simulator supports the same source file
format described in :doc:`source-files-format`.

Once a program is loaded, you can:

* Execute a single CPU cycle (single step).
* Run the program until it terminates with a ``SYSCALL 0`` (or
  equivalent) or a ``BREAK`` instruction.
* Reset the simulator to the initial state, keeping the program loaded.

Help and language
-----------------
The Help dialog (the question mark icon in the top bar) opens this
manual inside the application, with a navigation drawer on the left and
a language selector that lets you switch between English, Italian and
Chinese.

The "About" tab of the Help dialog shows the version of the simulator
and a description of the running build (production, per-PR preview, or
local development).

Running EduMIPS64 as a desktop or CLI application
-------------------------------------------------
The web frontend is convenient because it requires no installation, but
EduMIPS64 is primarily distributed as a Java desktop application that
can also be run from the command line. The desktop JAR exposes
additional features (configurable settings dialog, the L1 cache
simulator, the Dinero frontend, and CLI options for batch / headless
execution) that are documented in the full manual available on
`Read the Docs <https://edumips64.readthedocs.io/>`_.

To install the desktop application or run EduMIPS64 from the command
line, see the project's GitHub repository:

* Project page: https://www.edumips.org
* Source code, releases and installation instructions:
  https://github.com/EduMIPS64/edumips64

If you find a bug or want to suggest an improvement to the web
frontend, please open an issue on GitHub.
