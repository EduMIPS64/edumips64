The user interface
==================
The GUI of EduMIPS64 is inspired to WinMIPS64 user interface. In fact, the
main window is identical, except for some menus.

.. Please refer to chapter~\ref{mips-simulators} for an overview of some MIPS
   and DLX simulators (including WinMIPS64), and to \cite{winmips-web} for more
   information about WinMIPS64. %In figure~\ref{fig:edumips-main} you can see
   the main window of EduMIPS64, composed by

The EduMIPS64 main window is composed by a menu bar and six frames, showing
different aspects of the simulation. There's also a status bar, that has the
double purpose to show the content of memory cells and registers when you
click them and to notify the user that the simulator is running when the
simulation has been started but verbose mode is not selected.

The status bar also shows the CPU status. It can show one of the following four
states:

* *READY* The CPU hasn't executed any instructions (no program is loaded).
* *RUNNING* The CPU is executing a series of instructions.
* *STOPPING* The CPU has found a termination instruction, and is executing the
  instructions that are already in the pipeline before terminating the
  execution.
* *HALTED* The CPU is stopped: a program just finished running.

Note that the CPU status is different from the simulator status. The
simulator may execute a number of CPU cycles and then stop executing,
allowing the user to inspect memory and registers: in this state, between CPU
cycles, the CPU stays in *RUNNING* or *STOPPING* state. Once the CPU reaches
the *HALTED* state, the user cannot run any CPU cycle without loading a
program again (the same program, or a different one).

There are more details in the following sections.

The menu bar
------------
The menu bar contains six menus:

File
~~~~
The File menu contains menu items about opening files, resetting or shutting
down the simulator, writing trace files.

* *Open...* Opens a dialog that allows the user to choose
  a source file to open.

* *Open recent* Shows the list of the recent files opened by the
  simulator, from which the user can choose the file to open

* *Reset* Resets the simulator, keeping open the file that was
  loaded but resetting the execution.

* *Write Dinero Tracefile...* Writes the memory access data to a
  file, in xdin format.

* *Exit* Closes the simulator.

The *Write Dinero Tracefile...* menu item is only available when a whole
source file has been executed and the end has been already reached.

Execute
~~~~~~~
The Execute menu contains menu items regarding the execution flow of the
simulation.

* *Single Cycle* Executes a single simulation step

* *Run* Starts the execution, stopping when the simulator reaches
  a `SYSCALL 0` (or equivalent) or a `BREAK` instruction, or
  when the user clicks the Stop menu item (or presses F9).

* *Multi Cycle* Executes some simulation steps. The number of
  steps executed can be configured through the Setting dialog.

.. See~\ref{dialog-settings} for more details.

* *Stop* Stops the execution when the simulator is in "Run"
  or "Multi cycle" mode, as described previously.

This menu is only available when a source file is loaded and the end of the
simulation is not reached. The *Stop* menu item is available only in
"Run" or "Multi Cycle" mode.

Note that the simulator slows down when updating the UI. If you want to
execute long (thousands of cycles) programs quickly, disable the "Sync
graphics with CPU in multi-step execution" option.

Configure
~~~~~~~~~
The Configure menu provides facilities for customizing EduMIPS64 appearance and
behavior.

* *Settings...* Opens the Settings dialog, described
  in the next sections of this chapter;

* *Change Language* Allows the user to change the language used
  by the user interface. Currently only English and Italian are supported.
  This change affects every aspect of the GUI, from the title of the frames to
  the online manual and warning/error messages.

The `Settings...` menu item is not available when the simulator is in
"Run" or "Multi Cycle" mode, because of potential race conditions.

Tools
~~~~~
This menu contains only an item, used to invoke the Dinero Frontend dialog.

* *Dinero Frontend...* Opens the Dinero Frontend dialog.

This menu is not available until you have not executed a program and the
execution has reached its end.

Window
~~~~~~
This menu contains items related to operations with frames.

* *Tile* Sorts the visible windows so that no more that three
  frames are put in a row. It tries to maximize the space occupied by every
  frame.

The other menu items simply toggle the status of each frame, making them
visible or minimizing them.

Help
~~~~
This menu contains help-related menu items.

* *Manual...* Shows the Help dialog.

* *About us...* Shows a cute dialog that contains the names of
  the project contributors, along with their roles.

Frames
------
The GUI is composed by seven frames, six of which are visible by default, and
one (the I/O frame) is hidden.

Cycles
~~~~~~
The Cycles frame shows the evolution of the execution flow during time,
showing for each time slot which instructions are in the pipeline, and in
which stage of the pipeline they are located.

Registers
~~~~~~~~~
The Registers frame shows the content of each register. By left-clicking on
them you can see in the status bar their decimal (signed) value, while
double-clicking on them will pop up a dialog that allows the user to change
the value of the register.

Statistics
~~~~~~~~~~
The Statistics frame shows some statistics about the program execution.

Note that during the last execution cycle the cycles counter is not
incremented, because the last execution cycle is not a full CPU cycle but
rather a pseudo-cycle whose only duties are to remove the last instruction
from the pipeline and increment the counter of executed instructions.

The Statistics frame also displays L1 cache statistics when the cache 
simulator is enabled:

* **L1I Reads** - Number of read accesses to the L1 instruction cache
* **L1I Read Misses** - Number of read misses in the L1 instruction cache
* **L1D Reads** - Number of read accesses to the L1 data cache
* **L1D Read Misses** - Number of read misses in the L1 data cache
* **L1D Writes** - Number of write accesses to the L1 data cache
* **L1D Write Misses** - Number of write misses in the L1 data cache

These statistics help analyze cache performance and understand memory access
patterns in your program. Cache misses indicate when the processor needs to
access slower main memory instead of the faster cache.

Pipeline
~~~~~~~~
The Pipeline frame shows the actual status of the pipeline, showing which
instruction is in which pipeline stage. Different colors highlight different
pipeline stages.

Memory
~~~~~~
The Memory frame shows memory cells content, along with labels and comments
taken from the source code. Memory cells content, like registers, can be
modified double-clicking on them, and clicking on them will show their
decimal value in the status bar.
The first column shows the hexadecimal address of the memory cell, and the
second column shows the value of the cell. Other columns show additional info
from the source code.

Code
~~~~
The Code window shows the instructions loaded in memory. The first column shows
the address of the instruction, while the second column shows the hexadecimal
representation of the instructions. Other columns show additional info taken
from the source code.

Input/Output
~~~~~~~~~~~~
The Input/Output window provides an interface for the user to see the output
that the program creates through the SYSCALLs 4 and 5. Actually it is not
used for input, as there's a dialog that pops up when a SYSCALL 3 tries to
read from standard input, but future versions will include an input text box.

Dialogs
-------
Dialogs are used by EduMIPS64 to interact with the user in many ways. Here's a
summary of the most important dialogs:

Settings
~~~~~~~~
In the Settings dialog various aspects of the simulator can be configured.
Clicking on the "OK" button will cause the options to be saved, while clicking
on "Cancel" (or simply closing the window) will cause the changes to be
ignored. Don't forget to click "OK" if you want to save your changes.

The Main Settings tab allow to configure forwarding and the number of steps
in the Multi Cycle mode.

The Behavior tab allow to enable or disable warnings during the parsing phase,
the "Sync graphics with CPU in multi-step execution" option, when checked,
will synchronize the frames' graphical status with the internal status of the
simulator. This means that the simulation will be slower, but you'll have an
explicit graphical feedback of what is happening during the simulation. If this
option is checked, the "Interval between cycles" option will influence how
many milliseconds the simulator will wait before starting a new cycle.
Those options are effective only when the simulation is run using the
"Run" or the "Multi Cycle" options from the Execute menu.

The last two options set the behavior of the simulator when a synchronous
exception is raised. If the "Mask synchronous exceptions" option is checked,
the simulator will ignore any Division by zero or Integer overflow exception.
If the "Terminate on synchronous exception" option is checked, the simulation
will be halted if a synchronous exception is raised. Please note that if
synchronous exceptions are masked, nothing will happen, even if the
termination option is checked. If exceptions are not masked and the
termination option is not checked, a dialog will pop out, but the simulation
will go on as soon as the dialog is closed. If exceptions are not masked and
the termination option is checked, the dialog will pop out, and the
simulation will be stopped as soon as the dialog is closed.

The last tab allows to change the appearance of the user interface. There are
options to change the colors associated to the different pipeline stages, an
option to choose whether memory cells are shown as long or double values and
an option to set the UI font size.

The Cache tab allows you to configure the L1 cache simulator settings:

* **L1 Data Cache Size** - Size of the L1 data cache in bytes (default: 1024)
* **L1 Instruction Cache Size** - Size of the L1 instruction cache in bytes (default: 1024)
* **L1 Data Cache Block Size** - Size of each cache block in bytes (default: 16)
* **L1 Instruction Cache Block Size** - Size of each cache block in bytes (default: 16)
* **L1 Data Cache Associativity** - Number of cache ways, determining cache organization (default: 1)
* **L1 Instruction Cache Associativity** - Number of cache ways, determining cache organization (default: 1)
* **L1 Data Cache Penalty** - Number of additional cycles for cache misses (default: 40)
* **L1 Instruction Cache Penalty** - Number of additional cycles for cache misses (default: 40)

These settings allow you to experiment with different cache configurations to
understand their impact on program performance. The cache simulator models
separate L1 data and instruction caches, which is common in modern processors.

Note that the UI scaling with font size is far from perfect, but it should be
enough to make the simulator usable with high-resolution displays (e.g., 4k).

L1 Cache Simulator
~~~~~~~~~~~~~~~~~~
EduMIPS64 includes an integrated L1 cache simulator that models the behavior
of separate instruction and data caches. The cache simulator operates
automatically during program execution and provides detailed statistics about
cache performance.

The cache simulator models:

* **L1 Instruction Cache** - Caches instruction fetches from memory
* **L1 Data Cache** - Caches data reads and writes from memory operations

Each cache can be independently configured with different parameters:

* **Size** - Total capacity of the cache in bytes
* **Block Size** - Size of each cache line in bytes (also called cache block)
* **Associativity** - Number of ways in the cache set (1 = direct-mapped, >1 = set-associative)
* **Penalty** - Additional CPU cycles incurred on cache misses

The cache simulator uses a Least Recently Used (LRU) replacement policy for
set-associative caches. Cache statistics are updated in real-time during
execution and displayed in the Statistics frame.

Cache performance can significantly impact program execution time, especially
for programs with poor memory locality. Use the cache simulator to:

* Analyze memory access patterns in your programs
* Experiment with different cache configurations
* Understand the performance impact of cache misses
* Learn about cache behavior in real processors

The cache configuration can be changed through the Settings dialog (Cache tab)
and takes effect when the simulation is reset.

Dinero Frontend
~~~~~~~~~~~~~~~
The Dinero Frontend dialog allows to feed a DineroIV process with the trace
file internally generated by the execution of the program. In the first text
box there is the path of the DineroIV executable, and in the second one there
must be the parameters of DineroIV.

.. % Please see~\cite{dinero-web} for further informations about the DineroIV
   cache simulator.

The lower section contains the output of the DineroIV process, from which you
can take the data that you need.

Help
~~~~
The Help dialog brings up the on-line manual, which is an HTML copy of this
document.

Command line options
--------------------
Four command line options are available. They are described in the following
list, with the long name enclosed in round brackets. Long and short names can
be used in the same way.

* `-v (--version)` prints the simulator version and exits.

* `-h (--help)` prints a help message with a brief summary of command line
  options, then exits.

* `-f (--file) filename` opens `filename` in the simulator

* `-r (--reset)` resets the stored configuration to the default values

* `-d (--debug)` enters Debug mode

* `-hl (--headless)` Runs EduMIPS64 in headless mode (no gui)

The `--debug` flag has the effect to activate Debug mode. In this mode, a new
frame is available, the Debug frame, and it shows the log of internal
activities of EduMIPS64. It is not useful for the end user, it is meant to be
used by EduMIPS64 developers.