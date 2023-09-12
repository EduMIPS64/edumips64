Code Examples
=============
In this chapter you'll find some sample listings that will be useful in
order to understand how EduMIPS64 works.

SYSCALL
-------

It's important to understand that examples for SYSCALL 1-4 refer to the
`print.s` file, that is the example for SYSCALL 5. If you want to run the
examples, you should copy the content of that example in a file named
`print.s` and include it in your code.

Some examples use an already existing file descriptor, even if it doesn't truly
exist. If you want to run those examples, use the SYSCALL 1 example to open a
file.

SYSCALL 0
~~~~~~~~~
When SYSCALL 0 is called, it stops the execution of the program.
Example::

  .code
      daddi   r1, r0, 0    ; saves 0 in R1
      syscall 0            ; exits

SYSCALL 1
~~~~~~~~~
Example program that opens a file::

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

In the first two rows we write to memory the strings containing the error
message and the success message that we will pass to print_string function, and
we give them two labels. The print_string function is included in the print.s
file.

Next, we write to memory the data required from SYSCALL 1 (row 4, 5), the
path of the file to be opened (that must exist if we work in read or
read/write mode) and, in the next memory cell, an integer that defines the
opening mode.

.. For more info about the opening mode of a file, please refer to \ref{sys1}.

In this example, the file was opened using the following modes:
`O_RDWR` | `O_CREAT` | `O_APPEND`. The
number 15 (0xF in base 16) comes from the sum of the values of these three
modes (3 + 4 + 8).

We give a label to this data so that we can use it later.

In the .text section, we save the address of params_sys1 (that for the
compiler is a number) in register r14; next we can call SYSCALL 1 and save
the content of r1 in $s2, so that we can use it in the rest of the program
(for instance, with other SYSCALL).

Then the print_string function is called, passing error_op as an argument if
r1 is equal to -1 (rows 13-14) or else passing ok_message as an argument if
everything went smoothly (rows 12 and 16).

SYSCALL 2
~~~~~~~~~
Example program that closes a file::

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

First we save some memory for the only argument of SYSCALL 2, the file
descriptor of the file that must be closed (row 2), and we give it a label so
that we can access it later.

Next we put in memory the strings containing the error message and the success
message, that will be passed to the print_string function (rows 3, 4).

In the .text section, we save the address of params_sys2 in r14; then we can
call SYSCALL 2.

Now we call the print_string function using error_cl as a parameter if r1
yields -1 (row 13), or we call it using ok_message as a parameter if all went
smoothly (row 11).

**Note:** This listing needs that registry $s2 contains the
file descriptor of the file to use.

SYSCALL 3
~~~~~~~~~
Example program that reads 16 bytes from a file and saves them to memory::

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

The first 4 rows of the .data section contain the arguments of SYSCALL 3, the
file descriptor of the from which we must read, the memory address where the
SYSCALL must save the read data, the number of bytes to read. We give labels
to those parameters that must be accessed later. Next we put, as usual, the
strings containing the error message and the success message.

In the .text section, we save the params_sys3 address to register r14, we save
in the memory cells for the SYSCALL parameters the file descriptor (that we
suppose to have in $s2) and the address that we want to use to save the read
bytes.

Next we can call SYSCALL 3, and then we call the print_string function
passing as argument error_3 or ok_message, according to the success of the
operation.

SYSCALL 4
~~~~~~~~~
Example program that writes to a file a string::

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

The first 4 rows of the .data section contain the arguments of SYSCALL 4, the
file descriptor of the from which we must read, the memory address from where
the SYSCALL must read the bytes to write, the number of bytes to write. We
give labels to those parameters that must be accessed later. Next we put, as
usual, the strings containing the error message and the success message.

In the .text section, we save the params_sys4 address to register r14, we save
in the memory cells for the SYSCALL parameters the file descriptor (that we
suppose to have in $s2) and the address from where we must take the bytes to
write.

Next we can call SYSCALL 3, and then we call the print_string function
passing as argument error_3 or ok_message, according to the success of the
operation.

SYSCALL 5
~~~~~~~~~
Example program that contains a function that prints to standard output the
string contained in $a0::

                  .data
  params_sys5:    .space  8

                  .text
  print_string:
                  sw      $a0, params_sys5(r0)
                  daddi   r14, r0, params_sys5
                  syscall 5
                  jr      r31

The second row is used to save space for the string that must be printed by the
SYSCALL, that is filled by the first instruction of the .text section, that
assumes that in \$a0 there's the address of the string to be printed.

The next instruction puts in r14 the address of this string, and then we can
call SYSCALL 5 and print the string. The last instruction sets the program
counter to the content of r31, as the usual MIPS calling convention states.

A more complex usage example of SYSCALL 5
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
SYSCALL 5 uses a not-so-simple arguments passing mechanism, that will be
shown in the following example::

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

The address of the format string is put into R5, whose content is then saved to
memory at address fs_addr. The string parameters' addresses are saved into
s1_addr and s2_addr. Those two string parameters are the ones that match the
two %s placeholders in the format string.

Looking at the memory, it's obvious that the parameters matching the
placeholders are stored immediately after the address of the format string:
numbers match integer parameters, while addresses match string parameters. In
the s1_addr and s2_addr locations there are the addresses of the two strings
that we want to print instead of the %s placeholders.

The execution of the example will show how SYSCALL 5 can handle complex format
strings like the one stored at format_str.
