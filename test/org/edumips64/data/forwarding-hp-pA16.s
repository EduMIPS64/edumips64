; Forwarding example taken from:
; Hennessy, Patterson, "Computer Architecture: A Quantitative Approach"
; Appendix A, page 16
;
; This code should run in 11 cycles with forwarding enabled (no stalls) and in
; 13 cycles with no forwarding (DSUB will stall in ID for 2 cycles).

.code
DADD  R1,R2,R3
DSUB  R4,R1,R5
AND   R6,R1,R7
OR    R8,R1,R9
XOR   R10,R1,R11
