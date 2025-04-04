指令集
===================

在本节中，我们将介绍 EduMIPS64 所认知的 MIPS64 指令集子集。我们可以进行两种不同的分类：一种基于指令的功能，一种基于指令的参数类型。

.. 有关这些分类的更多信息，请参阅第~/ref{mipsis}节。

第一种分类法将指令分为三类： ALU 结构指令、加载/存储指令、流控制指令。接下来的几个小节将介绍每一类指令以及属于这些类别的每一条指令。

第四小节将介绍不属于上述任何类别的指令。

.. 如需更完整的 MIPS64 指令集参考，请参阅~\cite{mips-2}。


ALU 指令
----------------
算术逻辑单元（简称 ALU）是 CPU 执行单元的一部分，负责进行算术和逻辑运算。因此，在 ALU 指令组中，我们可以找到进行此类运算的指令。

ALU 指令可分为两组： *R型*和I型*。

其中四条指令使用两个特殊寄存器： LO和HI。它们是 CPU 的内部寄存器，可以通过 FLO` 和 MFHI` 指令访问其值。

下面是 R 型 ALU 指令的列表。

* `AND rd, rs, rt`

  在 rs 和 rt 之间执行比特 AND，并将结果放入 rd。

* `ADD rd, rs, rt`

  将 32 位寄存器 rs 和 rt 的内容相加，将其视为带符号值，并将结果放入 rd。如果出现溢出，则捕获。

* `ADDU rd, rs, rt`

  将 32 位寄存器 rs 和 rt 的内容相加，并将结果放入 rd。
  在任何情况下都不会发生整数溢出。

.. \MISN{}

* `DADD rd, rs, rt`

  将 64 位寄存器 rs 和 rt 的内容相加，认为它们是带符号的值，并将结果放入 rd。如果出现溢出，则进行陷阱处理。

* `DADDU rd, rs, rt`

  将 64 位寄存器 rs 和 rt 的内容相加，并将结果存入 rd。
  在任何情况下都不会发生整数溢出。

.. \MISN{}

* `DDIV rs, rt`

  执行 64 位寄存器 rs 和 rt 之间的除法运算，将 64 位商放入 LO，将 64 位余数放入 HI。

* `DDIVU rs, rt`

  执行 64 位寄存器 rs 和 rt 的除法运算，将它们视为无符号值，并将 64 位商放入 LO，将 64 位余数放入 HI。

* `DIV rs, rt`

  执行 32 位寄存器 rs 和 rt 之间的除法运算，将 32 位商放入 LO，将 32 位余数放入 HI。

* `DIVU rs, rt`

  执行 32 位寄存器 rs 和 rt 的除法运算，将它们视为无符号值，并将 32 位商放入 LO，将 32 位余数放入 HI。

* `DMUHU rd, rs, rt`

  执行 64 位寄存器 rs 和 rt 之间的乘法，将它们视为无符号值，并将结果的高位 64 位双字放入寄存器 rd。

* `DMULT rs, rt` 
  
  执行 64 位寄存器 rs 和 rt 之间的乘法运算，将运算结果的低阶 64 位双字放入特殊寄存器 LO，将运算结果的高阶 64 位双字放入特殊寄存器 HI。

* `DMULU rd, rs, rt`

  执行 64 位寄存器 rs 和 rt 之间的乘法运算，将它们视为无符号值，并将结果的低阶 64 位双字放入特殊寄存器 LO，将结果的高阶 64 位双字放入寄存器 rd。

* `DMULTU rs, rt`.

  执行 64 位寄存器 rs 和 rt 之间的乘法运算，将它们视为无符号值，并将结果的低阶 64 位双字放入特殊寄存器 LO，将结果的高阶 64 位双字放入特殊寄存器 HI。

* `DSLL rd, rt, sa`

  将 64 位寄存器 rt 左移，移动量由立即（正）值 sa 指定，并将结果存入 64 位寄存器 rd。空位用零填充。

* `DSLLV rd, rt, rs`

  对 64 位寄存器 rt 进行左移，左移量为 rs 的低阶 6 位无符号值，并将结果存入 64 位寄存器 rd。空位用零填充。

* `DSRA rd, rt, sa`

  对 64 位寄存器 rt 进行右移，移动量为即时（正）值 sa 指定的值，并将结果存入 64 位寄存器 rd。如果 rt 的最左边位为 0，则空位用 0 填充，否则用 1 填充。

* `DSRAV rd, rt, rs`

  对 64 位寄存器 rt 进行右移，将 rs 的低阶 6 位数指定为无符号值，并将结果放入 64 位寄存器 rd。如果 rt 的最左边位为 0，则空位用 0 填充，否则用 1 填充。

* `DSRL rd, rs, sa`

  对 64 位寄存器 rs 进行右移，移动量为即时（正）值 sa 指定的量，并将结果放入 64 位寄存器 rd。空位用零填充。

* `DSRLV rd, rt, rs`

  对 64 位寄存器 rt 进行右移，将 rs 的低阶 6 位数指定为无符号值，并将结果存入 64 位寄存器 rd。空位用零填充。

* `DSUB rd, rs, rt`.

  将 64 位寄存器 rt 的值减去 64 位寄存器 rs 的值，将它们视为有符号值，并将结果放入 rd。如果出现溢出，则捕获。

* `DSUBU rd, rs, rt`

  将 64 位寄存器 rt 的值减去 64 位寄存器 rs 的值，并将结果存入 rd。在任何情况下都不会发生整数溢出。

.. \MISN{}

* `MFLO rd`

  将特殊寄存器 LO 的内容移入 rd。

* `MFHI rd`.

  将特殊寄存器 HI 的内容移入 rd。

* `MOVN rd, rs, rt``

  如果 rt 与零不同，则将 rs 的内容移入 rd。

* `MOVZ rd, rs, rt`

  如果 rt 等于零，则将 rs 的内容移入 rd。

* `MULT rs, rt`

  在 32 位寄存器 rs 和 rt 之间执行乘法运算，将运算结果的低阶 32 位字放入特殊寄存器 LO，将运算结果的高阶 32 位字放入特殊寄存器 HI。

* `MULTU rs, rt``

  执行 32 位寄存器 rs 和 rt 之间的乘法运算，将它们视为无符号值，并将运算结果的低阶 32 位字放入特殊寄存器 LO，将运算结果的高阶 32 位字放入特殊寄存器 HI。

* `OR rd、rs、rt``

  在 rs 和 rt 之间执行比特 OR，并将结果放入 rd。

* `SLL rd, rt, sa`.

  对 32 位寄存器 rt 进行左移，左移量为即时（正）值 sa 指定的量，并将结果放入 32 位寄存器 rd。空位用零填充。

* `SLLV rd, rt, rs`

  对 32 位寄存器 rt 进行左移，左移量为 rs 的低阶 5 位无符号值，并将结果存入 32 位寄存器 rd。空位用零填充。

* `SRA rd, rt, sa`

  对 32 位寄存器 rt 进行右移，移动量为立即（正）值 sa 指定的值，并将结果放入 32 位寄存器 rd。如果 rt 的最左边位为 0，则空位用 0 填充，否则用 1 填充。

* `SRAV rd, rt, rs`

  对 32 位寄存器 rt 进行右移，将 rs 的低阶 5 位指定为无符号值，并将结果放入 32 位寄存器 rd。如果 rt 的最左边位为 0，则空位用 0 填充，否则用 1 填充。

* `SRL rd, rs, sa`

  对 32 位寄存器 rs 进行右移，移动量为即时（正）值 sa 指定的量，并将结果放入 32 位寄存器 rd。空位用零填充。

* `SRLV rd, rt, rs`

  对 32 位寄存器 rt 进行右移，将 rs 的低阶 5 位数指定为无符号值，并将结果存入 32 位寄存器 rd。空位用零填充。

* `SUB rd, rs, rt`.

  将 32 位寄存器 rt 的值减去 32 位寄存器 rs 的值，将它们视为有符号值，并将结果放入 rd。如果出现溢出，则捕获。

* `SUBU rd, rs, rt`

  将 32 位寄存器 rt 的值减去 32 位寄存器 rs 的值，并将结果存入 rd。在任何情况下都不会发生整数溢出。

.. \MISN{}

* `SLT rd, rs, rt`

  如果 rs 的值小于 rt 的值，则将 rd 的值设置为 1，否则设置为 0。 该指令执行带符号比较。

* `SLTU rd, rs, rt`

  如果 rs 的值小于 rt 的值，则将 rd 的值设置为 1，否则将其设置为 0。该指令执行无符号比较。

* `XOR rd, rs, rt`

  在 rs 和 rt 之间执行位排他性 OR（XOR），并将结果存入 rd。

  以下是 I 型 ALU 指令列表。

* `ADDI rt, rs, immediate` 执行 32 位寄存器 rs 与立即值的和，并将结果存入 rt。

  执行 32 位寄存器 rs 与立即值之和，并将结果存入 rt。该指令将 rs 和立即值视为带符号值。如果发生溢出，则捕获。

* `ADDIU rt, rs, immediate`

  执行 32 位寄存器 rs 与立即值的和，并将结果存入 rt。在任何情况下都不会发生整数溢出。

.. \MISN{}

* `ANDI rt, rs, immediate`

  执行 rs 与立即值之间的位和运算，并将结果存入 rt。

* `DADDI rt, rs, immediate``

  执行 64 位寄存器 rs 与立即值的和，将结果存入 rt。如果发生溢出，则捕获。

* `DADDIU rt, rs, immediate`

  执行 64 位寄存器 rs 与立即值的和，将结果存入 rt。在任何情况下都不会发生整数溢出。

.. \MISN{}

* `DADDUI rt, rs, immediate`.

  执行 64 位寄存器 rs 与立即值的和，并将结果存入 rt。在任何情况下都不会发生整数溢出。

.. \MISN{}
.. \WARN{}

* `LUI rt, immediate`.

  将立即值中定义的常量加载到 rt 下 32 位的上半部分（16 位），并对寄存器的上 32 位进行符号扩展。

* `ORI rt, rs, immediate`

  执行 rs 和立即值之间的位操作 OR，将结果放入 rt。

* `SLTI rt, rs, immediate` 设置 rt 的值为 0。

  如果 rs 的值小于立即值，则将 rt 的值设置为 1，否则设置为 0。

* `SLTIU rt, rs, immediate`

  如果 rs 的值小于立即值，则将 rt 的值设置为 1，否则设置为 0。

* `XORI rt, rs, immediate``

  在 rs 和立即值之间执行位排他性 OR（XOR），并将结果存入 rt。

加载/存储指令
-----------------------
这类指令包含所有在寄存器和内存之间进行传输操作的指令。所有这些指令的形式都是：：

  [label:] 指令 rt, 偏移量（基数）

其中，rt 是源寄存器或目标寄存器，取决于我们使用的是存储指令还是加载指令；offset 是标签或立即值，base 是寄存器。在寄存器 `base` 的值上加上立即值 `offset`，就得到了地址。

指定的地址必须根据处理的数据类型对齐。以 "U "结尾的加载指令将寄存器 rt 的内容视为无符号值。

加载指令列表：

* `LB rt, offset(base)`

  将寄存器 rt 中偏移量和基数指定地址处的存储单元内容作为有符号字节加载。

* `LBU rt, offset(base)`

  将寄存器 rt 中偏移量和基数指定地址处的存储单元内容作为无符号字节加载。

* `LD rt, offset(base)`

  将寄存器 rt 中偏移量和基数指定地址处的存储单元内容作为双字加载。

* `LH rt, offset(base)` 

  加载内存单元的内容。将寄存器 rt 中偏移量和基数指定地址处的存储单元内容作为带符号的半字加载。

* `LHU rt, offset(base)` 

  加载内存单元的内容。将寄存器 rt 中偏移量和基数指定地址处的存储单元内容作为无符号半字加载。

* `LW rt, offset(base)`

  将寄存器 rt 中偏移量和基数指定地址处的存储单元内容作为有符号字加载。

* `LWU rt, offset(base)`

  将寄存器 rt 中偏移量和基数指定地址处的存储单元内容作为无符号字加载。

存储指令列表：

* `SB rt, offset(base)`

  将寄存器 rt 的内容存储到偏移量和基数指定的存储单元中，将其视为字节。

* `SD rt, offset(base)`

  将寄存器 rt 的内容存储到由偏移量和基数指定的存储单元中，并将其视为双字。

* `SH rt, offset(base)` 

  存储寄存器 rt 的内容。将寄存器 rt 的内容存储到由偏移量和基数指定的存储单元中，并将其视为半字。

* `SW rt, offset(base)` 

  存储寄存器 rt 的内容。将寄存器 rt 的内容存储到偏移量和基数指定的存储单元中，将其视为一个字。

流控制指令
-------------------------
流控制指令用于改变 CPU 抓取指令的顺序。我们可以对这些指令进行区分： R型、I型和J型。

这些指令实际上是在 ID 阶段执行跳转，因此往往会执行无用的取指。在这种情况下，会从流水线中移除两条指令，分支执行停滞计数器会增加两个单位。

R 型流控制指令列表：

* `JALR rs`

  将 rs 的内容放入程序计数器，并将 JALR 指令后的指令地址（即返回值）放入 R31。

* `JR rs`

  将 rs 的内容放入程序计数器。

I 型流控制指令列表：

* `B offset`

  无条件跳转到偏移量

* `BEQ rs, rt, offset`

  如果 rs 等于 rt，则跳转到偏移量。

* `BEQZ rs, offset`

  如果 rs 等于零，则跳转到偏移量。

..	\警告

* `BGEZ rs, offset`.

  如果 rs 大于或等于零，执行 PC 相对跳转到偏移量。

* `BNE rs, rt, offset`.

  如果 rs 不等于 rt，则跳转到偏移量。

* `BNEZ rs, offset`.

  如果 rs 不等于零，则跳转到偏移量。

..	\WARN

J 型流控制指令列表：

* `J target`

  将立即值 target 放入程序计数器。

* `JAL target`

  将即期目标值放入程序计数器，并将 JAL 指令后的指令地址（即返回值）放入 R31。

SYSCALL 指令
-------------------------
SYSCALL 指令为程序员提供了一个类似操作系统的接口，可进行六种不同的系统调用。

系统调用希望将其参数地址寄存在寄存器 R14（$t6）中，并将其返回值寄存在寄存器 R1（$at）中。

系统调用尽可能遵循 POSIX 协议。

`SYSCALL 0 - exit()`
~~~~~~~~~~~~~~~~~~~~
SYSCALL 0 不需要任何参数，也不返回任何值。它只是停止模拟器。

请注意，如果模拟器在源代码中找不到 SYSCALL 0 或其任何等效代码（HALT - TRAP 0），它将自动添加到源代码的末尾。

`SYSCALL 1 - open()`
~~~~~~~~~~~~~~~~~~~~
SYSCALL 1 需要两个参数：一个以零结尾的字符串，表示必须打开的文件的路径名；一个包含整数的双字，表示必须用来指定如何打开文件的标志。

这个整数必须是你想使用的标志的总和，从以下列表中选择：

* `O_RDONLY (0x01)` 以只读模式打开文件；
* `O_WRONLY (0x02)` 以只写模式打开文件；
* `O_RDWR (0x03)` 以读/写模式打开文件；
* `O_CREAT (0x04)` 如果文件不存在，则创建该文件；
* `O_APPEND (0x08)` 在写模式下，在文件末尾追加已写入的文本；
* `O_TRUNC (0x08)` 在写模式下，打开文件后立即删除文件内容。

必须指定前三种模式之一。第四和第五种模式是排他性的，如果指定 O_TRUNC，则不能指定 O_APPEND（反之亦然）。

只需将这些标志的整数值相加，就可以指定模式组合。例如，如果想以只写模式打开文件，并将写入的文本追加到文件末尾，则应指定模式 2 + 8 = 10。

系统调用的返回值是与文件相关联的新文件描述符，可以进一步用于其他系统调用。如果出现错误，返回值将为-1。

`SYSCALL 2 - close()`
~~~~~~~~~~~~~~~~~~~~~
SYSCALL 2 只需要一个参数，即被关闭文件的文件描述符。

如果操作成功结束，SYSCALL 2 将返回 0，否则将返回-1。可能的失败原因是试图关闭一个不存在的文件描述符，或试图关闭分别与标准输入、标准输出和标准错误相关联的文件描述符 0、1 或 2。

`SYSCALL 3 - read()`
~~~~~~~~~~~~~~~~~~~~
SYSCALL 3 需要三个参数：要读取的文件描述符、读取数据的地址、读取的字节数。

如果第一个参数为 0，模拟器将通过输入对话框提示用户输入。如果输入的长度大于需要读取的字节数，模拟器将再次显示信息对话框。

如果读取操作失败，模拟器将返回已有效读取的字节数或-1。可能的失败原因包括试图从一个不存在的文件描述符中读取、试图从文件描述符 1（标准输出）或 2（标准错误）中读取或试图从一个只写文件描述符中读取。

`SYSCALL 4 - write()`
~~~~~~~~~~~~~~~~~~~~~
SYSCALL 4 需要三个参数：要写入的文件描述符、必须读取数据的地址以及要写入的字节数。

如果第一个参数为 2 或 3，模拟器将弹出输入/输出窗口，并在那里写入读取的数据。

如果写入操作失败，模拟器将返回已写入的字节数或-1。失败的可能原因是尝试向不存在的文件描述符写入数据、尝试向文件描述符 0（标准输入）写入数据或尝试向只读文件描述符写入数据。

`SYSCALL 5 - printf()`
~~~~~~~~~~~~~~~~~~~~~~
SYSCALL 5 需要多个参数，第一个参数是所谓“格式字符串”的地址。格式字符串中可以包含一些占位符，如下表所示：

* `%s` 表示字符串参数；
* `%i` 表示整数参数；
* `%d` 行为类似于 `%i`；
* `%%` 字面意义为 `%`

对于每一个 `%s`、 `%d` 或 `%i` 占位符，SYSCALL 5 都期望一个参数，从上一个占位符的地址开始。

如果 SYSCALL 找到一个整数参数的占位符，它就希望相应的参数是一个整数值；如果 SYSCALL 找到一个字符串参数的占位符，它就希望参数是字符串的地址。

结果打印在输入/输出窗口中，写入的字节数放入 R1。

如果出现错误，则向 R1 写入-1。

其他指令
------------------
在本节中，有一些指令不属于前几类。

`BREAK``
~~~~~~~
如果模拟器正在运行，BREAK 指令抛出的异常具有停止执行的效果。它可用于调试目的。

`NOP`
~~~~~
NOP 指令不执行任何操作，用于在源代码中创建间隙。

`TRAP`
~~~~~~
TRAP 指令是 SYSCALL 指令的弃用别名。

`HALT`
~~~~~~
HALT 指令是 SYSCALL 0 指令的弃用别名，用于停止模拟器。