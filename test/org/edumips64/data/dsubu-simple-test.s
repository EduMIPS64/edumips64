.code

ADDI r1, r0, 2
ADDI r2, r0, 1
DSUBU r3, r1, r2
BEQ r2, r3, exit
BREAK

exit: SYSCALL 0