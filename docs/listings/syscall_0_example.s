; Example for SYSCALL 0

        .code
        daddi   r1, r0, 0    ;salva 0 nel registro R1
        syscall 0            ;esce dal programma
