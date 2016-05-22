.code

ADDI r1, r0, 1
ADDI r2, r0, 1
ADDI r4, r0, 2
DADDU r3, r1, r2
BEQ r4, r3, exit
BREAK

exit: SYSCALL 0

