; delay-slot-not-taken-loop.s - test file for EduMIPS64.
;
; Edge case (generalisation of delay-slot-not-taken.s): a loop in which
; the conditional branch is taken on every iteration except the last. The
; instruction immediately after the branch is *both* the slot AND the
; loop-exit fall-through; it must execute exactly once after the loop
; exits, regardless of whether the delay slot is enabled.
;
; (c) 2026 EduMIPS64 project
;
; This file is part of the EduMIPS64 project, and is released under the GNU
; General Public License.
                .code
                daddi   r1, r0, 3           ; loop counter
                daddi   r2, r0, 0           ; running sum (tested at the end)
                daddi   r3, r0, 0           ; fall-through marker (tested at end)
loop:
                daddi   r2, r2, 1           ; loop body
                daddi   r1, r1, -1
                bnez    r1, loop            ; taken while r1 != 0
                daddi   r3, r3, 7           ; loop-exit fall-through / delay slot
                syscall 0
