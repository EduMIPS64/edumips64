浮点单元
===================

本章 [#]_ 介绍 EduMIPS64 仿真的浮点运算单元（FPU）。

在第一段中，我们将介绍双倍格式、IEEE 754 标准中定义的特殊浮点数值以及浮点计算可能引发的异常情况。

第二段介绍 EduMIPS64 如何允许用户启用或禁用 IEEE 浮点陷阱。

第三段介绍如何在源程序中指定双精度数和特殊值。

在第四段中，我们将介绍 FPU 用来表示其状态的 FCSR 寄存器。它包含四舍五入、比较运算的布尔结果以及处理 IEEE 浮点异常的策略等信息。

在第五段，也是最后一段，我们将介绍 EduMIPS64 中实现的所有 MIPS64 浮点指令。

在开始讨论 FPU 之前，我们将浮点双精度数域定义为[-1.79E308,-4.94E-324] ⋃ {0} ⋃ [4.94E-324,1.79E308]。

.. [#] 本章是马西莫-特鲁比亚（Massimo Trubia）学士学位论文 "Progetto e implementazione di un modello di Floating Point Unit per un simulatore di CPU MIPS64 "的一部分。

.. _special-values:

特殊价值
--------------
浮点运算允许程序员选择在进行无效运算时是否停止计算。在这种情况下，零与零之间的除法或负数的平方根等运算必须产生一个结果，而这个结果如果不是数字（NaN），就会被视为不同的结果。

.. _nan:

NaN 或无效操作
************************
IEEE 浮点运算标准（IEEE 754）规定，无效的运算操作可以在程序运行时发出错误信号（使用 IEEE 异常**无效操作**的陷阱），
或者返回特殊值 QNan（退出非数值）。另一个 NaN 值是 SNan（Signalling Not a Number），一旦检测到它是操作数之一，
就会无条件地引发相同的陷阱。这个值在应用程序中很少使用，历史上一直用于初始化变量。

.. _zeroes:

零或下溢
********************
该标准定义的另一个特殊值是零。由于 double 格式的域中不包含零，因此它被视为一个特殊值。
零值有正零值和负零值两种：前者用于表示负数∈ ]-4.94E-324,0[) 时，并且需要一个结果（而不是允许**下溢**陷阱）；
后者用于表示的数字∈ [0,4.94E-324[]时，并且禁止下溢陷阱。

.. _infinites:

无穷大还是溢出
**********************
当程序试图表示一个绝对值极大的数值（∈ ]-∞,-1.79E308[ ⋃ ]1.79E308,+∞[），而这个数值超出了双数值的范围时，CPU 会返回 -∞ 或 +∞。另一种方法是触发异常**溢出**条件陷阱。

在除以零的情况下，也可以返回无穷小数值；在这种情况下，无穷小数值的符号由零的符号和红利的符号的乘积给出。除以零**的陷阱也会出现。

.. _exception-configuration:

异常配置
-----------------------
EduMIPS64 允许用户通过*配置* → *设置*窗口中的*FPU 异常*选项卡，启用或禁用 5 个 IEEE 异常中 4 个的陷阱。
如果禁用了其中任何一个，将返回相应的特殊值（如 :ref:`special-values` 中所述）。

.. In the situation depicted in
.. Figure :ref:`fig-exception_cfg`, in which some checkbox are selected, if the
.. CPU does not mask synchronous exceptions (Figure
.. :ref:`fig-exception_mask_cfg`) the selected traps will be raised if the IEEE
.. exceptional condition is encountered (Figure
.. :ref:`fig-invalid_operation_trap`).

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

.double 指令
---------------------
``.double`` 指令必须在源文件的 ``.data`` 部分使用，它允许为一个 *double* 值分配一个内存单元。

该指令有两种使用方式::

    变量名：.double double_number
    变量名：.double 关键字

其中，``double_number`` 可以用扩展符号（``1.0,0.003``）或科学符号（`3.7E-12,0.5E32`）表示。
关键字``可以是``POSITIVEINFINITY``、``NEGATIVEINFINITY``、``POSITIVEZERO``、``NEGATIVEZERO``、`SNAN``和`QNAN``，
因此可以直接在内存中插入特殊值。

FCSR 寄存器
-----------------
FCSR（浮点控制状态寄存器）是控制 FPU 多个功能方面的寄存器。它的长度为 32 位，在统计窗口中表示。


**FCC** 字段宽 8 位，从 0 到 7。条件指令（``C.EQ.D,C.LT.D``）使用它来保存两个寄存器之间比较的布尔结果。

Cause、Enables 和 Flag 字段用于处理 :ref:`special-values` 中描述的 IEEE 异常动态。每个字段由 5 个位组成，分别是 V（无效操作）、Z（除以零）、O（溢出）、U（下溢）和 I（不精确）；后者尚未使用。

如果在程序执行过程中出现相应的 IEEE 异常，**Clause** 字段位将被置位。

启用**字段位通过配置窗口设置，显示启用陷阱的 IEEE 异常。

**Flag** 字段显示已发生的异常，但由于陷阱未针对该异常启用，因此返回了特殊值（:ref:`special-values`中描述的特殊值）。

**RM**字段描述了当前使用的将浮点数转换为整数的舍入方法（请参阅 "CVT.L.D "指令的描述）。

指令集
---------------
本节介绍 EduMIPS64 实现的 MIPS64 FPU 指令；它们按字母顺序排列。指令执行的操作使用以下符号描述：第 i 个存储单元表示为 ``memory[i]``，FCSR 寄存器的 FCC 字段表示为 ``FCSR_FCC[cc]``，``cc``∈ [0,7]。

在某些指令中，为了避免歧义，寄存器被表示为 ``GPR[i]`` 和 ``FPR[i]``, ``i`` ∈ [0,31]，但在大多数情况下，我们只使用 ``rx`` 或 ``fx`` 符号，其中 ``x``∈ {d,s,t}。三个字母用来表示每个寄存器的用途（目的寄存器、源寄存器、第三寄存器）。最后，转换操作返回的值用以下符号表示：``convert_conversiontype(register[,rounding_type])``、
其中 ``rounding_type`` 参数是可选的。

有关 FPU 指令的一些示例，请访问 ``http://www.edumips.org/attachment/wiki/Upload/FPUMaxSamples.rar``。

*`ADD.D fd, fs, ft`

  *描述*： `fd =fs+ft```。

  *异常*： 如果结果无法根据 IEEE 754 表示，将产生溢出和下溢陷阱。如果fs或ft包含QNaN或SNan，或者执行了无效操作（+∞ - ∞），则会产生无效操作。

* `BC1F cc, offset`.

  *描述*： `if FCSR_FCC[cc] == 0 then branch``.

  如果 ``FCSR_FCC[cc]`` 为 false，则执行 PC 相关分支。

  *示例*::

     C.EQ.D 7,f1,f2
     BC1F 7,label

  在本例中，``C.EQ.D`` 检查``f1``和``f2``是否相等，并将比较结果写入 FCSR 寄存器 FCC 字段的第 7 位。之后，如果比较结果为 0（假），`BC1F`` 将跳转到`label`。

* `BC1T cc, 偏移量

  *描述*： `if FCSR_FCC[cc] == 1 then branch``.

  如果 ``FCSR_FCC[cc]`` 为真，则执行 PC 相关分支。

  *示例*::

    C.EQ.D 7,f1,f2
    BC1T 7,label

  在本例中，``C.EQ.D`` 检查``f1``和``f2``是否相等，并将比较结果写入 FCSR 寄存器 FCC 字段的第 7 位。之后，如果比较结果为 1（假），则 ``BC1F`` 跳转到 ``label`` 。

* `C.EQ.D cc, fs, ft`.

  *描述*： `FCSR_FCC[cc] = (fs==ft)``

  检查 `fs` 是否等于 `ft`，并将比较结果保存在 `FCSR_FCC[cc]` 中。请参阅 ``BC1T``, ``BC1F`` 的示例。

  *异常*： 如果 ``fs`` 或 ``ft`` 包含 QNaN（如果启用则触发陷阱） o SNaN（总是触发陷阱），则可能抛出无效操作。

* `C.LT.D cc,fs,ft`。

  *描述*： `FCSR_FCC[cc] = (fs<ft)``

  检查 `fs` 是否小于 `ft`，并将比较结果保存在 `FCSR_FCC[cc]`。

  *示例*:：

     C.LT.D 2,f1,f2
     BC1T 2,target

  在本例中，`C.LT.D`` 检查 `f1` 是否小于 `f2`，并将比较结果保存在 FCSR 寄存器 FCC 字段的第二位。之后，如果 ``BC1T`` 位设置为 1，则跳转到 ``target`` 位。

  *异常*： 如果 ``fs`` 或 ``ft`` 包含 QNaN（陷阱启用时触发） o SNaN（陷阱总是触发），则会抛出无效操作。

* `CVT.D.L fd,fs`。

  *描述*： `fd = convert_longToDouble(fs)``

  将 long 转换为 double。

  *示例*:：

    DMTC1 r6,f5
    CVT.D.L f5,f5

  在此示例中，`DMTC1`` 将 GPR r6 的值复制到 FPR f5；然后，`CVT.D.L`` 将存储在 f5 中的值从 long 转换为 double。例如，如果 r6 包含值 52，在执行 ``DMTC1`` 之后，52 的二进制表示将被复制到 f5。在执行 ``CVT.D.L`` 之后，f5 包含 52.0 的 IEEE 754 表示。

  *异常：* 如果 fs 包含 QNaN、SNaN 或无限值，则会抛出无效操作。

* `CVT.D.W fd,fs`。

  *描述：* ``fd = convert_IntToDouble(fs)``

  将 int 转换为 double。

  *示例*:：

    MTC1 r6,f5
    CVT.D.W f5,f5

  在本例中，``MTC1`` 将 GPR r6 的低 32 位复制到 FPR f5 中。然后，``CVT.D.W`` 读取 f5 作为 int，并将其转换为 double。

  如果我们有 ``r6=0xAAAAAAAABBBBBBBB`` ，在执行 ``MTC1`` 后，我们会得到 ``f5=0xXXXXXXXXBBBBBB`` ；其上 32 位（``XX..X``）现在是未定义的（未被覆盖）。CVT.D.W``将 f5 解释为 int (``f5=-1145324613``) 并转换为 double (``f5=0xC1D11111400000 =-1.145324613E9``).

  *异常：* 如果 fs 包含 QNaN、SNaN 或无限值，则会抛出无效操作。

* `CVT.L.D fd,fs`。

  *描述：* ``fd = convert_doubleToLong(fs, CurrentRoundingMode)``

  将 double 转换为 long，在转换前进行四舍五入。

  *示例*:：

    CVT.L.D f5,f5
    DMFC1 r6,f5

  CVT.L.D "将 f5 中的 double 值转换为 long 值；然后 "DMFC1 "将 f5 复制到 r6；此操作的结果取决于当前的舍入模式，可在 "配置 "* → "设置 "* 窗口的 "*FPU 舍入 "* 选项卡中进行设置。

.. 如图 :ref:`fig:fpu_rounding` 所示。

  *异常：* 如果 fs 包含无限值、任何 NaN 或结果超出长域 [-2 :sup:`63`, 2 :sup:`63` -1] 则抛出无效操作。

.. .. _fig-fpu_rounding:
.. .. figure:: ../../../img/fpu_rounding.png
..    :scale: 50%
..
..    FPU Rounding

... 表格:: 四舍五入示例

   =============== ========== ============= =============
    Tipo RM 字段 f5 寄存器 r6 寄存器
   =============== ========== ============= =============
    至最近的 0 6.4 6
    至最近的 0 6.8 7
    至最近的 0 6.5 6（至偶数）
    至最近 0 7.5 8（至偶数）
    向 0 1 7.1 7
    向 0 1 -2.3 -2
    向 ∞ 2 4.2 5
    向 ∞ 2 -3.9 -3
    朝着 -∞ 3 4.2 4
    朝向 -∞ 3 -3.9 -4
   =============== ========== ============= =============

* `CVT.W.D fd, fs`

  *描述：* ``fd = convert_DoubleToInt(fs, CurrentRoundingMode)``

  使用当前舍入模式将 double 转换为 int。

  *异常：* 如果 fs 包含一个无限值、任何 NaN 或结果超出有符号 int 域 [-2 :sup:`63`, 2 :sup:`63` -1] 则会抛出无效操作。

* `DIV.D fd, fs, ft`

  *描述：* `fd = fs\div ft``

  *异常：* 如果结果不能用 IEEE 754 标准表示，则会出现溢出或下溢。如果fs或ft包含QNaN或SNan，或者执行了无效操作（0\div0,∞ \div ∞），则会出现无效操作。如果试图用非 QNaN 或 SNaN 的红利除以零，则会出现除以零的提示。

* `DMFC1 rt,fs`.

  *描述：* `rt = fs``

  将 FPR fs 按位复制到 GPR rt 中。

* `DMTC1 rt, fs``

  *描述：* ``fs = rt``

  将 GPR rt 按位复制到 FPR fs 中。

* `L.D ft, offset(base)`

  *描述：* `ft = memory[GPR[base] + offset]``

  从内存中加载一个双字，并将其存储在 ft 中。

注：`L.D`不存在于 MIPS64 ISA 中，它是`LDC1`的别名，存在于 EduMIPS64 中，以便与 WinMIPS64 兼容。

* LDC1 ft, offset(base)`

  *描述：* ``memory[GPR[base] + offset]``

  从内存中加载一个双字，并将其存储在 ft 中。

* `LWC1 ft, offset(base)`

  *描述：* `ft = memory[GPR[base] + offset]``

  从内存中加载一个字并将其存储在 ft 中。

* `MFC1 rt,fs`。

  *描述：* ``rt = readInt(fs)``

  读取 fs FPR 的 int 值，并将其写入 rt GPR 的 long 值。
  *示例*:：

      MFC1 r6,f5
      SD r6,mem(R0)

  让 ``f5=0xAAAAAAAABBBBBB``; ``MFC1`` 读取 f5 作为 int（低 32 位），将 ``BBBBBBBB`` 解释为 ``-1145324613``，并将值写入 f6（64 位）。执行``MFC1``后，``r6=0xFFFFFFFFBBBBBBBB=-1145324613``。
  因此，由于 r6 中的符号被扩展，`SD`` 指令将向内存写入一个具有此值的双字。

* `MOVF.D fd, fs, cc`.

  *描述：* ``if FCSR_FCC[cc] == 0 then fd=fs``

  如果 FCSR_FCC[cc] 为假，则将 fs 复制到 fd。

* `MOVT.D fd, fs, cc``

  *说明：* ``if FCSR_FCC[cc] == 1 then fd=fs``

  如果 FCSR_FCC[cc] 为真，则将 fs 复制到 fd。

* `MOV.D fd,fs`。

  *描述：* `fd = fs``

  将 fs 复制到 fd。

`MOVN.D fd, fs, rt` *描述：* ``fd = fs`` 将 fs 复制到 fd。

  *描述：* ``if rt != 0 then fd=fs``

  如果 rt 不为零，则将 fs 复制到 fd。

* `MOVZ.D fd, fs, rt`

  *说明：* ``if rt == 0 then fd=fs``

  如果 rt 等于零，则将 fs 复制到 fd。

.. TODO: 找到使用固定宽度字体的下标方法。

* MTC1 rt, fs

  *描述：*fs = rt :sub:`0..31`

  将 rt 的低 32 位复制到 fs。

  *示例*:：

      MTC1 r6,f5

  让 ``r5=0xAAAAAAABBBBBBB``B``; ``MTC1`` 读取 r5 的低 32 位，并将其复制到 f5 的低 32 位。 f5 的高 32 位不会被覆盖。

* `MUL.D fd, fs, ft`

  *描述：* ``fd =fs×ft``。

  *异常：* 如果结果不能用 IEEE 754 标准表示，则会出现溢出或下溢。如果 fs 或 ft 包含 QNaN 或 SNan，或执行了无效操作（乘以 ∞ 或 BY QNaN），则会出现无效操作。

* `S.D ft，offset(base)`。

  *描述：* `memory[base+offset] = ft``

  将 ft 复制到内存中。

注意：MIPS64 ISA 中没有`S.D`，它是`SDC1`的别名，EduMIPS64 中有`SDC1`，以便与 WinMIPS64 兼容。

* `SDC1 ft, offset(base)`

  *描述：* ``memory[base+offset] = ft``

  将 ft 复制到内存。

* `SUB.D fd, fs, ft`

  *描述：* ``fd = fs-ft``

  *异常*： 如果结果无法根据 IEEE 753 表示，则会产生溢出和下溢陷阱。如果fs或ft包含QNaN或SNan，或者执行了无效操作（+∞ - ∞），则会产生无效操作。

* `SWC1 ft, offset(base)`

  *描述：* ``memory[base+offset] = ft``

  将 ft 的低 32 位复制到内存中。