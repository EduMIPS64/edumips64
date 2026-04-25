The pipeline and forwarding
===========================

EduMIPS64 models the classic 5-stage MIPS integer pipeline described in
Appendix C (*Pipelining: Basic and Intermediate Concepts*) of Hennessy &
Patterson, *Computer Architecture: A Quantitative Approach* (the appendix is
numbered "A" in some earlier editions). The stages are:

* **IF** — Instruction Fetch
* **ID** — Instruction Decode / register fetch
* **EX** — Execute / effective address calculation
* **MEM** — Memory access
* **WB** — Write-Back

At any given cycle, up to five instructions can be in flight, one per stage.

Data hazards
------------
A *Read-After-Write* (RAW) hazard occurs when an instruction tries to read a
register before a preceding, still in-flight instruction has written its
result. EduMIPS64 detects RAW hazards in the **ID** stage of the consuming
instruction by checking a per-register write semaphore that is set when the
producing instruction locks its destination register (also in ID) and cleared
when the value is finally available. When a hazard is detected the pipeline
inserts one or more *bubbles* in EX and counts them as **RAW stalls** in the
statistics frame.

A *Write-After-Write* (WAW) hazard can only occur on the FPU, because the FPU
pipeline is non-uniform (different functional units have different latencies)
and two instructions may reach WB out of program order. WAW hazards are
detected in ID and counted separately as **WAW stalls**.

Forwarding
----------
Forwarding (also known as *bypassing*) is a hardware technique that makes the
result of an instruction available to dependent instructions before it is
written back to the register file, reducing the number of stalls caused by
RAW hazards. This is the standard technique described in Hennessy &
Patterson, Appendix C, Section C.2 ("The Major Hurdle of Pipelining — Pipeline
Hazards"), specifically under "Minimizing Data Hazard Stalls by Forwarding".

In EduMIPS64 forwarding can be enabled or disabled from the *Main Settings*
tab of the *Settings* dialog (see :doc:`user-interface`). When enabled, the
simulator bypasses the value directly from the producing stage (EX output,
MEM output, or WB) to the consuming stage (typically the next instruction's
EX, or a branch's ID), as long as the producer is already far enough ahead in
the pipeline. When disabled, every RAW dependency must wait until the
producer's WB stage completes before the consumer can read the value in ID.

The two behaviors can be compared by loading the same program twice, once
with forwarding on and once with forwarding off, and looking at the cycles
counter and the RAW stalls counter in the *Statistics* frame.

ALU-to-ALU dependencies
~~~~~~~~~~~~~~~~~~~~~~~
Consider the canonical example from Hennessy & Patterson, Appendix C,
Figure C.5 (approximately page C-16):

.. code-block:: text

    DADD  R1, R2, R3
    DSUB  R4, R1, R5
    AND   R6, R1, R7
    OR    R8, R1, R9
    XOR   R10, R1, R11

All four instructions following ``DADD`` depend on ``R1``.

* **Without forwarding**: ``DSUB`` would need to stall in ID until ``DADD``
  has written back ``R1``, i.e. for two cycles. ``AND`` would need to stall
  for one cycle. ``OR`` is fine only because the register file is assumed to
  be written in the first half of the clock and read in the second half
  (equivalent to "internal" forwarding inside the register file).
* **With forwarding**: the value of ``R1`` computed at the end of ``DADD``'s
  EX stage is forwarded to the EX stage of ``DSUB`` (and of ``AND``, ``OR``,
  ``XOR``). No stalls are needed for ALU-to-ALU chains.

This example is shipped as ``forwarding-hp-pA16.s`` in the test fixtures.

Load-to-use dependencies
~~~~~~~~~~~~~~~~~~~~~~~~
Forwarding cannot remove every stall. Consider (Hennessy & Patterson,
Appendix C, Figure C.7, approximately page C-18):

.. code-block:: text

    DADD R1, R2, R3
    LD   R4, 0(R1)
    SD   R4, 8(R1)

``LD`` produces ``R4`` at the end of MEM, but ``SD`` would need ``R4`` at the
start of its own MEM stage. Even with forwarding, one bubble must be inserted
between the load and the dependent instruction. This is commonly called a
*load-use* hazard and is visible in EduMIPS64 as a single RAW stall when
forwarding is enabled. This example is shipped as ``forwarding-hp-pA18.s``.

Branches and the ID stage
~~~~~~~~~~~~~~~~~~~~~~~~~
The MIPS integer pipeline resolves conditional branches in the **ID** stage
(see Hennessy & Patterson, Appendix C, Section C.2, "Branch Hazards"), by
comparing the source registers as soon as they are read. This has an
important consequence on RAW hazards involving branches:

.. code-block:: text

    SLT   R1, R2, R4
    BEQZ  R1, finish

``BEQZ`` reads ``R1`` in its ID stage, which runs in the same clock cycle as
``SLT``'s EX stage. Forwarding from EX to ID is **not** possible, because the
ALU result is produced at the *end* of the EX stage, which is also when ID
finishes. Therefore **at least one stall is required** between an ALU
instruction and a branch that depends on its result, even with forwarding
enabled. A load followed by a dependent branch requires two stalls for the
same reason.

EduMIPS64 detects these cases and counts them in the RAW stalls statistic.

Further reading
---------------
The definitive treatment of data hazards, forwarding and branch hazards in
the 5-stage MIPS pipeline is Appendix C of:

   John L. Hennessy and David A. Patterson, *Computer Architecture: A
   Quantitative Approach*, Morgan Kaufmann. (The same appendix is labelled
   "A" in earlier editions of the book.)

The test programs ``forwarding.s``, ``forwarding-hp-pA16.s`` and
``forwarding-hp-pA18.s`` under ``src/test/resources/`` reproduce the
examples from that appendix and are a good starting point for experimenting
with the simulator.
