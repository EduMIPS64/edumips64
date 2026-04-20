; branch-delay-slot-disabled.s - test file for EduMIPS64.
;
; Tests that with the branch delay slot disabled (branch-not-taken prediction,
; the default behaviour), the instruction immediately following a taken branch
; is flushed from the pipeline. The test raises BREAK (which fails the test)
; if the would-be delay slot instruction was executed anyway.
;
; (c) 2024 EduMIPS64 team
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
            daddi   r1, r0, 10
            j       after
            daddi   r1, r0, 42      ; must be flushed when delay slot is disabled
            daddi   r1, r0, 99      ; unreachable when the jump is taken
after:
            daddi   r2, r0, 10
            beq     r1, r2, ok      ; r1 must still be 10
            nop                     ; delay slot / filler (no side effect)
            break                   ; assertion failed: r1 was modified
ok:
            nop
            syscall 0
