.. EduMIPS64 documentation master file, created by
   sphinx-quickstart on Tue Apr 26 23:10:10 2011.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to the EduMIPS64 documentation!
=======================================

EduMIPS64 is a MIPS64 Instruction Set Architecture (ISA) simulator. It is designed to
be used to execute small programs that use the subset of the MIPS64 ISA
implemented by the simulator, allowing the user to see how instructions behave
in the pipeline, how stalls are handled by the CPU, the status of registers and
memory and much more. It is both a simulator and a visual debugger.

EduMIPS64 is developed by a group of students of the University of Catania (Italy),
and started as a clone of WinMIPS64, even if now there are lots of differences
between the two simulators.

This manual will introduce you to EduMIPS64, and will cover some details on how to
use it.

The first chapter of this manual covers the format of source files accepted by
the simulator, describing the data types and the directives, in addition to
command line parameters. In the second chapter there's an overview of the subset
of the MIPS64 instruction set that is accepted by EduMIPS64, with all the needed
parameters and indications to use them. The third chapter is a description of
the user interface of EduMIPS64, that explains the purpose of each frame and menu,
along with a description of the configuration dialog, the Dinero frontend
dialog, the Manual dialog and command line options. The fourth chapter contains some useful examples.

This manual refers to EduMIPS64 version 0.5.3.

.. toctree::
   :maxdepth: 2

   source-files-format
   instructions
   user-interface

.. Indices and tables
   ==================
  
   * :ref:`genindex`
   * :ref:`modindex`
   * :ref:`search`
 
