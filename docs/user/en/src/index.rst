.. EduMIPS64 documentation master file, created by
   sphinx-quickstart on Tue Apr 26 23:10:10 2011.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to the EduMIPS64 documentation!
=======================================

EduMIPS64 is a MIPS64 Instruction Set Architecture (ISA) simulator. It is
designed to be used to execute small programs that use the subset of the
MIPS64 ISA implemented by the simulator, allowing the user to see how
instructions behave in the pipeline, how stalls are handled by the CPU, the
status of registers and memory and much more. It is both a simulator and a
visual debugger.

The website for the project is http://www.edumips.org, and the code is hosted
at http://github.com/EduMIPS64/edumips64. If you find any bugs, or have any
suggestion for improving the simulator, please open an issue on github or send
an email at bugs@edumips.org.

EduMIPS64 is developed by a group of students of the University of Catania
(Italy), and started as a clone of WinMIPS64, even if now there are lots of
differences between the two simulators.

This manual will introduce you to EduMIPS64, and will cover some details on
how to use it.

This manual describes EduMIPS64 version |version|.

This manual is split into two parts. The first part is independent from the
user interface in use and covers the source file format, the supported
instruction set, the Floating Point Unit and a set of example programs.
The second part documents the user interfaces: a chapter for the desktop
(Swing) application, which also includes the command line options of the
JAR, and a chapter for the web frontend.

When this manual is opened from inside the running application, only the
chapter that is relevant for the active user interface is shown. The full
manual (with both UI chapters) is available on
`Read the Docs <https://edumips64.readthedocs.io/>`_ and as a PDF.

.. toctree::
   :maxdepth: 2

   source-files-format
   instructions
   fpu
   examples

.. only:: not web

   .. toctree::
      :maxdepth: 2

      user-interface-swing

.. only:: not swing

   .. toctree::
      :maxdepth: 2

      user-interface-web

.. Indices and tables
   ==================

   * :ref:`genindex`
   * :ref:`modindex`
   * :ref:`search`

