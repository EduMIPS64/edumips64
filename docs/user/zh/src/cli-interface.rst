交互式命令行界面
================
除了桌面 GUI 之外，EduMIPS64 的 JAR 还可以作为交互式命令行 shell 运行。
该 shell 适用于汇编程序的批处理式实验（在脚本中运行测试程序、可脚本化
的课程材料、自动评分、终端调试等等），并提供与桌面 GUI 完全相同的模拟器
内核，只是采用了文本界面。

本章描述 shell 中可用的 *命令*。用于启动 JAR 的命令行 *选项*\ （\ ``--headless``\ 、
``--file``\ 、\ ``--verbose``\  …）记录在
:ref:`command-line-options-zh` 中；尤其是，要进入该 shell，需要在启动
JAR 时附加 ``--headless``\ （可同时使用或不使用 ``--verbose``\ ）。

启用 ``--verbose`` 后，shell 会打印额外的信息性消息 —— 启动时的欢迎
横幅，``run`` 前后的"开始执行" / "结束执行"消息，长时间运行中的
进度点，``load`` 之后解析器发出的警告等等。不带 ``--verbose`` 时，
shell 仅产生程序输出（例如 ``SYSCALL 5`` 打印的字符串）以及
``show``\ 、\ ``config`` 等命令的显式回复。这种安静的默认行为非常适合
将 EduMIPS64 嵌入到由其他工具组成的流水线中。

提示符
------
启动后，shell 会打印一个 ``>`` 提示符并等待命令。命令按行读取，并按空格
分割成 token。在空行上按回车会重新打印 help。shell 的循环会一直运行，
直到执行 ``exit`` 命令（或在标准输入上发送 ``Ctrl+D`` / EOF）。

每个命令都接受 ``-h`` / ``--help``\ ，以打印其专属的用法说明。顶层命令
``help`` 列出所有可用命令以及简短描述；这是发现 shell 功能的最简单
方式。

可用命令
--------
shell 提供少量命令，它们直接对应于模拟器的生命周期：加载源文件、单步
或一次性运行、检查产生的状态，以及可选地导出供 Dinero 进行缓存分析的
跟踪文件。

load
~~~~
加载新的可执行文件::

    > load path/to/program.s

``load`` 解析给定的文件并使模拟器准备好执行。成功后 CPU 进入
``RUNNING`` 状态，可以通过 ``step`` 或 ``run`` 推进。

如果解析器报告错误，文件不会被加载，并会打印错误描述。如果只产生警告，
文件仍会被加载 —— 与 GUI 的行为一致；带 ``--verbose`` 时这些警告也会
被打印出来。

不能在前一个程序仍在运行时执行新的 ``load``\ ；请先使用 ``reset`` 将
CPU 恢复到可加载的状态。

step
~~~~
让 CPU 状态机推进 N 个周期::

    > step          # 推进 1 个周期（默认）
    > step 10       # 推进 10 个周期

每个周期之后都会打印流水线的内容，因此 ``step`` 是逐条指令跟踪执行
并观察指令如何穿过 IF / ID / EX / MEM / WB 以及 FPU 各阶段的标准方式。

如果在所请求的周期数尚未全部完成之前程序就结束了（``SYSCALL 0`` 或
``BREAK``\ ），``step`` 会在该处停下并打印对应的消息。

run
~~~
不间断地执行程序::

    > run

模拟器会一直推进，直到程序以 ``SYSCALL 0``\ （或等效指令）或 ``BREAK``
指令结束。带 ``--verbose`` 时，shell 会在 run 周围打印 *开始* / *结束*
横幅，每千个周期打印一个进度点，并在最后给出已执行周期总数和墙钟用时
的摘要。使用 ``run`` 可以快速获得程序的输出（``SYSCALL 4`` /
``SYSCALL 5``\ ）和最终状态，然后用 ``show`` 检查结果。

show
~~~~
检查模拟 CPU 的状态。``show`` 是一组子命令；每个子命令都会将模拟器
状态的不同部分打印到标准输出。

* ``show registers`` — 打印全部 32 个整数通用寄存器
  （``R0``\ –\ ``R31``\ ）及其当前值。
* ``show register N`` — 打印整数寄存器 ``N``
  （\ ``0 ≤ N ≤ 31``\ ）的内容。
* ``show fps`` — 打印全部 32 个浮点寄存器（``F0``\ –\ ``F31``\ ）。
* ``show fp N`` — 打印浮点寄存器 ``N``\ （\ ``0 ≤ N ≤ 31``\ ）的内容。
* ``show fcsr`` — 打印浮点控制与状态寄存器（FCSR）。
* ``show hi`` / ``show lo`` — 打印用于乘除指令的特殊寄存器 ``HI``
  和 ``LO`` 的内容。
* ``show memory`` — 打印模拟主存的内容。
* ``show symbols`` — 打印符号表（``.data`` 与 ``.code`` 段中声明的
  标签及其地址）。
* ``show pipeline`` — 打印当前流水线各阶段中正在执行的指令。

不带子命令调用 ``show`` 时，会打印可用子命令的列表。

dinero
~~~~~~
将 Dinero 跟踪文件写入文件::

    > dinero trace.xdin

这会按照 Dinero IV 缓存模拟器期望的格式生成程序所执行的内存访问的
文本跟踪，可用于离线缓存分析。``dinero`` 可在执行的任意时刻调用；
跟踪文件反映迄今为止观察到的访问。

config
~~~~~~
打印当前的配置值::

    > config

这是桌面 GUI 在 *Settings* 对话框中展示的相同一组首选项（forwarding
开/关、跟踪文件路径、缓存参数、行为选项 …）。它是只读的 —— shell 仅
打印当前值并退出该命令。这些值来自与 GUI 共享的配置存储，因此 GUI
中所做的更改对 shell 可见，反之亦然。

reset
~~~~~
重置 CPU 状态机::

    > reset

``reset`` 会重新初始化内存、寄存器、符号表、I/O 管理器、缓存模拟器和
解析器，将 CPU 恢复到 ``READY`` 状态。在加载完一个程序之后想要加载
另一个程序，或在部分运行后想要从头重新开始时，请使用 ``reset``\ 。

help
~~~~
显示可用命令的列表以及每个命令的简短描述。``help`` 是探索 shell 功能
的入口；将其与每个命令的 ``-h`` / ``--help`` 选项结合使用，可以查看
各子命令所接受的参数。

exit
~~~~
退出 shell 并终止 JVM。在标准输入上发送 ``Ctrl+D``\ （EOF）也可达到
相同效果。

一次典型的会话
--------------
将这些命令组合在一起，一次典型的交互式会话看起来如下::

    $ java -jar edumips64.jar --headless --verbose
    > load examples/hello.s
    File loaded: /…/examples/hello.s
    > step 5
    … （5 个周期的流水线内容）
    > run
    Hello, world!
    Execution finished in 42 cycles, 3 ms
    > show registers
    … (R0..R31)
    > show pipeline
    … （流水线最终状态）
    > dinero hello.xdin
    > reset
    > load examples/sum.s
    > run
    > exit

来自 ``SYSCALL 3`` 的标准输入直接从终端读取，因此请求用户输入的程序
可以在 shell 中透明地工作。

脚本化提示
----------
由于 shell 按行从标准输入读取命令，可以直接将脚本喂给它::

    $ java -jar edumips64.jar --headless < session.txt

例如，``session.txt`` 内容如下::

    load examples/hello.s
    run
    show registers
    exit

这将加载该程序、运行它、打印最终的寄存器堆并退出。结合 ``--verbose``
和系统 shell 的重定向，可以非常方便地将 EduMIPS64 集成到自动化测试
套件中，或捕获可复现的执行轨迹。
