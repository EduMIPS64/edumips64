; branch-delay-slot-enabled.s - test file for EduMIPS64.
;
; Tests that with the branch delay slot enabled, the instruction immediately
; following a taken branch is executed (i.e. it behaves as the delay slot).
; The test raises BREAK (which fails the test) if the delay slot instruction
; was skipped.
;
; (c) 2024 EduMIPS64 team
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
.code
            daddi   r1, r0, 0
            j       after
            daddi   r1, r0, 42      ; delay slot: must execute when enabled
            daddi   r1, r0, 99      ; must NOT execute (unreachable when the jump is taken)
after:
            daddi   r2, r0, 42
            beq     r1, r2, ok      ; r1 must equal 42
            nop                     ; delay slot / filler (no side effect)
            break                   ; assertion failed: r1 was not 42
ok:
            nop
            syscall 0
