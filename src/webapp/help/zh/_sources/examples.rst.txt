代码示例
=============
在本章中，您将看到一些对理解 EduMIPS64 如何工作非常有用的示例列表，以了解 EduMIPS64 如何工作。

SYSCALL
-------

重要的是要了解 SYSCALL 1-4 的示例引用了`print.s` 文件，这是 SYSCALL 5 的示例。如果要运行
示例，应将该示例的内容复制到一个名为`print.s` 的文件，并将其包含在代码中。

有些示例会使用已存在的文件描述符，即使它并不真正存在。如果要运行这些示例，请使用 SYSCALL 1 示例打开一个文件。

SYSCALL 0
~~~~~~~~~
调用 SYSCALL 0 时，程序停止执行。
示例::

  .code
      daddi   r1, r0, 0    ; saves 0 in R1
      syscall 0            ; exits

SYSCALL 1
~~~~~~~~~
打开文件的示例程序::

                  .data
  error_op:       .asciiz     "Error opening the file"
  ok_message:     .asciiz     "All right"
  params_sys1:    .asciiz     "filename.txt"
                  .word64     0xF

                  .text
  open:           daddi       r14, r0, params_sys1
                  syscall     1
                  daddi       $s0, r0, -1
                  dadd        $s2, r0, r1
                  daddi       $a0,r0,ok_message
                  bne         r1,$s0,end
                  daddi       $a0,r0,error_op

  end:            jal         print_string
                  syscall 0

                  #include    print.s

在前两行中，我们将包含错误信息和成功信息的字符串写入内存，并将其传递给 print_string 函数。
给它们加上两个标签。print_string 函数包含在 print.s 文件中。

接下来，我们向内存写入 SYSCALL 1 所需的数据（第 4、5 行）、要打开的文件的路径（第 6、7 行
要打开的文件的路径（如果我们使用 读取 或 读/写模式），并在下一个内存单元中写入一个定义打开模式的整数。

.. 有关文件打开方式的更多信息，请参阅 \ref{sys1}。

在本例中，文件使用以下模式打开：
| `o_rdwr` | `o_creat` | `_append`。数字
数字 15（0xF，以 16 为基数）来自这三种模式的值之和（3 + 4 + 8）。

我们给这些数据加上一个标签，以便以后使用。

在 .text 部分，我们将 params_sys1 的地址（对编译器来说是一个数字）保存在寄存器中。
在 .text 部分，我们将 params_sys1 的地址（对编译器来说是一个数字）保存在寄存器 r14 中；接下来我们可以调用 SYSCALL 1 并将 r1 的内容保存在 $sys1 中。
将 r1 的内容保存在 $s2 中，这样我们就可以在程序的其余部分中使用它了
（例如，使用其他 SYSCALL）。

然后调用 print_string 函数，将 error_op 作为参数传递，如果
r1等于-1（第 13-14 行），否则将 ok_message 作为参数（第 12 和 14 行）。
则将 ok_message 作为参数（第 12 和 16 行）。

SYSCALL 2
~~~~~~~~~
关闭文件的示例程序::

                  .data
  params_sys2:    .space 8
  error_cl:       .asciiz     "Error closing the file"
  ok_message:     .asciiz     "All right"

                  .text
  close:          daddi       r14, r0, params_sys2
                  sw          $s2, params_sys2(r0)
                  syscall     2
                  daddi       $s0, r0, -1
                  daddi       $a0, r0, ok_message
                  bne         r1, $s0, end
                  daddi       $a0, r0, error_cl

  end:            jal         print_string
                  syscall     0

                  #include    print.s

首先，我们为 SYSCALL 2 的唯一参数，即必须关闭的文件的文件描述符（第 2 行）保存一些内存，并给它一个标签，以便以后访问。

接下来，我们将包含错误信息和成功信息的字符串放入内存，这些字符串将传递给 print_string 函数（第 3、4 行）。

在 .text 部分，我们将 params_sys2 的地址保存在 r14 中；然后我们就可以 调用 SYSCALL 2。

现在我们使用 error_cl 作为参数调用 print_string 函数，如果 r1
则调用 print_string 函数（第 13 行），如果一切顺利，则使用 ok_message 作为参数调用 print_string 函数（第 11 行）。则使用 ok_message 作为参数调用该函数（第 11 行）。

**注：** 此列表需要寄存器 $s2 包含要调用的文件的文件描述符。文件描述符。

SYSCALL 3
~~~~~~~~~
从文件读取 16 个字节并保存到内存的示例程序::

                  .data
  params_sys3:    .space      8
  ind_value:      .space      8
                  .word64     16
  error_3:        .asciiz     "Error while reading from file"
  ok_message:     .asciiz     "All right"

  value:          .space      30

                  .text
  read:           daddi       r14, r0, params_sys3
                  sw          $s2, params_sys3(r0)
                  daddi       $s1, r0, value
                  sw          $s1, ind_value(r0)
                  syscall     3
                  daddi       $s0, r0, -1
                  daddi       $a0, r0,ok_message
                  bne         r1, $s0,end
                  daddi       $a0, r0,error_3

  end:            jal         print_string
                  syscall     0

                  #include    print.s

.data 部分的前 4 行包含 SYSCALL 3 的参数、我们必须从中读取的文件描述符、SYSCALL 必须保存读取数据的内存地址以及要读取的字节数。我们给那些稍后必须访问的参数加上标签。接下来，像往常一样，我们将包含错误信息和成功信息的字符串放入其中。

在 .text 部分，我们将 params_sys3 的地址保存到寄存器 r14 中，并在 SYSCALL 参数的内存单元中保存文件描述符（我们假设保存在 $s2 中）和用于保存读取字节的地址。

接下来，我们可以调用 SYSCALL 3，然后调用 print_string 函数
根据操作成功与否，将 error_3 或 ok_message 作为参数传递。

SYSCALL 4
~~~~~~~~~
向文件写入字符串的示例程序::

                  .data
  params_sys4:    .space      8
  ind_value:      .space      8
                  .word64     16
  error_4:        .asciiz     "Error writing to file"
  ok_message:     .asciiz     "All right"
  value:          .space      30

                  .text

  write:          daddi       r14, r0,params_sys4
                  sw          $s2, params_sys4(r0)
                  daddi       $s1, r0,value
                  sw          $s1, ind_value(r0)
                  syscall     4
                  daddi       $s0, r0,-1
                  daddi       $a0, r0,ok_message
                  bne         r1, $s0,end
                  daddi       $a0, r0,error_4

  end:            jal         print_string
                  syscall     0

                  #include    print.s

.data 部分的前 4 行包含 SYSCALL 4 的参数、我们必须读取的文件描述符、SYSCALL 必须读取的内存地址、要写入的字节数。我们给那些稍后必须访问的参数加上标签。接下来，像往常一样，我们将包含错误信息和成功信息的字符串放入其中。

在 .text 部分，我们将 params_sys4 的地址保存到寄存器 r14 中，在 SYSCALL 参数的内存单元中保存文件描述符（我们假设保存在 $s2 中）和我们必须读取写入字节的地址。

接下来我们可以调用 SYSCALL 3，然后根据操作的成功与否调用 print_string 函数，参数为 error_3 或 ok_message。


SYSCALL 5
~~~~~~~~~
包含将 $a0 中的字符串打印到标准输出的函数的示例程序::

                  .data
  params_sys5:    .space  8

                  .text
  print_string:
                  sw      $a0, params_sys5(r0)
                  daddi   r14, r0, params_sys5
                  syscall 5
                  jr      r31

第二行用于为 SYSCALL 必须打印的字符串保存空间，由 .text 部分的第一条指令填充，该指令假定 \$a0 中有要打印的字符串地址。

下一条指令将字符串的地址放入 r14，然后我们就可以调用 SYSCALL 5 打印字符串了。最后一条指令将程序计数器设置为 r31 中的内容，这是 MIPS 通常的调用习惯。

一个更复杂的 SYSCALL 5 使用示例
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
SYSCALL 5 使用了一种并不简单的参数传递机制，下面的示例将对此进行说明::

                  .data
  format_str:     .asciiz   "%dth of %s:\n%s version %i.%i is being tested!"
  s1:             .asciiz   "June"
  s2:             .asciiz   "EduMIPS64"
  fs_addr:        .space    4
                  .word     5
  s1_addr:        .space    4
  s2_addr:        .space    4
                  .word     0
                  .word     5
  test:
                  .code
                  daddi     r5, r0, format_str
                  sw        r5, fs_addr(r0)
                  daddi     r2, r0, s1
                  daddi     r3, r0, s2
                  sd        r2, s1_addr(r0)
                  sd        r3, s2_addr(r0)
                  daddi     r14, r0, fs_addr
                  syscall   5
                  syscall   0

格式字符串的地址被放入 R5，其内容随后被保存到内存中的 fs_addr 地址。字符串参数的地址被保存到 s1_addr 和 s2_addr。这两个字符串参数与格式字符串中的两个 %s 占位符相匹配。

从内存来看，与占位符相匹配的参数显然是紧跟在格式字符串地址之后存储的：数字与整数参数相匹配，而地址与字符串参数相匹配。在 s1_addr 和 s2_addr 位置，存放着我们要打印的两个字符串的地址，而不是 %s 占位符。

示例的执行将显示 SYSCALL 5 如何处理复杂的格式字符串，如存储在 format_str 中的字符串。
