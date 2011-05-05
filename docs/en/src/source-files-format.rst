Source files format
===================

EduMIPS64 tries to follow the conventions used in other MIPS64 and DLX simulators, so
that old time users will not be confused by its syntax.

There are two sections in a source file, the *data* section and the
*code* section, introduced respectively by the *.data* and the
*.code* directives. In the following listing you can see a very basic
EduMIPS64 program::
  ; This is a comment
          .data
  label:  .word   15     ; This is an inline comment

          .code
          daddi   r1, r0, 0
          syscall 0


To distinguish the various parts of each source code line, any combination of
spaces and tabs can be used, as the parser ignores multiple spaces and only
detects whitespaces to separate tokens.

Comments can be specified using the ";" character, everything that follows
that character will be ignored. So a comment can be used "inline" (after the
directive) or on a row by itself.

Labels can be used in the code to reference a memory cell or an
instruction.  They are case insensitive. Only a label for each source code line
can be used.  The label can be specified one or more rows above the effective
data declaration or instruction, provided that there's nothing, except for
comments and empty lines, between the label and the declaration. 

The `.data` section
-------------------
The *data* section contains commands that specify how the memory must be
filled before program execution starts. The general form of a `.data` command
is::
  [label:] .datatype value1 [, value2 [, ...]]

EduMIPS64 supports different data types, that are described in the following
table.

            =========== ==================== =============
            Type        Directive            Bits required
            =========== ==================== =============
            Byte        `.byte`              8
            Half word   `.word16`            16
            Word        `.word32`            32
            Double Word `.word` or `.word64` 64
            =========== ==================== =============

Please note that a double word can be introduced either by the `.word`
directive or by the `.word64` directive.

There is a big difference between declaring a list of data elements
using a single directive or by using multiple directives of the same type. EduMIPS64
starts writing from the next 64-bit double word as soon as it finds a datatype identifier,
so the first `.byte` statement in the following listing will put
the numbers 1, 2, 3 and 4 in the space of 4 bytes, taking 32 bits, while code in
the next four rows will put each number in a different memory cell, occupying 32
bytes::

    .data
    .byte    1, 2, 3, 4
    .byte    1
    .byte    2
    .byte    3
    .byte    4

In the following table, the memory is represented using byte-sized cells
and each row is 64 bits wide. The address on the left side of each row of the
table refers to the right-most memory cell, that has the lowest address of the
eight cells in each line.

+----+-+-+-+-+-+-+-+-+
|*0* |0|0|0|0|4|3|2|1|   
+----+-+-+-+-+-+-+-+-+
|*8* |0|0|0|0|0|0|0|1|
+----+-+-+-+-+-+-+-+-+
|*16*|0|0|0|0|0|0|0|2|
+----+-+-+-+-+-+-+-+-+
|*24*|0|0|0|0|0|0|0|3|
+----+-+-+-+-+-+-+-+-+
|*36*|0|0|0|0|0|0|0|4|
+----+-+-+-+-+-+-+-+-+

There are some special directives that need to be discussed: `.space`, 
`.ascii` and `.asciiz`.

The `.space` directive is used to leave some free space in memory. It
accepts as a parameter an integer, that indicates the number of bytes that must
be left empty. It is handy when you must save some space in memory for the
results of your computations.

The `.ascii` directive accepts strings containing any of the ASCII
characters, and some special C-like escaping sequences, that are described in
the following table, and puts those strings in memory.


        ================= =========================== ==========
        Escaping sequence Meaning                     ASCII code
        ================= =========================== ==========
        \\0               Null byte                   0
        \\t               Horizontal tabulation       9
        \\n               Newline character           10
        \\"               Literal quote character     34
        \\\\              Literal backslash character 92
        ================= =========================== ==========

The `.asciiz` directive behaves exactly like the `.ascii` command,
with the difference that it automatically ends the string with a null byte.

The `.code` section
-------------------
The *code* section contains commands that specify how the memory must be
filled when the program will start. The general form of a `.code` command
is::
  [label:] instruction [param1 [, param2 [, param3]]]

The *code* section can be specified with the `.text` alias.

The number and the type of parameters depends on the instruction itself.

.. %TODO: questa va sicuramente inserita.
   %Please see table~\ref{table:segm-type} for the list of possible parameters.

Instructions can take three types of parameters:

* *Registers* a register parameter is indicated by an uppercase
  or lowercase "r", or a $, followed by the number of the register (between
  0 and 31), as in "r4", "R4" or "\$4";
* *Immediate values* an immediate value can be a number or a
  label; the number can be specified in base 10 or in base 16: base 10 numbers
  are simply inserted by writing the number, while base 16 number are inserted
  by putting before the number the prefix "0x"
* *Address* an address is composed by an immediate value followed
  by a register name enclosed in brackets. The value of the register will be 
  used as base, the value of the immediate will be the offset.

The size of immediate values is limited by the number of bits that are available
in the bit encoding of the instruction. 

You can use standard MIPS assembly aliases to address the first 32 registers,
appending the alias to one of the standard register prefixes like "r", "\$"
and "R". See the next table.

            ======== ======
            Register Alias
            ======== ======
            0        `zero`
            1        `at`
            2        `v0`
            3        `v1`
            4        `a0`
            5        `a1`
            6        `a2`
            7        `a3`
            8        `t0`
            9        `t1`
            10       `t2`
            11       `t3`
            12       `t4`
            13       `t5`
            14       `t6`
            15       `t7`
            16       `s0`
            17       `s1`
            18       `s2`
            19       `s3`
            20       `s4`
            21       `s5`
            22       `s6`
            23       `s7`
            24       `t8`
            25       `t9`
            26       `k0`
            27       `k1`
            28       `gp`
            29       `sp`
            30       `fp`
            31       `ra`
            ======== ======

.. % TODO: anche questa, ma nell'indice
   %Please see~\cite{mips-2} for more details about how instruction are actually encoded.

.. The instructions that can be used in this section will be discussed in section~\ref{instructions}

The `\#include` command
-----------------------
Source files can contain the `\#include filename` command, which has the
effect of putting in place of the command row the content of the file
`filename`.
It is useful if you want to include external routines, and it comes with a
loop-detection algorithm that will warn you if you try to do something like
"`\#include A.s`" in file `B.s` and "`\#include B.s`" in file `A.s`.

