.code
       DADDI   r1, r0, 4
       DADDI   r2, r0, 2
       DMTC1   r1, f1
       DMTC1   r2, f2
       CVT.D.L f1, f1
       CVT.D.L f2, f2
       DIV.D   f3, f1, f2
       CVT.L.D f3, f3
       DMFC1   r3, f3
       BNE     r3, r2, error
       SYSCALL 0

error: BREAK
       SYSCALL 0
