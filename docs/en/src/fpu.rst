Floating Point Unit
===================

This chapter [#]_ describes the Floating Point Unit (FPU) emulated in
EduMIPS64.

In the first paragraph we introduce the double format, the special floating
point values defined in the IEEE 754 standard and the exceptions that floating
point computations can raise.

In the second paragraph we explain how EduMIPS64 allows users to enable or
disable the IEEE floating point traps.

In the third paragraph we describe how double precision numbers and special
values can be specified in the source programs. 

In the fourth paragraph, we introduce the FCSR register, used by the FPU to
represent its state. It contains information about rounding, the boolean
results of comparison operations and the policies for handling IEEE floating
point exceptions.

In the fifth and last paragraph, we present all the MIPS64 floating point
instructions that have been implemented in EduMIPS64.

Before starting the discussion about the FPU, we define the domain of floating
point double precision numbers as [-1.79E308,-4.94E-324] ⋃  {0} ⋃
[4.94E-324,1.79E308].

.. [#] This chapter is part of the Bachelor's degree thesis by Massimo Trubia:
       "Progetto e implementazione di un modello di Floating Point Unit per un
       simulatore di CPU MIPS64".

.. _special-values:

Special values
--------------
Floating point arithmetics allows the programmer to choose whether to stop the
computation or not, if invalid operations are carried on. In this scenario,
operations like the division between zeroes or square roots of negative
numbers must produce a result that, not being a number (NaN) is treated as
somehting different.

.. _nan:

NaN or Invalid Operation
************************
The IEEE Standard for Floating-Point Arithmetic (IEEE 754) defined that
invalid arithmetic operations can either signal the error while the program is
running (using a trap for the IEEE exception **Invalid Operation**) or return
as a result the special value QNan (Quit Not a Number). Another NaN value,
that inconditionally raises the same trap once it is detected as being one of
the operands, is SNan (Signalling Not a Number). This value is seldom used in
applications, and historically it has been used to initialize variables.

.. _zeroes:

Zeroes or Underflows
********************
Another special value defined by the standard is zero. Since the double format
does not include the zero in its domain, it is considered a special value.
There is a positive zero and a negative zero: the former is used when a
representation of a negative number ∈ ]-4.94E-324,0[) is attempted, and a
result is required (as opposed to allowing an **Underflow** trap), while the
latter is used when the number that should be represented is ∈  [0,4.94E-324[,
and the Underflow trap is disabled.

.. _infinites:

Infinites or Overflows
**********************
When a program attempts to represent a value with an extremely large absolute
value (∈ ]-∞,-1.79E308[ ⋃ ]1.79E308,+∞[), that is outside the domain of double
values, the CPU returns either -∞ or +∞. The alternative is to trigger a trap
for the exceptional **Overflow** condition.

Infinites can also be returned in case of a division by zero; in that case the
sign of the infinite is given by the product of the sign of the zero and the
sign of the dividend. The **Divide by zero** trap can be alternatively raised.

.. _exception-configuration:

Exception configuration
-----------------------
EduMIPS64 allows the user to enable or disable the traps for 4 of the 5 IEEE
exceptions, through the *FPU Exceptions* tab in the *Configure* → *Settings*
window. If any of them is disabled, the respective special value will be
returned (as described in :ref:`special-values`). 
.. In the situation depicted in
.. Figure :ref:`fig-exception_cfg`, in which some checkbox are selected, if the
.. CPU does not mask synchronous exceptions (Figure
.. :ref:`fig-exception_mask_cfg`) the selected traps will be raised if the IEEE
.. exceptional condition is encountered (Figure :ref:`fig-invalid_operation_trap`).

.. TODO: see how to include it in the resulting in-app help
.. .. _fig-exception_cfg:
   .. figure:: ../../../img/exception_cfg.png
      :scale: 50%
   
      Trap configuration for IEEE exceptions
   
   .. _fig-exception_mask_cfg:
   .. figure:: ../../../img/exception_mask_cfg.png
      :scale: 50%
   
      Option that masks all the synchronous exceptions
   
   .. _fig-invalid_operation_trap:
   .. figure:: ../../../img/invalid_operation_trap.png
      :scale: 50%
   
      Trap notification window


.. _double-directive:

The .double directive
---------------------
The ``.double`` directive must be used in the ``.data`` section of source
files, and allows to allocate a memory cell for a *double* value.

The directive can be used in 2 ways::

    variable-name: .double double_number
    variable-name: .double keyword

where ``double_number`` can be represented either in extended notation
(``1.0,0.003``), or in scientific notation(``3.7E-12,0.5E32``). 
``keyword`` can be ``POSITIVEINFINITY``, ``NEGATIVEINFINITY``,
``POSITIVEZERO``, ``NEGATIVEZERO``, ``SNAN`` e ``QNAN``,
thus allowing to directly insert in memory the special values.

The FCSR register
-----------------
The FCSR (Floating point Control Status Register) is the register that
controls several functional aspects of the FPU. It is 32 bits long and it is
represented in the statistics window.

.. .. figure:: ../../../img/fcsr_register.png
..    :scale: 50%
.. 
..    FCSR register in EduMIPS64

The **FCC** field is 8 bits wide, from 0 to 7. The conditional instructions
(``C.EQ.D,C.LT.D``) use it to save the boolean result of comparisons between
two registers.

The Cause, Enables and Flag fields handle the dynamics of IEEE exceptions
described in :ref:`special-values`. Each of them is composed of 5 bits, V
(Invalid Operation), Z (Divide by Zero), O (Overflow), U (Underflow) and I
(Inexact); the latter is not yet used.

The **Clause** field bits are set if the corresponding IEEE exceptions occur
during the execution of a program.

The **Enable** field bits are set through the configuration window and show
the IEEE exceptions for which traps are enabled.
.. shown in Figure :ref:`fig-exception_cfg`, 

The **Flag** field shows the exceptions that have happened but, since the trap
is not enabled for that particular exception, have returned special values
(the ones described in :ref:`special-values`).

The **RM** field describes the rounding method currently in use to convert
floating point numbers to integers (see the description of the ``CVT.L.D``
instruction).

Instruction set
---------------
This section describes the MIPS64 FPU instruction implemented by EduMIPS64;
they are listed in alphabetic order. The operations performed by the
instruction are described using a notation according to which the i-th
memory cell is represented as ``memory[i]``, and the FCC fields of the FCSR
register are ``FCSR_FCC[cc]``, ``cc`` ∈ [0,7].

In some instructions, to avoid ambiguity, the registers are represented as 
``GPR[i]`` and ``FPR[i]``, ``i`` ∈ [0,31], but in most cases we just use the 
``rx`` or ``fx`` notation, with ``x`` ∈ {d,s,t}. The three letters are used to
indicate the purpose of each register (destination, source, third). Lastly,
the values returned by conversion operations are represented with the
following notation: ``convert_conversiontype(register[,rounding_type])``,
where the ``rounding_type`` parameter is optional.

Some examples for the FPU instructions are available at
``http://www.edumips.org/attachment/wiki/Upload/FPUMaxSamples.rar``.

* `ADD.D fd, fs, ft`

  *Description*: ``fd = fs + ft``

  *Exceptions*: Overflow and underflow traps are generated if the result
  cannot be represented according to IEEE 754. Invalid operation is raised if
  fs or ft contain QNaN or SNan, or if an invalid operation (+∞ - ∞) is
  executed.

* `BC1F cc, offset`

  *Description*: ``if FCSR_FCC[cc] == 0 then branch``

  If ``FCSR_FCC[cc]`` is false, do a PC-relative branch. 

  *Example*::

     C.EQ.D 7,f1,f2
     BC1F 7,label

  In this example, ``C.EQ.D`` checks if ``f1`` and ``f2`` are equal, writing
  the results of the comparison in the 7th bit of the FCC field of the FCSR
  register. After that, ``BC1F`` jumps to ``label`` if the result of the
  comparison is 0 (false).

* `BC1T cc, offset`

  *Description*: ``if FCSR_FCC[cc] == 1 then branch``

  If ``FCSR_FCC[cc]`` is true, do a PC-relative branch. 

  *Example*::
 
    C.EQ.D 7,f1,f2
    BC1T 7,label

  In this example, ``C.EQ.D`` checks if ``f1`` and ``f2`` are equal, writing
  the results of the comparison in the 7th bit of the FCC field of the FCSR
  register. After that, ``BC1F`` jumps to ``label`` if the result of the
  comparison is 1 (false).

* `C.EQ.D cc, fs, ft`

  *Description*: ``FCSR_FCC[cc] = (fs==ft)``

  Checks if ``fs`` is equal to ``ft``, and saves the result of the comparison
  in ``FCSR_FCC[cc]``. See examples for ``BC1T``, ``BC1F``.

  *Exceptions*: Invalid Operation can be thrown if ``fs`` or ``ft`` contain
  QNaN (trap is triggered if it is enabled) o SNaN (trap is always triggered).

* `C.LT.D cc, fs, ft`

  *Description*: ``FCSR_FCC[cc] = (fs<ft)``

  Checks if ``fs`` is smaller than ``ft``, and saves the result of the
  comparison in ``FCSR_FCC[cc]``. 

  *Example*::
 
     C.LT.D 2,f1,f2
     BC1T 2,target

  In this example, ``C.LT.D`` checks if ``f1`` is smaller than ``f2``, and
  saves the result of the comparison in the second bit of the FCC field of the
  FCSR register. After that, ``BC1T`` jumps to ``target`` if that bit is set
  to 1.

  *Exceptions*: Invalid Operation can be thrown if ``fs`` or ``ft`` contain
  QNaN (trap is triggered if it is enabled) o SNaN (trap is always triggered).

* `CVT.D.L fd, fs`

  *Description*: ``fd = convert_longToDouble(fs)``

  Converts a long to a double.

  *Example*::
 
    DMTC1 r6,f5
    CVT.D.L f5,f5

  In this example, ``DMTC1`` copies the value of GPR r6 to FPR f5; after that
  ``CVT.D.L`` converts the value stored in f5 from long to double. If for
  instance r6 contains the value 52, after the execution of ``DMTC1`` the
  binary representation of 52 gets copied to f5. After the execution of
  ``CVT.D.L``, f5 contains the IEEE 754 representation of 52.0.

  *Exceptions:* Invalid Operation is thrown if fs contains QNaN, SNaN or an
  infinite.

* `CVT.D.W fd, fs`

  *Description:* ``fd = convert_IntToDouble(fs)``

  Converts an int to a double.

  *Example*::
 
    MTC1 r6,f5
    CVT.D.W f5,f5

  In this example, ``MTC1`` copies the lower 32 bit of the GPR r6 into the FPR
  f5. Then, ``CVT.D.W``, reads f5 as an int, and converts it to double.

  If we had ``r6=0xAAAAAAAABBBBBBBB``, after the execution of  ``MTC1`` we get
  ``f5=0xXXXXXXXXBBBBBBBB``; its upper 32 bits (``XX..X``) are now UNDEFINED
  (haven't been overwritten). ``CVT.D.W`` interprets f5 as an int
  (``f5=-1145324613``), and converts it to double(``f5=0xC1D1111111400000
  =-1.145324613E9``).

  *Exceptions:* Invalid Operation is thrown if fs contains QNaN, SNaN or an
  infinite.

* `CVT.L.D fd, fs`

  *Description:* ``fd = convert_doubleToLong(fs, CurrentRoundingMode)``
  
  Converts a double to a long, rounding it before the conversion.

  *Example*::
 
    CVT.L.D f5,f5	
    DMFC1 r6,f5

  ``CVT.L.D`` the double value in f5 to a long; then ``DMFC1`` copies f5 to
  r6; the result of this operation depends on the current rounding modality,
  that can be set in the *FPU Rounding* tab of the *Configure* →  *Settings*
  window.
.. , as depicted in Figure :ref:`fig:fpu_rounding`.

  *Exceptions:* Invalid Operation is thrown if fs contains an infinite value,
  any NaN or the results is outside the long domain [-2 :sup:`63`, 2 :sup:`63`
  -1]


.. .. _fig-fpu_rounding:
.. .. figure:: ../../../img/fpu_rounding.png
..    :scale: 50%
.. 
..    FPU Rounding

.. table:: Rounding examples

   =============== ========== ============= ============= 
    Tipo            RM field   f5 register   r6 register 
   =============== ========== ============= ============= 
    To nearest      0          6.4           6             
    To nearest      0          6.8           7            
    To nearest      0          6.5           6 (to even)  
    To nearest      0          7.5           8 (to even)  
    Towards  0      1          7.1           7            
    Towards  0      1          -2.3          -2           
    Towards  ∞      2          4.2           5            
    Towards  ∞      2          -3.9          -3           
    Towards -∞      3          4.2           4            
    Towards -∞      3          -3.9          -4           
   =============== ========== ============= ============= 

* `CVT.W.D fd, fs`

  *Description:* ``fd = convert_DoubleToInt(fs, CurrentRoundingMode)``

  Converts a double to an int, using the current rounding modality.
  
  *Exceptions:* Invalid Operation is thrown if fs contains an infinite value,
  any NaN or the results is outside the signed int domain [-2 :sup:`63`, 2
  :sup:`63` -1]

* `DIV.D fd, fs, ft`
  
  *Description:* ``fd = fs \div ft``

  *Exceptions:* Overflow or Underflow are raised if the results cannot be
  represented using the IEEE 754 standard. Invalid Operation is raised if fs
  or ft contain QNaN or SNan, or if an invalid operation is executed (0\div0,∞
  \div ∞). Divide by zero is raised if a division by zero is attempted with a
  dividend that is not QNaN or SNaN.

* `DMFC1 rt,fs`
  
  *Description:* ``rt = fs``

  Executes a bit per bit copy of the FPR fs into the GPR rt.
  
* `DMTC1 rt, fs`

  *Description:* ``fs = rt``

  Executes a bit per bit copy of the GPR rt into the FPR fs.

* `L.D ft, offset(base)`
 
  *Description:* ``ft = memory[GPR[base] + offset]``

  Loads from memory a doubleword and stores it in ft.

.. note:: `L.D` is not present in the MIPS64 ISA, it is an alias for ``LDC1``
          that is present in EduMIPS64 for compatibility with WinMIPS64.

* `LDC1 ft, offset(base)`

  *Description:* ``memory[GPR[base] + offset]``

  Loads from memory a doubleword and stores it in ft.

* `LWC1 ft, offset(base)`

  *Description:* ``ft = memory[GPR[base] + offset]``

  Loads from memory a word and stores it in ft.
  
* `MFC1 rt, fs`

  *Description:* ``rt = readInt(fs)``

  Reads the fs FPR as an int and writes its value to the rt GPR as long.
  *Example*::
    
      MFC1 r6,f5
      SD r6,mem(R0)

  Let ``f5=0xAAAAAAAABBBBBBBB``;  ``MFC1`` reads f5 as an int (lower 32 bits),
  interpreting ``BBBBBBBB`` as ``-1145324613``, and writes the value to f6 (64
  bits). After the execution of ``MFC1``, ``r6=0xFFFFFFFFBBBBBBBB = -1145324613``.
  So the ``SD`` instruction will write to memory a doubleword with this value,
  since the sign in r6 was extended.
  
* `MOVF.D fd, fs, cc`

  *Description:* ``if FCSR_FCC[cc] == 0 then fd=fs``

  If FCSR_FCC[cc] is false, the copies fs to fd.
  
* `MOVT.D fd, fs, cc`

  *Description:* ``if FCSR_FCC[cc] == 1 then fd=fs``

  If FCSR_FCC[cc] is true, the copies fs to fd.
  
* `MOV.D fd, fs`

  *Description:* ``fd = fs``

  Copies fs to fd.
  
* `MOVN.D fd, fs, rt`

  *Description:* ``if rt != 0 then fd=fs``

  If rt is not zero, copies fs to fd.
  
* `MOVZ.D fd, fs, rt`

  *Description:* ``if rt == 0 then fd=fs``

  If rt is equal to zero, copies fs to fd.
  
.. TODO: find a way to do subscript with fixed-width font.

* `MTC1 rt, fs`

  *Description:* fs = rt :sub:`0..31`

  Copies the lower 32 bit of rt to fs.

  *Example*::

      MTC1 r6,f5

  Let ``r5=0xAAAAAAAABBBBBBBB``;  ``MTC1`` reads the lower 32 bits of r5
  copying them to the 32 lower bits of f5. The higher 32 bits of f5 are not
  overwritten.
  
* `MUL.D fd, fs, ft`

  *Description:* ``fd = fs × ft``

  *Exceptions:* Overflow or Underflow are raised if the results cannot be
  represented using the IEEE 754 standard. Invalid Operation is raised if fs
  or ft contain QNaN or SNan, or if an invalid operation is executed (multiply
  by ∞ OR BY QNaN).
  
* `S.D ft, offset(base)`

  *Description:* ``memory[base+offset] = ft``

  Copies ft to memory.

.. note:: `S.D` is not present in the MIPS64 ISA, it is an alias for ``SDC1``
          that is present in EduMIPS64 for compatibility with WinMIPS64.
  
* `SDC1 ft, offset(base)`

  *Description:* ``memory[base+offset] = ft``

  Copies ft to memory.
  
* `SUB.D fd, fs, ft`

  *Description:* ``fd = fs-ft``

  *Exceptions*: Overflow and underflow traps are generated if the result
  cannot be represented according to IEEE 753. Invalid operation is raised if
  fs or ft contain QNaN or SNan, or if an invalid operation (+∞ - ∞) is
  executed.
  
* `SWC1 ft, offset(base)`

  *Description:* ``memory[base+offset] = ft``

  Copies the lower 32 bits of ft to memory.
