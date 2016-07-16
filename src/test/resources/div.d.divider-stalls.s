.code
       DADDI   r1, r0, 4
       DADDI   r2, r0, 2
       DMTC1   r1, f1
       DMTC1   r2, f2
       CVT.D.L f1, f1
       CVT.D.L f2, f2
       ; Two DIV.D to cause divider stalls.
       DIV.D   f3, f1, f2
       DIV.D   f4, f1, f2
       CVT.L.D f4, f4
       DMFC1   r4, f4
       BNE     r4, r2, error
       SYSCALL 0

error: BREAK
       SYSCALL 0
