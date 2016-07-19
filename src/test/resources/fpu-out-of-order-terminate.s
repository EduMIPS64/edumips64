; In this code, the SYSCALL instruction will terminate before
; ADD.D, as the former will go through the integer pipeline,
; while the latter goes through the FP adder.
;
; The external test driver should verify that f2 contains the
; number 2.
.code
DADDI   r1, r0, 1
DMTC1   r1, f1
ADD.D   f2, f1, f1
SYSCALL 0
