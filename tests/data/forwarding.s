; Tests for the forwarding feature.
;
; The expected number of execution cycles are:
; * without forwarding: 19
; * with forwarding: 16

    .code
    dadd    r1, r2, r3
    dsub    r4, r1, r5
    nop
    nop
    nop
    nop
    nop
    lw      r1, 0(r0)
    dsub    r2, r1, r5
    syscall 0
