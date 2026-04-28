The interactive command line interface
======================================
In addition to the desktop GUI, the EduMIPS64 JAR can be run as an
interactive command line shell. The shell is meant for batch-style
experimentation with assembly programs (running test programs in a
script, scripted course material, automated grading, debugging from a
terminal, etc.) and offers the same simulator core as the desktop GUI,
just with a textual interface.

This chapter describes the *commands* available inside the shell. The
command line *options* used to launch the JAR (``--headless``,
``--file``, ``--verbose``, …) are documented in
:ref:`command-line-options`; in particular, the shell is reached by
launching the JAR with ``--headless`` (with or without ``--verbose``).

When ``--verbose`` is enabled, the shell prints additional informative
messages — a welcome banner on startup, "execution started" /
"execution finished" messages around ``run``, progress dots during long
runs, parser warnings after ``load``, and so on. Without ``--verbose``
the shell produces only the program output (e.g. the strings printed
by ``SYSCALL 5``) and the explicit replies of ``show``, ``config`` and
similar commands. The quiet default is convenient when EduMIPS64 is
embedded in a pipeline of other tools.

The prompt
----------
After startup, the shell prints a ``>`` prompt and waits for a
command. Commands are read one line at a time and tokenized at
whitespace. Pressing Enter on an empty line reprints the help. The
shell loop runs until the ``exit`` command is issued (or until
``Ctrl+D`` / EOF is sent on standard input).

Every command accepts ``-h`` / ``--help``, which prints its specific
usage. The top-level ``help`` command lists every available command
together with a short description; this is the easiest way to discover
the shell's capabilities.

Available commands
------------------
The shell exposes a small number of commands. They map directly to the
simulator's lifecycle: load a source file, single-step or run it,
inspect the resulting state, and optionally dump a Dinero trace file
for cache analysis.

load
~~~~
Provide a new file to execute::

    > load path/to/program.s

``load`` parses the given file and prepares the simulator for
execution. On success the CPU enters the ``RUNNING`` state, ready to
be advanced with ``step`` or ``run``.

If the parser reports errors the file is not loaded and the error
description is printed. If only warnings are produced, the file is
still loaded — matching the behaviour of the GUI; with ``--verbose``
the warnings are also printed.

A new ``load`` cannot be issued while a previous program is still
running; use ``reset`` first to bring the CPU back to a loadable
state.

step
~~~~
Make the CPU state machine advance N cycles::

    > step          # advances by 1 cycle (default)
    > step 10       # advances by 10 cycles

After every cycle the contents of the pipeline are printed, so
``step`` is the canonical way to follow the execution one instruction
at a time and observe how instructions traverse IF / ID / EX / MEM /
WB and the FPU stages.

If the program reaches its end (``SYSCALL 0`` or ``BREAK``) before all
the requested cycles have been executed, ``step`` stops at that point
and prints the corresponding message.

run
~~~
Execute the program without intervention::

    > run

The simulator advances until the program terminates with ``SYSCALL 0``
(or equivalent) or with a ``BREAK`` instruction. With ``--verbose``,
the shell prints a *start* / *end* banner around the run and a
progress dot every thousand cycles, plus a final summary with the
total number of executed cycles and the wall-clock time it took. Use
``run`` to obtain the program's output (``SYSCALL 4`` / ``SYSCALL 5``)
and final state quickly, then ``show`` to inspect the result.

show
~~~~
Inspect the state of the simulated CPU. ``show`` is a group of
sub-commands; each one prints a different aspect of the simulator
state to standard output.

* ``show registers`` — prints all 32 integer general-purpose registers
  (``R0``–``R31``) with their current value.
* ``show register N`` — prints the content of integer register
  ``N`` (``0 ≤ N ≤ 31``).
* ``show fps`` — prints all 32 floating-point registers
  (``F0``–``F31``).
* ``show fp N`` — prints the content of floating-point register
  ``N`` (``0 ≤ N ≤ 31``).
* ``show fcsr`` — prints the Floating-Point Control and Status
  Register.
* ``show hi`` / ``show lo`` — print the contents of the special
  ``HI`` and ``LO`` registers used by multiply / divide instructions.
* ``show memory`` — prints the contents of the simulated main memory.
* ``show symbols`` — prints the symbol table (labels declared in the
  ``.data`` and ``.code`` sections together with their addresses).
* ``show pipeline`` — prints which instruction is currently in each
  stage of the pipeline.

Calling ``show`` without a sub-command prints the list of available
sub-commands.

dinero
~~~~~~
Write a Dinero tracefile to a file::

    > dinero trace.xdin

This produces a textual trace of the memory accesses performed by the
program in the format expected by the Dinero IV cache simulator,
suitable for offline cache analysis. ``dinero`` can be issued at any
point during execution; the tracefile reflects the accesses observed
so far.

config
~~~~~~
Print the current configuration values::

    > config

This is the same set of preferences that the desktop GUI exposes in
the *Settings* dialog (forwarding on/off, tracefile path, cache
parameters, behavioural options, …). It is read-only — the shell
prints the current values and exits the command. The values are
loaded from the same configuration store used by the GUI, so changes
made in the GUI are visible from the shell and vice versa.

reset
~~~~~
Reset the CPU state machine::

    > reset

``reset`` re-initializes memory, registers, the symbol table, the I/O
manager, the cache simulator and the parser, bringing the CPU back to
the ``READY`` state. Issue ``reset`` to load a different program after
the current one has been loaded, or to start a program over from
scratch after a partial run.

help
~~~~
Show the list of available commands together with a short description
of each one. ``help`` is the entry point for discovering what the
shell can do; combine it with the per-command ``-h`` / ``--help``
option to see the parameters accepted by each sub-command.

exit
~~~~
Quit the shell and terminate the JVM. The same effect can be achieved
by sending ``Ctrl+D`` (EOF) on standard input.

A typical session
-----------------
Putting the commands together, a typical interactive session looks
like this::

    $ java -jar edumips64.jar --headless --verbose
    > load examples/hello.s
    File loaded: /…/examples/hello.s
    > step 5
    … (5 cycles of pipeline contents)
    > run
    Hello, world!
    Execution finished in 42 cycles, 3 ms
    > show registers
    … (R0..R31)
    > show pipeline
    … (final pipeline state)
    > dinero hello.xdin
    > reset
    > load examples/sum.s
    > run
    > exit

Standard input from ``SYSCALL 3`` is read directly from the terminal,
so programs that ask for user input work transparently in the shell.

Scripting tips
--------------
Because the shell reads commands from standard input one per line, a
script can be fed in directly::

    $ java -jar edumips64.jar --headless < session.txt

A ``session.txt`` containing for example::

    load examples/hello.s
    run
    show registers
    exit

will load the program, run it, print the final register file and
quit. Combined with ``--verbose`` and shell redirection, this makes
it straightforward to integrate EduMIPS64 in automated test suites or
to capture reproducible traces of an execution.
