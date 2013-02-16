; store-after-load.s - functional check for load/store instructions
; (c) 2012 Andrea Spadaccini, licensed under the GNU GPL v2 or later
; Checks whether the data dependency in a load/store cycle is honored.
; Executes a break instruction if the test fails
        .data
src:    .byte 10
dst:    .byte 0

        .code
        lb      r1, src(r0)
        sb      r1, dst(r0)
        addi    r2, r0, 10
        lb      r3, dst(r0)
        dsub    r4, r3, r2
        bnez    r4, fail
        syscall 0

fail:   break
