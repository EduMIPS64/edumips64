.code

        daddi   r1, r0, 5
        daddi   r2, r0, 2
        divu    r1, r2
        mflo    r3
        mfhi    r4

        daddi   r5, r0, 1
        bne     r3, r2, error
        bne     r4, r5, error
        syscall 0

error:  break
        syscall 0
