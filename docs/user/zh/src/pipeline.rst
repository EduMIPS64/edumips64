流水线与转发
============

EduMIPS64 模拟了经典的 5 级 MIPS 整数流水线，该流水线在 Hennessy 与
Patterson 所著 *Computer Architecture: A Quantitative Approach* 的附录 C
（*Pipelining: Basic and Intermediate Concepts*）中有详细描述（在一些早期
版本中该附录编号为 "A"）。各级流水线如下：

* **IF** —— 取指（Instruction Fetch）
* **ID** —— 译码 / 寄存器读取（Instruction Decode）
* **EX** —— 执行 / 有效地址计算（Execute）
* **MEM** —— 访存（Memory access）
* **WB** —— 写回（Write-Back）

在任意给定的时钟周期内，流水线中最多可以有五条指令同时执行，每一级对应
一条。

数据冒险
--------
*写后读*\ （Read-After-Write，RAW）冒险是指一条指令试图读取一个寄存器，而
该寄存器正被一条尚未完成的前序指令所写入。EduMIPS64 在消费者指令的 **ID**
阶段通过检查每个寄存器的写信号量来检测 RAW 冒险：当生产者指令（同样在 ID
阶段）锁定其目的寄存器时信号量被置位，当结果真正可用时再清除。一旦检测
到冒险，流水线会在 EX 阶段插入一个或多个 *气泡*\ （bubble），并在统计面板
中以 **RAW 停滞**\ （RAW stalls）计数。

*写后写*\ （Write-After-Write，WAW）冒险只可能出现在 FPU 上，因为 FPU 的
流水线并非均匀的（不同的功能单元具有不同的延迟），两条指令可能会以不同
于程序顺序的次序到达 WB 阶段。WAW 冒险在 ID 阶段检测，并作为
**WAW 停滞**\ （WAW stalls）单独计数。

转发（Forwarding）
------------------
转发（又称 *旁路*，bypassing）是一种硬件技术：将一条指令的结果在写回寄存
器文件之前提前提供给后续依赖它的指令，从而减少由 RAW 冒险引起的停滞。这
正是 Hennessy 与 Patterson 所著《计算机体系结构：量化研究方法》附录 C
第 C.2 节（"The Major Hurdle of Pipelining — Pipeline Hazards"）中
"Minimizing Data Hazard Stalls by Forwarding"（通过转发最小化数据冒险
停滞）一段所描述的标准技术。

在 EduMIPS64 中，可以通过 *设置* 对话框的 *主设置* 选项卡来启用或禁用
转发（参见 :doc:`user-interface`）。启用时，模拟器会将值直接从生产阶段
（EX 输出、MEM 输出或 WB）旁路到消费阶段（通常是下一条指令的 EX 阶段，
或分支指令的 ID 阶段），只要生产者已经在流水线中足够靠前。禁用时，每个
RAW 依赖都必须等到生产者的 WB 阶段完成后，消费者才能在 ID 阶段读取该值。

通过将同一个程序分别在转发开启和关闭的情况下加载运行，并观察 *统计* 面
板中的周期计数与 RAW 停滞计数，就可以比较这两种行为。

ALU 到 ALU 的依赖
~~~~~~~~~~~~~~~~~
考虑 Hennessy 与 Patterson 附录 C 图 C.5（大约在 C-16 页）中给出的典型
示例：

.. code-block:: text

    DADD  R1, R2, R3
    DSUB  R4, R1, R5
    AND   R6, R1, R7
    OR    R8, R1, R9
    XOR   R10, R1, R11

紧随 ``DADD`` 之后的四条指令都依赖 ``R1``。

* **没有转发**：``DSUB`` 必须在 ID 阶段停顿，直到 ``DADD`` 把 ``R1``
  写回，即停顿两个周期；``AND`` 必须停顿一个周期；``OR`` 之所以不会
  停顿，仅仅是因为假设寄存器文件在时钟周期的前半段写入、后半段读取
  （等效于寄存器文件内部的 "内部转发"）。
* **开启转发**：``DADD`` 在 EX 阶段末尾算出的 ``R1`` 值被直接转发到
  ``DSUB`` 的 EX 阶段（以及 ``AND``、``OR``、``XOR`` 的 EX 阶段）。对于
  ALU 到 ALU 的指令链，不需要任何停滞。

该示例作为测试用例 ``forwarding-hp-pA16.s`` 随项目一同发布。

load-use 依赖
~~~~~~~~~~~~~
转发并不能消除所有停滞。考虑如下示例（Hennessy 与 Patterson 附录 C
图 C.7，大约在 C-18 页）：

.. code-block:: text

    DADD R1, R2, R3
    LD   R4, 0(R1)
    SD   R4, 8(R1)

``LD`` 在 MEM 阶段末尾才产生 ``R4``，但 ``SD`` 需要在自己的 MEM 阶段开始
时就读到 ``R4``。即使启用转发，也必须在 load 指令和依赖它的指令之间插入
一个气泡。这就是常说的 *load-use* 冒险，在 EduMIPS64 中，当转发启用时
会表现为一次 RAW 停滞。该示例作为测试用例 ``forwarding-hp-pA18.s`` 随
项目一同发布。

分支指令与 ID 阶段
~~~~~~~~~~~~~~~~~~
MIPS 整数流水线在 **ID** 阶段解析条件分支（参见 Hennessy 与 Patterson
附录 C 第 C.2 节 "Branch Hazards"），即在源寄存器被读出之后立即比较。
这对涉及分支指令的 RAW 冒险有一个重要后果：

.. code-block:: text

    SLT   R1, R2, R4
    BEQZ  R1, finish

``BEQZ`` 在它的 ID 阶段读取 ``R1``，而这一 ID 阶段与 ``SLT`` 的 EX 阶段
处于同一个时钟周期。从 EX 到 ID 的转发 **不可能** 实现，因为 ALU 的结果
是在 EX 阶段 *末尾* 才产生的，而这也正是 ID 阶段结束的时刻。因此即使开启
转发，在一条 ALU 指令和依赖其结果的分支指令之间 **至少也要一个停滞**。
load 指令后紧跟依赖它的分支指令则同理需要两个停滞。

EduMIPS64 会识别出这些情形，并将它们计入 RAW 停滞统计。

延伸阅读
--------
关于 5 级 MIPS 流水线中的数据冒险、转发以及分支冒险，权威参考资料是下
书的附录 C：

   John L. Hennessy 与 David A. Patterson，*Computer Architecture:
   A Quantitative Approach*，Morgan Kaufmann。（在该书的早期版本中
   这一附录被编号为 "A"。）

``src/test/resources/`` 目录下的测试程序 ``forwarding.s``、
``forwarding-hp-pA16.s`` 以及 ``forwarding-hp-pA18.s`` 复现了该附录中
的示例，是使用模拟器进行实验的良好起点。
