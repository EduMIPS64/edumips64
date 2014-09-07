.code
          daddi   r2, r0, 2
start:    daddi   r2, r2, -1
          bne     r2, r0, start
          syscall 0
